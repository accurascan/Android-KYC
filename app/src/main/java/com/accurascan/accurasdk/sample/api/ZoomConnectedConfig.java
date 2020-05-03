package com.accurascan.accurasdk.sample.api;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;

import com.facetec.zoom.sdk.ZoomCancelButtonCustomization;
import com.facetec.zoom.sdk.ZoomCustomization;
import com.facetec.zoom.sdk.ZoomFeedbackCustomization;
import com.facetec.zoom.sdk.ZoomFrameCustomization;
import com.facetec.zoom.sdk.ZoomOvalCustomization;
import com.facetec.zoom.sdk.ZoomSDK;

public class ZoomConnectedConfig {
    // Visit https://dev.zoomlogin.com/zoomsdk/#/account to retrieve your app token
   public static String AppToken = "dBcUMvlXtcsK21sCwXCsWlIT3toLa8Uw";

    //   The below URL hits our rest api, you can deploy your own version of our rest api
    //   by following our zoom rest api guides.
    //   Once deployed, you will just need to modify the URL below with the base path to the functions
    //   Note that in a real/production deployment, most likely you will want to access
    //   zoom rest api functions from internal, secure, server to server calls
    static String ZoomServerAPIUrl = "https://api.zoomauth.com/api/v1/biometrics/";

//    static String PublicKey =
//            "-----BEGIN PUBLIC KEY-----\n" +
//                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5PxZ3DLj+zP6T6HFgzzk\n" +
//                    "M77LdzP3fojBoLasw7EfzvLMnJNUlyRb5m8e5QyyJxI+wRjsALHvFgLzGwxM8ehz\n" +
//                    "DqqBZed+f4w33GgQXFZOS4AOvyPbALgCYoLehigLAbbCNTkeY5RDcmmSI/sbp+s6\n" +
//                    "mAiAKKvCdIqe17bltZ/rfEoL3gPKEfLXeN549LTj3XBp0hvG4loQ6eC1E1tRzSkf\n" +
//                    "GJD4GIVvR+j12gXAaftj3ahfYxioBH7F7HQxzmWkwDyn3bqU54eaiB7f0ftsPpWM\n" +
//                    "ceUaqkL2DZUvgN0efEJjnWy5y1/Gkq5GGWCROI9XG/SwXJ30BbVUehTbVcD70+ZF\n" +
//                    "8QIDAQAB\n" +
//                    "-----END PUBLIC KEY-----";
   public static String PublicKey = "-----BEGIN PUBLIC KEY-----\n" +
             "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5PxZ3DLj+zP6T6HFgzzk\n" +
             "M77LdzP3fojBoLasw7EfzvLMnJNUlyRb5m8e5QyyJxI+wRjsALHvFgLzGwxM8ehz\n" +
             "DqqBZed+f4w33GgQXFZOS4AOvyPbALgCYoLehigLAbbCNTkeY5RDcmmSI/sbp+s6\n" +
             "mAiAKKvCdIqe17bltZ/rfEoL3gPKEfLXeN549LTj3XBp0hvG4loQ6eC1E1tRzSkf\n" +
             "GJD4GIVvR+j12gXAaftj3ahfYxioBH7F7HQxzmWkwDyn3bqU54eaiB7f0ftsPpWM\n" +
             "ceUaqkL2DZUvgN0efEJjnWy5y1/Gkq5GGWCROI9XG/SwXJ30BbVUehTbVcD70+ZF\n" +
             "8QIDAQAB\n" +
             "-----END PUBLIC KEY-----";
//    static String PublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5PxZ3DLj+zP6T6HFgzzkM77LdzP3fojBoLasw7EfzvLMnJNUlyRb5m8e5QyyJxI+wRjsALHvFgLzGwxM8ehzDqqBZed+f4w33GgQXFZOS4AOvyPbALgCYoLehigLAbbCNTkeY5RDcmmSI/sbp+s6\n" +
//            "mAiAKKvCdIqe17bltZ/rfEoL3gPKEfLXeN549LTj3XBp0hvG4loQ6eC1E1tRzSkfGJD4GIVvR+j12gXAaftj3ahfYxioBH7F7HQxzmWkwDyn3bqU54eaiB7f0ftsPpWMceUaqkL2DZUvgN0efEJjnWy5y1/Gkq5GGWCROI9XG/SwXJ30BbVUehTbVcD70+ZF8QIDAQAB";

    static float frameSizeRatio = 0.88f;
    static int frameTopMargin = 50;
    static int frameBorderWidth = 5;
    static int frameBorderCornerRadius = 5;
    static int ovalWidth = 5;
    static int progressSpinnerWidth = 6;

    static Typeface lightFont = Typeface.create("sans-serif-light", Typeface.NORMAL);
    static Typeface regularFont = Typeface.create("sans-serif", Typeface.NORMAL);
    static Typeface mediumFont = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? Typeface.create("sans-serif-medium", Typeface.NORMAL) : Typeface.create("sans-serif", Typeface.BOLD);

    public static ZoomCustomization currentCustomization;
    public static ZoomFrameCustomization zoomFrameCustomization;
    public static ZoomFeedbackCustomization zoomFeedbackCustomization;
    public static ZoomOvalCustomization zoomOvalCustomization;
    public static ZoomCancelButtonCustomization zoomCancelButtonCustomization;

   public static boolean shouldSetIdealFrameSizeRatio = true;
   public static boolean shouldCenterZoomFrame = true;

    // Create ZoomCustomization object to modify look and feel
   public static ZoomCustomization ZoomConnectedCustomization() {
        ZoomCustomization zoomCustomization = new ZoomCustomization();

        zoomFrameCustomization = new ZoomFrameCustomization();
        zoomFeedbackCustomization = new ZoomFeedbackCustomization();
        zoomOvalCustomization = new ZoomOvalCustomization();
        zoomCancelButtonCustomization = new ZoomCancelButtonCustomization();

        zoomFrameCustomization.sizeRatio = frameSizeRatio;
        zoomFrameCustomization.topMargin = frameTopMargin;
        zoomFrameCustomization.borderWidth = frameBorderWidth;
        zoomFrameCustomization.borderCornerRadius = frameBorderCornerRadius;

        zoomOvalCustomization.strokeWidth = ovalWidth;
        zoomOvalCustomization.progressStrokeWidth = progressSpinnerWidth;

        zoomCustomization.screenSubtextFont = lightFont;
        zoomCustomization.screenButtonFont = regularFont;
        zoomCustomization.screenHeaderFont = mediumFont;
        return zoomCustomization;
    }

   public static void setIdealFrameSizeRatio(Activity activity, int appWidth) {
        if (isTablet(activity)) {
            frameSizeRatio = 0.70f;
            zoomFrameCustomization.topMargin = 100;
        } else if (isSmallScreen(activity)) {
            zoomFrameCustomization.topMargin = 0;
        }

        // here we set frameSizeRatio base the actual activity size instead of device size
        float idealFrameSizeRatio = ((float) appWidth / (float) Resources.getSystem().getDisplayMetrics().widthPixels) * frameSizeRatio;
        zoomFrameCustomization.sizeRatio = idealFrameSizeRatio;
    }

   public static void centerZoomFrame(int appWidth, int appHeight) {
        float zoomFrameWidth = zoomFrameCustomization.sizeRatio * appWidth;
        float zoomFrameHeight = zoomFrameWidth * ZoomSDK.getZoomFrameAspectRatio();

        // ZoOm frame height exceeds device height
        if (zoomFrameHeight >= appHeight) {
            frameSizeRatio *= (float) appHeight / zoomFrameHeight - 0.05;
            zoomFrameCustomization.sizeRatio = frameSizeRatio;
            zoomFrameHeight = appHeight * 0.95f;
        }

        float zoomTopMargin = (appHeight - zoomFrameHeight) / 2;
        zoomFrameCustomization.topMargin = (int) zoomTopMargin;
    }

    static boolean isSmallScreen(Activity activity) {
        return activity.getResources().getDisplayMetrics().densityDpi < 320;
    }

    public static boolean isTablet(Activity context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
