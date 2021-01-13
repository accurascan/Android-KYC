package com.accurascan.accurasdk.sample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.accurascan.facedetection.utils.AccuraLivenessLog;
import com.accurascan.ocr.mrz.model.ContryModel;
import com.accurascan.ocr.mrz.util.AccuraLog;
import com.accurascan.ocr.mrz.util.Util;
import com.docrecog.scan.MRZDocumentType;
import com.docrecog.scan.RecogEngine;
import com.docrecog.scan.RecogType;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog progressBar;
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                if (activity.progressBar != null && activity.progressBar.isShowing()) {
                    activity.progressBar.dismiss();
                }
                Log.e(TAG, "handleMessage: " + msg.what);
                if (msg.what == 1) {
                    if (activity.sdkModel.isMRZEnable) {
                        activity.btnIdMrz.setVisibility(View.VISIBLE);
                        activity.btnVisaMrz.setVisibility(View.VISIBLE);
                        activity.btnPassportMrz.setVisibility(View.VISIBLE);
                        activity.btnMrz.setVisibility(View.VISIBLE);
                    }
                    if (activity.sdkModel.isBankCardEnable)
                        activity.btnBank.setVisibility(View.VISIBLE);
                    if (activity.sdkModel.isOCREnable && activity.modelList != null) {
                        activity.setCountryLayout();
                    }
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                    builder1.setMessage(activity.responseMessage);
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(
                            "OK",
                            (dialog, id) -> dialog.cancel());
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        }
    }

    private static class NativeThread extends Thread {
        private final WeakReference<MainActivity> mActivity;

        public NativeThread(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void run() {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                try {
                    // doWorkNative();
                    RecogEngine recogEngine = new RecogEngine();
                    activity.sdkModel = recogEngine.initEngine(activity);
                    AccuraLog.enableLogs(true);
                    AccuraLivenessLog.setDEBUG(true);
                    AccuraLog.loge(TAG, "Initialized Engine : " + activity.sdkModel.i + " -> " + activity.sdkModel.message);
                    activity.responseMessage = activity.sdkModel.message;

                    if (activity.sdkModel.i >= 0) {

                        // if OCR enable then get card list
                        if (activity.sdkModel.isOCREnable)
                            activity.modelList = recogEngine.getCardList(activity);
                        AccuraLog.loge(TAG, "run: " + new Gson().toJson(activity.modelList) );

                        recogEngine.setBlurPercentage(activity, 62);
                        recogEngine.setFaceBlurPercentage(activity, 70);
                        recogEngine.setGlarePercentage(activity, 6, 98);
                        recogEngine.isCheckPhotoCopy(activity, false);
                        recogEngine.SetHologramDetection(activity, true);
                        recogEngine.setLowLightTolerance(activity, 39);
                        recogEngine.setMotionData(activity, 18);

                        activity.handler.sendEmptyMessage(1);
                    } else
                        activity.handler.sendEmptyMessage(0);

                } catch (Exception e) {
                }
            }
            super.run();
        }
    }

    private Thread nativeThread = new NativeThread(this);
    private RecyclerView rvCountry, rvCards;
    private CardListAdpter countryAdapter, cardAdapter;
    private List<Object> contryList = new ArrayList<>();
    private List<Object> cardList = new ArrayList<>();
    private List<ContryModel> modelList;
    private int selectedPosition = -1;
    private View btnMrz, btnPassportMrz, btnIdMrz, btnVisaMrz, btnBank, lout_country;
    private RecogEngine.SDKModel sdkModel;
    private String responseMessage;
    private Handler handler = new MyHandler(this);

    private void setCountryLayout() {
//        contryList = new ArrayList<>();
        contryList.clear();
        contryList.addAll(modelList);
        countryAdapter.notifyDataSetChanged();
        MainActivity.this.rvCountry.setVisibility(View.VISIBLE);
        MainActivity.this.rvCards.setVisibility(View.INVISIBLE);
    }

    // must have to required storage permission to print logs
    public void printLog() {
        File file = new File(Environment.getExternalStorageDirectory(), "AccuraKYCDemo.log");
        String command = "logcat -f "+ file.getPath() + " -v time *:V";
        Log.d(TAG, "command: " + command);

        try{
            Runtime.getRuntime().exec(command);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(file));
        sendBroadcast(scanIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMrz = findViewById(R.id.lout_mrz);
        btnMrz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OcrActivity.class);
                RecogType.MRZ.attachTo(intent);
                MRZDocumentType.NONE.attachTo(intent);
                intent.putExtra("card_name", getResources().getString(R.string.other_mrz));
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        btnPassportMrz = findViewById(R.id.lout_passport_mrz);
        btnIdMrz = findViewById(R.id.lout_id_mrz);
        btnVisaMrz = findViewById(R.id.lout_visa_mrz);
        btnPassportMrz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OcrActivity.class);
                RecogType.MRZ.attachTo(intent);
                MRZDocumentType.PASSPORT_MRZ.attachTo(intent);
                intent.putExtra("card_name", getResources().getString(R.string.passport_mrz));
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        btnIdMrz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OcrActivity.class);
                RecogType.MRZ.attachTo(intent);
                MRZDocumentType.ID_CARD_MRZ.attachTo(intent);
                intent.putExtra("card_name", getResources().getString(R.string.id_mrz));
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        btnVisaMrz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OcrActivity.class);
                RecogType.MRZ.attachTo(intent);
                MRZDocumentType.VISA_MRZ.attachTo(intent);
                intent.putExtra("card_name", getResources().getString(R.string.visa_mrz));
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        btnBank = findViewById(R.id.lout_bank);
        btnBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OcrActivity.class);
                RecogType.BANKCARD.attachTo(intent);
                intent.putExtra("card_name", getResources().getString(R.string.bank_card));
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        lout_country = findViewById(R.id.lout_country);
        rvCountry = findViewById(R.id.rv_country);
        rvCountry.setLayoutManager(new LinearLayoutManager(this));
        countryAdapter = new CardListAdpter(this, contryList);
        rvCountry.setAdapter(countryAdapter);

        rvCards = findViewById(R.id.rv_card);
        rvCards.setLayoutManager(new LinearLayoutManager(this));
        cardAdapter = new CardListAdpter(this, cardList);
        rvCards.setAdapter(cardAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isPermissionsGranted(this)) {
            requestCameraPermission();
        } else {
            doWork();
        }
    }

    public static boolean isPermissionsGranted(Context context) {
        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
        }
        return true;
    }

    //requesting the camera permission
    public void requestCameraPermission() {
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        doWork();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(this, "You declined to allow the app to access your camera", Toast.LENGTH_LONG).show();
                }
        }
    }

    public void doWork() {
        printLog();  // Create Log file
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage("Please wait...");
        progressBar.setCancelable(false);
        progressBar.show();
        nativeThread.start();
    }

    public class CardListAdpter extends RecyclerView.Adapter {

        private final Context context;
        private final List<Object> modelList;

        public CardListAdpter(Context context, List<Object> modelList) {
            this.context = context;
            this.modelList = modelList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false));

        }

        @Override
        public int getItemViewType(int position) {
            if (this.modelList.get(position) instanceof ContryModel) {
                return 0;
            } else
                return 1;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
            Holder holder = (Holder) viewHolder;
            if (this.modelList.get(position) instanceof ContryModel) {
                final ContryModel contryModel = (ContryModel) this.modelList.get(position);
                holder.txt_card_name.setText(contryModel.getCountry_name());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedPosition = position;
                        updateCardLayout(contryModel);
                    }
                });
            } else if (this.modelList.get(position) instanceof ContryModel.CardModel) {
                final ContryModel.CardModel cardModel = (ContryModel.CardModel) this.modelList.get(position);
                holder.txt_card_name.setText(cardModel.getCard_name());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(CardListAdpter.this.context, OcrActivity.class);
                        intent.putExtra("country_id", ((ContryModel) MainActivity.this.contryList.get(selectedPosition)).getCountry_id());
                        intent.putExtra("card_id", cardModel.getCard_id());
                        intent.putExtra("card_name", cardModel.getCard_name());

                        if (cardModel.getCard_type() == 1) RecogType.PDF417.attachTo(intent);
                        else if (cardModel.getCard_type() == 2) RecogType.DL_PLATE.attachTo(intent);
                        else RecogType.OCR.attachTo(intent);

                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return this.modelList.size();
        }

        public class Holder extends RecyclerView.ViewHolder {
            TextView txt_card_name;

            public Holder(@NonNull View itemView) {
                super(itemView);
                txt_card_name = itemView.findViewById(R.id.tv_title);
            }
        }
    }

    private void updateCardLayout(ContryModel model) {
        MainActivity.this.cardList.clear();
        MainActivity.this.cardList.addAll(model.getCards());
        MainActivity.this.cardAdapter.notifyDataSetChanged();
        MainActivity.this.lout_country.setVisibility(View.INVISIBLE);
        MainActivity.this.rvCards.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (MainActivity.this.rvCards.getVisibility() == View.INVISIBLE) {
            super.onBackPressed();
        } else {
            selectedPosition = -1;
            MainActivity.this.lout_country.setVisibility(View.VISIBLE);
            MainActivity.this.rvCards.setVisibility(View.INVISIBLE);
        }
    }
}
