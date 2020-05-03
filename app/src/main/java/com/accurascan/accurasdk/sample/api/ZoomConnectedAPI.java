package com.accurascan.accurasdk.sample.api;

import android.content.Context;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ZoomConnectedAPI {
    public interface Callback {
        void completion(final boolean completed, final String message, final JSONObject data);
    }

    private ZoomConnectedNetworking zoomConnectedNetworking;
    private String baseURL = "https://api.zoomauth.com/api/v1/biometrics";

    public ZoomConnectedAPI(String appToken, String packageName, Context mainActivity) {
        this.zoomConnectedNetworking = new ZoomConnectedNetworking(appToken, packageName, mainActivity);
    }

    // convert the biometric to a base64 string, this is temporary as we will be updating our
    // REST APIs in the near future to accept non base64 payloads which will save upload
    // payload size and make the process much faster
    private String convertFacemapToBase64String(byte[] facemap) {
        return Base64.encodeToString(facemap, Base64.NO_WRAP);
    }

    public void isUserEnrolled(final String user, final Callback callback) {
        String url = baseURL + "/enrollment/" + user;
        String method = "GET";
        JSONObject parameters = new JSONObject();
        final Handler handler = new Handler();
        final long startTime = System.currentTimeMillis();

        zoomConnectedNetworking.performHTTPRequest(url, method, parameters, 3, new ZoomConnectedNetworking.Request() {
            @Override
            public void onSuccessResponse(String result) {
                long elapseTime = System.currentTimeMillis() - startTime;
                long waitTime = 1000 - elapseTime;

                if (result.contains("meta")) {
                    try {
                        final JSONObject meta = new JSONObject(result).getJSONObject("meta");
                        final boolean isEnrolled = meta.getBoolean("ok");
                        final JSONObject jsonResult = new JSONObject(result);

                        // make sure this check run for at least 1 second so the loading animation isn't jumpy
                        if (waitTime > 0) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    callback.completion(isEnrolled, "", jsonResult);
                                }
                            }, waitTime);
                        } else {
                            callback.completion(isEnrolled, "", jsonResult);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Response:" + e.toString());
                    }
                } else {
                    callback.completion(false, "We had trouble connecting to the server. Please try again", null);
                }
            }
        });
    }

    public void deleteEnrollment(final String user, final Callback callback) {
        String url = baseURL + "/enrollment/" + user;
        String method = "DELETE";
        JSONObject parameters = new JSONObject();
        final Handler handler = new Handler();
        final long startTime = System.currentTimeMillis();

        zoomConnectedNetworking.performHTTPRequest(url, method, parameters, 3, new ZoomConnectedNetworking.Request() {
            @Override
            public void onSuccessResponse(String result) {
                long elapseTime = System.currentTimeMillis() - startTime;
                long waitTime = 1000 - elapseTime;

                if (result.contains("meta")) {
                    try {
                        final JSONObject meta = new JSONObject(result).getJSONObject("meta");
                        final boolean isDeleted = meta.getBoolean("ok");
                        final JSONObject jsonResult = new JSONObject(result);

                        // make sure this check run for at least 1 second so the loading animation isn't jumpy
                        if (waitTime > 0) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    callback.completion(isDeleted, "", jsonResult);
                                }
                            }, waitTime);
                        } else {
                            callback.completion(isDeleted, "", jsonResult);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Response:" + e.toString());
                    }
                } else {
                    callback.completion(false, "We had trouble connecting to the server. Please try again", null);
                }
            }
        });
    }

    public void enrollUser(final String user, final byte[] zoomFacemap, final String sessionId, final Callback callback) {
        String url = baseURL + "/enrollment";
        String method = "POST";
        // Note: This will be updated in the future, see comment above convertFacemapToBase64String function
        String zoomFacemapStr = convertFacemapToBase64String(zoomFacemap);

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("enrollmentIdentifier", user);
            parameters.put("facemap", zoomFacemapStr);
            parameters.put("sessionId", sessionId);

        } catch (Exception e) {
            // handle exception
        }

        zoomConnectedNetworking.performHTTPRequest(url, method, parameters, 3, new ZoomConnectedNetworking.Request() {
            @Override
            public void onSuccessResponse(String result) {
                if (result.contains("meta")) {
                    try {
                        final JSONObject meta = new JSONObject(result).getJSONObject("meta");

                        if (meta.getBoolean("ok")) {
                            callback.completion(true, "Enrolled", new JSONObject(result));

                        } else {
                            callback.completion(false, "Enrollment Failed", new JSONObject(result));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Response:" + e.toString());
                    }
                } else {
                    callback.completion(false, "We had trouble connecting to the server. Please try again", null);

                }
            }
        });
    }

    public void facemap(final byte[] zoomFacemap, final String sessionId, final Callback callback) {
        String url = baseURL + "/facemap";
        String method = "POST";

        // Note: This will be updated in the future, see comment above convertFacemapToBase64String function
        String zoomFacemapStr = convertFacemapToBase64String(zoomFacemap);
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("zoomSessionData", zoomFacemapStr);
            parameters.put("sessionId", sessionId);

//            parameters.put("enrollmentIdentifier", sessionId);
//            parameters.put("facemap", zoomFacemapStr);
//            parameters.put("sessionId", sessionId);

        } catch (Exception e) {
            // handle exception
        }

        zoomConnectedNetworking.performHTTPRequest(url, method, parameters, 3, new ZoomConnectedNetworking.Request() {
            @Override
            public void onSuccessResponse(String result) {
                if (result.contains("meta")) {
                    try {
                        final JSONObject meta = new JSONObject(result).getJSONObject("meta");

                        if (meta.getBoolean("ok")) {
                            JSONObject data = new JSONObject(result).getJSONObject("data");
                            JSONArray results = data.getJSONArray("results");

                            if (results != null && results.length() > 0) {
                                if (results.getJSONObject(0).getBoolean("authenticated")) {
                                    callback.completion(true, "facemap", new JSONObject(result));
                                } else {
                                    callback.completion(false, "facemap Failed", new JSONObject(result));
                                }
                            } else {
                                callback.completion(false, "facemap Failed", new JSONObject(result));
                            }
                        } else if (!meta.getString("message").replace("HERE IS RAW RESEPONSE FROM JAVA SERVER:", "").isEmpty()) {
                            callback.completion(false, meta.getString("message").replace("HERE IS RAW RESEPONSE FROM JAVA SERVER:", ""), new JSONObject(result));
                        } else {
                            callback.completion(false, "facemap Failed", new JSONObject(result));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    callback.completion(false, "We had trouble connecting to the server. Please try again", null);

                }
            }
        });
    }

    public void authenticateUser(final String user, final byte[] zoomFacemap, final String sessionId, final Callback callback) {
        String url = baseURL + "/authenticate";
        String method = "POST";

        JSONObject parameters = new JSONObject();

        // Note: This will be updated in the future, see comment above convertFacemapToBase64String function
        String zoomFacemapStr = convertFacemapToBase64String(zoomFacemap);

        JSONObject sourceObject = new JSONObject();
        JSONArray jsonFacemapsArray = new JSONArray();
        JSONObject facemapObject = new JSONObject();

        try {
            parameters.put("performContinuousLearning", true);

            facemapObject.put("facemap", zoomFacemapStr);
            jsonFacemapsArray.put(facemapObject);
            parameters.put("targets", jsonFacemapsArray);

            sourceObject.put("enrollmentIdentifier", user);
            parameters.put("source", sourceObject);

            parameters.put("sessionId", sessionId);

        } catch (Exception e) {
            // handle exception
        }

        zoomConnectedNetworking.performHTTPRequest(url, method, parameters, 3, new ZoomConnectedNetworking.Request() {
            @Override
            public void onSuccessResponse(String result) {
                if (result.contains("meta")) {
                    try {
                        final JSONObject meta = new JSONObject(result).getJSONObject("meta");

                        if (meta.getBoolean("ok")) {
                            JSONObject data = new JSONObject(result).getJSONObject("data");
                            JSONArray results = data.getJSONArray("results");

                            if (results != null && results.length() > 0) {
                                if (results.getJSONObject(0).getBoolean("authenticated")) {
                                    callback.completion(true, "Authenticated", new JSONObject(result));
                                } else {
                                    callback.completion(false, "Authentication Failed", new JSONObject(result));
                                }
                            } else {
                                callback.completion(false, "Authentication Failed", new JSONObject(result));
                            }
                        } else if (!meta.getString("message").replace("HERE IS RAW RESEPONSE FROM JAVA SERVER:", "").isEmpty()) {
                            callback.completion(false, meta.getString("message").replace("HERE IS RAW RESEPONSE FROM JAVA SERVER:", ""), new JSONObject(result));
                        } else {
                            callback.completion(false, "Authentication Failed", new JSONObject(result));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Response:" + e.toString());
                    }
                } else {
                    callback.completion(false, "We had trouble connecting to the server. Please try again", null);

                }
            }
        });
    }

    public void checkLiveness(final byte[] zoomFacemap, final String sessionId, final Callback callback) {
        String url = baseURL + "/liveness";
        String method = "POST";

        // Note: This will be updated in the future, see comment above convertFacemapToBase64String function
        String zoomFacemapStr = convertFacemapToBase64String(zoomFacemap);

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("facemap", zoomFacemapStr);
            parameters.put("sessionId", sessionId);

        } catch (Exception e) {
            // handle exception
        }

        zoomConnectedNetworking.performHTTPRequest(url, method, parameters, 3, new ZoomConnectedNetworking.Request() {
            @Override
            public void onSuccessResponse(String result) {
                Log.e("ZoomAPI", "onSuccessResponse: "+result );
                if (result.contains("meta")) {
                    try {
                        final JSONObject meta = new JSONObject(result).getJSONObject("meta");
                        if (meta.getBoolean("ok")) {
                            JSONObject data = new JSONObject(result).getJSONObject("data");
                            String livenessResult = data.getString("livenessResult");

                            if (livenessResult.equals("passed")) {
                                callback.completion(true, "Liveness Confirmed", new JSONObject(result));
                            } else {
                                callback.completion(false, "Liveness Could Not Be Determined", new JSONObject(result));
                            }
                        } else if (!meta.getString("message").replace("HERE IS RAW RESEPONSE FROM JAVA SERVER:", "").isEmpty()) {
                            callback.completion(false, meta.getString("message").replace("HERE IS RAW RESEPONSE FROM JAVA SERVER:", ""), new JSONObject(result));
                        } else {
                            callback.completion(false, "Liveness Could Not Be Determined", new JSONObject(result));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Response:" + e.toString());
                    }
                } else {
                    try {
                        callback.completion(false, "We had trouble connecting to the server. Please try again", new JSONObject(""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    public void checkAge(final byte[] zoomFacemap, final String sessionId, final Callback callback) {
        String url = baseURL + "/age-check";
        String method = "POST";

        // Note: This will be updated in the future, see comment above convertFacemapToBase64String function
        String zoomFacemapStr = convertFacemapToBase64String(zoomFacemap);

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("facemap", zoomFacemapStr);
            parameters.put("sessionId", sessionId);
            parameters.put("targetAge", 1);

        } catch (Exception e) {
            // handle exception
        }

        zoomConnectedNetworking.performHTTPRequest(url, method, parameters, 3, new ZoomConnectedNetworking.Request() {
            @Override
            public void onSuccessResponse(String result) {
                if (result.contains("meta")) {
                    try {
                        final JSONObject meta = new JSONObject(result).getJSONObject("meta");
                        if (meta.getBoolean("ok")) {
                            JSONObject data = new JSONObject(result).getJSONObject("data");
                            int ageResult = data.getInt("ageCheckResult");

                            callback.completion(true, "Liveness Confirmed", new JSONObject(result));

                        } else if (!meta.getString("message").replace("HERE IS RAW RESEPONSE FROM JAVA SERVER:", "").isEmpty()) {
                            callback.completion(false, meta.getString("message").replace("HERE IS RAW RESEPONSE FROM JAVA SERVER:", ""), new JSONObject(result));
                        } else {
                            callback.completion(false, "Liveness Could Not Be Determined", new JSONObject(result));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Response:" + e.toString());
                    }
                } else {
                    callback.completion(false, "We had trouble connecting to the server. Please try again", null);

                }
            }
        });
    }
}
