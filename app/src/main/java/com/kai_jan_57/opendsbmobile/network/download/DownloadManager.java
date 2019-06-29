package com.kai_jan_57.opendsbmobile.network.download;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {

    private static DownloadManager mDownloadManager;

    public static DownloadManager getDownloadManager() {
        if (mDownloadManager == null) {
            mDownloadManager = new DownloadManager();
        }
        return mDownloadManager;
    }

    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(3);
    private final List<DownloadTask> mDownloadTasks = new ArrayList<>();

    public DownloadTask download(String url, DownloadTask.DownloadEventListener downloadEventListener) {
        if (url.isEmpty()) {
            return null;
        }
        for (DownloadTask downloadTask : mDownloadTasks) {
            if (downloadTask.getUrl() != null && downloadTask.getUrl().equals(url)) {
                downloadTask.addDownloadEventListener(downloadEventListener);
                return downloadTask;
            }
        }
        DownloadTask downloadTask = new DownloadTask(this, url);
        downloadTask.addDownloadEventListener(downloadEventListener);
        mDownloadTasks.add(downloadTask);
        downloadTask.executeOnExecutor(mExecutorService);
        return downloadTask;
    }

    public void removeDownloadTask(DownloadTask downloadTask) {
        mDownloadTasks.remove(downloadTask);
    }

}
