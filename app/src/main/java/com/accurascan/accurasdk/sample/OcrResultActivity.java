package com.accurascan.accurasdk.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.accurascan.accurasdk.sample.util.AlertDialogAbstract;
import com.accurascan.facedetection.LivenessCustomization;
import com.accurascan.facedetection.SelfieCameraActivity;
import com.accurascan.facedetection.model.AccuraVerificationResult;
import com.accurascan.facematch.util.BitmapHelper;
import com.accurascan.facematch.util.FaceHelper;
import com.accurascan.ocr.mrz.model.CardDetails;
import com.accurascan.ocr.mrz.model.OcrData;
import com.accurascan.ocr.mrz.model.PDF417Data;
import com.accurascan.ocr.mrz.model.RecogResult;
import com.bumptech.glide.Glide;
import com.docrecog.scan.RecogType;
import com.inet.facelock.callback.FaceCallback;
import com.inet.facelock.callback.FaceDetectionResult;

import org.json.JSONObject;

import java.io.File;

public class OcrResultActivity extends BaseActivity implements FaceHelper.FaceMatchCallBack, FaceCallback {

    Bitmap face1;
    private final int ACCURA_LIVENESS_CAMERA = 101;
    TableLayout mrz_table_layout, front_table_layout, back_table_layout, usdl_table_layout, pdf417_table_layout, bank_table_layout;

    ImageView ivUserProfile, ivUserProfile2, iv_frontside, iv_backside;
    LinearLayout ly_back, ly_front;
    View ly_auth_container, ly_mrz_container, ly_front_container, ly_back_container, ly_security_container,
            ly_pdf417_container, ly_usdl_container, dl_plate_lout, ly_bank_container;
    View loutImg, loutImg2;
    private FaceHelper faceHelper;
    private TextView tvFaceMatchScore, tvLivenessScore, tv_security;
    private boolean isFaceMatch = false, isLiveness = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

        initUI();

        if (RecogType.detachFrom(getIntent()) == RecogType.OCR) {
            // RecogType.OCR
            OcrData ocrData = OcrData.getOcrResult();
            if (ocrData != null) {
                setOcrData(ocrData);
            }
        } else if (RecogType.detachFrom(getIntent()) == RecogType.BANKCARD) {
            ly_back.setVisibility(View.GONE);
            loutImg.setVisibility(View.GONE);
            loutImg2.setVisibility(View.GONE);
            ivUserProfile.setVisibility(View.GONE);
            ly_auth_container.setVisibility(View.GONE);

            CardDetails cardDetails = CardDetails.getCardDetails();
            setBankData(cardDetails);

            if (cardDetails.getBitmap() != null) {
                iv_frontside.setImageBitmap(cardDetails.getBitmap());
            } else {
                ly_front.setVisibility(View.GONE);
            }

        } else if (RecogType.detachFrom(getIntent()) == RecogType.MRZ) {
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
        } else if (RecogType.detachFrom(getIntent()) == RecogType.DL_PLATE) {
            View view = findViewById(R.id.v_divider);
            dl_plate_lout.setVisibility(View.VISIBLE);
            loutImg.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
            ly_back.setVisibility(View.GONE);
            ivUserProfile.setVisibility(View.GONE);
            ly_auth_container.setVisibility(View.GONE);

            OcrData ocrData = OcrData.getOcrResult();

            if (ocrData != null) {
                TextView textView = findViewById(R.id.tv_value);
                textView.setText(ocrData.getFrontData().getOcr_data().get(0).getKey_data());

                final Bitmap frontBitmap = ocrData.getFrontimage();
                if (frontBitmap != null && !frontBitmap.isRecycled()) iv_frontside.setImageBitmap(frontBitmap);
                else ly_front.setVisibility(View.GONE);
            }

        } else if (RecogType.detachFrom(getIntent()) == RecogType.PDF417) {
            // RecogType.PDF417
            PDF417Data pdf417Data = PDF417Data.getPDF417Result();

            if (pdf417Data == null) return;
            setBarcodeData(pdf417Data);

            if (pdf417Data.docFrontBitmap != null) {
                iv_frontside.setImageBitmap(pdf417Data.docFrontBitmap);
            } else {
                ly_front.setVisibility(View.GONE);
            }

            if (pdf417Data.docBackBitmap != null) {
                iv_backside.setImageBitmap(pdf417Data.docBackBitmap);
            } else {
                ly_back.setVisibility(View.GONE);
            }

            if (pdf417Data.faceBitmap != null) {
                face1 = pdf417Data.faceBitmap;
            }
            setData();
        }
    }

    private void initUI() {
        //initialize the UI
        ivUserProfile = findViewById(R.id.ivUserProfile);
        ivUserProfile2 = findViewById(R.id.ivUserProfile2);
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
                                }
                            } else {
                                setMRZData(ocrData.getMrzData());
                            }
                        } else if (data_type == 2) {
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
            addLayout("Second Row Check No.", recogResult.secondrowchecksum);
            addLayout("Correct Second Row Check No.", recogResult.correctsecondrowchecksum);
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
            ivUserProfile.setVisibility(View.GONE);
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

                    faceHelper.setInputImage(face1);

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
            };
            new Handler().postDelayed(runnable, 100);
        }
    }

    private void displayRetryAlert(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialogAbstract(OcrResultActivity.this, msg, getString(R.string.ok), "") {
                    @Override
                    public void positive_negativeButtonClick(int pos_neg_id) {

                    }
                };
            }
        });
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
                    Toast.makeText(this, result.getStatus() + " " + result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == 102) {
                if (faceHelper!=null && face1 != null) {
                    faceHelper.setInputImage(face1);
                }
                File f = new File(Environment.getExternalStorageDirectory().toString());
                File ttt = null;
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        ttt = temp;
                        break;
                    }
                }
                if (ttt == null)
                    return;

                try {
                    faceHelper.setMatchImage(ttt.getAbsolutePath());
                    ttt.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                e.printStackTrace();
            }
        }else if (RecogType.detachFrom(getIntent()) == RecogType.MRZ && RecogResult.getRecogResult() != null) {
            try {
                RecogResult.getRecogResult().docFrontBitmap.recycle();
                RecogResult.getRecogResult().faceBitmap.recycle();
                RecogResult.getRecogResult().docBackBitmap.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (RecogType.detachFrom(getIntent()) == RecogType.PDF417 && PDF417Data.getPDF417Result() != null) {
            PDF417Data.getPDF417Result().faceBitmap.recycle();
            PDF417Data.getPDF417Result().docFrontBitmap.recycle();
            PDF417Data.getPDF417Result().docBackBitmap.recycle();
        }
        //</editor-fold>

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setResult(RESULT_OK);
//        startActivity(new Intent(this, OcrActivity.class));
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
        livenessCustomization.feedBackCenterMessage = "Center Your Face";
        livenessCustomization.feedBackMultipleFaceMessage = "Multiple Face Detected";
        livenessCustomization.feedBackHeadStraightMessage = "Keep Your Head Straight";
        livenessCustomization.feedBackBlurFaceMessage = "Blur Detected Over Face";
        livenessCustomization.feedBackGlareFaceMessage = "Glare Detected";

        Intent intent = SelfieCameraActivity.getCustomIntent(this, livenessCustomization, "your liveness url");
        startActivityForResult(intent, ACCURA_LIVENESS_CAMERA);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
        Uri uriForFile = FileProvider.getUriForFile(
                OcrResultActivity.this,
                getPackageName() + ".provider",
                f
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        } else {
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
        startActivityForResult(intent, 102);

    }

    @Override
    public void onFaceMatch(float score) {
//        NumberFormat nf = NumberFormat.getNumberInstance();
//        nf.setMaximumFractionDigits(1);
//        String ss = nf.format(score);
//        System.out.println("Match Score : " + ss + " %");
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
        Log.e("TAG", "onInitEngine: " + i);
        if (i != -1) {
            performClick(isFaceMatch, isLiveness);
        }
    }

    @Override
    public void onLeftDetect(FaceDetectionResult faceDetectionResult) {
        faceHelper.onLeftDetect(faceDetectionResult);
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
        faceHelper.onRightDetect(faceDetectionResult);
    }

    @Override
    public void onExtractInit(int i) {

    }

}
