package com.accurascan.accurasdk.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import com.accurascan.ocr.mrz.util.Util;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Util.isPermissionsGranted(SplashActivity.this)) {
                    requestCameraPermission();
                } else {
                    nextActivity(); //memory leak
                }
            }
        }, 3000);
    }

    private void nextActivity() {
        startActivity(new Intent(this, MenuActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        overridePendingTransition(0, 0);
        finish();
    }

    //requesting the camera permission
    public void requestCameraPermission() {
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
//            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                requestCameraPermission();
//            }
            switch (requestCode) {
                case 1:
                    // If request is cancelled, the result arrays are empty.
                    int i = 0;
                    boolean showRationaleStorage = false;
                    boolean showRationaleCamera = false;
                    boolean callStorage = false;
                    boolean callCamera = false;

                    for (String per : permissions) {
                        if (grantResults[i++] == PackageManager.PERMISSION_DENIED) {
                            boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, per);
                            if (!showRationale) {
                                //user also CHECKED "never ask again"
                                if (per.equals(Manifest.permission.CAMERA)) {
                                    showRationaleCamera = true;
                                } else if (per.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    showRationaleStorage = true;
                                }
                            } else if (per.equals(Manifest.permission.CAMERA)) {
                                callCamera = true;
                            } else if (per.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                callStorage = true;
                            }
                        }
                    }
                    if (showRationaleCamera && showRationaleStorage)
                        Toast.makeText(this, "You declined to allow the app to access Camera and Storage", Toast.LENGTH_SHORT).show();
                    else if (showRationaleCamera)
                        Toast.makeText(this, "You declined to allow the app to access Camera", Toast.LENGTH_SHORT).show();
                    else if (showRationaleStorage)
                        Toast.makeText(this, "You declined to allow the app to access Storage", Toast.LENGTH_SHORT).show();
                    else if (callStorage && callCamera)
                        Toast.makeText(this, "Please allow app to access Camera and Storage", Toast.LENGTH_SHORT).show();
                    else if (callCamera)
                        Toast.makeText(this, "Please allow app to access Camera", Toast.LENGTH_SHORT).show();
                    else if (callStorage)
                        Toast.makeText(this, "Please allow app to access Storage", Toast.LENGTH_SHORT).show();
                    else {
                        nextActivity();
                        return;
                    }
                    if (callCamera || callStorage) {
                        requestCameraPermission();
                    }
            }
        } else {
            requestCameraPermission();
        }
    }
}
