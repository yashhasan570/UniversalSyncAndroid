package com.nutomic.universalSyncandroid;

import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import com.nutomic.universalSyncandroid.service.NotificationHandler;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class UniversalSyncModule {

    private final UniversalSyncApp mApp;

    public UniversalSyncModule(UniversalSyncApp app) {
        mApp = app;
    }

    @Provides
    @Singleton
    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApp);
    }

    @Provides
    @Singleton
    public NotificationHandler getNotificationHandler(SharedPreferences preferences) {
        return new NotificationHandler(mApp, preferences);
    }
}
