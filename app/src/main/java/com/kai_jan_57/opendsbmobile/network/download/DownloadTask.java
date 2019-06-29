package com.kai_jan_57.opendsbmobile.network.download;

import android.os.AsyncTask;

import com.google.common.io.ByteStreams;
import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.Application;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static java.net.HttpURLConnection.HTTP_OK;

public class DownloadTask extends AsyncTask<Void, Integer, Object> {

    private static final String BUNDLE_ID = "BUNDLE_ID";

    private final DownloadManager mDownloadManager;

    private final String mUrl;

    private final List<DownloadEventListener> mDownloadEventListeners = new ArrayList<>();

    DownloadTask(DownloadManager downloadManager, String url) {
        mDownloadManager = downloadManager;
        mUrl = url;
    }

    String getUrl() {
        return mUrl;
    }

    void addDownloadEventListener(DownloadEventListener downloadEventListener) {
        mDownloadEventListeners.add(downloadEventListener);
    }

    @Override
    protected Object doInBackground(Void[] voids) {
        byte[] buffer;
        try {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(mUrl).openConnection();
            httpsURLConnection.setRequestProperty(BUNDLE_ID, Application.getInstance().getResources().getString(R.string.BUNDLE_ID));
            int responseCode = httpsURLConnection.getResponseCode();
            if (responseCode == HTTP_OK) {
                int contentLength = httpsURLConnection.getContentLength();
                InputStream inputStream = httpsURLConnection.getInputStream();
                if (contentLength == -1) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ByteStreams.copy(inputStream, byteArrayOutputStream);
                    buffer = byteArrayOutputStream.toByteArray();
                    if (buffer.length == 0) {
                        return new Exception("No data received");
                    }
                } else if (contentLength > 0) {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    buffer = new byte[contentLength];
                    int progress = 0;
                    int lastProgress = 0;
                    int readLength;
                    do {
                        readLength = bufferedInputStream.read(buffer, progress, contentLength - progress);

                        progress += readLength;
                        if (progress != lastProgress) {
                            publishProgress(100 * progress / contentLength);
                            lastProgress = progress;
                        }
                    } while (readLength > 0);
                    httpsURLConnection.disconnect();
                } else {
                    //No data
                    return new Exception("No content.");
                }

            } else {
                return new Exception("HTTP: " + responseCode);
            }
        } catch (MalformedURLException e) {
            return e;
        } catch (IOException e) {
            return e;
        }
        return buffer;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        for (DownloadEventListener downloadEventListener : mDownloadEventListeners) {
            downloadEventListener.onProgress(progress[0]);
        }
    }

    @Override
    protected void onPostExecute(Object object) {
        mDownloadManager.removeDownloadTask(this);
        for (DownloadEventListener downloadEventListener : mDownloadEventListeners) {
            if (object instanceof Exception) {
                downloadEventListener.onDownloadFailed((Exception) object);
            } else {
                downloadEventListener.onDownloadFinished((byte[]) object);
            }
        }
    }

    @Override
    protected void onCancelled() {
        mDownloadManager.removeDownloadTask(this);
    }

    public interface DownloadEventListener {
        void onProgress(int progress);

        void onDownloadFinished(byte[] data);

        void onDownloadFailed(Exception exception);
    }
}
