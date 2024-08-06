package com.accurascan.accurasdk.sample;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.accurascan.accurasdk.sample.adapter.BarCodeFormatListAdapter;
import com.accurascan.ocr.mrz.CameraView;
import com.accurascan.ocr.mrz.interfaces.OcrCallback;
import com.accurascan.ocr.mrz.model.BarcodeFormat;
import com.accurascan.ocr.mrz.model.CardDetails;
import com.accurascan.ocr.mrz.model.OcrData;
import com.accurascan.ocr.mrz.model.PDF417Data;
import com.accurascan.ocr.mrz.model.RecogResult;
import com.accurascan.ocr.mrz.motiondetection.SensorsActivity;
import com.accurascan.ocr.mrz.util.AccuraLog;
import com.docrecog.scan.MRZDocumentType;
import com.docrecog.scan.RecogEngine;
import com.docrecog.scan.RecogType;

import java.lang.ref.WeakReference;
import java.util.List;

public class OcrActivity extends SensorsActivity implements OcrCallback {

    private static final String TAG = OcrActivity.class.getSimpleName();
    private CameraView cameraView;
    private View viewLeft, viewRight, borderFrame;
    private TextView tvTitle, tvScanMessage;
    private View btn_barcode_selection;
    private ImageView imageFlip;
    private int cardId;
    private int countryId;
    RecogType recogType;
    Dialog types_dialog;
    private String cardName;
    private boolean isBack = false;
    private MRZDocumentType mrzType;
    private ProgressDialog pd;

    private static class MyHandler extends Handler {
        private final WeakReference<OcrActivity> mActivity;

        public MyHandler(OcrActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            OcrActivity activity = mActivity.get();
            if (activity != null) {
                String s = "";
                if (msg.obj instanceof String) s = (String) msg.obj;
                switch (msg.what) {
                    case 0: activity.tvTitle.setText(s);break;
                    case 1: activity.tvScanMessage.setText(s);break;
                    case 2: if (activity.cameraView != null) activity.cameraView.flipImage(activity.imageFlip);
                        break;
                    default: break;
                }
            }
            super.handleMessage(msg);
        }
    }

    private Handler handler = new MyHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getIntent().getIntExtra("app_orientation", 1) != 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide the window title.
        setContentView(R.layout.ocr_activity);
        AccuraLog.loge(TAG, "Start Camera Activity");
        init();

        recogType = RecogType.detachFrom(getIntent());
        if (getIntent().hasExtra(MRZDocumentType.class.getName())) {
            mrzType = MRZDocumentType.detachFrom(getIntent());
        } else {
            mrzType = MRZDocumentType.NONE;
        }
        cardId = getIntent().getIntExtra("card_id", 0);
        countryId = getIntent().getIntExtra("country_id", 0);
        cardName = getIntent().getStringExtra("card_name");

        AccuraLog.loge(TAG, "RecogType " + recogType);
        AccuraLog.loge(TAG, "Card Id " + cardId);
        AccuraLog.loge(TAG, "Country Id " + countryId);

        initCamera();
        if (recogType == RecogType.BARCODE) barcodeFormatDialog();
    }

    private void initCamera() {
        AccuraLog.loge(TAG, "Initialized camera");
        //<editor-fold desc="To get status bar height">
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarTop = rectangle.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int statusBarHeight = contentViewTop - statusBarTop;
        //</editor-fold>

        RelativeLayout linearLayout = findViewById(R.id.ocr_root); // layout width and height is match_parent

        cameraView = new CameraView(this);
        if (recogType == RecogType.OCR || recogType == RecogType.DL_PLATE) {
            // must have to set data for RecogType.OCR and RecogType.DL_PLATE
            cameraView.setCountryId(countryId).setCardId(cardId)
                    .setMinFrameForValidate(3); // to set min frame for qatar Id card
        } else if (recogType == RecogType.PDF417) {
            // must have to set data RecogType.PDF417
            cameraView.setCountryId(countryId);
        } else if (recogType == RecogType.MRZ) {
            cameraView.setMRZDocumentType(mrzType);
            // Pass 'all' for accepting MRZs of all countries
            // or you can pass respective country codes of countries whose MRZ you want to accept. Eg:- IND, USA, TUN, etc.
            cameraView.setMRZCountryCodeList("all");
        }
        cameraView.setRecogType(recogType)
                .setView(linearLayout) // To add camera view
                .setCameraFacing(0) // To set front or back camera.
                .setOcrCallback(this)  // To get Update and Success Call back
                .setStatusBarHeight(statusBarHeight)  // To remove Height from Camera View if status bar visible
                .setFrontSide()
                .enableDocLiveness(true)
                .setMaxTimeLimitInSeconds(60)
                .setServerUrl("")
//                optional field
//                .setEnableMediaPlayer(false) // false to disable sound and true to enable sound and default it is true
//                .setCustomMediaPlayer(MediaPlayer.create(this, com.accurascan.ocr.mrz.R.raw.beep)) // To add your custom sound and Must have to enable media player
                .init();  // initialized camera
    }

    private void init() {
        viewLeft = findViewById(R.id.view_left_frame);
        viewRight = findViewById(R.id.view_right_frame);
        borderFrame = findViewById(R.id.border_frame);
        tvTitle = findViewById(R.id.tv_title);
        tvScanMessage = findViewById(R.id.tv_scan_msg);
        imageFlip = findViewById(R.id.im_flip_image);
        btn_barcode_selection = findViewById(R.id.select_type);
        View btn_flip = findViewById(R.id.btn_flip);
        btn_flip.setOnClickListener(v -> {
            if (cameraView!=null) {
                cameraView.flipCamera();
            }
        });
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
    public void onDestroy() {
        AccuraLog.loge(TAG, "onDestroy");
        if (cameraView != null) cameraView.onDestroy();
        super.onDestroy();
        Runtime.getRuntime().gc(); // to clear garbage
    }

    /**
     * Override method call after camera initialized successfully
     *
     * And update your border frame according to width and height
     * it's different for different card
     *
     * Call {@link CameraView#startOcrScan(boolean isReset)} To start Camera Preview
     *
     * @param width    border layout width
     * @param height   border layout height
     */
    @Override
    public void onUpdateLayout(int width, int height) {
        AccuraLog.loge(TAG, "Frame Size (wxh) : " + width + "x" +  height);
        if (cameraView != null) cameraView.startOcrScan(false);

        //<editor-fold desc="To set camera overlay Frame">
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
        //</editor-fold>
        //<editor-fold desc="Barcode Selection only add for RecogType.BARCODE">
        if (recogType == RecogType.BARCODE) btn_barcode_selection.setVisibility(View.VISIBLE);
        else btn_barcode_selection.setVisibility(View.GONE);
        //</editor-fold>
    }

    /**
     * Override this method after scan complete to get data from document
     *
     * @param result is scanned card data
     *  result instance of {@link OcrData} if recog type is {@link com.docrecog.scan.RecogType#OCR}
     *              or {@link com.docrecog.scan.RecogType#DL_PLATE} or {@link com.docrecog.scan.RecogType#BARCODE}
     *  result instance of {@link RecogResult} if recog type is {@link com.docrecog.scan.RecogType#MRZ}
     *  result instance of {@link PDF417Data} if recog type is {@link com.docrecog.scan.RecogType#PDF417}
     *
     */
    @Override
    public void onScannedComplete(Object result) {
        Runtime.getRuntime().gc(); // To clear garbage
        AccuraLog.loge(TAG, "onScannedComplete: ");
        if (result != null) {
            if (result instanceof OcrData) {
                if (recogType == RecogType.OCR) {
                    if (isBack || !cameraView.isBackSideAvailable()) {
                        OcrData.setOcrResult((OcrData) result);
                        /**@recogType is {@link RecogType#OCR}*/
                        sendDataToResultActivity(RecogType.OCR);

                    } else {
                        isBack = true;
                        cameraView.setBackSide();
                        cameraView.flipImage(imageFlip);
                    }
                } else if (recogType == RecogType.DL_PLATE || recogType == RecogType.BARCODE) {
                    /**
                     * @recogType is {@link RecogType#DL_PLATE} or recogType == {@link RecogType#BARCODE}*/
                    OcrData.setOcrResult((OcrData) result);
                    sendDataToResultActivity(recogType);
                }
            } else if (result instanceof RecogResult) {
                /**
                 *  @recogType is {@link RecogType#MRZ}*/
                RecogResult.setRecogResult((RecogResult) result);
                sendDataToResultActivity(RecogType.MRZ);
            } else if (result instanceof CardDetails) {
                /**
                 *  @recogType is {@link RecogType#BANKCARD}*/
                CardDetails.setCardDetails((CardDetails) result);
                sendDataToResultActivity(RecogType.BANKCARD);
            } else if (result instanceof PDF417Data) {
                /**
                 *  @recogType is {@link RecogType#PDF417}*/
                if (isBack || !cameraView.isBackSideAvailable()) {
                    PDF417Data.setPDF417Result((PDF417Data) result);
                    sendDataToResultActivity(recogType);
                } else {
                    isBack = true;
                    cameraView.setBackSide();
                    cameraView.flipImage(imageFlip);
                }
            }
        } else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
    }

    private void sendDataToResultActivity(RecogType recogType) {
        if (cameraView != null) cameraView.release(true);
        Intent intent = new Intent(this, OcrResultActivity.class);
        recogType.attachTo(intent);
        intent.putExtra("app_orientation", getRequestedOrientation());
        startActivityForResult(intent, 101);
    }

    /**
     * @param titleCode to display scan card message on top of border Frame
     *
     * @param errorMessage To display process message.
     *                null if message is not available
     * @param isFlip  To set your customize animation after complete front scan
     */
    @Override
    public void onProcessUpdate(int titleCode, String errorMessage, boolean isFlip) {
        AccuraLog.loge(TAG, "onProcessUpdate :-> " + titleCode + "," + errorMessage + "," + isFlip);
        Message message;
        if (getTitleMessage(titleCode) != null) {
            /**
             *
             * 1. Scan Frontside of Card Name // for front side ocr
             * 2. Scan Backside of Card Name // for back side ocr
             * 3. Scan Card Name // only for single side ocr
             * 4. Scan Front Side of Document // for MRZ and PDF417
             * 5. Now Scan Back Side of Document // for MRZ and PDF417
             * 6. Scan Number Plate // for DL plate
             */

            message = new Message();
            message.what = 0;
            message.obj = getTitleMessage(titleCode);
            handler.sendMessage(message);
//            tvTitle.setText(title);
        }
        if (errorMessage != null) {
            message = new Message();
            message.what = 1;
            message.obj = getErrorMessage(errorMessage);
            handler.sendMessage(message);
//            tvScanMessage.setText(message);
        }
        if (isFlip) {
            message = new Message();
            message.what = 2;
            handler.sendMessage(message);//  to set default animation or remove this line to set your customize animation
        }

    }

    private String getTitleMessage(int titleCode) {
        if (titleCode < 0) return null;
        switch (titleCode){
            case RecogEngine.SCAN_TITLE_OCR_FRONT:// for front side ocr;
                return String.format("Scan Front Side of %s", cardName);
            case RecogEngine.SCAN_TITLE_OCR_BACK: // for back side ocr
                return String.format("Scan Back Side of %s", cardName);
            case RecogEngine.SCAN_TITLE_OCR: // only for single side ocr
                return String.format("Scan %s", cardName);
            case RecogEngine.SCAN_TITLE_MRZ_PDF417_FRONT:// for front side MRZ and PDF417
                if (recogType == RecogType.BANKCARD) {
                    return "Scan Bank Card";
                } else if (recogType == RecogType.BARCODE) {
                    return "Scan Barcode";
                } else
                    return "Scan Front Side of Document";
            case RecogEngine.SCAN_TITLE_MRZ_PDF417_BACK: // for back side MRZ and PDF417
                return "Now Scan Back Side of Document";
            case RecogEngine.SCAN_TITLE_DLPLATE: // for DL plate
                return "Scan Number Plate";
            default:return "";
        }
    }

    private String getErrorMessage(String s) {
        switch (s) {
            case RecogEngine.ACCURA_ERROR_CODE_MOTION:
                return "Keep Document Steady";
            case RecogEngine.ACCURA_ERROR_CODE_DOCUMENT_IN_FRAME:
                return "Keep document in frame";
            case RecogEngine.ACCURA_ERROR_CODE_BRING_DOCUMENT_IN_FRAME:
                return "Bring card near to frame.";
            case RecogEngine.ACCURA_ERROR_CODE_PROCESSING:
                return "Processing...";
            case RecogEngine.ACCURA_ERROR_CODE_BLUR_DOCUMENT:
                return "Blur detect in document";
            case RecogEngine.ACCURA_ERROR_CODE_FACE_BLUR:
                return "Blur detected over face";
            case RecogEngine.ACCURA_ERROR_CODE_GLARE_DOCUMENT:
                return "Glare detect in document";
            case RecogEngine.ACCURA_ERROR_CODE_HOLOGRAM:
                return "Hologram Detected";
            case RecogEngine.ACCURA_ERROR_CODE_DARK_DOCUMENT:
                return "Low lighting detected";
            case RecogEngine.ACCURA_ERROR_CODE_PHOTO_COPY_DOCUMENT:
                return "Can not accept Photo Copy Document";
            case RecogEngine.ACCURA_ERROR_CODE_FACE:
                return "Face not detected";
            case RecogEngine.ACCURA_ERROR_CODE_MRZ:
                return "MRZ not detected";
            case RecogEngine.ACCURA_ERROR_CODE_PASSPORT_MRZ:
                return "Passport MRZ not detected";
            case RecogEngine.ACCURA_ERROR_CODE_ID_MRZ:
                return "ID card MRZ not detected";
            case RecogEngine.ACCURA_ERROR_CODE_VISA_MRZ:
                return "Visa MRZ not detected";
            case RecogEngine.ACCURA_ERROR_CODE_WRONG_SIDE:
                return "Scanning wrong side of document";
            case RecogEngine.ACCURA_ERROR_CODE_UPSIDE_DOWN_SIDE:
                return "Document is upside down. Place it properly";
            default:
                return s;
        }
    }

    @Override
    public void onAPIUpdate(int code, String message) {
        Runnable runnable = () -> {
            if (code == RecogEngine.ACCURA_API_DISPLAY_PROGRESS) {
                // Display your custom progress dialog
                if (pd == null) {
                    pd = new ProgressDialog(OcrActivity.this);
                    pd.setCancelable(false);
                    pd.setCanceledOnTouchOutside(false);
                    pd.setMessage("Wait for API response");
                }
                try {
                    // show/hide your custom dialog according to message
                    if (message.equals("Show") && !pd.isShowing()) {
                        // To update message
                        Message _message = new Message();
                        _message.what = 1;
                        _message.obj = "Please wait...";
                        handler.sendMessage(_message);
                        pd.show();
                    } else if (message.equals("Hide") && pd.isShowing()) {
                        pd.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (code == RecogEngine.ACCURA_API_ERROR) {
                Toast.makeText(OcrActivity.this, "Api Error is " + message, Toast.LENGTH_LONG).show();
            }
        };
        runOnUiThread(runnable);
    }

    @Override
    public void onError(final String errorMessage) {
        // stop ocr if failed
        tvScanMessage.setText(errorMessage);
        Runnable runnable = () -> Toast.makeText(OcrActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        runOnUiThread(runnable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 101) {
                Runtime.getRuntime().gc(); // To clear garbage
                //<editor-fold desc="Call CameraView#startOcrScan(true) To start again Camera Preview
                //And CameraView#startOcrScan(false) To start first time">
                if (cameraView != null) {
                    isBack = false;
                    cameraView.setFrontSide();
                    AccuraLog.loge(TAG, "Rescan Document");
                    cameraView.startOcrScan(true);
                    if (types_dialog != null && types_dialog.isShowing()) types_dialog.dismiss();
                }
                //</editor-fold>
            }
        }
    }

    /**
     * Set Barcode selection Dialog to Scan only selected barcode format
     * See {@link BarcodeFormat} to get All Barcode format
     * And use Array List {@link BarcodeFormat#getList()}
     */
    int mposition = 0;
    private void barcodeFormatDialog() {
        btn_barcode_selection.setOnClickListener(v -> {
            if (cameraView != null) cameraView.stopCamera();
            types_dialog.show();
        });
        List<BarcodeFormat> CODE_NAMES = BarcodeFormat.getList();
        types_dialog = new Dialog(this);
        types_dialog.setContentView(R.layout.dialog_barcode_type);
        types_dialog.setCanceledOnTouchOutside(false);
        types_dialog.setOnKeyListener((dialog, keyCode, event) -> {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                types_dialog.cancel();
            }
            return true;
        });
        types_dialog.setOnCancelListener(dialog -> {
            if (cameraView != null) cameraView.startCamera();
        });

        View im_close = types_dialog.findViewById(R.id.btn_close);
        im_close.setOnClickListener(v -> {
            types_dialog.cancel();
        });
        ListView listView = types_dialog.findViewById(R.id.typelv);

        BarCodeFormatListAdapter adapter = new BarCodeFormatListAdapter(this, CODE_NAMES);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            for (int i = 0; i < CODE_NAMES.size(); i++) {
                CODE_NAMES.get(i).isSelected = i == position;
            }
            adapter.notifyDataSetChanged();
            mposition = position;
            // set barcode format to scan only selected barcode and by default scan all barcode
            cameraView.setBarcodeFormat(CODE_NAMES.get(mposition).formatsType);

            types_dialog.cancel();
        });

    }
}