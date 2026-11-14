package com.universalsync.android.service;

import android.os.Binder;

public class Universal SyncServiceBinder extends Binder {

    private final Universal SyncService mService;

    public Universal SyncServiceBinder(Universal SyncService service) {
        mService = service;
    }

    public Universal SyncService getService() {
        return mService;
    }

}
