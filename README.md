# AccuraScan's Android SDK for KYC & ID Verification - OCR, Face Biometrics and Liveness Check

1. High Accuracy OCR (Optical Character Recognition) Includes English, Latin, Chinese, Korean and Japanese Languages.
2. Face Biometrics Is Used for Matching Both The Source And The Target Image. It Matches the User's Selfie Image with The Image on The Document.
3. User Authentication and Liveness Check Is Used for Customer Verification and Authentication. It Protects You from Identity Theft & Spoofing Attacks Through the Use of Active and Passive Selfie Technology for Liveness Check.

Below steps to setup AccuraScan's SDK to your project.

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

    authToken=jp_45kf9tvkijvd9c7cf34mehj1b6

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
            sourceCompatibility JavaVersion.VERSION_17
            targetCompatibility JavaVersion.VERSION_17
        }
		
    }
    dependencies {
        ...
        // for Accura OCR
        implementation 'com.github.accurascan:AccuraOCR:7.0.2'
    }

#### Step 4: Add files to project assets folder:

    Create "assets" folder under app/src/main and Add license file in to assets folder.
    - key.license // for Accura OCR

## 1. Setup Accura OCR
* Require `key.license` to implement Accura OCR-MICR in to your app
#### Step 1 : To initialize sdk on app start:

    RecogEngine recogEngine = new RecogEngine();
    RecogEngine.SDKModel sdkModel = recogEngine.initEngine(your activity context);

    if (sdkModel.i > 0) { // if license is valid

         if (sdkModel.isChequeEnable) // RecogType.MICR
    }

#### Optional: Load License File Dynamically
If you prefer to place the license file dynamically, you can use the following method. This allows you to specify the license file path at runtime.
For a demo of dynamic licensing, please refer to the **dynamic_license_demo** branch.
```
RecogEngine.SDKModel sdkModel = recogEngine.initEngine(activity, "your license filepath");
```

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
    RecogType recogType = RecogType.MICR;

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
 *  result instance of {@link MICR} if recog type is {@link com.docrecog.scan.RecogType#MICR}
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

        if (result instanceof MICRDetails) {
            /**
             *  @recogType is {@link RecogType#MICR}*/
            MICRDetails.setMicrDetails((MICRDetails) result);
            sendDataToResultActivity(RecogType.MICR);
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
        case RecogEngine.SCAN_TITLE_MRZ_PDF417_FRONT:// for Cheque
            if (recogType == RecogType.MICR) {
                return "Scan Cheque";
            } else
                return "Scan Front Side of Document";
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
        case RecogEngine.ACCURA_ERROR_CODE_CLOSER:
            return "Move phone Closer";
        case RecogEngine.ACCURA_ERROR_CODE_AWAY:
            return "Move phone Away";
        case RecogEngine.ACCURA_ERROR_CODE_MICR_IN_FRAME:
            return "Place MICR Properly in Frame";
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

## ProGuard

Depending on your ProGuard (DexGuard) config and usage, you may need to include the following lines in your proguards.

```
-keep public class com.docrecog.scan.ImageOpencv {;}
-keep class com.accurascan.ocr.mrz.model.* {;}
-keep class com.accurascan.ocr.mrz.interfaces.* {;}
```
