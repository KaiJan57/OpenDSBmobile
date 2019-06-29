package com.kai_jan_57.opendsbmobile.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.kai_jan_57.opendsbmobile.R;

import java.util.Collections;
import java.util.regex.Pattern;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class QrScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_PERMISSION = 4;

    static final int ACTIVITY_REQUEST_CODE = 3;

    static final String RESULT_ID = "id";
    static final String RESULT_PASSWORD = "password";

    private static final String SPLIT_SYMBOL = "<?|?>";

    private ZXingScannerView mScannerView;
    private ConstraintLayout mConstraintLayout;

    @Override
    public void handleResult(Result rawResult) {
        // splits strings like username<?|?>password into {username, password}
        String[] split = Pattern.compile(Pattern.quote(SPLIT_SYMBOL)).split(rawResult.getText());
        if (split.length == 2) {
            mScannerView.stopCamera();
            Intent intent = getIntent();
            intent.putExtra(RESULT_ID, split[0]);
            intent.putExtra(RESULT_PASSWORD, split[1]);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return;
        }
        Snackbar.make(mConstraintLayout, R.string.error_qr_no_cred, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, v-> mScannerView.resumeCameraPreview(this))
                .show();
    }

    public static void start(Activity parent) {
        Intent intent = new Intent(parent, QrScannerActivity.class);
        parent.startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
        mConstraintLayout = findViewById(R.id.constraintLayoutQr);
        mScannerView = findViewById(R.id.scannerView);
        mScannerView.setFormats(Collections.singletonList(BarcodeFormat.QR_CODE));
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        if (cameraPermissionGranted()) {
            mScannerView.startCamera();
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private boolean cameraPermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(CAMERA)) {
            Snackbar.make(mConstraintLayout, R.string.permission_camera_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, v -> requestPermissions(new String[]{CAMERA}, REQUEST_PERMISSION))
                    .show();
        } else {
            requestPermissions(new String[]{CAMERA}, REQUEST_PERMISSION);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mScannerView.startCamera();
            }
        }
    }
}
