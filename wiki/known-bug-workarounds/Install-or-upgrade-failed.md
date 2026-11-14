Errors covered by this article:
```
INSTALL_FAILED_DUPLICATE_PERMISSION
(appName) conflicts with another package already installed
```

This error may occur on some Android systems while trying to update the app to v1.29.6.5 or higher.

Workaround:
- Open the Universal Sync-Fork menu -> Import & Export -> Export config
- Open the Universal Sync-Fork menu -> Exit
- Android settings -> Apps -> Universal Sync-Fork -> Force stop
- Android settings -> Apps -> Universal Sync-Fork -> Uninstall, do NOT keep data when Android asks (this refers to the app's config, not your synced folders)
- Install latest release
- Open the Universal Sync-Fork menu -> Import & Export -> Import config

If that didn't solve the problem, you could try this workaround:
- Open the Universal Sync-Fork menu -> Import & Export -> Export config
- Open the Universal Sync-Fork menu -> Exit
- Connect your phone to a computer
- Phone
  - Android settings -> Enable Developer Options
  - Android settings -> Developer Options -> Enable USB Debugging
- Computer
  - Install ADB
  - Open command line to completely uninstall the app
```
adb uninstall com.github.universalsync.syncthingandroid
adb uninstall com.github.universalsync.syncthingandroid.debug
adb uninstall com.universalsync.android
adb uninstall com.universalsync.android.debug
```
- Phone
  - Install latest release
  - Open the Universal Sync-Fork menu -> Import & Export -> Import config
