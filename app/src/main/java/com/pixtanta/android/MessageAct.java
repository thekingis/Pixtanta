package com.pixtanta.android;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;

import com.pixtanta.android.Views.CustomScrollView;
import com.pixtanta.android.Utils.HtmlParser;
import com.pixtanta.android.Views.SliderView;
import com.pixtanta.android.Utils.StringUtils;

import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pixtanta.android.Utils.SpaceItemDecoration;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
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
import static com.pixtanta.android.HomeAct.updateMessageDelivery;
import static com.pixtanta.android.InboxAct.updatedArchs;
import static com.pixtanta.android.InboxAct.updatedBlocks;
import static com.pixtanta.android.InboxAct.updatedFavs;
import static com.pixtanta.android.InboxAct.updatedOffs;
import static com.pixtanta.android.InboxAct.updatedSounds;
import static com.pixtanta.android.SaveOpenedMessages.linkDatas;
import static com.pixtanta.android.SaveOpenedMessages.relativeObject;
import static com.pixtanta.android.SaveOpenedMessages.saveOpenedMessage;

public class MessageAct extends ThemeActivity {

    public Context cntxt;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    boolean shakeOpt;
    Activity actvty;
    String regex = "(^|\\s)((?:https?://|[a-z0-9-_]\\d{0,3}[.]|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))";
    ArrayList<String> allMediaList = new ArrayList<>();
    ArrayList<File> itemLists = new ArrayList<>();
    static ArrayList<String> selectedImages;
    String lang, fwdMsgId, myIDtoString, userIDtoString, myPht, name, userName, pronoun, photo, msgId, tempDir, audioRecordDir, fCharName, jsonString = "";
    String[] allFiles;
    ImageLoader imageLoader;
    private int myId;
    ImageView userPht;
    ProgressBar progressBar;
    EditText msgTxt, search;
    @SuppressLint("StaticFieldLeak")
    static GridView gridView;
    File[] allFilesArr;
    Animation rotation;
    MediaRecorder mediaRecorder = null;
    public static final int REPEAT_INTERVAL = 1;
    private Handler handler;
    ImageButton sendBtn, selectImages, recordAudio, curBtn, cancelRef, backBtn, closeTrash;
    TextView msgerName, msgerUName, recordTimer, refName, refDisplay, msgNum, txtHed;
    @SuppressLint("StaticFieldLeak")
    static TextView imageNum;
    RelativeLayout mainHomeView, lastMsgView, typingView, swipeToCancel;
    SliderView sliderView;
    CardView frwdMessage;
    LinearLayout messageDisplay, forward, fwdList, mediaLinksDisplay, blackFade, recordLayout, hed, mediaLinks, blockLayout, curAudioLength, menuLayoutBox, menuLayout, curAudioProgress, referLayout, header, msgMenu;
    ScrollView scrllViewX;
    CustomScrollView scrllView;
    VisualizerView visualizerView;
    View scrllToView;
    int screenWidth, audioMsgId = 0, fSize = 0, maxId = 0, inbox = 0, targetId = 0, width, imgW, sendBtnIcon, deleteBtnIcon, user, msgRef = 0, sentCnt = 0;
    boolean finalAccess, released = true, pressed = false, draggedOut = false, typing = false, isKeyboardShowing = false, lastMsgFromMe = false, maxReached = false, menuOptionsSet = false, loading = false, firstLoad = true, isRecording = false, isDragging = false;
    ImageAdaptor imageAdaptor;
    DisplayMetrics displayMetrics;
    Timer timer, audioTimer;
    double thirtyPer;
    long timeCounter, audioTimeCounter, audioDuration, audioWidth, audioProgressWidth;
    MediaPlayer mediaPlayer = null, popSound = null;
    JSONObject messageObject, optIcons = new JSONObject(), savedUrls = new JSONObject(), klind = new JSONObject(), selectedFwdrs = new JSONObject(), allers = new JSONObject(), menuTextViews = new JSONObject(), mediaPlayers = new JSONObject(), newLayouts = new JSONObject(), pendingMessages = new JSONObject(), jsonObject = new JSONObject(), excludedKeys = new JSONObject(), msgOptions = new JSONObject(), msgOptionVals = new JSONObject(), relObject = new JSONObject(), relObjectX = new JSONObject(), textViews = new JSONObject();
    Socket socket;
    LinksAdapter linksAdapter;
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    int evtY, evtX, iniY, iniX, oldY, oldX;
    JSONObject sockt = new JSONObject();
    SharedPrefMngr sharedPrefMngr;

    @SuppressLint({"ClickableViewAccessibility", "InflateParams", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        cntxt = this;
        actvty = this;
        sharedPrefMngr = new SharedPrefMngr(this);
        lang = sharedPrefMngr.getSelectedLanguage();
        tempDir = StorageUtils.getStorageDirectories(cntxt)[0] + "/Android/data/" + getApplicationContext().getPackageName() + "/tempFiles";

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(MessageAct.this, LoginAct.class));
            return;
        }
        handler = new Handler();
        imageLoader = new ImageLoader(this);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels - 5;
        width = screenWidth - 5;
        imgW = (width/3) - 10;
        myId = sharedPrefMngr.getMyId();
        myPht = Constants.www + sharedPrefMngr.getMyPht();
        Bundle userParams = getIntent().getExtras();
        user = userParams.getInt("user");
        myIDtoString = Integer.toString(myId);
        userIDtoString = Integer.toString(user);
        selectedImages = new ArrayList<>();

        try {
            messageObject = SaveOpenedMessages.getInstance().getOpenedMessage(user);
            sockt.put("userFrom", myId);
            sockt.put("userTo", user);
            optIcons.put("copy", R.drawable.ic_copy);
            optIcons.put("reply", R.drawable.ic_reply_ash);
            optIcons.put("forward", R.drawable.ic_forward);
            optIcons.put("delete", R.drawable.ic_delete);
            socket = IO.socket(socketUrl);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
                socket.emit("connected", myId);
                socket.emit("connectedMessagePages", sockt);
            }));
            socket.connect();
            excludedKeys.put("favourite", "favourite");
            excludedKeys.put("archive", "archive");
            excludedKeys.put("block", "block");
        } catch (JSONException | URISyntaxException e){
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
        mainHomeView =  findViewById(R.id.mainHomeView);
        msgerName =  findViewById(R.id.msgerName);
        msgerUName =  findViewById(R.id.msgerUName);
        sliderView =  findViewById(R.id.sliderView);
        imageNum =  findViewById(R.id.imageNum);
        recordTimer =  findViewById(R.id.recordTimer);
        refName =  findViewById(R.id.refName);
        refDisplay =  findViewById(R.id.refDisplay);
        msgNum =  findViewById(R.id.msgNum);
        txtHed =  findViewById(R.id.txtHed);
        progressBar =  findViewById(R.id.progressBar);
        userPht =  findViewById(R.id.userPht);
        swipeToCancel =  findViewById(R.id.swipeToCancel);
        msgMenu =  findViewById(R.id.msgMenu);
        sendBtn =  findViewById(R.id.sendBtn);
        closeTrash =  findViewById(R.id.closeTrash);
        selectImages =  findViewById(R.id.selectImages);
        recordAudio =  findViewById(R.id.recordAudio);
        cancelRef =  findViewById(R.id.cancelRef);
        backBtn =  findViewById(R.id.backBtn);
        msgTxt =  findViewById(R.id.msgTxt);
        search =  findViewById(R.id.search);
        gridView =  findViewById(R.id.gridView);
        header =  findViewById(R.id.header);
        forward =  findViewById(R.id.forward);
        fwdList =  findViewById(R.id.fwdList);
        blockLayout =  findViewById(R.id.blockLayout);
        messageDisplay =  findViewById(R.id.messageDisplay);
        blackFade =  findViewById(R.id.blackFade);
        recordLayout =  findViewById(R.id.recordLayout);
        menuLayoutBox =  findViewById(R.id.menuLayoutBox);
        menuLayout =  findViewById(R.id.menuLayout);
        referLayout =  findViewById(R.id.referLayout);
        mediaLinksDisplay =  findViewById(R.id.mediaLinksDisplay);
        mediaLinks =  findViewById(R.id.mediaLinks);
        hed =  findViewById(R.id.hed);
        scrllView =  findViewById(R.id.scrllView);
        scrllViewX =  findViewById(R.id.scrllViewX);
        frwdMessage =  findViewById(R.id.frwdMessage);
        frwdMessage.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryRed));
        visualizerView =  findViewById(R.id.visualizerView);
        sendBtnIcon = R.drawable.ic_send;
        deleteBtnIcon = R.drawable.ic_delete;
        rotation = AnimationUtils.loadAnimation(MessageAct.this, R.anim.rotate);
        rotation.setFillAfter(true);
        userPht.setOnClickListener(v -> {
            if(finalAccess){
                Intent intent = new Intent(MessageAct.this, ProfileAct.class);
                Bundle userParams1 = new Bundle();
                userParams1.putInt("userID", user);
                intent.putExtras(userParams1);
                startActivity(intent);
            }
        });
        msgMenu.setOnClickListener(v -> {
            if(!firstLoad && !(menuLayoutBox.getVisibility() == View.VISIBLE))
                menuLayoutBox.setVisibility(View.VISIBLE);
            else
                menuLayoutBox.setVisibility(View.GONE);
        });
        sendBtn.setOnClickListener(v -> {
            try {
                emitTyping(false);
                sendMessage();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        selectImages.setOnClickListener(v -> loadImages());
        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });
        cancelRef.setOnClickListener(v -> cancelMsgRef(true));
        recordAudio.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    hideSoftKeyboard(v);
                    menuLayoutBox.setVisibility(View.GONE);
                    startAudioRecording();
                    evtX = (int)event.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    evtX = (int)event.getX();
                    thirtyPer = screenWidth * 0.2;
                    if(evtX > (int) thirtyPer && isRecording)
                        stopAudioRecording();
                    break;
                case MotionEvent.ACTION_UP:
                    if(isRecording){
                        try {
                            sendRecordedAudio();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
            return true;
        });
        msgNum.setOnClickListener(v -> {
            inbox = 0;
            v.setVisibility(View.GONE);
            int scrllH = scrllView.getHeight(), viewH = messageDisplay.getHeight();
            int scrllTo = viewH - scrllH;
            scrllView.scrollTo(0, scrllTo);
            try {
                JSONObject emitObj = new JSONObject();
                emitObj.put("userFrom", myId);
                emitObj.put("userTo", user);
                emitObj.put("msgId", msgId);
                socket.emit("seenMsg", emitObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        backBtn.setOnClickListener(v -> mediaLinks.setVisibility(View.GONE));
        frwdMessage.setOnClickListener(v -> {
            try {
                sendForwardMessage();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        msgTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    boolean typingX = true;
                    String text = s.toString();
                    if(StringUtils.isEmpty(text))
                        typingX = false;
                    emitTyping(typingX);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().toLowerCase();
                if(!StringUtils.isEmpty(text)) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        JSONArray jsonArray = allers.names();
                        JSONArray splitText = new JSONArray(text.split(" "));
                        String fWord = splitText.getString(0);
                        splitText.remove(0);
                        if (jsonArray != null) {
                            for(int i = 0; i < jsonArray.length(); i++){
                                boolean found = true;
                                String key = jsonArray.getString(i);
                                JSONArray allSearch = allers.getJSONArray(key);
                                String userName = allSearch.getString(0).toLowerCase();
                                String fName = allSearch.getString(1).toLowerCase();
                                String lName = allSearch.getString(2).toLowerCase();
                                boolean userNameS = userName.startsWith(fWord);
                                boolean fNameS = fName.startsWith(fWord);
                                boolean lNameS = lName.startsWith(fWord);
                                if(userNameS || fNameS || lNameS){
                                    for(int x = 0; x < splitText.length(); x++){
                                        String ffWord = splitText.getString(0);
                                        boolean userNameW = userName.startsWith(ffWord);
                                        boolean fNameW = fName.startsWith(ffWord);
                                        boolean lNameW = lName.startsWith(ffWord);
                                        if((userNameS && (fNameW || lNameW)) || (fNameS && (userNameW || lNameW)) || (lNameS && (userNameW || fNameW))) {
                                            if(x > 0){
                                                String fffWord = splitText.getString(1);
                                                if((userNameS && fNameW && lName.startsWith(fffWord)) || (userNameW && fNameS && lName.startsWith(fffWord))){
                                                    if(splitText.length() > 2){
                                                        JSONArray jsonArr = new JSONArray(lName.split(" "));
                                                        splitText.remove(1);
                                                        splitText.remove(0);
                                                        jsonArr.remove(0);
                                                        for(int o = 0; o < jsonArr.length(); o++){
                                                            String nextWord = jsonArr.getString(o);
                                                            String newWord = splitText.getString(o);
                                                            if(!(nextWord.equals(newWord)))
                                                                found = false;
                                                        }
                                                    }
                                                }
                                                if((userNameS && lNameW && fName.startsWith(fffWord)) || (userNameW && lNameS && fName.startsWith(fffWord))){
                                                    if(splitText.length() > 2){
                                                        JSONArray jsonArr = new JSONArray(fName.split(" "));
                                                        splitText.remove(1);
                                                        splitText.remove(0);
                                                        jsonArr.remove(0);
                                                        for(int o = 0; o < jsonArr.length(); o++){
                                                            String nextWord = jsonArr.getString(o);
                                                            String newWord = splitText.getString(o);
                                                            if(!(nextWord.equals(newWord)))
                                                                found = false;
                                                        }
                                                    }
                                                }
                                                if((lNameS && fNameW) || (lNameW && fNameS)){
                                                    if(!userName.startsWith(fffWord))
                                                        found = false;
                                                }
                                            }
                                        } else
                                            found = false;
                                    }
                                } else
                                    found = false;
                                if(found)
                                    jsonObject.put(userName, "");
                            }
                        }
                        for(int a = 0; a < fwdList.getChildCount(); a++){
                            LinearLayout linearLayout = (LinearLayout) fwdList.getChildAt(a);
                            String tag = linearLayout.getTag().toString();
                            if(jsonObject.has(tag))
                                linearLayout.setVisibility(View.VISIBLE);
                            else
                                linearLayout.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    for(int a = 0; a < fwdList.getChildCount(); a++){
                        LinearLayout linearLayout = (LinearLayout) fwdList.getChildAt(a);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mainHomeView.getViewTreeObserver().addOnGlobalLayoutListener(
                () -> {

                    Rect r = new Rect();
                    mainHomeView.getWindowVisibleDisplayFrame(r);
                    int screenHeight = mainHomeView.getRootView().getHeight();

                    // r.bottom is the position above soft keypad or device button.
                    // if keypad is shown, the r.bottom is smaller than that before.
                    int keypadHeight = screenHeight - r.bottom;

                    if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                        // keyboard is opened
                        if (!isKeyboardShowing) {
                            isKeyboardShowing = true;
                            onKeyboardVisibilityChanged(true);
                        }
                    }
                    else {
                        // keyboard is closed
                        if (isKeyboardShowing) {
                            isKeyboardShowing = false;
                            onKeyboardVisibilityChanged(false);
                        }
                    }
                });
        setupUI(mainHomeView);
        nonStopCounter();
        new android.os.Handler().postDelayed(this::loadMessages, 100);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrllView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int scrllH = scrllView.getHeight(), viewH = messageDisplay.getHeight();
                int scrllTo = viewH - scrllH;
                if(scrllTo == scrollY){
                    inbox = 0;
                    msgNum.setVisibility(View.GONE);
                }
                if(!firstLoad){
                    if(scrollY == 0 && !maxReached && !loading){
                        loading = true;
                        progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.image_loader, null);
                        messageDisplay.addView(progressBar, 0);
                        progressBar.post(this::loadMessages);
                    }
                }
            });
        }

        socket.on("submitMessage", args -> {
            try {
                JSONObject msgData = new JSONObject(args[0].toString());
                int theMsgId = msgData.getInt("msgId");
                long time = msgData.getLong("time");
                if(theMsgId == Integer.parseInt(msgId) && relativeObject.isNull(String.valueOf(time))){
                    String id = msgData.getString("id");
                    jsonObject.put(id, msgData);
                    int userFrom = msgData.getInt("userFrom");
                    String msgBody = msgData.getString("msgBody");
                    int msgRefX = msgData.getInt("msgRef");
                    int dlvd = msgData.getInt("dlvd");
                    int seen = msgData.getInt("seen");
                    String msgRefData = msgData.getString("msgRefData");
                    String filesStr = msgData.getString("newFiles");
                    String date = msgData.getString("date");
                    boolean audioType = msgData.getBoolean("audioType");
                    boolean hasLink = hasLink(msgBody);
                    String newFiles = "";
                    if(!StringUtils.isEmpty(filesStr)) {
                        JSONArray jsonArray = new JSONArray(filesStr.split(","));
                        newFiles = jsonArray.toString();
                    }
                    boolean deletable = false;
                    boolean copyable = true;
                    lastMsgFromMe = false;
                    if(userFrom == myId) {
                        deletable = true;
                        lastMsgFromMe = true;
                    }
                    boolean finalDeletable = deletable;
                    messageObject.put("lastMsgFromMe", lastMsgFromMe);
                    JSONObject ject = new JSONObject();
                    ject.put("id", id);
                    ject.put("userFrom", userFrom);
                    ject.put("msgBody", msgBody);
                    ject.put("msgRef", msgRefX);
                    ject.put("msgRefData", msgRefData);
                    ject.put("dlvd", dlvd);
                    ject.put("seen", seen);
                    ject.put("files", newFiles);
                    ject.put("audioType", audioType);
                    ject.put("hasLink", hasLink);
                    ject.put("date", date);
                    ject.put("time", time);
                    ject.put("deletable", deletable);
                    ject.put("linkData", "");
                    String msgDta = ject.toString(0);
                    String data = messageObject.getString("data");
                    boolean dragLeft = false;
                    JSONObject dataObject = new JSONObject(data);
                    dataObject.put(id, msgDta);
                    data = dataObject.toString(0);
                    messageObject.put("data", data);
                    saveMessageContent();
                    if(typing) {
                        messageDisplay.removeView(typingView);
                        typing = false;
                    }
                    if (!(lastMsgView == null)) {
                        @SuppressLint("CutPasteId") ImageButton msgSeenX = lastMsgView.findViewById(R.id.msgSeen);
                        runOnUiThread(() -> msgSeenX.setBackgroundResource(R.color.white));
                    }
                    RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.ash_message, null);
                    String image = photo;
                    if (userFrom == myId) {
                        dragLeft = true;
                        lastMsgFromMe = true;
                        relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.red_message, null);
                        image = myPht;
                        ImageButton msgSeen = relativeLayout.findViewById(R.id.msgSeen);
                        msgSeen.setBackgroundResource(R.drawable.ic_msg_sent);
                        lastMsgView = relativeLayout;
                    } else {
                        lastMsgFromMe = false;
                        updateMessageDelivery(myId);
                        playMessagePop();
                    }
                    relativeLayout.setTag(time);
                    relObject.put(id, relativeLayout);
                    ImageView imageView =  relativeLayout.findViewById(R.id.profPic);
                    TextView textView =  relativeLayout.findViewById(R.id.message);
                    @SuppressLint("CutPasteId") TextView dateTxt =  relativeLayout.findViewById(R.id.date);
                    RelativeLayout messageHolder =  relativeLayout.findViewById(R.id.messageHolder);
                    LinearLayout linkDisplay =  relativeLayout.findViewById(R.id.linkDisplay);
                    if (!StringUtils.isEmpty(msgBody))
                        textView.setText(msgBody);
                    else
                        copyable = false;
                    boolean finalCopyable = copyable;
                    dateTxt.setText("Just Now");
                    dateTxt.setTag(time);
                    imageLoader.displayImage(image, imageView);
                    if (hasLink) {
                        JSONArray rArr;
                        String firstLink = getFirstLink(msgBody);
                        if(!(savedUrls.has(firstLink)))
                            rArr = new JSONArray();
                        else
                            rArr = savedUrls.getJSONArray(firstLink);
                        rArr.put(relativeLayout);
                        savedUrls.put(firstLink, rArr);
                        getLinkContent(firstLink);
                    } else {
                        linkDisplay.setVisibility(View.GONE);
                    }
                    if (msgRefX > 0) {
                        LinearLayout messageRef =  relativeLayout.findViewById(R.id.messageRef);
                        TextView msgRefName =  relativeLayout.findViewById(R.id.msgRefName);
                        TextView msgRefDisplay =  relativeLayout.findViewById(R.id.msgRefDisplay);
                        messageRef.setVisibility(View.VISIBLE);
                        JSONObject object = new JSONObject(msgRefData);
                        String userFromRef = object.getString("userFrom");
                        String msgBodyRef = object.getString("msgBody");
                        String filesStrRef = object.getString("files");
                        boolean audioTypeRef = object.getBoolean("type");
                        int drawableLeft = 0;
                        String textFrom = "You";
                        if (Integer.parseInt(userFromRef) == user)
                            textFrom = name;
                        if (!StringUtils.isEmpty(filesStrRef)) {
                            boolean fileImg = false, fileVid = false;
                            String[] filesRef = filesStrRef.split(",");
                            for (String s : filesRef) {
                                String fileType = Functions.checkFileType(s);
                                
                                if (fileType.equals("image"))
                                    fileImg = true;
                                if (fileType.equals("video"))
                                    fileVid = true;
                            }
                            if (fileVid) {
                                if (StringUtils.isEmpty(msgBodyRef))
                                    msgBodyRef = "Video";
                                drawableLeft = R.drawable.ic_video_black;
                            }
                            if (fileImg) {
                                if (StringUtils.isEmpty(msgBodyRef)) {
                                    msgBodyRef = "Photo";
                                    if (fileVid)
                                        msgBodyRef += " / Video";
                                }
                                drawableLeft = R.drawable.ic_cam;
                            }
                        }
                        if (audioTypeRef) {
                            msgBodyRef = "Recorded Audio";
                            drawableLeft = R.drawable.ic_mic;
                        }
                        msgRefName.setText(textFrom);
                        msgRefDisplay.setText(msgBodyRef);
                        msgRefDisplay.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
                        messageRef.setOnClickListener(v -> {
                            try {
                                scrollToReferredMessage(msgRef);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        });
                        msgRefName.setOnClickListener(v -> {
                            try {
                                scrollToReferredMessage(msgRef);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        });
                        msgRefDisplay.setOnClickListener(v -> {
                            try {
                                scrollToReferredMessage(msgRef);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        });
                        messageRef.setOnLongClickListener(v -> {
                            openMessageOptions(id, finalCopyable, finalDeletable, dateTxt, textView, true);
                            return true;
                        });
                    }
                    if (!StringUtils.isEmpty(filesStr)) {
                        if (audioType) {
                            RelativeLayout audioView =  relativeLayout.findViewById(R.id.audioView);
                            TextView audioTimer =  relativeLayout.findViewById(R.id.audioTimer);
                            ImageButton audioPlayPause =  relativeLayout.findViewById(R.id.audioPlayPause);
                            LinearLayout audioLength =  relativeLayout.findViewById(R.id.audioLength);
                            LinearLayout audioProgress =  relativeLayout.findViewById(R.id.audioProgress);
                            audioView.setVisibility(View.VISIBLE);
                            String audioFile = Constants.www + filesStr;
                            String audioTime = Functions.getMediaTime(audioFile);
                            audioTimer.setText(audioTime);
                            audioPlayPause.setOnClickListener(v -> playPauseAudio(audioPlayPause, audioLength, audioProgress, audioFile, id));
                            audioView.setOnLongClickListener(v -> {
                                openMessageOptions(id, finalCopyable, finalDeletable, dateTxt, textView, true);
                                return true;
                            });
                        } else {
                            GridView filesGrid =  relativeLayout.findViewById(R.id.filesGrid);
                            filesGrid.setVisibility(View.VISIBLE);
                            String[] files = filesStr.split(",");
                            int filesSize = files.length;
                            int rem = 0;
                            int newW = imgW - 20;
                            int gridH = newW;
                            int chosenSize = filesSize;
                            if (chosenSize > 4) {
                                chosenSize = 4;
                                rem = filesSize - 3;
                            }
                            if (chosenSize > 2)
                                gridH += newW;
                            if (userFrom == user) {
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                layoutParams.setMargins(0, 0, 180, 0);
                                messageHolder.setLayoutParams(layoutParams);
                                messageHolder.requestLayout();
                            }
                            filesGrid.getLayoutParams().height = gridH;
                            String[] msgFiles = new String[chosenSize];
                            ArrayList<String> strings = new ArrayList<>();
                            for (int i = 0; i < filesSize; i++) {
                                String fileUrl = Constants.www + files[i];
                                strings.add(fileUrl);
                                if (i < chosenSize)
                                    msgFiles[i] = fileUrl;
                            }
                            imageAdaptor = new ImageAdaptor(cntxt, itemLists, msgFiles, newW, false, rem, strings, filesSize);
                            filesGrid.setAdapter(imageAdaptor);
                            filesGrid.setOnLongClickListener(v -> {
                                openMessageOptions(id, finalCopyable, finalDeletable, dateTxt, textView, true);
                                return true;
                            });
                        }
                    }
                    textView.setOnLongClickListener(v -> {
                        openMessageOptions(id, finalCopyable, finalDeletable, dateTxt, textView, true);
                        return true;
                    });
                    messageHolder.setOnLongClickListener(v -> {
                        openMessageOptions(id, finalCopyable, finalDeletable, dateTxt, textView, true);
                        return true;
                    });
                    boolean finalDragLeft = dragLeft;
                    RelativeLayout finalRelativeLayout = relativeLayout;
                    relativeLayout.setOnTouchListener((v, event) -> {
                        hideSoftKeyboard(v);
                        listenToMessageDrag(finalRelativeLayout, id, finalDragLeft, event);
                        return false;
                    });
                    setDragForAllViews(relativeLayout, relativeLayout, id, finalDragLeft);
                    runOnUiThread(() -> messageDisplay.addView(finalRelativeLayout));
                    int scrllH = scrllView.getHeight(), viewH = messageDisplay.getHeight();
                    int scrllTo = viewH - scrllH, scrllY = scrllView.getScrollY();
                    if (scrllTo > scrllY) {
                        if (!(userFrom == myId)) {
                            inbox++;
                            msgNum.setVisibility(View.VISIBLE);
                            msgNum.setText(inbox);
                        }
                    } else {
                        relativeLayout.post(() -> {
                            boolean b = true;
                            if (userFrom == myId)
                                b = false;
                            scrollToView(finalRelativeLayout, b);
                            if (!(userFrom == myId)) {
                                try {
                                    JSONObject emitObj = new JSONObject();
                                    emitObj.put("userFrom", myId);
                                    emitObj.put("userTo", user);
                                    emitObj.put("msgId", msgId);
                                    socket.emit("seenMsg", emitObj);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        socket.on("submitMessageId", args -> {
            try {
                JSONObject msgData = new JSONObject(args[0].toString());
                long time = msgData.getLong("time");
                if(!pendingMessages.isNull(String.valueOf(time)))
                    return;
                String id = msgData.getString("id");
                String sentCntX = msgData.getString("sentCnt");
                int userFrom = msgData.getInt("userFrom");
                String msgBody = msgData.getString("msgBody");
                int msgRefX = msgData.getInt("msgRef");
                int dlvd = msgData.getInt("dlvd");
                int seen = msgData.getInt("seen");
                int fs = msgData.getInt("fSize");
                fSize -= fs;
                String msgRefData = msgData.getString("msgRefData");
                String filesStr = msgData.getString("files");
                String date = msgData.getString("date");
                boolean audioType = msgData.getBoolean("audioType");
                boolean hasLink = hasLink(msgBody);
                boolean deletable = true;
                jsonObject.put(id, msgData);
                JSONObject ject = new JSONObject();
                ject.put("id", id);
                ject.put("userFrom", userFrom);
                ject.put("msgBody", msgBody);
                ject.put("msgRef", msgRefX);
                ject.put("msgRefData", msgRefData);
                ject.put("dlvd", dlvd);
                ject.put("seen", seen);
                ject.put("files", filesStr);
                ject.put("audioType", audioType);
                ject.put("hasLink", hasLink);
                ject.put("date", date);
                ject.put("time", time);
                ject.put("deletable", deletable);
                ject.put("linkData", "");
                String msgDta = ject.toString(0);
                String data = messageObject.getString("data");
                JSONObject dataObject;
                if(!StringUtils.isEmpty(data))
                    dataObject = new JSONObject(data);
                else
                    dataObject = new JSONObject();
                dataObject.put(id, msgDta);
                data = dataObject.toString(0);
                messageObject.put("data", data);
                saveMessageContent();
                if(!relObjectX.isNull(sentCntX) || !relativeObject.isNull(String.valueOf(time))) {
                    RelativeLayout relativeLayout = (RelativeLayout) newLayouts.get(String.valueOf(time));
                    relObject.put(id, relativeLayout);
                    relObjectX.remove(id);
                    newLayouts.remove(String.valueOf(time));
                    relativeObject.remove(String.valueOf(time));
                    klind.remove(String.valueOf(time));
                    if (hasLink) {
                        JSONArray rArr;
                        String firstLink = getFirstLink(msgBody);
                        if(!(savedUrls.has(firstLink)))
                            rArr = new JSONArray();
                        else
                            rArr = savedUrls.getJSONArray(firstLink);
                        rArr.put(relativeLayout);
                        savedUrls.put(firstLink, rArr);
                        getLinkContent(firstLink);
                    }
                    boolean copyable = true;
                    if(StringUtils.isEmpty(msgBody))
                        copyable = false;
                    @SuppressLint("CutPasteId") TextView dateTxtV =  relativeLayout.findViewById(R.id.date);
                    ImageButton msgSeen =  relativeLayout.findViewById(R.id.msgSeen);
                    LinearLayout messageRef =  relativeLayout.findViewById(R.id.messageRef);
                    RelativeLayout audioView =  relativeLayout.findViewById(R.id.audioView);
                    GridView filesGrid =  relativeLayout.findViewById(R.id.filesGrid);
                    TextView textView =  relativeLayout.findViewById(R.id.message);
                    RelativeLayout messageHolder =  relativeLayout.findViewById(R.id.messageHolder);
                    boolean finalCopyable = copyable;
                    runOnUiThread(() -> {
                        msgSeen.setBackgroundResource(R.drawable.ic_msg_sent);
                        messageRef.setOnLongClickListener(v -> {
                            openMessageOptions(id, finalCopyable, deletable, dateTxtV, textView, true);
                            return true;
                        });
                        audioView.setOnLongClickListener(v -> {
                            openMessageOptions(id, finalCopyable, deletable, dateTxtV, textView, true);
                            return true;
                        });
                        filesGrid.setOnLongClickListener(v -> {
                            openMessageOptions(id, finalCopyable, deletable, dateTxtV, textView, true);
                            return true;
                        });
                        textView.setOnLongClickListener(v -> {
                            openMessageOptions(id, finalCopyable, deletable, dateTxtV, textView, true);
                            return true;
                        });
                        messageHolder.setOnLongClickListener(v -> {
                            openMessageOptions(id, finalCopyable, deletable, dateTxtV, textView, true);
                            return true;
                        });
                        relativeLayout.setOnTouchListener((v, event) -> {
                            hideSoftKeyboard(v);
                            listenToMessageDrag(relativeLayout, id, true, event);
                            return false;
                        });
                        setDragForAllViews(relativeLayout, relativeLayout, id, true);
                    });
                }
            } catch (JSONException | MalformedURLException e) {
                e.printStackTrace();
            }
        });
        socket.on("deleteMessage", args -> {
            try {
                JSONObject argsArr = new JSONObject(args[0].toString());
                String id = argsArr.getString("id");
                int userP = argsArr.getInt("userFrom");
                String theMsgId = argsArr.getString("msgId");
                boolean deletable = argsArr.getBoolean("deletable");
                if(Integer.parseInt(theMsgId) == Integer.parseInt(msgId) && !relObject.isNull(id) && (deletable || userP == myId)) {
                    lastMsgFromMe = argsArr.getBoolean("lastMsgFromMe");
                    RelativeLayout relativeLayout = (RelativeLayout) relObject.get(id);
                    relObject.remove(id);
                    String data = messageObject.getString("data");
                    JSONObject dataObject = new JSONObject(data);
                    JSONArray dataKeys = dataObject.names();

                    int lastIndex = dataKeys.length() - 1;
                    String lastKey = dataKeys.getString(lastIndex);
                    if(Integer.parseInt(id) == Integer.parseInt(lastKey) && lastIndex > 0){
                        lastIndex--;
                        lastKey = dataKeys.getString(lastIndex);
                        String newData = dataObject.getString(lastKey);
                        JSONObject newObj = new JSONObject(newData);
                        int userFrom = newObj.getInt("userFrom");
                        if(userFrom == myId){
                            int dlvd = newObj.getInt("dlvd");
                            int seen = newObj.getInt("seen");
                            RelativeLayout relativeLayoutX = (RelativeLayout) relObject.get(lastKey);
                            ImageButton msgSeen =  relativeLayoutX.findViewById(R.id.msgSeen);
                            runOnUiThread(() -> {
                                if (dlvd > 0)
                                    msgSeen.setBackgroundResource(R.drawable.ic_msg_dlvd);
                                else if (seen > 0)
                                    msgSeen.setBackgroundResource(R.drawable.ic_msg_seen);
                                else
                                    msgSeen.setBackgroundResource(R.drawable.ic_msg_sent);
                            });
                        }
                    }
                    dataObject.remove(id);
                    data = dataObject.toString(0);
                    messageObject.put("data", data);
                    saveMessageContent();
                    runOnUiThread(() -> messageDisplay.removeView(relativeLayout));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("typing", args -> {
            try {
                JSONObject argsArr = new JSONObject(args[0].toString());
                String theMsgId = argsArr.getString("msgId");
                if(Integer.parseInt(theMsgId) == Integer.parseInt(msgId)) {
                    boolean typingR = argsArr.getBoolean("typing");
                    String photo = argsArr.getString("photo");
                    if(typingR && !typing){
                        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                        int typingImage;
                        if(darkThemeEnabled)
                            typingImage = R.drawable.typing_white;
                        else
                            typingImage = R.drawable.typing_faster;
                        typingView = (RelativeLayout) getLayoutInflater().inflate(R.layout.typing, null);
                        ImageView imageView =  typingView.findViewById(R.id.profPic);
                        ImageView typingImg =  typingView.findViewById(R.id.gifImg);
                        imageLoader.displayImage(photo, imageView);
                        typingImg.setBackgroundResource(typingImage);
                        typing = true;
                        runOnUiThread(() -> {
                            typingView.setOnTouchListener((v, event) -> {
                                hideSoftKeyboard(v);
                                menuLayoutBox.setVisibility(View.GONE);
                                return false;
                            });
                            messageDisplay.addView(typingView);
                        });
                        typingView.post(() -> scrollToView(typingView, false));
                    } else if(!typingR){
                        typing = false;
                        runOnUiThread(() -> messageDisplay.removeView(typingView));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("sound", args -> {
            if(!firstLoad) {
                try {
                    JSONArray argsArr = new JSONArray(args[0].toString());
                    int userTo = argsArr.getInt(0);
                    boolean value = argsArr.getBoolean(1);
                    if (user == userTo) {
                        boolean newVal = !value;
                        msgOptionVals.put("sound", newVal);
                        TextView optTV = (TextView) menuTextViews.get("sound");
                        optTV.setTag(newVal);
                        runOnUiThread(() -> {
                            if(newVal)
                                optTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
                            else
                                optTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        socket.on("online", args -> {
            if(!firstLoad) {
                try {
                    JSONArray argsArr = new JSONArray(args[0].toString());
                    int userTo = argsArr.getInt(0);
                    boolean value = argsArr.getBoolean(1);
                    if (user == userTo) {
                        boolean newVal = !value;
                        msgOptionVals.put("online", newVal);
                        TextView optTV = (TextView) menuTextViews.get("online");
                        optTV.setTag(newVal);
                        runOnUiThread(() -> {
                            if(newVal)
                                optTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
                            else
                                optTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        socket.on("favourite", args -> {
            if(!firstLoad) {
                try {
                    JSONArray argsArr = new JSONArray(args[0].toString());
                    int msgID = argsArr.getInt(0);
                    boolean value = argsArr.getBoolean(1);
                    if (msgID == Integer.parseInt(msgId)) {
                        boolean newVal = !value;
                        String text = "Mark as Favourite";
                        if(newVal)
                            text = "Remove from Favourites";
                        msgOptions.put("favourite", text);
                        msgOptionVals.put("favourite", newVal);
                        TextView optTV = (TextView) menuTextViews.get("favourite");
                        String finalText = text;
                        runOnUiThread(() -> {
                            optTV.setTag(newVal);
                            optTV.setText(finalText);
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        socket.on("archive", args -> {
            if(!firstLoad) {
                try {
                    JSONArray argsArr = new JSONArray(args[0].toString());
                    int msgID = argsArr.getInt(0);
                    boolean value = argsArr.getBoolean(1);
                    if (msgID == Integer.parseInt(msgId)) {
                        boolean newVal = !value;
                        String text = "Move to Archive";
                        if(newVal)
                            text = "Remove from Archive";
                        msgOptions.put("archive", text);
                        msgOptionVals.put("archive", newVal);
                        TextView optTV = (TextView) menuTextViews.get("archive");
                        String finalText = text;
                        runOnUiThread(() -> {
                            optTV.setTag(newVal);
                            optTV.setText(finalText);
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        socket.on("deleteChat", args -> {
            int msgID = Integer.parseInt(args[0].toString());
            if(msgID == Integer.parseInt(msgId)){
                socket.emit("removeMessagePage", myId);
                socket.emit("disconnected", myId);
                finish();
            }
        });
        socket.on("blockChat", args -> {
            if(!firstLoad) {
                try {
                    JSONArray argsArr = new JSONArray(args[0].toString());
                    int userTo = argsArr.getInt(0);
                    int msgID = argsArr.getInt(1);
                    boolean value1 = argsArr.getBoolean(2);
                    boolean newVal = argsArr.getBoolean(3);
                    if(msgID == Integer.parseInt(msgId)) {
                        if (user == userTo) {
                            String text = "Block Messages from " + fCharName;
                            if (newVal)
                                text = "Unblock Messages from " + fCharName;
                            msgOptions.put("block", text);
                            msgOptionVals.put("block", newVal);
                            TextView optTV = (TextView) menuTextViews.get("block");
                            String finalText = text;
                            setMessageObject("block", text);
                            runOnUiThread(() -> {
                                optTV.setTag(newVal);
                                optTV.setText(finalText);
                            });
                        }
                        messageObject.put("access", value1);
                        saveMessageContent();
                        blockMessage(value1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        socket.on("seenMsg", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                int msgID = argsArr.getInt(2);
                if(Integer.parseInt(msgId) == msgID && lastMsgFromMe){
                    @SuppressLint("CutPasteId") ImageButton msgSeen = lastMsgView.findViewById(R.id.msgSeen);
                    runOnUiThread(() -> msgSeen.setBackgroundResource(R.drawable.ic_msg_seen));
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        });
        socket.on("msgDelivered", args -> {
            int msgID = Integer.parseInt(args[0].toString());
            if(Integer.parseInt(msgId) == msgID && lastMsgFromMe){
                @SuppressLint("CutPasteId") ImageButton msgSeen = lastMsgView.findViewById(R.id.msgSeen);
                runOnUiThread(() -> msgSeen.setBackgroundResource(R.drawable.ic_msg_dlvd));
            }
        });
        socket.on("restoreMessage", args -> {
            try {
                JSONObject msgData = new JSONObject(args[0].toString());
                long time = msgData.getLong("time");
                String msgRefData = msgData.getString("msgRefData");
                boolean audioType = msgData.getBoolean("audioType");
                String id = msgData.getString("id");
                jsonObject.put(id, msgData);
                int userFrom = msgData.getInt("userFrom");
                String msgBody = msgData.getString("msgBody");
                int msgRefX = msgData.getInt("msgRef");
                int dlvd = msgData.getInt("dlvd");
                int seen = msgData.getInt("seen");
                String filesStr = msgData.getString("files");
                String date = msgData.getString("date");
                boolean hasLink = hasLink(msgBody);
                boolean deletable = true;
                JSONObject ject = new JSONObject();
                ject.put("id", id);
                ject.put("userFrom", userFrom);
                ject.put("msgBody", msgBody);
                ject.put("msgRef", msgRefX);
                ject.put("msgRefData", msgRefData);
                ject.put("dlvd", dlvd);
                ject.put("seen", seen);
                ject.put("files", filesStr);
                ject.put("audioType", audioType);
                ject.put("hasLink", hasLink);
                ject.put("date", date);
                ject.put("time", time);
                ject.put("deletable", deletable);
                ject.put("linkData", "");
                String msgDta = ject.toString(0);
                String data = messageObject.getString("data");
                JSONArray dataArray = new JSONArray(data);
                dataArray.put(msgDta);
                data = dataArray.toString(0);
                messageObject.put("data", data);
                saveMessageContent();
                RelativeLayout relativeLayout = messageDisplay.findViewWithTag(time);
                relObject.put(id, relativeLayout);
                relativeObject.remove(String.valueOf(time));
                runOnUiThread(() -> {
                    ImageButton msgSeen = relativeLayout.findViewById(R.id.msgSeen);
                    recordAudio.setVisibility(View.GONE);
                    msgSeen.setBackgroundResource(R.drawable.ic_msg_sent);
                });
            } catch (JSONException e) {
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
        socket.on("scratched", args -> {
            try {
                String sCount = (String) args[0];
                RelativeLayout relativeLayout = (RelativeLayout) relObjectX.get(sCount);
                runOnUiThread(() -> messageDisplay.removeView(relativeLayout));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void onKeyboardVisibilityChanged(boolean opened) {
        try {
            if(!opened)
                emitTyping(opened);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void emitTyping(boolean typingX) throws JSONException {
        JSONObject emitObj = new JSONObject();
        emitObj.put("userTo", user);
        emitObj.put("userFrom", myId);
        emitObj.put("msgId", msgId);
        emitObj.put("photo", myPht);
        emitObj.put("typing", typingX);
        socket.emit("typing", emitObj);
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

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void sendRecordedAudio() throws JSONException {
        audioMsgId--;
        sentCnt++;
        long time = new Date().getTime() / 1000;
        klind.put(String.valueOf(time), sentCnt);
        stopAudioRecording();
        cancelMsgRef(false);
        JSONObject emitObj = new JSONObject();
        if(!(lastMsgView == null)) {
            ImageButton msgSeen = lastMsgView.findViewById(R.id.msgSeen);
            runOnUiThread(() -> {
                if(!(msgSeen == null))
                    msgSeen.setBackgroundResource(R.color.white);
            });
        }
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        @SuppressLint("InflateParams") RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.red_message, null);
        ImageView imageView =  relativeLayout.findViewById(R.id.profPic);
        TextView dateTxt =  relativeLayout.findViewById(R.id.date);
        ImageButton msgSeen = relativeLayout.findViewById(R.id.msgSeen);
        LinearLayout linkDisplay =  relativeLayout.findViewById(R.id.linkDisplay);
        RelativeLayout messageHolder =  relativeLayout.findViewById(R.id.messageHolder);
        lastMsgView = relativeLayout;
        relativeLayout.setTag(time);
        newLayouts.put(String.valueOf(time), relativeLayout);
        dateTxt.setText("Just Now");
        dateTxt.setTag(time);
        textViews.put(String.valueOf(sentCnt), dateTxt);
        imageLoader.displayImage(myPht, imageView);
        linkDisplay.setVisibility(View.GONE);
        msgSeen.setBackgroundResource(R.drawable.ic_msg_sending);
        if(msgRef > 0){
            LinearLayout messageRef =  relativeLayout.findViewById(R.id.messageRef);
            TextView msgRefName =  relativeLayout.findViewById(R.id.msgRefName);
            TextView msgRefDisplay =  relativeLayout.findViewById(R.id.msgRefDisplay);
            messageRef.setVisibility(View.VISIBLE);
            JSONObject object = jsonObject.getJSONObject(String.valueOf(msgRef));
            String userFrom = object.getString("userFrom");
            String msgBody = object.getString("msgBody");
            String filesStr = object.getString("files");
            boolean audioType = object.getBoolean("audioType");
            int drawableLeft = 0;
            String textFrom = "You";
            if(Integer.parseInt(userFrom) == user)
                textFrom = name;
            if(!StringUtils.isEmpty(filesStr)){
                boolean fileImg = false, fileVid = false;
                String[] files = filesStr.split(",");
                for (String file : files) {
                    String fileType = Functions.checkFileType(file);
                    
                    if (fileType.equals("image"))
                        fileImg = true;
                    if (fileType.equals("video"))
                        fileVid = true;
                }
                if(fileVid){
                    if(StringUtils.isEmpty(msgBody))
                        msgBody = "Video";
                    drawableLeft = R.drawable.ic_video_black;
                }
                if(fileImg){
                    if(StringUtils.isEmpty(msgBody)) {
                        msgBody = "Photo";
                        if (fileVid)
                            msgBody += " / Video";
                    }
                    drawableLeft = R.drawable.ic_cam;
                }
            }
            if(audioType) {
                msgBody = "Recorded Audio";
                drawableLeft = R.drawable.ic_mic;
            }
            msgRefName.setText(textFrom);
            msgRefDisplay.setText(msgBody);
            msgRefDisplay.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
            messageRef.setOnClickListener(v -> {
                try {
                    scrollToReferredMessage(msgRef);
                } catch (Throwable e){
                    e.printStackTrace();
                }
            });
            msgRefName.setOnClickListener(v -> {
                try {
                    scrollToReferredMessage(msgRef);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            msgRefDisplay.setOnClickListener(v -> {
                try {
                    scrollToReferredMessage(msgRef);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            messageRef.setOnLongClickListener(v -> {
                openMessageOptions(null, false, true, dateTxt, null, false);
                return true;
            });
        }
        RelativeLayout audioView =  relativeLayout.findViewById(R.id.audioView);
        TextView audioTimer =  relativeLayout.findViewById(R.id.audioTimer);
        ImageButton audioPlayPause =  relativeLayout.findViewById(R.id.audioPlayPause);
        LinearLayout audioLength =  relativeLayout.findViewById(R.id.audioLength);
        LinearLayout audioProgress =  relativeLayout.findViewById(R.id.audioProgress);
        relObjectX.put(String.valueOf(sentCnt), relativeLayout);
        audioView.setVisibility(View.VISIBLE);
        audioView.setOnLongClickListener(v -> {
            openMessageOptions(null, false, true, dateTxt, null, false);
            return true;
        });
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        long timeInMillisec = Functions.getVideoDuration(retriever, audioRecordDir, cntxt);
        String audioTime = Functions.convertMilliTime(timeInMillisec);
        try {
            retriever.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        audioTimer.setText(audioTime);
        audioPlayPause.setBackgroundResource(R.drawable.rotate_black);
        audioPlayPause.startAnimation(rotation);
        messageHolder.setOnLongClickListener(v -> {
            openMessageOptions(null, false, true, dateTxt, null, false);
            return true;
        });
        messageDisplay.addView(relativeLayout);
        relativeLayout.post(() -> scrollToView(relativeLayout, false));
        File file = new File(audioRecordDir);
        Uri uris = Uri.fromFile(file);
        String fileExt = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.toLowerCase());
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("files[]", file.getName(), RequestBody.create(file, MediaType.parse(mimeType)))
                .build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(Constants.uploadMsgFilesUrl)
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

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = Objects.requireNonNull(response.body()).string();
                String files = responseString.replace(" ", "").replaceAll("\\s+", "").trim();
                String audioFile = Constants.www + files;
                runOnUiThread(() -> {
                    audioPlayPause.clearAnimation();
                    audioPlayPause.setBackgroundResource(R.drawable.ic_play_circle_white);
                    audioPlayPause.setOnClickListener(v -> playPauseAudio(audioPlayPause, audioLength, audioProgress, audioFile, String.valueOf(audioMsgId)));
                });
                try {
                    JSONArray jsonArray = new JSONArray(files.split(","));
                    String newFiles = jsonArray.toString();
                    emitObj.put("files", newFiles);
                    emitObj.put("msgId", msgId);
                    emitObj.put("userFrom", myId);
                    emitObj.put("userTo", user);
                    emitObj.put("fSize", 0);
                    emitObj.put("msgBody", "");
                    emitObj.put("type", "true");
                    emitObj.put("newFiles", files);
                    emitObj.put("msgRef", msgRef);
                    emitObj.put("sentCnt", sentCnt);
                    emitObj.put("msgRefData", jsonString);
                    emitObj.put("dateX", date);
                    emitObj.put("time", time);
                    emitObj.put("audioType", true);
                    emitObj.put("hasLink", true);
                    emitObj.put("date", "Just Now");
                    relativeObject.put(String.valueOf(time), emitObj);
                    if(!(socket == null)) {
                        socket.emit("submitMessage", emitObj);
                        lastMsgFromMe = true;
                        messageObject.put("lastMsgFromMe", lastMsgFromMe);
                        @SuppressLint("ResourceType") TextView tView = menuLayout.findViewById(5);
                        if(!(tView == null)){
                            String text = "Move to Archive";
                            tView.setTag(false);
                            tView.setText(text);
                            msgOptionVals.put("archive", false);
                            String optionVals = msgOptionVals.toString(0);
                            messageObject.put("optionVals", optionVals);
                            setMessageObject("archive", text);
                            saveMessageContent();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cancelMsgRef(true);
            }
        });
        relativeLayout.setOnTouchListener((v, event) -> {
            menuLayoutBox.setVisibility(View.GONE);
            hideSoftKeyboard(v);
            return false;
        });
    }

    private void deletePendingMessage(long time, int sentCntX) throws JSONException {
        pendingMessages.put(String.valueOf(time), true);
        textViews.remove(String.valueOf(sentCntX));
        relObjectX.remove(String.valueOf(sentCntX));
        relativeObject.remove(String.valueOf(time));
        klind.remove(String.valueOf(time));
        JSONObject emitObj = new JSONObject();
        emitObj.put("userFrom", myId);
        emitObj.put("userTo", user);
        emitObj.put("msgId", msgId);
        emitObj.put("time", time);
        emitObj.put("deletable", true);
        socket.emit("deletePendingMessage", emitObj);
    }

    private void startAudioRecording() {
        isRecording = true;
        msgTxt.setVisibility(View.GONE);
        swipeToCancel.setVisibility(View.VISIBLE);
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()),
                randStr = UUID.randomUUID().toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RandomString gen = new RandomString(8, ThreadLocalRandom.current());
            randStr = gen.toString();
        }
        if(!new File(tempDir).exists())
            new File(tempDir).mkdirs();
        audioRecordDir = tempDir + "/audio_" + randStr + timeStamp +".mp3";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioSamplingRate(8000);
        mediaRecorder.setOutputFile(audioRecordDir);

        MediaRecorder.OnErrorListener errorListener = null;
        mediaRecorder.setOnErrorListener(errorListener);
        MediaRecorder.OnInfoListener infoListener = null;
        mediaRecorder.setOnInfoListener(infoListener);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        sendBtn.setBackgroundResource(deleteBtnIcon);
        recordLayout.setVisibility(View.VISIBLE);
        startRecordTimer();
        handler.post(updateVisualizer);
    }

    private void stopAudioRecording() {
        if(isRecording && mediaRecorder != null){
            sliderView.setVisibility(View.VISIBLE);
            swipeToCancel.setVisibility(View.GONE);
            isRecording = false;
            mediaRecorder.stop();
            mediaRecorder.release();
            recordLayout.setVisibility(View.GONE);
            visualizerView.clear();
            stopRecordTimer();
            AnimationDrawable animationDrawable = (AnimationDrawable) closeTrash.getBackground();
            animationDrawable.setEnterFadeDuration(0);
            animationDrawable.setExitFadeDuration(0);
            new android.os.Handler().postDelayed(
                    () -> {
                        sendBtn.setVisibility(View.GONE);
                        closeTrash.setVisibility(View.VISIBLE);
                        sliderView.setVisibility(View.GONE);
                        sendBtn.setBackgroundResource(sendBtnIcon);
                        animationDrawable.start();
                    },
                    1000);
            new android.os.Handler().postDelayed(
                    () -> {
                        animationDrawable.stop();
                        closeTrash.setVisibility(View.GONE);
                        sendBtn.setVisibility(View.VISIBLE);
                        msgTxt.setVisibility(View.VISIBLE);
                    },
                    2000);
        }
    }

    private void startRecordTimer(){
        timeCounter = -1000;
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                timeCounter += 1000;
                String recordedTime = Functions.convertMilliTime(timeCounter);
                runOnUiThread(() -> recordTimer.setText(recordedTime));
            }
        };
        timer.schedule(task, 0, 1000);
    }

    private void stopRecordTimer(){
        timer.cancel();
    }

    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (isRecording){
                // get the current amplitude
                int x = mediaRecorder.getMaxAmplitude();
                visualizerView.addAmplitude(x); // update the VisualizeView
                visualizerView.invalidate(); // refresh the VisualizerView
                // update in 40 milliseconds
                handler.postDelayed(this, REPEAT_INTERVAL);
            }
        }
    };

    @SuppressLint({"SetTextI18n", "InflateParams", "ClickableViewAccessibility"})
    private void sendMessage() throws Throwable {
        String message = msgTxt.getText().toString();
        int filesSize = itemLists.size();
        if(!(StringUtils.isEmpty(message) && filesSize == 0)){
            if(!(lastMsgView == null)) {
                ImageButton msgSeen = lastMsgView.findViewById(R.id.msgSeen);
                runOnUiThread(() -> {
                    if(!(msgSeen == null))
                        msgSeen.setBackgroundResource(R.color.white);
                });
            }
            sentCnt++;
            msgTxt.setText("");
            cancelMsgRef(false);
            JSONObject emitObj = new JSONObject();
            boolean copyable = true;
            long time = new Date().getTime() / 1000;
            klind.put(String.valueOf(time), sentCnt);
            @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            resetGridView(false);
            RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.red_message, null);
            ImageView imageView =  relativeLayout.findViewById(R.id.profPic);
            TextView textView =  relativeLayout.findViewById(R.id.message);
            TextView dateTxt =  relativeLayout.findViewById(R.id.date);
            ImageButton msgSeen = relativeLayout.findViewById(R.id.msgSeen);
            RelativeLayout messageHolder =  relativeLayout.findViewById(R.id.messageHolder);
            GridView filesGrid =  relativeLayout.findViewById(R.id.filesGrid);
            LinearLayout linkDisplay =  relativeLayout.findViewById(R.id.linkDisplay);
            relObjectX.put(String.valueOf(sentCnt), relativeLayout);
            lastMsgView = relativeLayout;
            relativeLayout.setTag(time);
            newLayouts.put(String.valueOf(time), relativeLayout);
            if(!StringUtils.isEmpty(message))
                textView.setText(message);
            else
                copyable = false;
            boolean finalCopyable = copyable;
            dateTxt.setText("Just Now");
            dateTxt.setTag(time);

            relativeObject.put(String.valueOf(time), true);
            textViews.put(String.valueOf(sentCnt), dateTxt);
            imageLoader.displayImage(myPht, imageView);
            msgSeen.setBackgroundResource(R.drawable.ic_msg_sending);
            if(hasLink(message)){
                JSONArray rArr;
                String firstLink = getFirstLink(message);
                if(!(savedUrls.has(firstLink)))
                    rArr = new JSONArray();
                else
                    rArr = savedUrls.getJSONArray(firstLink);
                rArr.put(relativeLayout);
                savedUrls.put(firstLink, rArr);
                getLinkContent(firstLink);
            } else {
                linkDisplay.setVisibility(View.GONE);
            }
            if(msgRef > 0){
                LinearLayout messageRef =  relativeLayout.findViewById(R.id.messageRef);
                TextView msgRefName =  relativeLayout.findViewById(R.id.msgRefName);
                TextView msgRefDisplay =  relativeLayout.findViewById(R.id.msgRefDisplay);
                messageRef.setVisibility(View.VISIBLE);
                JSONObject object = jsonObject.getJSONObject(String.valueOf(msgRef));
                String userFrom = object.getString("userFrom");
                String msgBody = object.getString("msgBody");
                String filesStr = object.getString("files");
                boolean audioType = object.getBoolean("audioType");
                int drawableLeft = 0;
                String textFrom = "You";
                if(Integer.parseInt(userFrom) == user)
                    textFrom = name;
                if(!StringUtils.isEmpty(filesStr)){
                    boolean fileImg = false, fileVid = false;
                    String[] files = filesStr.split(",");
                    for (String file : files) {
                        String fileType = Functions.checkFileType(file);
                        
                        if (fileType.equals("image"))
                            fileImg = true;
                        if (fileType.equals("video"))
                            fileVid = true;
                    }
                    if(fileVid){
                        if(StringUtils.isEmpty(msgBody))
                            msgBody = "Video";
                        drawableLeft = R.drawable.ic_video_black;
                    }
                    if(fileImg){
                        if(StringUtils.isEmpty(msgBody)) {
                            msgBody = "Photo";
                            if (fileVid)
                                msgBody += " / Video";
                        }
                        drawableLeft = R.drawable.ic_cam;
                    }
                }
                if(audioType) {
                    msgBody = "Recorded Audio";
                    drawableLeft = R.drawable.ic_mic;
                }
                msgRefName.setText(textFrom);
                msgRefDisplay.setText(msgBody);
                msgRefDisplay.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
                messageRef.setOnClickListener(v -> {
                    try {
                        scrollToReferredMessage(msgRef);
                    } catch (Throwable e){
                        e.printStackTrace();
                    }
                });
                msgRefName.setOnClickListener(v -> {
                    try {
                        scrollToReferredMessage(msgRef);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
                msgRefDisplay.setOnClickListener(v -> {
                    try {
                        scrollToReferredMessage(msgRef);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
                messageRef.setOnLongClickListener(v -> {
                    openMessageOptions(null, finalCopyable, true, dateTxt, textView, false);
                    return true;
                });
            }
            fSize += filesSize;
            if(filesSize > 0){
                filesGrid.setVisibility(View.VISIBLE);
                int rem = 0;
                int newW = imgW - 20;
                int gridH = newW;
                int chosenSize = filesSize;
                if(chosenSize > 4) {
                    chosenSize = 4;
                    rem = filesSize - 3;
                }
                if(chosenSize > 2)
                    gridH += newW;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                messageHolder.setLayoutParams(layoutParams);
                filesGrid.getLayoutParams().height = gridH;
                RelativeLayout progressView = (RelativeLayout) getLayoutInflater().inflate(R.layout.progress_bar, null);
                TextView progressText =  progressView.findViewById(R.id.postProgressText);
                ProgressBar progressBar =  progressView.findViewById(R.id.postProgressBar);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) filesGrid.getLayoutParams();
                params.rightMargin = 30;
                if(filesSize == 1)
                    params.rightMargin = 30 + newW;
                progressView.setLayoutParams(params);
                messageHolder.addView(progressView);
                String[] msgFiles = new String[chosenSize];
                MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (int i = 0; i < filesSize; i++){
                    File file = itemLists.get(i);
                    if(i < chosenSize)
                        msgFiles[i] = file.getAbsolutePath();
                    Uri uris = Uri.fromFile(file);
                    String fileExt = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.toLowerCase());
                    multipartBody.addFormDataPart("files[]", file.getName(), RequestBody.create(file, MediaType.parse(mimeType)));
                }
                imageAdaptor = new ImageAdaptor(cntxt, itemLists, msgFiles, newW, false, rem, new ArrayList<>(), filesSize);
                filesGrid.setAdapter(imageAdaptor);
                filesGrid.setOnLongClickListener(v -> {
                    openMessageOptions(null, finalCopyable, true, dateTxt, textView, false);
                    return true;
                });
                resetGridView(true);

                final CountingRequestBody.Listener progressListener = (bytesRead, contentLength) -> {
                    if(bytesRead < contentLength && contentLength > 0){
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
                        .url(Constants.uploadMsgFilesUrl)
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

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseString = Objects.requireNonNull(response.body()).string();
                        runOnUiThread(() -> {
                            progressBar.setProgress(100, true);
                            progressText.setText("100%");
                            progressView.setVisibility(View.GONE);
                        });
                        try {
                            String files = responseString.replace(" ", "").replaceAll("\\s+", "").trim();
                            String[] uploads = files.split(",");
                            for (int i = 0; i < uploads.length; i++){
                                String filePath = uploads[i];
                                String fileUrl = Constants.www + filePath;
                                imageAdaptor.changeData(i, fileUrl);
                            }
                            JSONArray jsonArray = new JSONArray(files.split(","));
                            String newFiles = jsonArray.toString();
                            emitObj.put("files", newFiles);
                            emitObj.put("msgId", msgId);
                            emitObj.put("userFrom", myId);
                            emitObj.put("userTo", user);
                            emitObj.put("msgBody", message);
                            emitObj.put("hasLink", hasLink(message));
                            emitObj.put("newFiles", files);
                            emitObj.put("type", "false");
                            emitObj.put("fSize", filesSize);
                            emitObj.put("msgRef", msgRef);
                            emitObj.put("sentCnt", sentCnt);
                            emitObj.put("msgRefData", jsonString);
                            emitObj.put("dateX", date);
                            emitObj.put("time", time);
                            emitObj.put("audioType", false);
                            emitObj.put("linkData", null);
                            emitObj.put("date", "Just Now");
                            relativeObject.put(String.valueOf(time), emitObj);
                            socket.emit("submitMessage", emitObj);
                            lastMsgFromMe = true;
                            messageObject.put("lastMsgFromMe", lastMsgFromMe);
                            @SuppressLint("ResourceType") TextView tView = menuLayout.findViewById(5);
                            if(!(tView == null)){
                                String text = "Move to Archive";
                                tView.setTag(false);
                                tView.setText(text);
                                msgOptionVals.put("archive", false);
                                String optionVals = msgOptionVals.toString(0);
                                messageObject.put("optionVals", optionVals);
                                setMessageObject("archive", text);
                                saveMessageContent();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        cancelMsgRef(true);
                    }
                });
            } else {
                try {
                    emitObj.put("msgId", msgId);
                    emitObj.put("userFrom", myId);
                    emitObj.put("userTo", user);
                    emitObj.put("msgBody", message);
                    emitObj.put("files", "");
                    emitObj.put("newFiles", "");
                    emitObj.put("fSize", 0);
                    emitObj.put("linkData", null);
                    emitObj.put("type", "false");
                    emitObj.put("msgRef", msgRef);
                    emitObj.put("sentCnt", sentCnt);
                    emitObj.put("hasLink", hasLink(message));
                    emitObj.put("msgRefData", jsonString);
                    emitObj.put("dateX", date);
                    emitObj.put("time", time);
                    emitObj.put("audioType", false);
                    emitObj.put("date", "Just Now");
                    relativeObject.put(String.valueOf(time), emitObj);
                    socket.emit("submitMessage", emitObj);
                    lastMsgFromMe = true;
                    messageObject.put("lastMsgFromMe", lastMsgFromMe);
                    @SuppressLint("ResourceType") TextView tView = menuLayout.findViewById(5);
                    if(!(tView == null)){
                        String text = "Move to Archive";
                        tView.setTag(false);
                        tView.setText(text);
                        msgOptionVals.put("archive", false);
                        String optionVals = msgOptionVals.toString(0);
                        messageObject.put("optionVals", optionVals);
                        setMessageObject("archive", text);
                        saveMessageContent();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cancelMsgRef(true);
            }
            textView.setOnLongClickListener(v -> {
                openMessageOptions(null, finalCopyable, true, dateTxt, textView, false);
                return true;
            });
            messageDisplay.addView(relativeLayout);
            messageHolder.setOnLongClickListener(v -> {
                openMessageOptions(null, finalCopyable, true, dateTxt, textView, false);
                return true;
            });
            relativeLayout.post(() -> scrollToView(relativeLayout, false));
            relativeLayout.setOnTouchListener((v, event) -> {
                menuLayoutBox.setVisibility(View.GONE);
                hideSoftKeyboard(v);
                return false;
            });
        }
    }

    private void getLinkContent(String urlLink) {
        if(linkDatas.has(urlLink)){
            try {
                JSONObject linkObj = linkDatas.getJSONObject(urlLink);
                displayLinkContents(urlLink, linkObj);
                if(savedUrls.has(urlLink))
                    savedUrls.remove(urlLink);
                sendCuedLinks();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            String prt = "http://";
            String newUrl = prt.concat(urlLink);
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("text", newUrl)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.getLinkContentUrl)
                    .post(requestBody)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    //onError
                    //Log.e("failure Response", mMessage);
                }

                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject linkObj = new JSONObject(responseString);
                        linkDatas.put(urlLink, linkObj);
                        displayLinkContents(urlLink, linkObj);
                        if(savedUrls.has(urlLink))
                            savedUrls.remove(urlLink);
                        sendCuedLinks();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void displayLinkContents(String urlLink, JSONObject linkObj) throws JSONException {
        String imageUrl = linkObj.getString("imageUrl");
        String title = linkObj.getString("title");
        String description = linkObj.getString("description");
        String host = linkObj.getString("host");
        String linkUrl = linkObj.getString("linkUrl");
        JSONArray array = savedUrls.getJSONArray(urlLink);
        for(int i = 0; i < array.length(); i++) {
            RelativeLayout relativeLayout = (RelativeLayout) array.get(i);
            LinearLayout linkDisplay = relativeLayout.findViewById(R.id.linkDisplay);
            ImageView linkImg = relativeLayout.findViewById(R.id.linkImg);
            TextView linkTitle = relativeLayout.findViewById(R.id.linkTitle);
            TextView linkDesc = relativeLayout.findViewById(R.id.linkDesc);
            TextView linkHost = relativeLayout.findViewById(R.id.linkHost);
            runOnUiThread(() -> {
                linkTitle.setText(title);
                linkDesc.setText(description);
                linkHost.setText(host);
                Bitmap bitmap = Functions.getBitmapFromURL(imageUrl, true);
                if (!(bitmap == null))
                    linkImg.setImageBitmap(bitmap);
                linkDisplay.setVisibility(View.VISIBLE);
                linkDisplay.setOnClickListener(v -> openLink(cntxt, linkUrl));
                linkDisplay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int h = linkDisplay.getHeight();
                        int st = scrllView.getScrollY();
                        int t = h + st;
                        scrllView.scrollTo(0, t);
                        linkDisplay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            });
        }
    }

    private void scrollToView(View view, boolean changeColor) {
        int scrllTo = view.getBottom(), scrllY = scrllView.getScrollY();
        int viewSpace = scrllY + scrllView.getHeight();
        if(scrllTo < messageDisplay.getHeight())
            scrllTo = view.getTop();
        if(scrllTo < scrllY || scrllTo > viewSpace)
            scrllView.scrollTo(0, scrllTo);
        if(changeColor) {
            Object c = 0xFFFFFFFF;
            if(getDefaultDarkThemeEnabled())
                c = 0x00000000;
            ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(view,
                    "backgroundColor",
                    new ArgbEvaluator(),
                    0xFFFFFF99,
                    c);
            backgroundColorAnimator.setDuration(2000);
            backgroundColorAnimator.start();
        }
    }

    private void loadImages() {
        if(!(gridView.getVisibility() == View.VISIBLE)) {
            gridView.setVisibility(View.VISIBLE);
            String[] allPath = StorageUtils.getStorageDirectories(this);
            for (String path : allPath) {
                File storage = new File(path);
                loadDirectoryFiles(storage);
            }
            allFiles = new String[allMediaList.size()];
            allFilesArr = new File[allMediaList.size()];
            for (int x = 0; x < allMediaList.size(); x++) {
                String filePth = allMediaList.get(x);
                allFilesArr[x] = new File(filePth);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Arrays.sort(allFilesArr, Comparator.comparingLong(File::lastModified).reversed());
            }
            for (int x = 0; x < allFilesArr.length; x++) {
                File file = allFilesArr[x];
                allFiles[x] = file.getAbsolutePath();
            }
            imageNum.setVisibility(View.VISIBLE);
            imageNum.setText("0");
            imageAdaptor = new ImageAdaptor(cntxt, itemLists, allFiles, imgW, true, 0, new ArrayList<>(), allFiles.length);
            gridView.setAdapter(null);
            gridView.setAdapter(imageAdaptor);
        }
    }

    public void loadDirectoryFiles(File directory){
        boolean notForbidden = false;
        String[] forbiddenPaths = new String[]{
                "/Android/data",
                "/Android/obb",
                "/LOST.DIR",
                "/.thumbnail"
        };
        File[] fileList = directory.listFiles();
        if(fileList != null && fileList.length > 0){
            for (File file : fileList) {
                String filePath = file.getAbsolutePath();
                if (file.isDirectory()) {
                    for (String forbiddenPath : forbiddenPaths) {
                        if (filePath.contains(forbiddenPath)) {
                            notForbidden = true;
                            break;
                        }
                    }
                    if (!notForbidden)
                        loadDirectoryFiles(file);
                } else {
                    String pthPar = file.getParent();
                    String[] pthPars = pthPar.split("/");
                    if (!(pthPars[pthPars.length - 1].equals("LOST.DIR") || pthPars[pthPars.length - 1].equals(".thumbnails"))) {
                        String name = file.getName().toLowerCase();
                        for (String ext : Constants.allowedExt) {
                            if (name.endsWith(ext)) {
                                allMediaList.add(filePath);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private static class ImageAdaptor extends BaseAdapter {

        Context context;
        private final int imageWidth;
        String[] itemList;
        ArrayList<String> allItems;
        Bitmap bitmap;
        boolean direct;
        int rem, fileCount;
        ArrayList<File> itemLists = new ArrayList<>();
        static JSONArray array;
        static int treeCount = 0;

        public ImageAdaptor(Context context, ArrayList<File> itemLists, String[] itemList, int imageWidth, boolean direct, int rem, ArrayList<String> allItems, int fileCount) {
            this.context = context;
            this.itemLists = itemLists;
            this.itemList = itemList;
            this.imageWidth = imageWidth;
            this.direct = direct;
            this.rem = rem;
            this.fileCount = fileCount;
            this.allItems = allItems;
            array = new JSONArray();
        }

        private ArrayList<String> getAllItems() {
            return allItems;
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

        private int getItemPosition(String filePath) {
            int i = 0;
            if(allItems.contains(filePath))
                i = allItems.indexOf(filePath);
            return i;
        }

        @SuppressLint({"ViewHolder", "SetTextI18n"})
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            treeCount++;
            convertView = LayoutInflater.from(context).inflate(R.layout.image_box, parent, false);
            convertView.setLayoutParams(new GridView.LayoutParams(imageWidth ,imageWidth ));
            final String filePath = itemList[position];
            File thisFile = new File(filePath);
            int pos = getItemPosition(filePath);
            String fileType = Functions.checkFileType(filePath.toLowerCase());
            final ImageView imageView = convertView.findViewById(R.id.imgView);
            convertView.setContentDescription(filePath);
            
            if(fileType.equals("video")){
                String vidTime = null;
                LinearLayout video = convertView.findViewById(R.id.video);
                TextView videoTime = convertView.findViewById(R.id.videoTime);
                video.setVisibility(View.VISIBLE);
                video.setVisibility(View.VISIBLE);
                if(!filePath.startsWith("http")) {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    long timeInMillisec = Functions.getVideoDuration(retriever, filePath, context);
                    vidTime = Functions.convertMilliTime(timeInMillisec);
                    try {
                        retriever.release();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(filePath.startsWith("http")) {
                    try {
                        vidTime = Functions.getMediaTime(filePath);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
                videoTime.setText(vidTime);
            }
            if (itemLists.contains(thisFile) && direct)
                convertView.setBackgroundResource(R.drawable.border_box);
            else
                convertView.setBackgroundResource(R.drawable.null_border);
            if (selectedImages.contains(filePath) && direct) {
                TextView txtNum = convertView.findViewById(R.id.numTxt);
                int n = selectedImages.indexOf(filePath) + 1;
                txtNum.setText(String.valueOf(n));
                txtNum.setVisibility(View.VISIBLE);
            }
            if(!filePath.startsWith("http") && !StringUtils.isEmpty(filePath)) {
                bitmap = Functions.decodeFiles(filePath, fileType, true);
                if(!(bitmap == null))
                    imageView.setImageBitmap(bitmap);
            } else {
                bitmap = Functions.getBitmapFromSource(filePath, fileType, true);
                imageView.setImageBitmap(bitmap);
            }
            if(rem > 0 && itemList.length - 1 == position){
                RelativeLayout revView = convertView.findViewById(R.id.revView);
                TextView remText =  convertView.findViewById(R.id.text);
                remText.setText("+"+rem);
                revView.setVisibility(View.VISIBLE);
            }
            if(direct) {
                View finalConvertView = convertView;
                imageView.setOnClickListener(v -> {
                    if (itemLists.contains(thisFile)) {
                        itemLists.remove(thisFile);
                        selectedImages.remove(filePath);
                        finalConvertView.setBackgroundResource(R.drawable.null_border);
                    } else {
                        itemLists.add(thisFile);
                        selectedImages.add(filePath);
                        finalConvertView.setBackgroundResource(R.drawable.border_box);
                    }
                    numberSelectedImages();
                });
            } else {
                imageView.setOnClickListener(v -> HomeAct.openImage(getAllItems(), pos, context));
            }
            return convertView;
        }

        public void changeData(int position, String urlStr){
            allItems.add(position, urlStr);
        }

    }

    private static void numberSelectedImages() {
        imageNum.setText(String.valueOf(selectedImages.size()));
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
    }

    private void copyContent(TextView textView) {
        CharSequence charSequence = textView.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", charSequence);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(cntxt, "Copied to Clipboard", Toast.LENGTH_LONG).show();
    }

    private void loadMessages(){
        if(!(messageObject == null) && firstLoad) {
            runOnUiThread(() -> new Handler().postDelayed(() -> displayMessageContent(messageObject),10));
        } else {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("lang", lang)
                    .addFormDataPart("myId", myIDtoString)
                    .addFormDataPart("user", userIDtoString)
                    .addFormDataPart("maxId", String.valueOf(maxId))
                    .addFormDataPart("targetId", String.valueOf(targetId))
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(Constants.msgerUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            try(okhttp3.Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject responseObject = new JSONObject(responseString);
                    if (messageObject == null) {
                        messageObject = new JSONObject(responseString);
                    } else {
                        int maxIdX = responseObject.getInt("maxId");
                        boolean maxReachedX = responseObject.getBoolean("maxReached");
                        String newData = responseObject.getString("data");
                        String oldData = messageObject.getString("data");
                        String data = newData.substring(0, newData.length() - 1) + "," + oldData.substring(1);
                        messageObject.put("maxId", maxIdX);
                        messageObject.put("maxReached", maxReachedX);
                        messageObject.put("data", data);
                    }
                    saveMessageContent();
                    displayMessageContent(responseObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveMessageContent() {
        try {
            saveOpenedMessage(user, messageObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"SetTextI18n, InflateParams", "ClickableViewAccessibility"})
    private void displayMessageContent(JSONObject jsonObjectX) {
        progressBar.clearAnimation();
        progressBar.setVisibility(View.GONE);
        loading = false;
        try {
            if(firstLoad) {
                msgId = jsonObjectX.getString("msgId");
                name = jsonObjectX.getString("name");
                userName = jsonObjectX.getString("userName");
                pronoun = jsonObjectX.getString("pronoun");
                photo = Constants.www + jsonObjectX.getString("photo");
                fCharName = jsonObjectX.getString("fCharName");
                String options = jsonObjectX.getString("options");
                String optionVals = jsonObjectX.getString("optionVals");
                boolean access = jsonObjectX.getBoolean("access");
                finalAccess = jsonObjectX.getBoolean("finalAccess");
                lastMsgFromMe = jsonObjectX.getBoolean("lastMsgFromMe");
                imageLoader.displayImage(photo, userPht);
                msgerName.setText(name);
                //msgerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_online, 0);
                msgerUName.setText("@" + userName);
                msgOptions = new JSONObject(options);
                msgOptionVals = new JSONObject(optionVals);
                if(!access)
                    blockMessage(access);
                setUpMenuOption();
                checkBlockUpdate();
                checkFavUpdate();
                checkArchUpdate();
                checkSoundUpdate();
                checkOffUpdate();
            }
            maxId = jsonObjectX.getInt("maxId");
            maxReached = jsonObjectX.getBoolean("maxReached");
            String data = jsonObjectX.getString("data");
            int fileCount = jsonObjectX.getInt("fileCount");
            if(!StringUtils.isEmpty(data)) {
                JSONObject msgDatas = new JSONObject(data);
                int dataLen = msgDatas.length(), count = 0;
                for (int x = 0; x < dataLen; x++) {
                    count++;
                    String key = Objects.requireNonNull(msgDatas.names()).getString(x);
                    JSONObject msgData = new JSONObject(msgDatas.getString(key));
                    String id = msgData.getString("id");
                    jsonObject.put(id, msgData);
                    String userFrom = msgData.getString("userFrom");
                    String msgBody = msgData.getString("msgBody");
                    int msgRefX = msgData.getInt("msgRef");
                    String msgRefData = msgData.getString("msgRefData");
                    int dlvd = msgData.getInt("dlvd");
                    int seen = msgData.getInt("seen");
                    String filesStr = msgData.getString("files");
                    String date = msgData.getString("date");
                    long time = msgData.getLong("time");
                    boolean hasLink = msgData.getBoolean("hasLink");
                    boolean audioType = msgData.getBoolean("audioType");
                    boolean deletable = msgData.getBoolean("deletable");
                    boolean copyable = true;
                    boolean dragLeft = false;
                    RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.ash_message, null);
                    String image = photo;
                    if (Integer.parseInt(userFrom) == myId) {
                        dragLeft = true;
                        relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.red_message, null);
                        image = myPht;
                    }
                    relObject.put(id, relativeLayout);
                    if (targetId > 0 && targetId == Integer.parseInt(id)) {
                        scrllToView = relativeLayout;
                    }
                    relativeLayout.setTag(time);
                    ImageView imageView =  relativeLayout.findViewById(R.id.profPic);
                    TextView textView =  relativeLayout.findViewById(R.id.message);
                    TextView dateTxt =  relativeLayout.findViewById(R.id.date);
                    RelativeLayout messageHolder =  relativeLayout.findViewById(R.id.messageHolder);
                    if (!StringUtils.isEmpty(msgBody)){
                        String htmlText = HtmlParser.parseURL(msgBody);
                        CharSequence sequence = Html.fromHtml(htmlText);
                        SpannableString spannableString = new SpannableString(sequence);
                        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                        for(URLSpan span : urls) {
                            makeLinkClickable(strBuilder, span, cntxt);
                        }
                        textView.setText(strBuilder);
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                    } else
                        copyable = false;
                    boolean finalCopyable = copyable;
                    dateTxt.setText(date);
                    dateTxt.setTag(time);
                    imageLoader.displayImage(image, imageView);
                    if(hasLink){
                        String firstLink = getFirstLink(msgBody);
                        JSONArray rArr, array = new JSONArray();
                        array.put(relativeLayout);
                        array.put(firstLink);
                        if(!(savedUrls.has(firstLink)))
                            rArr = new JSONArray();
                        else
                            rArr = savedUrls.getJSONArray(firstLink);
                        rArr.put(relativeLayout);
                        savedUrls.put(firstLink, rArr);
                    }
                    if (msgRefX > 0) {
                        LinearLayout messageRef =  relativeLayout.findViewById(R.id.messageRef);
                        TextView msgRefName =  relativeLayout.findViewById(R.id.msgRefName);
                        TextView msgRefDisplay =  relativeLayout.findViewById(R.id.msgRefDisplay);
                        messageRef.setVisibility(View.VISIBLE);
                        JSONObject object = new JSONObject(msgRefData);
                        String userFromRef = object.getString("userFrom");
                        String msgBodyRef = object.getString("msgBody");
                        String filesStrRef = object.getString("files");
                        boolean audioTypeRef = object.getBoolean("type");
                        int drawableLeft = 0;
                        String textFrom = "You";
                        if (Integer.parseInt(userFromRef) == user)
                            textFrom = name;
                        if (!StringUtils.isEmpty(filesStrRef)) {
                            boolean fileImg = false, fileVid = false;
                            String[] filesRef = filesStrRef.split(",");
                            for (String s : filesRef) {
                                String fileType = Functions.checkFileType(s);
                                
                                if (fileType.equals("image"))
                                    fileImg = true;
                                if (fileType.equals("video"))
                                    fileVid = true;
                            }
                            if (fileVid) {
                                if (StringUtils.isEmpty(msgBodyRef))
                                    msgBodyRef = "Video";
                                drawableLeft = R.drawable.ic_video_black;
                            }
                            if (fileImg) {
                                if (StringUtils.isEmpty(msgBodyRef)) {
                                    msgBodyRef = "Photo";
                                    if (fileVid)
                                        msgBodyRef += " / Video";
                                }
                                drawableLeft = R.drawable.ic_cam;
                            }
                        }
                        if (audioTypeRef) {
                            msgBodyRef = "Recorded Audio";
                            drawableLeft = R.drawable.ic_mic;
                        }
                        msgRefName.setText(textFrom);
                        msgRefDisplay.setText(msgBodyRef);
                        msgRefDisplay.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
                        messageRef.setOnClickListener(v -> {
                            try {
                                scrollToReferredMessage(msgRefX);
                            } catch (Throwable e){
                                e.printStackTrace();
                            }
                        });
                        msgRefName.setOnClickListener(v -> {
                            try {
                                scrollToReferredMessage(msgRefX);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        });
                        msgRefDisplay.setOnClickListener(v -> {
                            try {
                                scrollToReferredMessage(msgRefX);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        });
                        messageRef.setOnLongClickListener(v -> {
                            openMessageOptions(id, finalCopyable, deletable, dateTxt, textView, true);
                            return true;
                        });
                    }
                    if (!StringUtils.isEmpty(filesStr)) {
                        JSONArray files = new JSONArray(filesStr);
                        if (audioType) {
                            RelativeLayout audioView =  relativeLayout.findViewById(R.id.audioView);
                            TextView audioTimer =  relativeLayout.findViewById(R.id.audioTimer);
                            ImageButton audioPlayPause =  relativeLayout.findViewById(R.id.audioPlayPause);
                            LinearLayout audioLength =  relativeLayout.findViewById(R.id.audioLength);
                            LinearLayout audioProgress =  relativeLayout.findViewById(R.id.audioProgress);
                            audioView.setVisibility(View.VISIBLE);
                            String filePath = files.getString(0);
                            String audioFile = Constants.www + filePath;
                            String audioTime = Functions.getMediaTime(audioFile);
                            audioTimer.setText(audioTime);
                            audioPlayPause.setOnClickListener(v -> playPauseAudio(audioPlayPause, audioLength, audioProgress, audioFile, id));
                            audioView.setOnLongClickListener(v -> {
                                openMessageOptions(id, finalCopyable, deletable, dateTxt, textView, true);
                                return true;
                            });
                        } else {
                            GridView filesGrid =  relativeLayout.findViewById(R.id.filesGrid);
                            filesGrid.setVisibility(View.VISIBLE);
                            int filesSize = files.length();
                            int rem = 0;
                            int newW = imgW - 20;
                            int gridH = newW;
                            int chosenSize = filesSize;
                            if (chosenSize > 4) {
                                chosenSize = 4;
                                rem = filesSize - 3;
                            }
                            if (chosenSize > 2)
                                gridH += newW;
                            if (Integer.parseInt(userFrom) == user) {
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                layoutParams.setMargins(0, 0, 180, 0);
                                messageHolder.setLayoutParams(layoutParams);
                                messageHolder.requestLayout();
                            }
                            filesGrid.getLayoutParams().height = gridH;
                            String[] msgFiles = new String[chosenSize];
                            ArrayList<String> strings = new ArrayList<>();
                            for (int i = 0; i < filesSize; i++) {
                                String fileUrl = Constants.www + files.getString(i);
                                strings.add(fileUrl);
                                if (i < chosenSize)
                                    msgFiles[i] = fileUrl;
                            }
                            imageAdaptor = new ImageAdaptor(cntxt, itemLists, msgFiles, newW, false, rem, strings, fileCount);
                            filesGrid.setAdapter(imageAdaptor);
                            filesGrid.setOnLongClickListener(v -> {
                                openMessageOptions(id, finalCopyable, deletable, dateTxt, textView, true);
                                return true;
                            });
                        }
                    }
                    textView.setOnLongClickListener(v -> {
                        openMessageOptions(id, finalCopyable, deletable, dateTxt, textView, true);
                        return true;
                    });
                    final boolean finalDragLeft = dragLeft;
                    RelativeLayout finalRelativeLayout = relativeLayout;
                    listenToLongClick(relativeLayout, id, finalCopyable, deletable, dateTxt, textView, true);
                    RelativeLayout finalRelativeLayout1 = relativeLayout;
                    setDragForAllViews(relativeLayout, relativeLayout, id, finalDragLeft);
                    messageDisplay.addView(relativeLayout, x);
                    if (count == dataLen) {
                        if (firstLoad) {
                            if (relativeObject.length() > 0) {
                                displayNewMessages(id);
                            } else {
                                if (lastMsgFromMe) {
                                    lastMsgView = relativeLayout;
                                    ImageButton msgSeen =  relativeLayout.findViewById(R.id.msgSeen);
                                    int msgSt = R.drawable.ic_msg_sent;
                                    if (dlvd > 0)
                                        msgSt = R.drawable.ic_msg_dlvd;
                                    if (seen > 0)
                                        msgSt = R.drawable.ic_msg_seen;
                                    msgSeen.setBackgroundResource(msgSt);
                                }
                                relativeLayout.post(() -> {
                                    int scrllTo = finalRelativeLayout.getBottom();
                                    scrllView.scrollTo(0, scrllTo);
                                });
                            }
                            firstLoad = false;
                            JSONObject emitObj = new JSONObject();
                            emitObj.put("userFrom", myId);
                            emitObj.put("userTo", user);
                            emitObj.put("msgId", msgId);
                            socket.emit("seenMsg", emitObj);
                            sendCuedLinks();
                        } else if (targetId > 0) {
                            targetId = 0;
                            scrllToView.setBackgroundResource(R.color.yellowLight);
                            scrllToView.post(() -> scrollToView(scrllToView, true));
                            sendCuedLinks();
                        } else {
                            relativeLayout.post(() -> scrollToView(finalRelativeLayout, false));
                            relativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    ViewTreeObserver.OnGlobalLayoutListener that = this;
                                    new android.os.Handler().postDelayed(() -> {
                                        try {
                                            sendCuedLinks();
                                            finalRelativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(that);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }, 1000);
                                }
                            });
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if(targetId > 0){
                targetId = 0;
                int scrllH = scrllView.getHeight(), viewH = messageDisplay.getHeight();
                int scrllTo = viewH - scrllH;
                scrllView.scrollTo(0, scrllTo);
                Toast.makeText(cntxt, "Message Deleted", Toast.LENGTH_LONG).show();
            } else
                e.printStackTrace();
        }
    }

    private void listenToLongClick(View view, String id, boolean finalCopyable, boolean deletable, TextView dateTxt, TextView textView, boolean b) {
        view.setOnLongClickListener(v -> {
            if(!isDragging){
                pressed = true;
                openMessageOptions(id, finalCopyable, deletable, dateTxt, textView, b);
            }
            return false;
        });
        if(view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                listenToLongClick(innerView, id, finalCopyable, deletable, dateTxt, textView, b);
            }
        }
    }

    private static void makeLinkClickable(SpannableStringBuilder strBuilder, URLSpan span, Context cntxt) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String href = span.getURL();
                openLink(cntxt, href);
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setColor(ContextCompat.getColor(cntxt, R.color.linkColor));
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    @SuppressLint("InflateParams")
    private void openMessageOptions(String id, boolean copyable, boolean deletable, TextView dateTxt, TextView textView, boolean real) {
        if(!isDragging) {
            try {
                cancelMsgRef(true);
                if (blackFade.getChildCount() > 0) {
                    blackFade.removeAllViews();
                }
                RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
                LinearLayout optLayer =  postOptView.findViewById(R.id.optLayer);
                JSONObject object = new JSONObject();
                if (copyable)
                    object.put("copy", "Copy");
                if (real) {
                    object.put("reply", "Reply");
                    object.put("forward", "Forward");
                }
                object.put("delete", "Delete Message");
                JSONArray objKeys = object.names();
                for (int r = 0; r < Objects.requireNonNull(objKeys).length(); r++) {
                    TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
                    String key = objKeys.getString(r);
                    String option = object.getString(key);
                    int drawableLeft = optIcons.getInt(key);
                    optionList.setText(option);
                    optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
                    optionList.setOnClickListener(v -> {
                        try {
                            blackFade.setVisibility(View.GONE);
                            switch (key) {
                                case "copy":
                                    copyContent(textView);
                                    break;
                                case "reply":
                                    referToMessage(id);
                                    break;
                                case "forward":
                                    forwardMessage(id);
                                    break;
                                case "delete":
                                    deleteMessage(id, deletable, dateTxt);
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                    optLayer.addView(optionList);
                }
                blackFade.addView(postOptView);
                blackFade.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("InflateParams")
    private void forwardMessage(String id) {
        fwdMsgId = id;
        forward.setVisibility(View.VISIBLE);
        if(fwdList.getChildCount() > 0)
            fwdList.removeAllViews();
        ProgressBar progressBr = (ProgressBar) getLayoutInflater().inflate(R.layout.image_loader, null, false);
        fwdList.addView(progressBr);
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("myId", myIDtoString)
                .build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(Constants.forwardListUrl)
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        try(okhttp3.Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseString = Objects.requireNonNull(response.body()).string();
                progressBr.clearAnimation();
                fwdList.removeView(progressBr);
                JSONArray dataArray = new JSONArray(responseString);
                for(int i = 0; i < dataArray.length(); i++){
                    String dataStr = dataArray.getString(i);
                    JSONObject dataObj = new JSONObject(dataStr);
                    int userId = dataObj.getInt("user");
                    String fName = dataObj.getString("fName");
                    String lName = dataObj.getString("lName");
                    String name = fName + ' ' + lName;
                    String userName = dataObj.getString("userName");
                    String photo = dataObj.getString("photo");
                    photo = Constants.www + photo;
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(userName);
                    jsonArray.put(fName);
                    jsonArray.put(lName);
                    allers.put(userName, jsonArray);
                    LinearLayout listView = (LinearLayout) getLayoutInflater().inflate(R.layout.listings, null);
                    ImageView imgView =  listView.findViewById(R.id.photo);
                    TextView nameTV =  listView.findViewById(R.id.name);
                    listView.setTag(userName);
                    imageLoader.displayImage(photo, imgView);
                    nameTV.setText(name);
                    listView.setOnClickListener(v -> {
                        try {
                            checkSelected(userId, userName, nameTV);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                    fwdList.addView(listView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"InflateParams", "SetTextI18n", "ClickableViewAccessibility"})
    private void sendForwardMessage() throws Throwable {
        frwdMessage.setVisibility(View.GONE);
        forward.setVisibility(View.GONE);
        if(selectedFwdrs.length() > 0 && !(fwdMsgId == null)){
            JSONObject emitObj = new JSONObject();
            long time = new Date().getTime() / 1000;
            @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            JSONObject msgData = jsonObject.getJSONObject(fwdMsgId);
            String userFrom = msgData.getString("userFrom");
            String msgBody = msgData.getString("msgBody");
            String filesStr = msgData.getString("files");
            String oriFiles = msgData.getString("oriFiles");
            boolean audioType = msgData.getBoolean("audioType");
            boolean hasLink = msgData.getBoolean("hasLink");
            String newFiles = "";
            if(!StringUtils.isEmpty(filesStr)) {
                JSONArray jsonArray = new JSONArray(filesStr.split(","));
                newFiles = jsonArray.toString();
            }
            emitObj.put("userFrom", myId);
            emitObj.put("userTo", user);
            emitObj.put("msgBody", msgBody);
            emitObj.put("files", newFiles);
            emitObj.put("newFiles", oriFiles);
            emitObj.put("linkData", null);
            emitObj.put("type", String.valueOf(audioType));
            emitObj.put("msgRef", 0);
            emitObj.put("sentCnt", 0);
            emitObj.put("hasLink", hasLink);
            emitObj.put("msgRefData", "");
            emitObj.put("dateX", date);
            emitObj.put("time", time);
            emitObj.put("audioType", audioType);
            emitObj.put("date", "Just Now");
            if(selectedFwdrs.has(userName)){
                emitObj.put("msgId", msgId);
                if(!(lastMsgView == null)) {
                    ImageButton msgSeen = lastMsgView.findViewById(R.id.msgSeen);
                    runOnUiThread(() -> {
                        if(!(msgSeen == null))
                            msgSeen.setBackgroundResource(R.color.white);
                    });
                }
                sentCnt++;
                klind.put(String.valueOf(time), sentCnt);
                boolean copyable = true;
                RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.red_message, null);
                lastMsgView = relativeLayout;
                relativeLayout.setTag(time);
                newLayouts.put(String.valueOf(time), relativeLayout);
                ImageView imageView =  relativeLayout.findViewById(R.id.profPic);
                TextView textView =  relativeLayout.findViewById(R.id.message);
                TextView dateTxt =  relativeLayout.findViewById(R.id.date);
                RelativeLayout messageHolder =  relativeLayout.findViewById(R.id.messageHolder);
                LinearLayout linkDisplay =  relativeLayout.findViewById(R.id.linkDisplay);
                ImageButton msgSeen = relativeLayout.findViewById(R.id.msgSeen);
                if(!StringUtils.isEmpty(msgBody))
                    textView.setText(msgBody);
                else
                    copyable = false;
                boolean finalCopyable = copyable;
                dateTxt.setText("Just Now");
                dateTxt.setTag(time);
                relativeObject.put(String.valueOf(time), true);
                textViews.put(String.valueOf(sentCnt), dateTxt);
                imageLoader.displayImage(myPht, imageView);
                msgSeen.setBackgroundResource(R.drawable.ic_msg_sending);
                if(hasLink){
                    JSONArray rArr;
                    String firstLink = getFirstLink(msgBody);
                    if(!(savedUrls.has(firstLink)))
                        rArr = new JSONArray();
                    else
                        rArr = savedUrls.getJSONArray(firstLink);
                    rArr.put(relativeLayout);
                    savedUrls.put(firstLink, rArr);
                    getLinkContent(firstLink);
                } else {
                    linkDisplay.setVisibility(View.GONE);
                }
                if(!StringUtils.isEmpty(filesStr)) {
                    JSONArray files = new JSONArray(filesStr);
                    if(audioType){
                        audioMsgId--;
                        RelativeLayout audioView =  relativeLayout.findViewById(R.id.audioView);
                        TextView audioTimer =  relativeLayout.findViewById(R.id.audioTimer);
                        ImageButton audioPlayPause =  relativeLayout.findViewById(R.id.audioPlayPause);
                        LinearLayout audioLength =  relativeLayout.findViewById(R.id.audioLength);
                        LinearLayout audioProgress =  relativeLayout.findViewById(R.id.audioProgress);
                        audioView.setVisibility(View.VISIBLE);
                        String audioFile = Constants.www + oriFiles;
                        String audioTime = Functions.getMediaTime(audioFile);
                        audioTimer.setText(audioTime);
                        audioView.setOnLongClickListener(v -> {
                            openMessageOptions(null, finalCopyable, true, dateTxt, textView, false);
                            return true;
                        });
                        audioPlayPause.setOnClickListener(v -> playPauseAudio(audioPlayPause, audioLength, audioProgress, audioFile, String.valueOf(audioMsgId)));
                    } else {
                        GridView filesGrid =  relativeLayout.findViewById(R.id.filesGrid);
                        filesGrid.setVisibility(View.VISIBLE);
                        int filesSize = files.length();
                        int rem = 0;
                        int newW = imgW - 20;
                        int gridH = newW;
                        int chosenSize = filesSize;
                        if (chosenSize > 4) {
                            chosenSize = 4;
                            rem = filesSize - 3;
                        }
                        if (chosenSize > 2)
                            gridH += newW;
                        if (Integer.parseInt(userFrom) == user) {
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            layoutParams.setMargins(0, 0, 180, 0);
                            messageHolder.setLayoutParams(layoutParams);
                            messageHolder.requestLayout();
                        }
                        filesGrid.getLayoutParams().height = gridH;
                        String[] msgFiles = new String[chosenSize];
                        ArrayList<String> strings = new ArrayList<>();
                        for (int i = 0; i < filesSize; i++) {
                            String fileUrl = Constants.www + files.getString(i);
                            strings.add(fileUrl);
                            if(i < chosenSize)
                                msgFiles[i] = fileUrl;
                        }
                        imageAdaptor = new ImageAdaptor(cntxt, itemLists, msgFiles, newW, false, rem, strings, filesSize);
                        filesGrid.setAdapter(imageAdaptor);
                        filesGrid.setOnLongClickListener(v -> {
                            openMessageOptions(null, finalCopyable, true, dateTxt, textView, false);
                            return true;
                        });
                    }
                }
                textView.setOnLongClickListener(v -> {
                    openMessageOptions(null, finalCopyable, true, dateTxt, textView, false);
                    return true;
                });
                messageHolder.setOnLongClickListener(v -> {
                    openMessageOptions(null, finalCopyable, true, dateTxt, textView, false);
                    return true;
                });
                messageDisplay.addView(relativeLayout);
                relativeLayout.post(() -> scrollToView(relativeLayout, false));
                relativeObject.put(String.valueOf(time), emitObj);
                relativeLayout.setOnTouchListener((v, event) -> {
                    hideSoftKeyboard(v);
                    return false;
                });
                lastMsgFromMe = true;
                messageObject.put("lastMsgFromMe", lastMsgFromMe);
                @SuppressLint("ResourceType") TextView tView = menuLayout.findViewById(5);
                if(!(tView == null)){
                    String text = "Move to Archive";
                    tView.setTag(false);
                    tView.setText(text);
                    msgOptionVals.put("archive", false);
                    String optionVals = msgOptionVals.toString(0);
                    messageObject.put("optionVals", optionVals);
                    setMessageObject("archive", text);
                    saveMessageContent();
                }
            }
            if(!(socket == null)) {
                JSONArray users = new JSONArray();
                JSONArray array = selectedFwdrs.names();
                for(int i = 0; i < Objects.requireNonNull(array).length(); i++){
                    String key = array.getString(i);
                    String newUser = selectedFwdrs.getString(key);
                    users.put(newUser);
                }
                emitObj.put("usersTo", users);
                socket.emit("sendForwardMessage", emitObj);
                selectedFwdrs = new JSONObject();
            }
        }
    }

    private void checkSelected(int userTo, String userName, TextView nameTV) throws JSONException {
        int drw = R.drawable.ic_check_true_red;
        if(selectedFwdrs.has(userName)) {
            selectedFwdrs.remove(userName);
            nameTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            selectedFwdrs.put(userName, userTo);
            nameTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, drw, 0);
        }
        if(selectedFwdrs.length() > 0)
            frwdMessage.setVisibility(View.VISIBLE);
        else
            frwdMessage.setVisibility(View.GONE);
    }

    private void checkBlockUpdate() throws JSONException {
        if(updatedBlocks.has(msgId)){
            JSONObject jsonObject = updatedBlocks.getJSONObject(msgId);
            int userTo = jsonObject.getInt("userTo");
            boolean value = jsonObject.getBoolean("value");
            boolean newVal = !value;
            if (user == userTo) {
                String text = "Block Messages from " + fCharName;
                if (newVal)
                    text = "Unblock Messages from " + fCharName;
                msgOptions.put("block", text);
                msgOptionVals.put("block", newVal);
                TextView optTV = (TextView) menuTextViews.get("block");
                String finalText = text;
                setMessageObject("block", text);
                runOnUiThread(() -> {
                    optTV.setTag(newVal);
                    optTV.setText(finalText);
                });
            }
            messageObject.put("access", value);
            saveMessageContent();
            blockMessage(value);
        }
    }

    private void checkFavUpdate() throws JSONException {
        if(updatedFavs.has(msgId)){
            JSONObject jsonObject = updatedFavs.getJSONObject(msgId);
            boolean value = jsonObject.getBoolean("value");
            boolean newVal = !value;
            String text = "Remove from Favourites";
            if(value)
                text = "Mark as Favourite";
            msgOptions.put("favourite", text);
            msgOptionVals.put("favourite", newVal);
            //messageObject.put("optionVals", msgOptionVals);
            TextView optTV = (TextView) menuTextViews.get("favourite");
            String finalText = text;
            setMessageObject("favourite", text);
            runOnUiThread(() -> {
                optTV.setTag(newVal);
                optTV.setText(finalText);
            });
            saveMessageContent();
        }
    }

    private void checkArchUpdate() throws JSONException {
        if(updatedArchs.has(msgId)){
            JSONObject jsonObject = updatedArchs.getJSONObject(msgId);
            boolean value = jsonObject.getBoolean("value");
            boolean newVal = !value;
            String text = "Remove from Archive";
            if(value)
                text = "Move to Archive";
            msgOptions.put("archive", text);
            msgOptionVals.put("archive", newVal);
            TextView optTV = (TextView) menuTextViews.get("archive");
            String finalText = text;
            setMessageObject("archive", text);
            runOnUiThread(() -> {
                optTV.setTag(newVal);
                optTV.setText(finalText);
            });
            saveMessageContent();
        }
    }

    private void checkSoundUpdate() throws JSONException {
        if(updatedSounds.has(msgId)){
            int drawableLeft = R.drawable.ic_checked;
            JSONObject jsonObject = updatedSounds.getJSONObject(msgId);
            boolean value = jsonObject.getBoolean("value");
            boolean newVal = !value;
            if(value)
                drawableLeft = 0;
            msgOptionVals.put("sound", newVal);
            TextView optTV = (TextView) menuTextViews.get("sound");
            int finalDrawableLeft = drawableLeft;
            runOnUiThread(() -> {
                optTV.setTag(newVal);
                optTV.setCompoundDrawablesWithIntrinsicBounds(finalDrawableLeft, 0, 0, 0);
            });
            saveMessageContent();
        }
    }

    private void checkOffUpdate() throws JSONException {
        if(updatedOffs.has(msgId)){
            int drawableLeft = R.drawable.ic_checked;
            JSONObject jsonObject = updatedOffs.getJSONObject(msgId);
            boolean value = jsonObject.getBoolean("value");
            boolean newVal = !value;
            if(value)
                drawableLeft = 0;
            msgOptionVals.put("online", newVal);
            TextView optTV = (TextView) menuTextViews.get("online");
            int finalDrawableLeft = drawableLeft;
            runOnUiThread(() -> {
                optTV.setTag(newVal);
                optTV.setCompoundDrawablesWithIntrinsicBounds(finalDrawableLeft, 0, 0, 0);
            });
            saveMessageContent();
        }
    }

    private void sendCuedLinks() throws JSONException {
        if(savedUrls.length() > 0){
            String urlLink = Objects.requireNonNull(savedUrls.names()).getString(0);
            getLinkContent(urlLink);
        }
    }

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    private void displayNewMessages(String id) throws Throwable {
        int dataLen = relativeObject.length(), count = 0;
        for (int x = 0; x < dataLen; x++){
            count++;
            String dataKey = Objects.requireNonNull(relativeObject.names()).getString(x);
            JSONObject msgData = new JSONObject(relativeObject.getString(dataKey));
            String userFrom = msgData.getString("userFrom");
            String msgBody = msgData.getString("msgBody");
            int msgRefX = msgData.getInt("msgRef");
            String msgRefData = msgData.getString("msgRefData");
            String filesStr = msgData.getString("files");
            String date = msgData.getString("date");
            long time = msgData.getLong("time");
            boolean audioType = msgData.getBoolean("audioType");
            boolean hasLink = hasLink(msgBody);
            boolean dragLeft = false;
            RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.ash_message, null);
            String image = photo;
            if(Integer.parseInt(userFrom) == myId) {
                dragLeft = true;
                relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.red_message, null);
                image = myPht;
            }
            ImageView imageView =  relativeLayout.findViewById(R.id.profPic);
            TextView textView =  relativeLayout.findViewById(R.id.message);
            TextView dateTxt =  relativeLayout.findViewById(R.id.date);
            RelativeLayout messageHolder =  relativeLayout.findViewById(R.id.messageHolder);
            LinearLayout linkDisplay = relativeLayout.findViewById(R.id.linkDisplay);
            ImageButton msgSeen = relativeLayout.findViewById(R.id.msgSeen);
            relativeLayout.setTag(time);
            if(!StringUtils.isEmpty(msgBody))
                textView.setText(msgBody);
            dateTxt.setText(date);
            dateTxt.setTag(time);
            imageLoader.displayImage(image, imageView);
            if(hasLink){
                JSONArray rArr;
                String firstLink = getFirstLink(msgBody);
                if(!(savedUrls.has(firstLink)))
                    rArr = new JSONArray();
                else
                    rArr = savedUrls.getJSONArray(firstLink);
                rArr.put(relativeLayout);
                savedUrls.put(firstLink, rArr);
                getLinkContent(firstLink);
            } else {
                linkDisplay.setVisibility(View.GONE);
            }
            if(msgRefX > 0){
                LinearLayout messageRef = relativeLayout.findViewById(R.id.messageRef);
                TextView msgRefName = relativeLayout.findViewById(R.id.msgRefName);
                TextView msgRefDisplay = relativeLayout.findViewById(R.id.msgRefDisplay);
                messageRef.setVisibility(View.VISIBLE);
                JSONObject object = new JSONObject(msgRefData);
                String userFromRef = object.getString("userFrom");
                String msgBodyRef = object.getString("msgBody");
                String filesStrRef = object.getString("files");
                boolean audioTypeRef = object.getBoolean("type");
                int drawableLeft = 0;
                String textFrom = "You";
                if(Integer.parseInt(userFromRef) == user)
                    textFrom = name;
                if(!StringUtils.isEmpty(filesStrRef)){
                    boolean fileImg = false, fileVid = false;
                    String[] filesRef = filesStrRef.split(",");
                    for (String s : filesRef) {
                        String fileType = Functions.checkFileType(s);
                        
                        if (fileType.equals("image"))
                            fileImg = true;
                        if (fileType.equals("video"))
                            fileVid = true;
                    }
                    if(fileVid){
                        if(StringUtils.isEmpty(msgBodyRef))
                            msgBodyRef = "Video";
                        drawableLeft = R.drawable.ic_video_black;
                    }
                    if(fileImg){
                        if(StringUtils.isEmpty(msgBodyRef)) {
                            msgBodyRef = "Photo";
                            if (fileVid)
                                msgBodyRef += " / Video";
                        }
                        drawableLeft = R.drawable.ic_cam;
                    }
                }
                if(audioTypeRef) {
                    msgBodyRef = "Recorded Audio";
                    drawableLeft = R.drawable.ic_mic;
                }
                msgRefName.setText(textFrom);
                msgRefDisplay.setText(msgBodyRef);
                msgRefDisplay.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
                messageRef.setOnClickListener(v -> {
                    try {
                        scrollToReferredMessage(msgRefX);
                    } catch (Throwable e){
                        e.printStackTrace();
                    }
                });
                msgRefName.setOnClickListener(v -> {
                    try {
                        scrollToReferredMessage(msgRefX);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
                msgRefDisplay.setOnClickListener(v -> {
                    try {
                        scrollToReferredMessage(msgRefX);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
                messageRef.setOnLongClickListener(v -> true);
            }
            if(!StringUtils.isEmpty(filesStr)) {
                if(audioType){
                    RelativeLayout audioView = relativeLayout.findViewById(R.id.audioView);
                    TextView audioTimer = relativeLayout.findViewById(R.id.audioTimer);
                    ImageButton audioPlayPause = relativeLayout.findViewById(R.id.audioPlayPause);
                    audioView.setVisibility(View.VISIBLE);
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    long timeInMillisec = Functions.getVideoDuration(retriever, filesStr, cntxt);
                    String audioTime = Functions.convertMilliTime(timeInMillisec);
                    retriever.release();
                    audioTimer.setText(audioTime);
                    audioPlayPause.setBackgroundResource(R.drawable.rotate_black);
                    audioPlayPause.startAnimation(rotation);
                    audioView.setOnLongClickListener(v -> true);
                } else {
                    GridView filesGrid = relativeLayout.findViewById(R.id.filesGrid);
                    filesGrid.setVisibility(View.VISIBLE);
                    String[] files = filesStr.split(",");
                    int filesSize = files.length;
                    int rem = 0;
                    int newW = imgW - 20;
                    int gridH = newW;
                    int chosenSize = filesSize;
                    if (chosenSize > 4) {
                        chosenSize = 4;
                        rem = filesSize - 3;
                    }
                    if (chosenSize > 2)
                        gridH += newW;
                    if (Integer.parseInt(userFrom) == user) {
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        layoutParams.setMargins(0, 0, 180, 0);
                        messageHolder.setLayoutParams(layoutParams);
                        messageHolder.requestLayout();
                    }
                    filesGrid.getLayoutParams().height = gridH;
                    String[] msgFiles = new String[chosenSize];
                    ArrayList<String> strings = new ArrayList<>();
                    for (int i = 0; i < filesSize; i++) {
                        String fileUrl = files[i];
                        strings.add(fileUrl);
                        if(i < chosenSize)
                            msgFiles[i] = fileUrl;
                    }
                    imageAdaptor = new ImageAdaptor(cntxt, itemLists, msgFiles, newW, false, rem, strings, fSize);
                    filesGrid.setAdapter(imageAdaptor);
                    filesGrid.setOnLongClickListener(v -> true);
                }
            }
            textView.setOnLongClickListener(v -> true);
            messageHolder.setOnLongClickListener(v -> true);
            boolean finalDragLeft = dragLeft;
            RelativeLayout finalRelativeLayout = relativeLayout;
            relativeLayout.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(v);
                listenToMessageDrag(finalRelativeLayout, id, finalDragLeft, event);
                return false;
            });
            setDragForAllViews(relativeLayout, relativeLayout, id, finalDragLeft);
            messageDisplay.addView(finalRelativeLayout);
            if(count == dataLen){
                lastMsgView = relativeLayout;
                msgSeen.setBackgroundResource(R.drawable.ic_msg_sending);
                relativeLayout.post(() -> {
                    int scrllTo = finalRelativeLayout.getBottom();
                    scrllView.scrollTo(0, scrllTo);
                });
                firstLoad = false;
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setDragForAllViews(RelativeLayout relativeLayout, View view, String id, boolean dragLeft) {
        view.setOnTouchListener((v, event) -> {
            //hideSoftKeyboard(view);
            listenToMessageDrag(relativeLayout, id, dragLeft, event);
            return false;
        });
        if(view instanceof ViewGroup){
            for (int i = 0;i < ((ViewGroup) view).getChildCount(); i++){
                View v = ((ViewGroup) view).getChildAt(i);
                setDragForAllViews(relativeLayout, v, id, dragLeft);
            }
        }
    }

    private void listenToMessageDrag(RelativeLayout relativeLayout, String id, boolean dragLeft, MotionEvent event) {
        try {
            runOnUiThread(() -> menuLayoutBox.setVisibility(View.GONE));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) relativeLayout.getLayoutParams();
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                iniX = (int) event.getX();
                iniY = (int) event.getY();
                oldX = iniX;
                oldY = iniY;
                released = false;
            }
            if(event.getAction() == MotionEvent.ACTION_CANCEL) {
                released = true;
                pressed = false;
                params.leftMargin = 0;
                params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                relativeLayout.setLayoutParams(params);
                if(isDragging && draggedOut)
                    referToMessage(id);
                isDragging = false;
                draggedOut = false;
                scrllView.setEnableScrolling(true);
            }
            if(event.getAction() == MotionEvent.ACTION_UP) {
                released = true;
                pressed = false;
                params.leftMargin = 0;
                params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                relativeLayout.setLayoutParams(params);
                if(isDragging && draggedOut)
                    referToMessage(id);
                isDragging = false;
                draggedOut = false;
                scrllView.setEnableScrolling(true);
            }
            if(event.getAction() == MotionEvent.ACTION_MOVE){
                if(!released && !pressed) {
                    evtX = (int) event.getX();
                    evtY = (int) event.getY();
                    int draggedLeft = iniX - evtX;
                    int draggedRight = evtX - iniX;
                    int draggedVert = evtY - iniY;
                    int horzSwp = Math.abs(draggedRight);
                    int vertSwp = Math.abs(draggedVert);
                    if(horzSwp < vertSwp){
                        released = true;
                        scrllView.setEnableScrolling(true);
                        return;
                    }
                    if (dragLeft && horzSwp > 10 && (!(oldX < evtX) || !(draggedLeft < 0))) {
                        isDragging = true;
                        draggedOut = draggedLeft > 100;
                        scrllView.setEnableScrolling(false);
                        params.leftMargin = -draggedLeft;
                        params.width = relativeLayout.getWidth();
                        relativeLayout.setLayoutParams(params);
                    }
                    if (!dragLeft && horzSwp > 10 && (!(oldX > evtX) || !(draggedRight < 0))) {
                        isDragging = true;
                        draggedOut = draggedRight > 100;
                        scrllView.setEnableScrolling(false);
                        params.leftMargin = draggedRight;
                        params.width = relativeLayout.getWidth();
                        relativeLayout.setLayoutParams(params);
                    }
                    oldX = evtX;
                    oldY = evtY;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void copyLinkContent(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(cntxt, "Link Copied to Clipboard", Toast.LENGTH_LONG).show();
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void executeMessageOptions(TextView textView, String key) throws JSONException {
        String toastText = null, text = null;
        JSONObject emitObj = new JSONObject();
        int drawableLeft = R.drawable.ic_checked;
        boolean value = Boolean.parseBoolean(textView.getTag().toString());
        if(!msgOptionVals.isNull(key)){
            textView.setTag(!value);
            msgOptionVals.put(key, !value);
            String optionVals = msgOptionVals.toString(0);
            messageObject.put("optionVals", optionVals);
            if(excludedKeys.isNull(key)) {
                if (value)
                    drawableLeft = 0;
                textView.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
            }
        }
        switch (key){
            case "sound":
                toastText = "Chat Sound Enabled for " + fCharName;
                if(value)
                    toastText = "Chat Sound Disabled for " + fCharName;
                emitObj.put("userFrom", myId);
                emitObj.put("userTo", user);
                emitObj.put("value", value);
                emitObj.put("msgId", Integer.valueOf(msgId));
                break;
            case "online":
                toastText = "You are offline for " + fCharName;
                if(value)
                    toastText = "You are online for " + fCharName;
                emitObj.put("userFrom", myId);
                emitObj.put("userTo", user);
                emitObj.put("value", value);
                emitObj.put("msgId", Integer.valueOf(msgId));
                break;
            case "media":
            case "links":
                menuLayoutBox.setVisibility(View.GONE);
                openMediaLinks(key);
                break;
            case "favourite":
                toastText = "Marked as Favourite";
                text = "Remove from Favourites";
                if(value) {
                    toastText = "Removed from favourites";
                    text = "Mark as Favourite";
                }
                emitObj.put("userFrom", myId);
                emitObj.put("userTo", user);
                emitObj.put("msgId", msgId);
                emitObj.put("value", value);
                break;
            case "archive":
                toastText = "Moved to Archive";
                text = "Remove from Archive";
                if(value) {
                    toastText = "Removed from Archive";
                    text = "Move to Archive";
                }
                emitObj.put("userFrom", myId);
                emitObj.put("userTo", user);
                emitObj.put("msgId", msgId);
                emitObj.put("value", value);
                break;
            case "report":
                menuLayoutBox.setVisibility(View.GONE);
                String[] opts = new String[]{
                        fCharName + " is feeling depressed and might hurt " + pronoun,
                        fCharName + " is sending suspicious links",
                        fCharName + " is sending sexual explicit contents",
                        fCharName + " is sending insulting and abusive words",
                        "I don't want to chat with " + fCharName
                };
                RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
                LinearLayout optLayer = postOptView.findViewById(R.id.optLayer);
                LinearLayout tvw = (LinearLayout) getLayoutInflater().inflate(R.layout.text_view, null);
                TextView txtHead = tvw.findViewById(R.id.head);
                TextView txtBody = tvw.findViewById(R.id.body);
                txtHead.setText("Help Us Understand What's Happening");
                txtBody.setText("Why do you want to report this conversation?");
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
                            @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            emitObj.put("date", date);
                            emitObj.put("user", myId);
                            emitObj.put("dataId", user);
                            emitObj.put("type", "message");
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
            case "delete":
                menuLayoutBox.setVisibility(View.GONE);
                View blockViewX = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                TextView txterX = blockViewX.findViewById(R.id.txter);
                Button cnclBtnX = blockViewX.findViewById(R.id.cancel);
                Button agreeBtnX = blockViewX.findViewById(R.id.agree);
                txterX.setText("Deleting this chat is irreversible.\n\nAre you sure you want to delete this Chat?");
                agreeBtnX.setOnClickListener(v -> {
                    blackFade.setVisibility(View.GONE);
                    try {
                        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        emitObj.put("date", date);
                        emitObj.put("userFrom", myId);
                        emitObj.put("userTo", user);
                        emitObj.put("msgId", msgId);
                        socket.emit("deleteChat", emitObj);
                        socket.emit("removeMessagePage", myId);
                        socket.emit("disconnected", myId);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                cnclBtnX.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
                blackFade.addView(blockViewX);
                blackFade.setVisibility(View.VISIBLE);
                break;
            case "block":
                menuLayoutBox.setVisibility(View.GONE);
                View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                TextView txter = blockView.findViewById(R.id.txter);
                Button cnclBtn = blockView.findViewById(R.id.cancel);
                Button agreeBtn = blockView.findViewById(R.id.agree);
                String txt = "Are you sure you want to block " + fCharName + " from chatting You?";
                if(value)
                    txt = "Are you sure you want to unblock " + fCharName + " from chatting You?";
                txter.setText(txt);
                agreeBtn.setOnClickListener(v -> {
                    blackFade.setVisibility(View.GONE);
                    try {
                        String toastText1 = "You have blocked " + fCharName;
                        String text1 = "Unblock Messages from " + fCharName;
                        if(value) {
                            toastText1 = "You have unblocked " + fCharName;
                            text1 = "Block Messages from " + fCharName;
                        }
                        messageObject.put("access", value);
                        setMessageObject(key, text1);
                        saveMessageContent();
                        textView.setText(text1);
                        Toast.makeText(cntxt, toastText1, Toast.LENGTH_LONG).show();
                        blockMessage(value);
                        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        emitObj.put("date", date);
                        emitObj.put("userFrom", myId);
                        emitObj.put("userTo", user);
                        emitObj.put("msgId", msgId);
                        emitObj.put("value", value);
                        emitObj.put("name", name);
                        socket.emit("blockChat", emitObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                cnclBtn.setOnClickListener(v -> {
                    try {
                        msgOptionVals.put(key, value);
                        String optionVals = msgOptionVals.toString(0);
                        messageObject.put("optionVals", optionVals);
                        textView.setTag(value);
                        saveMessageContent();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    blackFade.setVisibility(View.GONE);
                });
                blackFade.addView(blockView);
                blackFade.setVisibility(View.VISIBLE);
                break;
        }
        if(emitObj.length() > 0)
            socket.emit(key, emitObj);
        if(!(text == null)) {
            setMessageObject(key, text);
            textView.setText(text);
        }
        if(!(toastText == null))
            Toast.makeText(cntxt, toastText, Toast.LENGTH_LONG).show();
        saveMessageContent();
    }

    private void setMessageObject(String key, String text) {
        try {
            msgOptions.put(key, text);
            String option = msgOptions.toString(0);
            messageObject.put("options", option);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void blockMessage(boolean value) {
        hideSoftKeyboard(mainHomeView);
        cancelMsgRef(true);
        resetGridView(true);
        runOnUiThread(() -> {
            if(value && finalAccess)
                blockLayout.setVisibility(View.GONE);
            else
                blockLayout.setVisibility(View.VISIBLE);
        });
    }

    @SuppressLint("InflateParams")
    private void openMediaLinks(String key) {
        if(mediaLinksDisplay.getChildCount() > 0)
            mediaLinksDisplay.removeAllViews();
        hed.setVisibility(View.GONE);
        progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.image_loader, null);
        mediaLinksDisplay.addView(progressBar);
        mediaLinks.setVisibility(View.VISIBLE);
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("msgId", msgId)
                .addFormDataPart("type", key)
                .addFormDataPart("myId", myIDtoString)
                .build();
        Request request = new Request.Builder()
                .url(Constants.mediaLinksUrl)
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
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
                runOnUiThread(() -> {
                    progressBar.clearAnimation();
                    progressBar.setVisibility(View.GONE);
                    hed.setVisibility(View.VISIBLE);
                });
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    int count = jsonObject.getInt("count");
                    JSONArray dataArr = jsonObject.getJSONArray("data");
                    switch (key){
                        case "media":
                            runOnUiThread(() -> txtHed.setText("Media Files"));
                            ArrayList<String> strings = new ArrayList<>();
                            for(int u = 0; u < dataArr.length(); u++){
                                JSONObject dataObj = new JSONObject(dataArr.getString(u));
                                String userKey = Objects.requireNonNull(dataObj.names()).getString(0);
                                JSONArray mediaFiles = new JSONArray(dataObj.getString(userKey));
                                int userFrom = Integer.parseInt(userKey);
                                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.grid_view, null, false);
                                TextView textView =  linearLayout.findViewById(R.id.textView);
                                GridView filesGrid =  linearLayout.findViewById(R.id.gridView);
                                String text = fCharName;
                                if(myId == userFrom){
                                    text = "You";
                                    textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                                }
                                textView.setText(text);
                                int filesSize = mediaFiles.length();
                                String[] msgFiles = new String[filesSize];
                                for (int i = 0; i < filesSize; i++) {
                                    String fileUrl = Constants.www + mediaFiles.getString(i);
                                    strings.add(fileUrl);
                                    msgFiles[i] = fileUrl;
                                }
                                int newW = imgW - 20, gridH = filesSize / 3, rem = filesSize % 3;
                                if(rem > 0)
                                    gridH += 1;
                                gridH *= (newW + 15);
                                filesGrid.getLayoutParams().height = gridH;
                                imageAdaptor = new ImageAdaptor(cntxt, itemLists, msgFiles, newW, false, 0, strings, count);
                                filesGrid.setAdapter(imageAdaptor);
                                runOnUiThread(() -> mediaLinksDisplay.addView(linearLayout));
                            }
                            break;
                        case "links":
                            runOnUiThread(() -> txtHed.setText("Links"));
                            int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
                            for(int u = 0; u < dataArr.length(); u++){
                                JSONObject dataObj = new JSONObject(dataArr.getString(u));
                                String userKey = Objects.requireNonNull(dataObj.names()).getString(0);
                                JSONArray linkData = new JSONArray(dataObj.getString(userKey));
                                int userFrom = Integer.parseInt(userKey);
                                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.recycler, null, false);
                                TextView textView = linearLayout.findViewById(R.id.textView);
                                RecyclerView recyclerView =  linearLayout.findViewById(R.id.recyclerView);
                                recyclerView.setLayoutManager(new LinearLayoutManager(actvty, LinearLayoutManager.VERTICAL, false));
                                recyclerView.setItemAnimator(new DefaultItemAnimator());
                                recyclerView.addItemDecoration(new SpaceItemDecoration(space));
                                String text = fCharName;
                                if(myId == userFrom){
                                    text = "You";
                                    textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                                }
                                textView.setText(text);
                                linksAdapter = new LinksAdapter(cntxt, linkData, count);
                                recyclerView.setAdapter(linksAdapter);
                                runOnUiThread(() -> mediaLinksDisplay.addView(linearLayout));
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void openLink(Context context, String linkUrl) {
        if(!cleanUrl(linkUrl))
            linkUrl = "http://" + linkUrl;
        Uri uri = Uri.parse(linkUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public boolean hasLink(String text){
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    public String getFirstLink(String text) throws MalformedURLException {
        Matcher matcher = pattern.matcher(text);
        Boolean f = matcher.find();
        ArrayList<String> list = new ArrayList<>();
        list.add(text.substring(matcher.start(0), matcher.end(0)));
        String fullUrl = list.get(0).replaceAll(" ", "");
        if(!cleanUrl(fullUrl))
            fullUrl = "http://" + fullUrl;
        URL url = new URL(fullUrl);
        String host = url.getHost();
        String path = url.getPath();
        return host.concat(path);
    }

    public static boolean cleanUrl(String urlStr){
        boolean strts1 = urlStr.startsWith("https://"), strts2 = urlStr.startsWith("http://");
        return strts1 || strts2;
    }

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    private void setUpMenuOption() throws JSONException {
        if(menuLayout.getChildCount() > 0) runOnUiThread(() -> menuLayout.removeAllViews());
        for(int i = 0; i < msgOptions.length(); i++){
            String key = Objects.requireNonNull(msgOptions.names()).getString(i);
            String option = msgOptions.getString(key);
            TextView optionTxt = (TextView) LayoutInflater.from(cntxt).inflate(R.layout.option_txt, null, false);
            menuTextViews.put(key, optionTxt);
            if(!msgOptionVals.isNull(key) && msgOptionVals.getBoolean(key)) {
                if(excludedKeys.isNull(key))
                    optionTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
                optionTxt.setTag(true);
            } else if(!msgOptionVals.isNull(key) && !msgOptionVals.getBoolean(key)) {
                if(excludedKeys.isNull(key))
                    optionTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                optionTxt.setTag(false);
            }
            optionTxt.setText(option);
            optionTxt.setId(i);
            if(!menuOptionsSet) {
                optionTxt.setOnClickListener((v) -> {
                    try {
                        executeMessageOptions(optionTxt, key);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
            runOnUiThread(() -> menuLayout.addView(optionTxt));
        }
        menuOptionsSet = true;
    }

    @SuppressLint("InflateParams")
    private void deleteMessage(String id, boolean deletable, TextView textView) {
        if(blackFade.getChildCount() > 0) blackFade.removeAllViews();
        long oldTime = (long) textView.getTag();
        View blockView = LayoutInflater.from(cntxt).inflate(R.layout.delete_layout, null, false);
        TextView cancel =  blockView.findViewById(R.id.cancel);
        TextView deleteForMe =  blockView.findViewById(R.id.deleteForMe);
        TextView deleteForAll =  blockView.findViewById(R.id.deleteForAll);
        if(deletable)
            deletable = checkDeletable(oldTime);
        if(deletable)
            deleteForAll.setVisibility(View.VISIBLE);
        boolean finalDeletable = deletable;
        cancel.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
        deleteForMe.setOnClickListener(v -> showDeleteConfirm(id, finalDeletable, oldTime));
        deleteForAll.setOnClickListener(v -> showDeleteConfirm(id, finalDeletable, oldTime));
        blackFade.addView(blockView);
        blackFade.setVisibility(View.VISIBLE);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void showDeleteConfirm(String id, boolean deletable, long time) {
        View blockViewX = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        TextView txterX = blockViewX.findViewById(R.id.txter);
        Button cnclBtnX = blockViewX.findViewById(R.id.cancel);
        Button agreeBtnX = blockViewX.findViewById(R.id.agree);
        txterX.setText("Are you sure you want to delete this Message?");
        agreeBtnX.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            if(!(id == null))
                confirmMessageDelete(id, deletable);
            else {
                try {
                    int sCntX = klind.getInt(String.valueOf(time));
                    deletePendingMessage(time, sCntX);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        cnclBtnX.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
        blackFade.addView(blockViewX);
    }

    private void confirmMessageDelete(String id, boolean deletable) {
        try {
            RelativeLayout relativeLayout = (RelativeLayout) relObject.get(id);
            relativeLayout.setVisibility(View.GONE);
            blackFade.setVisibility(View.GONE);
            relObject.remove(id);
            String data = messageObject.getString("data");
            JSONObject dataObject = new JSONObject(data);
            JSONArray dataKeys = dataObject.names();
            
            int lastIndex = dataKeys.length() - 1;
            String lastKey = dataKeys.getString(lastIndex);
            if(Integer.parseInt(id) == Integer.parseInt(lastKey) && lastIndex > 0){
                lastIndex--;
                lastKey = dataKeys.getString(lastIndex);
                String newData = dataObject.getString(lastKey);
                JSONObject newObj = new JSONObject(newData);
                int userFrom = newObj.getInt("userFrom");
                if(userFrom == myId){
                    lastMsgFromMe = true;
                    int dlvd = newObj.getInt("dlvd");
                    int seen = newObj.getInt("seen");
                    RelativeLayout relativeLayoutX = (RelativeLayout) relObject.get(lastKey);
                    ImageButton msgSeen =  relativeLayoutX.findViewById(R.id.msgSeen);
                    runOnUiThread(() -> {
                        if (dlvd > 0)
                            msgSeen.setBackgroundResource(R.drawable.ic_msg_dlvd);
                        else if (seen > 0)
                            msgSeen.setBackgroundResource(R.drawable.ic_msg_seen);
                        else
                            msgSeen.setBackgroundResource(R.drawable.ic_msg_sent);
                    });
                } else
                    lastMsgFromMe = false;
                messageObject.put("lastMsgFromMe", lastMsgFromMe);
            }
            dataObject.remove(id);
            data = dataObject.toString(0);
            messageObject.put("data", data);
            saveMessageContent();
            JSONObject emitObj = new JSONObject();
            emitObj.put("id", id);
            emitObj.put("userFrom", myId);
            emitObj.put("userTo", user);
            emitObj.put("msgId", msgId);
            emitObj.put("deletable", deletable);
            socket.emit("deleteMessage", emitObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean checkDeletable(long oldTime){
        long time = new Date().getTime() / 1000;
        long timeDiff = time - oldTime;
        double minutes = (double) (timeDiff / 60);
        return minutes < 10;
    }

    @SuppressLint("InflateParams")
    private void scrollToReferredMessage(int msgRefId) throws JSONException {
        String refMsgId = String.valueOf(msgRefId);
        if(!relObject.isNull(refMsgId)){
            View view = (View) relObject.get(refMsgId);
            view.setBackgroundResource(R.color.yellowLight);
            scrollToView(view, true);
        } else if(msgRefId > maxId) {
            Toast.makeText(cntxt, "Message Deleted", Toast.LENGTH_LONG).show();
        } else if(!loading){
            targetId = msgRefId;
            loading = true;
            scrllView.scrollTo(0, 0);
            progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.image_loader, null);
            messageDisplay.addView(progressBar, 0);
            progressBar.post(this::loadMessages);
        }
    }

    private void referToMessage(String id) throws JSONException {
        msgRef = Integer.parseInt(id);
        referLayout.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)scrllView.getLayoutParams();
        layoutParams.bottomMargin = 220;
        scrllView.setLayoutParams(layoutParams);
        JSONObject object = jsonObject.getJSONObject(id);
        jsonString = object.toString();
        String userFrom = object.getString("userFrom");
        String msgBody = object.getString("msgBody");
        String filesStr = object.getString("files");
        boolean audioType = object.getBoolean("audioType");
        boolean hasLink = object.getBoolean("hasLink");
        int drawableLeft = 0;
        String textFrom = "You";
        if(Integer.parseInt(userFrom) == user)
            textFrom = name;
        if(hasLink) {
            if(StringUtils.isEmpty(msgBody))
                msgBody = "Link";
            drawableLeft = R.drawable.ic_copy_link;
        }
        if(!StringUtils.isEmpty(filesStr)){
            boolean fileImg = false, fileVid = false;
            JSONArray files = new JSONArray(filesStr);
            for(int i = 0; i < files.length(); i++){
                String fileType = Functions.checkFileType(files.getString(i));
                
                if(fileType.equals("image"))
                    fileImg = true;
                if(fileType.equals("video"))
                    fileVid = true;
            }
            if(fileVid){
                if(StringUtils.isEmpty(msgBody))
                    msgBody = "Video";
                drawableLeft = R.drawable.ic_video_black;
            }
            if(fileImg){
                if(StringUtils.isEmpty(msgBody)) {
                    msgBody = "Photo";
                    if (fileVid)
                        msgBody += " / Video";
                }
                drawableLeft = R.drawable.ic_cam;
            }
        }
        if(audioType) {
            msgBody = "Recorded Audio";
            drawableLeft = R.drawable.ic_mic;
        }
        refName.setText(textFrom);
        refDisplay.setText(msgBody);
        refDisplay.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
        msgTxt.requestFocus();
    }

    private void cancelMsgRef(boolean clearAll) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) scrllView.getLayoutParams();
        layoutParams.bottomMargin = 100;
        runOnUiThread(() -> {
            scrllView.setLayoutParams(layoutParams);
            referLayout.setVisibility(View.GONE);
        });
        if(clearAll) {
            msgRef = 0;
            jsonString = "";
        }
    }

    private void playPauseAudio(ImageButton audioPlayPause, LinearLayout audioLength, LinearLayout audioProgress, String audioFile, String id) {
        boolean isFromMe = Boolean.parseBoolean(audioPlayPause.getTag().toString());
        if(!mediaPlayers.isNull(id)){
            if (curBtn == audioPlayPause){
                if(mediaPlayer.isPlaying()){
                    pauseMediaPlayer();
                    if(isFromMe)
                        audioPlayPause.setBackgroundResource(R.drawable.ic_play_circle_white);
                    else
                        audioPlayPause.setBackgroundResource(R.drawable.ic_play_circle_red);
                } else {
                    startMediaPlayer(audioLength, audioProgress);
                    if(isFromMe)
                        audioPlayPause.setBackgroundResource(R.drawable.ic_pause_circle_white);
                    else
                        audioPlayPause.setBackgroundResource(R.drawable.ic_pause_circle_red);

                }
            } else {
                boolean prevIsFromMe = Boolean.parseBoolean(curBtn.getTag().toString());
                if(mediaPlayer.isPlaying()) {
                    pauseMediaPlayer();
                    if(prevIsFromMe)
                        curBtn.setBackgroundResource(R.drawable.ic_play_circle_white);
                    else
                        curBtn.setBackgroundResource(R.drawable.ic_play_circle_red);
                }
                try {
                    mediaPlayer = (MediaPlayer) mediaPlayers.get(id);
                    startMediaPlayer(audioLength, audioProgress);
                    if(isFromMe)
                        audioPlayPause.setBackgroundResource(R.drawable.ic_pause_circle_white);
                    else
                        audioPlayPause.setBackgroundResource(R.drawable.ic_pause_circle_red);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if(!(mediaPlayer == null) && mediaPlayer.isPlaying()) {
                boolean prevIsFromMe = Boolean.parseBoolean(curBtn.getTag().toString());
                pauseMediaPlayer();
                if(prevIsFromMe)
                    curBtn.setBackgroundResource(R.drawable.ic_play_circle_white);
                else
                    curBtn.setBackgroundResource(R.drawable.ic_play_circle_red);
            }
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(audioFile);
                mediaPlayer.prepare();
                mediaPlayers.put(id, mediaPlayer);
                startMediaPlayer(audioLength, audioProgress);
                if(isFromMe)
                    audioPlayPause.setBackgroundResource(R.drawable.ic_pause_circle_white);
                else
                    audioPlayPause.setBackgroundResource(R.drawable.ic_pause_circle_red);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        curBtn = audioPlayPause;
    }

    private void playMessagePop() {
        if(!(popSound == null) && popSound.isPlaying())
            popSound.pause();
        popSound = MediaPlayer.create(this, R.raw.bbm_end_call);
        popSound.setLooping(false);
        popSound.start();
    }

    private void startMediaPlayer(LinearLayout audioLength, LinearLayout audioProgress) {
        mediaPlayer.start();
        curAudioLength = audioLength;
        curAudioProgress = audioProgress;
        startAudioTimer();
        mediaPlayer.setOnCompletionListener(mp -> {
            stopAudioTimer();
            boolean prevIsFromMe = Boolean.parseBoolean(curBtn.getTag().toString());
            if(prevIsFromMe)
                curBtn.setBackgroundResource(R.drawable.ic_play_circle_white);
            else
                curBtn.setBackgroundResource(R.drawable.ic_play_circle_red);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) curAudioProgress.getLayoutParams();
            layoutParams.width = 0;
            runOnUiThread(() -> curAudioProgress.setLayoutParams(layoutParams));
        });
    }

    private void startAudioTimer() {
        audioDuration = mediaPlayer.getDuration();
        audioWidth = curAudioLength.getWidth();
        audioTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                audioTimeCounter = mediaPlayer.getCurrentPosition() + 1000;
                audioProgressWidth = audioWidth / (audioDuration / 1000);
                audioProgressWidth *= (double) audioTimeCounter / 1000;
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) curAudioProgress.getLayoutParams();
                layoutParams.width = (int)audioProgressWidth;
                runOnUiThread(() -> curAudioProgress.setLayoutParams(layoutParams));
            }
        };
        audioTimer.schedule(task, 1000, 1000);
    }

    private void pauseMediaPlayer() {
        mediaPlayer.pause();
        stopAudioTimer();
    }

    private void stopAudioTimer() {
        audioTimer.cancel();
    }

    public void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
        } else if(mediaLinks.getVisibility() == View.VISIBLE){
            mediaLinks.setVisibility(View.GONE);
        }  else if(menuLayoutBox.getVisibility() == View.VISIBLE){
            menuLayoutBox.setVisibility(View.GONE);
        } else if(forward.getVisibility() == View.VISIBLE){
            forward.setVisibility(View.GONE);
            frwdMessage.setVisibility(View.GONE);
            fwdMsgId = null;
            selectedFwdrs = new JSONObject();
        } else if(gridView.getVisibility() == View.VISIBLE){
            resetGridView(true);
        } else if(referLayout.getVisibility() == View.VISIBLE){
            cancelMsgRef(true);
        } else {
            socket.emit("disconnected", myId);
            socket.emit("removeMessagePage", sockt);
            finish();
        }
    }

    private void resetGridView(boolean resetAll) {
        gridView.setVisibility(View.GONE);
        imageNum.setVisibility(View.GONE);
        allMediaList.clear();
        if(resetAll) {
            itemLists.clear();
            selectedImages.clear();
        }
    }

    private void nonStopCounter(){
        Timer timerX = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                for(int y = 0; y < textViews.length(); y++) {
                    try {
                        String key = Objects.requireNonNull(textViews.names()).getString(y);
                        TextView textViewx = (TextView) textViews.get(key);
                        long oldTime = (long) textViewx.getTag();
                        long finalOldTime = oldTime + 1;
                        runOnUiThread(() -> textViewx.setTag(finalOldTime));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                for(int x = 0; x < relObject.length(); x++) {
                    try {
                        String key = Objects.requireNonNull(relObject.names()).getString(x);
                        RelativeLayout relativeLayout = (RelativeLayout) relObject.get(key);
                        TextView dateTxt =  relativeLayout.findViewById(R.id.date);
                        long oldTime = (long) relativeLayout.getTag();
                        String date = Functions.timeConverter(oldTime, true);
                        runOnUiThread(() -> dateTxt.setText(date));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timerX.schedule(task, 0, 1000);
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if(!(view == recordAudio)) {
            view.setOnTouchListener((v, event) -> {
                if (!(view instanceof EditText))
                    hideSoftKeyboard(v);
                if (!(view == menuLayoutBox) && !(view == msgMenu))
                    menuLayoutBox.setVisibility(View.GONE);
                return false;
            });

            //If a layout container, iterate over children and seed recursion.
            if (view instanceof ViewGroup && !(view == menuLayoutBox)) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    View innerView = ((ViewGroup) view).getChildAt(i);
                    setupUI(innerView);
                }
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
