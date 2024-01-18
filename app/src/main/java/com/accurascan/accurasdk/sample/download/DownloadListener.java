package com.accurascan.accurasdk.sample.download;

import com.androidnetworking.error.ANError;

public abstract class DownloadListener {

    public abstract void onDownloadComplete(String filePath);
    public void onProgress(int progress){}
    public abstract void onError(String s, ANError error);
}
