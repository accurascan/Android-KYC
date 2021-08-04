# Accura KYC Android SDK - OCR, Face Match & Liveness Check
Android KYC SDK - OCR &amp; Face Match <br/><br/>
Accura OCR is used for Optical character recognition.<br/><br/>
Accura Face Match is used for Matching 2 Faces. Source and Target. It matches the User Image from a Selfie vs User Image in document.<br/><br/>
Accura Authentication is used for your customer verification and authentication.Unlock the True Identity of Your Users with 3D Selfie Technology<br/><br/>


Below steps to setup Accura SDK's to your project.

## Install SDK in to your App

#### Step 1: Add the JitPack repository to your build file:
    Add it in your root build.gradle at the end of repositories.

    allprojects {
        repositories {
            ...
            maven {
                url 'https://jitpack.io'
                credentials { username authToken }
            }
        }
    }

#### Step 2. Add the token to `gradle.properties`:

    authToken=jp_ssguccab6c5ge2l4jitaj92ek2

#### Step 3: Add the dependency:
    Set Accura SDK as a dependency to our app/build.gradle file.

    android {
    
        defaultConfig {
            ...
            ndk {
                // Specify CPU architecture.
                // 'armeabi-v7a' & 'arm64-v8a' are respectively 32 bit and 64 bit device architecture 
                // 'x86' & 'x86_64' are respectively 32 bit and 64 bit emulator architecture
                abiFilters 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
            }
        }
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
        packagingOptions {
            pickFirst 'lib/arm64-v8a/libcrypto.so'
            pickFirst 'lib/arm64-v8a/libssl.so'

            pickFirst 'lib/armeabi-v7a/libcrypto.so'
            pickFirst 'lib/armeabi-v7a/libssl.so'

            pickFirst 'lib/x86/libcrypto.so'
            pickFirst 'lib/x86/libssl.so'

            pickFirst 'lib/x86_64/libcrypto.so'
            pickFirst 'lib/x86_64/libssl.so'
		}
		
    }
    dependencies {
        ...
        // for Accura OCR
        implementation 'com.github.accurascan:AccuraOCR:3.1.1'
        // for Accura Face Match
        implementation 'com.github.accurascan:AccuraFaceMatch:3.1.1'
        // for liveness
		implementation 'com.github.accurascan:Liveness-Android:3.1.1'
    }

#### Step 4: Add files to project assets folder:

    Create "assets" folder under app/src/main and Add license file in to assets folder.
    - key.license // for Accura OCR
    - accuraface.license // for Accura Face Match
    Generate your Accura license from https://accurascan.com/developer/dashboard

## 1. Setup Accura OCR
* Require `key.license` to implement Accura OCR in to your app
#### Step 1 : To initialize sdk on app start:

    RecogEngine recogEngine = new RecogEngine();
    RecogEngine.SDKModel sdkModel = recogEngine.initEngine(your activity context);

    if (sdkModel.i > 0) { // if license is valid

         if (sdkModel.isMRZEnable) // RecogType.MRZ

         if (sdkModel.isBankCardEnable)  // RecogType.BANKCARD
         
         if (sdkModel.isAllBarcodeEnable) // RecogType.BARCODE

        // sdkModel.isOCREnable is true then get card list which you are selected on creating license
        if (sdkModel.isOCREnable) List<ContryModel> modelList = recogEngine.getCardList(MainActivity.this);
        if (modelList != null) { // if country & card added in license
            ContryModel contryModel = modelList.get(selected country position);
            contryModel.getCountry_id(); // getting country id
            CardModel model = contryModel.getCards().get(0/*selected card position*/); // getting card
            model.getCard_id() // getting card id
            model.getCard_name()  // getting card name

            if (cardModel.getCard_type() == 1) {
                // RecogType.PDF417
            } else if (cardModel.getCard_type() == 2) {
                // RecogType.DL_PLATE
            } else {
                // RecogType.OCR
            }
        }
    }

##### Update filters like below.</br>
  Call this function after initialize sdk if license is valid(sdkModel.i > 0)
   * Set Blur Percentage to allow blur on document

        ```
		//0 for clean document and 100 for Blurry document
		recogEngine.setBlurPercentage(Context context, int /*blurPercentage*/50);
		```
   * Set Face blur Percentage to allow blur on detected Face

        ```
		// 0 for clean face and 100 for Blurry face
		recogEngine.setFaceBlurPercentage(Context context, int /*faceBlurPercentage*/50);
        ```
   * Set Glare Percentage to detect Glare on document

        ```
		// Set min and max percentage for glare
		recogEngine.setGlarePercentage(Context context, int /*minPercentage*/6, int /*maxPercentage*/98);
		```
   * Set Photo Copy to allow photocopy document or not

        ```
		// Set min and max percentage for glare
		recogEngine.isCheckPhotoCopy(Context context, boolean /*isCheckPhotoCopy*/false);
		```
   * Set Hologram detection to verify the hologram on the face

        ```
		// true to check hologram on face
		recogEngine.SetHologramDetection(Context context, boolean /*isDetectHologram*/true);
		```
   * Set light tolerance to detect light on document

        ```
        // 0 for full dark document and 100 for full bright document
        recogEngine.setLowLightTolerance(Context context, int /*tolerance*/30);
        ```
   * Set motion threshold to detect motion on camera document
		```
        // 1 - allows 1% motion on document and
        // 100 - it can not detect motion and allow document to scan.
        recogEngine.setMotionThreshold(Context context, int /*motionThreshold*/18);
        ```

#### Step 2 : Set CameraView
```
Must have to extend com.accurascan.ocr.mrz.motiondetection.SensorsActivity to your activity.
- Make sure your activity orientation locked from Manifest. Because auto rotate not support.

private CameraView cameraView;

@Override
public void onCreate(Bundle savedInstanceState) {
    if (isPortrait) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // to set portarait mode
    } else {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // to set landscape mode
    }
    super.onCreate(savedInstanceState);
    setTheme(R.style.AppThemeNoActionBar);
    setContentView(R.layout.your layout);

    // Recog type selection base on your license data
    // As like RecogType.OCR, RecogType.MRZ, RecogType.PDF417, RecogType.DL_PLATE, RecogType.BANKCARD
    RecogType recogType = RecogType.OCR;
    cardId = CardModel.getCard_id();
    cardName = CardModel.getCard_name();
    countryId = ContryModel.getCountry_id();

    // initialized camera
    initCamera();
}

private void initCamera() {
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
        		.setMinFrameForValidate(3/*minFrame*/); // Set min frame for qatar ID card for Most validated data. minFrame supports only odd numbers like 3,5...
    } else if (recogType == RecogType.PDF417) {
        // must have to set data RecogType.PDF417
        cameraView.setCountryId(countryId);
    }
    if (recogType == RecogType.MRZ) {
        // Also set MRZ document type to scan specific MRZ document
        // 1. ALL MRZ document       - MRZDocumentType.NONE        
        // 2. Passport MRZ document  - MRZDocumentType.PASSPORT_MRZ
        // 3. ID card MRZ document   - MRZDocumentType.ID_CARD_MRZ 
        // 4. Visa MRZ document      - MRZDocumentType.VISA_MRZ    
        cameraView.setMRZDocumentType(mrzDocumentType);
    }
    cameraView.setRecogType(recogType)
            .setView(linearLayout) // To add camera view
            .setCameraFacing(0) // // To set selfie(1) or rear(0) camera.
            .setOcrCallback(this)  // To get feedback and Success Call back
            .setStatusBarHeight(statusBarHeight)  // To remove Height from Camera View if status bar visible
            .setFrontSide() // or cameraView.setBackSide(); to scan card side front or back default it's scan front side first
//                Option setup
//                .setEnableMediaPlayer(false) // false to disable default sound and true to enable sound and default it is true
//                .setCustomMediaPlayer(MediaPlayer.create(this, /*custom sound file*/)) // To add your custom sound and Must have to enable media player
            .init();  // initialized camera
	// To set barcode formate.
	cameraView.setBarcodeFormat(int barcodeFormat); // access all type of BarcodeFormate from BarcodeFormat.java class
}

/**
 * To handle camera on window focus update
 * @param hasFocus
 */
@Override
public void onWindowFocusChanged(boolean hasFocus) {
    if (cameraView != null) {
        cameraView.onWindowFocusUpdate(hasFocus);
    }
}

@Override
protected void onResume() {
    super.onResume();
    cameraView.onResume();
}

@Override
protected void onPause() {
    cameraView.onPause();
    super.onPause();
}

@Override
protected void onDestroy() {
    cameraView.onDestroy();
    super.onDestroy();
}

/**
 * To update your border frame according to width and height
 * it's different for different card
 * Call {@link CameraView#startOcrScan(boolean isReset)} To start Camera Preview
 * @param width    border layout width
 * @param height   border layout height
 */
@Override
public void onUpdateLayout(int width, int height) {
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
    //</editor-fold>
}

/**
 * Override this method after scan complete to get data from document
 *
 * @param result is scanned card data
 *  result instance of {@link OcrData} if recog type is {@link com.docrecog.scan.RecogType#OCR}
 *              or {@link com.docrecog.scan.RecogType#DL_PLATE} or {@link com.docrecog.scan.RecogType#BARCODE}
 *  result instance of {@link RecogResult} if recog type is {@link com.docrecog.scan.RecogType#MRZ}
 *  result instance of {@link CardDetails} if recog type is {@link com.docrecog.scan.RecogType#BANKCARD}
 *  result instance of {@link PDF417Data} if recog type is {@link com.docrecog.scan.RecogType#PDF417}
 *
 */
@Override
public void onScannedComplete(Object result) {
    // display data on ui thread
    Log.e("TAG", "onScannedComplete: ");
    if (result != null) {
    	// make sure release camera view before open result screen
    	// if (cameraView != null) cameraView.release(true);
        // Do some code for display data

        if (result instanceof OcrData) {
            if (recogType == RecogType.OCR) {
                // @recogType is {@see com.docrecog.scan.RecogType#OCR}
                if (isBack || !cameraView.isBackSideAvailable()) { // To check card has back side or not
            		OcrData.setOcrResult((OcrData) result); // Set data To retrieve it anywhere
                } else {
                    isBack = true;
                    cameraView.setBackSide(); // To recognize data from back side too.
                    cameraView.flipImage(imageFlip);
                }
            } else if (recogType == RecogType.DL_PLATE || recogType == RecogType.BARCODE) {
                // @recogType is {@link RecogType#DL_PLATE} or recogType == {@link RecogType#BARCODE}
            	OcrData.setOcrResult((OcrData) result); // Set data To retrieve it anywhere
            }
        } else if (result instanceof RecogResult) {
            // @recogType is {@see com.docrecog.scan.RecogType#MRZ}
            RecogResult.setRecogResult((RecogResult) result); // Set data To retrieve it anywhere
        } else if (result instanceof CardDetails) {
            //  @recogType is {@see com.docrecog.scan.RecogType#BANKCARD}
            CardDetails.setCardDetails((CardDetails) result); // Set data To retrieve it anywhere
        } else if (result instanceof PDF417Data) {
            //  @recogType is {@see com.docrecog.scan.RecogType#PDF417}
            if (isBack || !cameraView.isBackSideAvailable()) {
                PDF417Data.setPDF417Result((PDF417Data) result); // Set data To retrieve it anywhere
                sendDataToResultActivity(RecogType.PDF417);
            } else {
                isBack = true;
                cameraView.setBackSide(); // To recognize data from back side too.
                cameraView.flipImage(imageFlip);
            }
        }
    } else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
}

/**
 * @param titleCode to display scan card message on top of border Frame
 *
 * @param errorMessage To display process message.
 *                null if message is not available
 * @param isFlip  true to set your customize animation for scan back card alert after complete front scan
 *                and also used cameraView.flipImage(ImageView) for default animation
 */
@Override
public void onProcessUpdate(int titleCode, String errorMessage, boolean isFlip) {
// make sure update view on ui thread
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            if (getTitleMessage(titleCode) != null) { // check
                Toast.makeText(context, getTitleMessage(titleCode), Toast.LENGTH_SHORT).show(); // display title
            }
            if (errorMessage != null) {
                Toast.makeText(context, getErrorMessage(errorMessage), Toast.LENGTH_SHORT).show(); // display message
            }
            if (isFlip) {
                // To set default animation or remove this line to set your custom animation after successfully scan front side.
                cameraView.flipImage(imageFlip);
            }
        }
    });
}

@Override
public void onError(String errorMessage) {
    // display data on ui thread
    // stop ocr if failed
    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
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
        case RecogEngine.SCAN_TITLE_MRZ_PDF417_FRONT:// for front side MRZ, PDF417 and BankCard
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

// After getting result to restart scanning you have to set below code onActivityResult
// when you use startActivityForResult(Intent, RESULT_ACTIVITY_CODE) to open result activity.
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    ...
    if (resultCode == RESULT_OK) {
        if (requestCode == RESULT_ACTIVITY_CODE) {
            Runtime.getRuntime().gc(); // To clear garbage
            //<editor-fold desc="Call CameraView#startOcrScan(true) to scan document again">

            if (cameraView != null) cameraView.startOcrScan(true);

            //</editor-fold>
        }
    }
}
```

## 2. Setup Accura Face Match
* Require `accuraface.license` to implement AccuraFaceMatch SDK in to your app

#### Step 1 : Add following code in Manifest.
    <manifest>
        ...
        <uses-permission android:name="android.permission.CAMERA" />
        <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    </manifest>

#### Step 2 : Open auto capture camera
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

    // 0 for clean face and 100 for Blurry face or set it -1 to remove blur filter
    cameraScreenCustomization.setBlurPercentage(80/*blurPercentage*/); // To allow blur on face
                                                    
    // Set min and max percentage for glare or set it -1 to remove glare filter
	cameraScreenCustomization.setGlarePercentage(6/*glareMinPercentage*/, 99/*glareMaxPercentage*/);
    
    Intent intent = SelfieFMCameraActivity.getCustomIntent(this, cameraScreenCustomization);
    startActivityForResult(intent, ACCURA_FACEMATCH_CAMERA);
    
    // Handle accura fm camera result.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ACCURA_LIVENESS_CAMERA && data != null) {
                AccuraFMCameraModel result = data.getParcelableExtra("Accura.fm");
                if (result == null) {
                    return;
                }
                if (result.getStatus().equals("1")) {
                    // result bitmap
                    Bitmap bitmap = result.getFaceBiometrics();
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed" + result.getStatus(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

#### Step 3 : Implement face match code manually to your activity.

    Important Grant Camera and storage Permission.

    must have to implements FaceCallback, FaceHelper.FaceMatchCallBack to your activity
    ImageView image1,image2;

    // Initialized facehelper in onCreate.
    FaceHelper helper = new FaceHelper(this);

    TextView tvFaceMatch = findViewById(R.id.tvFM);
    tvFaceMatch.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
            ********** For faceMatch
            
            // To pass two image uri for facematch.
            // @params uri1 is for input image
            // @params uri2 is for match image
            
            helper.getFaceMatchScore(uri1, uri2);


            // also use some other method for for face match
            // must have to helper.setInputImage first and then helper.setMatchImage
            // helper.setInputImage(uri1);
            // helper.setMatchImage(uri2);
        
        }
    });
    // Override methods of FaceMatchCallBack

    @Override
    public void onFaceMatch(float score) {
        // get face match score
        System.out.println("Match Score : " + ss + " %");
    }

    @Override
    public void onSetInputImage(Bitmap src1) {
        // set Input image to your view
        image1.setImageBitmap(src1);
    }

    @Override
    public void onSetMatchImage(Bitmap src2) {
        // set Match image to your view
        image2.setImageBitmap(src2);
    }

    // Override methods for FaceCallback

    @Override
    public void onInitEngine(int ret) {
    }

    //call if face detect
    
    @Override
    public void onLeftDetect(FaceDetectionResult faceResult) {
        // must have to call helper method onLeftDetect(faceResult) to get faceMatch score.
        helper.onLeftDetect(faceResult);
    }

    //call if face detect
    @Override
    public void onRightDetect(FaceDetectionResult faceResult) {
        // must have to call helper method onRightDetect(faceResult) to get faceMatch score.
        helper.onRightDetect(faceResult);
    }

    @Override
    public void onExtractInit(int ret) {
    }

    And take a look ActivityFaceMatch.java for full working example.
    
#### Step 4 : Simple Usage to face match in your app.

    // Just add FaceMatchActivity to your manifest:
    <activity android:name="com.accurascan.facematch.ui.FaceMatchActivity"/>

    // Start Intent to open activity
    Intent intent = new Intent(this, FaceMatchActivity.class);
    startActivity(intent);

## 3. Liveness Check

Contact AccuraScan at contact@accurascan.com for Liveness SDK or API

#### Step 1 : Add following code in Manifest.

    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.INTERNET" />

    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />

    <application
        ...
        android:networkSecurityConfig="@xml/network_security_config"
        >

     </application>
     
    Note : 'android:networkSecurityConfig="@xml/network_security_config"' setup will permit clear text traffic   

#### Step 2 :  Add following code to your Application class or MainActivity for hostname verification

    new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
            return hv.verify("your url host name", session);
        }
    };

#### Step 3 : Open camera for liveness Detectcion.

    Must have to Grant camera permission

    private final static int ACCURA_LIVENESS_CAMERA = 100;

    // To customize your screen theme and feed back messages
    LivenessCustomization livenessCustomization = new LivenessCustomization();

    livenessCustomization.backGroundColor = getResources().getColor(R.color.livenessBackground);
    livenessCustomization.closeIconColor = getResources().getColor(R.color.livenessCloseIcon);
    livenessCustomization.feedbackBackGroundColor = Color.TRANSPARENT;
    livenessCustomization.feedbackTextColor = Color.BLACK;
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
    
    // 0 for clean face and 100 for Blurry face or set it -1 to remove blur filter
    livenessCustomization.setBlurPercentage(80/*blurPercentage*/); // To allow blur on face
                                                    
    // Set min and max percentage for glare or set it -1 to remove glare filter
    livenessCustomization.setGlarePercentage(6/*glareMinPercentage*/, 99/*glareMaxPercentage*/);

    // must have to call SelfieCameraActivity.getCustomIntent() to create intent
    Intent intent = SelfieCameraActivity.getCustomIntent(this, livenessCustomization, "your_url");
    startActivityForResult(intent, ACCURA_LIVENESS_CAMERA);


    // Handle accura liveness result.
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
                    // result get bitmap of face by using following code
                    Bitmap bitmap = result.getFaceBiometrics();
                    double livenessScore = result.getLivenessResult().getLivenessScore() * 100.0;
                    Toast.makeText(this, "Liveness Score : " + livenessScore, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, result.getStatus() + " " + result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

## ProGuard

Depending on your ProGuard (DexGuard) config and usage, you may need to include the following lines in your proguards.

```
-keep public class com.docrecog.scan.ImageOpencv {;}
-keep class com.accurascan.ocr.mrz.model.* {;}
-keep class com.accurascan.ocr.mrz.interfaces.* {;}
-keep public class com.inet.facelock.callback.FaceCallback {*;}
-keep public class com.inet.facelock.callback.FaceDetectionResult {*;}
```
