package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pixtanta.android.Adapter.ViewPagerAdapter;
import com.pixtanta.android.Utils.ContextData;
import com.pixtanta.android.Utils.StaticSaver;
import com.pixtanta.android.Utils.StringUtils;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.pageCounter;
import static com.pixtanta.android.Constants.pageForArt;
import static com.pixtanta.android.Constants.pageForBrnd;
import static com.pixtanta.android.Constants.pageForCom;
import static com.pixtanta.android.Constants.pageForEnt;
import static com.pixtanta.android.Constants.pageForLocal;
import static com.pixtanta.android.Constants.socketUrl;
import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.HomeAct.updateMessageDelivery;

public class PageActivity extends ThemeActivity {

    LinearLayout covPhtLayout, holder, viewHolder, saverL, foldLayout, boxer, blackFade, comMentionLayer;
    Context cntxt;
    static Activity activity;
    Button saveChange, discardChange;
    TextView textName, userName, follow, editPage;
    ImageView prfPht, covPht, changeDpBtn, changeCpBtn;
    ProgressBar progressBar;
    ImageLoader imageLoader;
    ViewPager viewPager;
    TabLayout tabLayout;
    CardView changeDp, changeCp;
    ScrollView comScrllView;
    int viewHolderH, tabH, verifiedIcon, index, pageId, user, width, height, pageCount;
    String lang, myIDtoString, coverPhoto, myPht, myName, myUserName, pageIDtoString, photo, pageName, category;
    String[][] pageTypes;
    static String tempDir;
    File file, selectedFile, CPFile, DPFile, CPFileCrop, DPFileCrop;
    private int myId;
    RelativeLayout mainHomeView, photoHolder;
    PageHomeFragment pageHomeFragment;
    PagePhotosFragment pagePhotosFragment;
    PageVideosFragment pageVideosFragment;
    AboutPageFragment aboutPageFragment;
    NestedScrollView nest;
    boolean aBoolean, uploading, loadedPhotos, loadedVideos, loadedInfo, pageLoaded;
    Socket socket;
    ClipboardManager clipboard;
    boolean verified, followingPage, shakeOpt;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    JSONObject sockt = new JSONObject();
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        cntxt = this;
        activity = this;
        sharedPrefMngr = new SharedPrefMngr(this);
		
        lang = sharedPrefMngr.getSelectedLanguage();
        verifiedIcon = R.drawable.ic_verified_user;
        aBoolean = false;
        uploading = false;
        loadedPhotos = false;
        loadedVideos = false;
        loadedInfo = false;
        pageLoaded = false;
        pageCounter++;
        pageCount = pageCounter;

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(this, LoginAct.class));
            return;
        }
        tempDir = StorageUtils.getStorageDirectories(cntxt)[0] + "/Android/data/" + getApplicationContext().getPackageName() + "/tempFiles";
        if(!(new File(tempDir).exists()))
            new File(tempDir).mkdir();
        myId = sharedPrefMngr.getMyId();
        Bundle userParams = getIntent().getExtras();
        pageId = userParams.getInt("pageId");
        pageTypes = new String[][]{
                pageForLocal,
                pageForCom,
                pageForBrnd,
                pageForArt,
                pageForEnt
        };

        try {
            sockt.put("user", myId);
            sockt.put("pageId", pageId);
            socket = IO.socket(socketUrl);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
                socket.emit("connected", myId);
                socket.emit("connectedPagePages", sockt);
            }));
            socket.connect();
            StaticSaver.saveSocket(pageCount, socket);
        } catch (URISyntaxException | JSONException e) {
            e.printStackTrace();
        }
        updateMessageDelivery(myId);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(() -> {
            shakeOpt = sharedPrefMngr.checkShakeOption();
            if(shakeOpt)
                openReportDialog();
        });
        myIDtoString = Integer.toString(myId);
        pageIDtoString = Integer.toString(pageId);
        myPht = www + sharedPrefMngr.getMyPht();
        myName = sharedPrefMngr.getMyName();
        myUserName = sharedPrefMngr.getMyUserName();
        verified = sharedPrefMngr.getMyVerification();

        mainHomeView =  findViewById(R.id.mainHomeView);
        photoHolder =  findViewById(R.id.photoHolder);
        boxer =  findViewById(R.id.boxer);
        foldLayout =  findViewById(R.id.foldLayout);
        blackFade =  findViewById(R.id.blackFade);
        holder =  findViewById(R.id.holder);
        saverL =  findViewById(R.id.saverL);
        viewHolder =  findViewById(R.id.viewHolder);
        covPhtLayout =  findViewById(R.id.covPhtLayout);
        comScrllView =  findViewById(R.id.comScrllView);
        comMentionLayer =  findViewById(R.id.comMentionLayer);
        textName =  findViewById(R.id.textName);
        userName =  findViewById(R.id.userName);
        follow =  findViewById(R.id.follow);
        editPage =  findViewById(R.id.editPage);
        progressBar =  findViewById(R.id.progressBar);
        covPht =  findViewById(R.id.covPht);
        prfPht =  findViewById(R.id.prfPht);
        changeDpBtn =  findViewById(R.id.changeDpBtn);
        changeCpBtn =  findViewById(R.id.changeCpBtn);
        saveChange =  findViewById(R.id.saveChange);
        discardChange =  findViewById(R.id.discardChange);
        changeDp =  findViewById(R.id.changeDp);
        changeCp =  findViewById(R.id.changeCp);
        tabLayout =  findViewById(R.id.tabLayout);
        viewPager =  findViewById(R.id.viewPager);
        nest =  findViewById(R.id.nest);
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        imageLoader = new ImageLoader(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        covPhtLayout.getLayoutParams().height = (int) Math.round(width * 0.45);
        tabLayout.post(() -> {
            tabH = tabLayout.getHeight();
            viewHolderH = mainHomeView.getHeight() - tabH;
            ViewGroup.LayoutParams params = viewPager.getLayoutParams();
            params.height = viewHolderH;
            viewPager.setLayoutParams(params);
        });
        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });

        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        if(darkThemeEnabled){
            changeDp.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryRed));
            changeCp.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryRed));
        }

        loadPageContent();

        socket.on("submitMessage", args -> {
            try {
                JSONObject msgData = new JSONObject(args[0].toString());
                int userTo = msgData.getInt("userTo");
                if(userTo == myId)
                    updateMessageDelivery(myId);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        socket.on("followPage", args -> {
            try {
                JSONObject emitObj = new JSONObject(args[0].toString());
                boolean val = emitObj.getBoolean("val");
                boolean newTag = !val;
                runOnUiThread(() -> setFollow(newTag));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        socket.on("addToFriend", args -> {
            String theUser = args[0].toString();
            socket.emit("addToFriend", theUser);
        });
        socket.on("removeFriend", args -> {
            String theUser = args[0].toString();
            socket.emit("removeFriend", theUser);
        });

    }

    private void openPageEditor() {
        Intent intent = new Intent(this, PageEditActivity.class);
        Bundle userParams = new Bundle();
        userParams.putInt("pageId", pageId);
        intent.putExtras(userParams);
        startActivity(intent);
    }

    @SuppressLint("InflateParams")
    private void openReportDialog() {
        RelativeLayout view = (RelativeLayout) getLayoutInflater().inflate(R.layout.report_box, null);
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

    @SuppressLint("InflateParams")
    private void savePhotoChange() {
        uploading = true;
        holder.setVisibility(View.VISIBLE);
        saverL.setVisibility(View.GONE);
        RelativeLayout progressView = (RelativeLayout) getLayoutInflater().inflate(R.layout.progress_bar, null);
        TextView progressText =  progressView.findViewById(R.id.postProgressText);
        ProgressBar progressBar =  progressView.findViewById(R.id.postProgressBar);
        photoHolder.addView(progressView);
        progressView.setOnClickListener(v -> {
            return;
        });
        int photoHolderW = photoHolder.getWidth(), photoHolderH = photoHolder.getHeight();
        progressView.setLayoutParams(new RelativeLayout.LayoutParams(photoHolderW, photoHolderH));
        MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if(!(DPFile == null)){
            Uri uris = Uri.fromFile(DPFile);
            String fileExt = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.toLowerCase());
            multipartBody.addFormDataPart("files[DPFile]", DPFile.getName(), RequestBody.create(DPFile, MediaType.parse(mimeType)));
            uris = Uri.fromFile(DPFileCrop);
            fileExt = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.toLowerCase());
            multipartBody.addFormDataPart("files[DPFileCrop]", DPFileCrop.getName(), RequestBody.create(DPFileCrop, MediaType.parse(mimeType)));
        }
        if(!(CPFile == null)){
            Uri uris = Uri.fromFile(CPFile);
            String fileExt = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.toLowerCase());
            multipartBody.addFormDataPart("files[CPFile]", CPFile.getName(), RequestBody.create(CPFile, MediaType.parse(mimeType)));
            uris = Uri.fromFile(CPFileCrop);
            fileExt = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.toLowerCase());
            multipartBody.addFormDataPart("files[CPFileCrop]", CPFileCrop.getName(), RequestBody.create(CPFileCrop, MediaType.parse(mimeType)));
        }
        multipartBody.addFormDataPart("id", pageIDtoString)
                .addFormDataPart("table", "pages");

        @SuppressLint("SetTextI18n") final CountingRequestBody.Listener progressListener = (bytesRead, contentLength) -> {
            if (bytesRead < contentLength && contentLength > 0) {
                final int progress = (int)Math.round((((double) bytesRead / contentLength) * 100));
                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        progressBar.setProgress(progress, true);
                    else
                        progressBar.setProgress(progress);
                    progressText.setText(progress+"%");
                });
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    Request originalRequest = chain.request();

                    if (originalRequest.body() == null) {
                        return chain.proceed(originalRequest);
                    }
                    Request progressRequest = originalRequest.newBuilder()
                            .method(originalRequest.method(),
                                    new CountingRequestBody(originalRequest.body(), progressListener))
                            .build();

                    return chain.proceed(progressRequest);

                })
                .build();
        RequestBody requestBody = multipartBody.build();
        Request request = new Request.Builder()
                .url(Constants.changePhotoUrl)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //onError
                //Log.e("failure Response", mMessage);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                uploading = false;
                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        progressBar.setProgress(100, true);
                    else
                        progressBar.setProgress(100);
                    progressText.setText("100%");
                    progressView.setVisibility(View.GONE);
                });
                if(!(DPFileCrop == null)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(DPFileCrop.toPath());
                    else
                        DPFileCrop.delete();
                }
                if(!(CPFileCrop == null)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(CPFileCrop.toPath());
                    else
                        CPFileCrop.delete();
                }
                if(!(DPFile == null)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(DPFile.toPath());
                    else
                        DPFile.delete();
                }
                if(!(CPFile == null)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(CPFile.toPath());
                    else
                        CPFile.delete();
                }
                DPFileCrop = null;
                CPFileCrop = null;
                CPFile = null;
                DPFile = null;
                file = null;
                selectedFile = null;
            }
        });
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void discardPhotoChange() {
        View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0) blackFade.removeAllViews();
        TextView txter = blockView.findViewById(R.id.txter);
        Button cnclBtn = blockView.findViewById(R.id.cancel);
        Button agreeBtn = blockView.findViewById(R.id.agree);
        txter.setText("Are you sure you want to discard this ?");
        agreeBtn.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            try {
                if(!(DPFileCrop == null)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Files.delete(DPFileCrop.toPath());
                    }
                    else
                        DPFileCrop.delete();
                }
                if(!(CPFileCrop == null)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(CPFileCrop.toPath());
                    else
                        CPFileCrop.delete();
                }
                if(!(DPFile == null)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(DPFile.toPath());
                    else
                        DPFile.delete();
                }
                if(!(CPFile == null)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(CPFile.toPath());
                    else
                        CPFile.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            CPFileCrop = null;
            DPFileCrop = null;
            CPFile = null;
            DPFile = null;
            file = null;
            selectedFile = null;
            imageLoader.displayImage(photo, prfPht);
            imageLoader.displayImage(coverPhoto, covPht);
            holder.setVisibility(View.VISIBLE);
            saverL.setVisibility(View.GONE);
        });
        cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
        blackFade.addView(blockView);
        blackFade.setVisibility(View.VISIBLE);
    }

    public static void handleSelectedImage(File image, int i){
        PageActivity pageActivity = new PageActivity();
        pageActivity.selectedFile = image;
        pageActivity.index = i;
        @SuppressLint("SimpleDateFormat") String filePath,
                timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()),
                randStr = UUID.randomUUID().toString();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            RandomString gen = new RandomString(8, ThreadLocalRandom.current());
            randStr = gen.toString();
        }
        Uri fileUri = Uri.fromFile(image);
        filePath = tempDir + "/IMG_" + randStr + timeStamp +".jpg";
        pageActivity.file = new File(filePath);
        UCrop uCrop = UCrop.of(fileUri, Uri.fromFile(pageActivity.file));
        if(pageActivity.index == 0){
            uCrop.withAspectRatio(1, 1);
        }
        if(pageActivity.index == 1){
            uCrop.withAspectRatio(20, 9);
        }
        uCrop.start(activity);
    }

    public static void changePageInfo(String name, int pageType, int pageCat){
        PageActivity pageActivity = new PageActivity();
        pageActivity.pageName = name;
        if(pageType == 5)
            pageActivity.category = "Community";
        else
            pageActivity.category = pageActivity.pageTypes[pageType][pageCat];
        pageActivity.textName.setText(pageActivity.pageName);
        pageActivity.userName.setText(pageActivity.category);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UCrop.REQUEST_CROP && !(data == null))
            handleCropper(data);
    }

    private void handleCropper(Intent data) {
        saverL.setVisibility(View.VISIBLE);
        holder.setVisibility(View.GONE);
        Uri resultUri = UCrop.getOutput(data);
        if(index == 0) {
            prfPht.setImageURI(resultUri);
            DPFileCrop = file;
            DPFile = selectedFile;
        }
        if(index == 1) {
            covPht.setImageURI(resultUri);
            CPFileCrop = file;
            CPFile = selectedFile;
        }
    }

    private void openImageSelector(int i) {
        Intent intent = new Intent(cntxt, ImageSelectorActivity.class);
        Bundle params = new Bundle();
        params.putInt("index", i);
        params.putInt("activityReq", 1);
        intent.putExtras(params);
        startActivity(intent);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void loadPageContent(){
        try {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("myId", myIDtoString)
                    .addFormDataPart("pageId", pageIDtoString)
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(Constants.pageUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseString = Objects.requireNonNull(response.body()).string();
                progressBar.setVisibility(View.GONE);
                holder.setVisibility(View.VISIBLE);
                JSONObject obj = new JSONObject(responseString);
                pageName = obj.getString("name");
                user = obj.getInt("user");
                photo = www + obj.getString("photo");
                coverPhoto = www + obj.getString("coverPhoto");
                followingPage = obj.getBoolean("followingPage");
                verified = obj.getBoolean("verified");
                int pageType = obj.getInt("pageType");
                int pageCat = obj.getInt("pageCat");
                int fllwersNum = obj.getInt("fllwersNum");
                String fllwNumStr = Functions.convertToText(fllwersNum);
                JSONArray fllwersArray = obj.getJSONArray("fllwersArray");
                String follower = " - Follower";
                if(fllwersNum > 1)
                    follower += "s";
                else
                    fllwNumStr = String.valueOf(fllwersNum);
                if(pageType == 5)
                    category = "Community";
                else
                    category = pageTypes[pageType][pageCat];
                imageLoader.displayImage(photo, prfPht);
                imageLoader.displayImage(coverPhoto, covPht);
                textName.setText(pageName);
                userName.setText(category);
                if(verified)
                    textName.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                if(myId == user){
                    changeCp.setVisibility(View.VISIBLE);
                    changeDp.setVisibility(View.VISIBLE);
                    editPage.setVisibility(View.VISIBLE);
                    changeDp.setOnClickListener(v -> {
                        if(!uploading)
                            openImageSelector(0);
                    });
                    changeCp.setOnClickListener(v -> {
                        if(!uploading)
                            openImageSelector(1);
                    });
                    changeDpBtn.setOnClickListener(v -> {
                        if(!uploading)
                            openImageSelector(0);
                    });
                    changeCpBtn.setOnClickListener(v -> {
                        if(!uploading)
                            openImageSelector(1);
                    });
                    saveChange.setOnClickListener(v -> savePhotoChange());
                    discardChange.setOnClickListener(v -> discardPhotoChange());
                    editPage.setOnClickListener(v -> openPageEditor());
                } else {
                    setFollow(followingPage);
                    follow.setVisibility(View.VISIBLE);
                    follow.setOnClickListener(v -> followPage(false));
                }
                LinearLayout layoutFllwer = (LinearLayout) getLayoutInflater().inflate(R.layout.list_header, null);
                TextView headerFllwer =  layoutFllwer.findViewById(R.id.header);
                TextView seeAllFllwer =  layoutFllwer.findViewById(R.id.seeAll);
                headerFllwer.setText(fllwNumStr + follower);
                if (fllwersNum < 21)
                    seeAllFllwer.setVisibility(View.GONE);
                else {
                    seeAllFllwer.setOnClickListener(v -> openLister());
                }
                boxer.addView(layoutFllwer);
                if (fllwersNum > 0)
                    setLister(fllwersArray);
                initializeViewPager();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void followPage(boolean free) {
        boolean tag = Boolean.parseBoolean(follow.getTag().toString());
        boolean newTag = !tag;
        if(tag){
            if(!free) {
                String text = "Are You Sure You Want To Unfollow " + pageName;
                displayConfirm(text);
                return;
            }
        }
        JSONObject emitObj = new JSONObject();
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            emitObj.put("user", myId);
            emitObj.put("pageId", pageId);
            emitObj.put("val", tag);
            emitObj.put("date", date);
            socket.emit("followPage", emitObj);
            followingPage = newTag;
            setFollow(newTag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    private void setFollow(boolean newTag) {
        follow.setTag(newTag);
        if(newTag) {
            follow.setText("Following");
            follow.setBackgroundResource(R.color.colorPrimaryRed);
            follow.setTextColor(ContextCompat.getColor(cntxt, R.color.white));
            follow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_followers_white, 0, 0, 0);
        } else {
            boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
            int color;
            if(darkThemeEnabled)
                color = R.color.ash;
            else
                color = R.color.blash;
            follow.setText("Follow");
            follow.setBackgroundResource(R.drawable.new_border);
            follow.setTextColor(ContextCompat.getColor(cntxt, color));
            follow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_followers, 0, 0, 0);
        }
    }

    @SuppressLint("InflateParams")
    private void displayConfirm(String text){
        text += "?";
        View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        TextView txter = blockView.findViewById(R.id.txter);
        Button cnclBtn = blockView.findViewById(R.id.cancel);
        Button agreeBtn = blockView.findViewById(R.id.agree);
        txter.setText(text);
        agreeBtn.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            followPage(true);
        });
        cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
        blackFade.addView(blockView);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void openLister() {
        Intent intent = new Intent(cntxt, FFFAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("user", pageId);
        userParams.putString("tab", "pageFollowers");
        intent.putExtras(userParams);
        startActivity(intent);
    }

    @SuppressLint("InflateParams")
    private void setLister(JSONArray jsonArray) throws JSONException {
        LinearLayout layoutFX = (LinearLayout) getLayoutInflater().inflate(R.layout.horz_scll_view, null);
        LinearLayout listLayout =  layoutFX.findViewById(R.id.listLayout);
        for (int i = 0; i < jsonArray.length(); i++){
            JSONObject object = jsonArray.getJSONObject(i);
            int userFX = object.getInt("user");
            String nameFX = object.getString("name");
            String photoFX = www + object.getString("photo");
            LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.list_box, null);
            LinearLayout holder =  layout.findViewById(R.id.holder);
            ImageView imageView =  layout.findViewById(R.id.photo);
            TextView textView =  layout.findViewById(R.id.name);
            imageLoader.displayImage(photoFX, imageView);
            textView.setText(nameFX);
            holder.setOnClickListener(v -> visitUserProfile(cntxt, userFX));
            listLayout.addView(layout);
        }
        boxer.addView(layoutFX);
    }

    private void initializeViewPager() {
        tabLayout.setupWithViewPager(viewPager);
        setupViewPager();
    }

    private void setupViewPager() {
        ArrayList<ContextData> contextDataArrayList = new ArrayList<>();
        ContextData contextData = new ContextData();
        contextData.context = cntxt;
        contextData.dataId = pageId;
        contextData.height = height;
        contextData.width = width;
        contextData.mainHomeView = mainHomeView;
        contextData.blackFade = blackFade;
        contextData.comScrllView = comScrllView;
        contextData.comMentionLayer = comMentionLayer;
        contextData.viewHolder = viewHolder;
        contextData.nest = nest;
        contextData.aBoolean = aBoolean;
        contextData.loadedPhotos = loadedPhotos;
        contextData.loadedVideos = loadedVideos;
        contextData.socket = socket;
        contextData.pageName = pageName;
        contextData.photo = photo;
        contextData.pageCount = pageCount;
        contextDataArrayList.add(contextData);
        ContextData newContextData = contextDataArrayList.get(0);
        contextDataArrayList.remove(0);
        viewPager.setOffscreenPageLimit(3);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        pageHomeFragment = new PageHomeFragment(newContextData);
        pagePhotosFragment = new PagePhotosFragment(newContextData);
        pageVideosFragment = new PageVideosFragment(newContextData);
        aboutPageFragment = new AboutPageFragment(newContextData);
        adapter.addFragment(pageHomeFragment, "POSTS");
        adapter.addFragment(pagePhotosFragment, "PHOTOS");
        adapter.addFragment(pageVideosFragment, "VIDEOS");
        adapter.addFragment(aboutPageFragment, "ABOUT");
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 1 && !loadedPhotos){
                    pagePhotosFragment.getFileDisplay();
                }
                if(position == 2 && !loadedVideos){
                    pageVideosFragment.getFileDisplay();
                }
                if(position == 3 && !loadedInfo){
                    aboutPageFragment.loadInfo();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public static void openLink(Context cntxt, String linkUrl) {
        Uri uri = Uri.parse(linkUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        cntxt.startActivity(intent);
    }

    public static void callNumber(Context cntxt, String phoneNum) {
        String s = "tel:" + phoneNum.trim();
        Uri uri = Uri.parse(s);
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(uri);
        cntxt.startActivity(intent);
    }

    public static void openEmail(Context cntxt, String emailAddr) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddr});
        cntxt.startActivity(Intent.createChooser(intent, ""));
    }

    private static void visitUserProfile(Context cntxt, int user) {
        Intent intent = new Intent(cntxt, ProfileAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("userID", user);
        intent.putExtras(userParams);
        cntxt.startActivity(intent);
    }

    public void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
        } else if(viewPager.getCurrentItem() > 0){
            viewPager.setCurrentItem(0, true);
        } else {
            StaticSaver.removeObject(pageCount);
            StaticSaver.removeSocket(pageCount);
            socket.emit("disconnected", myId);
            socket.emit("removePagePage", sockt);
            finish();
        }
    }

    public void hideSoftKeyboard(Context cntxt, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)cntxt.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(Context cntxt, View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        view.setOnTouchListener((v, event) -> {
            if (!(view instanceof EditText) && !(view == comScrllView)) {
                comScrllView.setVisibility(View.GONE);
                hideSoftKeyboard(cntxt,v);
            }
            return false;
        });

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(cntxt, innerView);
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
        if(pageLoaded) {
            HomeFragment.postLayouts = StaticSaver.getObject(pageCount);
            socket = StaticSaver.getSocket(pageCount);
            HomeFragment.socket = socket;
        }
        pageLoaded = true;
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    /*public static int getDPsFromPixels(Context context, int pixels){
        Resources r = context.getResources();
        int  dps = Math.round(pixels/(r.getDisplayMetrics().densityDpi/160f));
        return dps;
    }*/
}
