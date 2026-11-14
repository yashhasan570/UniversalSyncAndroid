package com.universalsync.android;

import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import com.universalsync.android.service.NotificationHandler;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class Universal SyncModule {

    private final Universal SyncApp mApp;

    public Universal SyncModule(Universal SyncApp app) {
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
