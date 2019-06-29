package com.kai_jan_57.opendsbmobile.viewmodels;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.kai_jan_57.opendsbmobile.Application;
import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.network.download.NodeDownloader;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;

public class ImageviewerViewModel extends ViewModel implements NodeDownloader.DownloadEventListener {

    private final String TAG = getClass().getCanonicalName();

    private final MutableLiveData<Bitmap> mBitmap = new MutableLiveData<>();
    private final MutableLiveData<Integer> mProgress = new MutableLiveData<>();
    private final MutableLiveData<Exception> mException = new MutableLiveData<>();

    public ImageviewerViewModel() {
        super();
    }

    public LiveData<Bitmap> getBitmap() {
        return mBitmap;
    }

    public LiveData<Exception> getException() {
        return mException;
    }

    private Node mNode;

    public Date getDate() {
        return mNode.mDate;
    }

    public void loadImage(Node node) {
        mNode = node;
        new NodeDownloader(mNode, this, NodeDownloader.GET_CONTENT);
    }

    private boolean applyImage(Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        }
        mBitmap.setValue(bitmap);
        return true;
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
        if (result != NodeDownloader.Result.FAIL && !applyImage(BitmapFactory.decodeFile(node.getContentCache().getAbsolutePath()))) {
            mException.setValue(new Exception("Could not decode image"));
            // no need to keep broken data
            if (!node.getPreviewCache().delete()) {
                Log.e(TAG, "Could not delete broken preview cache for node: " + node.toString());
            }
            node.mContentCacheDate = null;
            Application.getInstance().getDatabase().getNodeDao().updateNode(node);
        }
    }

}
