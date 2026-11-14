package com.universalsync.android.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowInsetsControllerCompat;

import com.universalsync.android.R;
import com.universalsync.android.service.RestApi;
import com.universalsync.android.service.UniversalSyncService;
import com.universalsync.android.service.UniversalSyncServiceBinder;

/**
 * Connects to {@link UniversalSyncService} and provides access to it.
 */
public abstract class UniversalSyncActivity extends ThemedAppCompatActivity implements ServiceConnection {

    private static final String TAG = "UniversalSyncActivity";

    private UniversalSyncService mUniversalSyncService;

    /**
     * Look for a Toolbar in the layout and bind it as the activity's actionbar with reasonable
     * defaults.
     *
     * The Toolbar must exist in the content view and have an id of R.id.toolbar. Trying to call
     * getSupportActionBar before this Activity's onPostCreate will cause a crash.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onPostCreateSetToolbar();
    }

    private void onPostCreateSetToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        toolbar.setNavigationContentDescription(R.string.main_menu);
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        toolbar.setTouchscreenBlocksFocus(false);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
        }
    }

    @Override
    protected void onPause() {
        unbindService(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, UniversalSyncService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        UniversalSyncServiceBinder universalsyncServiceBinder = (UniversalSyncServiceBinder) iBinder;
        mUniversalSyncService = universalsyncServiceBinder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mUniversalSyncService = null;
    }

    /**
     * Returns service object (or null if not bound).
     */
    public UniversalSyncService getService() {
        return mUniversalSyncService;
    }

    /**
     * Returns RestApi instance, or null if UniversalSyncService is not yet connected.
     */
    public RestApi getApi() {
        return (getService() != null)
                ? getService().getApi()
                : null;
    }
}
