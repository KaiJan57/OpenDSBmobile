package com.kai_jan_57.opendsbmobile.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.kai_jan_57.opendsbmobile.Application;
import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.activities.LoginActivity;
import com.kai_jan_57.opendsbmobile.database.AppDatabase;
import com.kai_jan_57.opendsbmobile.database.Login;
import com.kai_jan_57.opendsbmobile.network.FetchIndexRequestTask;
import com.kai_jan_57.opendsbmobile.utils.FileUtils;
import com.kai_jan_57.opendsbmobile.utils.LogUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Authenticator extends AbstractAccountAuthenticator implements FetchIndexRequestTask.FetchIndexEventListener {

    private final String TAG = getClass().getCanonicalName();
    public static final String ACCOUNT_TYPE = "com.kai_jan_57.opendsbmobile.DSB";
    public static final String AUTHTOKEN_TYPE = "com.kai_jan_57.opendsbmobile.Password";
    private static final long TIMEOUT_SECONDS = 15;
    private final Context mContext;

    private Object mResult = null;

    Authenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        mResult = null;
        final AccountManager accountManager = AccountManager.get(mContext);
        String authToken = accountManager.peekAuthToken(account, authTokenType);

        Login.Dao loginDao = AppDatabase.getInstance(mContext).getLoginDao();
        Login login = loginDao.getLoginByName(account.name);
        if (login == null) {
            login = new Login(account.name, account.name);
            login.mId = loginDao.addLogin(login);
        }
        FetchIndexRequestTask fetchIndexRequestTask = new FetchIndexRequestTask(this, login, account.name, accountManager.getPassword(account));
        fetchIndexRequestTask.execute();
        try {
            fetchIndexRequestTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception pE) {
            Log.e(TAG, LogUtils.getStackTrace(pE));
            throw new NetworkErrorException(pE.getCause());
        }

        final Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        if (mResult instanceof FetchIndexRequestTask.LoginResult) {
            if (mResult == FetchIndexRequestTask.LoginResult.Login_Failed) {
                // password changed
                final Intent intent = new Intent(mContext, LoginActivity.class);
                intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
                intent.putExtra(LoginActivity.EXTRA_LOGIN_ID, account.name);

                bundle.clear();
                bundle.putParcelable(AccountManager.KEY_INTENT, intent);
                Toast.makeText(mContext, R.string.login_failed, Toast.LENGTH_LONG).show();
                return bundle;
            } else {
                // license expired
                Toast.makeText(mContext, R.string.dsb_license_expired, Toast.LENGTH_LONG).show();
            }
        }
        if (!authToken.isEmpty()) {
            // last resort on network problems: return old index
            bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return bundle;
        }
        if (mResult instanceof Exception) {
            throw new NetworkErrorException(((Exception) mResult).getLocalizedMessage());
        }
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }

    @Override
    public void onProgress(int progress) { }

    @Override
    public void onException(Exception exception) {
        mResult = exception;
    }

    @Override
    public void onSuccess(long loginId, boolean cached) {
        if (cached) {
            mResult = new Exception("");
        }
    }

    @Override
    public void onFail(FetchIndexRequestTask.LoginResult loginResult, String mandantId) {
        mResult = loginResult;
    }

    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
        // do cleanup
        Log.d("Authenticator", "Account removal pending, deleting account data...");
        Login.Dao loginDao = Application.getInstance().getDatabase().getLoginDao();
        long removalPending = loginDao.getLoginByName(account.name).mId;
        File loginDirectory = new File(Application.getInstance().getFilesDir(), String.valueOf(removalPending));
        if (loginDirectory.exists() && loginDirectory.setWritable(true)) {
            FileUtils.deleteRecursively(loginDirectory);
        }
        loginDao.deleteId(removalPending);
        return super.getAccountRemovalAllowed(response, account);
    }
}
