package com.accurascan.accurasdk.sample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.accurascan.ocr.mrz.model.ContryModel;
import com.accurascan.ocr.mrz.util.Util;
import com.docrecog.scan.RecogEngine;
import com.docrecog.scan.RecogType;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog progressBar;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (progressBar != null && progressBar.isShowing()) {
                progressBar.dismiss();
            }
            Log.e(TAG, "handleMessage: " + msg.what);
            if (msg.what == 1) {
                if (sdkModel.isMRZEnable) btnMrz.setVisibility(View.VISIBLE);
                if (sdkModel.isOCREnable && modelList != null) {
                    setCountryLayout();
                }
            }
        }
    };
    private RecyclerView rvCountry, rvCards;
    private CardListAdpter countryAdapter, cardAdapter;
    private List<Object> contryList = new ArrayList<>();
    private List<Object> cardList = new ArrayList<>();
    private List<ContryModel> modelList;
    private int selectedPosition = -1;
    private View btnMrz, btnPDF417, lout_country;
    private RecogEngine.SDKModel sdkModel;

    private void setCountryLayout() {
//        contryList = new ArrayList<>();
        contryList.clear();
        contryList.addAll(modelList);
        countryAdapter.notifyDataSetChanged();
        MainActivity.this.rvCountry.setVisibility(View.VISIBLE);
        MainActivity.this.rvCards.setVisibility(View.INVISIBLE);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Util.isPermissionsGranted(this)) {
            requestCameraPermission();
        } else {
            doWork();
        }
    }

    //requesting the camera permission
    public void requestCameraPermission() {
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
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
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage("Please wait...");
        progressBar.setCancelable(false);
        progressBar.show();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Log.d(TAG, "Worker started");
                try {
                    // doWorkNative();
                    RecogEngine recogEngine = new RecogEngine();
                    sdkModel = recogEngine.initEngine(MainActivity.this);
                    if (sdkModel.i > 0) {
                        recogEngine.setBlurPercentage(MainActivity.this, 60);
                        recogEngine.setFaceBlurPercentage(MainActivity.this, 55);
                        recogEngine.setGlarePercentage(MainActivity.this, 6,98);
                        recogEngine.isCheckPhotoCopy(MainActivity.this, false);
                        recogEngine.SetHologramDetection(MainActivity.this, true);
                        if (sdkModel.isOCREnable)
                            modelList = recogEngine.getCardList(MainActivity.this);
                        handler.sendEmptyMessage(1);
                    } else
                        handler.sendEmptyMessage(0);

                } catch (Exception e) {
                    Log.e("threadmessage", e.getMessage());
                }
            }
        },500);
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
                        intent.putExtra("country_code", ((ContryModel) MainActivity.this.contryList.get(selectedPosition)).getCountry_id());
                        intent.putExtra("card_code", cardModel.getCard_id());
                        if (cardModel.getCard_type() == 1) {
                            RecogType.PDF417.attachTo(intent);
                        }else {
                            RecogType.OCR.attachTo(intent);
                        }
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
