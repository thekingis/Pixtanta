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
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import static com.pixtanta.android.Constants.socketUrl;
import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.HomeAct.updateMessageDelivery;

public class ProfileAct extends ThemeActivity {

    LinearLayout covPhtLayout, holder, viewHolder, saverL, foldLayout, boxer, blackFade, comMentionLayer;
    Context cntxt;
    static Activity activity;
    Button saveChange, discardChange;
    TextView userName, textName, follow, addFrnd;
    ImageButton openOtpions;
    ImageView prfPht, covPht, changeDpBtn, changeCpBtn;
    ProgressBar progressBar;
    ImageLoader imageLoader;
    ViewPager viewPager;
    TabLayout tabLayout;
    CardView changeDp, changeCp;
    ScrollView comScrllView;
    int[] tabIcons;
    int viewHolderH, tabH, friendToUser, followingUser, followPerm, verifiedIcon, pageCount;
    String lang, myIDtoString, userIDtoString, photo, coverPhoto, myPht, myName, myUserName, fCharName, profileName;
    static File file, selectedFile, CPFile, DPFile, CPFileCrop, DPFileCrop;
    static String tempDir;
    private int myId;
    RelativeLayout mainHomeView, photoHolder;
    HomeFragment homeFragment;
    PhotosFragment photosFragment;
    VideosFragment videosFragment;
    NestedScrollView nest;
    Socket socket;
    static int index;
    int user, width, height;
    ClipboardManager clipboard;
    boolean userVerified, blocked, verified, aBoolean, uploading, loadedPhotos, loadedVideos, pageLoaded, blocking;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    boolean shakeOpt;
    public JSONObject sockt = new JSONObject(), blockOpt = new JSONObject(), postLayouts = new JSONObject(), options = new JSONObject(), optionIcons = new JSONObject();
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
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
        pageLoaded = false;
        blocking = false;
        pageCounter++;
        pageCount = pageCounter;

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(ProfileAct.this, LoginAct.class));
            return;
        }
        tempDir = StorageUtils.getStorageDirectories(cntxt)[0] + "/Android/data/" + getApplicationContext().getPackageName() + "/tempFiles";
        if(!(new File(tempDir).exists()))
            new File(tempDir).mkdir();
        myId = sharedPrefMngr.getMyId();
        Bundle userParams = getIntent().getExtras();
        user = userParams.getInt("userID");
        tabIcons = new int[]{
                R.drawable.ic_posts,
                R.drawable.ic_photo_library,
                R.drawable.ic_video_library
        };

        try {
            options.put("message", "Send Message");
            options.put("report", "Report");
            options.put("block", "Block");
            optionIcons.put("message", R.drawable.ic_message);
            optionIcons.put("report", R.drawable.ic_report_post);
            optionIcons.put("block", R.drawable.ic_block);
            sockt.put("userFrom", myId);
            sockt.put("userTo", user);
            socket = IO.socket(socketUrl);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
                socket.emit("connected", myId);
                socket.emit("connectedProfilePages", sockt);
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
        userIDtoString = Integer.toString(user);
        myPht = www + sharedPrefMngr.getMyPht();
        myName = sharedPrefMngr.getMyName();
        myUserName = sharedPrefMngr.getMyUserName();
        userVerified = sharedPrefMngr.getMyVerification();

        mainHomeView = findViewById(R.id.mainHomeView);
        photoHolder = findViewById(R.id.photoHolder);
        boxer = findViewById(R.id.boxer);
        foldLayout = findViewById(R.id.foldLayout);
        blackFade = findViewById(R.id.blackFade);
        holder = findViewById(R.id.holder);
        saverL = findViewById(R.id.saverL);
        viewHolder = findViewById(R.id.viewHolder);
        covPhtLayout = findViewById(R.id.covPhtLayout);
        comScrllView = findViewById(R.id.comScrllView);
        comMentionLayer = findViewById(R.id.comMentionLayer);
        textName = findViewById(R.id.textName);
        userName = findViewById(R.id.userName);
        addFrnd = findViewById(R.id.addFrnd);
        follow = findViewById(R.id.follow);
        openOtpions = findViewById(R.id.openOtpions);
        progressBar = findViewById(R.id.progressBar);
        covPht = findViewById(R.id.covPht);
        prfPht = findViewById(R.id.prfPht);
        changeDpBtn = findViewById(R.id.changeDpBtn);
        changeCpBtn = findViewById(R.id.changeCpBtn);
        saveChange = findViewById(R.id.saveChange);
        discardChange = findViewById(R.id.discardChange);
        changeDp = findViewById(R.id.changeDp);
        changeCp = findViewById(R.id.changeCp);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        nest = findViewById(R.id.nest);
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
            if(!blocking)
                blackFade.setVisibility(View.GONE);
            return;
        });
        if(myId == user){
            boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
            if(darkThemeEnabled){
                changeDp.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryRed));
                changeCp.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryRed));
            }
            changeCp.setVisibility(View.VISIBLE);
            changeDp.setVisibility(View.VISIBLE);
            foldLayout.setVisibility(View.GONE);
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
        } else {
            openOtpions.setOnClickListener(v -> {
                try {
                    openProfileOptions();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            follow.setOnClickListener(v -> followUser(false));
            addFrnd.setOnClickListener(v -> addFriend(false, false));
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                int red;
                if(darkThemeEnabled)
                    red = ContextCompat.getColor(cntxt, R.color.lightRed);
                else
                    red = ContextCompat.getColor(cntxt, R.color.colorPrimaryRed);
                Objects.requireNonNull(tab.getIcon()).setColorFilter(red, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                int blash;
                if(darkThemeEnabled)
                    blash = ContextCompat.getColor(cntxt, R.color.ash);
                else
                    blash = ContextCompat.getColor(cntxt, R.color.blash);
                Objects.requireNonNull(tab.getIcon()).setColorFilter(blash, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        loadProfileContent();

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
        socket.on("friend", args -> {
            try {
                JSONObject emitObj = new JSONObject(args[0].toString());
                int userTo = emitObj.getInt("user");
                int newTag = emitObj.getInt("newTag");
                    if (userTo == myId && newTag == 1)
                        newTag = 2;
                    int finalNewTag = newTag;
                    runOnUiThread(() -> setFriend(finalNewTag));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        socket.on("follow", args -> {
            try {
                JSONObject emitObj = new JSONObject(args[0].toString());
                int newTag = emitObj.getInt("newTag");
                    runOnUiThread(() -> setFollow(newTag));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        socket.on("acceptFollow", args -> {
            try {
                JSONObject emitObj = new JSONObject(args[0].toString());
                    boolean accepted = emitObj.getBoolean("val");
                    int i = 0;
                    if (accepted)
                        i = 2;
                    int finalI = i;
                    runOnUiThread(() -> setFollow(finalI));
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
        socket.on("blocked", args -> {
            blocked = false;
            runOnUiThread(() -> {
                Toast.makeText(cntxt, "You have blocked "+fCharName, Toast.LENGTH_LONG).show();
                blackFade.setVisibility(View.GONE);
            });
        });

    }

    @SuppressLint("InflateParams")
    private void openProfileOptions() throws JSONException {
        RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
        LinearLayout optLayer =  postOptView.findViewById(R.id.optLayer);
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        JSONArray objKeys = options.names();
        for (int r = 0; r < Objects.requireNonNull(objKeys).length(); r++){
            TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
            String key = objKeys.getString(r);
            String option = options.getString(key);
            int drawableLeft = optionIcons.getInt(key);
            optionList.setText(option);
            optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
            optionList.setOnClickListener(v -> ProfileAct.this.executeProfileOption(key));
            optLayer.addView(optionList);
        }
        blackFade.addView(postOptView);
        blackFade.setVisibility(View.VISIBLE);
    }

    @SuppressLint("InflateParams")
    private void executeProfileOption(String key) {
        switch (key){
            case "message":
                openMessage();
                break;
            case "report":
                String thStr = "Help Us Understand What's Happening",
                        tbStr = "Why do you want to report " + fCharName + "?";
                String[] opts = new String[]{
                        fCharName + " is pretending to be someone else",
                        fCharName + "'s account is fake",
                        fCharName + " is using a fake name",
                        fCharName + " is posting inappropriate things",
                        fCharName + " is bullying or harassing me (or someone)",
                        fCharName + " is in trouble",
                        fCharName + "'s account have been hacked",
                };
                RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
                LinearLayout optLayer = postOptView.findViewById(R.id.optLayer);
                LinearLayout tvw = (LinearLayout) getLayoutInflater().inflate(R.layout.text_view, null);
                TextView txtHead = tvw.findViewById(R.id.head);
                TextView txtBody = tvw.findViewById(R.id.body);
                txtHead.setText(thStr);
                txtBody.setText(tbStr);
                optLayer.addView(tvw);
                for (int r = 0; r < opts.length; r++){
                    TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
                    String option = opts[r];
                    optionList.setText(option);
                    int finalR = r;
                    optionList.setOnClickListener(v -> {
                        blackFade.setVisibility(View.GONE);
                        Toast.makeText(cntxt, "Report Submitted", Toast.LENGTH_LONG).show();
                        try {
                            Toast.makeText(cntxt, "Report Sent", Toast.LENGTH_LONG).show();
                            JSONObject emitObj = new JSONObject();
                            @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            emitObj.put("date", date);
                            emitObj.put("user", myId);
                            emitObj.put("dataId", user);
                            emitObj.put("type", "profile");
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
            case "block":
                try {
                    stateBlocks();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @SuppressLint({"InflateParams", "ResourceAsColor", "SetTextI18n"})
    private void stateBlocks() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("unfollow", "Unfollow " + fCharName);
        object.put("unfriend", "Unfriend " + fCharName);
        object.put("tags", "Remove All Tags in Posts and Comments Between You and " + fCharName);
        object.put("comments", "Delete All Comments in Posts and Comments Between You and " + fCharName);
        object.put("likes", "Delete All Reactions in Posts and Comments Between You and " + fCharName);
        blockOpt.put("unfollow", true);
        blockOpt.put("unfriend", true);
        blockOpt.put("tags", false);
        blockOpt.put("comments", false);
        blockOpt.put("likes", false);
        RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
        LinearLayout optLayer =  postOptView.findViewById(R.id.optLayer);
        optLayer.setOnClickListener((v3) -> {return;});
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        JSONArray objKeys = object.names();
        for (int r = 0; r < Objects.requireNonNull(objKeys).length(); r++){
            int drawableLeft = R.drawable.ic_check_false;
            String key = objKeys.getString(r);
            String option = object.getString(key);
            TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
            if(key.equals("unfollow") || key.equals("unfriend"))
                drawableLeft = R.drawable.ic_check_true_red;
            else
                optionList.setOnClickListener(v -> {
                    try {
                        TextView textView = (TextView) v;
                        boolean selOpt = blockOpt.optBoolean(key);
                        int dLeft = R.drawable.ic_check_false;
                        if(!selOpt)
                            dLeft = R.drawable.ic_check_true_red;
                        textView.setCompoundDrawablesWithIntrinsicBounds(dLeft, 0, 0, 0);
                        blockOpt.put(key, !selOpt);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            optionList.setText(option);
            optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
            optLayer.addView(optionList);
        }
        Button button = new Button(cntxt);
        button.setPadding(20, 0, 20, 0);
        button.setBackgroundResource(R.color.colorPrimaryRed);
        button.setTextColor(ContextCompat.getColor(cntxt, R.color.white));
        button.setText("Continue");
        button.setOnClickListener((v) -> {
            String text = "<span style=\"color:#b00000;\"><b>Blocking " + fCharName + " will delete all activities between the both of you.</b></span><br>";
            text += "Are you sure you want to block " + fCharName + "?";
            View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
            if(blackFade.getChildCount() > 0)
                blackFade.removeAllViews();
            TextView txter = blockView.findViewById(R.id.txter);
            Button cnclBtn = blockView.findViewById(R.id.cancel);
            Button agreeBtn = blockView.findViewById(R.id.agree);
            Spanned spanned;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                spanned = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
            else
                spanned = Html.fromHtml(text);
            txter.setText(spanned);
            agreeBtn.setOnClickListener(v1 -> {
                try {
                    blockUser();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            cnclBtn.setOnClickListener(v2 -> blackFade.setVisibility(View.GONE));
            blackFade.addView(blockView);
            blackFade.setVisibility(View.VISIBLE);
        });
        optLayer.addView(button);
        blackFade.addView(postOptView);
        blackFade.setVisibility(View.VISIBLE);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void blockUser() throws JSONException {
        blocking = true;
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.requesting_layout, null);
        TextView textView =  linearLayout.findViewById(R.id.textView);
        textView.setText("Blocking...");
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        blackFade.addView(linearLayout);
        blackFade.setVisibility(View.VISIBLE);
        textName.setText("User Profile Not Found");
        textName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_warning, 0, 0, 0);
        prfPht.setImageResource(R.drawable.default_photo);
        foldLayout.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        userName.setVisibility(View.GONE);
        boxer.setVisibility(View.GONE);
        covPht.setImageDrawable(null);
        JSONObject emitObj = new JSONObject();
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        emitObj.put("date", date);
        emitObj.put("userFrom", myId);
        emitObj.put("userTo", user);
        emitObj.put("name", profileName);
        emitObj.put("blockOpt", blockOpt);
        emitObj.put("blocking", true);
        socket.emit("blockUser", emitObj);
    }

    private void addFriend(boolean free, boolean rejected) {
        int tag = (int) addFrnd.getTag();
        int newTag = 1;
        if(tag > 0){
            String text = "Are You Sure You Want To Unfriend " + fCharName;
            newTag = 0;
            if(tag == 2) {
                text = "Do You Want To Accept Friend Request From " + fCharName;
                newTag = 3;
                if(rejected)
                    newTag = 0;
            }
            if(tag == 1)
                text = "Do You Want To Cancel Friend Request To " + fCharName;
            if(!free){
                displayConfirm(text, true, tag);
                return;
            }
        }
        JSONObject emitObj = new JSONObject();
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            emitObj.put("myId", myId);
            emitObj.put("user", user);
            emitObj.put("date", date);
            emitObj.put("tag", tag);
            emitObj.put("newTag", newTag);
            emitObj.put("val", rejected);
            socket.emit("friend", emitObj);
            setFriend(newTag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void followUser(boolean free) {
        int tag = (int) follow.getTag();
        boolean val = false;
        int newTag = followPerm;
        if(tag > 0){
            val = true;
            newTag = 0;
            String text = "Are You Sure You Want To Unfollow " + fCharName;
            if(tag == 1)
                text = "Do You Want To Cancel Follow Request To " + fCharName;
            if(!free) {
                displayConfirm(text, false, -1);
                return;
            }
        }
        JSONObject emitObj = new JSONObject();
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            emitObj.put("myId", myId);
            emitObj.put("user", user);
            emitObj.put("date", date);
            emitObj.put("val", val);
            emitObj.put("newTag", newTag);
            emitObj.put("tag", tag);
            socket.emit("follow", emitObj);
            setFollow(newTag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void displayConfirm(String text, boolean ADMethod, int tag){
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
            if(ADMethod)
                addFriend(true, false);
            else
                followUser(true);
        });
        cnclBtn.setOnClickListener(v -> {
            if(tag == 2)
                addFriend(true, true);
            blackFade.setVisibility(View.GONE);
        });
        if(tag == 2){
            agreeBtn.setText("Accept");
            cnclBtn.setText("Reject");
        }
        blackFade.addView(blockView);
        blackFade.setVisibility(View.VISIBLE);
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
        multipartBody.addFormDataPart("id", String.valueOf(myId))
                .addFormDataPart("table", "accounts");

        @SuppressLint("SetTextI18n") final CountingRequestBody.Listener progressListener = (bytesRead, contentLength) -> {
            if(bytesRead >= contentLength && contentLength > 0) {
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
                String responseString = Objects.requireNonNull(response.body()).string();
                uploading = false;
                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        progressBar.setProgress(100, true);
                    else
                        progressBar.setProgress(100);
                    progressText.setText("100%");
                    progressView.setVisibility(View.GONE);
                });
                try {
                    JSONObject object = new JSONObject(responseString);
                    if(!(DPFileCrop == null)) {
                        String newPhoto = object.getString("DPFileCrop");
                        myPht = www + newPhoto;
                        HomeAct.myPht = myPht;
                        CommentsActivity.myPht = myPht;
                        RepliesActivity.myPht = myPht;
                        sharedPrefMngr.storeUserInfo(myId, newPhoto, myName, myUserName, userVerified);
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
                    DPFileCrop = null;
                    CPFileCrop = null;
                    CPFile = null;
                    DPFile = null;
                    file = null;
                    selectedFile = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void discardPhotoChange() {
        View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
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
        ProfileAct profileAct = new ProfileAct();
        selectedFile = image;
        index = i;
        @SuppressLint("SimpleDateFormat") String filePath,
                timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()),
                randStr = UUID.randomUUID().toString();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            RandomString gen = new RandomString(8, ThreadLocalRandom.current());
            randStr = gen.toString();
        }
        Uri fileUri = Uri.fromFile(image);
        filePath = tempDir + "/IMG_" + randStr + timeStamp +".jpg";
        file = new File(filePath);
        UCrop uCrop = UCrop.of(fileUri, Uri.fromFile(file));
        if(profileAct.index == 0){
            uCrop.withAspectRatio(1, 1);
        }
        if(profileAct.index == 1){
            uCrop.withAspectRatio(20, 9);
        }
        uCrop.start(activity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UCrop.REQUEST_CROP && !(data == null)) {
            Uri resultUri = UCrop.getOutput(data);
            handleCropper(resultUri);
        }
    }

    private void handleCropper(Uri resultUri) {
        saverL.setVisibility(View.VISIBLE);
        holder.setVisibility(View.GONE);
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
        params.putInt("activityReq", 0);
        intent.putExtras(params);
        startActivity(intent);
    }

    public void openMessage() {
        Intent intent = new Intent(this, MessageAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("user", user);
        intent.putExtras(userParams);
        startActivity(intent);
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void loadProfileContent(){
        try {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("myId", myIDtoString)
                    .addFormDataPart("user", userIDtoString)
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(Constants.profileUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseString = Objects.requireNonNull(response.body()).string();
                progressBar.setVisibility(View.GONE);
                holder.setVisibility(View.VISIBLE);
                JSONArray data = new JSONArray(responseString);
                blocked = data.getBoolean(0);
                String dataStr = data.getString(1);
                JSONObject obj = new JSONObject(dataStr);
                photo = www + obj.getString("photo");
                if(blocked){
                    textName.setText("User Profile Not Found");
                    textName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_warning, 0, 0, 0);
                    foldLayout.setVisibility(View.GONE);
                } else {
                    fCharName = obj.getString("fCharName");
                    coverPhoto = www + obj.getString("coverPhoto");
                    followPerm = obj.getInt("followPerm");
                    profileName = obj.getString("fName") + " " + obj.getString("lName");
                    int frndsNum = obj.getInt("frndsNum");
                    int fllwersNum = obj.getInt("fllwersNum");
                    int fllwingNum = obj.getInt("fllwingNum");
                    String frndsNumStr = Functions.convertToText(frndsNum);
                    String fllwersNumStr = Functions.convertToText(fllwersNum);
                    String fllwingNumStr = Functions.convertToText(fllwingNum);
                    verified = obj.getBoolean("verified");
                    boolean seeFrndList = obj.getBoolean("seeFrndList");
                    boolean seeFllwList = obj.getBoolean("seeFllwList");
                    boolean seeFllwgList = obj.getBoolean("seeFllwgList");
                    String friend = " - Friend";
                    String follower = " - Follower";
                    String following = " - Following";
                    if(frndsNum > 1)
                        friend += "s";
                    else
                        frndsNumStr = String.valueOf(frndsNum);
                    if(fllwersNum > 1)
                        follower += "s";
                    else
                        fllwersNumStr = String.valueOf(fllwersNum);
                    if(fllwingNum > 1)
                        following += "s";
                    else
                        fllwingNumStr = String.valueOf(fllwingNum);
                    JSONArray frndsArray = obj.getJSONArray("frndsArray");
                    JSONArray fllwersArray = obj.getJSONArray("fllwersArray");
                    JSONArray fllwingArray = obj.getJSONArray("fllwingArray");
                    imageLoader.displayImage(photo, prfPht);
                    imageLoader.displayImage(coverPhoto, covPht);
                    textName.setText(profileName);
                    userName.setText("@" + obj.getString("userName"));
                    if(verified)
                        textName.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                    followingUser = obj.getInt("followingUser");
                    friendToUser = obj.getInt("friendToUser");
                    setFollow(followingUser);
                    setFriend(friendToUser);
                    if(seeFrndList) {
                        LinearLayout layoutFrnd = (LinearLayout) getLayoutInflater().inflate(R.layout.list_header, null);
                        TextView headerFrnd =  layoutFrnd.findViewById(R.id.header);
                        TextView seeAllFrnd =  layoutFrnd.findViewById(R.id.seeAll);
                        headerFrnd.setText(frndsNumStr + friend);
                        if (frndsNum < 11)
                            seeAllFrnd.setVisibility(View.GONE);
                        else {
                            seeAllFrnd.setOnClickListener(v -> openLister("friends"));
                        }
                        boxer.addView(layoutFrnd);
                        if (frndsNum > 0)
                            setLister(frndsArray);
                    }
                    if(seeFllwList) {
                        LinearLayout layoutFllwer = (LinearLayout) getLayoutInflater().inflate(R.layout.list_header, null);
                        TextView headerFllwer =  layoutFllwer.findViewById(R.id.header);
                        TextView seeAllFllwer =  layoutFllwer.findViewById(R.id.seeAll);
                        headerFllwer.setText(fllwersNumStr + follower);
                        if (fllwersNum < 11)
                            seeAllFllwer.setVisibility(View.GONE);
                        else {
                            seeAllFllwer.setOnClickListener(v -> openLister("followers"));
                        }
                        boxer.addView(layoutFllwer);
                        if (fllwersNum > 0)
                            setLister(fllwersArray);
                    }
                    if(seeFllwgList) {
                        LinearLayout layoutFlling = (LinearLayout) getLayoutInflater().inflate(R.layout.list_header, null);
                        TextView headerFlling =  layoutFlling.findViewById(R.id.header);
                        TextView seeAllFlling =  layoutFlling.findViewById(R.id.seeAll);
                        headerFlling.setText(fllwingNumStr + following);
                        if (fllwingNum < 11)
                            seeAllFlling.setVisibility(View.GONE);
                        else {
                            seeAllFlling.setOnClickListener(v -> openLister("following"));
                        }
                        boxer.addView(layoutFlling);
                        if (fllwingNum > 0)
                            setLister(fllwingArray);
                    }
                    initializeViewPager();
                }
                imageLoader.displayImage(photo, prfPht);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setFriend(int level) {
        addFrnd.setTag(level);
        if(level > 1){
            String s = "Friends";
            int drw = R.drawable.ic_checked_white;
            if (level == 2) {
                s = "Respond";
                drw = R.drawable.ic_add_user_white;
            }
            addFrnd.setText(s);
            addFrnd.setBackgroundResource(R.color.colorPrimaryRed);
            addFrnd.setTextColor(ContextCompat.getColor(cntxt, R.color.white));
            addFrnd.setCompoundDrawablesWithIntrinsicBounds(drw, 0, 0, 0);
        } else {
            boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
            int color;
            if(darkThemeEnabled)
                color = R.color.ash;
            else
                color = R.color.blash;
            String s = "Pending";
            if (level == 0)
                s = "Add Friend";
            addFrnd.setText(s);
            addFrnd.setBackgroundResource(R.drawable.new_border);
            addFrnd.setTextColor(ContextCompat.getColor(cntxt, color));
            addFrnd.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_user, 0, 0, 0);
        }
    }

    private void openLister(String tab) {
        Intent intent = new Intent(cntxt, FFFAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("user", user);
        userParams.putString("tab", tab);
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
            holder.setOnClickListener(v -> visitUserProfile(userFX));
            listLayout.addView(layout);
        }
        boxer.addView(layoutFX);
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    private void setFollow(int level) {
        follow.setTag(level);
        if(level == 2) {
            follow.setText("Following");
            follow.setBackgroundResource(R.color.colorPrimaryRed);
            follow.setTextColor(ContextCompat.getColor(cntxt, R.color.white));
            follow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_followers_white, 0, 0, 0);
        } else {
            String s = "Follow";
            if(level == 1)
                s = "Pending";
            follow.setText(s);
            boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
            int color;
            if(darkThemeEnabled)
                color = R.color.ash;
            else
                color = R.color.blash;
            follow.setBackgroundResource(R.drawable.new_border);
            follow.setTextColor(ContextCompat.getColor(cntxt, color));
            follow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_followers, 0, 0, 0);
        }
    }

    private void initializeViewPager() {
        tabLayout.setupWithViewPager(viewPager);
        setupViewPager();
        setupTabIcons();
    }

    private void setupViewPager() {
        try {
            ArrayList<ContextData> contextDataArrayList = new ArrayList<>();
            ContextData contextData = new ContextData();
            contextData.context = cntxt;
            contextData.dataId = user;
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
            contextData.pageName = "";
            contextData.photo = "";
            contextData.pageCount = pageCount;
            contextDataArrayList.add(contextData);
            ContextData newContextData = contextDataArrayList.get(0);
            contextDataArrayList.remove(0);
            viewPager.setOffscreenPageLimit(3);
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
            homeFragment = new HomeFragment(newContextData);
            photosFragment = new PhotosFragment(newContextData);
            videosFragment = new VideosFragment(newContextData);
            adapter.addFragment(homeFragment, "POSTS");
            adapter.addFragment(photosFragment, "PHOTOS");
            adapter.addFragment(videosFragment, "VIDEOS");
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if(position == 1 && !loadedPhotos){
                        loadedPhotos = true;
                        photosFragment.getFileDisplay();
                    }
                    if(position == 2 && !loadedVideos){
                        loadedVideos = true;
                        videosFragment.getFileDisplay();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTabIcons() {
        for(int x = 0; x < tabIcons.length; x++){
            Objects.requireNonNull(tabLayout.getTabAt(x)).setIcon(tabIcons[x]);
        }
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        int red;
        if(darkThemeEnabled)
            red = ContextCompat.getColor(cntxt, R.color.lightRed);
        else
            red = ContextCompat.getColor(cntxt, R.color.colorPrimaryRed);
        if (tab != null)
            Objects.requireNonNull(tab.getIcon()).setColorFilter(red, PorterDuff.Mode.SRC_IN);
    }

    private void visitUserProfile(int user) {
        Intent intent = new Intent(cntxt, ProfileAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("userID", user);
        intent.putExtras(userParams);
        startActivity(intent);
    }

    public void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            if(!blocking)
                blackFade.setVisibility(View.GONE);
        } else if(viewPager.getCurrentItem() > 0){
            viewPager.setCurrentItem(0, true);
        } else {
            StaticSaver.removeObject(pageCount);
            StaticSaver.removeSocket(pageCount);
            socket.emit("disconnected", myId);
            socket.emit("removeProfilePage", sockt);
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
                hideSoftKeyboard(cntxt, v);
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
