package com.accurascan.accurasdk.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.accurascan.accurasdk.sample.download.DownloadUtils;
import com.accurascan.facematch.customview.FaceImageview;
import com.accurascan.facematch.util.BitmapHelper;
import com.accurascan.facematch.util.Utils;
import com.accurascan.ocr.mrz.util.AccuraLog;
import com.facedetection.FMCameraScreenCustomization;
import com.facedetection.SelfieFMCameraActivity;
import com.facedetection.model.AccuraFMCameraModel;
import com.inet.facelock.callback.FaceCallback;
import com.inet.facelock.callback.FaceDetectionResult;
import com.inet.facelock.callback.FaceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.NumberFormat;

public class ActivityFaceMatch extends BaseActivity implements FaceCallback {
    int ind;

    FaceImageview image1;
    FaceImageview image2;
    TextView txtScore;
    boolean bImage2 = false;
    boolean bImage1 = false;


    final private int PICK_IMAGE = 1; // request code of select image from gallery
    final private int CAPTURE_IMAGE = 2; //request code of capture image in camera
    private static final int ACCURA_FACEMATCH_CAMERA = 3;
    private FaceHelper helper;
    private String faceParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facematch);

        findViewById(R.id.ivBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        faceParams = sharedPreferences.getString(DownloadUtils.FACE_PARAMS, "");
        String faceLicense = sharedPreferences.getString(DownloadUtils.FM_LICENSE_PATH, "");

        Log.e("TAG", "onCreate: " + faceLicense + "," + faceParams);

        helper = new FaceHelper(this);
        helper.setFaceMatchCallBack(this);
        if (!TextUtils.isEmpty(faceLicense) && new File(faceLicense).exists()) {
            helper.initEngine(faceLicense);
        } else {
            helper.initEngine();
        }
        if (Utils.isPermissionsGranted(this)) {
            init();
        } else {
            requestCameraPermission();
        }
    }

    private void init() {

        //handle click of gallery button of front side image that is used to select image from gallery
        findViewById(R.id.btnGallery1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 1;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
            }
        });

        //handle click of camera button of front side image  that is used to capture image
        findViewById(R.id.btnCamera1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 1;
                openFaceMatchCamera();
            }
        });

        //handle click of gallery button of back side image
        findViewById(R.id.btnGallery2).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 2;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
            }
        });

        //handle click of camera button of back side image
        findViewById(R.id.btnCamera2).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 2;
                openFaceMatchCamera();
            }
        });

        txtScore = (TextView) findViewById(R.id.tvScore);
        txtScore.setText("Match Score : 0 %");

        image1 = new FaceImageview(this);  //initialize the view of front image
        image2 = new FaceImageview(this);  //initialize the view of back side image
    }

    //requesting the camera permission
    public void requestCameraPermission() {
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);

                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission();
                } else {
                    init();
                }
                return;
            }
        }
    }


    private void openFaceMatchCamera() {
        FMCameraScreenCustomization cameraScreenCustomization = new FMCameraScreenCustomization();

        cameraScreenCustomization.backGroundColor = getResources().getColor(R.color.fm_camera_Background);
        cameraScreenCustomization.closeIconColor = getResources().getColor(R.color.fm_camera_CloseIcon);
        cameraScreenCustomization.feedbackBackGroundColor = getResources().getColor(R.color.fm_camera_feedbackBg);
        cameraScreenCustomization.feedbackTextColor = getResources().getColor(R.color.fm_camera_feedbackText);
        cameraScreenCustomization.feedbackTextSize = 18;
        cameraScreenCustomization.feedBackframeMessage = "Frame Your Face";
        cameraScreenCustomization.feedBackAwayMessage = "Move Phone Away";
        cameraScreenCustomization.feedBackOpenEyesMessage = "Keep Your Eyes Open";
        cameraScreenCustomization.feedBackCloserMessage = "Move Phone Closer";
        cameraScreenCustomization.feedBackCenterMessage = "Center Your Face";
        cameraScreenCustomization.feedBackMultipleFaceMessage = "Multiple Face Detected";
        cameraScreenCustomization.feedBackHeadStraightMessage = "Keep Your Head Straight";
        cameraScreenCustomization.feedBackBlurFaceMessage = "Blur Detected Over Face";
        cameraScreenCustomization.feedBackGlareFaceMessage = "Glare Detected";
        cameraScreenCustomization.feedBackLowLightMessage = "Low light detected";
        cameraScreenCustomization.feedbackDialogMessage = "Loading...";
        cameraScreenCustomization.feedBackProcessingMessage = "Processing...";
        cameraScreenCustomization.showlogo = 1; // Set 0 if hide logo
        //cameraScreenCustomization.logoIcon = R.drawable.accura_fm_logo; // To set your custom logo

        //cameraScreenCustomization.facing = FMCameraScreenCustomization.CAMERA_FACING_FRONT;
        if (faceParams != null && !faceParams.isEmpty()) {
            try {
                JSONObject object = new JSONObject(faceParams);
                cameraScreenCustomization.setLowLightTolerence(object.getInt("setLowLightTolerence"));
                cameraScreenCustomization.setBlurPercentage(object.getInt("setBlurPercentage"));
                cameraScreenCustomization.setGlarePercentage(object.getInt("setMinGlarePercentage"), object.getInt("setMaxGlarePercentage"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            cameraScreenCustomization.setLowLightTolerence(-1);
            cameraScreenCustomization.setBlurPercentage(80);
            cameraScreenCustomization.setGlarePercentage(-1, -1);
        }

        Intent intent = SelfieFMCameraActivity.getCustomIntent(this, cameraScreenCustomization);
        startActivityForResult(intent, ACCURA_FACEMATCH_CAMERA);
    }

    public void handleVerificationSuccessResult(final AccuraFMCameraModel result) {
        if (result != null) {
//            showProgressDialog();
            Runnable runnable = new Runnable() {
                public void run() {

                    if (result.getFaceBiometrics() != null) {
                        Bitmap nBmp = result.getFaceBiometrics();

                        if (ind == 1) {
                            helper.setInputImage(nBmp);
                        } else if (ind == 2) {
                            helper.setMatchImage(nBmp);
                        }
                    }
                }
            };
            new Handler().postDelayed(runnable, 50);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == PICK_IMAGE) { //handle request code PICK_IMAGE used for selecting image from gallery

                if (data == null) // data contain result of selected image from gallery and other
                    return;

                Uri uri = data.getData();
                if (ind == 1) {
                    helper.setInputImage(uri);
                } else if (ind == 2) {
                    helper.setMatchImage(uri);
                }

            } else if (requestCode == ACCURA_FACEMATCH_CAMERA) { // handle request code CAPTURE_IMAGE used for capture image in camera
                AccuraFMCameraModel result = data.getParcelableExtra("Accura.fm");
                if (result == null) {
                    return;
                }
                if (result.getStatus().equals("1")) {
                    handleVerificationSuccessResult(result);
                } else {
                    Toast.makeText(this, result.getStatus() + "Retry...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void SetImageView1() {
        //create imageView for inout image
        if (!bImage1) {
            FrameLayout layout = (FrameLayout) findViewById(R.id.ivCardLayout);
            ImageView ivCard = (ImageView) findViewById(R.id.ivCard);
            image1.getLayoutParams().height = ivCard.getHeight();
            image1.requestLayout();
            layout.removeAllViews();
            layout.addView(image1);
            bImage1 = true;
        } else image1.invalidate();
    }

    private void SetImageView2() {
        //create imageView for match image
        if (!bImage2) {
            FrameLayout layout2 = (FrameLayout) findViewById(R.id.ivFaceLayout);
            ImageView ivFace = (ImageView) findViewById(R.id.ivFace);
            image2.getLayoutParams().height = ivFace.getHeight();
            image2.requestLayout();
            layout2.removeAllViews();
            layout2.addView(image2);
            bImage2 = true;
        } else image2.invalidate();
    }

    @Override
    public void onFaceMatch(float score) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(1);
        String ss = nf.format(score);
        txtScore.setText("Match Score : " + ss + " %");
    }

    @Override
    public void onSetInputImage(Bitmap src1) {
        if (image1.getImage() != null && !image1.getImage().isRecycled()) image1.getImage().recycle();
        image1.setImage(src1);
        SetImageView1();
    }


    @Override
    public void onSetMatchImage(Bitmap src2) {
        if (image2.getImage() != null && !image2.getImage().isRecycled()) image2.getImage().recycle();
        image2.setImage(src2);
        SetImageView2();
    }

    @Override
    public void onInitEngine(int ret) {
        AccuraLog.loge("Activity TAG", "onInitEngine: " + ret );
    }

    //call if face detect
    @Override
    public void onLeftDetect(FaceDetectionResult faceResult) {
        if (faceResult != null) {
            image1.setFaceDetectionResult(faceResult);
            if (image1.getImage() != null && !image1.getImage().isRecycled()) image1.getImage().recycle();
            image1.setImage(BitmapHelper.createFromARGB(faceResult.getNewImg(), faceResult.getNewWidth(), faceResult.getNewHeight()));
            image1.requestLayout();
        } else {
            if (image1 != null && image1.getImage() != null) {
                image1.setImage(image1.getImage());
                image1.setFaceDetectionResult(null);
            }

        }
        //helper.onLeftDetect(faceResult);
    }

    //call if face detect
    @Override
    public void onRightDetect(FaceDetectionResult faceResult) {
        if (faceResult != null) {
            if (image2 != null) {
                image2.setFaceDetectionResult(faceResult);
                if (image2.getImage() != null && !image2.getImage().isRecycled()) image2.getImage().recycle();
                image2.setImage(BitmapHelper.createFromARGB(faceResult.getNewImg(), faceResult.getNewWidth(), faceResult.getNewHeight()));
                image2.requestLayout();
            }
        } else {
            if (image2 != null && image2.getImage() != null) {
                image2.setImage(image2.getImage());
                image2.setFaceDetectionResult(null);
            }
        }
        //helper.onRightDetect(faceResult);
    }

    @Override
    public void onExtractInit(int ret) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (helper != null) {
            helper.closeEngine();
        }
    }
}
