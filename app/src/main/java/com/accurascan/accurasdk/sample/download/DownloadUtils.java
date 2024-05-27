package com.accurascan.accurasdk.sample.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.accurascan.ocr.mrz.util.AccuraLog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DownloadUtils {
    public static final String CARD_PARAMS = "card_params";
    public static final String FACE_PARAMS = "face_params";
    private static final String TAG = DownloadUtils.class.getSimpleName();
    public static String LICENSE_VERSION = "license_version";
    public static String LICENSE_NAME = "ocr_license_name";
    public static String LICENSE_PATH = "ocr_license_path";
    public static String FM_LICENSE_NAME = "fm_license_name";
    public static String FM_LICENSE_PATH = "fm_license_path";
    public static String ERROR_CODE_FILE_NOT_FOUND = "File not found";
    private static SCAN_RESULT mCallback;
    private static com.accurascan.accurasdk.sample.download.DownloadListener mListener;

    public interface SCAN_RESULT {

        void onSuccess(String id, String message);

        void onFailed(String s);
    }

    private static final DownloadUtils ourInstance = new DownloadUtils();

    public static DownloadUtils getInstance(SCAN_RESULT callback) {
        mCallback = callback;
        return ourInstance;
    }

    public static DownloadUtils getInstance(com.accurascan.accurasdk.sample.download.DownloadListener callback) {
        mListener = callback;
        return ourInstance;
    }

    private DownloadUtils() {
    }

    public boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void onFailed(ANError error, com.accurascan.accurasdk.sample.download.DownloadListener mCallback) {
        if (mCallback != null) {
            if (error.getErrorCode() == 404) {
                mCallback.onError(ERROR_CODE_FILE_NOT_FOUND, error);
                return;
            } else if (error.getErrorDetail().equalsIgnoreCase("connectionerror")) {
                mCallback.onError("Internet connection error", error);
                return;
            }
            mCallback.onError(error.getErrorDetail(), error);
        }
    }

    private String readText (String input) {
        File file = new File (input);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line=br.readLine())!=null){
                text.append(line);
                text.append("\n");
            }
            br.close();
        } catch (IOException e) {
            AccuraLog.loge(TAG, Log.getStackTraceString(e));
        }
        if (file.exists()) {
            file.delete();
        }
        return text.toString();
    }

    public void parseFile(String path, String link, String fileName) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path+fileName);
        if (file.exists()) {
            file.delete();
        }
        AndroidNetworking.download(link, path, fileName)
                .setTag("parseData")
                .setPriority(Priority.HIGH)
                .build()
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        String licenseDetails = readText(path + "/" + fileName);
                        if (licenseDetails.isEmpty()) {
                            mListener.onError("File not added", null);
                            return;
                        }
                        if (mListener != null)
                            mListener.onDownloadComplete(licenseDetails);
                    }

                    @Override
                    public void onError(ANError error) {
                        error.printStackTrace();
                        if (mListener != null)
                            onFailed(error, mListener);
                    }
                });
    }

    public void downloadLicenseFile(String path, String link, String fileName, String lastSavedFile) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (fileName.equals(lastSavedFile)) {
            String temp = fileName;
            int i = 1;
            while (new File(path, temp).exists()) {
                temp = i+"_" + fileName;
                i++;
            }
            fileName = temp;
        }
        String finalFileName = fileName;
        AndroidNetworking.download(link, path, finalFileName)
                .setTag("downloadTest")
                .setPriority(Priority.HIGH)
                .build()
                .setDownloadProgressListener(new DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long totalBytes) {
                        if (mListener != null) {
                            mListener.onProgress((int) ((bytesDownloaded * 100.0) / (float )totalBytes));
                        }
                    }
                })
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        if (!lastSavedFile.equals(finalFileName)) {
                            File old = new File(path, lastSavedFile);
                            if (old.exists()) {
                                old.delete();
                            }
                        }
                        if (mListener != null)
                            mListener.onDownloadComplete(finalFileName);
                    }

                    @Override
                    public void onError(ANError error) {
                        if (mListener != null)
                            onFailed(error, mListener);
                    }
                });
    }
}