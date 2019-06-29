package com.kai_jan_57.opendsbmobile.adapters;

import android.util.SparseArray;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public abstract class TrackingFragmentPagerAdapter extends FragmentPagerAdapter {

    private final SparseArray<Fragment> mLoadedFragments = new SparseArray<>();

    public Fragment findFragment(int position) {
        return mLoadedFragments.get(position);
    }

    protected TrackingFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = getFragment(position);
        mLoadedFragments.append(position, fragment);
        return fragment;
    }

    protected abstract Fragment getFragment(int position);
}
