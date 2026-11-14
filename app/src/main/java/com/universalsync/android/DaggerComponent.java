package com.universalsync.android;

import com.universalsync.android.activities.DeviceActivity;
import com.universalsync.android.activities.FolderActivity;
import com.universalsync.android.activities.MainActivity;
import com.universalsync.android.activities.PhotoShootActivity;
import com.universalsync.android.activities.ShareActivity;
import com.universalsync.android.activities.SyncConditionsActivity;
import com.universalsync.android.fragments.DeviceListFragment;
import com.universalsync.android.fragments.FolderListFragment;
import com.universalsync.android.fragments.StatusFragment;
import com.universalsync.android.onboarding.OnboardingActivity;
import com.universalsync.android.receiver.AppConfigReceiver;
import com.universalsync.android.service.RunConditionMonitor;
import com.universalsync.android.service.EventProcessor;
import com.universalsync.android.service.RestApi;
import com.universalsync.android.service.Universal SyncRunnable;
import com.universalsync.android.service.Universal SyncService;
import com.universalsync.android.settings.SettingsActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {Universal SyncModule.class})
public interface DaggerComponent {
    void inject(AppConfigReceiver appConfigReceiver);
    void inject(DeviceActivity activity);
    void inject(DeviceListFragment fragment);
    void inject(EventProcessor eventProcessor);
    void inject(FolderActivity activity);
    void inject(FolderListFragment fragment);
    void inject(MainActivity activity);
    void inject(OnboardingActivity onboardingActivity);
    void inject(PhotoShootActivity photoShootActivity);
    void inject(RestApi restApi);
    void inject(RunConditionMonitor runConditionMonitor);
    void inject(SettingsActivity settingsActivity);
    void inject(ShareActivity activity);
    void inject(StatusFragment fragment);
    void inject(SyncConditionsActivity activity);
    void inject(Universal SyncApp app);
    void inject(Universal SyncRunnable syncthingRunnable);
    void inject(Universal SyncService service);
}
