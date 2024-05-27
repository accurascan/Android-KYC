package com.accurascan.accurasdk.sample.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.accurascan.ocr.mrz.model.OcrData;
import com.accurascan.ocr.mrz.util.AccuraLog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class ArabicApiUtils {
    private static final String TAG = ArabicApiUtils.class.getSimpleName();
    private static SCAN_RESULT mCallback;
    private ProgressDialog pd;
    private boolean done1;
    private boolean done2;
    private String frontError, backError;

    public interface SCAN_RESULT {

        void onSuccess(String id, String message);

        void onFailed(String s);
    }

    private static final ArabicApiUtils ourInstance = new ArabicApiUtils();

    public static ArabicApiUtils getInstance(SCAN_RESULT callback) {
        mCallback = callback;
        return ourInstance;
    }

    private ArabicApiUtils() {
    }

    /**
     *
     * @param cardside 0, 1 and 2. set 0 for both front and back, 1 for front side and set 2 for back side
     * @param context
     * @param countryCode for kuwait - "KWT" and for Bahrain - "BHR"
     * @param ocrData receive data after scanned completed at onScannedComplete(Object result), to set arabic details
     * @param serverUrl Add url for api calling
     * @param timeout not require set -1 for default
     */
    public void getArabicApiData(int cardside, final Context context, final String countryCode, final OcrData ocrData, String serverUrl, int timeout) {
        done1 = done2 = false;
        frontError = backError = "";
        boolean isApiFEnabled = false, isApiBEnabled = false;
        if (cardside == 0 || cardside == 1)
            if (ocrData.getFrontData() != null) {
                isApiFEnabled = true;
            }
        if (cardside == 0 || cardside == 2)
            if (ocrData.getBackData() != null) {
                isApiBEnabled = true;
            }
        if (isApiFEnabled || isApiBEnabled) {
            if (!isNetworkAvailable(context)) {
                if (mCallback != null) {
                    mCallback.onFailed("Please check your internet connection");
                }
                return;
            }
            if (TextUtils.isEmpty(serverUrl)) {
                if (mCallback != null) {
                    mCallback.onFailed("Server URL not Added");
                }
                return;
            }
            if ((isApiFEnabled && ocrData.getFrontData() != null) || (isApiBEnabled && ocrData.getBackData() != null)){
                initialize(context);
            }
            done1 = !isApiFEnabled;
            done2 = !isApiBEnabled;
            if (isApiFEnabled) getArabicOcrDetails(context,true,countryCode, ocrData, serverUrl, timeout, 2, isApiBEnabled); // set i = 2 , To stop retry api calling on api error
            else getArabicOcrDetails(context, false,countryCode, ocrData, serverUrl, timeout, 2, false);
        } else if (mCallback != null) {
            mCallback.onSuccess("1", "Success");
        }

    }

    private void initialize(Context context) {

        if (pd == null) {
            pd = new ProgressDialog(context);
            pd.setCancelable(false);
            pd.setCanceledOnTouchOutside(false);
            pd.setMessage("Loading");
        }
        try {
            if (pd != null && !pd.isShowing()) {
                pd.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static File imageToFile(Context context, Bitmap bitmap, String filename) {
        //create a file to write bitmap data
        File f = new File(context.getCacheDir(), filename);
        try {
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
        } catch (IOException e) {
            AccuraLog.loge(TAG, Log.getStackTraceString(e));
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            AccuraLog.loge(TAG, Log.getStackTraceString(e));
        } catch (IOException e) {
            AccuraLog.loge(TAG, Log.getStackTraceString(e));
        }

        return f;
    }

    private void getArabicOcrDetails(Context context, boolean isFront, String countryCode, OcrData ocrData, String serverUrl, int timeout, final int i, boolean isBothEnabled) {
        List<OcrData.MapData.ScannedData> scannedDataList;
        Bitmap bmp;
        String filename = "";
        File imageFile;

        Map<String, String> jsonObject = new HashMap<>();
        jsonObject.put("country_code", countryCode);

        if (isFront) {
            bmp = ocrData.getFrontimage();
            scannedDataList = ocrData.getFrontData().getOcr_data();
            filename = "frontImage_"+System.currentTimeMillis()+".jpg";
            imageFile = imageToFile(context, bmp, filename);
        } else {
            bmp = ocrData.getBackimage();
            scannedDataList = ocrData.getBackData().getOcr_data();
            filename = "backImage_"+System.currentTimeMillis()+".jpg";
            imageFile = imageToFile(context, bmp, filename);
        }

//        String encodedImage = base64FromBitmap(bmp);
        Map<String, File> multiPartFileMap = new HashMap<>();
        multiPartFileMap.put("file", imageFile);

        ANRequest.MultiPartBuilder request = AndroidNetworking.upload(serverUrl)
                .addMultipartFile(multiPartFileMap)
                .addMultipartParameter(jsonObject);
        if (timeout > -1) {
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .build();
            request.setOkHttpClient(okHttpClient); // passing a custom okHttpClient
        }
        request.setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (isBothEnabled) {
                            getArabicOcrDetails(context, !isFront, countryCode, ocrData, serverUrl, timeout, i, false);
                        }
                        String status = "";
                        String message = "";
                        try {
                            if (response.has("Status")) {
                                status = response.getString("Status");
                            }
                            if (response.has("Message")) {
                                message = response.getString("Message");
                            }
                        } catch (JSONException e) {
                            AccuraLog.loge(TAG, Log.getStackTraceString(e));
                        }
                        if (status.equalsIgnoreCase("Success")) {
                            try {
                                JSONObject res2 = response.getJSONObject("data");
                                JSONObject posts = res2.getJSONObject("OCRdata");
                                String key, value;
                                Iterator<String> listKEY = posts.keys();
                                do {
                                    key = listKEY.next();
                                    try {
                                        value = posts.getString(key);
                                        try {
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("type", 1);
                                            jsonObject.put("key", convertString(key));
                                            jsonObject.put("key_data", value);
                                            OcrData.MapData.ScannedData scannedData = new Gson().fromJson(jsonObject.toString(), OcrData.MapData.ScannedData.class);
                                            scannedDataList.add(scannedData);
                                        } catch (JSONException e) {
                                            AccuraLog.loge(TAG, Log.getStackTraceString(e));
                                        }

                                    } catch (JSONException e) {
                                        AccuraLog.loge(TAG, Log.getStackTraceString(e));
                                    }
                                } while (listKEY.hasNext());
                            } catch (JSONException e) {
                                AccuraLog.loge(TAG, Log.getStackTraceString(e));
                            }
                            if (isFront) done1 = true;
                            else done2 = true;
                            if (done1 && done2) {
                                hidePD();
                                if (mCallback != null) {
                                    if (frontError.isEmpty() && backError.isEmpty()) {
                                        mCallback.onSuccess("1", "Success");
                                        return;
                                    }
                                    String errorMessage = frontError;
                                    if (!frontError.isEmpty() && !backError.isEmpty()) errorMessage += "\n";
                                    errorMessage += backError;
                                    mCallback.onFailed(errorMessage);
                                }
                            }
                        } else {
                            if (isFront){
                                if (!message.isEmpty()) frontError = "Front Error: " + message;
                                else frontError = "Front Error: Template Did Not Match";
                                done1 = true;
                            }
                            else {
                                if (!message.isEmpty()) backError = "Back Error: " + message;
                                else backError = "Back Error: Template Did Not Match";
                                done2 = true;
                            }
                            if (done1 && done2) {
                                hidePD();
                                if (mCallback != null) {
                                    String errorMessage = frontError;
                                    if (!frontError.isEmpty() && !backError.isEmpty()) errorMessage += "\n";
                                    errorMessage += backError;
                                    mCallback.onFailed(errorMessage);
                                }
                            }
                        }
                    }
                    String convertString( String s )
                    {
                        try {
                            int n = s.length( ) ;
                            char[] ch = s.toCharArray( ) ;
                            int c = 0 ;
                            for ( int i = 0; i < n; i++ )
                            {
                                if( i == 0 ) ch[ i ] = Character.toUpperCase( ch[ i ] ) ;
                                // as we need to replace all the '_' by spaces in between, we check for '_'
                                if ( ch[ i ] == '_' || ch[ i ] == ' ' )
                                {
                                    ch[ c++ ] = ' ' ;
                                    // converting the letter immediately after the space to upper case
                                    ch[ c ] = Character.toUpperCase( ch[ i + 1] ) ;
                                }
                                else ch[ c++ ] = ch[ i ] ;
                            }
                            return String.valueOf( ch, 0, n) ;
                        } catch (Exception e) {
                            return s;
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        error.printStackTrace();
                        if (isBothEnabled) {
                            getArabicOcrDetails(context, !isFront, countryCode, ocrData, serverUrl, timeout, i, false);
                        }
                        AccuraLog.loge(TAG, Log.getStackTraceString(error));
//                            if (isFront) done1 = i > 1;
//                            else done2 = i > 1;
                        if (i > 1) {
                            if (isFront) {
                                done1 = true;
                                frontError = "Front Error: " + error.getErrorDetail();
                            }
                            else {
                                done2 = true;
                                backError = "Back Error: " + error.getErrorDetail();
                            }
                        }
                        if (done1 && done2) {
                            hidePD();
                            if (mCallback != null) {
                                String errorMessage = frontError;
                                if (!frontError.isEmpty() && !backError.isEmpty()) errorMessage += "\n";
                                errorMessage += backError;
                                mCallback.onFailed(errorMessage);
                            }
                        } else if (i < 2) getArabicOcrDetails(context, isFront, countryCode, ocrData, serverUrl, timeout, i + 1, isBothEnabled);
                    }
                });
    }

    private void hidePD() {

        if (pd != null && pd.isShowing()) {
            try {
                pd.dismiss();
            } catch (Exception e) {
            }
            try {
                pd.cancel();
            } catch (Exception e) {
            }
            pd = null;
        }
    }
}