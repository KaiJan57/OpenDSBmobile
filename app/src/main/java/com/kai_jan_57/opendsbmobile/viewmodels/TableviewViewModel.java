package com.kai_jan_57.opendsbmobile.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.network.download.NodeDownloader;
import com.kai_jan_57.opendsbmobile.tableparser.Parser;
import com.kai_jan_57.opendsbmobile.utils.LogUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class TableviewViewModel extends ViewModel implements NodeDownloader.DownloadEventListener {

    private final String TAG = getClass().getCanonicalName();

    private MutableLiveData<String> mTitle = new MutableLiveData<>();
    private MutableLiveData<List<String>> mColumnHeaders = new MutableLiveData<>();
    private MutableLiveData<List<List<String>>> mTableContent = new MutableLiveData<>();

    private final MutableLiveData<Integer> mProgress = new MutableLiveData<>();
    private final MutableLiveData<Exception> mException = new MutableLiveData<>();

    private Node mNode;

    public TableviewViewModel() {
        super();
    }

    public Date getDate() {
        if (mNode == null) {
            return null;
        }
        return mNode.mDate;
    }

    public LiveData<String> getTitle() {
        return mTitle;
    }

    public LiveData<List<String>> getColumnHeaders() {
        return mColumnHeaders;
    }

    public LiveData<List<List<String>>> getTableContent() {
        return mTableContent;
    }

    public void loadTable(Node node) {
        mNode = node;
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
        if (result != NodeDownloader.Result.FAIL) {
            Parser parser;
            try {
                parser = Parser.getParserByContent(node.getContentCache());
            } catch (IOException e) {
                Log.e(TAG, LogUtils.getStackTrace(e));
                return;
            }
            int tableCount = parser.getTableCount();
            if (tableCount > 0) {
                /*StringBuilder titleBuilder = new StringBuilder(parser.getTitle());
                for (String string : parser.getDocumentMetadata()) {
                    titleBuilder.append('\n').append(string);
                }
                titleBuilder.append('\n').append(parser.getTableTitle(0));
                mTitle.setValue(titleBuilder.toString());*/
                mTitle.setValue(parser.getTableTitle(0));
                mColumnHeaders.setValue(parser.getColumnHeaders(0));
                mTableContent.setValue(parser.getRows(0));
            }
        }
    }
}
