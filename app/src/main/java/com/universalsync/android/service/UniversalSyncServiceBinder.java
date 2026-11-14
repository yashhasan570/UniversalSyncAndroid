package com.universalsync.android.service;

import android.os.Binder;

public class UniversalSyncServiceBinder extends Binder {

    private final UniversalSyncService mService;

    public UniversalSyncServiceBinder(UniversalSyncService service) {
        mService = service;
    }

    public UniversalSyncService getService() {
        return mService;
    }

}
