package com.accurascan.accurasdk.sample;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.accurascan.accurasdk.sample.download.DownloadListener;
import com.accurascan.accurasdk.sample.download.DownloadUtils;
import com.accurascan.facematch.util.Utils;
import com.accurascan.ocr.mrz.util.AccuraLog;
import com.androidnetworking.error.ANError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        AccuraLog.enableLogs(true);
        AccuraLog.refreshLogfile(this);

        View btnOCR = findViewById(R.id.btnAccuraOCR);
        View btnFace = findViewById(R.id.btnAccuraFace);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        btnOCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        btnFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ActivityFaceMatch.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        requestCameraPermission();
    }

    public void requestCameraPermission() {
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Start your camera handling here
                    try {
                        download();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(this, "You declined to allow the app to access your camera", Toast.LENGTH_SHORT).show();
                }
        }
    }

    String keyLicensePath;
    String faceLicensePath;
    public void downloadTextFile(Context context, final String link) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            onErrora("Please check your internet connection", null);
            return;
        }

        String oldVersion = sharedPreferences.getString(DownloadUtils.LICENSE_VERSION, "");
        String lastSavedFile = sharedPreferences.getString(DownloadUtils.LICENSE_NAME, "");
        String lastSavedFaceFile = sharedPreferences.getString(DownloadUtils.FM_LICENSE_NAME, "");

        File licenseDir = new File(context.getFilesDir().toString(), "accura");
        showDialog(loader_type);
        DownloadUtils.getInstance(new DownloadListener() {
            @Override
            public void onDownloadComplete(String licenseDetails) {
                dismissDialog(loader_type);
                String updatedVersion = "";
                String keyfileName = "";
                String facefileName = "";
                try {
                    JSONObject jsonObject = new JSONObject(licenseDetails);
                    if (!jsonObject.has("Android")) {
                        onErrora("Please add license details for Android", null);
                        return;
                    }
                    JSONObject object = jsonObject.getJSONObject("Android");
                    if (object.has("card_params") && !object.isNull("card_params")) {
                        String card_params = object.getJSONObject("card_params").toString();
                        sharedPreferences.edit().putString(DownloadUtils.CARD_PARAMS, card_params).apply();
                    }
                    if (object.has("ocr_license") && !TextUtils.isEmpty(object.getString("ocr_license"))) {
                        keyfileName = object.getString("ocr_license");
                    }
                    if (object.has("face_params") && !object.isNull("face_params")) {
                        String face_params = object.getJSONObject("face_params").toString();
                        sharedPreferences.edit().putString(DownloadUtils.FACE_PARAMS, face_params).apply();
                    }
                    if (object.has("face_license") && !TextUtils.isEmpty(object.getString("face_license"))) {
                        facefileName = object.getString("face_license");
                    }
                    if (jsonObject.has("version") && !TextUtils.isEmpty(jsonObject.getString("version"))) {
                        updatedVersion = jsonObject.getString("version");
                    }
                } catch (JSONException e) {
                    AccuraLog.loge(TAG, Log.getStackTraceString(e));
                }

                if (TextUtils.isEmpty(keyfileName)) {
                    onErrora("File details not valid", null);
                    return;
                }

                boolean downloadlicense;
                if (lastSavedFile.isEmpty() || oldVersion.isEmpty() || !updatedVersion.equals(oldVersion)) {
                    downloadlicense = true;
                } else {
                    downloadlicense = false;
                    File licenseFilePath = null;
                    if (new File(licenseDir, keyfileName).exists()) {
                        licenseFilePath = new File(licenseDir, keyfileName);
                    } else if (new File(licenseDir, lastSavedFile).exists()) {
                        licenseFilePath = new File(licenseDir, lastSavedFile);
                    }
                    if (licenseFilePath != null) {
                        keyLicensePath = licenseFilePath.getPath();
                        sharedPreferences.edit().putString(DownloadUtils.LICENSE_PATH, keyLicensePath).apply();
                    } else {
                        onErrora(DownloadUtils.ERROR_CODE_FILE_NOT_FOUND, null);
                    }
                }

                if (lastSavedFaceFile.isEmpty() || oldVersion.isEmpty() || !updatedVersion.equals(oldVersion)) {
                    String finalKeyfileName = keyfileName;
                    String finalUpdatedVersion = updatedVersion;
                    showDialog(progress_bar_type);
                    AccuraLog.loge(TAG, "Start Download FM license : " );
                    DownloadUtils.getInstance(new DownloadListener() {
                        @Override
                        public void onDownloadComplete(String fileName) {
                            sharedPreferences.edit().putString(DownloadUtils.LICENSE_VERSION, finalUpdatedVersion).apply();
                            sharedPreferences.edit().putString(DownloadUtils.FM_LICENSE_NAME, fileName).apply();
                            faceLicensePath =  licenseDir.getPath() + "/" + fileName;
                            sharedPreferences.edit().putString(DownloadUtils.FM_LICENSE_PATH, faceLicensePath).apply();
                            if (downloadlicense)
                                downloadKeyFile(finalKeyfileName, finalUpdatedVersion);
                            else {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            }
                        }

                        @Override
                        public void onProgress(int progress) {
                            if (pDialog != null) {
                                pDialog.setProgress(progress);
                                AccuraLog.loge(TAG, "on FM Progress : " + progress);
                            }
                        }

                        @Override
                        public void onError(String s, ANError error) {
                            AccuraLog.loge(TAG, "onError: " + Log.getStackTraceString(error));
                            if (downloadlicense)
                                downloadKeyFile(finalKeyfileName, finalUpdatedVersion);
                            else {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                onError("FM license error "+s, error);
                            }
                        }

                    }).downloadLicenseFile(licenseDir.getPath(), link.substring(0, link.lastIndexOf("/")+1)+ facefileName, facefileName, lastSavedFaceFile);
                } else {
                    File licenseFilePath = null;
                    if (new File(licenseDir, facefileName).exists()) {
                        licenseFilePath = new File(licenseDir, facefileName);
                    } else if (new File(licenseDir, lastSavedFaceFile).exists()) {
                        licenseFilePath = new File(licenseDir, lastSavedFaceFile);
                    }
                    if (licenseFilePath != null) {
                        faceLicensePath = licenseFilePath.getPath();
                        sharedPreferences.edit().putString(DownloadUtils.FM_LICENSE_PATH, faceLicensePath).apply();
                    } else {
                        onErrora(DownloadUtils.ERROR_CODE_FILE_NOT_FOUND, null);
                    }
                    if (downloadlicense)
                        downloadKeyFile(keyfileName, updatedVersion);
                }
            }

            private void downloadKeyFile(String finalKeyfileName, String finalUpdatedVersion) {
                showDialog(progress_bar_type);
                AccuraLog.loge(TAG, "Start Download key license : " );

                DownloadUtils.getInstance(new DownloadListener() {
                    @Override
                    public void onDownloadComplete(String fileName) {
                        sharedPreferences.edit().putString(DownloadUtils.LICENSE_VERSION, finalUpdatedVersion).apply();
                        sharedPreferences.edit().putString(DownloadUtils.LICENSE_NAME, fileName).apply();
                        keyLicensePath = licenseDir.getPath() + "/" + fileName;
                        sharedPreferences.edit().putString(DownloadUtils.LICENSE_PATH, keyLicensePath).apply();
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }

                    @Override
                    public void onProgress(int progress) {
                        if (pDialog != null) {
                            pDialog.setProgress(progress);
                            AccuraLog.loge(TAG, "on Key Progress : " + progress);

                        }
                    }

                    @Override
                    public void onError(String s, ANError error) {
                        AccuraLog.loge(MenuActivity.class.getSimpleName(), "Download key file: " +  Log.getStackTraceString(error));
                        error.printStackTrace();
                        sharedPreferences.edit().putString(DownloadUtils.LICENSE_VERSION, "").apply();
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        onErrora("Key license error " + s, error);
                    }
                }).downloadLicenseFile(licenseDir.getPath(), link.substring(0, link.lastIndexOf("/")+1)+ finalKeyfileName, finalKeyfileName, lastSavedFile);
            }

            @Override
            public void onError(String s, ANError error) {
                dismissDialog(loader_type);
                onErrora(s, error);
            }
        }).parseFile(licenseDir.getPath(), link, link.substring(link.lastIndexOf("/")));
    }

    public void onErrora(String s, ANError error) {
        Toast.makeText(MenuActivity.this, s+"\n"+Log.getStackTraceString(error), Toast.LENGTH_SHORT).show();
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    private void download() {
        downloadTextFile(this, "https://example.com/config.json");
    }

    // Progress Dialog
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    public static final int loader_type = 1;

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setCancelable(false);
                pDialog.show();
                return pDialog;
            case loader_type: // we set this to 1
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Please wait...");
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setCancelable(false);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

}
