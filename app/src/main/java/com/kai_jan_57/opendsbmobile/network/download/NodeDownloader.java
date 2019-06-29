package com.kai_jan_57.opendsbmobile.network.download;

import android.util.Log;

import com.kai_jan_57.opendsbmobile.Application;
import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.utils.LogUtils;

import java.io.FileOutputStream;
import java.io.IOException;

public class NodeDownloader implements DownloadTask.DownloadEventListener {

    private final String TAG = getClass().getCanonicalName();

    public static final int GET_CONTENT = 0;

    private final DownloadEventListener mDownloadEventListener;

    private DownloadTask mDownloadTask;

    private Node mNode;

    private int mRequestedResolution;

    public NodeDownloader(Node node, DownloadEventListener pDownloadEventListener, int resolution) {
        mDownloadEventListener = pDownloadEventListener;
        if (pDownloadEventListener != null) {
            mNode = node;
            mRequestedResolution = resolution;
            String downloadUrl;
            if (mRequestedResolution > 0) {
                if (mNode.isNewPreviewCacheRequired(mNode.mDate, mRequestedResolution)) {
                    downloadUrl = mNode.getPreviewUrl(mRequestedResolution);
                } else {
                    mDownloadEventListener.onResult(mNode, Result.SUCCESS);
                    return;
                }
            } else {
                if (mNode.isNewContentCacheRequired(mNode.mDate)) {
                    downloadUrl = mNode.mContent;
                } else {
                    mDownloadEventListener.onResult(mNode, Result.SUCCESS);
                    return;
                }
            }
            if (downloadUrl.isEmpty()) {
                mDownloadEventListener.onResult(mNode, Result.FAIL);
            } else {
                mDownloadTask = DownloadManager.getDownloadManager().download(downloadUrl, this);
            }
        }
    }

    public void cancel() {
        if (mDownloadTask != null) {
            mDownloadTask.cancel(false);
        }
    }

    @Override
    public void onProgress(int progress) {
        if (mDownloadEventListener != null) {
            mDownloadEventListener.onProgress(progress);
        }
    }

    @Override
    public void onDownloadFinished(byte[] data) {
        try {
            if (!mNode.getCacheDir().isDirectory() && !mNode.getCacheDir().mkdirs()) {
                throw new IOException("Could not create cache directory: " + mNode.getCacheDir().toString());
            }
            if (mRequestedResolution > 0) {
                if (!mNode.getPreviewCache().exists() && !mNode.getPreviewCache().createNewFile()) {
                    throw new IOException("Could not create preview cache file: " + mNode.getPreviewCache().toString());
                }
            } else {
                if (!mNode.getContentCache().exists() && !mNode.getContentCache().createNewFile()) {
                    throw new IOException("Could not create content cache file: " + mNode.getContentCache().toString());
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream((mRequestedResolution > 0) ? mNode.getPreviewCache() : mNode.getContentCache());
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
            if (mRequestedResolution > 0) {
                mNode.mPreviewCacheDate = mNode.mDate;
                mNode.mPreviewCacheResolution = mRequestedResolution;
            } else {
                mNode.mContentCacheDate = mNode.mDate;
            }
            Application.getInstance().getDatabase().getNodeDao().updateNode(mNode);
            mDownloadEventListener.onResult(mNode, Result.SUCCESS);
        } catch (IOException e) {
            // could not save to cache
            Log.e(TAG, LogUtils.getStackTrace(e));
            mDownloadEventListener.onException(e);
            mDownloadEventListener.onResult(mNode, Result.FAIL);
        }
    }

    @Override
    public void onDownloadFailed(Exception exception) {
        // last resort: check cache for old content
        mDownloadEventListener.onException(exception);
        if ((mRequestedResolution > 0) ? mNode.getPreviewCache().exists() : mNode.getContentCache().exists()) {
            // old cache exists
            mDownloadEventListener.onResult(mNode, Result.SUCCESS_OUTDATED);
        } else {
            mDownloadEventListener.onResult(mNode, Result.FAIL);
        }
    }

    public interface DownloadEventListener {
        void onProgress(int progress);

        void onException(Exception exception);

        void onResult(Node node, Result result);
    }

    public enum Result {
        SUCCESS,
        SUCCESS_OUTDATED,
        FAIL,
    }

}
