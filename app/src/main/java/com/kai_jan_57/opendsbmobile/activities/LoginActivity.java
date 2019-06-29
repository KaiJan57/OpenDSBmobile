package com.kai_jan_57.opendsbmobile.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;
import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.account.Authenticator;
import com.kai_jan_57.opendsbmobile.Application;
import com.kai_jan_57.opendsbmobile.database.AppDatabase;
import com.kai_jan_57.opendsbmobile.database.Login;
import com.kai_jan_57.opendsbmobile.network.FetchIndexRequestTask;
import com.kai_jan_57.opendsbmobile.utils.AccountUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import static android.Manifest.permission.READ_CONTACTS;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatAuthenticatorActivity implements FetchIndexRequestTask.FetchIndexEventListener {

    public final String TAG = this.getClass().getCanonicalName();

    public static final int ACTIVITY_REQUEST_CODE = 1;
    private static final String EXTRA_EDIT_NAME = "EXTRA_EDIT_NAME";
    public static final String EXTRA_LOGIN_ID = "EXTRA_LOGIN_ID";

    /**
     * Id to identify permission request.
     */
    private static final int REQUEST_PERMISSION = 0;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private FetchIndexRequestTask mAuthTask = null;

    // UI references.
    private EditText mIdView;
    private EditText mAliasView;
    private EditText mPasswordView;
    private ProgressBar mProgressBar;
    private Button mSubmitButton;

    private Account editingAccount = null;
    private Login mLogin;

    private AccountManager mAccountManager;

    private String mId;
    private String mAlias;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(Application.getInstance().getNightMode());
        setContentView(R.layout.activity_login);

        mAccountManager = AccountManager.get(getBaseContext());
        setupActionBar();
        // Set up the login form.
        mIdView = findViewById(R.id.id);
        mAliasView = findViewById(R.id.alias);
        mPasswordView = findViewById(R.id.password);

        mSubmitButton = findViewById(R.id.sign_in_button);

        mIdView.addTextChangedListener(new TextWatcher() {
            boolean equal = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                equal = s.toString().contentEquals(mAliasView.getText());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (equal) {
                    mAliasView.setText(s);
                    mAliasView.setSelectAllOnFocus(true);
                }
                mSubmitButton.setEnabled(s.length() > 0 && mPasswordView.getText().length() > 0);
            }
        });
        mAliasView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().contentEquals(mIdView.getText())) {
                    mAliasView.setSelectAllOnFocus(true);
                } else {
                    mAliasView.setSelectAllOnFocus(false);
                }
            }
        });
        mPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mSubmitButton.setEnabled(s.length() > 0 && mIdView.getText().length() > 0);
            }
        });

        if (getIntent().getExtras() != null && getIntent().hasExtra(EXTRA_EDIT_NAME)) {
            Account account = AccountUtils.getAccountByName(AccountManager.get(this), getIntent().getExtras().getString(EXTRA_EDIT_NAME));
            if (account != null) {
                editingAccount = account;
                mIdView.setEnabled(false);
                if (mAliasView.getText().toString().contentEquals(mIdView.getText().toString())) {
                    mAliasView.setSelectAllOnFocus(true);
                }
                mAliasView.requestFocus();
                getWindow().setSoftInputMode(SOFT_INPUT_STATE_VISIBLE);
                mPasswordView.setSelectAllOnFocus(true);
            }
        }

        doSensitiveAction();

        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                hideKeyboard();
                attemptLogin();
                return true;
            }
            return false;
        });

        if (editingAccount != null) {
            mIdView.setText(editingAccount.name);
            AccountManager accountManager = AccountManager.get(this);
            mAliasView.setText(AppDatabase.getInstance(this).getLoginDao().getLoginByName(editingAccount.name).mAlias);
            mPasswordView.setText(accountManager.getPassword(editingAccount));
        }

        mSubmitButton.setOnClickListener(view -> attemptLogin());

        View loginFormView = findViewById(R.id.login_form);
        mProgressBar = findViewById(R.id.login_progress);
    }

    private void showKeyboard() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideKeyboard() {
        if (getCurrentFocus() != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    private void doSensitiveAction() {
        if (!may_Permission_()) {
            //noinspection UnnecessaryReturnStatement
            return;
        }
        // Permission granted
    }

    @SuppressLint("ObsoleteSdkInt")
    private boolean may_Permission_() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mIdView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, v -> requestPermissions(new String[]{READ_CONTACTS}, REQUEST_PERMISSION));
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_PERMISSION);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doSensitiveAction();
            }
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // AuthActivity doesn't work with parent activity values specified in android manifest -> fall back to manual handling
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
            }
            case R.id.action_demo_login: {
                mIdView.setText(getString(R.string.cred_demo_login));
                mAliasView.setText(getString(R.string.demo_alias));
                mPasswordView.setText(getString(R.string.cred_demo_password));
                return true;
            }
            case R.id.action_qr_login: {
                QrScannerActivity.start(this);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mSubmitButton.setEnabled(false);

        // Reset errors.
        mIdView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mId = mIdView.getText().toString();
        mPassword = mPasswordView.getText().toString();
        mAlias = mAliasView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid identification.
        if (TextUtils.isEmpty(mId)) {
            mIdView.setError(getString(R.string.error_field_required));
            focusView = mIdView;
            cancel = true;
        }

        if (editingAccount == null && AccountUtils.getAccountByName(mAccountManager, mId) != null) {
            mIdView.setError(getString(R.string.error_login_exists));
            focusView = mIdView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            reset();
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if (mAlias.isEmpty()) {
                mAliasView.setText(mId);
            } else {
                Login.Dao loginDao = AppDatabase.getInstance(this).getLoginDao();
                if (editingAccount == null || !mAccountManager.getPassword(editingAccount).equals(mPassword)) {
                    mLogin = new Login(mId, mAlias);
                    mLogin.mId = AppDatabase.getInstance(this).getLoginDao().addLogin(mLogin);
                    mAuthTask = new FetchIndexRequestTask(this, mLogin, mId, mPassword);
                    mAuthTask.execute();
                } else {
                    Login login = loginDao.getLoginByName(mId);
                    login.mAlias = mAlias;
                    loginDao.updateLogin(login);
                    done(null);
                }
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressBar.setIndeterminate(show);
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressBar.setVisibility(show ? View.GONE : View.VISIBLE);
            mProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressBar.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressBar.setVisibility(show ? View.GONE : View.VISIBLE);
        }*/
    }

    private void reset() {
        mLogin = null;
        mAuthTask = null;
        showProgress(false);
        mSubmitButton.setEnabled(false);
    }

    private void onValidateCancelled() {
        mAuthTask = null;
        showProgress(false);
    }

    public static void start(Activity parent, Account toBeEdited) {
        Intent intent = new Intent(parent, LoginActivity.class);
        if (toBeEdited != null) {
            intent.putExtra(EXTRA_EDIT_NAME, toBeEdited.name);
        }
        parent.startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    @Override
    public void onException(Exception exception) {
        if (mLogin != null) {
            AppDatabase.getInstance(this).getLoginDao().deleteId(mLogin.mId);
        }

        reset();
        hideKeyboard();
        Snackbar.make(findViewById(R.id.id_login_form), exception.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
        mSubmitButton.setEnabled(true);
    }

    @Override
    public void onSuccess(long loginId, boolean cached) {
        // cant check new password when offline
        if (!cached) {
            Account account = editingAccount;
            if (editingAccount == null) {
                account = new Account(mId, Authenticator.ACCOUNT_TYPE);
                mAccountManager.addAccountExplicitly(account, mPassword, null);

                ContentResolver.setIsSyncable(account, getString(R.string.content_authority), 1);
                ContentResolver.setSyncAutomatically(account, getString(R.string.content_authority), true);
                ContentResolver.addPeriodicSync(account, getString(R.string.content_authority), new Bundle(), 60 * 60);

                Application.getInstance().setActiveAccountId(mId);
            }
            mAccountManager.setAuthToken(account, Authenticator.AUTHTOKEN_TYPE, mPassword);

            Intent data = new Intent()
                    .putExtra(AccountManager.KEY_ACCOUNT_NAME, mId)
                    .putExtra(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE);
            done(data);
        }
    }

    private void done(Intent data) {
        if (data != null) {
            setAccountAuthenticatorResult(data.getExtras());
            setResult(RESULT_OK, data);
        } else {
            setResult(RESULT_OK);
        }
        reset();
        finish();
    }

    @Override
    public void onFail(FetchIndexRequestTask.LoginResult loginResult, String mandantId) {
        mPasswordView.setError(getString(R.string.error_incorrect_credentials));
        mPasswordView.requestFocus();
        reset();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QrScannerActivity.ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data.getExtras() != null) {
            if (data.hasExtra(QrScannerActivity.RESULT_ID)) {
                mIdView.setText(data.getStringExtra(QrScannerActivity.RESULT_ID));
            }
            if (data.hasExtra(QrScannerActivity.RESULT_PASSWORD)) {
                mPasswordView.setText(data.getStringExtra(QrScannerActivity.RESULT_PASSWORD));
            }
        }
    }
}

