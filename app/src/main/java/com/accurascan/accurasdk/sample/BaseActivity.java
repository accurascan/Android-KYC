package com.accurascan.accurasdk.sample;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

class BaseActivity extends AppCompatActivity {

    public ProgressDialog mProgressDialog;
    private View statusBarView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        applyInsets();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        applyInsets();
    }
    private void applyInsets() {
        statusBarView = findViewById(R.id.statusBarView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
            ViewGroup content = findViewById(R.id.rootView);
            if (content == null ) return;
            ViewCompat.setOnApplyWindowInsetsListener(content, (v, windowInsets) -> {
                Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
                Insets navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
                Insets systemGestures = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
                Insets displayCutout = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout());
                int top = Math.max(statusBars.top, displayCutout.top);
                int bottom = Math.max(Math.max(navigationBars.bottom, systemGestures.bottom),
                        displayCutout.bottom);
                v.setPadding(0, top, 0, bottom);
                if (statusBarView != null) {
                    statusBarView.getLayoutParams().height = top;
                    statusBarView.requestLayout();
                    statusBarView.bringToFront();
                }
                return windowInsets;
            });
        }
    }
    private void initializeProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Loading...");
    }

    public void showProgressDialog() {
        if (mProgressDialog==null){
            initializeProgressDialog();
        }
        try {
            if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissProgressDialog() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
