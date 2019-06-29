package com.kai_jan_57.opendsbmobile.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.common.io.Files;
import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.Application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

public class ShareUtils {

    private static final String FILE_PROVIDER_AUTHORITY = Application.getInstance().getResources().getString(R.string.content_authority);
    public static final int SHARE_REQUEST_CODE = 3;
    private static final String SHARE_CACHE = "ShareData";

    private static Uri prepareFileForSharing(Context pContext, File pFile, String pFilename) throws IOException {
        File cachePath = new File(pContext.getCacheDir(), SHARE_CACHE);
        if (!cachePath.isDirectory() && !cachePath.mkdirs()) {
            throw new IOException("Could not create cache directory");
        }
        cachePath = new File(cachePath, pFilename);
        //noinspection UnstableApiUsage
        Files.copy(pFile, cachePath);

        return FileProvider.getUriForFile(pContext, FILE_PROVIDER_AUTHORITY, cachePath);
    }

    public static void shareFile(@NonNull Context pContext, File pFile, String pFilename, String pChooserTitle) throws IOException {
        Uri contentUri = prepareFileForSharing(pContext, pFile, pFilename);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType(pContext.getContentResolver().getType(contentUri));
        Intent chooser = Intent.createChooser(shareIntent, pChooserTitle);
        if (pContext instanceof Activity) {
            ((Activity) pContext).startActivityForResult(chooser, SHARE_REQUEST_CODE);
        } else {
            pContext.startActivity(chooser);
        }
    }

    public static void shareFiles(@NonNull Context pContext, List<File> pFiles, List<String> pFilenames, String pChooserTitle) throws IOException {
        if (pFilenames.size() == pFiles.size() && !pFilenames.isEmpty()) {
            ArrayList<Uri> contentUri = new ArrayList<>();

            for (int i = 0; i < pFilenames.size(); i++) {
                contentUri.add(prepareFileForSharing(pContext, pFiles.get(i), pFilenames.get(i)));
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setType(pContext.getContentResolver().getType(contentUri.get(0)));
            Intent chooser = Intent.createChooser(shareIntent, pChooserTitle);
            if (pContext instanceof Activity) {
                ((Activity) pContext).startActivityForResult(chooser, SHARE_REQUEST_CODE);
            } else {
                pContext.startActivity(chooser);
            }
        }
    }

    public static void shareLink(@NonNull Context pContext, String pLink, String pChooserTitle) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, pLink);
        shareIntent.setType("text/plain");
        Intent chooser = Intent.createChooser(shareIntent, pChooserTitle);
        if (pContext instanceof Activity) {
            ((Activity) pContext).startActivityForResult(chooser, SHARE_REQUEST_CODE);
        } else {
            pContext.startActivity(chooser);
        }
    }

    public static void shareText(@NonNull Context pContext, String pText, String pMimeType, String pChooserTitle) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, pText);
        shareIntent.setType(pMimeType);
        Intent chooser = Intent.createChooser(shareIntent, pChooserTitle);
        if (pContext instanceof Activity) {
            ((Activity) pContext).startActivityForResult(chooser, SHARE_REQUEST_CODE);
        } else {
            pContext.startActivity(chooser);
        }
    }

    public static void shareBitmap(@NonNull Context pContext, Bitmap pBitmap, String pChooserTitle) throws IOException {

        File cachePath = new File(pContext.getCacheDir(), SHARE_CACHE);
        if (!cachePath.isDirectory() && !cachePath.mkdirs()) {
            throw new IOException("Could not create cache directory");
        }
        cachePath = new File(cachePath, UUID.randomUUID() + "_webview_share.png");

        pBitmap.compress(Bitmap.CompressFormat.PNG, 90, new FileOutputStream(cachePath));

        Uri contentUri = FileProvider.getUriForFile(pContext, FILE_PROVIDER_AUTHORITY, cachePath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType(pContext.getContentResolver().getType(contentUri));
        Intent chooser = Intent.createChooser(shareIntent, pChooserTitle);
        if (pContext instanceof Activity) {
            ((Activity) pContext).startActivityForResult(chooser, SHARE_REQUEST_CODE);
        } else {
            pContext.startActivity(chooser);
        }
    }

    public static void cleanCache(Context pContext) {
        File[] cachedFiles = new File(pContext.getCacheDir(), SHARE_CACHE).listFiles();
        if (cachedFiles != null) {
            for (File file : cachedFiles) {
                FileUtils.deleteRecursively(file);
            }
        }
    }
}
