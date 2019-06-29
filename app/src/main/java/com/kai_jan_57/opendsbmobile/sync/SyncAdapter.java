package com.kai_jan_57.opendsbmobile.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.account.Authenticator;
import com.kai_jan_57.opendsbmobile.activities.ContentViewerActivity;
import com.kai_jan_57.opendsbmobile.activities.MainActivity;
import com.kai_jan_57.opendsbmobile.database.AppDatabase;
import com.kai_jan_57.opendsbmobile.database.Login;
import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.network.FetchIndexRequestTask;
import com.kai_jan_57.opendsbmobile.utils.LogUtils;
import com.kai_jan_57.opendsbmobile.utils.NotificationUtils;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class SyncAdapter extends AbstractThreadedSyncAdapter implements FetchIndexRequestTask.FetchIndexEventListener, FetchIndexRequestTask.NodeUpdateEventListener {

    private final String TAG = getClass().getCanonicalName();

    private final AccountManager mAccountManager;

    private Context mContext;

    private SyncResult mSyncResult;

    private Object mResult;

    private boolean mClearNotifications;

    private String mAccountAlias;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
        mContext = context;
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        /*try {
            mAccountManager.invalidateAuthToken(Authenticator.ACCOUNT_TYPE, mAccountManager.blockingGetAuthToken(account, Authenticator.AUTHTOKEN_TYPE, true));
        } catch (AuthenticatorException pE) {
            syncResult.stats.numAuthExceptions ++;
        } catch (IOException pE) {
            syncResult.stats.numIoExceptions ++;
        } catch (OperationCanceledException pE) {
            Log.e(TAG, LogUtils.getStackTrace(pE));
        }*/
        NotificationUtils.createNotificationChannel(mContext);
        mClearNotifications = true;
        mSyncResult = syncResult;
        Login.Dao loginDao = AppDatabase.getInstance(mContext).getLoginDao();
        Login login = loginDao.getLoginByName(account.name);
        if (login == null) {
            login = new Login(account.name, account.name);
            login.mId = loginDao.addLogin(login);
        }
        if (login.mAlias.isEmpty()) {
            mAccountAlias = login.mName;
        } else {
            mAccountAlias = login.mAlias;
        }
        FetchIndexRequestTask fetchIndexRequestTask = new FetchIndexRequestTask(this, login, account.name, mAccountManager.getPassword(account));
        fetchIndexRequestTask.setNodeUpdateEventListener(this);
        fetchIndexRequestTask.execute();
        try {
            fetchIndexRequestTask.get(1, TimeUnit.MINUTES);
        } catch (ExecutionException pE) {
            mSyncResult.stats.numIoExceptions++;
            Log.e(TAG, LogUtils.getStackTrace(pE));
        } catch (InterruptedException pE) {
            mSyncResult.stats.numIoExceptions++;
            Log.e(TAG, LogUtils.getStackTrace(pE));
        } catch (TimeoutException pE) {
            mSyncResult.stats.numIoExceptions++;
            Log.e(TAG, LogUtils.getStackTrace(pE));
        }
    }

    @Override
    public void onException(Exception exception) {
        Log.e(TAG, LogUtils.getStackTrace(exception));
        if (mResult == null) {
            mResult = exception;
        }
    }

    @Override
    public void onFail(FetchIndexRequestTask.LoginResult loginResult, String mandantId) {
        mResult = loginResult;
        mSyncResult.stats.numAuthExceptions++;
        // TODO: show reauth notification
    }

    @Override
    public void onNodeUpdated(Node node, FetchIndexRequestTask.UpdateType updateType) {
        // TODO: preview in notification (plus settings) using bigpicturestylenotification; notification bundling
        if (mClearNotifications) {
            mClearNotifications = false;
            //NotificationUtils.clearNotifications();
        }
        Node parentNode = node;
        if (node.mParentId != null) {
            parentNode = AppDatabase.getInstance(mContext).getNodeDao().getNodeById(node.mParentId);
            if (parentNode == null) {
                parentNode = node;
            }
        }

        if (!AppDatabase.getInstance(mContext).getLoginDao().getLoginById(parentNode.mLoginId).mNotificationsEnabled) {
            return;
        }
        Intent contentViewerActivity = new Intent(mContext, MainActivity.class);
        contentViewerActivity.putExtra(MainActivity.LOGIN_ID, AppDatabase.getInstance(mContext).getLoginDao().getLoginById(parentNode.mLoginId).mName);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, contentViewerActivity, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notification = new Notification.Builder(mContext)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(new Notification.BigTextStyle()
                        .setSummaryText(mAccountAlias)
                        .bigText(node.mContent)
                        .setBigContentTitle(parentNode.mTitle)
                )
                /*.setStyle(new Notification.Style() {
                    Notification.Style setSummary(CharSequence summary) {
                        internalSetSummaryText(summary);
                        return this;
                    }
                }.setSummary(mAccountAlias))*/
                .setContentIntent(pendingIntent)
                .setWhen(node.mDate == null? new Date().getTime() : node.mDate.getTime())
                .setShowWhen(true)
                .setContentText(parentNode.mTitle);
        switch (updateType) {
            // TODO: settings; which notifications to show etc.
            case NEW: {
                mSyncResult.stats.numInserts++;
                notification.setContentTitle(mContext.getString(R.string.notification_content_new));
                break;
            }
            case NOT_CACHED: {
                mSyncResult.stats.numSkippedEntries++;
                //notification.setContentTitle(mContext.getString(R.string.notification_content_new));
                return;
            }
            case CACHE_DEPRECATED: {
                mSyncResult.stats.numUpdates++;
                notification.setContentTitle(mContext.getString(R.string.notification_content_updated));
                break;
            }
        }
        NotificationUtils.postNotification(mContext, notification);
    }

    @Override
    public void onSuccess(long loginId, boolean cached) {
        // we are only interested in updates, so we don't care about success
    }

    @Override
    public void onProgress(int progress) {
    }
}
