package com.kai_jan_57.opendsbmobile.utils;

import android.util.Base64;
import android.util.Log;

import java.io.File;

public class FileUtils {

    @SuppressWarnings("FieldCanBeLocal")
    private static final String TAG = "com.kai_jan_57.opendsbmobile.utils.FileUtils";

    public static void deleteRecursively(File file) {
        if (file.exists() && file.setWritable(true)) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File subNode : files) {
                    deleteRecursively(subNode);
                }
            }
            if(!file.delete()) {
                Log.e(TAG, "In deleteRecursively(File): Could not be deleted: " + file.toString());
            }
        } else {
            Log.e(TAG, "In deleteRecursively(File): exists() && setWritable(true) failed on: " + file.toString());
        }
    }

    public static String encodeSafeFilename(String unsafe) {
        return Base64.encodeToString(unsafe.getBytes(), Base64.URL_SAFE).replace('=', '_').replace("\n", "");
    }
/*
    public static String decodeSafeFilename(String safe) {
        return new String(Base64.decode(safe, Base64.URL_SAFE));
    }
*/
    public static String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.'));
    }

}
