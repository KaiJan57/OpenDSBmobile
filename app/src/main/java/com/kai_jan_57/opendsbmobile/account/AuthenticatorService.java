package com.kai_jan_57.opendsbmobile.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.kai_jan_57.opendsbmobile.Application;
import com.kai_jan_57.opendsbmobile.database.AppDatabase;
import com.kai_jan_57.opendsbmobile.database.Login;
import com.kai_jan_57.opendsbmobile.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AuthenticatorService extends Service implements OnAccountsUpdateListener {

    private AccountManager mAccountManager;

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        List<String> availableLoginIds = new ArrayList<>();
        for (Account account : accounts) {
            if (account.type.equals(Authenticator.ACCOUNT_TYPE)) {
                availableLoginIds.add(account.name);
            }
        }
        if (availableLoginIds.isEmpty()) {
            FileUtils.deleteRecursively(getFilesDir());
            AppDatabase.getInstance(this).getLoginDao().removeAllLogins();
        } else {
            AppDatabase.getInstance(this).getLoginDao().cleanUp(availableLoginIds);
            List<Long> availableIds = AppDatabase.getInstance(this).getLoginDao().getIds();
            for (File directory : getFilesDir().listFiles()) {
                if (directory.isDirectory()) {
                    Long id;
                    try {
                        id = Long.parseLong(directory.getName());
                    } catch (NumberFormatException exception) {
                        continue;
                    }
                    if (!availableIds.contains(id)) {
                        FileUtils.deleteRecursively(directory);
                    }
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAccountManager = AccountManager.get(this);
        //mAccountManager.addOnAccountsUpdatedListener(this, new Handler(), true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mAccountManager.removeOnAccountsUpdatedListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Authenticator authenticator = new Authenticator(this);
        return authenticator.getIBinder();
    }
}
