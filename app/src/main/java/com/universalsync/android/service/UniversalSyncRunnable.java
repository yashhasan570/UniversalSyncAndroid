package com.universalsync.android.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.RouteInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.universalsync.android.R;
import com.universalsync.android.UniversalSyncApp;
import com.universalsync.android.util.ConfigXml;
import com.universalsync.android.util.FileUtils;
import com.universalsync.android.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import static com.universalsync.android.service.UniversalSyncService.EXTRA_STOP_AFTER_CRASHED_NATIVE;

/**
 * Runs the universalsync binary from command line, and prints its output to logcat.
 *
 * @see <a href="http://docs.universalsync.net/users/universalsync.html">Command Line Docs</a>
 */
public class UniversalSyncRunnable implements Runnable {

    private static final String TAG = "UniversalSyncRunnable";
    private static final String TAG_NATIVE = "UniversalSyncNativeCode";
    private static final String TAG_NICE = "UniversalSyncRunnableIoNice";

    private Boolean ENABLE_VERBOSE_LOG = false;
    private static final int LOG_FILE_MAX_LINES = 200000;
    private static final int LOG_FILE_BUFFER_SIZE = 1024 * 1024;

    private static final AtomicReference<Process> mUniversal Sync = new AtomicReference<>();
    private final Context mContext;
    private final File mUniversalSyncBinary;
    private String[] mCommand;
    private final File mUniversalSyncLogFile;

    @Inject
    SharedPreferences mPreferences;

    @Inject
    NotificationHandler mNotificationHandler;

    public enum Command {
        deviceid,           // Output the device ID to the command line.
        generate,           // Generate keys, a config file and immediately exit.
        main,               // Run the main Universal Sync application.
        resetdatabase,      // Reset Universal Sync's database
        resetdeltas,        // Reset Universal Sync's delta indexes
    }

    /**
     * Constructs instance.
     *
     * @param command Which type of Universal Sync command to execute.
     */
    public UniversalSyncRunnable(Context context, Command command) {
        ((UniversalSyncApp) context.getApplicationContext()).component().inject(this);
        ENABLE_VERBOSE_LOG = AppPrefs.getPrefVerboseLog(mPreferences);
        mContext = context;
        // Example: mUniversalSyncBinary="/data/app/${applicationId}-8HsN-IsVtZXc8GrE5-Hepw==/lib/x86/"
        mUniversalSyncBinary = Constants.getUniversalSyncBinary(mContext);
        mUniversalSyncLogFile = Constants.getUniversalSyncLogFile(mContext);

        // Get preferences relevant to starting universalsync core.
        switch (command) {
            case deviceid:
                mCommand = new String[]{mUniversalSyncBinary.getPath(), "device-id"};
                break;
            case generate:
                mCommand = new String[]{mUniversalSyncBinary.getPath(), "generate"};
                break;
            case main:
                mCommand = new String[]{mUniversalSyncBinary.getPath(), "serve", "--no-browser"};
                break;
            case resetdatabase:
                mCommand = new String[]{mUniversalSyncBinary.getPath(), "debug", "reset-database"};
                break;
            case resetdeltas:
                mCommand = new String[]{mUniversalSyncBinary.getPath(), "serve", "--debug-reset-delta-idxs"};
                break;
            default:
                throw new InvalidParameterException("Unknown command option");
        }
    }

    @Override
    public void run() {
        try {
            run(false);
        } catch (ExecutableNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String run(boolean returnStdOut) throws ExecutableNotFoundException {
        Boolean sendStopToService = false;
        Boolean restartUniversalSyncNative = false;
        int exitCode;
        String capturedStdOut = "";

        // Trim Universal Sync log.
        trimUniversalSyncLogFile();

        MulticastLock multicastLock = null;
        Process process = null;
        try {
            // Android 11 blocks local discovery if we did not acquire MulticastLock.
            // The lock lets every broadcast and multicast packet on the network wake the
            // device, so only take it when local discovery is on.
            if (isLocalDiscoveryEnabled()) {
                WifiManager wifi = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                multicastLock = wifi.createMulticastLock("multicastLock");
                multicastLock.setReferenceCounted(true);
                multicastLock.acquire();
            } else {
                Log.i(TAG, "Local discovery is disabled, not acquiring MulticastLock.");
            }

            /**
             * Setup and run a new universalsync instance
             */
            HashMap<String, String> targetEnv = buildEnvironment();
            process = setupAndLaunch(targetEnv);

            mUniversal Sync.set(process);

            Thread lInfo = null;
            Thread lWarn = null;
            if (returnStdOut) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new InputStreamReader(process.getInputStream(), Charsets.UTF_8));
                    String line;
                    while ((line = br.readLine()) != null) {
                        Log.i(TAG_NATIVE, line);
                        capturedStdOut = capturedStdOut + line + "\n";
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Failed to read Universal Sync's command line output", e);
                } finally {
                    if (br != null)
                        br.close();
                }
            } else {
                lInfo = log(process.getInputStream(), Log.INFO);
                lWarn = log(process.getErrorStream(), Log.WARN);
            }

            exitCode = process.waitFor();
            LogV("Universal Sync exited with code " + exitCode);
            mUniversal Sync.set(null);
            if (lInfo != null) {
                lInfo.join();
            }
            if (lWarn != null) {
                lWarn.join();
            }

            switch (exitCode) {
                case 0:
                case 137:
                    Log.i(TAG, "Universal Sync was shut down normally via API or SIGKILL. Exit code = " + exitCode);
                    break;
                case 1:
                    Log.w(TAG, "exit reason = exitError. Another Universal Sync instance may be already running.");
                    mNotificationHandler.showCrashedNotification(R.string.notification_crash_title, Integer.toString(exitCode));
                    sendStopToService = true;
                    break;
                case 2:
                    // This should not happen as STNOUPGRADE is set.
                    Log.w(TAG, "exit reason = exitNoUpgradeAvailable. Another Universal Sync instance may be already running.");
                    mNotificationHandler.showCrashedNotification(R.string.notification_crash_title, Integer.toString(exitCode));
                    sendStopToService = true;
                    break;
                case 3:
                    // Restart was requested via Rest API call.
                    Log.i(TAG, "exit reason = exitRestarting. Restarting universalsync.");
                    restartUniversalSyncNative = true;
                    break;
                case 9:
                    // Native was force killed.
                    Log.w(TAG, "exit reason = exitForceKill.");
                    mNotificationHandler.showCrashedNotification(R.string.notification_crash_title, Integer.toString(exitCode));
                    sendStopToService = true;
                    break;
                case 64:
                    Log.w(TAG, "exit reason = exitInvalidCommandLine.");
                    mNotificationHandler.showCrashedNotification(R.string.notification_crash_title, Integer.toString(exitCode));
                    sendStopToService = true;
                    break;
                default:
                    Log.w(TAG, "Universal Sync exited unexpectedly. Exit code = " + exitCode);
                    mNotificationHandler.showCrashedNotification(R.string.notification_crash_title, Integer.toString(exitCode));
                    sendStopToService = true;
            }
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Failed to execute universalsync binary or read output", e);
        } finally {
            if (multicastLock != null) {
                multicastLock.release();
                multicastLock = null;
            }
            if (process != null) {
                process.destroy();
            }
        }

        // Restart universalsync if it exited unexpectedly while running on a separate thread.
        if (!returnStdOut && restartUniversalSyncNative) {
            mContext.startService(new Intent(mContext, UniversalSyncService.class)
                    .setAction(UniversalSyncService.ACTION_RESTART));
        }

        // Notify {@link UniversalSyncService} that service state State.ACTIVE is no longer valid.
        if (!returnStdOut && sendStopToService) {
            Intent intent = new Intent(mContext, UniversalSyncService.class);
            intent.setAction(UniversalSyncService.ACTION_STOP);
            intent.putExtra(EXTRA_STOP_AFTER_CRASHED_NATIVE, true);
            mContext.startService(intent);
        }

        // Return captured command line output.
        return capturedStdOut;
    }

    /**
     * Reads localAnnounceEnabled from config.xml. Falls back to true, which keeps the old
     * behaviour of always acquiring MulticastLock, if the config can't be read.
     */
    private boolean isLocalDiscoveryEnabled() {
        try {
            ConfigXml configXml = new ConfigXml(mContext);
            configXml.loadConfig();
            return configXml.getOptions().localAnnounceEnabled;
        } catch (Exception e) {
            Log.w(TAG, "isLocalDiscoveryEnabled: Failed to read config, assuming true", e);
            return true;
        }
    }

    private void putCustomEnvironmentVariables(Map<String, String> environment, SharedPreferences sp) {
        String customEnvironment = sp.getString(Constants.PREF_ENVIRONMENT_VARIABLES, null);
        if (TextUtils.isEmpty(customEnvironment))
            return;

        for (String e : customEnvironment.split(" ")) {
            String[] e2 = e.split("=", 2);
            LogV("Setting env var: [" + e2[0] + "]=[" + e2[1] + "]");
            environment.put(e2[0], e2[1]);
        }
    }

    /**
     * Logs the outputs of a stream to logcat and mNativeLog.
     *
     * @param is       The stream to log.
     * @param priority The priority level.
     * @param saveLog  True if the log should be stored to {@link #mUniversalSyncLogFile}.
     */
    private Thread log(final InputStream is, final int priority) {
        Thread t = new Thread(() -> {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    /*
                    if (ENABLE_VERBOSE_LOG) {
                        String lineWithoutTimestamp = line.replaceFirst("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2} ?", "");
                        Log.println(priority, TAG_NATIVE, lineWithoutTimestamp);
                    }
                    */
                    // Always output SynchtingNative's output to "universalsync.log".
                    Files.append(line + "\n", mUniversalSyncLogFile, Charsets.UTF_8);
                }
            } catch (IOException e) {
                Log.w(TAG, "Failed to read Universal Sync's command line output", e);
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.w(TAG, "log: Failed to close bufferedReader", e);
                }
            }
        });
        t.start();
        return t;
    }

    // If the nth last newline is found within this buffer, then the offset of that newline within
    // the buffer is returned. Otherwise, the negative of (nth - newlines consumed) is returned for
    // use with the new search.
    private static int findNthLastNewline(byte[] data, int size, int nth) {
        if (nth <= 0) {
            throw new IllegalArgumentException("nth must be positive: " + nth);
        }

        for (int i = size - 1; i >= 0; i--) {
            if (data[i] == '\n') {
                nth--;

                if (nth == 0) {
                    return i;
                }
            }
        }

        return -nth;
    }

    /**
     * Only keep last {@link #LOG_FILE_MAX_LINES} lines in log file, to avoid bloat.
     */
    private void trimUniversalSyncLogFile() {
        if (!mUniversalSyncLogFile.exists()) {
            return;
        }

        try (RandomAccessFile input = new RandomAccessFile(mUniversalSyncLogFile, "r")) {
            // Find the offset of the (n + 1)th newline with constant memory. The last n lines is
            // everything after that point. This will read in block-aligned chunks if
            // LOG_FILE_BUFFER_SIZE is a multiple of the filesystem block size.
            byte[] buf = new byte[LOG_FILE_BUFFER_SIZE];
            long length = input.length();
            long chunks = Math.ceilDiv(length, buf.length);
            int newlinesRemaining = LOG_FILE_MAX_LINES + 1;
            long truncationOffset = -1;

            for (long chunk = chunks - 1; chunk >= 0; chunk--) {
                long offset = buf.length * chunk;
                input.seek(offset);

                // Last chunk can be smaller than the whole buffer.
                int n = (int) Math.min(length - offset, buf.length);
                input.readFully(buf, 0, n);

                int ret = findNthLastNewline(buf, n, newlinesRemaining);
                if (ret >= 0) {
                    truncationOffset = offset + ret + 1;
                    break;
                } else {
                    newlinesRemaining = -ret;
                }
            }

            if (truncationOffset < 0) {
                // The file already contains fewer than maximum lines.
                return;
            }

            input.seek(truncationOffset);

            File tempFile = new File(mContext.getFilesDir().toString(), "universalsync.log.tmp");
            long remain = length - truncationOffset;

            try (FileOutputStream output = new FileOutputStream(tempFile)) {
                while (remain > 0) {
                    int n = (int) Math.min(remain, buf.length);

                    input.readFully(buf, 0, n);
                    output.write(buf, 0, n);

                    remain -= n;
                }
            }

            tempFile.renameTo(mUniversalSyncLogFile);
        } catch (IOException e) {
            Log.w(TAG, "Failed to trim log file", e);
        }
    }

    private HashMap<String, String> buildEnvironment() {
        HashMap<String, String> targetEnv = new HashMap<>();

        // Set home directory to data folder for web GUI folder picker.
        targetEnv.put("HOME", FileUtils.getUniversalSyncTildeAbsolutePath());

        // Set config, key and database directory.
        targetEnv.put("STHOMEDIR", mContext.getFilesDir().toString());
        targetEnv.put("STTRACE", TextUtils.join(" ",
                mPreferences.getStringSet(Constants.PREF_DEBUG_FACILITIES_ENABLED, new HashSet<>())));
        targetEnv.put("STMONITORED", "1");
        targetEnv.put("STNOUPGRADE", "1");
        targetEnv.put("STVERSIONEXTRA", mContext.getString(R.string.app_name));

        // Database tuning against slowness.
        targetEnv.put("SQLITE_TMPDIR", mContext.getCacheDir().getAbsolutePath());

        // Workaround UniversalSyncNativeCode denied to read gatewayIP by Android 14+ restriction.
        final String gatewayIpV4 = getGatewayIpV4(mContext);
        if (gatewayIpV4 != null) {
            targetEnv.put("FALLBACK_NET_GATEWAY_IPV4", gatewayIpV4);
        }

        if (mPreferences.getBoolean(Constants.PREF_USE_TOR, false)) {
            targetEnv.put("all_proxy", "socks5://localhost:9050");
            targetEnv.put("ALL_PROXY_NO_FALLBACK", "1");
        } else {
            String socksProxyAddress = mPreferences.getString(Constants.PREF_SOCKS_PROXY_ADDRESS, "");
            if (!socksProxyAddress.equals("")) {
                targetEnv.put("all_proxy", socksProxyAddress);
            }

            String httpProxyAddress = mPreferences.getString(Constants.PREF_HTTP_PROXY_ADDRESS, "");
            if (!httpProxyAddress.equals("")) {
                targetEnv.put("http_proxy", httpProxyAddress);
                targetEnv.put("https_proxy", httpProxyAddress);
            }
        }

        // Optimize memory usage for older devices.
        int gogc = 100;         // GO default
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            gogc = 75;
        }
        LogV("Setting env var: [GOGC]=[" + Integer.toString(gogc) + "]");
        targetEnv.put("GOGC", Integer.toString(gogc));

        putCustomEnvironmentVariables(targetEnv, mPreferences);
        return targetEnv;
    }

    private Process setupAndLaunch(HashMap<String, String> env) throws IOException, ExecutableNotFoundException {
        // Check if "" exists.
        if (mCommand.length > 0) {
            File libUniversal Sync = new File(mCommand[0]);
            if (!libUniversal Sync.exists()) {
                Log.e(TAG, "CRITICAL - Universal Sync core binary is missing in APK package location " + mCommand[0]);
                throw new ExecutableNotFoundException(mCommand[0]);
            }
        }
        ProcessBuilder pb = new ProcessBuilder(mCommand);
        pb.environment().putAll(env);
        return pb.start();
    }

    public class ExecutableNotFoundException extends Exception {

        public ExecutableNotFoundException(String message) {
            super(message);
        }

        public ExecutableNotFoundException(String message, Throwable throwable) {
            super(message, throwable);
        }

    }

    private void LogV(String logMessage) {
        if (ENABLE_VERBOSE_LOG) {
            Log.v(TAG, logMessage);
        }
    }

    public static String getGatewayIpV4(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork == null) return null;

        LinkProperties props = cm.getLinkProperties(activeNetwork);
        if (props == null) return null;

        for (RouteInfo route : props.getRoutes()) {
            InetAddress gateway = route.getGateway();
            if (route.isDefaultRoute() && gateway instanceof Inet4Address) {
                return gateway.getHostAddress();
            }
        }
        return null;
    }
}
