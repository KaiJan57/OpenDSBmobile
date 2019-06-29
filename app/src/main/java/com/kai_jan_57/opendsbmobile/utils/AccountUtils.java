package com.kai_jan_57.opendsbmobile.utils;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.kai_jan_57.opendsbmobile.account.Authenticator;
import com.kai_jan_57.opendsbmobile.Application;

import java.io.File;

public class AccountUtils {

    public static Account getAccountByName(AccountManager pAccountManager, String name) {
        Account[] accounts = pAccountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
        for (Account account : accounts) {
            if (account.name.equals(name)) {
                return account;
            }
        }
        return null;
    }

}
