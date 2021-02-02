package com.accurascan.accurasdk.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.accurascan.facematch.customview.CustomTextView;
import com.accurascan.facematch.customview.FaceImageview;
import com.accurascan.facematch.util.BitmapHelper;
import com.accurascan.facematch.util.Utils;
import com.facedetection.FMCameraScreenCustomization;
import com.facedetection.SelfieFMCameraActivity;
import com.facedetection.model.AccuraFMCameraModel;
import com.inet.facelock.callback.FaceCallback;
import com.inet.facelock.callback.FaceDetectionResult;
import com.inet.facelock.callback.FaceHelper;

import java.io.File;
import java.text.NumberFormat;

public class ActivityFaceMatch extends BaseActivity implements FaceCallback, FaceHelper.FaceMatchCallBack {
    int ind;

    FaceImageview image1;
    FaceImageview image2;
    CustomTextView txtScore;
    boolean bImage2 = false;
    boolean bImage1 = false;


    final private int PICK_IMAGE = 1; // request code of select image from gallery
    final private int CAPTURE_IMAGE = 2; //request code of capture image in camera
    private FaceHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.accurascan.facematch.R.layout.activity_facematch);

        findViewById(com.accurascan.facematch.R.id.ivBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        helper = new FaceHelper(this);
        if (Utils.isPermissionsGranted(this)) {
            init();
        } else {
            requestCameraPermission();
        }
    }

    private void init() {

        //handle click of gallery button of front side image that is used to select image from gallery
        findViewById(com.accurascan.facematch.R.id.btnGallery1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 1;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
            }
        });

        //handle click of camera button of front side image  that is used to capture image
        findViewById(com.accurascan.facematch.R.id.btnCamera1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 1;
                openFaceMatchCamera();
            }
        });

        //handle click of gallery button of back side image
        findViewById(com.accurascan.facematch.R.id.btnGallery2).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 2;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
            }
        });

        //handle click of camera button of back side image
        findViewById(com.accurascan.facematch.R.id.btnCamera2).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 2;
                openFaceMatchCamera();
            }
        });

        txtScore = (CustomTextView) findViewById(com.accurascan.facematch.R.id.tvScore);
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

        cameraScreenCustomization.backGroundColor = getResources().getColor(com.accurascan.facematch.R.color.fm_camera_Background);
        cameraScreenCustomization.closeIconColor = getResources().getColor(com.accurascan.facematch.R.color.fm_camera_CloseIcon);
        cameraScreenCustomization.feedbackBackGroundColor = getResources().getColor(com.accurascan.facematch.R.color.fm_camera_feedbackBg);
        cameraScreenCustomization.feedbackTextColor = getResources().getColor(com.accurascan.facematch.R.color.fm_camera_feedbackText);
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

        Intent intent = SelfieFMCameraActivity.getCustomIntent(this, cameraScreenCustomization);
        startActivityForResult(intent, CAPTURE_IMAGE);
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
            new Handler().postDelayed(runnable, 100);
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

            } else if (requestCode == CAPTURE_IMAGE) { // handle request code CAPTURE_IMAGE used for capture image in camera
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
            FrameLayout layout = (FrameLayout) findViewById(com.accurascan.facematch.R.id.ivCardLayout);
            ImageView ivCard = (ImageView) findViewById(com.accurascan.facematch.R.id.ivCard);
            image1.getLayoutParams().height = ivCard.getHeight();
            image1.requestLayout();
            layout.removeAllViews();
            layout.addView(image1);
            bImage1 = true;
        }
    }

    private void SetImageView2() {
        //create imageView for match image
        if (!bImage2) {
            FrameLayout layout2 = (FrameLayout) findViewById(com.accurascan.facematch.R.id.ivFaceLayout);
            ImageView ivFace = (ImageView) findViewById(com.accurascan.facematch.R.id.ivFace);
            image2.getLayoutParams().height = ivFace.getHeight();
            image2.requestLayout();
            layout2.removeAllViews();
            layout2.addView(image2);
            bImage2 = true;
        }
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
        image1.setImage(src1);
        SetImageView1();
    }


    @Override
    public void onSetMatchImage(Bitmap src2) {
        image2.setImage(src2);
        SetImageView2();
    }

    @Override
    public void onInitEngine(int ret) {
        Log.e("Activity+TAG", "onInitEngine: " + ret );
    }

    //call if face detect
    @Override
    public void onLeftDetect(FaceDetectionResult faceResult) {
        if (faceResult != null) {
            image1.setImage(BitmapHelper.createFromARGB(faceResult.getNewImg(), faceResult.getNewWidth(), faceResult.getNewHeight()));
            image1.setFaceDetectionResult(faceResult);
        } else {
            image1.setImage(image1.getImage());
            image1.setFaceDetectionResult(null);

        }
        helper.onLeftDetect(faceResult);
    }

    //call if face detect
    @Override
    public void onRightDetect(FaceDetectionResult faceResult) {
        if (faceResult != null) {
            image2.setImage(BitmapHelper.createFromARGB(faceResult.getNewImg(), faceResult.getNewWidth(), faceResult.getNewHeight()));
            image2.setFaceDetectionResult(faceResult);
        } else {
            image1.setImage(image1.getImage());
            image2.setFaceDetectionResult(null);
        }
        helper.onRightDetect(faceResult);
    }

    @Override
    public void onExtractInit(int ret) {
    }

}
