package com.accurascan.accurasdk.sample;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.accurascan.ocr.mrz.model.MICRDetails;
import com.docrecog.scan.RecogType;


public class OcrResultActivity extends BaseActivity {

    TableLayout bank_table_layout;
    ImageView iv_frontside, iv_backside;
    LinearLayout ly_back, ly_front;
    View  ly_bank_container;

    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getIntExtra("app_orientation", 1) != 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

        initUI();
        RecogType recogType = RecogType.detachFrom(getIntent());

        if (recogType == RecogType.MICR) {
            ly_back.setVisibility(View.GONE);

            TextView view = findViewById(R.id.tv_title_bank);
            view.setText("MICR Details");

            MICRDetails micrDetails = MICRDetails.getMicrDetails();
            setMICRData(micrDetails);

            if (micrDetails.bitmap != null) {
                iv_frontside.setImageBitmap(micrDetails.bitmap);
            } else {
                ly_front.setVisibility(View.GONE);
            }
        }
    }

    private void initUI() {
        //initialize the UI

        ly_back = findViewById(R.id.ly_back);
        ly_front = findViewById(R.id.ly_front);
        iv_frontside = findViewById(R.id.iv_frontside);
        iv_backside = findViewById(R.id.iv_backside);

        bank_table_layout = findViewById(R.id.bank_table_layout);
        ly_bank_container = findViewById(R.id.ly_bank_container);
        ly_bank_container.setVisibility(View.GONE);
    }

    private void setMICRData(MICRDetails micrDetails) {
        if (micrDetails == null) return;
        ly_bank_container.setVisibility(View.VISIBLE);
        addBankLayout("MICR Line", micrDetails.MICRLine);
        addBankLayout("Routing Number", micrDetails.routingNumber);
    }

    private void addBankLayout(String key, String s) {
        if (TextUtils.isEmpty(s)) return;
        View layout1 = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
        TextView tv_key1 = layout1.findViewById(R.id.tv_key);
        TextView tv_value1 = layout1.findViewById(R.id.tv_value);
        tv_key1.setText(key);
        tv_value1.setText(s);
        bank_table_layout.addView(layout1);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }

    @Override
    public void onBackPressed() {

        //<editor-fold desc="To resolve memory leak">
        if (RecogType.detachFrom(getIntent()) == RecogType.MICR && MICRDetails.getMicrDetails() != null) {
            try {
                MICRDetails.getMicrDetails().bitmap.recycle();
            } catch (Exception e) {
            }
            MICRDetails.setMicrDetails(null);
        }
        //</editor-fold>

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setResult(RESULT_OK);
        finish();
    }
}
