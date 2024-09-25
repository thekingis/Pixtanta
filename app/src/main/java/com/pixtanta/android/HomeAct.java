package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pixtanta.android.Adapter.ViewPagerAdapter;
import com.pixtanta.android.Utils.BlurBitmap;
import com.pixtanta.android.Utils.HtmlParser;
import com.pixtanta.android.Utils.JsonObject;
import com.pixtanta.android.Utils.StringUtils;
import com.vdurmont.emoji.EmojiParser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

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

import static com.pixtanta.android.Constants.socketUrl;
import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.Functions.convertToTextPlus;

public class HomeAct extends ThemeActivity {

    LinearLayout layout, comMentionLayer, topBar, loadingLayer, postView;
    @SuppressLint("StaticFieldLeak")
    static PhotoAdapter photoAdapter;
    static ArrayList<String> postFilesLists;
    Context cntxt;
    String searchText, prevHtmlTxt;
    public static ArrayList<File> itemLists;
    public static ArrayList<String> selectedImages, editedImages;
    ArrayList<String> selectedPosts = new ArrayList<>();
    ImageView profPic;
    ProgressBar progressBar;
    RelativeLayout mainHomeView;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout blackFade;
    TextView ups, msgNumTextView, noteNumTextView, searchRslt;
    FileCache fileCache;
    CardView viewPrf;
    static CardView msgCV, noteCV;
    public ProgressBar progressBarNt;
    public static TabLayout tabLayout;
    public static ViewPager curViewPager, noteViewPager;
    ImageView writePost;
    EditText search, postTxt, curEditor;
    ImageLoader imageLoader;
    ResizedImageLoader resizedImageLoader;
    ImageAdaptor imageAdapter;
    LinearLayout writeLayout, curLayout, msgBtn, noteBtn;
    LinearLayout menu, menuLayout, menuDis, noteLayout, searchLayout, searchDis, logoutLayer, mentionLayer;
    ScrollView postScrllVw, scrllView, comScrllView;
    Button postBtn;
    static int[] numberOfImages;
    int[][] reactions;
    int width, height, viewPagerHeight, selStart, selEnd, charLn, strLn, verifiedIcon, count = 0, vpc = 0, mCount = 0;
    static int noteCount;
    static int imgW, msgCount;
    private static int myId;
    public static boolean noteLoaded;
    boolean hashed, firstLoad, btwnHtml, afterAnchor, addingTxt, changingTxt, scrllVIState, menuLoaded, uploadingPost, loadingPost, allLoaded, userVerified, shakeOpt;
    static boolean hasPage, pageOver;
    public static String lang, myName, myPht, myUserName, poster, postType, posterImg, posterName, posterUName, myIDtoString;
    DisplayMetrics displayMetrics;
    @SuppressLint("StaticFieldLeak")
    static GridView gridView;
    public static JSONArray userPages;
    static JSONObject postLayouts;
    JsonObject searchData = new JsonObject();
    JSONObject drawableLefts = new JSONObject(), reactorView = new JSONObject(), commentsLayoutObj = new JSONObject(), postsOptions = new JSONObject(), postsOptionsVal = new JSONObject(), postsOptionsIcons, likesDisplay = new JSONObject(), comsDisplay = new JSONObject(), miniComDisplay = new JSONObject(), onPagerScrolled = new JSONObject(), viewPagers = new JSONObject(), posters = new JSONObject();
    ArrayList<LinearLayout> miniComLayouts = new ArrayList<>();
    public static Socket socket;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    SharedPrefMngr sharedPrefMngr;
    public static Handler UIHandler;
    static {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        sharedPrefMngr = new SharedPrefMngr(this);

        sharedPrefMngr.initializeSmartLogin();
        lang = sharedPrefMngr.getSelectedLanguage();
        cntxt = this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        fileCache = new FileCache(this);
        verifiedIcon = R.drawable.ic_verified_user;

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(HomeAct.this, LoginAct.class));
            return;
        }
        myId = sharedPrefMngr.getMyId();

        scrllVIState = false;
        hashed = false;
        firstLoad = true;
        menuLoaded = false;
        uploadingPost = false;
        loadingPost = true;
        allLoaded = false;
        noteLoaded = false;
        changingTxt = false;
        addingTxt = false;

        try {
            socket = IO.socket(socketUrl);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> socket.emit("connected", myId)));
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(() -> {
            shakeOpt = sharedPrefMngr.checkShakeOption();
            if(shakeOpt)
                openReportDialog();
        });

        updateMessageDelivery(myId);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        int widthPadding = width - 5;
        imgW = (widthPadding/3) - 10;
        viewPagerHeight = width;
        itemLists = new ArrayList<>();
        selectedImages = new ArrayList<>();
        editedImages = new ArrayList<>();
        hasPage = false;
        pageOver = false;
        userPages = new JSONArray();
        postLayouts = new JSONObject();

        postsOptionsIcons = new JSONObject();
        try {
            postsOptionsIcons.put("savePost", R.drawable.ic_save_post);
            postsOptionsIcons.put("editPost", R.drawable.ic_edit_post);
            postsOptionsIcons.put("deletePost", R.drawable.ic_delete_post);
            postsOptionsIcons.put("unfollowPers", R.drawable.ic_unfollow);
            postsOptionsIcons.put("unfollowPage", R.drawable.ic_unfollow);
            postsOptionsIcons.put("notifyStatus", R.drawable.ic_notify);
            postsOptionsIcons.put("copyLink", R.drawable.ic_copy_link);
            postsOptionsIcons.put("hidePost", R.drawable.ic_hide_post);
            postsOptionsIcons.put("reportPost", R.drawable.ic_report_post);

            drawableLefts.put("actLog", R.drawable.ic_activity);
            drawableLefts.put("tags", R.drawable.ic_tag);
            drawableLefts.put("savedPosts", R.drawable.ic_saved);
            drawableLefts.put("managePages", R.drawable.ic_man_page);
            drawableLefts.put("createPage", R.drawable.ic_add_page);
            drawableLefts.put("settings", R.drawable.ic_settings);
            drawableLefts.put("hiddenPosts", R.drawable.ic_cancel);
            drawableLefts.put("deletedPosts", R.drawable.ic_delete);
            drawableLefts.put("theme", R.drawable.ic_theme);
            drawableLefts.put("languages", R.drawable.ic_language);
            drawableLefts.put("clearCache", R.drawable.ic_clear);
            drawableLefts.put("tac", R.drawable.ic_tac);
            drawableLefts.put("reportProblem", R.drawable.ic_report_problem);
            drawableLefts.put("logout", R.drawable.ic_logout);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        reactions = new int[][]{
                new int[]{1, R.drawable.ic_loved},
                new int[]{0, R.drawable.ic_love}
        };

        numberOfImages = new int[] {
                R.drawable.ic_imgs_plus,
                R.drawable.ic_imgs_1,
                R.drawable.ic_imgs_2,
                R.drawable.ic_imgs_3,
                R.drawable.ic_imgs_4,
                R.drawable.ic_imgs_5,
                R.drawable.ic_imgs_6,
                R.drawable.ic_imgs_7,
                R.drawable.ic_imgs_8,
                R.drawable.ic_imgs_9
        };

        myPht = www + sharedPrefMngr.getMyPht();
        myName = sharedPrefMngr.getMyName();
        myUserName = sharedPrefMngr.getMyUserName();
        userVerified = sharedPrefMngr.getMyVerification();
        sharedPrefMngr.storeUserVerification(myId, userVerified);
        myIDtoString = Integer.toString(myId);
        imageLoader = new ImageLoader(this);
        resizedImageLoader = new ResizedImageLoader(this);
        mainHomeView = findViewById(R.id.mainHomeView);
        postView = findViewById(R.id.postView);
        loadingLayer = findViewById(R.id.loadingLayer);
        gridView = findViewById(R.id.gridView);
        postScrllVw = findViewById(R.id.postScrllVw);
        scrllView = findViewById(R.id.scrllView);
        comScrllView = findViewById(R.id.comScrllView);
        search = findViewById(R.id.search);
        topBar = findViewById(R.id.topBar);
        blackFade = findViewById(R.id.blackFade);
        mentionLayer = findViewById(R.id.mentionLayer);
        searchLayout = findViewById(R.id.searchLayout);
        searchDis = findViewById(R.id.searchDis);
        writeLayout = findViewById(R.id.writeLayout);
        menuLayout = findViewById(R.id.menuLayout);
        menuDis = findViewById(R.id.menuDis);
        noteLayout = findViewById(R.id.noteLayout);
        logoutLayer = findViewById(R.id.logoutLayer);
        layout = findViewById(R.id.layout);
        noteViewPager = findViewById(R.id.noteViewPager);
        tabLayout = findViewById(R.id.tabLayout);
        profPic = findViewById(R.id.profPic);
        progressBarNt = findViewById(R.id.progressBarNt);
        progressBar = findViewById(R.id.progressBar);
        menu = findViewById(R.id.menu);
        comMentionLayer = findViewById(R.id.comMentionLayer);
        viewPrf = findViewById(R.id.viewPrf);
        noteCV = findViewById(R.id.noteCV);
        msgCV = findViewById(R.id.msgCV);
        writePost = findViewById(R.id.writePost);
        msgBtn = findViewById(R.id.msgBtn);
        noteBtn = findViewById(R.id.noteBtn);
        postBtn = findViewById(R.id.postBtn);
        postTxt = findViewById(R.id.postTxt);
        ups = findViewById(R.id.ups);
        msgNumTextView = findViewById(R.id.msgNumTextView);
        noteNumTextView = findViewById(R.id.noteNumTextView);
        searchRslt = findViewById(R.id.searchRslt);
        setupUI(mainHomeView);
        tabLayout.setupWithViewPager(noteViewPager);
        msgCV.setCardBackgroundColor(getResources().getColor(R.color.red));
        noteCV.setCardBackgroundColor(getResources().getColor(R.color.red));

        imageLoader.displayImage(myPht, profPic);

        search.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus && (!(curLayout == searchLayout) || !(searchLayout.getVisibility() == View.VISIBLE))){
                searchLayout.setVisibility(View.VISIBLE);
                pageOver = true;
                if(!(curLayout == null))
                    curLayout.setVisibility(View.GONE);
                curLayout = searchLayout;
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(search);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        search.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_UP && keyCode == 66){
                searchText = search.getText().toString();
                if(!StringUtils.isEmpty(searchText)){
                    try {
                        JSONObject object = new JSONObject();
                        object.put("dataId", 0);
                        object.put("name", searchText);
                        object.put("txt", searchText);
                        object.put("type", "search");
                        object.put("userName", "");
                        object.put("photo", "");
                        object.put("verified", false);
                        object.put("isFrnd", false);
                        saveSearch(object);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        });
        viewPrf.setOnClickListener(v -> {
            if(!(curLayout == null))
                curLayout.setVisibility(View.GONE);
            curLayout = null;
            pageOver = false;
            visitUserProfile(cntxt, myId);
        });
        menu.setOnClickListener(v -> {
            if(!(curLayout == menuLayout)){
                menuLayout.setVisibility(View.VISIBLE);
                pageOver = true;
                if(!(curLayout == null))
                    curLayout.setVisibility(View.GONE);
                curLayout = menuLayout;
                if(!menuLoaded){
                    loadMenuContents();
                }
            }
        });
        writePost.setOnClickListener(v -> {
            if(!(curLayout == writeLayout) && !uploadingPost){
                writeLayout.setVisibility(View.VISIBLE);
                pageOver = true;
                if(!(curLayout == null))
                    curLayout.setVisibility(View.GONE);
                curLayout = writeLayout;
            }
        });
        msgBtn.setOnClickListener(v -> {
            if(!(curLayout == null)) {
                curLayout.setVisibility(View.GONE);
                curLayout = null;
                pageOver = false;
            }
            startActivity(new Intent(cntxt, InboxAct.class));
        });
        noteBtn.setOnClickListener(v -> {
            if(!(curLayout == noteLayout)){
                noteLayout.setVisibility(View.VISIBLE);
                pageOver = true;
                if(!(curLayout == null))
                    curLayout.setVisibility(View.GONE);
                curLayout = noteLayout;
                if(!noteLoaded)
                    setNoteViewPager();
                else {
                    noteCount = 0;
                    noteCV.setVisibility(View.INVISIBLE);
                    emitPopper("notifications");
                }
            }
        });
        ups.setOnClickListener(v -> {
            Intent intent = new Intent(cntxt, PhotoGridAct.class);
            Bundle fileParams = new Bundle();
            fileParams.putInt("act", 0);
            intent.putExtras(fileParams);
            startActivity(intent);
        });
        postBtn.setOnClickListener(v -> {
            String writeUp = postTxt.getText().toString();
            if(StringUtils.isEmpty(writeUp) && itemLists.size() == 0){
                @SuppressLint("InflateParams") View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0){
                    for (int t = 0; t < blackFade.getChildCount(); t++){
                        blackFade.getChildAt(t).setVisibility(View.GONE);
                    }
                }
                TextView txter = blockView.findViewById(R.id.txter);
                Button cnclBtn = blockView.findViewById(R.id.cancel);
                Button agreeBtn = blockView.findViewById(R.id.agree);
                txter.setText("Please write a post or upload a photo or video");
                agreeBtn.setText("OK");
                cnclBtn.setVisibility(View.GONE);
                agreeBtn.setOnClickListener(v1 -> {
                    pageOver = false;
                    curLayout = null;
                    blackFade.setVisibility(View.GONE);
                });
                blackFade.addView(blockView);
                blackFade.setVisibility(View.VISIBLE);
                pageOver = true;
                curLayout = blackFade;
            } else {
                uploadPost(userVerified);
            }
        });
        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });
        searchRslt.setOnClickListener(v -> {
            if(!StringUtils.isEmpty(searchText)){
                try {
                    JSONObject object = new JSONObject();
                    object.put("dataId", 0);
                    object.put("name", searchText);
                    object.put("txt", searchText);
                    object.put("type", "search");
                    object.put("userName", "");
                    object.put("photo", "");
                    object.put("verified", false);
                    object.put("isFrnd", false);
                    saveSearch(object);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        initializeEditText(postTxt, scrllView, false, "0");
        comScrllView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if(!(curEditor == null)){
                int[] location = new int[2];
                curEditor.getLocationOnScreen(location);
                int offsetTop = location[1] - 100;
                int scrllViewHeight = comScrllView.getHeight();
                if(offsetTop < scrllViewHeight)
                    offsetTop += 170;
                else
                    offsetTop -= scrllViewHeight - 30;
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) comScrllView.getLayoutParams();
                layoutParams.setMargins(0, offsetTop, 0, 0);
                comScrllView.setLayoutParams(layoutParams);
                if(comScrllView.getVisibility() == View.VISIBLE && !scrllVIState)
                    comScrllView.scrollTo(0, 0);
                scrllVIState = comScrllView.getVisibility() == View.VISIBLE;
            }
        });

        new android.os.Handler().postDelayed(this::getPostDisplay, 1000);
        postScrllVw.setSmoothScrollingEnabled(true);
        postScrllVw.setOnScrollChangeListener((scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int scrollH = layout.getHeight() - scrllVw.getHeight() - height;
                if(scrollY > scrollH && !loadingPost && !allLoaded){
                    loadingPost = true;
                    loadingLayer = (LinearLayout) getLayoutInflater().inflate(R.layout.loading_layer, null, false);
                    layout.addView(loadingLayer);
                    ViewTreeObserver viewTreeObserver = loadingLayer.getViewTreeObserver();
                    viewTreeObserver.addOnGlobalLayoutListener(this::getPostDisplay);
                }
            });

        socket.on("postLike", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postId = String.valueOf(argsArr.getInt(0));
                int likes = argsArr.getInt(1);
                if(!(likesDisplay.isNull(postId))){
                    TextView textView = (TextView) likesDisplay.get(postId);
                    TextView likesTextView = (TextView) reactorView.get(postId);
                    String likeNumStr = textView.getText().toString();
                    int likeNum = Functions.convertToNumber(likeNumStr);
                    likeNum += likes;
                    int finalLikeNum = likeNum;
                    int finalLikeNum1 = likeNum;
                    runOnUiThread(() -> {
                        textView.setText(Functions.convertToText(finalLikeNum1));
                        if(finalLikeNum > 0){
                            likesTextView.setVisibility(View.VISIBLE);
                        } else {
                            likesTextView.setVisibility(View.GONE);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("postLikeX", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postId = String.valueOf(argsArr.getInt(0));
                int user = argsArr.getInt(2);
                int tag = argsArr.getInt(3);
                if(!(postLayouts.isNull(postId)) && user == myId){
                    RelativeLayout relativeLayout = (RelativeLayout) postLayouts.get(postId);
                    int newTag = reactions[tag][0];
                    int newReact = reactions[tag][1];
                    ImageButton imageButton = relativeLayout.findViewById(R.id.likePost);
                    runOnUiThread(() -> {
                        imageButton.setTag(newTag);
                        imageButton.setBackgroundResource(newReact);
                    });
                    if(!(likesDisplay.isNull(postId))){
                        TextView textView = (TextView) likesDisplay.get(postId);
                        TextView likesTextView = (TextView) reactorView.get(postId);
                        String likeNumStr = textView.getText().toString();
                        int likeNum = Functions.convertToNumber(likeNumStr);
                        int likes = argsArr.getInt(1);
                        likeNum += likes;
                        int finalLikeNum = likeNum;
                        int finalLikeNum1 = likeNum;
                        runOnUiThread(() -> {
                            textView.setText(Functions.convertToText(finalLikeNum1));
                            if(finalLikeNum > 0){
                                likesTextView.setVisibility(View.VISIBLE);
                            } else {
                                likesTextView.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("reversePostLike", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postId = String.valueOf(argsArr.getInt(0));
                if(!(postLayouts.isNull(postId))){
                    boolean subtract = argsArr.getBoolean(1);
                    RelativeLayout relativeLayout = (RelativeLayout) postLayouts.get(postId);
                    ImageButton imageButton = relativeLayout.findViewById(R.id.likePost);
                    TextView textView = (TextView) likesDisplay.get(postId);
                    TextView likesTextView = (TextView) reactorView.get(postId);
                    String likeNumStr = textView.getText().toString();
                    int likeNum = Functions.convertToNumber(likeNumStr);
                    if(subtract)
                        likeNum -= 1;
                    int finalLikeNum = likeNum;
                    runOnUI(() -> {
                        imageButton.setTag(0);
                        imageButton.setBackgroundResource(R.drawable.ic_love);
                        textView.setText(Functions.convertToText(finalLikeNum));
                        if(finalLikeNum > 0)
                            likesTextView.setVisibility(View.VISIBLE);
                        else
                            likesTextView.setVisibility(View.GONE);
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("reverseSubmitComment", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postId = String.valueOf(argsArr.getInt(0));
                int counter = argsArr.getInt(1);
                if(!(counter == -1) && !(miniComLayouts.get(counter) == null)) {
                    RelativeLayout postLayout = (RelativeLayout) postLayouts.get(postId);
                    LinearLayout commentsLayout = postLayout.findViewById(R.id.commentsLayout);
                    LinearLayout miniComLayout = miniComLayouts.get(counter);
                    runOnUI(() -> commentsLayout.removeView(miniComLayout));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("editComment", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String comId = String.valueOf(argsArr.getInt(0));
                if(!(miniComDisplay.isNull(comId))){
                    LinearLayout linearLayout = (LinearLayout) miniComDisplay.get(comId);
                    TextView textView = linearLayout.findViewById(R.id.commentView);
                    String newCom = String.valueOf(argsArr.getString(1));
                    String comUserName = String.valueOf(argsArr.getString(2));
                    newCom = EmojiParser.parseToUnicode(newCom);
                    newCom = HtmlParser.parseBreaks(newCom);
                    String htmlText = "<font><b>@"+comUserName+"</b> "+newCom+"</font>";
                    runOnUiThread(() -> textView.setText(Html.fromHtml(htmlText)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("submitComment", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String commentID = String.valueOf(argsArr.getInt(0));
                String postId = String.valueOf(argsArr.getInt(1));
                if(!(comsDisplay.isNull(postId))){
                    String comment = argsArr.getString(8);
                    String userPht = argsArr.getString(5);
                    String userUName = argsArr.getString(6);
                    TextView textView = (TextView) comsDisplay.get(postId);
                    String likeNumStr = textView.getText().toString();
                    int likeNum = Functions.convertToNumber(likeNumStr);
                    likeNum++;

                    comment = EmojiParser.parseToUnicode(comment);
                    String miniCom = HtmlParser.parseBreaks(comment);
                    miniCom = EmojiParser.parseToUnicode(miniCom);
                    @SuppressLint("InflateParams") LinearLayout miniComLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.mini_comments, null);
                    ImageView imgView = miniComLayout.findViewById(R.id.profPic);
                    TextView commentTxtVw = miniComLayout.findViewById(R.id.commentView);
                    imageLoader.displayImage(userPht, imgView);
                    String htmlText = "<font><b>@"+userUName+"</b> "+miniCom+"</font>";
                    commentTxtVw.setText(Html.fromHtml(htmlText));
                    LinearLayout commentsLayout = (LinearLayout) commentsLayoutObj.get(postId);
                    miniComDisplay.put(commentID, miniComLayout);
                    int finalLikeNum = likeNum;
                    runOnUiThread(() -> {
                        commentsLayout.addView(miniComLayout);
                        textView.setText(Functions.convertToText(finalLikeNum));
                        miniComLayout.setOnClickListener(v -> openComments(postId, commentID));
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("submitCommentId", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String commentID = String.valueOf(argsArr.getInt(0));
                String postId = String.valueOf(argsArr.getInt(1));
                int counter = argsArr.getInt(2);
                if(!(counter == -1) && !(miniComLayouts.get(counter) == null)) {
                    LinearLayout miniComLayout = miniComLayouts.get(counter);
                    try {
                        miniComDisplay.put(commentID, miniComLayout);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    miniComLayout.setOnClickListener(v -> openComments(postId, commentID));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("deleteComment", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postId = String.valueOf(argsArr.getInt(1));
                if(!(comsDisplay.isNull(postId))){
                    TextView textView = (TextView) comsDisplay.get(postId);
                    String likeNumStr = textView.getText().toString();
                    int likeNum = Functions.convertToNumber(likeNumStr);
                    likeNum--;
                    String comId = String.valueOf(argsArr.getInt(0));
                    int finalLikeNum = likeNum;
                    runOnUiThread(() -> {
                        textView.setText(Functions.convertToText(finalLikeNum));
                        try {
                            LinearLayout linearLayout = (LinearLayout) miniComDisplay.get(comId);
                            linearLayout.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("hidePost", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postId = String.valueOf(argsArr.getInt(0));
                int userC = argsArr.getInt(1);
                if(!(postLayouts.isNull(postId)) && userC == myId){
                    RelativeLayout relativeLayout = (RelativeLayout) postLayouts.get(postId);
                    runOnUiThread(() -> relativeLayout.setVisibility(View.GONE));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("deletePost", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postId = String.valueOf(argsArr.getInt(0));
                if(!(postLayouts.isNull(postId))){
                    RelativeLayout relativeLayout = (RelativeLayout) postLayouts.get(postId);
                    runOnUiThread(() -> relativeLayout.setVisibility(View.GONE));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("submitMessage", args -> {
            try {
                JSONObject msgData = new JSONObject(args[0].toString());
                int userTo = msgData.getInt("userTo");
                if(userTo == myId) {
                    updateMessageDelivery(myId);
                    msgCount++;
                    String msgNumText = convertToTextPlus(msgCount);
                    runOnUI(() -> {
                        msgNumTextView.setText(msgNumText);
                        msgCV.setVisibility(View.VISIBLE);
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        socket.on("notify", args -> {
            if(!(curLayout == noteLayout)) {
                noteCount++;
                String noteNumText = convertToTextPlus(noteCount);
                runOnUI(() -> {
                    noteNumTextView.setText(noteNumText);
                    noteCV.setVisibility(View.VISIBLE);
                });
            }
        });
        socket.on("mentionList", args -> {
            try {
                JSONArray listArray = new JSONArray(args[0].toString());
                ScrollView curScrllView = scrllView;
                LinearLayout curLayerX = mentionLayer;
                if(!(curEditor == postTxt)){
                    curScrllView = comScrllView;
                    curLayerX = comMentionLayer;
                }
                final ScrollView curScrllVw = curScrllView;
                final LinearLayout curLayer = curLayerX;
                if(listArray.length() == 0){
                    runOnUiThread(() -> curScrllVw.setVisibility(View.GONE));
                    return;
                }
                runOnUiThread(() -> {
                    if(curLayer.getChildCount() > 0)
                        curLayer.removeAllViews();
                });
                for(int i = 0; i < listArray.length(); i++){
                    String data = listArray.getString(i);
                    JSONObject dataObj = new JSONObject(data);
                    int id = dataObj.getInt("id");
                    String name = dataObj.getString("fName");
                    String lName = dataObj.getString("lName");
                    String photo = www + dataObj.getString("photo");
                    String tab = dataObj.getString("tab");
                    boolean verified = Boolean.parseBoolean(dataObj.getString("verified"));
                    boolean commented = dataObj.getBoolean("commented");
                    boolean tUser = dataObj.getBoolean("tUser");
                    if(!StringUtils.isEmpty(lName))
                        name += " " + lName;
                    String lash = tab;
                    if(commented) {
                        lash = "Commented";
                        if(tab.equals("Page"))
                            lash += "-"+tab;
                    }
                    if(tUser)
                        lash = "Posted";
                    if(tab.equals("Friend") && lash.equals(tab) && id == myId)
                        lash = "You";
                    @SuppressLint("InflateParams") LinearLayout listView = (LinearLayout) getLayoutInflater().inflate(R.layout.show_users, null);
                    ImageView imageView = listView.findViewById(R.id.photo);
                    TextView nameTV = listView.findViewById(R.id.name);
                    TextView userNameTV = listView.findViewById(R.id.userName);
                    imageLoader.displayImage(photo, imageView);
                    nameTV.setText(name);
                    userNameTV.setText(lash);
                    if(verified)
                        nameTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user, 0);
                    String finalName = name;
                    listView.setOnClickListener(v -> addTextToView(curEditor, finalName, tab, id));
                    runOnUiThread(() -> curLayer.addView(listView));
                }
                runOnUiThread(() -> curScrllVw.setVisibility(View.VISIBLE));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
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

    private void performSearch(EditText editText) {
        searchRslt.setVisibility(View.INVISIBLE);
        searchText = editText.getText().toString();
        if(!StringUtils.isEmpty(searchText)){
            if(searchDis.getChildCount() > 0)
                searchDis.removeAllViews();
            @SuppressLint("InflateParams") ImageView listLoader = (ImageView) getLayoutInflater().inflate(R.layout.list_loader, null, false);
            listLoader.setMinimumWidth(width);
            searchDis.addView(listLoader);
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("category", "search")
                    .addFormDataPart("user", myIDtoString)
                    .addFormDataPart("searchText", searchText)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.searchUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()){
                if (response.isSuccessful()) {
                    searchDis.removeView(listLoader);
                    searchRslt.setVisibility(View.VISIBLE);
                    String searchData = Objects.requireNonNull(response.body()).string();
                    JSONObject jObject = new JSONObject(searchData);
                    JSONArray jsonArray = jObject.getJSONArray("data");
                    if(jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            showSearchHistory(jsonObject, i, false);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(searchData.length() > 0){
            if(searchDis.getChildCount() > 0)
                searchDis.removeAllViews();
            try {
                for (int i = 0; i < searchData.length(); i++){
                    JSONObject jsonObject = searchData.getJSONObject(Objects.requireNonNull(searchData.names()).getString(i));
                    showSearchHistory(jsonObject, i, false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveSearch(JSONObject object) throws JSONException {
        hideSoftKeyboard(mainHomeView);
        int dataId = object.getInt("dataId");
        String type = object.getString("type");
        String txt = object.getString("txt");
        if(type.equals("page"))
            visitPage(cntxt, dataId);
        else if(type.equals("profile"))
            visitUserProfile(cntxt, dataId);
        else
            visitSearch(cntxt, txt);
        search.setText("");
        search.clearFocus();
        JSONObject emitObj = new JSONObject();
        emitObj.put("user", myId);
        emitObj.put("category", "search");
        emitObj.put("type", type);
        emitObj.put("dataId", dataId);
        emitObj.put("txt", txt);
        emitObj.put("count", 20);
        socket.emit("saveSearchHistory", emitObj);
        boolean saved = showSearchHistory(object, 0, true);
        if(saved) {
            if(searchDis.getChildCount() > 0)
                searchDis.removeAllViews();
            try {
                for (int i = 0; i < searchData.length(); i++) {
                    JSONObject jsonObject = searchData.getJSONObject(Objects.requireNonNull(searchData.names()).getString(i));
                    showSearchHistory(jsonObject, i, false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean showSearchHistory(JSONObject object, int i, boolean save) throws JSONException {
        int dataId = object.getInt("dataId");
        if(save) {
            String type = object.getString("type");
            String key = type + "-" + dataId;
            searchData.prepend(key, object);
            return save;
        }
        boolean verified = object.getBoolean("verified");
        String name = object.getString("name");
        String userName = object.getString("userName");
        String photo = object.getString("photo");
        LinearLayout lLayout = searchDis.findViewWithTag(dataId);
        if(!(lLayout == null))
            searchDis.removeView(lLayout);
        @SuppressLint("InflateParams") LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.search_lists, null);
        LinearLayout layout = linearLayout.findViewById(R.id.layout);
        ImageView imageView = linearLayout.findViewById(R.id.photo);
        TextView textView = linearLayout.findViewById(R.id.name);
        TextView textViewUN = linearLayout.findViewById(R.id.userName);
        if(StringUtils.isEmpty(photo)){
            imageView.setVisibility(View.GONE);
            textView.setCompoundDrawablePadding(50);
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_history, 0, 0, 0);
            textView.setTypeface(null, Typeface.NORMAL);
            textView.setPadding(20, 0, 0, 0);
        } else
            imageLoader.displayImage(www + photo, imageView);
        textView.setText(name);
        if(!StringUtils.isEmpty(userName)){
            userName = "@"+userName;
            textViewUN.setVisibility(View.VISIBLE);
            textViewUN.setText(userName);
        }
        if(verified)
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
        linearLayout.setTag(dataId);
        layout.setOnClickListener(v -> {
            try {
                saveSearch(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        searchDis.addView(linearLayout, i);
        if(searchDis.getChildCount() > 20)
            searchDis.removeViewAt(20);
        return save;
    }

    public static void emitPopper(String emitter) {
        socket.emit(emitter, myId);
    }

    public static void popMessage() {
        msgCount = 0;
        msgCV.setVisibility(View.INVISIBLE);
        emitPopper("message");
    }

    private void setNoteViewPager() {
        noteViewPager.setOffscreenPageLimit(3);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        NotificationsFragment notificationsFragment = new NotificationsFragment(cntxt, progressBarNt);
        RequestsFragment requestsFragment = new RequestsFragment(cntxt);
        adapter.addFragment(notificationsFragment, "Notifications");
        adapter.addFragment(requestsFragment, "Requests");
        noteViewPager.setAdapter(adapter);
    }

    public static void changeTabTitle(String newTitle, int i){
        if(!(tabLayout.getTabAt(i) == null))
            Objects.requireNonNull(tabLayout.getTabAt(i)).setText(newTitle);
        else {
            new android.os.Handler().postDelayed(() -> changeTabTitle(newTitle, i), 100);
        }
    }

    private void overrideBackspace(EditText editText, int start, int end) throws JSONException {
        if(end == 0)
            return;
        changingTxt = true;
        Spanned newHtmlText;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            newHtmlText = Html.fromHtml(prevHtmlTxt, Html.FROM_HTML_MODE_COMPACT);
        else
            newHtmlText = Html.fromHtml(prevHtmlTxt);
        editText.setText(newHtmlText);
        if(start == end){
            String htmlText = Html.toHtml((Spanned) editText.getText().subSequence(start - 1, end));
            Document doc = Jsoup.parse(htmlText, "UTF-8");
            Elements anchorElement = doc.getElementsByTag("a");
            String href = anchorElement.attr("href");
            JSONArray preObj = textWalker(editText, start, href, false, 0, false);
            JSONArray proObj = textWalker(editText, start, href, true, 0, false);
            int preCnt = preObj.getInt(0), proCnt = proObj.getInt(0);
            boolean preSpace = preObj.getBoolean(1);
            if(preSpace)
                preCnt += 1;
            boolean endsWithBr = Functions.endsWithBr(Html.toHtml(editText.getText()));
            editText.setText(editText.getText().delete(start, start + proCnt));
            editText.setText(editText.getText().delete(start - preCnt, start));
            editText.setSelection(start - preCnt);
        } else {
            int[] ints;
            if(end - start == 1)
                ints = textWalker(editText, start, end);
            else
                ints = dispatchCaret(editText, start, end);
            int s = ints[0], e = ints[1];
            editText.setText(editText.getText().delete(start - s, end + e));
            if(end - start > 1){
                boolean sameHref, firstIsSpace, secondIsSpace;
                String href1 = null, href2 = null, spc1 = null, spc2 = null;
                if(!(start - s - 1 < 0)) {
                    String htmlText1 = Html.toHtml((Spanned) editText.getText().subSequence(start - s - 1, start - s));
                    Document doc1 = Jsoup.parse(htmlText1, "UTF-8");
                    Elements anchorElement1 = doc1.getElementsByTag("a");
                    href1 = anchorElement1.attr("href");
                    spc1 = anchorElement1.text();
                }
                if(!(start - s + 1 > editText.getText().length())) {
                    String htmlText2 = Html.toHtml((Spanned) editText.getText().subSequence(start - s, start - s + 1));
                    Document doc2 = Jsoup.parse(htmlText2, "UTF-8");
                    Elements anchorElement2 = doc2.getElementsByTag("a");
                    href2 = anchorElement2.attr("href");
                    spc2 = anchorElement2.text();
                }
                sameHref = (!(href1 == null) && !(href2 == null)) && href1.equals(href2);
                firstIsSpace = !(spc1 == null) && spc1.equals(" ");
                secondIsSpace = !(spc2 == null) && spc2.equals(" ");
                if(firstIsSpace && secondIsSpace){
                    editText.setText(editText.getText().delete(start - s - 1, start - s + 1));
                    if(sameHref){
                        Spanned htmlText;
                        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                        String bColor = "#F9F9F9";
                        if(darkThemeEnabled)
                            bColor = "#585858";
                        String str = " <a href=\""+href1+"\"><b><span style=\"background-color:"+bColor+";\"> </span></b></a>";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            htmlText = Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT);
                        else
                            htmlText = Html.fromHtml(str);
                        editText.getText().insert(start - s - 1, htmlText);
                    } else
                        editText.getText().insert(start - s - 1, " ");
                } else if(firstIsSpace && !sameHref){
                    editText.setText(editText.getText().delete(start - s - 1, start - s));
                    editText.getText().insert(start - s - 1, " ");
                } else if(secondIsSpace && !sameHref){
                    editText.setText(editText.getText().delete(start - s, start - s + 1));
                    if(start - s > 0)
                        editText.getText().insert(start - s, " ");
                }
            }
            editText.setSelection(start - s);
        }
        changingTxt = false;
    }

    private void removeLineBreaks(EditText editText, int caret, boolean endsWithBr){
        if(!endsWithBr) {
            String htmlText = Html.toHtml(editText.getText());
            Log.i("bfr", htmlText);
            htmlText = HtmlParser.parseLineBreaks(htmlText);
            Log.i("aftr", htmlText);
            Spanned spanned;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                spanned = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT);
            else
                spanned = Html.fromHtml(htmlText);
            editText.setText(spanned);
        }
        if(!(caret < 0))
            editText.setSelection(caret);
    }

    private int[] dispatchCaret(EditText editText, int start, int end) throws JSONException {
        int[] ints = new int[2];
        int startS = start - 1, startE = start + 1, endS = end - 1, endE = end + 1, txtLn = editText.getText().length();
        String endHtmlE, startHtmlS, startHrefS, startHrefE, endHrefS, endHrefE,
                startHtmlE = Html.toHtml((Spanned) editText.getText().subSequence(start, startE)),
                endHtmlS = Html.toHtml((Spanned) editText.getText().subSequence(endS, end));
        Document startDocE = Jsoup.parse(startHtmlE, "UTF-8");
        Elements startAnchE = startDocE.getElementsByTag("a");
        startHrefE = startAnchE.attr("href");
        Document endDocS = Jsoup.parse(endHtmlS, "UTF-8");
        Elements endAnchS = endDocS.getElementsByTag("a");
        endHrefS = endAnchS.attr("href");
        if(!(startS < 0)) {
            startHtmlS = Html.toHtml((Spanned) editText.getText().subSequence(startS, start));
            Document startDocS = Jsoup.parse(startHtmlS, "UTF-8");
            Elements startAnchS = startDocS.getElementsByTag("a");
            startHrefS = startAnchS.attr("href");
            if(startHrefS.equals(startHrefE)){
                JSONArray array = textWalker(editText, start, startHrefE, false, 0, true);
                ints[0] = array.getInt(0);
            } else
                ints[0] = start;
        }

        if(!(endE > txtLn)) {
            endHtmlE = Html.toHtml((Spanned) editText.getText().subSequence(end, endE));
            Document endDocE = Jsoup.parse(endHtmlE, "UTF-8");
            Elements endAnchE = endDocE.getElementsByTag("a");
            endHrefE = endAnchE.attr("href");
            if(endHrefS.equals(endHrefE)){
                JSONArray array = textWalker(editText, end, endHrefE, true, 0, true);
                ints[1] = array.getInt(0);
            }
        } else
            ints[1] = txtLn;
        return ints;
    }

    private int[] textWalker(EditText editText, int start, int end) throws JSONException {
        int[] ints = new int[2];
        String htmlText = Html.toHtml((Spanned) editText.getText().subSequence(start, end));
        Document doc = Jsoup.parse(htmlText, "UTF-8");
        Elements anchorElement = doc.getElementsByTag("a");
        String href = anchorElement.attr("href");
        boolean nxtCharIsSpace = anchorElement.text().equals(" ");
        if(nxtCharIsSpace){
            start -= 1;
            end -= 1;
        }
        JSONArray preObj = textWalker(editText, start, href, false, 0, true);
        JSONArray proObj = textWalker(editText, end, href, true, 0, true);
        int preCnt = preObj.getInt(0), proCnt = proObj.getInt(0);
        boolean preSpace = preObj.getBoolean(1), proSpace = preObj.getBoolean(1);
        if(preSpace)
            preCnt += 1;
        if(!preSpace && !proSpace && nxtCharIsSpace) {
            boolean frontSpaced = spaceWalker(editText, start + 2, end + 2, href);
            if(frontSpaced)
                proCnt += 1;
        }
        int s = start - preCnt, e = end + proCnt;
        ints[0] = s;
        ints[1] = e;
        return ints;
    }

    private JSONArray textWalker(EditText editText, int caret, String href, boolean forward, int count, boolean selected) throws JSONException {
        JSONArray array = new JSONArray();
        boolean clearFrwd = false;
        String htmlText;
        if(!href.isEmpty() && forward && selected && caret > 0){
            htmlText = Html.toHtml((Spanned) editText.getText().subSequence(caret - 1, caret));
            Document doc = Jsoup.parse(htmlText, "UTF-8");
            Elements anchorElement = doc.getElementsByAttributeValue("href", href);
            if(anchorElement.size() > 0) {
                boolean nxtCharIsSpace = anchorElement.text().equals(" ");
                if(nxtCharIsSpace)
                    clearFrwd = true;
            }
        }
        if(clearFrwd || caret == 0 || href.isEmpty()){
            array.put(0, count);
            array.put(1, false);
            return array;
        }
        int start = caret -1, end = caret, next = caret - 1;
        if(forward) {
            start = caret;
            end = caret + 1;
            next = caret + 1;
        }
        String nxtChar = editText.getText().subSequence(start, end).toString();
        htmlText = Html.toHtml((Spanned) editText.getText().subSequence(start, end));
        Document doc = Jsoup.parse(htmlText, "UTF-8");
        Elements anchorElement = doc.getElementsByAttributeValue("href", href);
        if(anchorElement.size() == 0) {
            array.put(0, count);
            array.put(1, false);
            return array;
        } else {
            boolean nxtCharIsSpace = nxtChar.equals(" ");
            if(nxtCharIsSpace) {
                array.put(0, count);
                array.put(1, true);
                return array;
            }
            count++;
            return textWalker(editText, next, href, forward, count, selected);
        }
    }

    private boolean spaceWalker(EditText editText, int start, int end, String href){
        if(end > editText.getText().length())
            return false;
        String htmlText = Html.toHtml((Spanned) editText.getText().subSequence(start, end));
        Document doc = Jsoup.parse(htmlText, "UTF-8");
        Elements anchorElement = doc.getElementsByAttributeValue("href", href);
        return anchorElement.size() > 0;
    }

    private void changeWordToHashTag(EditText editText, String lastWord) {
        hashed = true;
        boolean matchedRegex = false;
        Spanned htmlText;
        String str = "#", regex = "[^\\w_]";
        int caret = editText.getSelectionStart(), lastWordLen = lastWord.length();
        int indexStart = caret - lastWordLen, length = editText.getText().length(), newCaretPos = caret;
        String lastChar = editText.getText().subSequence(caret - 1, caret).toString();
        boolean ex = Pattern.matches(regex, lastChar);
        if(ex){
            hashed = false;
            return;
        }
        String afterCursorTxt = editText.getText().subSequence(caret, length).toString();
        if(!StringUtils.isEmpty(afterCursorTxt)) {
            String nxtChar = String.valueOf(afterCursorTxt.charAt(0));
            matchedRegex = Pattern.matches(regex, nxtChar);
        }
        if(!matchedRegex) {
            if(!StringUtils.isEmpty(afterCursorTxt)) {
                String[] strings = afterCursorTxt.split(regex);
                String nextWord = strings[0];
                lastWord += nextWord;
            }
            int indexEnd = indexStart + 1;
            if(!lastWord.equals("#")) {
                int newLn = lastWord.length();
                indexEnd = indexStart + newLn;
                newCaretPos = indexStart + lastWordLen;
                String href = lastWord.substring(1);
                boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                String bColor = "#F9F9F9";
                if (darkThemeEnabled)
                    bColor = "#585858";
                str = "<a href=\"search/" + href + "\"><span style=\"background-color:" + bColor + ";\"><b>" + lastWord + "</b></span></a>";
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                htmlText = Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT);
            else
                htmlText = Html.fromHtml(str);
            editText.setText(editText.getText().delete(indexStart, indexEnd));
            editText.getText().insert(indexStart, htmlText);
            editText.setSelection(newCaretPos);
        }
        hashed = false;
    }

    private void addTextToView(EditText editText, String name, String tab, int id) {
        addingTxt = true;
        mCount++;
        Spanned htmlText;
        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        String bColor = "#F9F9F9";
        if(darkThemeEnabled)
            bColor = "#585858";
        String str = " <a href=\""+tab+"-"+id+"-"+mCount+"\"><b><span style=\"background-color:"+bColor+";\">"+name+"</span></b></a> ";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            htmlText = Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT);
        else
            htmlText = Html.fromHtml(str);
        String allText = editText.getText().toString();
        int indexEnd = editText.getSelectionStart(), addedCaret = name.length();
        String textBfr = allText.substring(0, indexEnd);
        String[] textArr = textBfr.split(" ");
        int index = textArr.length - 1;
        String lastWord = textArr[index];
        int lastWordLen = lastWord.length();
        int indexStart = indexEnd - lastWordLen;
        int newCaretPos = indexStart + addedCaret + 1;
        editText.setText(editText.getText().delete(indexStart, indexEnd));
        editText.getText().insert(indexStart, htmlText);
        editText.setSelection(newCaretPos);
        addingTxt = false;
    }

    private void openReportDialog() {
        @SuppressLint("InflateParams") RelativeLayout view = (RelativeLayout) getLayoutInflater().inflate(R.layout.report_box, null);
        Button sendBtn = view.findViewById(R.id.sendBtn);
        EditText reportText = view.findViewById(R.id.reportText);
        TextView toogle = view.findViewById(R.id.toogle);
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

    public static void updateMessageDelivery(int user){
        socket.emit("msgDelivered", user);
    }

    private void getPostDisplay(){
        loadingPost = true;
        while (loadingPost){
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("lang", lang)
                    .addFormDataPart("firstLoad", String.valueOf(firstLoad))
                    .addFormDataPart("user", myIDtoString)
                    .addFormDataPart("selectedPosts", selectedPosts.toString().intern())
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.postDisplayUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    loadingPost = false;
                    postScrllVw.setVisibility(View.VISIBLE);
                    Log.e("DATA", responseString);
                    JSONObject responseArr = new JSONObject(responseString);
                    if(firstLoad) {
                        postView.removeView(loadingLayer);
                        firstLoad = false;
                        msgCount = responseArr.getInt("msgCount");
                        noteCount = responseArr.getInt("noteCount");
                        JSONArray searchArr = responseArr.getJSONArray("searchData");
                        if(msgCount > 0){
                            String msgNumTxt = convertToTextPlus(msgCount);
                            msgCV.setVisibility(View.VISIBLE);
                            msgNumTextView.setText(msgNumTxt);
                        }
                        if(noteCount > 0){
                            String noteNumTxt = convertToTextPlus(noteCount);
                            noteCV.setVisibility(View.VISIBLE);
                            noteNumTextView.setText(noteNumTxt);
                        }
                        for (int j = 0; j < searchArr.length(); j++) {
                            JSONObject searchObj = searchArr.getJSONObject(j);
                            int dataId = searchObj.getInt("dataId");
                            String type = searchObj.getString("type");
                            String key = type + "-" + dataId;
                            searchData.put(key, searchObj);
                            showSearchHistory(searchObj, j, false);
                        }
                    } else
                        layout.removeView(loadingLayer);
                    JSONObject pagesInfo = responseArr.getJSONObject("pageArray");
                    hasPage = pagesInfo.getBoolean("hasPage");
                    userPages = pagesInfo.getJSONArray("userPages");
                    JSONArray postsArr = responseArr.getJSONArray("postDatas");
                    allLoaded = responseArr.getBoolean("allLoaded");
                    if(!hasPage){
                        poster = String.valueOf(myId);
                        postType = "profile";
                        posterImg = myPht;
                        posterName = myName;
                        posterUName = myUserName;
                    }
                    for (int p = 0; p < postsArr.length(); p++) {
                        JSONObject postObj = new JSONObject(postsArr.getString(p));
                        RelativeLayout postView = setupPostView(postObj);
                        layout.addView(postView);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    public void uploadPost(boolean v){
        if(postType == null && poster == null){
            try {
                RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
                LinearLayout optLayer = postOptView.findViewById(R.id.optLayer);
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                LinearLayout tvw = (LinearLayout) getLayoutInflater().inflate(R.layout.text_view, null);
                TextView txtHead = tvw.findViewById(R.id.head);
                TextView txtBody = tvw.findViewById(R.id.body);
                txtBody.setVisibility(View.GONE);
                txtHead.setText("Make Post As:");
                optLayer.addView(tvw);
                LinearLayout tabletR = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.tablet, null, false);
                LinearLayout layR = tabletR.findViewById(R.id.lay);
                ImageView imageViewR = tabletR.findViewById(R.id.image);
                TextView txtNameR = tabletR.findViewById(R.id.name);
                TextView txtUserNameR = tabletR.findViewById(R.id.userName);
                imageLoader.displayImage(myPht, imageViewR);
                txtNameR.setText(myName);
                txtUserNameR.setText("@"+myUserName);
                if(userVerified)
                    txtNameR.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                optLayer.addView(tabletR);
                layR.setOnClickListener(v16 -> setUploader(myIDtoString, "profile", myPht, myName, myUserName, userVerified));
                for (int r = 0; r < userPages.length(); r++){
                    JSONObject pageInfo = new JSONObject(userPages.getString(r));
                    String pageId = pageInfo.getString("id");
                    String pageName = pageInfo.getString("pageName");
                    String pagePhoto = www + pageInfo.getString("photo");
                    boolean verified = pageInfo.getBoolean("verified");
                    LinearLayout tablet = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.tablet, null, false);
                    LinearLayout lay = tablet.findViewById(R.id.lay);
                    ImageView imageView = tablet.findViewById(R.id.image);
                    TextView txtName = tablet.findViewById(R.id.name);
                    TextView txtUserName = tablet.findViewById(R.id.userName);
                    txtUserName.setVisibility(View.GONE);
                    imageLoader.displayImage(pagePhoto, imageView);
                    txtName.setText(pageName);
                    optLayer.addView(tablet);
                    if(verified)
                        txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                    lay.setOnClickListener(v15 -> setUploader(pageId, "page", pagePhoto, pageName, null, verified));
                }
                blackFade.addView(postOptView);
                blackFade.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }
        pageOver = false;
        curLayout = null;
        writeLayout.setVisibility(View.GONE);
        imageAdapter = new ImageAdaptor(cntxt, new File[]{}, imgW);
        gridView.setAdapter(imageAdapter);
        uploadingPost = true;
        boolean vary = false;
        boolean hasFiles = false;
        String postText = postTxt.getText().toString();
        String htmlText = Html.toHtml(postTxt.getText());
        htmlText = HtmlParser.parseString(htmlText);
        String text = HtmlParser.parseSpan(htmlText);
        RelativeLayout postView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post, null);
        RelativeLayout progressView = (RelativeLayout) getLayoutInflater().inflate(R.layout.progress_bar, null);
        TextView postProgressText = progressView.findViewById(R.id.postProgressText);
        ProgressBar postProgressBar = progressView.findViewById(R.id.postProgressBar);
        ImageView userImageView = postView.findViewById(R.id.posterPht);
        LinearLayout postTextLayout = postView.findViewById(R.id.postTextLayout);
        LinearLayout postFilesLayout = postView.findViewById(R.id.postFilesLayout);
        TextView nameTxtVw = postView.findViewById(R.id.posterName);
        TextView userNameTxtVw = postView.findViewById(R.id.posterUName);
        TextView postDate = postView.findViewById(R.id.postDate);
        ImageView myComPht = postView.findViewById(R.id.myComPht);
        TextView pstText = postView.findViewById(R.id.postText);
        ViewPager viewPager = postView.findViewById(R.id.viewPager);
        imageLoader.displayImage(posterImg, userImageView);
        imageLoader.displayImage(myPht, myComPht);
        CharSequence sequence = Html.fromHtml(text);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span, cntxt);
        }
        pstText.setText(strBuilder);
        pstText.setMovementMethod(LinkMovementMethod.getInstance());
        nameTxtVw.setText(posterName);
        if(v)
            nameTxtVw.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
        postDate.setText("Just Now");
        if(posterUName == null) {
            userNameTxtVw.setVisibility(View.GONE);
            vary = true;
        } else
            userNameTxtVw.setText("@"+myUserName);
        if (StringUtils.isEmpty(postText))
            postTextLayout.setVisibility(View.GONE);
        String postTyp = postType;
        int postr = Integer.parseInt(poster);
        progressView.setOnClickListener(v14 -> {
            return;
        });
        userImageView.setOnClickListener(v13 -> visitPosterPage(postr, postTyp));
        userNameTxtVw.setOnClickListener(v12 -> visitPosterPage(postr, postTyp));
        nameTxtVw.setOnClickListener(v1 -> visitPosterPage(postr, postTyp));
        if(itemLists.size() > 0){
            hasFiles = true;
            int numOfImgs = itemLists.size();
            if(itemLists.size() > 9)
                numOfImgs = 0;
            int numDis = numberOfImages[numOfImgs];
            View imgNum = postView.findViewById(R.id.imgNum);
            imgNum.setBackgroundResource(numDis);
            postFilesLists = new ArrayList<>();
            for (int w = 0; w < itemLists.size(); w++){
                File file = itemLists.get(w);
                String filePath = file.getAbsolutePath();
                postFilesLists.add(filePath);
            }
            photoAdapter = new PhotoAdapter(postFilesLists, cntxt);
            curViewPager = viewPager;
            ViewGroup.LayoutParams params = postFilesLayout.getLayoutParams();
            viewPager.setOffscreenPageLimit(itemLists.size());
            params.height = viewPagerHeight;
            viewPager.setLayoutParams(params);
            viewPager.setAdapter(photoAdapter);
            viewPager.setPageTransformer(true, new PageTransformer());
            viewPager.post(() -> {
                View view = viewPager.getChildAt(0);
                ImageView imageView = view.findViewById(R.id.image);
                imageView.post(() -> extractBlur(viewPager, imageView));
            });
        } else  {
            postFilesLayout.setVisibility(View.GONE);
        }
        postView.addView(progressView);
        layout.addView(postView, 0);
        postView.post(() -> {
            int postW = postView.getWidth();
            int postH = postView.getHeight();
            progressView.setLayoutParams(new RelativeLayout.LayoutParams(postW, postH));
        });
        MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (int g = 0; g < itemLists.size(); g++){
            File readFile = itemLists.get(g);
            Uri uris = Uri.fromFile(readFile);
            String fileExt = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.toLowerCase());
            multipartBody.addFormDataPart("files[]", readFile.getName(), RequestBody.create(readFile, MediaType.parse(mimeType)));
        }
        multipartBody.addFormDataPart("lang", lang)
                .addFormDataPart("user", poster)
                .addFormDataPart("postType", postType)
                .addFormDataPart("postText", htmlText);

        final CountingRequestBody.Listener progressListener = (bytesRead, contentLength) -> {
            if (bytesRead < contentLength && contentLength > 0) {
                final int progress = (int)Math.round((((double) bytesRead / contentLength) * 100));
                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        postProgressBar.setProgress(progress, true);
                    else
                        postProgressBar.setProgress(progress);
                    postProgressText.setText(progress+"%");
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
                .url(Constants.submitPostUrl)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();


        boolean finalVary = vary;
        if(postType.equals("profile"))
            finalVary = false;
        boolean finalVary1 = finalVary;
        String finalHtmlText = htmlText;
        boolean finalHasFiles = hasFiles;
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = Objects.requireNonNull(response.body()).string();
                for(int c = 0; c < editedImages.size(); c++){
                    String filePath = editedImages.get(c);
                    File file = new File(filePath);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(file.toPath());
                    else
                        file.delete();
                }
                try {
                    JSONObject emitObj = new JSONObject();
                    JSONObject postObj = new JSONObject(responseStr);
                    String postId = postObj.getString("id");
                    JSONArray mentionedUsers = new JSONArray(postObj.getString("mentionedUsers"));
                    JSONArray mentionedPages = new JSONArray(postObj.getString("mentionedPages"));
                    if((mentionedUsers.length() + mentionedPages.length()) > 0) {
                        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        emitObj.put("user", myId);
                        emitObj.put("postId", postId);
                        emitObj.put("mentionedUsers", mentionedUsers);
                        emitObj.put("mentionedPages", mentionedPages);
                        emitObj.put("date", date);
                        socket.emit("notifyTags", emitObj);
                    }
                    postObj.put("user", poster);
                    postObj.put("name", posterName);
                    postObj.put("userName", myUserName);
                    postObj.put("post", finalHtmlText);
                    postObj.put("date", "Just Now");
                    postObj.put("comments", "[]");
                    postObj.put("type", postType);
                    postObj.put("verified", v);
                    postObj.put("hasFiles", finalHasFiles);
                    postObj.put("liked", false);
                    postObj.put("perm", true);
                    postObj.put("vary", finalVary1);
                    postObj.put("likeNum", 0);
                    postObj.put("comNum", 0);
                    RelativeLayout newPostView = setupPostView(postObj);
                    runOnUiThread(() -> {
                        postProgressBar.setProgress(100, true);
                        postProgressText.setText("100%");
                        layout.removeView(postView);
                        layout.addView(newPostView, 0);
                    });
                    socket.emit("postScratch", postId);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                uploadingPost = false;
                resetPost();
            }
        });
    }

    private void visitPosterPage(int postr, String postTyp) {
        switch (postTyp){
            case "profile":
                visitUserProfile(cntxt, postr);
                break;
            case "page":
                visitPage(cntxt, postr);
                break;
        }
    }

    @SuppressLint("InflateParams")
    private RelativeLayout setupPostView(JSONObject postObj) throws JSONException {
        String postID = postObj.getString("id");
        String postUser = postObj.getString("user");
        String postName = postObj.getString("name");
        String postUserName = postObj.getString("userName");
        String postPhoto = www + postObj.getString("photo");
        String postText = postObj.getString("post");
        String files = postObj.getString("files");
        String postDate = postObj.getString("date");
        String postOptions = postObj.getString("options");
        String postOptionsVal = postObj.getString("optionsVal");
        String postComments = postObj.getString("comments");
        String pagerId = postObj.getString("pagerId");
        String type = postObj.getString("type");
        boolean hasFiles = postObj.getBoolean("hasFiles");
        boolean verified = postObj.getBoolean("verified");
        boolean liked = postObj.getBoolean("liked");
        boolean perm = postObj.getBoolean("perm");
        boolean vary = postObj.getBoolean("vary");
        int likeNum = postObj.getInt("likeNum");
        int comNum = postObj.getInt("comNum");
        JSONObject object = new JSONObject(postOptionsVal);
        postsOptions.put(postID, postOptions);
        posters.put(postID, postUser);
        postsOptionsVal.put(postID, object);
        selectedPosts.add(postID);
        RelativeLayout postView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post, null);
        LinearLayout postTextLayout = postView.findViewById(R.id.postTextLayout);
        LinearLayout postFilesLayout = postView.findViewById(R.id.postFilesLayout);
        LinearLayout commentsLayout = postView.findViewById(R.id.commentsLayout);
        LinearLayout commentBx = postView.findViewById(R.id.commentBx);
        ImageView userImageView = postView.findViewById(R.id.posterPht);
        ImageView myComPht = postView.findViewById(R.id.myComPht);
        ImageButton postOpt = postView.findViewById(R.id.postOpt);
        ImageButton likePost = postView.findViewById(R.id.likePost);
        ImageButton comments = postView.findViewById(R.id.comments);
        TextView pstText = postView.findViewById(R.id.postText);
        TextView nameTxtVw = postView.findViewById(R.id.posterName);
        TextView userNameTxtVw = postView.findViewById(R.id.posterUName);
        TextView postDateVw = postView.findViewById(R.id.postDate);
        TextView comNumTV = postView.findViewById(R.id.comNum);
        TextView likeNumTV = postView.findViewById(R.id.likeNum);
        TextView likers = postView.findViewById(R.id.likers);
        ImageButton submitCommentBtn = postView.findViewById(R.id.submitCommentBtn);
        EditText commentBox = postView.findViewById(R.id.commentBox);
        ViewPager viewPager = postView.findViewById(R.id.viewPager);
        postLayouts.put(postID, postView);
        reactorView.put(postID, likers);
        likesDisplay.put(postID, likeNumTV);
        commentsLayoutObj.put(postID, commentsLayout);
        comsDisplay.put(postID, comNumTV);
        imageLoader.displayImage(postPhoto, userImageView);
        imageLoader.displayImage(myPht, myComPht);
        nameTxtVw.setText(postName);
        postDateVw.setText(postDate);
        String htmlText = HtmlParser.parseSpan(postText);
        CharSequence sequence = Html.fromHtml(htmlText);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span, cntxt);
        }
        pstText.setText(strBuilder);
        pstText.setMovementMethod(LinkMovementMethod.getInstance());
        if(verified)
            nameTxtVw.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
        if(!perm)
            commentBx.setVisibility(View.GONE);
        if (StringUtils.isEmpty(postText))
            postTextLayout.setVisibility(View.GONE);
        if (type.equals("profile")) {
            postUserName= "@" + postUserName;
            userNameTxtVw.setText(postUserName);
        } else
            userNameTxtVw.setVisibility(View.GONE);
        if(liked) {
            likePost.setBackgroundResource(R.drawable.ic_loved);
            likePost.setTag("1");
        }
        if (likeNum > 0) {
            String likes = Functions.convertToText(likeNum);
            likeNumTV.setText(likes);
            likers.setVisibility(View.VISIBLE);
        }
        if (comNum > 0) {
            String coms = Functions.convertToText(comNum);
            comNumTV.setText(coms);
        }
        initializeEditText(commentBox, comScrllView, true, postID);
        likePost.setOnClickListener(v -> reactToPost(Integer.parseInt(postID), v));
        likers.setOnClickListener(v -> showReactors(cntxt, postID, "posts", likeNum));
        submitCommentBtn.setOnClickListener(v -> submitComment(commentBox, postID, commentsLayout, vary, pagerId));
        comments.setOnClickListener(v -> openComments(postID, null));
        postOpt.setOnClickListener(v -> {
            try {
                displayPostsOptions(postID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        userImageView.setOnClickListener(v -> {
            switch (type){
                case "profile":
                    visitUserProfile(cntxt, Integer.parseInt(postUser));
                    break;
                case "page":
                    visitPage(cntxt, Integer.parseInt(postUser));
                    break;
            }
        });
        userNameTxtVw.setOnClickListener(v -> {
            switch (type){
                case "profile":
                    visitUserProfile(cntxt, Integer.parseInt(postUser));
                    break;
                case "page":
                    visitPage(cntxt, Integer.parseInt(postUser));
                    break;
            }
        });
        nameTxtVw.setOnClickListener(v -> {
            switch (type){
                case "profile":
                    visitUserProfile(cntxt, Integer.parseInt(postUser));
                    break;
                case "page":
                    visitPage(cntxt, Integer.parseInt(postUser));
                    break;
            }
        });
        if(hasFiles) {
            onPagerScrolled.put(postID, false);
            JSONArray postFiles = new JSONArray(files);
            postFilesLists = new ArrayList<>();
            int numOfImgs = postFiles.length();
            if (postFiles.length() > 9)
                numOfImgs = 0;
            int numDis = numberOfImages[numOfImgs];
            View imgNum = postView.findViewById(R.id.imgNum);
            imgNum.setBackgroundResource(numDis);
            for (int h = 0; h < postFiles.length(); h++) {
                String filePath = www + postFiles.getString(h);
                postFilesLists.add(filePath);
            }
            photoAdapter = new PhotoAdapter(postFilesLists, cntxt);
            viewPagers.put(String.valueOf(vpc), viewPager);
            vpc++;
            ViewGroup.LayoutParams params = postFilesLayout.getLayoutParams();
            params.height = viewPagerHeight;
            viewPager.setLayoutParams(params);
            viewPager.setOffscreenPageLimit(postFiles.length());
            viewPager.setAdapter(photoAdapter);
            viewPager.setPageTransformer(true, new PageTransformer());
            viewPager.post(() -> {
                View view = viewPager.getChildAt(0);
                ImageView imageView = view.findViewById(R.id.image);
                imageView.post(() -> extractBlur(viewPager, imageView));
            });
        } else {
            postFilesLayout.setVisibility(View.GONE);
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                try {
                    if (!onPagerScrolled.getBoolean(postID) && positionOffset == 0 && positionOffsetPixels == 0){
                        viewPager.setCurrentItem(0);
                        onPageSelected(0);
                        onPagerScrolled.put(postID, true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageSelected(int position) {
                setViewPagerChangeListener(viewPager, position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        JSONArray commentsArr = new JSONArray(postComments);
        for (int r = 0; r < commentsArr.length(); r++){
            JSONObject commentsObj = new JSONObject(commentsArr.getString(r));
            String commentID = commentsObj.getString("id");
            String comUserName = commentsObj.getString("userName");
            String comPhoto = www + commentsObj.getString("photo");
            String miniCom = commentsObj.getString("comment");
            LinearLayout miniComLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.mini_comments, null);
            miniComDisplay.put(commentID, miniComLayout);
            ImageView imgView = miniComLayout.findViewById(R.id.profPic);
            TextView commentTxtVw = miniComLayout.findViewById(R.id.commentView);
            imageLoader.displayImage(comPhoto, imgView);
            miniCom = HtmlParser.parseBreaks(miniCom);
            miniCom = EmojiParser.parseToUnicode(miniCom);
            String htmlComText = "<font><b>@"+comUserName+"</b> "+miniCom+"</font>";
            commentTxtVw.setText(Html.fromHtml(htmlComText));
            miniComLayout.setOnClickListener(v -> openComments(postID, commentID));
            commentsLayout.addView(miniComLayout);
        }
        socket.emit("postScratch", postID);
        return postView;
    }

    private void initializeEditText(EditText editText, ScrollView scrollView, boolean advanced, String dataId) {
        editText.setAccessibilityDelegate(new View.AccessibilityDelegate(){
            @Override
            public void sendAccessibilityEvent(View host, int eventType) {
                super.sendAccessibilityEvent(host, eventType);
                if(eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
                    new android.os.Handler().postDelayed(() -> {
                        selStart = editText.getSelectionStart();
                        selEnd = editText.getSelectionEnd();
                        afterAnchor = getCursorPosition(editText);
                        btwnHtml = getCursorState(editText);
                    }, 50);
                }
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                strLn = s.length();
                prevHtmlTxt = Html.toHtml(editText.getText());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charLn = count;
                if(!changingTxt) {
                    try {
                        curEditor = editText;
                        String allText = s.toString();
                        int caretPos = editText.getSelectionStart();
                        String text = allText.substring(0, caretPos);
                        int textLen = text.length(), textL = textLen - 1;
                        if(allText.length() > caretPos) {
                            char nextChar = allText.charAt(caretPos);
                            if (!Character.isWhitespace(nextChar))
                                scrollView.setVisibility(View.GONE);
                        }
                        if (textLen > 0) {
                            char lastChar = text.charAt(textL);
                            if (Character.isWhitespace(lastChar))
                                scrollView.setVisibility(View.GONE);
                        }
                        String[] textArr = text.split("[^\\w_#@]");
                        String lastWord = text;
                        if(textArr.length > 0) {
                            int lastWordIndex = textArr.length - 1;
                            lastWord = textArr[lastWordIndex];
                        }
                        int lastWordLen = lastWord.length();
                        String regex = "^#[\\w_]+$";
                        boolean matchedRegex = Pattern.matches(regex, lastWord) || lastWord.equals("#");
                        if (!hashed && matchedRegex) {
                            changeWordToHashTag(editText, lastWord);
                            return;
                        }
                        if (lastWordLen > 1) {
                            String htmlText = Html.toHtml((Spanned) editText.getText().subSequence(caretPos - lastWordLen, caretPos));
                            Document doc = Jsoup.parse(htmlText, "UTF-8");
                            Elements anchorElement = doc.getElementsByTag("a");
                            String href = anchorElement.attr("href");
                            if (href.length() == 0) {
                                JSONObject emitObj = new JSONObject();
                                emitObj.put("user", myId);
                                emitObj.put("advanced", advanced);
                                if (advanced) {
                                    emitObj.put("table", "comments");
                                    emitObj.put("dataId", dataId);
                                }
                                emitObj.put("lastWord", lastWord);
                                socket.emit("checkMention", emitObj);
                                return;
                            }
                            scrollView.setVisibility(View.GONE);
                        } else {
                            if(lastWordLen == 1 && lastWord.equals("@")){
                                JSONObject emitObj = new JSONObject();
                                emitObj.put("user", myId);
                                emitObj.put("advanced", advanced);
                                if (advanced) {
                                    emitObj.put("table", "comments");
                                    emitObj.put("dataId", dataId);
                                }
                                emitObj.put("lastWord", "");
                                socket.emit("checkMention", emitObj);
                                return;
                            }
                            scrollView.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean backSpaced = strLn > s.length() && charLn == 0;
                if(btwnHtml && !changingTxt && !backSpaced){
                    int end = editText.getSelectionEnd();
                    boolean minCheck = selStart > 0, maxCheck = end < s.length();
                    if(minCheck || maxCheck)
                        removeAnchor(editText, selStart, end, charLn, minCheck, maxCheck);
                } else if(backSpaced && !changingTxt && !addingTxt && afterAnchor){
                    try {
                        overrideBackspace(editText, selStart, selEnd);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        editText.setOnKeyListener((v, keyCode, event) -> {
            curEditor = editText;
            return false;
        });
        editText.setOnClickListener(v -> curEditor = editText);
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus)
                curEditor = editText;
        });
    }

    private void removeAnchor(EditText editText, int start, int end, int charL, boolean minCheck, boolean maxCheck) {
        changingTxt = true;
        String htmlText, cropTxt, href, anchorTxt;
        Document doc, document;
        Elements anchorElement, allAnchors;
        int strLn, length = editText.getText().length();
        if(maxCheck){
            htmlText = Html.toHtml((Spanned) editText.getText().subSequence(end, length));
            cropTxt = Html.toHtml((Spanned) editText.getText().subSequence(end, end + 1));
            doc = Jsoup.parse(cropTxt, "UTF-8");
            anchorElement = doc.getElementsByTag("a");
            if(!(anchorElement == null)){
                href = anchorElement.attr("href");
                if(href.length() > 0){
                    document = Jsoup.parse(htmlText, "UTF-8");
                    allAnchors = document.getElementsByAttributeValue("href", href);
                    anchorTxt = allAnchors.text().replaceAll(" {2}", " ");
                    strLn = anchorTxt.length();
                    int rStart = start + charL, rEnd = start + strLn + charL,  e = start + charL;
                    if(!(start < 0) && !(e > length)) {
                        boolean nxtCharIsSpace = editText.getText().subSequence(rStart, e).toString().equals(" ");
                        if (nxtCharIsSpace) {
                            rEnd += 1;
                            anchorTxt = " " + anchorTxt;
                        }
                    }
                    editText.setText(editText.getText().delete(rStart, rEnd));
                    editText.getText().insert(rStart, anchorTxt);
                    editText.setSelection(rStart);
                }
            }
        }
        if(minCheck){
            htmlText = Html.toHtml((Spanned) editText.getText().subSequence(0, start));
            cropTxt = Html.toHtml((Spanned) editText.getText().subSequence(start - 1, start));
            doc = Jsoup.parse(cropTxt, "UTF-8");
            anchorElement = doc.getElementsByTag("a");
            if(!(anchorElement == null)){
                href = anchorElement.attr("href");
                if(href.length() > 0){
                    document = Jsoup.parse(htmlText, "UTF-8");
                    allAnchors = document.getElementsByAttributeValue("href", href);
                    anchorTxt = allAnchors.text().replaceAll(" {2}", " ");
                    strLn = anchorTxt.length();
                    int rStart = end - strLn - charL, rEnd = end - charL, caret = end + charL - 1, s = end - 2;
                    if(!(s < 0) && !(end > length)) {
                        boolean nxtCharIsSpace = editText.getText().subSequence(s, end - 1).toString().equals(" ");
                        if(nxtCharIsSpace) {
                            rStart -= 1;
                            caret -= 1;
                            anchorTxt += " ";
                        }
                    }
                    editText.setText(editText.getText().delete(rStart, rEnd));
                    editText.getText().insert(rStart, anchorTxt);
                    editText.setSelection(caret);
                }
            }
        }
        changingTxt = false;
    }

    private boolean getCursorPosition(EditText editText){
        prevHtmlTxt = null;
        if(selEnd == 0)
            return false;
        int caretEnd = selEnd, caretStart = selStart;
        if(caretStart == caretEnd)
            caretStart = caretEnd - 1;
        if(caretStart > caretEnd){
            int[] ints1 = swapInts(caretStart, caretEnd);
            caretStart = ints1[0];
            caretEnd = ints1[1];
        }
        String htmlText = Html.toHtml((Spanned) editText.getText().subSequence(caretStart, caretEnd));
        Document doc = Jsoup.parse(htmlText, "UTF-8");
        Elements anchorElement = doc.getElementsByTag("a");
        if(anchorElement == null)
            return false;
        String href = anchorElement.attr("href");
        if(href.length() == 0)
            return false;
        String startStrOne = "Friend";
        String startStrTwo = "Page";
        return href.startsWith(startStrOne) || href.startsWith(startStrTwo);
    }

    private boolean getCursorState(EditText editText){
        if(selEnd == 0 || selStart == selEnd)
            return false;
        int caretEnd1 = selStart + 1, caretStart1 = selStart - 1, caretEnd2 = selEnd + 1, caretStart2 = selEnd - 1, textLen = editText.getText().length();
        if(caretStart1 < 0 || caretStart2 < 0 || caretEnd1 > textLen || caretEnd2 > textLen)
            return false;
        String htmlText1 = Html.toHtml((Spanned) editText.getText().subSequence(caretStart1, caretEnd1));
        String htmlText2 = Html.toHtml((Spanned) editText.getText().subSequence(caretStart2, caretEnd2));
        Document doc1 = Jsoup.parse(htmlText1, "UTF-8");
        Document doc2 = Jsoup.parse(htmlText2, "UTF-8");
        Elements anchorElement1 = doc1.getElementsByTag("a");
        Elements anchorElement2 = doc2.getElementsByTag("a");
        if(anchorElement1 == null || anchorElement2 == null)
            return false;
        String href1 = anchorElement1.attr("href");
        String href2 = anchorElement2.attr("href");
        if(href1.length() == 0 || href2.length() == 0)
            return false;
        String startStrOne = "Friend";
        String startStrTwo = "Page";
        return !href1.equals(href2) && (href1.startsWith(startStrOne) || href1.startsWith(startStrTwo));
    }

    private int[] swapInts(int a, int b){
        return new int[] {b, a};
    }

    private static void makeLinkClickable(SpannableStringBuilder strBuilder, URLSpan span, Context cntxt) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                String href = span.getURL();
                if(href.startsWith("Friend") || href.startsWith("Page") || href.startsWith("search")) {
                    String[] hrefSplit = href.split("[-/]");
                    String dataType = hrefSplit[0];
                    String dataId = hrefSplit[1];
                    if (dataType.equals("Friend"))
                        visitUserProfile(cntxt, Integer.parseInt(dataId));
                    if (dataType.equals("Page"))
                        visitPage(cntxt, Integer.parseInt(dataId));
                    if (dataType.equals("search"))
                        visitSearch(cntxt, dataId);
                } else
                    openLink(cntxt, href);
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    private static void openLink(Context context, String linkUrl) {
        Uri uri = Uri.parse(linkUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static void showReactors(Context cntxt, String postID, String dataType, int likeNum) {
        Intent intent = new Intent(cntxt, ReactActivity.class);
        Bundle userParams = new Bundle();
        userParams.putString("dataId", postID);
        userParams.putString("dataType", dataType);
        userParams.putInt("likeNum", likeNum);
        intent.putExtras(userParams);
        cntxt.startActivity(intent);
    }

    private static void visitPage(Context cntxt, int pageId) {
        Intent intent = new Intent(cntxt, PageActivity.class);
        Bundle pageParams = new Bundle();
        pageParams.putInt("pageId", pageId);
        intent.putExtras(pageParams);
        cntxt.startActivity(intent);
    }

    private static void visitSearch(Context cntxt, String word) {
        Intent intent = new Intent(cntxt, SearchActivity.class);
        Bundle pageParams = new Bundle();
        pageParams.putString("word", word);
        pageParams.putInt("position", 3);
        intent.putExtras(pageParams);
        cntxt.startActivity(intent);
    }

    private void setViewPagerChangeListener(ViewPager viewPager, int position) {
        View view = viewPager.getChildAt(position);
        ImageView imageView = view.findViewById(R.id.image);
        boolean imgLoaded = Boolean.parseBoolean(imageView.getTag().toString());
        if(imgLoaded)
            extractBlur(viewPager, imageView);
    }

    private void extractBlur(ViewPager viewPager, ImageView imageView){
        try {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            if(!(bitmap == null)){
                Bitmap blurredBtmp = BlurBitmap.blurify(bitmap);
                Drawable drawable = new BitmapDrawable(cntxt.getResources(), blurredBtmp);
                viewPager.setBackground(drawable);
            } else {
                new android.os.Handler().postDelayed(() -> extractBlur(viewPager, imageView), 2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reactToPost(int postID, View v) {
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        int tag = Integer.parseInt(v.getTag().toString());
        JSONObject emitObj = new JSONObject();
        try {
            TextView textView = (TextView) likesDisplay.get(String.valueOf(postID));
            String text = textView.getText().toString();
            emitObj.put("user", myId);
            emitObj.put("postId", postID);
            emitObj.put("date", date);
            emitObj.put("tag", tag);
            int newTag = reactions[tag][0];
            int newReact = reactions[tag][1];
            int num = Functions.convertToNumber(text);
            if(tag == 0)
                num++;
            else
                num--;
            text = Functions.convertToText(num);
            textView.setText(text);
            v.setTag(newTag);
            v.setBackgroundResource(newReact);
            TextView likesTextView = (TextView) reactorView.get(String.valueOf(postID));
            if(num > 0){
                runOnUiThread(() -> likesTextView.setVisibility(View.VISIBLE));
            } else {
                runOnUiThread(() -> likesTextView.setVisibility(View.GONE));
            }
            socket.emit("postLike", emitObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void submitComment(EditText commentBox, String postID, LinearLayout commentsLayout, boolean vary, String pagerId) {
        String comment = commentBox.getText().toString();
        if(!StringUtils.isEmpty(comment)){
            if(vary){
                try {
                    RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
                    LinearLayout optLayer = postOptView.findViewById(R.id.optLayer);
                    if(blackFade.getChildCount() > 0){
                        blackFade.removeAllViews();
                    }
                    LinearLayout tvw = (LinearLayout) getLayoutInflater().inflate(R.layout.text_view, null);
                    TextView txtHead = tvw.findViewById(R.id.head);
                    TextView txtBody = tvw.findViewById(R.id.body);
                    txtBody.setVisibility(View.GONE);
                    txtHead.setText("Make Comment As:");
                    optLayer.addView(tvw);
                    LinearLayout tabletR = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.tablet, null, false);
                    LinearLayout layR = tabletR.findViewById(R.id.lay);
                    ImageView imageViewR = tabletR.findViewById(R.id.image);
                    TextView txtNameR = tabletR.findViewById(R.id.name);
                    TextView txtUserNameR = tabletR.findViewById(R.id.userName);
                    imageLoader.displayImage(myPht, imageViewR);
                    txtNameR.setText(myName);
                    txtUserNameR.setText("@"+myUserName);
                    optLayer.addView(tabletR);
                    tabletR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName, commentBox, postID, commentsLayout));
                    imageViewR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName, commentBox, postID, commentsLayout));
                    txtNameR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName, commentBox, postID, commentsLayout));
                    txtUserNameR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName, commentBox, postID, commentsLayout));
                    layR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName, commentBox, postID, commentsLayout));
                    for (int r = 0; r < userPages.length(); r++){
                        JSONObject pageInfo = new JSONObject(userPages.getString(r));
                        String pageId = pageInfo.getString("id");
                        if(Integer.parseInt(pageId) == Integer.parseInt(pagerId)) {
                            String pageName = pageInfo.getString("pageName");
                            String pagePhoto = www + pageInfo.getString("photo");
                            LinearLayout tablet = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.tablet, null, false);
                            LinearLayout lay = tablet.findViewById(R.id.lay);
                            ImageView imageView = tablet.findViewById(R.id.image);
                            TextView txtName = tablet.findViewById(R.id.name);
                            TextView txtUserName = tablet.findViewById(R.id.userName);
                            txtUserName.setVisibility(View.GONE);
                            imageLoader.displayImage(pagePhoto, imageView);
                            txtName.setText(pageName);
                            optLayer.addView(tablet);
                            tablet.setOnClickListener(v -> setCommenter(pageId, "page", pagePhoto, null, pageName, commentBox, postID, commentsLayout));
                            imageView.setOnClickListener(v -> setCommenter(pageId, "page", pagePhoto, null, pageName, commentBox, postID, commentsLayout));
                            txtName.setOnClickListener(v -> setCommenter(pageId, "page", pagePhoto, null, pageName, commentBox, postID, commentsLayout));
                            lay.setOnClickListener(v -> setCommenter(pageId, "page", pagePhoto, null, pageName, commentBox, postID, commentsLayout));
                        }
                    }
                    blackFade.addView(postOptView);
                    blackFade.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            } else if(poster == null) {
                poster = myIDtoString;
                posterUName = myUserName;
                posterName = myName;
                posterImg = myPht;
                postType = "profile";
            }
            boolean verif = userVerified;
            if(posterName == null)
                verif = false;
            String htmlText = Html.toHtml(commentBox.getText());
            commentBox.setText("");
            comment = HtmlParser.parseString(htmlText);
            String miniCom = HtmlParser.parseSpan(comment);
            miniCom = HtmlParser.parseBreaks(miniCom);
            miniCom = EmojiParser.parseToUnicode(miniCom);
            LinearLayout miniComLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.mini_comments, null);
            ImageView imgView = miniComLayout.findViewById(R.id.profPic);
            TextView commentTxtVw = miniComLayout.findViewById(R.id.commentView);
            imageLoader.displayImage(posterImg, imgView);
            htmlText = "<font><b>@"+posterUName+"</b> "+miniCom+"</font>";
            commentTxtVw.setText(Html.fromHtml(htmlText));
            commentsLayout.addView(miniComLayout);
            miniComLayouts.add(count, miniComLayout);
            comment = EmojiParser.parseToAliases(comment);
            JSONObject emitObj = new JSONObject();
            @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            try {
                TextView textView = (TextView) comsDisplay.get(postID);
                String text = textView.getText().toString();
                int num = Functions.convertToNumber(text);
                num++;
                text = Functions.convertToText(num);
                textView.setText(text);
                emitObj.put("user", poster);
                emitObj.put("postID", postID);
                emitObj.put("name", posterName);
                emitObj.put("userName", posterUName);
                emitObj.put("photo", posterImg);
                emitObj.put("verified", verif);
                emitObj.put("comment", comment);
                emitObj.put("type", postType);
                emitObj.put("date", date);
                emitObj.put("count", count);
                socket.emit("submitComment", emitObj);
                count++;
                resetPost();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setCommenter(String posterId, String posterType, String photo, String name, String uName, EditText commentBox, String postID, LinearLayout commentsLayout) {
        blackFade.setVisibility(View.GONE);
        poster = posterId;
        postType = posterType;
        posterImg = photo;
        posterName = name;
        posterUName = uName;
        submitComment(commentBox, postID, commentsLayout, false, null);
    }

    private void openComments(String postID, String commentID) {
        Intent intent = new Intent(HomeAct.this, CommentsActivity.class);
        Bundle userParams = new Bundle();
        userParams.putString("postID", postID);
        userParams.putString("commentID", commentID);
        intent.putExtras(userParams);
        startActivity(intent);
    }

    @SuppressLint("InflateParams")
    private void displayPostsOptions(String postID) throws JSONException {
        RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
        LinearLayout optLayer = postOptView.findViewById(R.id.optLayer);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        String optionArr = postsOptions.getString(postID);
        JSONObject optionsObj = new JSONObject(optionArr);
        JSONArray objKeys = optionsObj.names();
        for (int r = 0; r < Objects.requireNonNull(objKeys).length(); r++){
            TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
            String key = objKeys.getString(r);
            String option = optionsObj.getString(key);
            int drawableLeft = postsOptionsIcons.getInt(key);
            optionList.setText(option);
            optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
            optionList.setOnClickListener(v -> {
                try {
                    executePostOption(postID, key);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            optLayer.addView(optionList);
        }
        blackFade.addView(postOptView);
        blackFade.setVisibility(View.VISIBLE);
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat", "InflateParams"})
    private void executePostOption(String postId, String key) throws JSONException {
        JSONObject optionsValObj, optionsObj, emitObj;
        boolean val, newVal;
        String newTxt, optionArr, theName, retTxt, user, date;
        String[] retTxtArr;
        blackFade.setVisibility(View.GONE);
        switch (key){
            case "savePost":
                optionsValObj = postsOptionsVal.getJSONObject(postId);
                val = optionsValObj.getBoolean(key);
                newTxt = "Save Post";
                if(val) {
                    Toast.makeText(cntxt, "Post Unsaved", Toast.LENGTH_LONG).show();
                } else {
                    newTxt = "Unsave Post";
                    Toast.makeText(cntxt, "Post Saved", Toast.LENGTH_LONG).show();
                }
                newVal = !val;
                optionsValObj.put(key, newVal);
                postsOptionsVal.put(postId, optionsValObj);
                optionArr = postsOptions.getString(postId);
                optionsObj = new JSONObject(optionArr);
                optionsObj.put(key, newTxt);
                postsOptions.put(postId, optionsObj.toString());
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                emitObj = new JSONObject();
                emitObj.put("date", date);
                emitObj.put("user", myId);
                emitObj.put("postId", postId);
                emitObj.put("val", val);
                socket.emit("savePost", emitObj);
                break;
            case "unfollowPers":
                optionsValObj = postsOptionsVal.getJSONObject(postId);
                val = optionsValObj.getBoolean(key);
                optionArr = postsOptions.getString(postId);
                optionsObj = new JSONObject(optionArr);
                retTxt = optionsObj.getString(key);
                retTxtArr = retTxt.split(" ");
                theName = retTxt.substring(retTxtArr[0].length());
                newTxt = "Follow";
                if(val) {
                    Toast.makeText(cntxt, "Unfollowed"+theName, Toast.LENGTH_LONG).show();
                } else {
                    newTxt = "Unfollow";
                    Toast.makeText(cntxt, "Followed"+theName, Toast.LENGTH_LONG).show();
                }
                newTxt += theName;
                newVal = !val;
                optionsValObj.put(key, newVal);
                postsOptionsVal.put(postId, optionsValObj);
                optionsObj.put(key, newTxt);
                postsOptions.put(postId, optionsObj.toString());
                user = posters.getString(postId);
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                emitObj = new JSONObject();
                emitObj.put("myId", myId);
                emitObj.put("user", user);
                emitObj.put("val", val);
                emitObj.put("date", date);
                socket.emit("follow", emitObj);
                break;
            case "unfollowPage":
                optionsValObj = postsOptionsVal.getJSONObject(postId);
                val = optionsValObj.getBoolean(key);
                optionArr = postsOptions.getString(postId);
                optionsObj = new JSONObject(optionArr);
                retTxt = optionsObj.getString(key);
                retTxtArr = retTxt.split(" ");
                theName = retTxt.substring(retTxtArr[0].length());
                newTxt = "Follow";
                if(val) {
                    Toast.makeText(cntxt, "Unfollowed"+theName, Toast.LENGTH_LONG).show();
                } else {
                    newTxt = "Unfollow";
                    Toast.makeText(cntxt, "Followed"+theName, Toast.LENGTH_LONG).show();
                }
                newTxt += theName;
                newVal = !val;
                optionsValObj.put(key, newVal);
                postsOptionsVal.put(postId, optionsValObj);
                optionsObj.put(key, newTxt);
                postsOptions.put(postId, optionsObj.toString());
                user = posters.getString(postId);
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                emitObj = new JSONObject();
                emitObj.put("user", myId);
                emitObj.put("pageId", user);
                emitObj.put("val", val);
                emitObj.put("date", date);
                socket.emit("followPage", emitObj);
                break;
            case "notifyStatus":
                optionsValObj = postsOptionsVal.getJSONObject(postId);
                val = optionsValObj.getBoolean(key);
                optionArr = postsOptions.getString(postId);
                optionsObj = new JSONObject(optionArr);
                newTxt = "On notification for Post";
                if(val) {
                    Toast.makeText(cntxt, "Notification Turned Off", Toast.LENGTH_LONG).show();
                } else {
                    newTxt = "Off notification for Post";
                    Toast.makeText(cntxt, "Notification Turned On", Toast.LENGTH_LONG).show();
                }
                newVal = !val;
                optionsValObj.put(key, newVal);
                postsOptionsVal.put(postId, optionsValObj);
                optionsObj.put(key, newTxt);
                postsOptions.put(postId, optionsObj.toString());
                emitObj = new JSONObject();
                emitObj.put("user", myId);
                emitObj.put("postId", postId);
                emitObj.put("val", val);
                socket.emit("notifyStatus", emitObj);
                break;
            case "copyLink":
                CharSequence copyLink = Constants.postLinkUrl + postId;
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", copyLink);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(cntxt, "Link Copied to Clipboard", Toast.LENGTH_LONG).show();
                break;
            case "hidePost":
                View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                TextView txter = blockView.findViewById(R.id.txter);
                Button cnclBtn = blockView.findViewById(R.id.cancel);
                Button agreeBtn = blockView.findViewById(R.id.agree);
                txter.setText("Are you sure you want to hide this post?");
                emitObj = new JSONObject();
                agreeBtn.setOnClickListener(v -> {
                    blackFade.setVisibility(View.GONE);
                    selectedPosts.remove(postId);
                    try {
                        RelativeLayout relativeLayout = (RelativeLayout) postLayouts.get(postId);
                        runOnUiThread(() -> relativeLayout.setVisibility(View.GONE));
                        Toast.makeText(cntxt, "Post Hidden", Toast.LENGTH_LONG).show();
                        @SuppressLint("SimpleDateFormat") String date13 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        emitObj.put("date", date13);
                        emitObj.put("user", myId);
                        emitObj.put("postId", postId);
                        socket.emit("hidePost", emitObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
                blackFade.addView(blockView);
                blackFade.setVisibility(View.VISIBLE);
                break;
            case "editPost":
                Intent intent = new Intent(HomeAct.this, EditPostActivity.class);
                Bundle userParams = new Bundle();
                userParams.putString("postId", postId);
                userParams.putString("object", "{}");
                userParams.putInt("reqFrm", 0);
                intent.putExtras(userParams);
                startActivity(intent);
                break;
            case "deletePost":
                View blockViewX = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                TextView txterX = blockViewX.findViewById(R.id.txter);
                Button cnclBtnX = blockViewX.findViewById(R.id.cancel);
                Button agreeBtnX = blockViewX.findViewById(R.id.agree);
                txterX.setText("Are you sure you want to delete this post?");
                emitObj = new JSONObject();
                agreeBtnX.setOnClickListener(v -> {
                    blackFade.setVisibility(View.GONE);
                    selectedPosts.remove(postId);
                    try {
                        RelativeLayout relativeLayout = (RelativeLayout) postLayouts.get(postId);
                        runOnUiThread(() -> relativeLayout.setVisibility(View.GONE));
                        Toast.makeText(cntxt, "Post Moved to Trash", Toast.LENGTH_LONG).show();
                        @SuppressLint("SimpleDateFormat") String date12 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        emitObj.put("date", date12);
                        emitObj.put("user", myId);
                        emitObj.put("postId", postId);
                        socket.emit("deletePost", emitObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                cnclBtnX.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
                blackFade.addView(blockViewX);
                blackFade.setVisibility(View.VISIBLE);
                break;
            case "reportPost":
                String[] opts = new String[]{
                        "It's annoying",
                        "It's abusive",
                        "It's a spam",
                        "It contains illegal contents",
                        "It contains sexual explicit contents"
                };
                RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
                LinearLayout optLayer = postOptView.findViewById(R.id.optLayer);
                LinearLayout tvw = (LinearLayout) getLayoutInflater().inflate(R.layout.text_view, null);
                TextView txtHead = tvw.findViewById(R.id.head);
                TextView txtBody = tvw.findViewById(R.id.body);
                txtHead.setText("Help Us Understand What's Happening");
                txtBody.setText("What's wrong with this post?");
                optLayer.addView(tvw);
                emitObj = new JSONObject();
                for (int r = 0; r < opts.length; r++){
                    TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
                    String option = opts[r];
                    optionList.setText(option);
                    int finalR = r;
                    optionList.setOnClickListener(v -> {
                        blackFade.setVisibility(View.GONE);
                        Toast.makeText(cntxt, "Report Submitted", Toast.LENGTH_LONG).show();
                        try {
                            @SuppressLint("SimpleDateFormat") String date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            emitObj.put("date", date1);
                            emitObj.put("user", myId);
                            emitObj.put("dataId", postId);
                            emitObj.put("type", "post");
                            emitObj.put("reportIndex", finalR);
                            socket.emit("report", emitObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                    optLayer.addView(optionList);
                }
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                blackFade.addView(postOptView);
                blackFade.setVisibility(View.VISIBLE);
                break;
        }
    }

    private static void visitUserProfile(Context cntxt, int user) {
        Intent intent = new Intent(cntxt, ProfileAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("userID", user);
        intent.putExtras(userParams);
        cntxt.startActivity(intent);
    }

    private void setUploader(String posterId, String posterType, String photo, String name, String uName, boolean verified) {
        blackFade.setVisibility(View.GONE);
        poster = posterId;
        postType = posterType;
        posterImg = photo;
        posterName = name;
        posterUName = uName;
        uploadPost(verified);
    }

    public static void savePostEdit(Context cntxt, ArrayList<String> originalImages, ArrayList<File> addedImages, ArrayList<String> editedImages, String htmlText, String postId, String type, String pageId) throws JSONException {
        int totalFilesSize = originalImages.size() + addedImages.size();
        RelativeLayout postView = (RelativeLayout) postLayouts.get(postId);
        @SuppressLint("InflateParams") RelativeLayout progressView = (RelativeLayout) LayoutInflater.from(cntxt).inflate(R.layout.progress_bar, null, false);
        TextView postProgressText = progressView.findViewById(R.id.postProgressText);
        ProgressBar postProgressBar = progressView.findViewById(R.id.postProgressBar);
        LinearLayout postTextLayout = postView.findViewById(R.id.postTextLayout);
        LinearLayout postFilesLayout = postView.findViewById(R.id.postFilesLayout);
        ViewPager viewPager = postView.findViewById(R.id.viewPager);
        TextView pstText = postView.findViewById(R.id.postText);
        htmlText = HtmlParser.parseString(htmlText);
        String text = HtmlParser.parseSpan(htmlText);
        CharSequence sequence = Html.fromHtml(text);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span, cntxt);
        }
        pstText.setText(strBuilder);
        pstText.setMovementMethod(LinkMovementMethod.getInstance());
        String string = Jsoup.parse(htmlText).toString();
        if (StringUtils.isEmpty(string))
            postTextLayout.setVisibility(View.GONE);
        else
            postTextLayout.setVisibility(View.VISIBLE);
        progressView.setOnClickListener(v -> {
            return;
        });
        postView.addView(progressView);
        int postW = postView.getWidth();
        int postH = postView.getHeight();
        progressView.setLayoutParams(new RelativeLayout.LayoutParams(postW, postH));
        if(totalFilesSize == 0)
            postFilesLayout.setVisibility(View.GONE);
        else {
            int numOfImgs = totalFilesSize;
            if(totalFilesSize > 9)
                numOfImgs = 0;
            int numDis = numberOfImages[numOfImgs];
            View imgNum = postView.findViewById(R.id.imgNum);
            imgNum.setBackgroundResource(numDis);
        }
        MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (int g = 0; g < originalImages.size(); g++){
            if(!originalImages.get(g).startsWith("http")){
                File readFile = new File(originalImages.get(g));
                Uri uris = Uri.fromFile(readFile);
                String fileExt = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.toLowerCase());
                multipartBody.addFormDataPart("files[]", readFile.getName(), RequestBody.create(readFile, MediaType.parse(mimeType)));
            } else {
                String oldFile = originalImages.get(g);
                multipartBody.addFormDataPart("oldFiles[]", oldFile);
            }
        }
        for (int g = 0; g < addedImages.size(); g++){
            File readFile = addedImages.get(g);
            Uri uris = Uri.fromFile(readFile);
            String fileExt = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.toLowerCase());
            multipartBody.addFormDataPart("files[]", readFile.getName(), RequestBody.create(readFile, MediaType.parse(mimeType)));
        }
        multipartBody.addFormDataPart("user", myIDtoString)
                .addFormDataPart("post", htmlText)
                .addFormDataPart("postId", postId);

        @SuppressLint("SetTextI18n") final CountingRequestBody.Listener progressListener = (bytesRead, contentLength) -> {
            if (bytesRead < contentLength && contentLength > 0) {
                final int progress = (int)Math.round((((double) bytesRead / contentLength) * 100));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    postProgressBar.setProgress(progress, true);
                else
                    postProgressBar.setProgress(progress);
                postProgressText.setText(progress+"%");
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
                .url(Constants.editPostUrl)
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
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String mMessage = Objects.requireNonNull(response.body()).string();
                postProgressBar.setProgress(100, true);
                postProgressText.setText("100%");
                for(int c = 0; c < editedImages.size(); c++){
                    String filePath = editedImages.get(c);
                    File file = new File(filePath);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(file.toPath());
                    else
                        file.delete();
                }
                try {
                    JSONObject emitObj = new JSONObject();
                    JSONObject emitObjD = new JSONObject();
                    JSONObject postObj = new JSONObject(mMessage);
                    String postFiles = postObj.getString("files");
                    JSONArray addedMU = new JSONArray(postObj.getString("addedMU"));
                    JSONArray addedMP = new JSONArray(postObj.getString("addedMP"));
                    JSONArray removedMU = new JSONArray(postObj.getString("removedMU"));
                    JSONArray removedMP = new JSONArray(postObj.getString("removedMP"));
                    if((addedMU.length() + addedMP.length()) > 0) {
                        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        emitObj.put("user", myId);
                        emitObj.put("postId", postId);
                        emitObj.put("type", type);
                        emitObj.put("pageId", pageId);
                        emitObj.put("mentionedUsers", addedMU);
                        emitObj.put("mentionedPages", addedMP);
                        emitObj.put("date", date);
                        socket.emit("notifyTags", emitObj);
                    }
                    if((removedMU.length() + removedMP.length()) > 0) {
                        emitObjD.put("userX", myId);
                        emitObjD.put("postIdX", postId);
                        emitObjD.put("mentionedUsersX", removedMU);
                        emitObjD.put("mentionedPagesX", removedMP);
                        socket.emit("deleteTags", emitObjD);
                    }
                    //viewPager.removeAllViews();
                    if(totalFilesSize > 0){
                        JSONArray filesList = new JSONArray(postFiles);
                        postFilesLists = new ArrayList<>();
                        for (int j = 0; j < totalFilesSize; j++){
                            String filePath = www + filesList.getString(j);
                            postFilesLists.add(filePath);
                        }
                        Objects.requireNonNull(viewPager.getAdapter()).notifyDataSetChanged();
                        viewPager.setOffscreenPageLimit(totalFilesSize);
                        photoAdapter = new PhotoAdapter(postFilesLists, cntxt);
                        viewPager.setAdapter(photoAdapter);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                runOnUI(() -> progressView.setVisibility(View.GONE));
            }
        });
    }

    public void menuOptionSelectClick(String key){
        switch (key){
            case "actLog":
                startActivity(new Intent(this, ActsActivity.class));
                break;
            case "tags":
                startActivity(new Intent(this, TagsActivity.class));
                break;
            case "savedPosts":
                startActivity(new Intent(this, SavedpostsAct.class));
                break;
            case "hiddenPosts":
                startActivity(new Intent(this, HiddenpostsAct.class));
                break;
            case "deletedPosts":
                startActivity(new Intent(this, DeletedpostsAct.class));
                break;
            case "reportProblem":
                startActivity(new Intent(this, ReportAct.class));
                break;
            case "tac":
                startActivity(new Intent(this, TermsAct.class));
                break;
            case "theme":
                showThemes();
                break;
            case "languages":
                Toast.makeText(cntxt, "Languages not installed", Toast.LENGTH_LONG).show();
                break;
            case "clearCache":
                clearCache();
                break;
            case "logout":
                socket.emit("disconnected", myId);
                boolean availableSmartLogin = sharedPrefMngr.checkAvailableSmartLogin();
                pageOver = true;
                logoutLayer.setVisibility(View.VISIBLE);
                sharedPrefMngr.loggedOut();
                Intent intent = new Intent(this, LoginAct.class);
                SaveOpenedMessages.messageArray = null;
                SaveOpenedMessages.resetInstance();
                InboxFragment.firstLoad = true;
                if(availableSmartLogin)
                    intent = new Intent(this, SmartLoginAct.class);
                finish();
                startActivity(intent);
                finish();
                break;
            case "settings":
                startActivity(new Intent(this, SettingsAct.class));
                break;
            case "createPage":
                startActivity(new Intent(this, CreatePageAct.class));
                break;
            case "managePages":
                startActivity(new Intent(this, ManagePageAct.class));
                break;
        }
    }

    @SuppressLint("InflateParams")
    private void showThemes() {
        int color;
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        AtomicBoolean clickedTheme = new AtomicBoolean(false);
        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.themes, null, false);
        TextView lightThemeTxtVw = linearLayout.findViewById(R.id.lightTheme);
        TextView darkThemeTxtVw = linearLayout.findViewById(R.id.darkTheme);
        Button cancel = linearLayout.findViewById(R.id.cancel);
        Button save = linearLayout.findViewById(R.id.save);
        cancel.setOnClickListener((v) -> blackFade.setVisibility(View.GONE));
        if(darkThemeEnabled) {
            color = R.color.lightRed;
            darkThemeTxtVw.setTextColor(ContextCompat.getColor(cntxt, R.color.lightRed));
            darkThemeTxtVw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
        } else {
            color = R.color.green;
            lightThemeTxtVw.setTextColor(ContextCompat.getColor(cntxt, R.color.green));
            lightThemeTxtVw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
        }
        lightThemeTxtVw.setOnClickListener((v) -> {
            clickedTheme.set(false);
            lightThemeTxtVw.setTextColor(ContextCompat.getColor(cntxt, color));
            darkThemeTxtVw.setTextColor(ContextCompat.getColor(cntxt, R.color.ashBlack));
            darkThemeTxtVw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_false, 0, 0, 0);
            lightThemeTxtVw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
        });
        darkThemeTxtVw.setOnClickListener((v) -> {
            clickedTheme.set(true);
            darkThemeTxtVw.setTextColor(ContextCompat.getColor(cntxt, color));
            lightThemeTxtVw.setTextColor(ContextCompat.getColor(cntxt, R.color.ashBlack));
            lightThemeTxtVw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_false, 0, 0, 0);
            darkThemeTxtVw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
        });
        save.setOnClickListener((v) -> {
            blackFade.setVisibility(View.GONE);
            boolean selectedTheme = clickedTheme.get();
            if(!(selectedTheme == darkThemeEnabled))
                saveSelectedTheme(selectedTheme);
        });
        blackFade.addView(linearLayout);
        blackFade.setVisibility(View.VISIBLE);
    }

    @SuppressLint("InflateParams")
    private void saveSelectedTheme(boolean selectedTheme) {
        setTheme(R.style.AppTheme);
        sharedPrefMngr.enabledDarkTheme(selectedTheme, activity);
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.requesting_layout, null);
        TextView textView =  linearLayout.findViewById(R.id.textView);
        textView.setText(R.string.saving_theme);
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        blackFade.addView(linearLayout);
        blackFade.setVisibility(View.VISIBLE);
    }

    public static void restartApp(Activity activity){
        activity.startActivity(new Intent(activity, HomeAct.class));
        activity.finish();
    }

    @SuppressLint("SetTextI18n")
    public void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
            return;
        }
        if(!pageOver) {
            gridView = null;
            socket.emit("disconnected", myId);
            finish();
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        } else {
            if(curLayout == writeLayout){
                String writeUp = postTxt.getText().toString();
                if(itemLists.size() > 0 || !StringUtils.isEmpty(writeUp)) {
                    @SuppressLint("InflateParams") View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
                    if(blackFade.getChildCount() > 0){
                        blackFade.removeAllViews();
                    }
                    TextView txter = blockView.findViewById(R.id.txter);
                    Button cnclBtn = blockView.findViewById(R.id.cancel);
                    Button agreeBtn = blockView.findViewById(R.id.agree);
                    txter.setText("Are you sure you want to discard this post?");
                    agreeBtn.setOnClickListener(v -> {
                        resetPost();
                        blackFade.setVisibility(View.GONE);
                        pageOver = false;
                        curLayout.setVisibility(View.GONE);
                        curLayout = null;
                    });
                    cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
                    blackFade.addView(blockView);
                    blackFade.setVisibility(View.VISIBLE);
                } else{
                    pageOver = false;
                    curLayout.setVisibility(View.GONE);
                    curLayout = null;
                }
            } else {
                pageOver = false;
                curLayout.setVisibility(View.GONE);
                View fView = getCurrentFocus();
                if(fView instanceof EditText)
                    fView.clearFocus();
                curLayout = null;
            }
        }
    }

    private void deleteTempFiles() {
        try {
            for (int i = 0; i < editedImages.size(); i++){
                String tempFilePath = editedImages.get(i);
                File tempFile = new File(tempFilePath);
                if(tempFile.exists()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(tempFile.toPath());
                    else
                        tempFile.delete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetPost() {
        deleteTempFiles();
        if(hasPage) {
            postType = null;
            poster = null;
            posterUName = null;
            posterName = null;
            posterImg = null;
        }
        itemLists.clear();
        selectedImages.clear();
        editedImages.clear();
        runOnUiThread(() -> {
            ImageAdaptor imageAdapter = new ImageAdaptor(cntxt, new File[]{}, 0);
            postTxt.setText("");
            gridView.setAdapter(imageAdapter);
        });
    }

    public static void numberSelectedImages(GridView gridView, TextView selectionsTxt) {
        for (int b = 0; b < gridView.getChildCount(); b++){
            View childView = gridView.getChildAt(b);
            String filePth = (String) childView.getContentDescription();
            TextView numTxt = childView.findViewById(R.id.numTxt);
            if(selectedImages.contains(filePth)){
                int c = selectedImages.indexOf(filePth) + 1;
                String viewNum = String.valueOf(c);
                numTxt.setText(viewNum);
                numTxt.setVisibility(View.VISIBLE);
            } else {
                numTxt.setVisibility(View.GONE);
            }
        }
        if(selectedImages.size() > 0){
            String selectedNumTxt = String.valueOf(selectedImages.size());
            selectedNumTxt += " Selected";
            selectionsTxt.setText(selectedNumTxt);
        } else {
            selectionsTxt.setText("");
        }
    }

    private class ImageAdaptor extends BaseAdapter {

        private final Context cntxt;
        private final int imageWidth;
        File[] itemList;
        Bitmap bitmap;

        public ImageAdaptor(Context context, File[] itemList, int imageWidth) {
            this.itemList = itemList;
            this.imageWidth = imageWidth;
            cntxt = context;
        }

        @Override
        public int getCount() {
            return this.itemList.length;
        }

        @Override
        public Object getItem(int position) {
            return this.itemList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(cntxt).inflate(R.layout.image_box, parent, false);
            convertView.setLayoutParams(new GridView.LayoutParams(imageWidth ,imageWidth ));
            File thisFile = itemList[position];
            final String filePath = thisFile.getAbsolutePath();
            String fileType = Functions.checkFileType(filePath.toLowerCase());
            final ImageView imageView = convertView.findViewById(R.id.imgView);
            convertView.setContentDescription(filePath);
            convertView.setBackgroundResource(R.drawable.null_border);
            LinearLayout imgOpts = convertView.findViewById(R.id.imgOpts);
            CardView editPhoto = convertView.findViewById(R.id.editPhoto);
            CardView deletePhoto = convertView.findViewById(R.id.deletePhoto);
            editPhoto.setCardBackgroundColor(Color.parseColor("#99000000"));
            deletePhoto.setCardBackgroundColor(Color.parseColor("#99000000"));
            imgOpts.setVisibility(View.VISIBLE);
            
            if(fileType.equals("video")){
                LinearLayout video = convertView.findViewById(R.id.video);
                TextView videoTime = convertView.findViewById(R.id.videoTime);
                video.setVisibility(View.VISIBLE);
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(cntxt, Uri.fromFile(thisFile));
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMillisec = Long.parseLong(time);
                String vidTime = Functions.convertMilliTime(timeInMillisec);
                videoTime.setText(vidTime);
                try {
                    retriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            bitmap = Functions.decodeFiles(filePath, fileType, false);
            imageView.setImageBitmap(bitmap);
            editPhoto.setOnClickListener(v -> openPhotoEditor(cntxt, position, fileType));
            deletePhoto.setOnClickListener(v -> requestDeletePhoto(cntxt, position));

            return convertView;
        }
    }

    private static void openPhotoEditor(Context cntxt, int fileId, String fileType) {
        String thisFilePath = selectedImages.get(fileId);
        Intent intent = new Intent(cntxt, PhotoEditorAct.class);
        if(fileType.equals("video"))
            intent = new Intent(cntxt, VideoEditorAct.class);
        Bundle fileParams = new Bundle();
        fileParams.putInt("act", 0);
        fileParams.putInt("fileId", fileId);
        fileParams.putString("filePath", thisFilePath);
        intent.putExtras(fileParams);
        cntxt.startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    private void requestDeletePhoto(Context cntxt, int fileId) {
        File thisFile = itemLists.get(fileId);
        String thisFilePath = selectedImages.get(fileId);
        @SuppressLint("InflateParams") View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        TextView txter = blockView.findViewById(R.id.txter);
        Button cnclBtn = blockView.findViewById(R.id.cancel);
        Button agreeBtn = blockView.findViewById(R.id.agree);
        txter.setText("Are you sure you want to delete this photo?");
        agreeBtn.setOnClickListener(v -> {
            itemLists.remove(thisFile);
            selectedImages.remove(thisFilePath);
            if(editedImages.contains(thisFilePath) && thisFile.exists()){
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(thisFile.toPath());
                    else
                        thisFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                editedImages.remove(thisFilePath);
            }
            setImageGridViews(cntxt);
            blackFade.setVisibility(View.GONE);
            pageOver = false;
            curLayout = writeLayout;
        });
        cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
        blackFade.addView(blockView);
        blackFade.setVisibility(View.VISIBLE);
        pageOver = true;
        curLayout = blackFade;
    }

    public void setImageGridViews(Context cntxt) {
        File[] selectedFiles = new File[itemLists.size()];
        for (int x = 0; x < itemLists.size(); x++){
            File selectedFile = itemLists.get(x);
            selectedFiles[x] = selectedFile;
        }
        ImageAdaptor imageAdapter = new ImageAdaptor(cntxt, selectedFiles, imgW);
        gridView.setAdapter(imageAdapter);
    }

    public static void listenToNoteLoad(ProgressBar progressBar){
        progressBar.setVisibility(View.GONE);
        noteCount = 0;
        noteCV.setVisibility(View.INVISIBLE);
        emitPopper("notifications");
    }

    @SuppressLint("SetTextI18n")
    public void clearCache(){
        MemoryCache.checkSize();
        @SuppressLint("InflateParams") View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        TextView txter = blockView.findViewById(R.id.txter);
        Button cnclBtn = blockView.findViewById(R.id.cancel);
        Button agreeBtn = blockView.findViewById(R.id.agree);
        txter.setText("Are you sure you want to clear all cached photos?");
        agreeBtn.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            Toast.makeText(cntxt, "Cache Cleared", Toast.LENGTH_LONG).show();
            MemoryCache.clear();
            FileCache.clear();
            MemoryCache.checkSize();
        });
        cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
        blackFade.addView(blockView);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void loadMenuContents() {
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("lang", lang)
                .addFormDataPart("user", myIDtoString)
                .build();
        Request request = new Request.Builder()
                .url(Constants.menuUrl)
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        try(Response response = call.execute()) {
            if (response.isSuccessful()) {
                String responseString = Objects.requireNonNull(response.body()).string();
                progressBar.clearAnimation();
                progressBar.setVisibility(View.GONE);
                menuLoaded = true;
                JSONObject menuObj = new JSONObject(responseString);
                for(int i = 0; i < Objects.requireNonNull(menuObj.names()).length(); i++){
                    String menuKey = Objects.requireNonNull(menuObj.names()).getString(i);
                    String objVal = menuObj.getString(menuKey);
                    @SuppressLint("InflateParams") LinearLayout menuListView = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_list, null);
                    TextView menuListText = menuListView.findViewById(R.id.tcc);
                    menuListText.setText(objVal);
                    int bgIcon = drawableLefts.getInt(menuKey);
                    menuListText.setCompoundDrawablesWithIntrinsicBounds(bgIcon, 0, 0, 0);
                    menuListView.setOnClickListener(v -> menuOptionSelectClick(menuKey));
                    menuDis.addView(menuListView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openImage(ArrayList<String> allItems, int position, Context cntxt) {
        Intent intent = new Intent(cntxt, PhotoOpenerAct.class);
        Bundle fileParams = new Bundle();
        fileParams.putStringArrayList("arrayList", allItems);
        fileParams.putInt("index", position);
        intent.putExtras(fileParams);
        cntxt.startActivity(intent);
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText) && (!(view == scrllView || view == comScrllView))) {
            view.setOnTouchListener((v, event) -> {
                comScrllView.setVisibility(View.GONE);
                scrllView.setVisibility(View.GONE);
                hideSoftKeyboard(v);
                if(search.hasFocus())
                    search.clearFocus();
                if(curEditor != null && curEditor.hasFocus())
                    curEditor.clearFocus();
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
