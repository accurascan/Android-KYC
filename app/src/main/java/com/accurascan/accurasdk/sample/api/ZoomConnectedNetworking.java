package com.accurascan.accurasdk.sample.api;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facetec.zoom.sdk.ZoomSDK;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class ZoomConnectedNetworking {
    String appToken;
    public static String packageName;
    ProgressBar uploadProgressBar;
    TextView resultTextView;
    Context mainActivity;

    public interface Request {
        void onSuccessResponse(String result);
    }

    public ZoomConnectedNetworking(String appToken, String packageName, Context mainActivity) {
        this.packageName = packageName;
        this.appToken = appToken;
        this.mainActivity = mainActivity;
//        this.uploadProgressBar = ((Activity) mainActivity).findViewById(R.id.uploadProgressBar);
//        this.resultTextView = ((Activity) mainActivity).findViewById(R.id.resultTextView);
    }

    String zoomUserAgentStringWithDeviceAndApplicationProperties(String appToken, String sessionId) {
        Locale locale = Locale.getDefault();
        String installationId = "_";

        return "facetec|zoomsdk|android|" + packageName + "|" + appToken + "|" + installationId +
                "|" + Build.MODEL + "|" + ZoomSDK.version() + "|" + locale.toString() +
                "|" + locale.getLanguage() + "|" + sessionId;
    }

    void performHTTPRequest(final String url, final String method, final JSONObject parameters, final int retries, final Request callback) {

        Log.d("~~OKHTTP", url);

        String sessionId = "";
        if(parameters != null) {
            if(parameters.has("sessionId")) {
                try {
                    sessionId = parameters.getString("sessionId");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
                .header("Content-Type", "application/json")
                .header("X-App-Token", appToken)
                .header("User-Agent", zoomUserAgentStringWithDeviceAndApplicationProperties(appToken,sessionId))
                .url(url);

        if(parameters != null) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody requestBody = RequestBody.create(JSON, parameters.toString());

            // Decorate the request body to keep track of the upload progress

            if(method.equals("POST")) {
                builder.post(requestBody);
            }
            else if(method.equals("GET")) {
                builder.get();
            }
            else if(method.equals("DELETE")) {
                builder.delete();
            }
        }

        okhttp3.Request request = builder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("~~OKHTTP3", e.toString());
                if(retries > 0) {
                    ((Activity) mainActivity).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    performHTTPRequest(url, method, parameters, retries-1, callback);
                                }
                            }, 5000);
                            return;
                        }
                    });
                }
                else {
                    callback.onSuccessResponse(e.toString());
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String responseString = response.body().string();
                Log.d("~~OKHTTP", responseString);
                response.body().close();
                callback.onSuccessResponse(responseString);
            }
        });
    }
}
