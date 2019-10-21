package com.kai_jan_57.opendsbmobile.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.kai_jan_57.opendsbmobile.account.Authenticator;
import com.kai_jan_57.opendsbmobile.fragments.ContentOverviewFragment;
import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.Application;
import com.kai_jan_57.opendsbmobile.database.AppDatabase;
import com.kai_jan_57.opendsbmobile.database.Login;
import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.network.FetchIndexRequestTask;
import com.kai_jan_57.opendsbmobile.utils.AccountUtils;
import com.kai_jan_57.opendsbmobile.utils.FileUtils;
import com.kai_jan_57.opendsbmobile.viewmodels.MainViewModel;
import com.kai_jan_57.opendsbmobile.widgets.TopProgressBar;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.kai_jan_57.opendsbmobile.Application.PREFERENCE_APPEARANCE_THEME;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SwipeRefreshLayout.OnRefreshListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String LOGIN_ID = "login_id";

    private NavigationView mNavigationView;
    private TopProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView titleTextView;
    private TextView subTitleTextView;

    private Fragment mContentOverviewFragment;

    private MainViewModel mViewModel;

    private AccountManager mAccountManager;

    private OnAccountsUpdateListener mOnAccountsUpdateListener = accounts -> {
        // reload that whole thing whenever the current account was removed
        if (mViewModel != null && mViewModel.getAccount().getValue() != null) {
            List<String> availableAccounts = new ArrayList<>();
            for (Account account : accounts) {
                if (account.type.equals(Authenticator.ACCOUNT_TYPE)) {
                    availableAccounts.add(account.name);
                }
            }
            if (!availableAccounts.contains(mViewModel.getAccount().getValue().name)) {
                recreate();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(Application.getInstance().getNightMode());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mNavigationView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        mProgressBar = findViewById(R.id.progressBar_content_loading);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_main);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        titleTextView = mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_title);

        subTitleTextView = mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_subtitle);

        mAccountManager = AccountManager.get(this);

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        mViewModel.getAvailableMethods().observe(this, availableMethods -> {
            updateMenuItemEntries(true);
            if (mViewModel.getAccount().getValue() != null) {
                Date lastUpdate = AppDatabase.getInstance(this).getLoginDao().getLoginByName(mViewModel.getAccount().getValue().name).mLastUpdate;
                if (lastUpdate != null && getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(String.format(getString(R.string.actionbar_last_update),
                            new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss", Locale.US)
                                    .format(lastUpdate)
                    ));
                }
            }
            hideProgress();
        });

        mViewModel.getSelectedMethod().observe(this, selectedMethod -> {
            if (mViewModel.getAccount().getValue() != null) {
                mContentOverviewFragment = ContentOverviewFragment.newInstance(mViewModel.getSelectedMethod().getValue(), AppDatabase.getInstance(this).getLoginDao().getLoginByName(mViewModel.getAccount().getValue().name).mId);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, mContentOverviewFragment).commit();
            }
        });

        mViewModel.setProgressListener(progress -> mProgressBar.setProgress(progress));
        mViewModel.setExceptionListener(exception -> {
            Snackbar.make(findViewById(R.id.main_fragment), exception.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            hideProgress();
        });
        mViewModel.setLoginFailListener(
                (loginResult, resultStatusInfo) ->
                        Snackbar.make(findViewById(R.id.main_fragment),
                            // show custom message if licence is expired
                            loginResult == FetchIndexRequestTask.LoginResult.Licence_Expired ? resultStatusInfo : getString(R.string.login_failed),
                            Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_sign_in, (view) -> {
            toggleLoginsMenu(null);
            drawer.openDrawer(GravityCompat.START);
        }).show());

        // on Login change
        mViewModel.getAccount().observe(this, this::updateNavigationHeader);

        if (mViewModel.getAccount().getValue() == null) {
            // we assured that this is a fresh start (new ViewModel) and we have to log in
            Account[] accounts = mAccountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
            if (accounts.length > 0) {
                Account activeAccount = null;
                if (getIntent().getExtras() != null && getIntent().hasExtra(LOGIN_ID)) {
                    activeAccount = AccountUtils.getAccountByName(mAccountManager, getIntent().getStringExtra(LOGIN_ID));
                }
                if (activeAccount == null) {
                    activeAccount = Application.getInstance().getActiveAccount();
                }
                if (AppDatabase.getInstance(this).getLoginDao().getLoginByName(activeAccount.name) == null) {
                    // active account is not in database; create new index for that login
                    AppDatabase.getInstance(this).getLoginDao().addLogin(new Login(activeAccount.name));
                }
                fetchIndex(activeAccount);
            } else {
                LoginActivity.start(this, null);
            }
        }

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        mAccountManager.addOnAccountsUpdatedListener(mOnAccountsUpdateListener, new Handler(), false);
    }

    @Override
    public void onPause() {
        super.onPause();
        Application.getInstance().mIsInForeground = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Application.getInstance().mIsInForeground = true;
        // TODO: update selected account when notification was clicked
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREFERENCE_APPEARANCE_THEME)) {
            recreate();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        mAccountManager.removeOnAccountsUpdatedListener(mOnAccountsUpdateListener);
    }

    private void fetchIndex(Account account) {
        mSwipeRefreshLayout.setRefreshing(true);
        showProgress();
        mViewModel.fetchIndex(mAccountManager, account);
        mViewModel.setAccount(account);
    }

    private android.view.MenuItem menuItemFromMethod(Node.Method method) {
        if (method != null) {
            SubMenu categories = mNavigationView.getMenu().findItem(R.id.nav_section_content).getSubMenu();
            if (categories != null) {
                switch (method) {
                    case Timetables:
                        return categories.findItem(R.id.nav_timetables);
                    case Tiles:
                        return categories.findItem(R.id.nav_tiles);
                    case News:
                        return categories.findItem(R.id.nav_news);
                    default:
                        return null;
                }
            }
        }
        return null;
    }

    private void updateNavigationHeader(@Nullable Account account) {
        if (account != null) {
            Login login = AppDatabase.getInstance(this).getLoginDao().getLoginByName(account.name);
            if (login != null) {
                titleTextView.setText(login.mAlias);
            }
            subTitleTextView.setText(account.name);
        }
    }

    private void updateMenuItemEntries(boolean refreshSelectedFragment) {
        if (!showLoginsMenu) {
            List<Node.Method> supportedMethods = mViewModel.getAvailableMethods().getValue();
            if (supportedMethods != null && !supportedMethods.isEmpty()) {
                if (mNavigationView.getMenu() != null) {
                    SubMenu categories = mNavigationView.getMenu().findItem(R.id.nav_section_content).getSubMenu();

                    categories.findItem(R.id.nav_timetables).setEnabled(supportedMethods.contains(Node.Method.Timetables));
                    categories.findItem(R.id.nav_tiles).setEnabled(supportedMethods.contains(Node.Method.Tiles));
                    categories.findItem(R.id.nav_news).setEnabled(supportedMethods.contains(Node.Method.News));

                    boolean selectedItemChanged = true;
                    android.view.MenuItem checkedMenuItem = menuItemFromMethod(mViewModel.getSelectedMethod().getValue());
                    if (checkedMenuItem == null || !checkedMenuItem.isEnabled()) {
                        mViewModel.selectMethod(Node.Method.Timetables);
                        checkedMenuItem = menuItemFromMethod(mViewModel.getSelectedMethod().getValue());
                        if (checkedMenuItem == null || !checkedMenuItem.isEnabled()) {
                            mViewModel.selectMethod(Node.Method.Tiles);
                            checkedMenuItem = menuItemFromMethod(mViewModel.getSelectedMethod().getValue());
                            if (checkedMenuItem == null || !checkedMenuItem.isEnabled()) {
                                mViewModel.selectMethod(Node.Method.News);
                                checkedMenuItem = menuItemFromMethod(mViewModel.getSelectedMethod().getValue());
                            }
                        }
                    } else {
                        selectedItemChanged = false;
                    }

                    if (checkedMenuItem != null) {
                        final android.view.MenuItem finalCheckedMenuItem = checkedMenuItem;
                        runOnUiThread(() -> mNavigationView.setCheckedItem(finalCheckedMenuItem));
                        if ((selectedItemChanged || refreshSelectedFragment) && mViewModel.getAccount().getValue() != null) {
                            mContentOverviewFragment = ContentOverviewFragment.newInstance(mViewModel.getSelectedMethod().getValue(), AppDatabase.getInstance(this).getLoginDao().getLoginByName(mViewModel.getAccount().getValue().name).mId);
                            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, mContentOverviewFragment).commitAllowingStateLoss();
                        }
                    }
                }
            }
        }
    }

    private void updateLoginItemEntries() {
        SubMenu loginItems = mNavigationView.getMenu().findItem(R.id.nav_section_ids).getSubMenu();
        loginItems.removeGroup(R.id.nav_group_ids);
        Account[] accounts = mAccountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
        int index = 0;
        for (Account account : accounts) {
            Login login = AppDatabase.getInstance(this).getLoginDao().getLoginByName(account.name);
            if (login == null) {
                login = new Login(account.name, "");
            }

            // intended to begin with 1
            index++;
            String title = login.toString();
            android.view.MenuItem current = loginItems.add(R.id.nav_group_ids, index, Menu.FIRST, title)
                    .setIcon(R.drawable.ic_menu_account)
                    .setCheckable(true);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setVisibility(View.GONE);
            checkBox.setAlpha(0);
            checkBox.setTag(account);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedAccounts.add((Account) buttonView.getTag());
                } else {
                    //noinspection RedundantCast
                    selectedAccounts.remove((Account) buttonView.getTag());
                }
                findViewById(R.id.imageButton_delete).setEnabled(!selectedAccounts.isEmpty());
            });
            current.setActionView(checkBox);

            if (account.equals(mViewModel.getAccount().getValue())) {
                runOnUiThread(() -> mNavigationView.setCheckedItem(current));
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoginActivity.ACTIVITY_REQUEST_CODE) {
            editLoginsButton(null);
            if (resultCode == RESULT_OK) {
                if (data != null && data.hasExtra(AccountManager.KEY_ACCOUNT_NAME)) {
                    String newAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    mViewModel.setAccount(AccountUtils.getAccountByName(mAccountManager, newAccountName));
                    mViewModel.updateAvailableMethods();
                }
                updateLoginItemEntries();
                updateNavigationHeader(mViewModel.getAccount().getValue());
            } else if (mViewModel.getAccount().getValue() == null) {
                if (AppDatabase.getInstance(this).getLoginDao().getCount() > 0) {
                    recreate();
                } else {
                    finish();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Account account = mViewModel.getAccount().getValue();
            if (account != null) {
                SettingsActivity.start(this, account.name);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (id == R.id.nav_settings) {
            SettingsActivity.start(this, null);
        } else if (id == R.id.nav_about) {
            new LibsBuilder()
                    .withActivityTitle(getString(R.string.action_about))
                    .withFields(R.string.class.getFields())
                    .withActivityStyle(Libs.ActivityStyle.DARK)
                    .start(this);
        } else if (id == R.id.nav_timetables) {
            mViewModel.selectMethod(Node.Method.Timetables);
        } else if (id == R.id.nav_tiles) {
            mViewModel.selectMethod(Node.Method.Tiles);
        } else if (id == R.id.nav_news) {
            mViewModel.selectMethod(Node.Method.News);
        }

        switch (id) {
            case R.id.nav_settings:
            case R.id.nav_timetables:
            case R.id.nav_tiles:
            case R.id.nav_news: {
                drawer.closeDrawer(GravityCompat.START);
                break;
            }
            default: {
                if (showLoginsMenu) {
                    Account selectedAccount = (Account) item.getActionView().getTag();
                    if (selectingLogins) {
                        LoginActivity.start(this, selectedAccount);
                        return false;
                    } else {
                        if (selectedAccount.equals(mViewModel.getAccount().getValue())) {
                            return false;
                        } else {
                            fetchIndex(selectedAccount);
                            toggleLoginsMenu(null);
                            drawer.closeDrawer(GravityCompat.START);
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onRefresh() {
        fetchIndex(mViewModel.getAccount().getValue());
    }

    private void showProgress() {
        //mProgressBar.clearAnimation();
        mProgressBar.setProgress(0);
        //mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.show();
    }

    private void hideProgress() {
        mSwipeRefreshLayout.setRefreshing(false);
        mProgressBar.hide();
    }

    private boolean showLoginsMenu = false;

    public void toggleLoginsMenu(View view) {
        showLoginsMenu = !showLoginsMenu;
        ImageView dropDownButton = findViewById(R.id.logins_header_dropdown);
        if (dropDownButton != null) {
            dropDownButton.animate()
                    .rotation(showLoginsMenu ? 180 : 0)
                    .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                    .setInterpolator(new LinearInterpolator());
        }
        if (showLoginsMenu) {
            selectingLogins = false;
            updateLoginItemEntries();
        } else {
            if (selectingLogins) {
                editLoginsButton(null);
            }
            updateMenuItemEntries(false);
        }
        runOnUiThread(() -> mNavigationView.getMenu().findItem(R.id.nav_item_edit_ids).getActionView().findViewById(R.id.imageButton_delete).setEnabled(false));
        mNavigationView.getMenu().findItem(R.id.nav_section_ids).setVisible(showLoginsMenu);
        mNavigationView.getMenu().findItem(R.id.nav_section_content).setVisible(!showLoginsMenu);
    }

    public void addLoginButton(View view) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        toggleLoginsMenu(null);
        LoginActivity.start(this, null);
    }

    public void editLoginsButton(View view) {
        selectingLogins = !selectingLogins;
        //mNavigationView.getMenu().setGroupCheckable(R.id.nav_group_ids, !selectingLogins, true);
        setImageButtonActive(findViewById(R.id.imageButton_edit), selectingLogins);
        SubMenu loginItems = mNavigationView.getMenu().findItem(R.id.nav_section_ids).getSubMenu();
        if (loginItems.size() > 2) {
            for (int i = 1; i < loginItems.size(); i++) {
                CheckBox checkBox = (CheckBox) loginItems.getItem(i).getActionView();
                if (checkBox != null && !checkBox.getTag().equals(mViewModel.getAccount().getValue())) {
                    if (selectingLogins) {
                        checkBox.setVisibility(View.VISIBLE);
                        checkBox.animate()
                                .alpha(1)
                                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                                .setListener(null);
                    } else {
                        checkBox.setChecked(false);
                        checkBox.animate()
                                .alpha(0)
                                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        if (!selectingLogins) {
                                            checkBox.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                }
            }
        }
    }

    private boolean selectingLogins = false;
    private final List<Account> selectedAccounts = new ArrayList<>();

    @SuppressLint("ObsoleteSdkInt")
    public void deleteLoginsButton(View view) {
        selectedAccounts.remove(mViewModel.getAccount().getValue());
        for (Account account : selectedAccounts) {
            // delete login information (name, index, cache)
            long loginId = AppDatabase.getInstance(this).getLoginDao().getLoginByName(account.name).mId;
            AppDatabase.getInstance(this).getNodeDao().deleteAllNodesByLoginId(loginId);
            FileUtils.deleteRecursively(new File(getFilesDir(), String.valueOf(loginId)));
            AppDatabase.getInstance(this).getLoginDao().deleteId(loginId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                mAccountManager.removeAccount(account, this, null, null);
            } else {
                //noinspection deprecation
                mAccountManager.removeAccount(account, null, null);
            }
        }
        if (!selectedAccounts.isEmpty()) {
            selectedAccounts.clear();
            toggleLoginsMenu(null);
        }
    }

    private void setImageButtonActive(ImageButton imageButton, boolean enabled) {
        if (imageButton != null) {
            if (enabled) {
                imageButton.getDrawable().setTint(ContextCompat.getColor(this, R.color.colorAccent));
            } else {
                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(R.attr.colorControlNormal, typedValue, true);
                TypedArray typedArray = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorControlNormal});
                imageButton.getDrawable().setTintList(typedArray.getColorStateList(0));
                typedArray.recycle();
            }
        }
    }
}
