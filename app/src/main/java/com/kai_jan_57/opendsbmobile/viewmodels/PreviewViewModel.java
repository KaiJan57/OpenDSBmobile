package com.kai_jan_57.opendsbmobile.viewmodels;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kai_jan_57.opendsbmobile.Application;
import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.network.download.NodeDownloader;

public class PreviewViewModel extends ViewModel implements NodeDownloader.DownloadEventListener {

    private final String TAG = getClass().getCanonicalName();

    public static final String ERROR_NO_PREVIEW = "ERROR_NO_PREVIEW";

    private final MutableLiveData<String> mTitle = new MutableLiveData<>();
    private final MutableLiveData<String> mSubtitle = new MutableLiveData<>();

    private final MutableLiveData<Status> mStatus = new MutableLiveData<>();
    private Exception mLastException;
    private final MutableLiveData<Bitmap> mPreviewImage = new MutableLiveData<>();

    private final MutableLiveData<Integer> mProgress = new MutableLiveData<>();

    private Node mPreviewContentNode = null;

    public PreviewViewModel() {
        super();
        mStatus.setValue(Status.IDLE);
    }

    public LiveData<Status> getStatus() {
        return mStatus;
    }

    public Exception getLastException() {
        return mLastException;
    }

    private void setException(Exception exception) {
        mLastException = exception;
        mStatus.setValue(Status.FAILED);
    }

    public LiveData<Bitmap> getPreviewImage() {
        return mPreviewImage;
    }

    public void loadPreviewImage(Node node, int resolution) {
        for (Node child : Application.getInstance().getDatabase().getNodeDao().getChildren(node.mId)) {
            if (!child.mPreviewUrl.isEmpty()) {
                mPreviewContentNode = child;
            }
        }
        if (mPreviewContentNode == null) {
            setException(new Exception(ERROR_NO_PREVIEW));
        } else {
            mStatus.setValue(Status.LOADING);
            new NodeDownloader(mPreviewContentNode, this, resolution);
        }
    }

    private boolean applyImage(Bitmap bitmap) {
        // crop to square
        if (bitmap == null) {
            return false;
        }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight() < bitmap.getWidth() ? bitmap.getHeight() : bitmap.getWidth());
        if (bitmap == null) {
            return false;
        }
        mPreviewImage.setValue(bitmap);
        mStatus.setValue(Status.COMPLETED);
        return true;
    }

    public LiveData<String> getTitle() {
        return mTitle;
    }

    public LiveData<String> getSubtitle() {
        return mSubtitle;
    }

    public LiveData<Integer> getProgress() {
        return mProgress;
    }

    @Override
    public void onProgress(int progress) {
        mProgress.setValue(progress);
    }

    @Override
    public void onException(Exception exception) {
        setException(exception);
    }

    @Override
    public void onResult(Node node, NodeDownloader.Result result) {
        if (result != NodeDownloader.Result.FAIL && !applyImage(BitmapFactory.decodeFile(node.getPreviewCache().getAbsolutePath()))) {
            setException(new Exception("Could not decode image"));
            // no need to keep broken data
            if (!node.getPreviewCache().delete()) {
                Log.e(TAG, "Could not delete broken preview cache for node: " + node.toString());
            }
            node.mPreviewCacheDate = null;
            Application.getInstance().getDatabase().getNodeDao().updateNode(node);
        }
    }

    public enum Status {
        IDLE,
        LOADING,
        COMPLETED,
        FAILED,
    }
}
