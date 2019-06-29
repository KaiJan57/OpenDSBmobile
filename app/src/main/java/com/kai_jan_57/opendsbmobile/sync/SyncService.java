package com.kai_jan_57.opendsbmobile.sync;

import android.accounts.Account;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.Application;

public class SyncService extends Service {

    private static SyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    private ContentResolver mContentResolver;

    @Override
    public void onCreate() {
        getSyncAdapter();
        mContentResolver = getApplicationContext().getContentResolver();
        /*mContentResolver.registerContentObserver(new Uri(), true, new ContentObserver() {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }
        });*/
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return getSyncAdapter().getSyncAdapterBinder();
    }

    private SyncAdapter getSyncAdapter() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
            return sSyncAdapter;
        }
    }

    public static void requestSync(Account account) {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account, Application.getInstance().getResources().getString(R.string.content_authority), settingsBundle);
    }
}
