package com.nutomic.universalSyncandroid;

import com.nutomic.universalSyncandroid.activities.DeviceActivity;
import com.nutomic.universalSyncandroid.activities.FolderActivity;
import com.nutomic.universalSyncandroid.activities.MainActivity;
import com.nutomic.universalSyncandroid.activities.PhotoShootActivity;
import com.nutomic.universalSyncandroid.activities.ShareActivity;
import com.nutomic.universalSyncandroid.activities.SyncConditionsActivity;
import com.nutomic.universalSyncandroid.fragments.DeviceListFragment;
import com.nutomic.universalSyncandroid.fragments.FolderListFragment;
import com.nutomic.universalSyncandroid.fragments.StatusFragment;
import com.nutomic.universalSyncandroid.onboarding.OnboardingActivity;
import com.nutomic.universalSyncandroid.receiver.AppConfigReceiver;
import com.nutomic.universalSyncandroid.service.RunConditionMonitor;
import com.nutomic.universalSyncandroid.service.EventProcessor;
import com.nutomic.universalSyncandroid.service.RestApi;
import com.nutomic.universalSyncandroid.service.UniversalSyncRunnable;
import com.nutomic.universalSyncandroid.service.UniversalSyncService;
import com.nutomic.universalSyncandroid.settings.SettingsActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {UniversalSyncModule.class})
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
    void inject(UniversalSyncApp app);
    void inject(UniversalSyncRunnable universalSyncRunnable);
    void inject(UniversalSyncService service);
}
