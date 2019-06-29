package com.kai_jan_57.opendsbmobile.viewmodels;

import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.network.download.NodeDownloader;

import java.io.File;
import java.util.Date;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WebviewViewModel extends ViewModel implements NodeDownloader.DownloadEventListener {

    private final MutableLiveData<File> mHtmlFile = new MutableLiveData<>();
    private final MutableLiveData<Integer> mProgress = new MutableLiveData<>();
    private final MutableLiveData<Exception> mException = new MutableLiveData<>();

    private OnOutdatedLoadedListener mOnOutdatedLoadedListener;

    private Node mNode;

    public WebviewViewModel() {
        super();
    }

    public Date getDate() {
        return mNode.mDate;
    }

    public LiveData<File> getHtmlFile() {
        return mHtmlFile;
    }

    public LiveData<Integer> getProgress() {
        return mProgress;
    }

    public LiveData<Exception> getException() {
        return mException;
    }

    public void setOnOutdatedLoadedListener(OnOutdatedLoadedListener onOutdatedLoadedListener) {
        mOnOutdatedLoadedListener = onOutdatedLoadedListener;
    }

    public void loadHtml(Node node) {
        mNode = node;
        // TODO: handle XAP
        switch (node.mContentType) {
            case HTML: {
                break;
            }
            case XAP: {
                break;
            }
            case URL: {
                break;
            }
            default: {

            }
        }

        new NodeDownloader(mNode, this, NodeDownloader.GET_CONTENT);
    }

    @Override
    public void onProgress(int progress) {
        mProgress.setValue(progress);
    }

    @Override
    public void onException(Exception exception) {
        mException.setValue(exception);
    }

    @Override
    public void onResult(Node node, NodeDownloader.Result result) {
        switch (result) {
            case SUCCESS_OUTDATED: {
                if (mOnOutdatedLoadedListener != null) {
                    mOnOutdatedLoadedListener.onOutdatedLoaded();
                }
                // no break intended
            }
            case SUCCESS: {
                //mHtmlFile.setValue(new String(data, StandardCharsets.ISO_8859_1));
                mHtmlFile.setValue(node.getContentCache());
                break;
            }
            case FAIL: {
                mHtmlFile.setValue(null);
                break;
            }
        }
    }

    public interface OnOutdatedLoadedListener {
        void onOutdatedLoaded();
    }

}
