package com.kai_jan_57.opendsbmobile.fragments.contentviewer;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.material.snackbar.Snackbar;
import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.utils.FileUtils;
import com.kai_jan_57.opendsbmobile.utils.LogUtils;
import com.kai_jan_57.opendsbmobile.utils.ShareUtils;
import com.kai_jan_57.opendsbmobile.viewmodels.WebviewViewModel;
import com.kai_jan_57.opendsbmobile.widgets.TopProgressBar;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebViewFragment extends ContentViewerFragment {

    private final String TAG = getClass().getCanonicalName();

    private WebView mWebView;
    private TopProgressBar mProgressBar;

    public WebViewFragment() {
        // Required empty public constructor
    }

    private void resetZoom() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setLoadWithOverviewMode(false);
        webSettings.setLoadWithOverviewMode(true);
        mWebView.setInitialScale(0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    private Float mWebViewScale = Float.NaN;

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        setupAdvancedSharing(R.menu.fragment_context_webview);

        mWebView = view.findViewById(R.id.webView);
        mProgressBar = view.findViewById(R.id.webViewProgressBar);

        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setUseWideViewPort(true);

        if (getNode().mContentType == Node.ContentType.URL) {
            mWebView.loadUrl(getNode().mContent);
            resetZoom();
            return;
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }

            @Override
            public void onScaleChanged(WebView view, float oldScale, float newScale) {
                super.onScaleChanged(view, oldScale, newScale);
                mWebViewScale = newScale;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl()));
                return true;
            }
        });
        WebviewViewModel viewModel = ViewModelProviders.of(this).get(getClass().getCanonicalName() + getNode().toString(), WebviewViewModel.class);

        viewModel.getException().observe(this, exception -> Snackbar.make(view, exception.getLocalizedMessage(), Snackbar.LENGTH_LONG).show());

        viewModel.getProgress().observe(this, pProgress -> mProgressBar.setProgress(pProgress));

        viewModel.getHtmlFile().observe(this, htmlFile -> {
            //mWebView.loadData(htmlFile, "text/html; charset=utf-8", StandardCharsets.UTF_8.name());
            if (htmlFile == null) {
                mWebView.loadUrl("about:blank");
            } else {
                mWebView.loadUrl(Uri.fromFile(htmlFile).toString());
                resetZoom();
            }
            mProgressBar.hide();
        });

        viewModel.setOnOutdatedLoadedListener(() -> Snackbar.make(view, R.string.cache_outdated, Snackbar.LENGTH_LONG).show());

        if (viewModel.getHtmlFile().getValue() == null || viewModel.getDate() == null || viewModel.getDate().after(getNode().mDate)) {
            mProgressBar.show();
            viewModel.loadHtml(getNode());
        }
    }

    @Override
    public void onSwipedOut() {
        resetZoom();
    }

    @Override
    public boolean shareAdvanced(MenuItem pMenuItem) {
        if (getActivity() == null) {
            return false;
        }
        switch (pMenuItem.getItemId()) {
            case R.id.share_link: {
                ShareUtils.shareLink(getActivity(), getNode().mContent, pMenuItem.getTitle().toString());
                return true;
            }
            case R.id.share_html: {
                //ShareUtils.shareText(getActivity(), mViewModel.getHtmlFile().getValue(), "text/html", pMenuItem.getTitle().toString());
                try {
                    ShareUtils.shareFile(getActivity(), getNode().getContentCache(), FileUtils.encodeSafeFilename(getNode().mContent) + ".html", pMenuItem.getTitle().toString());
                } catch (IOException pE) {
                    Log.e(TAG, LogUtils.getStackTrace(pE));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void shareContent(View view) {
        if (!mWebViewScale.isNaN()) {
            try {
                Bitmap bitmap = Bitmap.createBitmap((int) (mWebView.getWidth() * mWebViewScale + 0.5f), (int) (mWebView.getContentHeight() * mWebViewScale + 0.5f), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                mWebView.draw(canvas);
                if (getActivity() != null) {
                    ShareUtils.shareBitmap(getActivity(), bitmap, getString(R.string.share_as_image));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isSearchable() {
        return true;
    }

    @Override
    public void search(String query) {
        if (query.isEmpty()) {
            mWebView.clearMatches();
        } else {
            mWebView.findAllAsync(query);
        }
    }
}
