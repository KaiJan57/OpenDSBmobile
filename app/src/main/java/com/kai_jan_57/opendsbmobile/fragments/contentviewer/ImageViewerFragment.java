package com.kai_jan_57.opendsbmobile.fragments.contentviewer;


import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.activities.ContentViewerActivity;
import com.kai_jan_57.opendsbmobile.utils.FileUtils;
import com.kai_jan_57.opendsbmobile.utils.ShareUtils;
import com.kai_jan_57.opendsbmobile.utils.LogUtils;
import com.kai_jan_57.opendsbmobile.viewmodels.ImageviewerViewModel;
import com.ortiz.touchview.TouchImageView;

import java.io.IOException;

public class ImageViewerFragment extends ContentViewerFragment implements GestureDetector.OnDoubleTapListener {

    private final String TAG = getClass().getCanonicalName();

    private TouchImageView mTouchImageView;

    public ImageViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSwipedOut() {
        if (mTouchImageView != null) {
            mTouchImageView.resetZoom();
        }
    }

    @Override
    public void shareContent(View view) {
        try {
            if (getActivity() == null) {
                return;
            }
            ShareUtils.shareFile(getActivity(),
                    getNode().getContentCache(),
                    FileUtils.encodeSafeFilename(getNode().mContent), getString(R.string.share_image));
        } catch (IOException pE) {
            Log.e(TAG, LogUtils.getStackTrace(pE));
        }
    }

    @Override
    public boolean shareAdvanced(MenuItem pMenuItem) {
        if (pMenuItem.getItemId() == R.id.share_link && getActivity() != null) {
            ShareUtils.shareLink(getActivity(), getNode().mContent, pMenuItem.getTitle().toString());
            return true;
        }
        return false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        mTouchImageView = view.findViewById(R.id.touchImageView);
        mTouchImageView.setEnabled(false);
        //mTouchImageView.setOnDoubleTapListener(this);
        if (getActivity() == null) {
            return;
        }
        ImageviewerViewModel viewModel = ViewModelProviders.of(this).get(getClass().getCanonicalName() + getNode().toString(), ImageviewerViewModel.class);

        viewModel.getBitmap().observe(this, pBitmap -> {
            mTouchImageView.setImageBitmap(pBitmap);
            mTouchImageView.setEnabled(true);
        });

        viewModel.getException().observe(this, pException -> {
            mTouchImageView.setEnabled(false);
            mTouchImageView.setImageResource(R.drawable.ic_item_image_broken);
        });


        if (viewModel.getBitmap().getValue() == null || getNode().mDate.after(viewModel.getDate())) {
            viewModel.loadImage(getNode());
        }

        setupAdvancedSharing(R.menu.fragment_context_imageviewer);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Activity parentActivity = getActivity();
        if (parentActivity instanceof ContentViewerActivity) {
            ((ContentViewerActivity) parentActivity).toggleFullscreen();
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
