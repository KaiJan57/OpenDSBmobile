package com.kai_jan_57.opendsbmobile.viewmodels;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;

import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.account.Authenticator;
import com.kai_jan_57.opendsbmobile.Application;
import com.kai_jan_57.opendsbmobile.database.Login;
import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.network.FetchIndexRequestTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<Account> mAccount = new MutableLiveData<>();
    private Account mPreviousAccount;

    private final MutableLiveData<List<Node.Method>> mAvailableMethods = new MutableLiveData<>();
    private final MutableLiveData<Node.Method> mSelectedMethod = new MutableLiveData<>();
    private ProgressListener mProgressListener;
    private ExceptionListener mExceptionListener;
    private LoginFailListener mLoginFailListener;

    public MainViewModel() {
        super();
    }

    private void fail(FetchIndexRequestTask.LoginResult loginResult, String resultStatusInfo) {
        if (mPreviousAccount != null) {
            mAccount.setValue(mPreviousAccount);
            Application.getInstance().setActiveAccount(mAccount.getValue());
            mPreviousAccount = null;
        }
        if (mLoginFailListener != null) {
            mLoginFailListener.onFail(loginResult, resultStatusInfo);
        }
    }

    public void fetchIndex(AccountManager pAccountManager, Account account) {
        mPreviousAccount = mAccount.getValue();
        Login.Dao loginDao = Application.getInstance().getDatabase().getLoginDao();
        Login login = loginDao.getLoginByName(account.name);
        if (login == null) {
            login = new Login(account.name, account.name);
            login.mId = loginDao.addLogin(login);
        }
        new FetchIndexRequestTask(new FetchIndexRequestTask.FetchIndexEventListener() {
            @Override
            public void onProgress(int progress) {
                if (mProgressListener != null) {
                    mProgressListener.onProgress(progress);
                }
            }

            @Override
            public void onException(Exception exception) {
                if (mExceptionListener != null) {
                    mExceptionListener.onException(exception);
                }
            }

            @Override
            public void onSuccess(long loginId, boolean cached) {
                // only update activeaccount if user switched actively; don't update on refresh
                if (mPreviousAccount != null) {
                    Application.getInstance().setActiveAccount(mAccount.getValue());
                    mPreviousAccount = null;
                }
                updateAvailableMethods();
                if (cached) {
                    mExceptionListener.onException(new Exception(Application.getInstance().getString(R.string.cache_outdated)));
                }
            }

            @Override
            public void onFail(FetchIndexRequestTask.LoginResult loginResult, String resultStatusInfo) {
                fail(loginResult, resultStatusInfo);
            }
        }, login, account.name, pAccountManager.getPassword(account)).execute();
    }

    public void setAccount(Account account) {
        mPreviousAccount = mAccount.getValue();
        mAccount.setValue(account);
    }

    public LiveData<Account> getAccount() {
        return mAccount;
    }

    public void setExceptionListener(ExceptionListener exceptionListener) {
        mExceptionListener = exceptionListener;
    }

    public void setProgressListener(ProgressListener progressListener) {
        mProgressListener = progressListener;
    }

    public void setLoginFailListener(LoginFailListener loginFailListener) {
        mLoginFailListener = loginFailListener;
    }

    public void updateAvailableMethods() {
        List<Node.Method> result = new ArrayList<>();
        if (mAccount.getValue() != null) {
            for (Node.Method method : Node.Method.values()) {
                if (Application.getInstance().getDatabase().getNodeDao().getNodeCountByMethod(Application.getInstance().getDatabase().getLoginDao().getLoginByName(mAccount.getValue().name).mId, method) > 0) {
                    result.add(method);
                }
            }
        }
        mAvailableMethods.setValue(result);
    }

    public LiveData<List<Node.Method>> getAvailableMethods() {
        return mAvailableMethods;
    }

    public LiveData<Node.Method> getSelectedMethod() {
        return mSelectedMethod;
    }

    public void selectMethod(Node.Method method) {
        mSelectedMethod.setValue(method);
    }

    public interface ProgressListener {
        void onProgress(int progress);
    }

    public interface ExceptionListener {
        void onException(Exception e);
    }

    public interface LoginFailListener {
        void onFail(FetchIndexRequestTask.LoginResult loginResult, String resultStatusInfo);
    }
}
