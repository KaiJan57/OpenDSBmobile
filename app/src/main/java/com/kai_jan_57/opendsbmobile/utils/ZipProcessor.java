package com.kai_jan_57.opendsbmobile.utils;

import android.os.AsyncTask;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.File;

class ZipProcessor extends AsyncTask<Integer, Integer, File> {

    private final File mDestinationFile;
    private final File mSourceFile;
    private final String mPassword;
    private final Callback mCallback;
    private Throwable mException;

    public ZipProcessor(File sourceFile, File destinationFile, String password, Callback callback) {
        this.mSourceFile = sourceFile;
        this.mDestinationFile = destinationFile;
        this.mPassword = password;
        this.mCallback = callback;
    }

    protected File doInBackground(Integer[] params) {
        int lastProgress = 0;
        try {
            if (this.mSourceFile.exists()) {
                ZipFile zipFile = new ZipFile(this.mSourceFile);
                zipFile.setRunInThread(true);
                ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
                if (zipFile.isEncrypted()) {
                    zipFile.setPassword(this.mPassword);
                }
                for (Object fileHeader : zipFile.getFileHeaders()) {
                    if (fileHeader instanceof FileHeader) {
                        FileHeader confirmedFileHeader = (FileHeader) fileHeader;
                        confirmedFileHeader.setFileName(confirmedFileHeader.getFileName().replace("\\", InternalZipConstants.ZIP_FILE_SEPARATOR));
                    }
                }
                zipFile.extractAll(this.mDestinationFile.getAbsolutePath());

                while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
                    int progress = progressMonitor.getPercentDone();
                    if (progress > lastProgress) {
                        publishProgress(progress);
                    } else {
                        progress = lastProgress;
                    }
                    lastProgress = progress;
                }
                if (progressMonitor.getResult() != ProgressMonitor.RESULT_ERROR) {
                    return this.mDestinationFile;
                }
                throw progressMonitor.getException();
            }
            else {
                mException = new Exception("File does not exist: " + mSourceFile.getAbsolutePath());
            }
        } catch (Throwable th) {
            mException = th;
        }
        return null;
    }

    protected void onPostExecute(File result) {

        if (this.mCallback != null) {
            if (result != null) {
                this.mCallback.onFinish(result);
            } else {
                this.mCallback.onError(mException);
            }
        }
    }

    protected void onProgressUpdate(Integer[] values) {

        if (this.mCallback != null) {
            this.mCallback.updateProgress((values)[0]);
        }
    }

    public interface Callback {
        void updateProgress(int progress);

        void onFinish(File result);

        void onError(Throwable pThrowable);
    }
}
