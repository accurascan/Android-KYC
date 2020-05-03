package com.accurascan.accurasdk.sample;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.accurascan.ocr.mrz.CameraView;
import com.accurascan.ocr.mrz.interfaces.OcrCallback;
import com.accurascan.ocr.mrz.model.OcrData;
import com.accurascan.ocr.mrz.model.PDF417Data;
import com.accurascan.ocr.mrz.model.RecogResult;
import com.docrecog.scan.RecogType;

public class OcrActivity extends AppCompatActivity implements OcrCallback {

    private CameraView cameraView;
    private View viewLeft, viewRight, borderFrame;
    private TextView tvTitle, tvScanMessage;
    private ImageView imageFlip;
    private int cardCode;
    private int countryCode;
    RecogType recogType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeNoActionBar);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide the window title.
        setContentView(R.layout.ocr_activity);
        init();

        recogType = RecogType.detachFrom(getIntent());
        cardCode = getIntent().getIntExtra("card_code", -1);
        countryCode = getIntent().getIntExtra("country_code", -1);

        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentViewTop - statusBarHeight;

        RelativeLayout linearLayout = findViewById(R.id.ocr_root); // layout width and height is match_parent
        cameraView = new CameraView(this);
        if (recogType == RecogType.OCR) {
            cameraView.setCountryCode(countryCode)
                    .setCardCode(cardCode);

        } else if (recogType == RecogType.PDF417) {
            cameraView.setCountryCode(countryCode);
        }
        cameraView.setRecogType(recogType)
                .setView(linearLayout)
                .setOcrCallback(this)
                .setTitleBarHeight(titleBarHeight)
                .init();
    }

    private void init() {
        viewLeft = findViewById(R.id.view_left_frame);
        viewRight = findViewById(R.id.view_right_frame);
        borderFrame = findViewById(R.id.border_frame);
        tvTitle = findViewById(R.id.tv_title);
        tvScanMessage = findViewById(R.id.tv_scan_msg);
        imageFlip = findViewById(R.id.im_flip_image);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (cameraView != null) cameraView.onWindowFocusUpdate(hasFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraView != null) cameraView.onResume();
    }

    @Override
    protected void onPause() {
        if (cameraView != null) cameraView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (cameraView != null) cameraView.onDestroy();
        super.onDestroy();
    }

    /**
     * to update your border frame according to width and height
     * it's different for different card
     * call {@link CameraView#startOcrScan()} method to start camera preview
     *
     * @param width
     * @param height
     */
    @Override
    public void onUpdateLayout(int width, int height) {
        if (cameraView != null) cameraView.startOcrScan();
        ViewGroup.LayoutParams layoutParams = borderFrame.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        borderFrame.setLayoutParams(layoutParams);
        ViewGroup.LayoutParams lpRight = viewRight.getLayoutParams();
        lpRight.height = height;
        viewRight.setLayoutParams(lpRight);
        ViewGroup.LayoutParams lpLeft = viewLeft.getLayoutParams();
        lpLeft.height = height;
        viewLeft.setLayoutParams(lpLeft);

        findViewById(R.id.ocr_frame).setVisibility(View.VISIBLE);
    }

    /**
     * call this method after retrieve data from card
     *
     * @param data    is scanned card data if set {@link com.docrecog.scan.RecogType#OCR} else it is null
     * @param mrzData an mrz card data if set {@link com.docrecog.scan.RecogType#MRZ} else it is null
     * @param pdf417Data an barcode PDF417 data if set {@link com.docrecog.scan.RecogType#PDF417} else it is null
     */
    @Override
    public void onScannedComplete(OcrData data, RecogResult mrzData, PDF417Data pdf417Data) {
        Intent intent = new Intent(this, OcrResultActivity.class);
        if (data != null) {
            OcrData.setOcrResult(data);
            RecogType.OCR.attachTo(intent);
            startActivityForResult(intent, 101);
//            finish();
        } else if (mrzData != null) {
            RecogResult.setRecogResult(mrzData);
            RecogType.MRZ.attachTo(intent);
            startActivityForResult(intent, 101);
//            finish();
        } else if (pdf417Data != null) {
            PDF417Data.setPDF417Result(pdf417Data);
            RecogType.PDF417.attachTo(intent);
            startActivityForResult(intent, 101);
//            finish();
        } else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
    }

    /**
     * @param title   to display scan card message(is front/ back card of the #cardName)
     *                null if title is not available.
     * @param message to display process message.
     *                null if message is not available
     * @param isFlip  to set your customize animation after complete front scan
     */
    @Override
    public void onProcessUpdate(String title, String message, boolean isFlip) {
        if (title != null) {
            tvTitle.setText(title);
        }
        if (message != null) {
            tvScanMessage.setText(message);
        }
        if (isFlip) {
            if (cameraView != null)
                cameraView.flipImage(imageFlip); //  to set default animation or remove this line to set your customize animation
        }
    }

    @Override
    public void onError(String errorMessage) {
        // stop ocr if failed
        tvScanMessage.setText(errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 101) {
                if (cameraView != null) cameraView.init();
            }
        }
    }
}