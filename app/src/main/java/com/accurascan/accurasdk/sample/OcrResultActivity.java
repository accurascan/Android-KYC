package com.accurascan.accurasdk.sample;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.accurascan.facedetection.LivenessCustomization;
import com.accurascan.facedetection.SelfieCameraActivity;
import com.accurascan.facedetection.model.AccuraVerificationResult;
import com.accurascan.facematch.util.BitmapHelper;
import com.accurascan.ocr.mrz.interfaces.PassportCallback;
import com.accurascan.ocr.mrz.model.AdditionalDocumentDetails;
import com.accurascan.ocr.mrz.model.AdditionalPersonalDetails;
import com.accurascan.ocr.mrz.model.CardDetails;
import com.accurascan.ocr.mrz.model.OcrData;
import com.accurascan.ocr.mrz.model.PDF417Data;
import com.accurascan.ocr.mrz.model.Passport;
import com.accurascan.ocr.mrz.model.RecogResult;
import com.accurascan.ocr.mrz.util.AccuraLog;
import com.bumptech.glide.Glide;
import com.docrecog.scan.AccuraNFCPassport;
import com.docrecog.scan.RecogType;
import com.facedetection.FMCameraScreenCustomization;
import com.facedetection.SelfieFMCameraActivity;
import com.facedetection.model.AccuraFMCameraModel;
import com.inet.facelock.callback.FaceCallback;
import com.inet.facelock.callback.FaceDetectionResult;
import com.inet.facelock.callback.FaceHelper;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kotlin.jvm.internal.Intrinsics;

public class OcrResultActivity extends BaseActivity implements FaceCallback {

    private static final String TAG = OcrResultActivity.class.getSimpleName();

    Bitmap face1;
    private final int ACCURA_LIVENESS_CAMERA = 101;
    private final int ACCURA_FACEMATCH_CAMERA = 102;
    TableLayout mrz_table_layout, front_table_layout, back_table_layout, usdl_table_layout, pdf417_table_layout, bank_table_layout;

    ImageView ivUserProfile, ivUserProfile2, iv_frontside, iv_backside;
    LinearLayout ly_back, ly_front;
    View ly_auth_container, ly_mrz_container, ly_front_container, ly_back_container, ly_security_container,
            ly_pdf417_container, ly_usdl_container, dl_plate_lout, ly_bank_container, ly_barcode_container;
    View loutImg, loutImg2, loutFaceImageContainer;
    private FaceHelper faceHelper;
    private TextView tvFaceMatchScore, tvLivenessScore, tv_security;
    private boolean isFaceMatch = false, isLiveness = false;
    private AlertDialog.Builder dialog;
    private RecogType recogType;

    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getIntExtra("app_orientation", 1) != 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

        initUI();
        recogType = RecogType.detachFrom(getIntent());
        if (nfcPassport == null) {
            nfcPassport = new AccuraNFCPassport(this);
            nfcPassport.setPassportCallback(nfcCallback);
        }
        if (recogType == RecogType.OCR) {
            // RecogType.OCR
            OcrData ocrData = OcrData.getOcrResult();
            if (ocrData != null) {
                setOcrData(ocrData);
            }
        } else if (recogType == RecogType.BANKCARD) {
            ly_back.setVisibility(View.GONE);
            loutFaceImageContainer.setVisibility(View.GONE);
            ly_auth_container.setVisibility(View.GONE);

            CardDetails cardDetails = CardDetails.getCardDetails();
            setBankData(cardDetails);

            if (cardDetails.getBitmap() != null) {
                iv_frontside.setImageBitmap(cardDetails.getBitmap());
            } else {
                ly_front.setVisibility(View.GONE);
            }

        } else if (recogType == RecogType.MRZ) {
            // RecogType.MRZ
            RecogResult g_recogResult = RecogResult.getRecogResult();
            if (g_recogResult != null) {
                setMRZData(g_recogResult);

                if (g_recogResult.docFrontBitmap != null) {
                    iv_frontside.setImageBitmap(g_recogResult.docFrontBitmap);
                } else {
                    ly_front.setVisibility(View.GONE);
                }

                if (g_recogResult.docBackBitmap != null) {
                    iv_backside.setImageBitmap(g_recogResult.docBackBitmap);
                } else {
                    ly_back.setVisibility(View.GONE);
                }


                if (g_recogResult.faceBitmap != null) {
                    face1 = g_recogResult.faceBitmap;
                }
            }
            setData();
        } else if (recogType == RecogType.DL_PLATE) {
            View view = findViewById(R.id.v_divider);
            dl_plate_lout.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
            ly_back.setVisibility(View.GONE);
//            ivUserProfile.setVisibility(View.GONE);
            loutFaceImageContainer.setVisibility(View.GONE);
            ly_auth_container.setVisibility(View.GONE);

            OcrData ocrData = OcrData.getOcrResult();

            if (ocrData != null) {
                TextView textView = findViewById(R.id.tv_value);
                textView.setText(ocrData.getFrontData().getOcr_data().get(0).getKey_data());

                final Bitmap frontBitmap = ocrData.getFrontimage();
                if (frontBitmap != null && !frontBitmap.isRecycled()) iv_frontside.setImageBitmap(frontBitmap);
                else ly_front.setVisibility(View.GONE);
            }

        } else if (recogType == RecogType.PDF417 || (recogType == RecogType.BARCODE && PDF417Data.getPDF417Result() != null)) {
            // RecogType.PDF417
            PDF417Data pdf417Data = PDF417Data.getPDF417Result();

            if (pdf417Data == null) return;
            setBarcodeData(pdf417Data);

            if (pdf417Data.docFrontBitmap != null) {
                iv_frontside.setImageBitmap(pdf417Data.docFrontBitmap);
            } else {
                ly_front.setVisibility(View.GONE);
            }

//            if (recogType == RecogType.BARCODE) {
//                ly_back.setVisibility(View.GONE);
//                loutFaceImageContainer.setVisibility(View.GONE);
//            ly_auth_container.setVisibility(View.GONE);
//            }

            if (pdf417Data.docBackBitmap != null) {
                iv_backside.setImageBitmap(pdf417Data.docBackBitmap);
            } else {
                ly_back.setVisibility(View.GONE);
            }

            if (pdf417Data.faceBitmap != null) {
                face1 = pdf417Data.faceBitmap;
            }
            setData();
        } else if (recogType == RecogType.BARCODE) {
            // RecogType.BARCODE
            View view = findViewById(R.id.v_divider);
            view.setVisibility(View.GONE);
            ly_barcode_container.setVisibility(View.VISIBLE);
            ly_back.setVisibility(View.GONE);
            loutFaceImageContainer.setVisibility(View.GONE);
            ly_auth_container.setVisibility(View.GONE);

            OcrData ocrData = OcrData.getOcrResult();

            if (ocrData != null) {
                TextView textView = findViewById(R.id.tv_barcode_data);
                textView.setText(ocrData.getFrontData().getOcr_data().get(0).getKey_data());

                final Bitmap frontBitmap = ocrData.getFrontimage();
                if (frontBitmap != null && !frontBitmap.isRecycled()) iv_frontside.setImageBitmap(frontBitmap);
                else ly_front.setVisibility(View.GONE);
            }
        }
    }

    private void initUI() {
        //initialize the UI
        ivUserProfile = findViewById(R.id.ivUserProfile);
        ivUserProfile2 = findViewById(R.id.ivUserProfile2);
        loutFaceImageContainer = findViewById(R.id.lyt_face_image_container);
        loutImg = findViewById(R.id.lyt_img_cover);
        loutImg2 = findViewById(R.id.lyt_img_cover2);
        tvLivenessScore = findViewById(R.id.tvLivenessScore);
        tvFaceMatchScore = findViewById(R.id.tvFaceMatchScore);
        loutImg2.setVisibility(View.GONE);

        tv_security = findViewById(R.id.tv_security);

        ly_back = findViewById(R.id.ly_back);
        ly_front = findViewById(R.id.ly_front);
        iv_frontside = findViewById(R.id.iv_frontside);
        iv_backside = findViewById(R.id.iv_backside);

        mrz_table_layout = findViewById(R.id.mrz_table_layout);
        front_table_layout = findViewById(R.id.front_table_layout);
        back_table_layout = findViewById(R.id.back_table_layout);
        pdf417_table_layout = findViewById(R.id.pdf417_table_layout);
        usdl_table_layout = findViewById(R.id.usdl_table_layout);
        bank_table_layout = findViewById(R.id.bank_table_layout);

        ly_auth_container = findViewById(R.id.layout_button_auth);
        ly_mrz_container = findViewById(R.id.ly_mrz_container);
        ly_front_container = findViewById(R.id.ly_front_container);
        ly_back_container = findViewById(R.id.ly_back_container);
        ly_security_container = findViewById(R.id.ly_security_container);
        ly_pdf417_container = findViewById(R.id.ly_pdf417_container);
        ly_usdl_container = findViewById(R.id.ly_usdl_container);
        dl_plate_lout = findViewById(R.id.dl_plate_lout);
        ly_bank_container = findViewById(R.id.ly_bank_container);
        ly_barcode_container = findViewById(R.id.barcode_lout);

        tvFaceMatchScore.setVisibility(View.GONE);
        tvLivenessScore.setVisibility(View.GONE);
        ly_security_container.setVisibility(View.GONE);
        ly_front_container.setVisibility(View.GONE);
        ly_back_container.setVisibility(View.GONE);
        ly_mrz_container.setVisibility(View.GONE);
        ly_pdf417_container.setVisibility(View.GONE);
        ly_usdl_container.setVisibility(View.GONE);
        dl_plate_lout.setVisibility(View.GONE);
        ly_bank_container.setVisibility(View.GONE);
        ly_barcode_container.setVisibility(View.GONE);
    }

    private void setOcrData(OcrData ocrData) {

        /**
         * {@link OcrData#getFrontData()}  and {@link OcrData#getBackData()} must have to check null
         * condition for card side front and back
         *
         * get FrontImage and backImage by {@link OcrData#getFrontimage()} and {@link OcrData#getBackimage()} respectively
         *
         * to get ocr data by list of {@link com.accurascan.ocr.mrz.model.OcrData.MapData.ScannedData}
         * The value {@link OcrData.MapData.ScannedData#getType()}
         * used the identify the data type. It's possible value is 1, 2 or 3
         * 1 - Text Code
         * 2 - Image Code
         * 3 - Security code
         *
         * if data type "Text" then {@link OcrData.MapData.ScannedData#getKey_data()}
         *               has String value
         * if data type "Image" then get Bitmap by {@link OcrData.MapData.ScannedData#getImage()}.
         * if data type "Security" then {@link OcrData.MapData.ScannedData#getKey_data()}
         *               has string values "true" or "false" for verified document
         *
         *
         */

        OcrData.MapData frontData = ocrData.getFrontData();
        OcrData.MapData backData = ocrData.getBackData();

        if (face1 == null && ocrData.getFaceImage() != null && !ocrData.getFaceImage().isRecycled()) {
            face1 = ocrData.getFaceImage();
        }

        if (frontData != null) {
            ly_front_container.setVisibility(View.VISIBLE);
            try {
                for (int i = 0; i < frontData.getOcr_data().size(); i++) {

                    final OcrData.MapData.ScannedData scannedData = frontData.getOcr_data().get(i);

                    if (scannedData != null) {
                        final int data_type = scannedData.getType();
                        final String key = scannedData.getKey();
                        final String value = scannedData.getKey_data();

                        final View layout = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
                        final TextView tv_key = layout.findViewById(R.id.tv_key);
                        final TextView tv_value = layout.findViewById(R.id.tv_value);
                        final ImageView imageView = layout.findViewById(R.id.iv_image);
                        if (data_type == 1) {
                            if (!key.toLowerCase().contains("mrz")) {
                                if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {
                                    tv_key.setText(key);
                                    tv_value.setText(value);
                                    imageView.setVisibility(View.GONE);
                                    front_table_layout.addView(layout);
                                }
                            } else if (key.toLowerCase().contains("mrz")) {
                                setMRZData(ocrData.getMrzData());
                            }
                        } else if (data_type == 2) {
                            if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {
                                try {
                                    if (key.toLowerCase().contains("face")) {
    //                                    if (face1 == null) {
    //                                        face1 = scannedData.getImage();
    //                                    }
                                    } else {
                                        tv_key.setText(key);
                                        Bitmap myBitmap = scannedData.getImage();
                                        if (myBitmap != null) {
                                            imageView.setImageBitmap(myBitmap);
                                            tv_value.setVisibility(View.GONE);
                                            front_table_layout.addView(layout);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                tv_value.setText(value);
                                imageView.setVisibility(View.GONE);
                                front_table_layout.addView(layout);
                            }
                        } else if (data_type == 3) {
                            updateSecurityLayout(value);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Bitmap frontBitmap = ocrData.getFrontimage();
            if (frontBitmap != null && !frontBitmap.isRecycled()) {
                iv_frontside.setImageBitmap(frontBitmap);
            }
        } else {
            ly_front.setVisibility(View.GONE);
            ly_front_container.setVisibility(View.GONE);
        }
        if (backData != null) {
            boolean isBackVisible = true;
            ly_back_container.setVisibility(View.VISIBLE);
            try {
                for (int i = 0; i < backData.getOcr_data().size(); i++) {
                    View layout = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
                    TextView tv_key = layout.findViewById(R.id.tv_key);
                    TextView tv_value = layout.findViewById(R.id.tv_value);
                    ImageView imageView = layout.findViewById(R.id.iv_image);
                    final OcrData.MapData.ScannedData scannedData = backData.getOcr_data().get(i);

                    if (scannedData != null) {
                        int data_type = scannedData.getType();
                        String key = scannedData.getKey();
                        final String value = scannedData.getKey_data();
                        if (data_type == 1) {
                            if (!key.equalsIgnoreCase("mrz")) {
                                if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {
                                    tv_key.setText(key + ":");
                                    tv_value.setText(value);
                                    imageView.setVisibility(View.GONE);
                                    back_table_layout.addView(layout);
                                    isBackVisible = true;
                                }
                            } else {
                                if (backData.getOcr_data().size()==1) isBackVisible = false;
                                setMRZData(ocrData.getMrzData());
                            }
                        } else if (data_type == 2) {
                            isBackVisible = true;
                            if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {
                                try {
                                    tv_key.setText(key + ":");
                                    Bitmap myBitmap = scannedData.getImage();
                                    if (myBitmap != null) {
                                        imageView.setImageBitmap(myBitmap);
                                        tv_value.setVisibility(View.GONE);
                                        back_table_layout.addView(layout);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                tv_value.setText(value);
                                imageView.setVisibility(View.GONE);
                                back_table_layout.addView(layout);

                            }
                        } else if (data_type == 3) {
                            updateSecurityLayout(value);
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Bitmap BackImage = ocrData.getBackimage();
            if (BackImage != null && !BackImage.isRecycled()) {
                iv_backside.setImageBitmap(BackImage);
            }
            if (!isBackVisible) {
                // hice OCR back container if Back side contains only MRZ data
                ly_back_container.setVisibility(View.GONE);
            }
        } else {
            ly_back.setVisibility(View.GONE);
            ly_back_container.setVisibility(View.GONE);
        }
        setData();
    }

    private void updateSecurityLayout(String s) {
        boolean isVerified = Boolean.parseBoolean(s);
        if (isVerified) {
            tv_security.setText("YES");
            tv_security.setTextColor(getResources().getColor(R.color.security_true));
        } else {
            tv_security.setTextColor(getResources().getColor(R.color.security_false));
            tv_security.setText("NO");
        }
        ly_security_container.setVisibility(View.VISIBLE);
    }

    private void setBankData(CardDetails bankData){
        if (bankData == null) return;
        ly_bank_container.setVisibility(View.VISIBLE);
//        addBankLayout("Owner", bankData.getOwner());
        addBankLayout("Card Type", bankData.getCardType());
        addBankLayout("Number", bankData.getNumber());
        addBankLayout("Expiry Month", bankData.getExpirationMonth());
        addBankLayout("Expiry Year", bankData.getExpirationYear());
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

    private void setMRZData(RecogResult recogResult) {

        ly_mrz_container.setVisibility(View.VISIBLE);
        try {
            addLayout("MRZ", recogResult.lines);
            addLayout("Document Type", recogResult.docType);
            addLayout("First Name", recogResult.givenname);
            addLayout("Last Name", recogResult.surname);
            addLayout("Document No.", recogResult.docnumber);
            addLayout("Document check No.", recogResult.docchecksum);
            addLayout("Correct Document check No.", recogResult.correctdocchecksum);
            addLayout("Country", recogResult.country);
            addLayout("Nationality", recogResult.nationality);
            String s = (recogResult.sex.equals("M")) ? "Male" : ((recogResult.sex.equals("F")) ? "Female" : recogResult.sex);
            addLayout("Sex", s);
            addLayout("Date of Birth", recogResult.birth);
            addLayout("Birth Check No.", recogResult.birthchecksum);
            addLayout("Correct Birth Check No.", recogResult.correctbirthchecksum);
            addLayout("Date of Expiry", recogResult.expirationdate);
            addLayout("Expiration Check No.", recogResult.expirationchecksum);
            addLayout("Correct Expiration Check No.", recogResult.correctexpirationchecksum);
            addLayout("Date Of Issue", recogResult.issuedate);
            addLayout("Department No.", recogResult.departmentnumber);
            addLayout("Other ID", recogResult.otherid);
            addLayout("Other ID Check", recogResult.otheridchecksum);
            addLayout("Other ID2", recogResult.otherid2);
            addLayout("Second Row Check No.", recogResult.secondrowchecksum);
            addLayout("Correct Second Row Check No.", recogResult.correctsecondrowchecksum);


            birthDate = convertDate(recogResult.birth,"dd-MM-yy");
            expirationDate = convertDate(recogResult.expirationdate, "dd-MM-yy");
            passportNumber = recogResult.docnumber;
            if (nfcPassport != null) {
                nfcPassport.setPassportNumber(passportNumber);
                nfcPassport.setExpirationDate(expirationDate);
                nfcPassport.setBirthDate(birthDate);
            }
            openNFCDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLayout(String key, String s) {
        if (TextUtils.isEmpty(s)) return;
        View layout1 = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
        TextView tv_key1 = layout1.findViewById(R.id.tv_key);
        TextView tv_value1 = layout1.findViewById(R.id.tv_value);
        tv_key1.setText(key);
        tv_value1.setText(s);
        mrz_table_layout.addView(layout1);
    }

    private void setBarcodeData(PDF417Data barcodeData) {
        if (barcodeData != null) {
            ly_usdl_container.setVisibility(View.VISIBLE);
        } else return;
        if (!TextUtils.isEmpty(barcodeData.wholeDataString)) {
            View layout = (View) LayoutInflater.from(this).inflate(R.layout.table_row, null);
            TextView tv_key417 = layout.findViewById(R.id.tv_key);
            TextView tv_value417 = layout.findViewById(R.id.tv_value);
            tv_key417.setText("PDF417");
            tv_value417.setGravity(View.TEXT_ALIGNMENT_TEXT_START);
            tv_value417.setText(barcodeData.wholeDataString);
            pdf417_table_layout.addView(layout);
            ly_pdf417_container.setVisibility(View.VISIBLE);
        }
        addBarcodeLayout(getString(R.string.firstName), barcodeData.fname);
        addBarcodeLayout(getString(R.string.firstName), barcodeData.firstName);
        addBarcodeLayout(getString(R.string.firstName), barcodeData.firstName1);
        addBarcodeLayout(getString(R.string.lastName), barcodeData.lname);
        addBarcodeLayout(getString(R.string.lastName), barcodeData.lastName);
        addBarcodeLayout(getString(R.string.lastName), barcodeData.lastName1);
        addBarcodeLayout(getString(R.string.middle_name), barcodeData.mname);
        addBarcodeLayout(getString(R.string.middle_name), barcodeData.middleName);
        addBarcodeLayout(getString(R.string.addressLine1), barcodeData.address1);
        addBarcodeLayout(getString(R.string.addressLine2), barcodeData.address2);
        addBarcodeLayout(getString(R.string.ResidenceStreetAddress1), barcodeData.ResidenceAddress1);
        addBarcodeLayout(getString(R.string.ResidenceStreetAddress2), barcodeData.ResidenceAddress2);
        addBarcodeLayout(getString(R.string.city), barcodeData.city);
        addBarcodeLayout(getString(R.string.zipcode), barcodeData.zipcode);
        addBarcodeLayout(getString(R.string.birth_date), barcodeData.birthday);
        addBarcodeLayout(getString(R.string.birth_date), barcodeData.birthday1);
        addBarcodeLayout(getString(R.string.license_number), barcodeData.licence_number);
        addBarcodeLayout(getString(R.string.license_expiry_date), barcodeData.licence_expire_date);
        addBarcodeLayout(getString(R.string.sex), barcodeData.sex);
        addBarcodeLayout(getString(R.string.jurisdiction_code), barcodeData.jurisdiction);
        addBarcodeLayout(getString(R.string.license_classification), barcodeData.licenseClassification);
        addBarcodeLayout(getString(R.string.license_restriction), barcodeData.licenseRestriction);
        addBarcodeLayout(getString(R.string.license_endorsement), barcodeData.licenseEndorsement);
        addBarcodeLayout(getString(R.string.issue_date), barcodeData.issueDate);
        addBarcodeLayout(getString(R.string.organ_donor), barcodeData.organDonor);
        addBarcodeLayout(getString(R.string.height_in_ft), barcodeData.heightinFT);
        addBarcodeLayout(getString(R.string.height_in_cm), barcodeData.heightCM);
        addBarcodeLayout(getString(R.string.full_name), barcodeData.fullName);
        addBarcodeLayout(getString(R.string.full_name), barcodeData.fullName1);
        addBarcodeLayout(getString(R.string.weight_in_lbs), barcodeData.weightLBS);
        addBarcodeLayout(getString(R.string.weight_in_kg), barcodeData.weightKG);
        addBarcodeLayout(getString(R.string.name_prefix), barcodeData.namePrefix);
        addBarcodeLayout(getString(R.string.name_suffix), barcodeData.nameSuffix);
        addBarcodeLayout(getString(R.string.prefix), barcodeData.Prefix);
        addBarcodeLayout(getString(R.string.suffix), barcodeData.Suffix);
        addBarcodeLayout(getString(R.string.suffix), barcodeData.Suffix1);
        addBarcodeLayout(getString(R.string.eye_color), barcodeData.eyeColor);
        addBarcodeLayout(getString(R.string.hair_color), barcodeData.hairColor);
        addBarcodeLayout(getString(R.string.issue_time), barcodeData.issueTime);
        addBarcodeLayout(getString(R.string.number_of_duplicate), barcodeData.numberDuplicate);
        addBarcodeLayout(getString(R.string.unique_customer_id), barcodeData.uniqueCustomerId);
        addBarcodeLayout(getString(R.string.social_security_number), barcodeData.socialSecurityNo);
        addBarcodeLayout(getString(R.string.social_security_number), barcodeData.socialSecurityNo1);
        addBarcodeLayout(getString(R.string.under_18), barcodeData.under18);
        addBarcodeLayout(getString(R.string.under_19), barcodeData.under19);
        addBarcodeLayout(getString(R.string.under_21), barcodeData.under21);
        addBarcodeLayout(getString(R.string.permit_classification_code), barcodeData.permitClassification);
        addBarcodeLayout(getString(R.string.veteran_indicator), barcodeData.veteranIndicator);
        addBarcodeLayout(getString(R.string.permit_issue), barcodeData.permitIssue);
        addBarcodeLayout(getString(R.string.permit_expire), barcodeData.permitExpire);
        addBarcodeLayout(getString(R.string.permit_restriction), barcodeData.permitRestriction);
        addBarcodeLayout(getString(R.string.permit_endorsement), barcodeData.permitEndorsement);
        addBarcodeLayout(getString(R.string.court_restriction), barcodeData.courtRestriction);
        addBarcodeLayout(getString(R.string.inventory_control_no), barcodeData.inventoryNo);
        addBarcodeLayout(getString(R.string.race_ethnicity), barcodeData.raceEthnicity);
        addBarcodeLayout(getString(R.string.standard_vehicle_class), barcodeData.standardVehicleClass);
        addBarcodeLayout(getString(R.string.document_discriminator), barcodeData.documentDiscriminator);
        addBarcodeLayout(getString(R.string.ResidenceCity), barcodeData.ResidenceCity);
        addBarcodeLayout(getString(R.string.ResidenceJurisdictionCode), barcodeData.ResidenceJurisdictionCode);
        addBarcodeLayout(getString(R.string.ResidencePostalCode), barcodeData.ResidencePostalCode);
        addBarcodeLayout(getString(R.string.MedicalIndicatorCodes), barcodeData.MedicalIndicatorCodes);
        addBarcodeLayout(getString(R.string.NonResidentIndicator), barcodeData.NonResidentIndicator);
        addBarcodeLayout(getString(R.string.VirginiaSpecificClass), barcodeData.VirginiaSpecificClass);
        addBarcodeLayout(getString(R.string.VirginiaSpecificRestrictions), barcodeData.VirginiaSpecificRestrictions);
        addBarcodeLayout(getString(R.string.VirginiaSpecificEndorsements), barcodeData.VirginiaSpecificEndorsements);
        addBarcodeLayout(getString(R.string.PhysicalDescriptionWeight), barcodeData.PhysicalDescriptionWeight);
        addBarcodeLayout(getString(R.string.CountryTerritoryOfIssuance), barcodeData.CountryTerritoryOfIssuance);
        addBarcodeLayout(getString(R.string.FederalCommercialVehicleCodes), barcodeData.FederalCommercialVehicleCodes);
        addBarcodeLayout(getString(R.string.PlaceOfBirth), barcodeData.PlaceOfBirth);
        addBarcodeLayout(getString(R.string.StandardEndorsementCode), barcodeData.StandardEndorsementCode);
        addBarcodeLayout(getString(R.string.StandardRestrictionCode), barcodeData.StandardRestrictionCode);
        addBarcodeLayout(getString(R.string.JuriSpeciVehiClassiDescri), barcodeData.JuriSpeciVehiClassiDescri);
        addBarcodeLayout(getString(R.string.JuriSpeciRestriCodeDescri), barcodeData.JuriSpeciRestriCodeDescri);
        addBarcodeLayout(getString(R.string.ComplianceType), barcodeData.ComplianceType);
        addBarcodeLayout(getString(R.string.CardRevisionDate), barcodeData.CardRevisionDate);
        addBarcodeLayout(getString(R.string.HazMatEndorsementExpiryDate), barcodeData.HazMatEndorsementExpiryDate);
        addBarcodeLayout(getString(R.string.LimitedDurationDocumentIndicator), barcodeData.LimitedDurationDocumentIndicator);
        addBarcodeLayout(getString(R.string.FamilyNameTruncation), barcodeData.FamilyNameTruncation);
        addBarcodeLayout(getString(R.string.FirstNamesTruncation), barcodeData.FirstNamesTruncation);
        addBarcodeLayout(getString(R.string.MiddleNamesTruncation), barcodeData.MiddleNamesTruncation);
        addBarcodeLayout(getString(R.string.organ_donor_indicator), barcodeData.OrganDonorIndicator);
        addBarcodeLayout(getString(R.string.PermitIdentifier), barcodeData.PermitIdentifier);
        addBarcodeLayout(getString(R.string.AuditInformation), barcodeData.AuditInformation);
        addBarcodeLayout(getString(R.string.JurisdictionSpecific), barcodeData.JurisdictionSpecific);

    }

    private void addBarcodeLayout(String key, String s) {
        if (TextUtils.isEmpty(s)) return;
        View layout1 = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
        TextView tv_key1 = layout1.findViewById(R.id.tv_key);
        TextView tv_value1 = layout1.findViewById(R.id.tv_value);
        tv_key1.setText(key);
        tv_value1.setText(s);
        usdl_table_layout.addView(layout1);

    }

    private void setData() {
        if (face1 != null) {
            Glide.with(this).load(face1).centerCrop().into(ivUserProfile);
            ivUserProfile.setVisibility(View.VISIBLE);
        } else {
            loutFaceImageContainer.setVisibility(View.GONE);
            ly_auth_container.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void handleVerificationSuccessResult(final AccuraVerificationResult result) {
        if (result != null) {
//            showProgressDialog();
            Runnable runnable = new Runnable() {
                public void run() {

                    if (face1 != null) {
                        faceHelper.setInputImage(face1.copy(Bitmap.Config.ARGB_8888, false));

                        if (result.getFaceBiometrics() != null) {
                            if (result.getLivenessResult() == null) {
                                return;
                            }
                            if (result.getLivenessResult().getLivenessStatus()) {
                                Bitmap face2 = result.getFaceBiometrics();
                                Glide.with(OcrResultActivity.this).load(face2).centerCrop().into(ivUserProfile2);
                                if (face2 != null) {
                                    faceHelper.setMatchImage(face2);
                                }
                                setLivenessData(result.getLivenessResult().getLivenessScore() * 100 + "");
                            }
                        }
                    }


                }
            };
            new Handler().postDelayed(runnable, 100);
        }
    }

    public void handleVerificationSuccessResult(final AccuraFMCameraModel result) {
        if (result != null) {
//            showProgressDialog();
            Runnable runnable = new Runnable() {
                public void run() {
                    setLivenessData("0.00");
                    if (faceHelper!=null && face1 != null) {
                        faceHelper.setInputImage(face1.copy(Bitmap.Config.ARGB_8888, false));
                    }

                    if (result.getFaceBiometrics() != null) {
                        Bitmap nBmp = result.getFaceBiometrics();
                        faceHelper.setMatchImage(nBmp);
                    }
                }
            };
            new Handler().postDelayed(runnable, 100);
        }
    }

    //method for setting liveness data
    //parameter to pass : livenessScore
    private void setLivenessData(String livenessScore) {
        tvLivenessScore.setText(String.format("%s %%", livenessScore.length() > 5 ? livenessScore.substring(0, 5) : livenessScore));
        tvLivenessScore.setVisibility(View.VISIBLE);
        tvFaceMatchScore.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ACCURA_LIVENESS_CAMERA && data != null) {
                AccuraVerificationResult result = data.getParcelableExtra("Accura.liveness");
                if (result == null) {
                    return;
                }
                if (result.getStatus().equals("1")) {
                    handleVerificationSuccessResult(result);
                } else {
                    Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == 102) {
                AccuraFMCameraModel result = data.getParcelableExtra("Accura.fm");
                if (result == null) {
                    return;
                }
                if (result.getStatus().equals("1")) {
                    handleVerificationSuccessResult(result);
                } else {
                    Toast.makeText(this, "Retry...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (faceHelper != null) {
            faceHelper.closeEngine();
        }
        Runtime.getRuntime().gc();
    }

    @Override
    public void onBackPressed() {

        //<editor-fold desc="To resolve memory leak">
        if ((RecogType.detachFrom(getIntent()) == RecogType.OCR || RecogType.detachFrom(getIntent()) == RecogType.DL_PLATE) && OcrData.getOcrResult() != null) {
            try {
                OcrData.getOcrResult().getFrontimage().recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                OcrData.getOcrResult().getBackimage().recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                OcrData.getOcrResult().getFaceImage().recycle();
            } catch (Exception e) {
            }
        }else if (RecogType.detachFrom(getIntent()) == RecogType.MRZ && RecogResult.getRecogResult() != null) {
            try {
                RecogResult.getRecogResult().docFrontBitmap.recycle();
                RecogResult.getRecogResult().faceBitmap.recycle();
                RecogResult.getRecogResult().docBackBitmap.recycle();
            } catch (Exception e) {
            }
        }else if (RecogType.detachFrom(getIntent()) == RecogType.PDF417 && PDF417Data.getPDF417Result() != null) {
            try {
                PDF417Data.getPDF417Result().docFrontBitmap.recycle();
                PDF417Data.getPDF417Result().faceBitmap.recycle();
                PDF417Data.getPDF417Result().docBackBitmap.recycle();
            } catch (Exception e) {
            }
        }

        OcrData.setOcrResult(null);
        RecogResult.setRecogResult(null);
        CardDetails.setCardDetails(null);
        PDF417Data.setPDF417Result(null);
        //</editor-fold>

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setResult(RESULT_OK);
        finish();
    }

    public void onCLickFaceMatch(View view) {
        if (view.getId() == R.id.btn_fm) {
            isFaceMatch = true;
            isLiveness = false;
        } else if (view.getId() == R.id.btn_liveness) {
            isFaceMatch = false;
            isLiveness = true;
        }
        if (faceHelper == null) {
            faceHelper = new FaceHelper(this);
            faceHelper.setFaceMatchCallBack(this);
            faceHelper.initEngine();
        } else {
            performClick(isFaceMatch, isLiveness);
        }

    }

    private void performClick(boolean isFaceMatch, boolean isLiveness) {
        if (isFaceMatch) openCamera();
        else if (isLiveness) openLivenessCamera();
    }

    private void openLivenessCamera() {
        LivenessCustomization livenessCustomization = new LivenessCustomization();

        livenessCustomization.backGroundColor = getResources().getColor(R.color.livenessBackground);
        livenessCustomization.closeIconColor = getResources().getColor(R.color.livenessCloseIcon);
        livenessCustomization.feedbackBackGroundColor = getResources().getColor(R.color.livenessfeedbackBg);
        livenessCustomization.feedbackTextColor = getResources().getColor(R.color.livenessfeedbackText);
        livenessCustomization.feedbackTextSize = 18;
        livenessCustomization.feedBackframeMessage = "Frame Your Face";
        livenessCustomization.feedBackAwayMessage = "Move Phone Away";
        livenessCustomization.feedBackOpenEyesMessage = "Keep Your Eyes Open";
        livenessCustomization.feedBackCloserMessage = "Move Phone Closer";
        livenessCustomization.feedBackCenterMessage = "Move Phone Center";
        livenessCustomization.feedBackMultipleFaceMessage = "Multiple Face Detected";
        livenessCustomization.feedBackHeadStraightMessage = "Keep Your Head Straight";
        livenessCustomization.feedBackBlurFaceMessage = "Blur Detected Over Face";
        livenessCustomization.feedBackGlareFaceMessage = "Glare Detected";
        livenessCustomization.feedBackLowLightMessage = "Low light detected";
        livenessCustomization.feedbackDialogMessage = "Loading...";
        livenessCustomization.feedBackProcessingMessage = "Processing...";
        livenessCustomization.showlogo = 1; // Set 0 to hide logo from selfie camera screen
        //livenessCustomization.logoIcon = R.drawable.accura_liveness_logo; // To set your custom logo
        //livenessCustomization.facing = LivenessCustomization.CAMERA_FACING_FRONT;

        livenessCustomization.setLowLightTolerence(-1/*lowLightTolerence*/);
        livenessCustomization.setBlurPercentage(80);
        livenessCustomization.setGlarePercentage(-1, -1);

        Intent intent = SelfieCameraActivity.getCustomIntent(this, livenessCustomization, "your liveness url");
        startActivityForResult(intent, ACCURA_LIVENESS_CAMERA);
    }

    private void openCamera() {

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
        cameraScreenCustomization.feedBackCenterMessage = "Move Phone Center";
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
        cameraScreenCustomization.setLowLightTolerence(-1);
        cameraScreenCustomization.setBlurPercentage(80);
        cameraScreenCustomization.setGlarePercentage(-1, -1);

        Intent intent = SelfieFMCameraActivity.getCustomIntent(this, cameraScreenCustomization);
        startActivityForResult(intent, ACCURA_FACEMATCH_CAMERA);

    }

    @Override
    public void onFaceMatch(float score) {
        tvFaceMatchScore.setText(String.format(getString(R.string.score_formate), score));
        tvLivenessScore.setVisibility(View.VISIBLE);
        tvFaceMatchScore.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSetInputImage(Bitmap bitmap) {

    }

    @Override
    public void onSetMatchImage(Bitmap bitmap) {

    }

    @Override
    public void onInitEngine(int i) {
        if (i >= 0) {
            performClick(isFaceMatch, isLiveness);
        }
    }

    @Override
    public void onLeftDetect(FaceDetectionResult faceDetectionResult) {
        //faceHelper.onLeftDetect(faceDetectionResult);
    }

    @Override
    public void onRightDetect(FaceDetectionResult faceDetectionResult) {
        if (faceDetectionResult != null) {
            try {
                Bitmap face2 = BitmapHelper.createFromARGB(faceDetectionResult.getNewImg(), faceDetectionResult.getNewWidth(), faceDetectionResult.getNewHeight());
                Glide.with(this).load(faceDetectionResult.getFaceImage(face2)).centerCrop().into(ivUserProfile2);
                loutImg2.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //faceHelper.onRightDetect(faceDetectionResult);
    }

    @Override
    public void onExtractInit(int i) {

    }

    private String passportNumber;
    private String birthDate;
    private String expirationDate;
    private AccuraNFCPassport nfcPassport;

    public void openNFCDialog() {
        if (dialog == null) {
            if (recogType == RecogType.MRZ) {
                ((TextView) findViewById(R.id.tv_bank_title)).setText("NFC Response");
            }
            dialog = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_nfc_info, null);
            dialog.setView(view);

            EditText passportNumberView;
            EditText expirationDateView;
            EditText birthDateView;
            passportNumberView = view.findViewById(R.id.input_passport_number);
            expirationDateView  = view.findViewById(R.id.input_expiration_date);
            birthDateView = view.findViewById(R.id.input_date_of_birth);

            passportNumberView.setText(passportNumber);
            expirationDateView.setText(expirationDate);
            birthDateView.setText(birthDate);
            passportNumberView.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(@NotNull CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(@NotNull CharSequence s, int start, int before, int count) {
                }

                public void afterTextChanged(@NotNull Editable s) {
                    passportNumber = s.toString();
                    if (nfcPassport != null) {
                        nfcPassport.setPassportNumber(passportNumber);
                    }
                }
            });

            expirationDateView.setOnClickListener(it -> {
                Calendar c = loadDate(expirationDateView.getText().toString());
                DatePickerDialog dialog = new DatePickerDialog(OcrResultActivity.this,
                        (datePicker, year, monthOfYear, dayOfMonth) ->
                        {
                            expirationDate = convertDate(saveDate(year, monthOfYear, dayOfMonth),"yyyy-MM-dd");
                            expirationDateView.setText(expirationDate);
                            if (nfcPassport != null) {
                                nfcPassport.setExpirationDate(expirationDate);
                            }

                        }, c.get(1), c.get(2), c.get(5));
                dialog.show();
            });
            birthDateView.setOnClickListener(it -> {
                Calendar c = loadDate(birthDateView.getText().toString());
                DatePickerDialog dialog = new DatePickerDialog(OcrResultActivity.this,
                        (datePicker, year, monthOfYear, dayOfMonth) -> {
                            birthDate = convertDate(saveDate(year, monthOfYear, dayOfMonth),"yyyy-MM-dd");
                            birthDateView.setText(birthDate);
                            if (nfcPassport != null) {
                                nfcPassport.setBirthDate(birthDate);
                            }
                        }, c.get(1), c.get(2), c.get(5));
                dialog.show();
            });
            dialog.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            });
            dialog.setCancelable(false);
        }

        if (!isFinishing()) {
            dialog.show();
        }
    }

    public Calendar loadDate(String dateString) {
        Calendar calendar = Calendar.getInstance();
        if (dateString.length() > 0) {
            try {
                Date parse = (new SimpleDateFormat("yyMMdd", Locale.US)).parse(dateString);
                if (parse != null) {
                    calendar.setTimeInMillis(parse.getTime());
                }
            } catch (ParseException var4) {
                Log.w("TAG", (Throwable)var4);
            }
        }

        return calendar;
    }
    public String saveDate(int year, int monthOfYear, int dayOfMonth) {
        Locale locale = Locale.US;
        String format = "%d-%02d-%02d";
        Object[] objects = new Object[]{year, monthOfYear + 1, dayOfMonth};
        return String.format(locale, format, Arrays.copyOf(objects, objects.length));
    }

    String nfcResponse  = "NFC response:-" + "<br/>";
    private void addNFCLayout(String key, String s) {
        if (TextUtils.isEmpty(s)) return;
        View layout1 = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
        TextView tv_key1 = layout1.findViewById(R.id.tv_key);
        TextView tv_value1 = layout1.findViewById(R.id.tv_value);
        tv_key1.setText(key);
        tv_value1.setText(s);
        bank_table_layout.addView(layout1);
        nfcResponse = nfcResponse + key + ":" + s.replaceAll("<", "&lt;") + "<br/>";
    }
    private void addTitleLayout(String key) {
        View layout1 = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
        TextView tv_key1 = layout1.findViewById(R.id.tv_key);
        View view = layout1.findViewById(R.id.lout_right);
        tv_key1.setText(key);
        view.setVisibility(View.GONE);
        bank_table_layout.addView(layout1);
        nfcResponse = nfcResponse + key + "<br/>";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcPassport == null) {
            nfcPassport = new AccuraNFCPassport(this);
            nfcPassport.setPassportCallback(nfcCallback);
        }
        int isEnabled = nfcPassport.enableNFC();
        if (isEnabled == AccuraNFCPassport.NFC_ERROR_NOT_ENABLED) {
            Toast.makeText(this, "Please activate NFC and press Back to return to the application!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcPassport != null) {
            nfcPassport.disableNFC();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            // drop NFC events
            int i = nfcPassport.handleNfcTag(intent);
        }else{
            super.onNewIntent(intent);
        }
    }

    private String convertDate(String input, String format) {
        if (input == null) {
            return null;
        } else {
            String var2;
            try {
                SimpleDateFormat yyMMdd = new SimpleDateFormat("yyMMdd", Locale.US);
                Date date = (new SimpleDateFormat(format, Locale.US)).parse(input);
                Intrinsics.checkNotNull(date);
                var2 = yyMMdd.format(date);
            } catch (ParseException var4) {
                Log.w(OcrResultActivity.class.getSimpleName(), (Throwable)var4);
                var2 = null;
            }

            return var2;
        }
    }
    private final PassportCallback nfcCallback = new PassportCallback() {
        @Override
        public void onPassportReadStart() {
            showProgressDialog();
        }

        @Override
        public void onPassportReadFinish() {
            dismissProgressDialog();
        }

        @Override
        public void onPassportRead(Passport passport) {

            if (passport == null) {
                Toast.makeText(OcrResultActivity.this, "NFC passport Read Failed", Toast.LENGTH_SHORT).show();
                return;
            }

            ly_bank_container.setVisibility(View.VISIBLE);
            addNFCLayout("firstName", passport.getFirstName());
            addNFCLayout("lastName", passport.getLastName());
            addNFCLayout("gender", passport.getGender());
            addNFCLayout("state", passport.getState());
            addNFCLayout("nationality", passport.getNationality());
            addNFCLayout("documentNumber", passport.getDocumentNumber());
            addNFCLayout("documentCode", passport.getDocumentCode());
            addNFCLayout("dateOfBirth", passport.getDateOfBirth());
            addNFCLayout("dateOfExpiry", passport.getDateOfExpiry());
            addNFCLayout("personalNumber", passport.getPersonalNumber());
            addNFCLayout("optionalData1", passport.getOptionalData1());
            addNFCLayout("optionalData2", passport.getOptionalData2());
            addNFCLayout("documentType", passport.getDocumentType() + "");
            addNFCLayout("chipAuth", passport.getChipAuth() + "");
            addNFCLayout("passiveAuth", passport.getPassiveAuth() + "");

            try {
                if (passport.getFaceImage() != null) {
//                    Glide.with(OcrResultActivity.this).load(passport.getFaceImage().copy(Bitmap.Config.ARGB_8888, false)).centerCrop().into(ivUserProfile2);
//                    loutImg2.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            AccuraLog.loge(TAG, "Passport details success");
            try {
                AdditionalPersonalDetails personalDetails = passport.getAdditionalPersonalDetails();
                AccuraLog.loge(TAG, "Passport details success : onPassportRead: personalDetails : ");
                if (personalDetails != null) {
                    addTitleLayout("Additional Personal Details:");
                    addNFCLayout("nameOfHolder", personalDetails.getNameOfHolder());
                    try {
                        addNFCLayout("otherNames", personalDetails.getOtherNames().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    addNFCLayout("personalNumber", personalDetails.getPersonalNumber());
                    addNFCLayout("fullDateOfBirth", personalDetails.getFullDateOfBirth());
                    try {
                        addNFCLayout("placeOfBirth", personalDetails.getPlaceOfBirth().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        addNFCLayout("permanentAddress", personalDetails.getPermanentAddress().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    addNFCLayout("telephone", personalDetails.getTelephone());
                    addNFCLayout("profession", personalDetails.getProfession());
                    addNFCLayout("title", personalDetails.getTitle());
                    addNFCLayout("personalSummary", personalDetails.getPersonalSummary());
                    addNFCLayout("proofOfCitizenship", personalDetails.getProofOfCitizenship() + "");
                    try {
                        addNFCLayout("otherValidTDNumbers", personalDetails.getOtherValidTDNumbers().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    addNFCLayout("custodyInformation", personalDetails.getCustodyInformation());
                    addNFCLayout("tag", personalDetails.getTag() + "");
                }
                AdditionalDocumentDetails documentDetails = passport.getAdditionalDocumentDetails();
                if (documentDetails != null) {
                    addTitleLayout("Additional Document Details:");
                    addNFCLayout("dateAndTimeOfPersonalization", documentDetails.getDateAndTimeOfPersonalization());
                    addNFCLayout("dateOfIssue", documentDetails.getDateOfIssue());
                    addNFCLayout("endorsementsAndObservations", documentDetails.getEndorsementsAndObservations());
                    addNFCLayout("issuingAuthority", documentDetails.getIssuingAuthority());
                    try {
                        addNFCLayout("namesOfOtherPersons", documentDetails.getNamesOfOtherPersons().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    addNFCLayout("personalizationSystemSerialNumber", documentDetails.getPersonalizationSystemSerialNumber());
                    addNFCLayout("taxOrExitRequirements", documentDetails.getTaxOrExitRequirements());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCardException(Exception exception) {
            exception.printStackTrace();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(OcrResultActivity.this, "Authentication has failed! Please try to scan the document again or introduce the data manually", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
}
