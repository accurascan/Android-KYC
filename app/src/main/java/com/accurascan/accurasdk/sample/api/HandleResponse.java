package com.accurascan.accurasdk.sample.api;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.accurascan.accurasdk.sample.R;
import com.accurascan.accurasdk.sample.model.LivenessData;
import com.accurascan.accurasdk.sample.util.ParsedResponse;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Created by latitude on 14/4/17.
 */

public class HandleResponse {

    public static ParsedResponse responseLiveness(Activity context, String response) {

        ParsedResponse p = new ParsedResponse();
        try {
            if (!TextUtils.isEmpty(response)) {
                JSONObject objRes = new JSONObject(response);
                if (objRes.getJSONObject("meta").getBoolean("ok")) {
                    p.error = false;
                    JSONObject jsonObject = objRes.getJSONObject("data");
                    p.o = new Gson().fromJson(jsonObject.toString(), LivenessData.class);
                } else {
                    p.error = true;
                    p.o = objRes.getJSONObject("meta").getString("message");
                }
            } else {
                p.error = true;
                p.o = context.getString(R.string.err_something_wrong);
            }
        } catch (JSONException e) {
            p.error = true;
            p.o = e.getMessage();
            e.printStackTrace();
        }
        return p;
    }

}
