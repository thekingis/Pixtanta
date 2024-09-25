package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pixtanta.android.Adapter.ViewPagerAdapter;
import com.pixtanta.android.Interface.AddTextFragmentListener;
import com.pixtanta.android.Interface.BrushFragmentListener;
import com.pixtanta.android.Interface.EditImageFragmentListener;
import com.pixtanta.android.Interface.EmojiFragmentListener;
import com.pixtanta.android.Interface.FiltersListFragmentListener;
import com.pixtanta.android.Utils.StringUtils;
import com.pixtanta.android.Views.CustomImageView;
import com.yalantis.ucrop.UCrop;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.ViewType;

public class PhotoEditorAct extends ThemeActivity implements FiltersListFragmentListener, EditImageFragmentListener, BrushFragmentListener, EmojiFragmentListener, AddTextFragmentListener {

    Context cntxt;
    String lang, myIDtoString, newFilePath, fileName, mimeType;
    int myId;
    static int fileId;
    public static String filePath;
    TabLayout tabLayout;
    static CustomImageView customImageView;
    @SuppressLint("StaticFieldLeak")
    static PhotoEditor photoEditor;
    ViewPager viewPager;
    Button cancelDiscard, agreeDiscard;
    TextView saveEdit, discardEdit;
    ImageButton undo, redo;
    LinearLayout discardLayout, blackFade;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout loader;
    static String tempDir, primaryStorage, cropPath;
    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;
    BrushFragment brushFragment;
    CropImageFragment cropImageFragment;
    EmojiFragment emojiFragment;
    AddTextFragment addTextFragment;
    AddImageFragment addImageFragment;
    RotateLeftFragment rotateLeftFragment;
    RotateRightFragment rotateRightFragment;
    public static Bitmap originalBitmap, filteredBitmap, finalBitmap, bitmap;
    int[] tabIcons;
    int brightnessFinal = 0;
    float constraintFinal = 1.0f;
    float saturationFinal = 1.0f;
    RelativeLayout mainHomeView;
    static {
        System.loadLibrary("NativeImageProcessor");
    }
    SharedPrefMngr sharedPrefMngr;
    static JSONArray editArray;
    static int act, editState, editCount, tabIndex;
    File file;
    static ImageLoader imageLoader;
    static String randStr = UUID.randomUUID().toString();
    @SuppressLint("SimpleDateFormat")
    static String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
    static String cropFilePath = randStr + timeStamp;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    boolean shakeOpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);

        lang = sharedPrefMngr.getSelectedLanguage();
        mainHomeView =  findViewById(R.id.mainHomeView);
        setupUI(mainHomeView);

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(PhotoEditorAct.this, LoginAct.class));
            return;
        }
        primaryStorage = StorageUtils.getStorageDirectories(cntxt)[0];
        //alternative if storage don't exist... but it definately will
                /*if(!(new File(tempDir).exists()))
                    tempDir = StorageUtils.getStorageDirectories(cntxt)[1];*/
        tempDir = primaryStorage + "/Android/data/" + getApplicationContext().getPackageName() + "/tempFiles";
        if(!(new File(tempDir).exists()))
            new File(tempDir).mkdir();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(() -> {
            shakeOpt = sharedPrefMngr.checkShakeOption();
            if(shakeOpt)
                openReportDialog();
        });

        editState = 0;
        editCount = 0;
        editArray = new JSONArray();
        myId = sharedPrefMngr.getMyId();
        Bundle fileParams = getIntent().getExtras();
        act = fileParams.getInt("act");
        fileId = fileParams.getInt("fileId");
        filePath = fileParams.getString("filePath");
        myIDtoString = Integer.toString(myId);
        file = new File(filePath);
        fileName = file.getName();
        Uri uris = Uri.fromFile(file);
        String fileExt = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.toLowerCase());
        tabIcons = new int[]{
                R.drawable.ic_filter,
                R.drawable.ic_image,
                R.drawable.ic_brush,
                R.drawable.ic_crop,
                R.drawable.ic_emoticon,
                R.drawable.ic_text,
                R.drawable.ic_rotate_left,
                R.drawable.ic_rotate_right
                /*R.drawable.ic_frames,*/
        };

        imageLoader = new ImageLoader(this);
        customImageView =  findViewById(R.id.photoEditorView);
        tabLayout =  findViewById(R.id.tabs);
        viewPager =  findViewById(R.id.viewPager);
        blackFade =  findViewById(R.id.blackFade);
        loader =  findViewById(R.id.loader);
        discardLayout =  findViewById(R.id.discardLayout);
        saveEdit =  findViewById(R.id.saveEdit);
        discardEdit =  findViewById(R.id.discardEdit);
        cancelDiscard =  findViewById(R.id.cancelDiscard);
        agreeDiscard =  findViewById(R.id.agreeDiscard);
        undo =  findViewById(R.id.undo);
        redo =  findViewById(R.id.redo);
        photoEditor = new PhotoEditor.Builder(this, customImageView)
                .setPinchTextScalable(true)
                .setDefaultEmojiTypeface(Typeface.createFromAsset(getAssets(), "emojione-android.ttf"))
                .build();
        if(filePath.startsWith("http"))
            bitmap = Functions.getBitmapFromURL(filePath, false);
        else
            bitmap = Functions.decodeFiles(filePath, "image", false);
        initializeEditor();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab){
                tabIndex = tab.getPosition();
                photoEditor.setBrushDrawingMode(tabIndex == 2);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(position == 2)
                    photoEditor.setBrushDrawingMode(false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                photoEditor.setBrushDrawingMode(position == 2);
            }
        });
        undo.setOnClickListener(v -> doImageEdit(false));
        redo.setOnClickListener(v -> doImageEdit(true));
        saveEdit.setOnClickListener(v -> saveImageEdit());
        discardEdit.setOnClickListener(v -> discardLayout.setVisibility(View.VISIBLE));
        cancelDiscard.setOnClickListener(v -> discardLayout.setVisibility(View.GONE));
        agreeDiscard.setOnClickListener(v -> {
            deleteTempFiles();
            finish();
        });
        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });

    }

    private void doImageEdit(boolean toRedo) {
        try {
            if((!toRedo && editState < 1) || (toRedo && editState > editArray.length() - 1))
                return;
            if (toRedo)
                editState += 1;
            else
                editState -= 1;
            Bitmap btmp = (Bitmap) editArray.get(editState);
            customImageView.setImageBitmap(btmp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void saveEditState(Context context){
        int x = editState + 1;
        if(editArray.length() > editState){
            for (int i = editState; i < editArray.length(); i++)
                editArray.remove(i);
        }
        try {
            //finalBitmap = getBitmapFromCanvas();
            editArray.put(x, finalBitmap);
            //photoEditor.clearAllViews();
            //customImageView.setImageBitmap(finalBitmap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap getBitmapFromCanvas() {
        Bitmap btmp = Bitmap.createBitmap(customImageView.getWidth(), customImageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(btmp);
        canvas.drawColor(Color.WHITE);
        customImageView.draw(canvas);
        return btmp;
    }

    private void openReportDialog() {
        @SuppressLint("InflateParams") RelativeLayout view = (RelativeLayout) getLayoutInflater().inflate(R.layout.report_box, null);
        Button sendBtn =  view.findViewById(R.id.sendBtn);
        EditText reportText =  view.findViewById(R.id.reportText);
        TextView toogle =  view.findViewById(R.id.toogle);
        shakeOpt = sharedPrefMngr.checkShakeOption();
        if(!shakeOpt){
            toogle.setTag("false");
            toogle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_false, 0, 0, 0);
        }
        toogle.setOnClickListener(v -> {
            boolean curOptn = Boolean.parseBoolean(toogle.getTag().toString());
            boolean optn = !curOptn;
            int drw = R.drawable.ic_check_true;
            if(!optn)
                drw = R.drawable.ic_check_false;
            toogle.setTag(optn);
            toogle.setCompoundDrawablesWithIntrinsicBounds(drw, 0, 0, 0);
            sharedPrefMngr.saveShakeOption(optn);
        });
        sendBtn.setOnClickListener(v -> {
            String text = reportText.getText().toString();
            if(!StringUtils.isEmpty(text)) {
                try {
                    String actName = cntxt.getClass().getSimpleName();
                    Functions.sendReport(myId, actName, text);
                    Toast.makeText(cntxt, "Report Sent!", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                blackFade.setVisibility(View.GONE);
            }
        });
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        blackFade.addView(view);
        blackFade.setVisibility(View.VISIBLE);

    }

    private void initializeEditor(){
        try {
            if (!(bitmap == null)) {
                editArray.put(editState, bitmap);
                newFilePath = getBitmapPath(cntxt, bitmap);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                cropPath = MediaStore.Images.Media.insertImage(cntxt.getContentResolver(), bitmap, cropFilePath, null);
                cropFilePath = getRealPathFromURI(Uri.parse(cropPath));
                tabLayout.setupWithViewPager(viewPager);
                loadImage();
                setupViewPager(viewPager);
                setupTabIcons();
                loader.setVisibility(View.GONE);
                sharedPrefMngr.stockTempFiles(newFilePath);
                sharedPrefMngr.stockTempFiles(cropFilePath);
                photoEditor.setOnPhotoEditorListener(new OnPhotoEditorListener() {
                    @Override
                    public void onEditTextChangeListener(View rootView, String text, int colorCode) {
                    }

                    @Override
                    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
                        saveEditState(getApplicationContext());
                    }

                    @Override
                    public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {
                        saveEditState(getApplicationContext());
                    }

                    @Override
                    public void onStartViewChangeListener(ViewType viewType) {
                    }

                    @Override
                    public void onStopViewChangeListener(ViewType viewType) {
                    }
                });
            } else new android.os.Handler().postDelayed(this::initializeEditor, 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UCrop.REQUEST_CROP && !(data == null)){
            handleCropper(data);
        }
    }

    private void handleCropper(Intent data) {
        final Uri resultUri = UCrop.getOutput(data);
        customImageView.setImageURI(resultUri);
        Bitmap bitmap = ((BitmapDrawable) customImageView.getSource().getDrawable()).getBitmap();
        originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        filteredBitmap = originalBitmap;
        finalBitmap = originalBitmap;
        saveEditState(cntxt);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        filtersListFragment = new FiltersListFragment();
        filtersListFragment.setListener(this);
        editImageFragment = new EditImageFragment();
        editImageFragment.setListener(this);
        brushFragment = new BrushFragment();
        brushFragment.setListener(this);
        cropImageFragment = new CropImageFragment(cntxt);
        emojiFragment = new EmojiFragment();
        emojiFragment.setListener(this);
        addTextFragment = new AddTextFragment();
        addTextFragment.setListener(this);
        //addImageFragment = new AddImageFragment(); //omitted
        rotateLeftFragment = new RotateLeftFragment();
        rotateRightFragment = new RotateRightFragment();
        adapter.addFragment(filtersListFragment, null);
        adapter.addFragment(editImageFragment, null);
        adapter.addFragment(brushFragment, null);
        adapter.addFragment(cropImageFragment, null);
        adapter.addFragment(emojiFragment, null);
        adapter.addFragment(addTextFragment, null);
        //adapter.addFragment(addImageFragment, null); //omitted
        adapter.addFragment(rotateLeftFragment, null);
        adapter.addFragment(rotateRightFragment, null);
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        for(int x = 0; x < tabIcons.length; x++){
            Objects.requireNonNull(tabLayout.getTabAt(x)).setIcon(tabIcons[x]);
        }
    }

    public static void loadImage() {
        originalBitmap = bitmap;
        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        customImageView.setImageBitmap(originalBitmap);
    }

    public String getBitmapPath(Context context, Bitmap inImage) {
        Uri uri;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
            uri = Uri.parse(path);
        } else {
            String newName = System.currentTimeMillis() + "_" + fileName;
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/");
            contentValues.put(MediaStore.Images.Media.TITLE, newName);
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, newName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
            contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
            ContentResolver contentResolver = getContentResolver();
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            contentValues.clear();
            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0);
            contentResolver.update(uri, contentValues, null, null);
        }
        return getRealPathFromURI(uri);
    }

    public String getRealPathFromURI(Uri uri) {
        @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @Override
    public void onBrightnessChanged(int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        customImageView.setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onConstraintChanged(float constraint) {
        constraintFinal = constraint;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(constraint));
        customImageView.setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onSaturationChanged(float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        customImageView.setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onEditStart() {

    }

    @Override
    public void onEditComplete() {
        Bitmap bitmap = finalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new ContrastSubFilter(constraintFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        finalBitmap = myFilter.processFilter(bitmap);
    }

    @Override
    public void onFilterSelected(Filter filter) {
        resetControl();
        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        customImageView.setImageBitmap(filter.processFilter(filteredBitmap));
        finalBitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    private void resetControl() {
        if(editImageFragment != null)
            editImageFragment.resetControls();
        brightnessFinal = 0;
        constraintFinal = 1.0f;
        saturationFinal = 1.0f;
    }

    private void saveImageEdit(){
        photoEditor.saveAsBitmap(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                customImageView.setImageBitmap(saveBitmap);
                finalBitmap = saveBitmap;
                File pictureFile = Functions.getOutputMediaFile(tempDir);
                String picturePath = Objects.requireNonNull(pictureFile).getAbsolutePath();
                try {
                    deleteTempFiles();
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    saveBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                    fos.close();
                    if(act == 0) {
                        if(HomeAct.editedImages.contains(filePath) && new File(filePath).exists())
                            HomeAct.editedImages.remove(filePath);
                        HomeAct homeAct = new HomeAct();
                        HomeAct.itemLists.set(fileId, pictureFile);
                        HomeAct.selectedImages.set(fileId, picturePath);
                        HomeAct.editedImages.add(picturePath);
                        homeAct.setImageGridViews(cntxt);
                    }
                    if(act == 1) {
                        if (EditPostActivity.editedImages.contains(filePath) && new File(filePath).exists() && !picturePath.equals(filePath))
                            EditPostActivity.editedImages.remove(filePath);
                        EditPostActivity.editedImages.add(picturePath);
                        if(EditPostActivity.postImages.size() > fileId){
                            EditPostActivity.postImages.set(fileId, picturePath);
                        } else {
                            int fileIndex = fileId - EditPostActivity.postImages.size();
                            EditPostActivity.itemLists.set(fileIndex, pictureFile);
                            EditPostActivity.selectedImages.set(fileIndex, picturePath);
                        }
                        EditPostActivity editPostActivity = new EditPostActivity();
                        editPostActivity.setImageGridViews(cntxt);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
        finish();
    }

    @Override
    public void onBrushSizeChangedListener(float size) {
        photoEditor.setBrushSize(size);
    }

    @Override
    public void onBrushOpacityChangedListener(int opacity) {
        photoEditor.setOpacity(opacity);
    }

    @Override
    public void onBrushColorChangedListener(int color) {
        photoEditor.setBrushColor(color);
    }

    @Override
    public void onEmojiSelected(String emoji) {
        photoEditor.addEmoji(emoji);
    }

    @Override
    public void onAddtextButtonClick(String text, int color) {
        photoEditor.addText(text, color);
    }

    public static void addImageToEdit(Bitmap bitmap){
        photoEditor.addImage(bitmap);
    }

    public static void rotateImage(Context context, int angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        finalBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, finalBitmap.getWidth(), finalBitmap.getHeight(), matrix, false);
        customImageView.setImageBitmap(finalBitmap);
        saveEditState(context);
        /*int prevAngle = Integer.parseInt(photoEditorView.getTag().toString());
        int newAngle = prevAngle + angle;
        photoEditorView.setTag(newAngle);
        photoEditorView.setRotation(newAngle);*/
    }

    private void deleteTempFiles() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(!(newFilePath == null)){
                    File newFile = new File(newFilePath);
                    if(newFile.exists())
                        Files.delete(newFile.toPath());
                }
                if(!(cropFilePath == null)){
                    File cropFileP = new File(cropFilePath);
                    if(cropFileP.exists())
                        Files.delete(cropFileP.toPath());
                }
            } else {
                if(!(newFilePath == null)){
                    File newFile = new File(newFilePath);
                    if(newFile.exists()) newFile.delete();
                }
                if(!(cropFilePath == null)){
                    File cropFileP = new File(cropFilePath);
                    if(cropFileP.exists()) cropFileP.delete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
        } else if(discardLayout.getVisibility() == View.VISIBLE){
            discardLayout.setVisibility(View.GONE);
        } else {
            discardLayout.setVisibility(View.VISIBLE);
        }
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(v);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    private boolean getDefaultDarkThemeEnabled(){
        int defaultThemeMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return defaultThemeMode == Configuration.UI_MODE_NIGHT_YES || defaultThemeMode == Configuration.UI_MODE_NIGHT_MASK;
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean currentTheme = getDefaultDarkThemeEnabled();
        if(!(currentTheme == defaultDarkThemeEnabled)){
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(activity.getPackageName());
            activity.finishAffinity();
            startActivity(new Intent(this, MainActivity.class));
            return;
        }
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }
}
