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

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.accurascan.accurasdk.sample.api.HandleResponse;
import com.accurascan.accurasdk.sample.api.ZoomConnectedAPI;
import com.accurascan.accurasdk.sample.api.ZoomConnectedConfig;
import com.accurascan.accurasdk.sample.model.LivenessData;
import com.accurascan.accurasdk.sample.util.AlertDialogAbstract;
import com.accurascan.accurasdk.sample.util.ParsedResponse;
import com.accurascan.facematch.util.BitmapHelper;
import com.accurascan.facematch.util.FaceHelper;
import com.accurascan.ocr.mrz.model.OcrData;
import com.accurascan.ocr.mrz.model.PDF417Data;
import com.accurascan.ocr.mrz.model.RecogResult;
import com.docrecog.scan.RecogType;
import com.facetec.zoom.sdk.ZoomAuditTrailType;
import com.facetec.zoom.sdk.ZoomCustomization;
import com.facetec.zoom.sdk.ZoomSDK;
import com.facetec.zoom.sdk.ZoomSDKStatus;
import com.facetec.zoom.sdk.ZoomVerificationActivity;
import com.facetec.zoom.sdk.ZoomVerificationResult;
import com.facetec.zoom.sdk.ZoomVerificationStatus;
import com.inet.facelock.callback.FaceCallback;
import com.inet.facelock.callback.FaceDetectionResult;

import org.json.JSONObject;

import java.io.File;

public class OcrResultActivity extends BaseActivity implements FaceHelper.FaceMatchCallBack, FaceCallback {

    Bitmap face1;

    TableLayout mrz_table_layout, front_table_layout, back_table_layout, usdl_table_layout, pdf417_table_layout;

    ImageView ivUserProfile, ivUserProfile2, iv_frontside, iv_backside;
    LinearLayout ly_back, ly_front;
    View ly_mrz_container, ly_front_container, ly_back_container, ly_security_container, ly_pdf417_container, ly_usdl_container;
    View loutImg2;
    private FaceHelper faceHelper;
    private TextView tvFaceMatchScore, tvLivenessScore, tv_security;
    private ZoomConnectedAPI zoomConnectedAPI;
    private final ZoomSDK.InitializeCallback mInitializeCallback = new ZoomSDK.InitializeCallback() {
        @Override
        public void onCompletion(boolean successful) {
            if (successful) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }
    };
    private boolean isFaceMatch = false, isLiveness = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

//        faceHelper = new FaceHelper(this); // call on button click
        zoomConnectedAPI = new ZoomConnectedAPI(ZoomConnectedConfig.AppToken, getApplicationContext().getPackageName(), this);

        initUI();

        if (RecogType.detachFrom(getIntent()) == RecogType.OCR) {
            // RecogType.OCR
            OcrData ocrData = OcrData.getOcrResult();
            setOcrData(ocrData);
        } else if (RecogType.detachFrom(getIntent()) == RecogType.MRZ) {
            // RecogType.MRZ
            RecogResult g_recogResult = RecogResult.getRecogResult();
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
            setData();
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

        ly_mrz_container = findViewById(R.id.ly_mrz_container);
        ly_front_container = findViewById(R.id.ly_front_container);
        ly_back_container = findViewById(R.id.ly_back_container);
        ly_security_container = findViewById(R.id.ly_security_container);
        ly_pdf417_container = findViewById(R.id.ly_pdf417_container);
        ly_usdl_container = findViewById(R.id.ly_usdl_container);

        tvFaceMatchScore.setVisibility(View.GONE);
        tvLivenessScore.setVisibility(View.GONE);
        ly_security_container.setVisibility(View.GONE);
        ly_front_container.setVisibility(View.GONE);
        ly_back_container.setVisibility(View.GONE);
        ly_mrz_container.setVisibility(View.GONE);
        ly_pdf417_container.setVisibility(View.GONE);
        ly_usdl_container.setVisibility(View.GONE);
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

        if (frontData != null) {
            ly_front_container.setVisibility(View.VISIBLE);
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
                                    if (face1 == null) {
                                        face1 = scannedData.getImage();
                                    }
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

    private void setMRZData(RecogResult recogResult) {

        ly_mrz_container.setVisibility(View.VISIBLE);
        addLayout("MRZ", recogResult.lines);
        addLayout("Document Type", recogResult.docType);
        addLayout("First Name", recogResult.givenname);
        addLayout("Last Name", recogResult.surname);
        addLayout("Document No.", recogResult.docnumber);
        addLayout("Document check No.", recogResult.docchecksum);
        addLayout("Country", recogResult.country);
        addLayout("Nationality", recogResult.nationality);
        addLayout("Sex", recogResult.sex);
        addLayout("Date of Birth", recogResult.birth);
        addLayout("Birth Check No.", recogResult.birthchecksum);
        addLayout("Date of Expiry", recogResult.expirationdate);
        addLayout("Expiration Check No.", recogResult.expirationchecksum);
        addLayout("Date Of Issue", recogResult.issuedate);
        addLayout("Department No.", recogResult.departmentnumber);
        addLayout("Other ID", recogResult.otherid);
        addLayout("Other ID Check", recogResult.otheridchecksum);
        addLayout("Second Row Check No.", recogResult.secondrowchecksum);
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
            ivUserProfile.setImageBitmap(face1);
            ivUserProfile.setVisibility(View.VISIBLE);
        } else {
            ivUserProfile.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeZoom(); ///initialize of zooming
    }

    private void initializeZoom() {
        // Visit https://dev.zoomlogin.com/zoomsdk/#/account to retrieve your app token
        // Replace BuildConfig.ZOOM_APP_TOKEN below with your app token

        ZoomSDK.setFacemapEncryptionKey(ZoomConnectedConfig.PublicKey);
        ZoomSDK.initialize(
                this,
                ZoomConnectedConfig.AppToken,
                mInitializeCallback
        );

        // preload sdk resources so the UI is snappy (optional)
        ZoomSDK.preload(this);

        // Signal to the ZoOm SDK that audit trail should be captured
        ZoomSDK.setAuditTrailType(ZoomAuditTrailType.HEIGHT_640);

        // Signal to ZoOm to also capture time-based session images which can be used in addition to ZoOm Audit Trail per our documentation.
        ZoomSDK.setTimeBasedSessionImagesEnabled(true);

        ZoomCustomization currentCustomization = new ZoomCustomization();
        ZoomConnectedConfig.currentCustomization = ZoomConnectedConfig.ZoomConnectedCustomization();
        ZoomSDK.setCustomization(currentCustomization);
    }

    public void launchZoomScanScreen() {
        ZoomSDKStatus status = ZoomSDK.getStatus(this);
        if (status != ZoomSDKStatus.INITIALIZED) {
            Log.w("ScanResult", "Launch Error Unable to launch ZoOm.\nReason: " + status.toString());
            return;
        }

        // only set this if settings have not been changed
        if (ZoomConnectedConfig.shouldSetIdealFrameSizeRatio) {
            ZoomConnectedConfig.setIdealFrameSizeRatio(this, getWindow().getDecorView().getWidth());
        }

        if (ZoomConnectedConfig.shouldCenterZoomFrame && !ZoomConnectedConfig.isTablet(this)) {
            ZoomConnectedConfig.centerZoomFrame(getWindow().getDecorView().getWidth(), findViewById(R.id.llMain).getHeight());
        }

        // set customization
        ZoomConnectedConfig.currentCustomization.setCancelButtonCustomization(ZoomConnectedConfig.zoomCancelButtonCustomization);
        ZoomConnectedConfig.currentCustomization.setOvalCustomization(ZoomConnectedConfig.zoomOvalCustomization);
        ZoomConnectedConfig.currentCustomization.setFrameCustomization(ZoomConnectedConfig.zoomFrameCustomization);
        ZoomConnectedConfig.currentCustomization.setFrameCustomization(ZoomConnectedConfig.zoomFrameCustomization);
        ZoomConnectedConfig.currentCustomization.showPreEnrollmentScreen = false;
        ZoomSDK.setCustomization(ZoomConnectedConfig.currentCustomization);

        // Developer Note:
        // This code hides all the app content and only show the branding logo right before launching ZoOm.
        // However, developers may choose a number of strategies instead of this behavior.
        // For instance, a developer may choose to put a fullscreen semi-transparent view over the screen
        // before launching ZoOm so their full app is visible in the background but ZoOm is exposed on top of it.
        // The options are endless and full control is given to the developer for how ZoOm looks on top of their app.

        Intent authenticationIntent = new Intent(this, ZoomVerificationActivity.class);
        startActivityForResult(authenticationIntent, ZoomSDK.REQUEST_CODE_VERIFICATION);
    }

    public void handleVerificationSuccessResult(final ZoomVerificationResult successResult) {
        // retrieve the ZoOm facemap as byte[]
        if (successResult.getFaceMetrics() != null) {
            showProgressDialog();
            Runnable runnable = new Runnable() {
                public void run() {
                    faceHelper.setInputImage(face1);

                    // this is the raw biometric data which can be uploaded, or may be
                    // base64 encoded in order to handle easier at the cost of processing and network usage
//            bytes = successResult.getFaceMetrics().getZoomFacemap();
                    Bitmap face2 = null;
                    if (!successResult.getFaceMetrics().getAuditTrail().isEmpty()) {
                        face2 = successResult.getFaceMetrics().getAuditTrail().get(0).copy(Bitmap.Config.ARGB_8888, true);
                    }

                    if (face2 != null) {
                        faceHelper.setMatchImage(face2);
                    }

                    liveness(successResult);
                }
            };
            new Handler().postDelayed(runnable, 100);
        }
    }

    //checking liveness data
    private void liveness(final ZoomVerificationResult zoomVerificationResult) {
        showProgressDialog();
        byte[] zoomFacemap = zoomVerificationResult.getFaceMetrics().getZoomFacemap();
        zoomConnectedAPI.checkLiveness(zoomFacemap, zoomVerificationResult.getSessionId(), new ZoomConnectedAPI.Callback() {
            @Override
            public void completion(final boolean completed, final String message, final JSONObject data) {
                dismissProgressDialog();
                final ParsedResponse p = HandleResponse.responseLiveness(OcrResultActivity.this, data.toString());
                if (!p.error) {
                    final LivenessData livenessData = (LivenessData) p.o;
                    if (!livenessData.livenessResult.equalsIgnoreCase("undetermined")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Liveness complete successfully
//                                ageCheck(zoomVerificationResult);
                                setLivenessData(livenessData);  //setting liveness data
                            }
                        });
                    } else {
                        displayRetryAlert("Something went wrong with Liveness Check. Please try again");
                    }
                } else {
                    displayRetryAlert("Something went wrong with Liveness Check. Please try again");
                }
            }
        });
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
    //parameter to pass : Livenessdata
    private void setLivenessData(LivenessData livenessData) {
        tvLivenessScore.setText(String.format(getString(R.string.score_formate), Float.parseFloat(livenessData.livenessScore.replace(",", ""))));
        tvLivenessScore.setVisibility(View.VISIBLE);
        tvFaceMatchScore.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ZoomSDK.REQUEST_CODE_VERIFICATION) {
                ZoomVerificationResult result = data.getParcelableExtra(ZoomSDK.EXTRA_VERIFY_RESULTS);

                // CASE: you did not set a public key before attempting to retrieve a facemap.
                // Retrieving facemaps requires that you generate a public/private key pair per the instructions at https://dev.zoomlogin.com/zoomsdk/#/zoom-server-guide
                if (result.getStatus() == ZoomVerificationStatus.ENCRYPTION_KEY_INVALID) {
                    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle(getString(R.string.public_key_not_set));
                    alertDialog.setMessage(getString(R.string.key_not_set));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                } else if (result.getStatus() == ZoomVerificationStatus.USER_PROCESSED_SUCCESSFULLY) {
                    handleVerificationSuccessResult(result);
                }
            } else if (requestCode == 101) {
                faceHelper.setInputImage(face1);
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
        else if (isLiveness) launchZoomScanScreen();
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
        startActivityForResult(intent, 101);

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
                ivUserProfile2.setImageBitmap(faceDetectionResult.getFaceImage(face2));
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
