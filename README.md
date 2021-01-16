# Accura KYC Android SDK - OCR, Face Match & Liveness Check
Android KYC SDK - OCR &amp; Face Match <br/><br/>
Accura OCR is used for Optical character recognition.<br/><br/>
Accura Face Match is used for Matching 2 Faces. Source and Target. It matches the User Image from a Selfie vs User Image in document.<br/><br/>
Accura Authentication is used for your customer verification and authentication.Unlock the True Identity of Your Users with 3D Selfie Technology<br/><br/>


Below steps to setup Accura SDK's to your project.

## Install SDK in to your App

Step 1: Add the JitPack repository to your build file:
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

Step 2. Add the token to $HOME/.gradle/gradle.properties:

    authToken=jp_lo9e8qo0o1bt4ofne9hob61v19

Step 3: Add the dependency:
    Set AccuraOcr as a dependency to our app/build.gradle file.

    android {
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
        implementation 'com.github.accurascan:AccuraOCR:1.0.3'
        // for Accura Face Match
        implementation 'com.github.accurascan:AccuraFaceMatch:1.0'
    }

Step 4: Add files to project assets folder:(/src/main/assets) <br />
    Add licence file in to your app assets folder.<br />
    - key.licence // for Accura OCR <br />
    - accuraface.license // for Accura Face Match <br />
    Generate your Accura licence from https://accurascan.com/developer/dashboard

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

## 1. Setup Accura OCR

Step 1 : To initialize sdk on app start:

    RecogEngine recogEngine = new RecogEngine();
    RecogEngine.SDKModel sdkModel = recogEngine.initEngine(your activity context);

    if (sdkModel.i > 0) {
        // if sdkModel.isOCREnable then get card data
        if (sdkModel.isOCREnable) List<ContryModel> modelList = recogEngine.getCardList(MainActivity.this);
        if (modelList != null) {
            ContryModel contryModel = modelList.get(position);
            contryModel.getCountry_id(); // get country code
            CardModel model = contryModel.getCards().get(0);
            model..getCard_id() // get card code
        }
    }

    Some customized function below.
    Call this function after initialize sdk

    /**
     * Set Blur Percentage to allow blur on document
     *
     * @param context        Activity context
     * @param blurPercentage is 0 to 100, 0 - clean document and 100 - Blurry document
     * @return 1 if success else 0
     */
     
    recogEngine.setBlurPercentage(Context context, int /*blurPercentage*/60);
    
    /**
     * Set Blur Percentage to allow blur on detected Face
     *
     * @param context            Activity context
     * @param faceBlurPercentage is 0 to 100, 0 - clean face and 100 - Blurry face
     * @return 1 if success else 0
     */
    
    recogEngine.setFaceBlurPercentage(Context context, int /*faceBlurPercentage*/55);
    
    /**
     * @param context       Activity context
     * @param minPercentage Min value
     * @param maxPercentage Max value
     * @return 1 if success else 0
     */
    
    recogEngine.setGlarePercentage(Context context, int /*minPercentage*/6, int /*maxPercentage*/98);
    
    /**
     * Set CheckPhotoCopy to allow photocopy document or not
     *
     * @param context          Activity context
     * @param isCheckPhotoCopy if true then reject photo copy document else vice versa
     * @return 1 if success else 0
     */
    
    recogEngine.isCheckPhotoCopy(Context context, boolean /*isCheckPhotoCopy*/false);
    
    /**
     * set Hologram detection to allow hologram on face or not
     *
     * @param context          Activity context
     * @param isDetectHologram if true then reject face if hologram in face else it is allow .
     * @return 1 if success else 0
     */
    
    recogEngine.SetHologramDetection(Context context, boolean /*isDetectHologram*/true);


Step 2 : Set CameraView

    private CameraView cameraView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeNoActionBar);
        setContentView(R.layout.your layout);

        RecogType recogType = RecogType.OCR; // or set RecogType.MRZ, RecogType.PDF417

        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentViewTop - statusBarHeight;

        RelativeLayout linearLayout = findViewById(R.id.ocr_root); // set layout width and height is match_parent
        cameraView = new CameraView(this);
        if (recogType == RecogType.OCR) {
        
        // must have a country code and card code for RecogType.OCR
        
        cameraView.setCountryCode(countryCode)
                    .setCardCode(cardCode);

        } else if (recogType == RecogType.PDF417) {
        
        // must have a country code for RecogType.PDF417
        
        cameraView.setCountryCode(countryCode);
        }
        cameraView.setRecogType(recogType)
                .setView(linearLayout)
                .setOcrCallback(this)
                .setTitleBarHeight(titleBarHeight) // for get camera height if title bar is visible and also add Action bar height if visible.
                .init();
    }

    /**
     * to handle camera on window focus update
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
     * to update your border frame according to width and height
     * it's different for different card
     * call {@link CameraView#startOcrScan()} method to start camera preview
     * @param width
     * @param height
     */
     
    @Override
    public void onUpdateLayout(int width, int height) {
        if (cameraView != null) cameraView.startOcrScan();

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
    }

    /**
     * call this method after retrieve data from card
     * @param data is scanned card data if set {@link com.docrecog.scan.RecogType#OCR} else it is null
     * @param mrzData an mrz card data if set {@link com.docrecog.scan.RecogType#MRZ} else it is null
     * @param pdf417Data an barcode PDF417 data if set {@link com.docrecog.scan.RecogType#PDF417} else it is null
     */
     
    @Override
    public void onScannedComplete(OcrData data, RecogResult mrzData, PDF417Data pdf417Data) {
        if (data != null) {
            OcrData.setOcrResult(data);
        } else if (mrzData != null) {
            RecogResult.setRecogResult(mrzData);
        } else if (pdf417Data != null) {
            PDF417Data.setPDF417Result(pdf417Data);
        } else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * @param title to display scan card message(is front/ back card of the #cardName)
     *              null if title is not available.
     * @param message to display process message.
     *                null if message is not available
     * @param isFlip to set your customize animation after complete front scan
     */
     
    @Override
    public void onProcessUpdate(String title, String message, boolean isFlip) {
        if (title != null) {
            Toast.makeText(this, title, Toast.LENGTH_SHORT).show(); // display title
        }
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); // display message
        }
        if (isFlip) {
            cameraView.flipImage(imageFlip); //  to set default animation or remove this line to set your customize animation
        }
    }

    @Override
    public void onError(String errorMessage) {
        // stop ocr if failed
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

## 2. Setup Accura Face Match

1). Simple Usage to face match in your app.

    // Just add FaceMatch activity to your manifest:
    <activity android:name="com.accurascan.facematch.ui.FaceMatchActivity"/>

    // Start Intent to open activity
    Intent intent = new Intent(this, FaceMatchActivity.class);
    startActivity(intent);

2). Implement face match code manually to your activity.

    Important Grant Camera and storage Permission.

    must have to implements FaceCallback, FaceHelper.FaceMatchCallBack to your activity
    ImageView image1,image2;

    // Initialized facehelper in onCreate.
    FaceHelper helper = new FaceHelper(this);

    TextView tvFaceMatch = findViewById(R.id.tvFM);
    tvFaceMatch.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
            ********** For faceMatch
            
            // To pass two image url for facematch.
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

3. Liveness Check

Contact AccuraScan at contact@accurascan.com for Liveness SDK or API 

## ProGuard
--------
Depending on your ProGuard (DexGuard) config and usage, you may need to include the following lines in your proguards.

```pro
-keep public class com.docrecog.scan.ImageOpencv {;}
-keep class com.accurascan.ocr.mrz.model.* {;}
-keep class com.accurascan.ocr.mrz.interfaces.* {;}
-keep public class com.inet.facelock.callback.FaceCallback {*;}
-keep public class com.inet.facelock.callback.FaceDetectionResult {*;}
```
