package com.kai_jan_57.opendsbmobile.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.SearchView;

import com.google.android.material.tabs.TabLayout;
import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.adapters.TrackingFragmentPagerAdapter;
import com.kai_jan_57.opendsbmobile.fragments.contentviewer.ContentViewerFragment;
import com.kai_jan_57.opendsbmobile.database.AppDatabase;
import com.kai_jan_57.opendsbmobile.utils.ShareUtils;
import com.kai_jan_57.opendsbmobile.viewpagertransformers.DepthPageTransformer;
import com.kai_jan_57.opendsbmobile.widgets.ZoomableViewPager;

import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ContentViewerActivity extends AppCompatActivity {

    private static final String EXTRA_ROOT_NODES = "node_id";
    private static final String EXTRA_INDEX = "index";
    private static final String STATE_VISIBILITY = "visibility";
    private static final String STATE_SEARCHABLE = "searchable";

    private ZoomableViewPager mViewPager;
    private TrackingFragmentPagerAdapter mPagerAdapter;
    private TabLayout mPageIndicator;

    private Menu mOptionsMenu;

    private long[] mRootNodeIds;

    private int mIndex = 0;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3500;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mViewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    };
    private View mControlsView;
    private View mOverlayContainer;
    private final Runnable mShowPart2Runnable = () -> {
        // Delayed display of UI elements
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        //mControlsView.setVisibility(View.VISIBLE);
        showControls();
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = () -> hide(false);

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black_overlay, getTheme())));
            actionBar.setElevation(0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_VISIBILITY, mVisible);
        outState.putBoolean(STATE_SEARCHABLE, findViewById(R.id.photopage_bottom_control_search).getVisibility() == View.VISIBLE);
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }
        setContentView(R.layout.activity_content_viewer);

        setupActionBar();

        mVisible = true;

        mOverlayContainer = findViewById(R.id.overlayContainer);
        mControlsView = findViewById(R.id.fullscreen_content_controls);

        mViewPager = findViewById(R.id.viewPager);
        mPageIndicator = findViewById(R.id.pageIndicator);
        mPageIndicator.setupWithViewPager(mViewPager);

        mViewPager.setPageTransformer(false, new DepthPageTransformer());

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        mViewPager.setOnClickListener(v -> toggleFullscreen());

        findViewById(R.id.photopage_bottom_control_search).setVisibility(savedInstanceState != null && savedInstanceState.getBoolean(STATE_SEARCHABLE, false) ? View.VISIBLE : View.GONE);
        if (savedInstanceState == null || savedInstanceState.getBoolean(STATE_VISIBILITY, true)) {
            delayAutohide();
        } else {
            hide(true);
        }

        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra(EXTRA_INDEX)) {
                mIndex = getIntent().getExtras().getInt(EXTRA_INDEX);
            } else {
                finish();
                return;
            }
            if (getIntent().hasExtra(EXTRA_ROOT_NODES)) {
                mRootNodeIds = getIntent().getExtras().getLongArray(EXTRA_ROOT_NODES);
                updateActionBarTitle();
            } else {
                finish();
                return;
            }
        } else {
            finish();
            return;
        }
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int mPreviouslySelectedPage = 0;

            @Override
            public void onPageSelected(int position) {
                Fragment previouslySelected = mPagerAdapter.findFragment(mPreviouslySelectedPage);
                if (previouslySelected instanceof ContentViewerFragment) {
                    ((ContentViewerFragment) previouslySelected).onSwipedOut();
                }
                mPreviouslySelectedPage = position;
                updateActionBarTitle();
                delayAutohide();
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_content_viewer, menu);
        mOptionsMenu = menu;
        updateBackNextControls(mIndex);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        searchItem.setVisible(false);
        SearchView searchView = ((SearchView) searchItem.getActionView());
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setIconified(false);
                searchView.requestFocusFromTouch();
                disableAutoHide();
                searchView.setOnCloseListener(() -> {
                    searchView.setOnCloseListener(null);
                    searchView.setQuery("", true);
                    searchItem.collapseActionView();
                    return true;
                });
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setIconified(true);
                searchView.clearFocus();
                searchItem.setVisible(false);
                delayAutohide();
                return true;
            }
        });
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Fragment fragment = mPagerAdapter.findFragment(mViewPager.getCurrentPosition());
                if (fragment instanceof ContentViewerFragment) {
                    ((ContentViewerFragment) fragment).search(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_back: {
                int newIndex = mIndex - 1;
                updateBackNextControls(newIndex);
                updateViewPager(newIndex);
                if (mOptionsMenu.findItem(R.id.app_bar_search).isActionViewExpanded())
                    delayAutohide();
                return true;
            }
            case R.id.action_next: {
                int newIndex = mIndex + 1;
                updateBackNextControls(newIndex);
                updateViewPager(newIndex);
                delayAutohide();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateViewPager(int newIndex) {
        mViewPager.animate().alpha(0).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime)).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                finished();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                finished();
            }

            private void finished() {
                mViewPager.setCurrentItem(0, false);
                mIndex = newIndex;
                mPagerAdapter.notifyDataSetChanged();
                mViewPager.setAdapter(mPagerAdapter);
                updateActionBarTitle();
                mViewPager.animate().alpha(1).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime)).setListener(null);
            }
        });
    }

    private void updateBackNextControls(int newIndex) {
        mOptionsMenu.findItem(R.id.action_back).setEnabled(newIndex > 0);
        mOptionsMenu.findItem(R.id.action_next).setEnabled(newIndex < mRootNodeIds.length - 1);
    }

    private void updateActionBarTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && mRootNodeIds != null && mRootNodeIds.length > 0) {
            actionBar.setTitle(AppDatabase.getInstance(this).getNodeDao().getNodeById(mRootNodeIds[mIndex]).mTitle);
            // TODO: fix possible crash? -> Nullpointer on Node.mId
            actionBar.setSubtitle(new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss", Locale.US)
                    .format(AppDatabase.getInstance(this).getNodeDao().getChildByIndex(AppDatabase.getInstance(this).getNodeDao().getNodeById(mRootNodeIds[mIndex]).mId, mViewPager.getCurrentPosition()).mDate));
        }
    }

    public void toggleFullscreen() {
        if (mVisible) {
            hide(false);
        } else {
            show();
        }
    }

    private void hide(boolean instant) {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        hideControls(instant);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mViewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);

        if (mOptionsMenu == null || !mOptionsMenu.findItem(R.id.app_bar_search).isActionViewExpanded()) {
            delayAutohide();
        }
    }


    private void showControls() {
        mOverlayContainer.animate()
                .translationY(0)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    private void hideControls(boolean instant) {
        mOverlayContainer.animate()
                .translationY(getResources().getDimension(R.dimen.content_controls_height))
                .setDuration(instant ? 0 : getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    @SuppressWarnings("SameParameterValue")
    private void delayedHide(int delayMillis) {
        disableAutoHide();
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void delayAutohide() {
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
    }

    public void disableAutoHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
    }

    public static void start(Activity parent, long[] rootNodeIds, int selectedIndex) {
        Intent intent = new Intent(parent, ContentViewerActivity.class);
        intent.putExtra(EXTRA_ROOT_NODES, rootNodeIds);
        intent.putExtra(EXTRA_INDEX, selectedIndex);
        parent.startActivity(intent);
    }

    private class MyPagerAdapter extends TrackingFragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getFragment(int position) {
            ContentViewerFragment contentViewerFragment = ContentViewerFragment.newInstance(AppDatabase.getInstance(ContentViewerActivity.this).getNodeDao().getChildByIndex(
                    AppDatabase.getInstance(ContentViewerActivity.this).getNodeDao().getNodeById(mRootNodeIds[mIndex]).mId, position
            ));
            ContentViewerActivity.this.findViewById(R.id.photopage_bottom_control_search).setVisibility(contentViewerFragment.isSearchable() ? View.VISIBLE : View.GONE);

            return contentViewerFragment;
        }

        @Override
        public int getCount() {
            // TODO: fix node is null;
            int count = AppDatabase.getInstance(ContentViewerActivity.this).getNodeDao().getChildCount(AppDatabase.getInstance(ContentViewerActivity.this).getNodeDao().getNodeById(mRootNodeIds[mIndex]).mId);
            mPageIndicator.setVisibility(count > 1 ? View.VISIBLE : View.GONE);
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "∙";
            //return String.valueOf(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (object instanceof Fragment) {
                getSupportFragmentManager().beginTransaction().remove((Fragment) object).commitNow();
            }
        }
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        delayAutohide();
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem pMenuItem) {
        Fragment fragment = mPagerAdapter.findFragment(mViewPager.getCurrentPosition());
        if (fragment instanceof ContentViewerFragment) {
            if (((ContentViewerFragment) fragment).shareAdvanced(pMenuItem)) {
                return true;
            }
        }
        return super.onContextItemSelected(pMenuItem);
    }

    public void onShareClick(View view) {
        delayAutohide();
        Fragment fragment = mPagerAdapter.findFragment(mViewPager.getCurrentPosition());
        if (fragment instanceof ContentViewerFragment) {
            ((ContentViewerFragment) fragment).shareContent(view);
        }
    }

    public void onBrowserClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppDatabase.getInstance(this).getNodeDao().getChildByIndex(AppDatabase.getInstance(this).getNodeDao().getNodeById(mRootNodeIds[mIndex]).mId, mViewPager.getCurrentPosition()).mContent));
        startActivity(browserIntent);
    }

    public void onSearchClick(View view) {
        if (mOptionsMenu != null) {
            MenuItem searchItem = mOptionsMenu.findItem(R.id.app_bar_search);
            if (searchItem != null) {
                if (searchItem.isActionViewExpanded()) {
                    searchItem.collapseActionView();
                } else {
                    searchItem.expandActionView();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ShareUtils.SHARE_REQUEST_CODE) {
            ShareUtils.cleanCache(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ShareUtils.cleanCache(this);
    }
}
