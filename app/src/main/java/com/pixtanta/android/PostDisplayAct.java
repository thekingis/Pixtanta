package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
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

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.pixtanta.android.Utils.BlurBitmap;
import com.pixtanta.android.Utils.EditableAccommodatingLatinIMETypeNullIssues;
import com.pixtanta.android.Utils.HtmlParser;
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
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
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
import static com.pixtanta.android.HomeAct.updateMessageDelivery;

public class PostDisplayAct extends ThemeActivity {

    String poster, posterImg, postType, posterName, posterUName, myName, myPht, postUser, pagerId;
    static String postId, myUserName;
    boolean loadingPost, hasPage, shakeOpt, userVerified, keyDown, hashed, onPagerScrolled, scrllVIState, vary;
    JSONArray userPages;
    private static int myId;
    int verifiedIcon, width, height, imgW, viewPagerHeight, mCount, count = 0;
    int[][] reactions, reactionsCom;
    static int[] numberOfImages;
    Context cntxt;
    FileCache fileCache;
    DisplayMetrics displayMetrics;
    SensorManager mSensorManager;
    ShakeEventListener mSensorListener;
    static Socket socket;
    JSONObject optIcons, postsOptionsIcons, likesDisplay, emitObject, pendingDel, repsDisplay, options, optionsVal;
    ImageLoader imageLoader;
    ResizedImageLoader resizedImageLoader;
    View currView;
    LinearLayout blackFade, layout, menLayout, commentBox, loadingLayer;
    @SuppressLint("StaticFieldLeak")
    static RelativeLayout postView;
    RelativeLayout mainHomeView;
    ScrollView scrllView, scrollView;
    ImageView myComPht;
    EditText editText;
    TextView likersTextView, likeNumTextView;
    @SuppressLint("StaticFieldLeak")
    static TextView curEditableView;
    ImageButton reactButton, submitCommentBtn;
    ArrayList<RelativeLayout> comLayouts = new ArrayList<>();
    ArrayList<String> selectedComms = new ArrayList<>();
    static JSONObject comLayoutsX, commentHtmlText = new JSONObject();
    public static Handler UIHandler;
    SharedPrefMngr sharedPrefMngr;
    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_display);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        sharedPrefMngr = new SharedPrefMngr(this);

        Bundle userParams = getIntent().getExtras();
        postId = userParams.getString("postId");

        sharedPrefMngr.initializeSmartLogin();
        cntxt = this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        fileCache = new FileCache(this);
        verifiedIcon = R.drawable.ic_verified_user;

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(this, LoginAct.class));
            return;
        }

        myId = sharedPrefMngr.getMyId();

        try {
            emitObject = new JSONObject();
            optIcons = new JSONObject();
            optIcons.put("copy", R.drawable.ic_copy);
            optIcons.put("edit", R.drawable.ic_edit);
            optIcons.put("reply", R.drawable.ic_reply_ash);
            optIcons.put("hide", R.drawable.ic_hide_post);
            optIcons.put("report", R.drawable.ic_report_post);
            optIcons.put("delete", R.drawable.ic_delete);
            emitObject.put("user", myId);
            emitObject.put("postID", postId);
            socket = IO.socket(socketUrl);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
                socket.emit("connected", myId);
                socket.emit("connectedCommentPage", emitObject);
            }));
            socket.connect();
        } catch (URISyntaxException | JSONException e) {
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
        mCount = 0;
        loadingPost = true;
        scrllVIState = false;
        onPagerScrolled = false;
        hashed = false;
        hasPage = false;
        userPages = new JSONArray();
        likesDisplay = new JSONObject();
        pendingDel = new JSONObject();
        repsDisplay = new JSONObject();
        comLayoutsX = new JSONObject();

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
        } catch (JSONException e) {
            e.printStackTrace();
        }

        reactions = new int[][]{
                new int[]{1, R.drawable.ic_loved},
                new int[]{0, R.drawable.ic_love}
        };
        reactionsCom = new int[][]{
                new int[]{1, R.drawable.ic_loved},
                new int[]{0, R.drawable.ic_unreacted}
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

        imageLoader = new ImageLoader(this);
        resizedImageLoader = new ResizedImageLoader(this);
        mainHomeView =  findViewById(R.id.mainHomeView);
        scrllView =  findViewById(R.id.scrllView);
        scrollView =  findViewById(R.id.scrollView);
        blackFade =  findViewById(R.id.blackFade);
        layout =  findViewById(R.id.layout);
        menLayout =  findViewById(R.id.mentionLayer);
        myComPht =  findViewById(R.id.myComPht);
        editText =  findViewById(R.id.commentBox);
        commentBox =  findViewById(R.id.commentBx);
        loadingLayer =  findViewById(R.id.loadingLayer);
        submitCommentBtn =  findViewById(R.id.submitCommentBtn);
        setupUI(mainHomeView);
        scrollView.setSmoothScrollingEnabled(true);
        imageLoader.displayImage(myPht, myComPht);

        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String allText = s.toString();
                    int caretPos = editText.getSelectionStart();
                    String text = allText.substring(0, caretPos);
                    int textLen = text.length(), textL = textLen - 1;
                    if(allText.length() > caretPos){
                        char nextChar = allText.charAt(caretPos);
                        if (!Character.isWhitespace(nextChar)) {
                            scrllView.setVisibility(View.GONE);
                            return;
                        }
                    }
                    if(textLen > 0) {
                        char lastChar = text.charAt(textL);
                        if (Character.isWhitespace(lastChar)) {
                            scrllView.setVisibility(View.GONE);
                            return;
                        }
                    }
                    String[] textArr = text.split("\\s+");
                    int lastWordIndex = textArr.length - 1;
                    String lastWord = textArr[lastWordIndex];
                    int lastWordLen = lastWord.length();
                    if(lastWordLen > 1){
                        String regex = "^#[\\w_]+$";
                        boolean matchedRegex = Pattern.matches(regex, lastWord);
                        if(!hashed && matchedRegex){
                            changeWordToHashTag(editText, lastWord);
                            return;
                        }
                        String htmlText = Html.toHtml((Spanned) editText.getText().subSequence(caretPos - lastWordLen, caretPos));
                        Document doc = Jsoup.parse(htmlText, "UTF-8");
                        Elements anchorElement = doc.getElementsByTag("a");
                        String href = anchorElement.attr("href");
                        if(href.length() == 0) {
                            JSONObject emitObj = new JSONObject();
                            emitObj.put("user", myId);
                            emitObj.put("advanced", true);
                            emitObj.put("table", "comments");
                            emitObj.put("dataId", postId);
                            emitObj.put("lastWord", lastWord);
                            socket.emit("checkMention", emitObj);
                            return;
                        }
                        scrllView.setVisibility(View.GONE);
                    } else {
                        if(lastWordLen == 1){
                            if(lastWord.equals("@")) {
                                JSONObject emitObj = new JSONObject();
                                emitObj.put("user", myId);
                                emitObj.put("advanced", true);
                                emitObj.put("table", "comments");
                                emitObj.put("dataId", postId);
                                emitObj.put("lastWord", "");
                                socket.emit("checkMention", emitObj);
                                return;
                            }
                            if(lastWord.equals("#")) {
                                String htmlText = Html.toHtml((Spanned) editText.getText().subSequence(caretPos - 1 , caretPos));
                                htmlText = htmlText.replace("<p dir=\"ltr\">", "");
                                htmlText = htmlText.replace("</p>", "");
                                String startStr = "<b><span style=\"background-color:#F9F9F9;\">";
                                if(htmlText.startsWith(startStr)) {
                                    editText.setText(editText.getText().delete(caretPos - 1, caretPos));
                                    editText.getText().insert(caretPos - 1, "#");
                                    editText.setSelection(caretPos);
                                }
                                return;
                            }
                        }
                        scrllView.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editText.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                keyDown = true;
                try {
                    if(event.getUnicodeChar() == (int) EditableAccommodatingLatinIMETypeNullIssues.ONE_UNPROCESSED_CHARACTER.charAt(0)){
                        return true;
                    } else if(keyCode == KeyEvent.KEYCODE_DEL) {
                        return overrideBackspace(editText);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                keyDown = false;
            }
            return false;
        });
        submitCommentBtn.setOnClickListener(v -> submitComment());
        scrllView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if(scrllView.getVisibility() == View.VISIBLE && !scrllVIState)
                scrllView.scrollTo(0, 0);
            scrllVIState = scrllView.getVisibility() == View.VISIBLE;
        });

        getPostDisplay();

        socket.on("postLike", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postID = String.valueOf(argsArr.getInt(0));
                if(postId.equals(postID)){
                    int likes = argsArr.getInt(1);
                    String likeNumStr = likeNumTextView.getText().toString();
                    int likeNum = Functions.convertToNumber(likeNumStr);
                    likeNum += likes;
                    int finalLikeNum = likeNum;
                    int finalLikeNum1 = likeNum;
                    runOnUiThread(() -> {
                        likeNumTextView.setText(Functions.convertToText(finalLikeNum1));
                        if(finalLikeNum > 0){
                            likersTextView.setVisibility(View.VISIBLE);
                        } else {
                            likersTextView.setVisibility(View.GONE);
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
                String postID = String.valueOf(argsArr.getInt(0));
                int user = argsArr.getInt(2);
                int tag = argsArr.getInt(3);
                if(postId.equals(postID) && user == myId){
                    int newTag = reactions[tag][0];
                    int newReact = reactions[tag][1];
                    runOnUiThread(() -> {
                        reactButton.setTag(newTag);
                        reactButton.setBackgroundResource(newReact);
                    });
                    String likeNumStr = likeNumTextView.getText().toString();
                    int likeNum = Functions.convertToNumber(likeNumStr);
                    int likes = argsArr.getInt(1);
                    likeNum += likes;
                    int finalLikeNum = likeNum;
                    int finalLikeNum1 = likeNum;
                    runOnUiThread(() -> {
                        likeNumTextView.setText(Functions.convertToText(finalLikeNum1));
                        if(finalLikeNum > 0){
                            likersTextView.setVisibility(View.VISIBLE);
                        } else {
                            likersTextView.setVisibility(View.GONE);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("reversePostLike", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                boolean subtract = argsArr.getBoolean(1);
                String likeNumStr = likeNumTextView.getText().toString();
                int likeNum = Functions.convertToNumber(likeNumStr);
                if(subtract)
                    likeNum -= 1;
                int finalLikeNum = likeNum;
                runOnUiThread(() -> {
                    reactButton.setTag(0);
                    reactButton.setBackgroundResource(R.drawable.ic_love);
                    likeNumTextView.setText(Functions.convertToText(finalLikeNum));
                    if(finalLikeNum > 0)
                        likersTextView.setVisibility(View.VISIBLE);
                    else
                        likersTextView.setVisibility(View.GONE);
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("reverseCommentLike", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String commId = String.valueOf(argsArr.getInt(0));
                if(!(comLayoutsX.isNull(commId)) && !(likesDisplay.isNull(commId))){
                    boolean subtract = argsArr.getBoolean(1);
                    RelativeLayout relativeLayout = (RelativeLayout) comLayoutsX.get(commId);
                    ImageButton imageButton = relativeLayout.findViewById(R.id.react);
                    TextView textView = (TextView) likesDisplay.get(commId);
                    String likeNumStr = textView.getText().toString();
                    int likeNum = Functions.convertToNumber(likeNumStr);
                    if(subtract)
                        likeNum -= 1;
                    int finalLikeNum = likeNum;
                    runOnUiThread(() -> {
                        imageButton.setTag(0);
                        imageButton.setBackgroundResource(R.drawable.ic_unreacted);
                        textView.setText(Functions.convertToText(finalLikeNum));
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("submitComment", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postID = String.valueOf(argsArr.getInt(1));
                if(postId.equals(postID)) {
                    String comID = String.valueOf(argsArr.getInt(0));
                    int comUser = argsArr.getInt(3);
                    String comName = argsArr.getString(4);
                    String comPht = argsArr.getString(5);
                    String comUserName = argsArr.getString(6);
                    String comment = argsArr.getString(8);
                    String type = argsArr.getString(9);
                    boolean userVrfd = argsArr.getBoolean(7);
                    commentHtmlText.put(comID, comment);
                    selectedComms.add(postID);
                    @SuppressLint("InflateParams") RelativeLayout commentView = (RelativeLayout) getLayoutInflater().inflate(R.layout.comment_layout, null);
                    LinearLayout helder =  commentView.findViewById(R.id.helder);
                    ImageView userImageView =  commentView.findViewById(R.id.profPic);
                    TextView commentText =  commentView.findViewById(R.id.commentText);
                    TextView nameTxtVw =  commentView.findViewById(R.id.posterName);
                    TextView likeNum =  commentView.findViewById(R.id.likeNum);
                    TextView userNameTxtVw =  commentView.findViewById(R.id.posterUName);
                    TextView commentDateVw =  commentView.findViewById(R.id.commentDate);
                    ImageButton reply =  commentView.findViewById(R.id.reply);
                    ImageButton react =  commentView.findViewById(R.id.react);
                    react.setTag("0");
                    imageLoader.displayImage(comPht, userImageView);
                    comLayoutsX.put(comID, commentView);
                    likesDisplay.put(comID, likeNum);
                    String htmlText = HtmlParser.parseSpan(comment);
                    htmlText = EmojiParser.parseToUnicode(htmlText);
                    CharSequence sequence = Html.fromHtml(htmlText);
                    SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                    URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                    for(URLSpan span : urls) {
                        makeLinkClickable(cntxt, strBuilder, span);
                    }
                    commentText.setText(strBuilder);
                    commentText.setMovementMethod(LinkMovementMethod.getInstance());
                    commentDateVw.setText("Just Now");
                    nameTxtVw.setText(comName);
                    if (!(comUserName == null || comUserName.equals("null")))
                        userNameTxtVw.setText("@" + comUserName);
                    if (userVrfd)
                        nameTxtVw.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                    boolean access = false;
                    if (comUser == myId)
                        access = true;
                    boolean finalAccess = access;
                    reply.setOnClickListener(v -> replyComment(commentView, comID));
                    react.setOnClickListener(v -> reactToComment(comID, v));
                    commentView.setOnLongClickListener(v -> {
                        openCommentOptions(comID, finalAccess, commentView, commentText, true);
                        return false;
                    });
                    helder.setOnLongClickListener(v -> {
                        openCommentOptions(comID, finalAccess, commentView, commentText, true);
                        return false;
                    });
                    commentText.setOnLongClickListener(v -> {
                        openCommentOptions(comID, finalAccess, commentView, commentText, true);
                        return false;
                    });
                    commentDateVw.setOnLongClickListener(v -> {
                        openCommentOptions(comID, finalAccess, commentView, commentText, true);
                        return false;
                    });
                    userImageView.setOnClickListener(v -> {
                        switch (type){
                            case "profile":
                                visitUserProfile(cntxt, comUser);
                                break;
                            case "page":
                                visitPage(cntxt, comUser);
                                break;
                        }
                    });
                    userNameTxtVw.setOnClickListener(v -> {
                        switch (type){
                            case "profile":
                                visitUserProfile(cntxt, comUser);
                                break;
                            case "page":
                                visitPage(cntxt, comUser);
                                break;
                        }
                    });
                    nameTxtVw.setOnClickListener(v -> {
                        switch (type){
                            case "profile":
                                visitUserProfile(cntxt, comUser);
                                break;
                            case "page":
                                visitPage(cntxt, comUser);
                                break;
                        }
                    });
                    runOnUiThread(() -> {
                        layout.addView(commentView);
                        setupUI(mainHomeView);
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("submitCommentId", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String comID = String.valueOf(argsArr.getInt(0));
                int cCount = argsArr.getInt(2);
                String coCount = String.valueOf(cCount);
                if(pendingDel.has(coCount)){
                    pendingDel.remove(comID);
                    deletePending(comID);
                    return;
                }
                String comment = argsArr.getString(3);
                commentHtmlText.put(comID, comment);
                RelativeLayout comLayout = comLayouts.get(cCount);
                LinearLayout helder =  comLayout.findViewById(R.id.helder);
                ImageButton reply =  comLayout.findViewById(R.id.reply);
                ImageButton react =  comLayout.findViewById(R.id.react);
                TextView replyNum =  comLayout.findViewById(R.id.replyNum);
                TextView likeNum =  comLayout.findViewById(R.id.likeNum);
                TextView commentTxtVw =  comLayout.findViewById(R.id.commentText);
                TextView commentDateVw =  comLayout.findViewById(R.id.commentDate);
                repsDisplay.put(comID, replyNum);
                likesDisplay.put(comID, likeNum);
                comLayoutsX.put(comID, comLayout);
                runOnUiThread(() -> {
                    react.setTag("0");
                    comLayout.setOnLongClickListener(v -> {
                        openCommentOptions(comID, true, comLayout, commentTxtVw, true);
                        return false;
                    });
                    helder.setOnLongClickListener(v -> {
                        openCommentOptions(comID, true, comLayout, commentTxtVw, true);
                        return false;
                    });
                    commentTxtVw.setOnLongClickListener(v -> {
                        openCommentOptions(comID, true, comLayout, commentTxtVw, true);
                        return false;
                    });
                    commentDateVw.setOnLongClickListener(v -> {
                        openCommentOptions(comID, true, comLayout, commentTxtVw, true);
                        return false;
                    });
                    reply.setOnClickListener(v -> replyComment(comLayout, comID));
                    react.setOnClickListener(v -> reactToComment(comID, v));
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("reverseSubmitComment", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                int counter = argsArr.getInt(1);
                if(!(counter == -1) && !(comLayouts.get(counter) == null)) {
                    RelativeLayout comLayout = comLayouts.get(counter);
                    runOnUiThread(() -> layout.removeView(comLayout));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("commentLike", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String commId = argsArr.getString(0);
                int likes = argsArr.getInt(1);
                int user = argsArr.getInt(2);
                int tag = argsArr.getInt(3);
                if(!(comLayoutsX.isNull(commId)) && !(likesDisplay.isNull(commId))){
                    TextView textView = (TextView) likesDisplay.get(commId);
                    String likeNumStr = textView.getText().toString();
                    int likeNum = Functions.convertToNumber(likeNumStr);
                    likeNum += likes;
                    int finalLikeNum = likeNum;
                    runOnUiThread(() -> textView.setText(Functions.convertToText(finalLikeNum)));
                    if(user == myId){
                        RelativeLayout relativeLayout = (RelativeLayout) comLayoutsX.get(commId);
                        int newTag = reactionsCom[tag][0];
                        int newReact = reactionsCom[tag][1];
                        ImageButton imageButton = relativeLayout.findViewById(R.id.react);
                        runOnUiThread(() -> {
                            imageButton.setTag(newTag);
                            imageButton.setBackgroundResource(newReact);
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("submitReply", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String cmmtId = String.valueOf(argsArr.getInt(1));
                if(!(repsDisplay.isNull(cmmtId))){
                    TextView textView = (TextView) repsDisplay.get(cmmtId);
                    String likeNumStr = textView.getText().toString();
                    int likeNum = Functions.convertToNumber(likeNumStr);
                    likeNum++;
                    int finalLikeNum = likeNum;
                    runOnUiThread(() -> textView.setText(Functions.convertToText(finalLikeNum)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("deleteReply", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String comId = String.valueOf(argsArr.getInt(1));
                if(!(comLayoutsX.isNull(comId))){
                    RelativeLayout relativeLayout = (RelativeLayout) comLayoutsX.get(comId);
                    TextView textView =  relativeLayout.findViewById(R.id.replyNum);
                    String likeNumStr = textView.getText().toString();
                    int likeNum = Functions.convertToNumber(likeNumStr);
                    likeNum--;
                    int finalLikeNum = likeNum;
                    runOnUiThread(() -> textView.setText(Functions.convertToText(finalLikeNum)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("editComment", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String comId = String.valueOf(argsArr.getInt(0));
                if(!(comLayoutsX.isNull(comId))){
                    RelativeLayout relativeLayout = (RelativeLayout) comLayoutsX.get(comId);
                    TextView textView =  relativeLayout.findViewById(R.id.commentText);
                    String newCom = String.valueOf(argsArr.getString(1));
                    if(commentHtmlText.has(comId))
                        commentHtmlText.put(comId, newCom);
                    newCom = EmojiParser.parseToUnicode(newCom);
                    newCom = EmojiParser.parseToUnicode(newCom);
                    newCom = HtmlParser.parseSpan(newCom);
                    String finalNewCom = newCom;
                    runOnUiThread(() -> {
                        CharSequence sequence = Html.fromHtml(finalNewCom);
                        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                        for(URLSpan span : urls) {
                            makeLinkClickable(cntxt, strBuilder, span);
                        }
                        textView.setText(strBuilder);
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("deleteComment", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String comId = String.valueOf(argsArr.getInt(0));
                if(!(comLayoutsX.isNull(comId))){
                    RelativeLayout relativeLayout = (RelativeLayout) comLayoutsX.get(comId);
                    runOnUiThread(() -> relativeLayout.setVisibility(View.GONE));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("hideComment", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String comId = String.valueOf(argsArr.getInt(0));
                int user = argsArr.getInt(1);
                if(!(comLayoutsX.isNull(comId)) && myId == user){
                    RelativeLayout relativeLayout = (RelativeLayout) comLayoutsX.get(comId);
                    runOnUiThread(() -> relativeLayout.setVisibility(View.GONE));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("hidePost", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postID = String.valueOf(argsArr.getInt(0));
                int userC = argsArr.getInt(1);
                if(postId.equals(postID) && userC == myId){
                    options.put("hide", "Unhide Post");
                    optionsVal.put("hide", true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("deletePost", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postID = String.valueOf(argsArr.getInt(0));
                if(postId.equals(postID)){
                    options.put("deletePost", "Restore Post");
                    optionsVal.put("deletePost", true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
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
        socket.on("mentionList", args -> {
            try {
                JSONArray listArray = new JSONArray(args[0].toString());
                if(listArray.length() == 0){
                    runOnUiThread(() -> scrllView.setVisibility(View.GONE));
                    return;
                }
                runOnUiThread(() -> {
                    if(menLayout.getChildCount() > 0)
                        menLayout.removeAllViews();
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
                    ImageView imageView =  listView.findViewById(R.id.photo);
                    TextView nameTV =  listView.findViewById(R.id.name);
                    TextView userNameTV =  listView.findViewById(R.id.userName);
                    imageLoader.displayImage(photo, imageView);
                    nameTV.setText(name);
                    userNameTV.setText(lash);
                    if(verified)
                        nameTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user, 0);
                    String finalName = name;
                    listView.setOnClickListener(v -> addTextToView(editText, finalName, tab, id));
                    runOnUiThread(() -> menLayout.addView(listView));
                }
                runOnUiThread(() -> scrllView.setVisibility(View.VISIBLE));
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

    private boolean overrideBackspace(EditText editText) throws JSONException {
        int caretEnd = editText.getSelectionEnd();
        if(caretEnd == 0)
            return false;
        int caretStart = editText.getSelectionStart();
        if(caretStart == caretEnd)
            caretStart = caretEnd - 1;
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
        if(!(href.startsWith(startStrOne) || href.startsWith(startStrTwo)))
            return false;
        String allHtmlText = Html.toHtml(editText.getText());
        Document allDoc = Jsoup.parse(allHtmlText, "UTF-8");
        Elements parentAnchorElement = allDoc.select("a[href=\""+href+"\"]");
        String anchorTextHtml = parentAnchorElement.html();
        String anchorText = Jsoup.parse(anchorTextHtml).text();
        int anchorTextLen = anchorText.length();
        int charIndex = textWalker(editText, href, caretStart, caretEnd, 0);
        if(anchorTextLen == charIndex){
            JSONArray anchorTextArr = new JSONArray(anchorText.split(" "));
            int lastIndex = anchorTextArr.length() - 1;
            anchorTextArr.remove(lastIndex);
            String newAnchorText = Functions.joinJSONArray(anchorTextArr, " ");
            Spanned newHtmlText;
            String str = " <a href=\""+href+"\"><b><span style=\"background-color:#F9F9F9;\">"+newAnchorText+"</span></b></a>";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                newHtmlText = Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT);
            else
                newHtmlText = Html.fromHtml(str);
            int indexStart = caretEnd - anchorTextLen;
            int newCaretPos = indexStart + Objects.requireNonNull(newAnchorText).length();
            editText.setText(editText.getText().delete(indexStart , caretEnd));
            editText.getText().insert(indexStart, newHtmlText);
            editText.setSelection(newCaretPos);
        } else {
            StringBuilder stringBuilder = new StringBuilder(anchorText);
            stringBuilder.deleteCharAt(charIndex - 1);
            anchorText = stringBuilder.toString();
            int diff = anchorTextLen - charIndex;
            caretEnd += diff;
            int indexStart = caretEnd - anchorTextLen;
            int newCaretPos = indexStart + charIndex - 1;
            editText.setText(editText.getText().delete(indexStart , caretEnd));
            editText.getText().insert(indexStart, anchorText);
            editText.setSelection(newCaretPos);
        }
        return true;
    }

    private int textWalker(EditText editText, String href, int caretStart, int caretEnd, int charCount){
        String htmlText = Html.toHtml((Spanned) editText.getText().subSequence(caretStart, caretEnd));
        Document doc = Jsoup.parse(htmlText, "UTF-8");
        Elements anchorElement = doc.select("a");
        String hrefAtrr = anchorElement.attr("href");
        caretStart--;
        caretEnd--;
        if(!href.equals(hrefAtrr))
            return charCount;
        charCount++;
        if(caretStart < 0)
            return charCount;
        return textWalker(editText, href, caretStart, caretEnd, charCount);
    }

    private void changeWordToHashTag(EditText editText, String lastWord) {
        hashed = true;
        Spanned htmlText;
        int indexEnd = editText.getSelectionStart();
        int lastWordLen = lastWord.length();
        int indexStart = indexEnd - lastWordLen;
        int newCaretPos = indexStart + lastWordLen;
        String href = lastWord.substring(1);
        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        String bColor = "#F9F9F9";
        if(darkThemeEnabled)
            bColor = "#585858";
        String str = "<a href=\"search/"+href+"\"><span style=\"background-color:"+bColor+";\"><b>"+lastWord+"</b></span></a>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            htmlText = Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT);
        else
            htmlText = Html.fromHtml(str);
        editText.setText(editText.getText().delete(indexStart , indexEnd));
        editText.getText().insert(indexStart, htmlText);
        editText.setSelection(newCaretPos);
        hashed = false;
    }

    private void addTextToView(EditText editText, String name, String tab, int id) {
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
        editText.setText(editText.getText().delete(indexStart , indexEnd));
        editText.getText().insert(indexStart, htmlText);
        editText.setSelection(newCaretPos);
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

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void getPostDisplay(){
        loadingPost = true;
        while (loadingPost){
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(myId))
                    .addFormDataPart("postId", postId)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.getPostUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    loadingPost = false;
                    mainHomeView.removeView(loadingLayer);
                    scrollView.setVisibility(View.VISIBLE);
                    JSONObject responseObject = new JSONObject(responseString);
                    JSONObject pagesInfo = responseObject.getJSONObject("pageArray");
                    options = responseObject.getJSONObject("options");
                    optionsVal = responseObject.getJSONObject("optionsVal");
                    hasPage = pagesInfo.getBoolean("hasPage");
                    userPages = pagesInfo.getJSONArray("userPages");
                    JSONObject postObj = responseObject.getJSONObject("data");
                    JSONArray commentArr = responseObject.getJSONArray("comments");
                    if(!hasPage){
                        poster = String.valueOf(myId);
                        postType = "profile";
                        posterImg = myPht;
                        posterName = myName;
                        posterUName = myUserName;
                    }
                    String postID = postId;
                    postUser = postObj.getString("user");
                    String postName = postObj.getString("name");
                    String postUserName = postObj.getString("userName");
                    String postPhoto = www + postObj.getString("photo");
                    String postText = postObj.getString("post");
                    String files = postObj.getString("files");
                    String postDate = postObj.getString("date");
                    pagerId = postObj.getString("pagerId");
                    String type = postObj.getString("type");
                    boolean hasFiles = postObj.getBoolean("hasFiles");
                    boolean verified = postObj.getBoolean("verified");
                    boolean liked = postObj.getBoolean("liked");
                    boolean perm = postObj.getBoolean("perm");
                    vary = postObj.getBoolean("vary");
                    int likeNum = postObj.getInt("likeNum");
                    int comNum = postObj.getInt("comNum");
                    postView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post, null);
                    LinearLayout postTextLayout =  postView.findViewById(R.id.postTextLayout);
                    LinearLayout postFilesLayout =  postView.findViewById(R.id.postFilesLayout);
                    LinearLayout commentBx =  postView.findViewById(R.id.commentBx);
                    ImageView userImageView =  postView.findViewById(R.id.posterPht);
                    ImageView myComPht =  postView.findViewById(R.id.myComPht);
                    ImageButton postOpt =  postView.findViewById(R.id.postOpt);
                    reactButton =  postView.findViewById(R.id.likePost);
                    ImageButton comments =  postView.findViewById(R.id.comments);
                    TextView pstText =  postView.findViewById(R.id.postText);
                    TextView nameTxtVw =  postView.findViewById(R.id.posterName);
                    TextView userNameTxtVw =  postView.findViewById(R.id.posterUName);
                    TextView postDateVw =  postView.findViewById(R.id.postDate);
                    TextView comNumTV =  postView.findViewById(R.id.comNum);
                    likeNumTextView =  postView.findViewById(R.id.likeNum);
                    likersTextView =  postView.findViewById(R.id.likers);
                    ViewPager viewPager =  postView.findViewById(R.id.viewPager);
                    imageLoader.displayImage(postPhoto, userImageView);
                    imageLoader.displayImage(myPht, myComPht);
                    nameTxtVw.setText(postName);
                    postDateVw.setText(postDate);
                    String htmlText = HtmlParser.parseSpan(postText);
                    CharSequence sequence = Html.fromHtml(htmlText);
                    SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                    URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                    for(URLSpan span : urls) {
                        makeLinkClickable(cntxt, strBuilder, span);
                    }
                    pstText.setText(strBuilder);
                    pstText.setMovementMethod(LinkMovementMethod.getInstance());
                    commentBx.setVisibility(View.GONE);
                    if(verified)
                        nameTxtVw.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                    if(!perm)
                        commentBox.setVisibility(View.GONE);
                    if (StringUtils.isEmpty(postText))
                        postTextLayout.setVisibility(View.GONE);
                    if (type.equals("profile"))
                        userNameTxtVw.setText("@" + postUserName);
                    else
                        userNameTxtVw.setVisibility(View.GONE);
                    if(liked) {
                        reactButton.setBackgroundResource(R.drawable.ic_loved);
                        reactButton.setTag("1");
                    }
                    if (likeNum > 0) {
                        String likes = Functions.convertToText(likeNum);
                        likeNumTextView.setText(likes);
                        likersTextView.setVisibility(View.VISIBLE);
                    }
                    if (comNum > 0) {
                        String coms = Functions.convertToText(comNum);
                        comNumTV.setText(coms);
                    }
                    reactButton.setOnClickListener(v -> reactToPost());
                    likersTextView.setOnClickListener(v -> showReactors(cntxt, postID, "posts", likeNum));
                    comments.setOnClickListener(v -> openComments(postID));
                    postOpt.setOnClickListener(v -> {
                        try {
                            displayPostsOptions();
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
                        JSONArray postFiles = new JSONArray(files);
                        ArrayList<String> postFilesLists = new ArrayList<>();
                        int numOfImgs = postFiles.length();
                        if (postFiles.length() > 9)
                            numOfImgs = 0;
                        int numDis = numberOfImages[numOfImgs];
                        View imgNum =  postView.findViewById(R.id.imgNum);
                        imgNum.setBackgroundResource(numDis);
                        for (int h = 0; h < postFiles.length(); h++) {
                            String filePath = www + postFiles.getString(h);
                            postFilesLists.add(filePath);
                        }
                        PhotoAdapter photoAdapter = new PhotoAdapter(postFilesLists, cntxt);
                        ViewGroup.LayoutParams params = postFilesLayout.getLayoutParams();
                        params.height = viewPagerHeight;
                        viewPager.setLayoutParams(params);
                        viewPager.setOffscreenPageLimit(postFiles.length());
                        viewPager.setAdapter(photoAdapter);
                        viewPager.setPageTransformer(true, new PageTransformer());
                        viewPager.post(() -> {
                            View view = viewPager.getChildAt(0);
                            ImageView imageView = view.findViewById(R.id.image);
                            imageView.post(() -> new Handler().postDelayed(
                                    () -> {
                                        try {
                                            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                            if(!(bitmap == null)){
                                                Bitmap blurredBtmp = BlurBitmap.blurify(bitmap);
                                                Drawable drawable = new BitmapDrawable(cntxt.getResources(), blurredBtmp);
                                                viewPager.setBackground(drawable);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    },
                                    5000));
                        });
                    } else {
                        postFilesLayout.setVisibility(View.GONE);
                    }
                    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            if (!onPagerScrolled && positionOffset == 0 && positionOffsetPixels == 0){
                                viewPager.setCurrentItem(0);
                                onPageSelected(0);
                                onPagerScrolled = true;
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
                    layout.addView(postView);
                    if(likeNum > 15) {
                        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                        int color;
                        if(darkThemeEnabled)
                            color = R.color.lightRed;
                        else
                            color = R.color.colorPrimaryRed;
                        String text = "View all comments";
                        TextView newTextView = new TextView(cntxt);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        newTextView.setPadding(0, 15, 0, 15);
                        newTextView.setLayoutParams(params);
                        newTextView.setTextColor(ContextCompat.getColor(cntxt, color));
                        newTextView.setText(text);
                        newTextView.setTextSize(17f);
                        newTextView.setGravity(Gravity.CENTER);
                        newTextView.setOnClickListener(v -> openComments(postId));
                        layout.addView(newTextView);
                    }
                    for (int p = 0; p < commentArr.length(); p++) {
                        JSONObject commentObj = new JSONObject(commentArr.getString(p));
                        String comID = commentObj.getString("id");
                        String commentUser = commentObj.getString("user");
                        String commentName = commentObj.getString("name");
                        String commentUserName = commentObj.getString("userName");
                        String commentPhoto = Constants.www + commentObj.getString("photo");
                        String comment = commentObj.getString("comment");
                        String commentDate = commentObj.getString("date");
                        String comType = commentObj.getString("type");
                        boolean comVerified = commentObj.getBoolean("verified");
                        boolean access = commentObj.getBoolean("access");
                        int comLikeNum = commentObj.getInt("likeNum");
                        int repNum = commentObj.getInt("repNum");
                        boolean comLiked = commentObj.getBoolean("liked");
                        commentHtmlText.put(comID, comment);
                        selectedComms.add(postID);
                        RelativeLayout commentView = (RelativeLayout) getLayoutInflater().inflate(R.layout.comment_layout, null);
                        ImageView comUserImageView =  commentView.findViewById(R.id.profPic);
                        LinearLayout helder =  commentView.findViewById(R.id.helder);
                        TextView commentText =  commentView.findViewById(R.id.commentText);
                        TextView comNameTxtVw =  commentView.findViewById(R.id.posterName);
                        TextView comUserNameTxtVw =  commentView.findViewById(R.id.posterUName);
                        TextView commentDateVw =  commentView.findViewById(R.id.commentDate);
                        TextView likeNumTV =  commentView.findViewById(R.id.likeNum);
                        TextView replyNumTV =  commentView.findViewById(R.id.replyNum);
                        ImageButton reply =  commentView.findViewById(R.id.reply);
                        ImageButton react =  commentView.findViewById(R.id.react);
                        comLayoutsX.put(comID, commentView);
                        repsDisplay.put(comID, replyNumTV);
                        likesDisplay.put(comID, likeNumTV);
                        imageLoader.displayImage(commentPhoto, comUserImageView);
                        String comHtmlText = HtmlParser.parseSpan(comment);
                        comHtmlText = EmojiParser.parseToUnicode(comHtmlText);
                        CharSequence comSequence = Html.fromHtml(comHtmlText);
                        SpannableStringBuilder comStrBuilder = new SpannableStringBuilder(comSequence);
                        URLSpan[] comUrls = comStrBuilder.getSpans(0, comSequence.length(), URLSpan.class);
                        for(URLSpan span : comUrls) {
                            makeLinkClickable(cntxt, comStrBuilder, span);
                        }
                        commentText.setText(comStrBuilder);
                        commentText.setMovementMethod(LinkMovementMethod.getInstance());
                        comNameTxtVw.setText(commentName);
                        commentDateVw.setText(commentDate);
                        if(comLiked) {
                            react.setBackgroundResource(R.drawable.ic_loved);
                            react.setTag("1");
                        }
                        if (!StringUtils.isEmpty(commentUserName)) {
                            commentUserName = "@" + commentUserName;
                            comUserNameTxtVw.setText(commentUserName);
                        } else
                            comUserNameTxtVw.setVisibility(View.GONE);
                        if(comVerified)
                            comNameTxtVw.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                        if (comLikeNum > 0) {
                            String likes = Functions.convertToText(comLikeNum);
                            likeNumTV.setText(likes);
                        }
                        if (repNum > 0) {
                            String reps = Functions.convertToText(repNum);
                            replyNumTV.setText(reps);
                        }
                        likeNumTV.setOnClickListener(v -> showReactors(cntxt, comID, "comments", comLikeNum));
                        react.setOnClickListener(v -> reactToComment(comID, v));
                        reply.setOnClickListener(v -> replyComment(commentView, comID));
                        commentView.setOnLongClickListener(v -> {
                            openCommentOptions(comID, access, commentView, commentText, true);
                            return false;
                        });
                        helder.setOnLongClickListener(v -> {
                            openCommentOptions(comID, access, commentView, commentText, true);
                            return false;
                        });
                        commentText.setOnLongClickListener(v -> {
                            openCommentOptions(comID, access, commentView, commentText, true);
                            return false;
                        });
                        commentDateVw.setOnLongClickListener(v -> {
                            openCommentOptions(comID, access, commentView, commentText, true);
                            return false;
                        });
                        comUserImageView.setOnClickListener(v -> {
                            switch (comType){
                                case "profile":
                                    visitUserProfile(cntxt, Integer.parseInt(commentUser));
                                    break;
                                case "page":
                                    visitPage(cntxt, Integer.parseInt(commentUser));
                                    break;
                            }
                        });
                        comUserNameTxtVw.setOnClickListener(v -> {
                            switch (comType){
                                case "profile":
                                    visitUserProfile(cntxt, Integer.parseInt(commentUser));
                                    break;
                                case "page":
                                    visitPage(cntxt, Integer.parseInt(commentUser));
                                    break;
                            }
                        });
                        comNameTxtVw.setOnClickListener(v -> {
                            switch (comType){
                                case "profile":
                                    visitUserProfile(cntxt, Integer.parseInt(commentUser));
                                    break;
                                case "page":
                                    visitPage(cntxt, Integer.parseInt(commentUser));
                                    break;
                            }
                        });
                        layout.addView(commentView);
                    }
                    socket.emit("postScratch", postId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("InflateParams")
    private void displayPostsOptions() throws JSONException {
        RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
        LinearLayout optLayer =  postOptView.findViewById(R.id.optLayer);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        JSONArray objKeys = options.names();
        for (int r = 0; r < Objects.requireNonNull(objKeys).length(); r++){
            TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
            String key = objKeys.getString(r);
            String option = options.getString(key);
            int drawableLeft = postsOptionsIcons.getInt(key);
            optionList.setText(option);
            optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
            optionList.setOnClickListener(v -> {
                try {
                    executePostOption(key);
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
    private void executePostOption(String key) throws JSONException {
        JSONObject emitObj;
        boolean val, newVal;
        String newTxt, theName, retTxt, date,  text, toastTxt, eventTxt;
        String[] retTxtArr;
        blackFade.setVisibility(View.GONE);
        switch (key){
            case "savePost":
                val = optionsVal.getBoolean(key);
                newTxt = "Save Post";
                if(val) {
                    Toast.makeText(cntxt, "Post Unsaved", Toast.LENGTH_LONG).show();
                } else {
                    newTxt = "Unsave Post";
                    Toast.makeText(cntxt, "Post Saved", Toast.LENGTH_LONG).show();
                }
                newVal = !val;
                optionsVal.put(key, newVal);
                options.put(key, newTxt);
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                emitObj = new JSONObject();
                emitObj.put("date", date);
                emitObj.put("user", myId);
                emitObj.put("postId", postId);
                emitObj.put("val", val);
                socket.emit("savePost", emitObj);
                break;
            case "unfollowPers":
                val = optionsVal.getBoolean(key);
                retTxt = options.getString(key);
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
                optionsVal.put(key, newVal);
                options.put(key, newTxt);
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                emitObj = new JSONObject();
                emitObj.put("myId", myId);
                emitObj.put("user", postUser);
                emitObj.put("val", val);
                emitObj.put("date", date);
                socket.emit("follow", emitObj);
                break;
            case "unfollowPage":
                val = optionsVal.getBoolean(key);
                retTxt = options.getString(key);
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
                optionsVal.put(key, newVal);
                options.put(key, newTxt);
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                emitObj = new JSONObject();
                emitObj.put("user", myId);
                emitObj.put("pageId", postUser);
                emitObj.put("val", val);
                emitObj.put("date", date);
                socket.emit("followPage", emitObj);
                break;
            case "notifyStatus":
                val = optionsVal.getBoolean(key);
                newTxt = "On notification for Post";
                if(val) {
                    Toast.makeText(cntxt, "Notification Turned Off", Toast.LENGTH_LONG).show();
                } else {
                    newTxt = "Off notification for Post";
                    Toast.makeText(cntxt, "Notification Turned On", Toast.LENGTH_LONG).show();
                }
                newVal = !val;
                optionsVal.put(key, newVal);
                options.put(key, newTxt);
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
                val = optionsVal.getBoolean(key);
                newVal = !val;
                if(val) {
                    newTxt = "Hide Post";
                    toastTxt = "Post Un-hidden";
                    text = "Are you sure you want to un-hide this post?";
                    eventTxt = "unhidePost";
                } else {
                    newTxt = "Unhide Post";
                    toastTxt = "Post Hidden";
                    text = "Are you sure you want to hide this post?";
                    eventTxt = "hidePost";
                }
                View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                TextView txter = blockView.findViewById(R.id.txter);
                Button cnclBtn = blockView.findViewById(R.id.cancel);
                Button agreeBtn = blockView.findViewById(R.id.agree);
                txter.setText(text);
                emitObj = new JSONObject();
                String finalNewTxt = newTxt;
                agreeBtn.setOnClickListener(v -> {
                    blackFade.setVisibility(View.GONE);
                    try {
                        optionsVal.put(key, newVal);
                        options.put(key, finalNewTxt);
                        Toast.makeText(cntxt, toastTxt, Toast.LENGTH_LONG).show();
                        @SuppressLint("SimpleDateFormat") String date13 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        emitObj.put("date", date13);
                        emitObj.put("user", myId);
                        emitObj.put("postId", postId);
                        socket.emit(eventTxt, emitObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
                blackFade.addView(blockView);
                blackFade.setVisibility(View.VISIBLE);
                break;
            case "editPost":
                Intent intent = new Intent(cntxt, EditPostActivity.class);
                Bundle userParams = new Bundle();
                userParams.putString("postId", postId);
                userParams.putInt("reqFrm", 3);
                intent.putExtras(userParams);
                startActivity(intent);
                break;
            case "deletePost":
                val = optionsVal.getBoolean(key);
                newVal = !val;
                if(val) {
                    newTxt = "Delete Post";
                    toastTxt = "Post Restored";
                    text = "Are you sure you want to restore this post?";
                    eventTxt = "undeletePost";
                } else {
                    newTxt = "Restore Post";
                    toastTxt = "Post Deleted";
                    text = "Are you sure you want to delete this post?";
                    eventTxt = "deletePost";
                }
                View blockViewX = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                TextView txterX = blockViewX.findViewById(R.id.txter);
                Button cnclBtnX = blockViewX.findViewById(R.id.cancel);
                Button agreeBtnX = blockViewX.findViewById(R.id.agree);
                txterX.setText(text);
                emitObj = new JSONObject();
                String finalNewTxtX = newTxt;
                agreeBtnX.setOnClickListener(v -> {
                    blackFade.setVisibility(View.GONE);
                    try {
                        optionsVal.put(key, newVal);
                        options.put(key, finalNewTxtX);
                        Toast.makeText(cntxt, toastTxt, Toast.LENGTH_LONG).show();
                        @SuppressLint("SimpleDateFormat") String date12 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        emitObj.put("date", date12);
                        emitObj.put("user", myId);
                        emitObj.put("postId", postId);
                        socket.emit(eventTxt, emitObj);
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
                LinearLayout optLayer =  postOptView.findViewById(R.id.optLayer);
                LinearLayout tvw = (LinearLayout) getLayoutInflater().inflate(R.layout.text_view, null);
                TextView txtHead =  tvw.findViewById(R.id.head);
                TextView txtBody =  tvw.findViewById(R.id.body);
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

    private void setViewPagerChangeListener(ViewPager viewPager, int position) {
        View view = viewPager.getChildAt(position);
        ImageView imageView =  view.findViewById(R.id.image);
        boolean imgLoaded = Boolean.parseBoolean(imageView.getTag().toString());
        if(imgLoaded){
            try {
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                if(!(bitmap == null)){
                    Bitmap blurredBtmp = BlurBitmap.blurify(bitmap);
                    Drawable drawable = new BitmapDrawable(cntxt.getResources(), blurredBtmp);
                    viewPager.setBackground(drawable);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private void reactToPost() {
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        int tag = Integer.parseInt(reactButton.getTag().toString());
        JSONObject emitObj = new JSONObject();
        try {
            String text = likeNumTextView.getText().toString();
            emitObj.put("user", myId);
            emitObj.put("postId", postId);
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
            likeNumTextView.setText(text);
            reactButton.setTag(newTag);
            reactButton.setBackgroundResource(newReact);
            if(num > 0){
                runOnUiThread(() -> likersTextView.setVisibility(View.VISIBLE));
            } else {
                runOnUiThread(() -> likersTextView.setVisibility(View.GONE));
            }
            socket.emit("postLike", emitObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openComments(String postID) {
        Intent intent = new Intent(cntxt, CommentsActivity.class);
        Bundle userParams = new Bundle();
        userParams.putString("postID", postID);
        userParams.putString("commentID", null);
        intent.putExtras(userParams);
        startActivity(intent);
    }

    private void reactToComment(String comID, View v) {
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        int tag = Integer.parseInt(v.getTag().toString());
        JSONObject emitObj = new JSONObject();
        try {
            TextView textView = (TextView) likesDisplay.get(comID);
            String text = textView.getText().toString();
            emitObj.put("user", myId);
            emitObj.put("comId", comID);
            emitObj.put("date", date);
            emitObj.put("postId", postId);
            emitObj.put("tag", tag);
            int newTag = reactionsCom[tag][0];
            int newReact = reactionsCom[tag][1];
            int num = Functions.convertToNumber(text);
            if(tag == 0)
                num++;
            else
                num--;
            text = Functions.convertToText(num);
            textView.setText(text);
            v.setTag(newTag);
            v.setBackgroundResource(newReact);
            TextView likesTView = (TextView) likesDisplay.get(comID);
            if(num > 0){
                runOnUiThread(() -> likesTView.setVisibility(View.VISIBLE));
            } else {
                runOnUiThread(() -> likesTView.setVisibility(View.GONE));
            }
            socket.emit("commentLike", emitObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void replyComment(View view, String comID) {
        currView = view;
        Intent intent = new Intent(cntxt, RepliesActivity.class);
        Bundle userParams = new Bundle();
        userParams.putString("postID", postId);
        userParams.putString("comID", comID);
        intent.putExtras(userParams);
        startActivity(intent);
    }

    @SuppressLint("InflateParams")
    private void openCommentOptions(String id, boolean access, RelativeLayout commentView, TextView textView, boolean real) {
        try {
            if(blackFade.getChildCount() > 0){
                blackFade.removeAllViews();
            }
            RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
            LinearLayout optLayer =  postOptView.findViewById(R.id.optLayer);
            JSONObject object = new JSONObject();
            object.put("copy", "Copy");
            if(real){
                object.put("reply", "Reply");
                if(!access){
                    object.put("hide", "Hide");
                    object.put("report", "Report");
                }
            }
            if(access){
                if(real)
                    object.put("edit", "Edit");
                object.put("delete", "Delete");
            }
            JSONArray objKeys = object.names();
            for (int r = 0; r < Objects.requireNonNull(objKeys).length(); r++){
                TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
                String key = objKeys.getString(r);
                String option = object.getString(key);
                int drawableLeft = optIcons.getInt(key);
                optionList.setText(option);
                optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
                optionList.setOnClickListener(v -> {
                    try {
                        blackFade.setVisibility(View.GONE);
                        executeOptions(key, id, commentView, textView, real);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                optLayer.addView(optionList);
            }
            blackFade.addView(postOptView);
            blackFade.setVisibility(View.VISIBLE);
            setupUI(mainHomeView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void executeOptions(String key, String id, RelativeLayout commentView, TextView textView, boolean real) throws JSONException {
        switch (key){
            case "copy":
                copyContent(textView);
                break;
            case "reply":
                replyComment(commentView, id);
                break;
            case "hide":
                hideComments(commentView, id);
                break;
            case "report":
                reportComments(id);
                break;
            case "edit":
                editComment(textView, id);
                break;
            case "delete":
                deleteComment(commentView, id, real);
        }
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void hideComments(RelativeLayout commentView, String comID) {
        View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        TextView txter = blockView.findViewById(R.id.txter);
        Button cnclBtn = blockView.findViewById(R.id.cancel);
        Button agreeBtn = blockView.findViewById(R.id.agree);
        txter.setText("Are you sure you want to hide this comment?");
        JSONObject emitObj = new JSONObject();
        agreeBtn.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            try {
                runOnUiThread(() -> commentView.setVisibility(View.GONE));
                Toast.makeText(cntxt, "Comment Hidden", Toast.LENGTH_LONG).show();
                @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                emitObj.put("date", date);
                emitObj.put("user", myId);
                emitObj.put("comId", comID);
                emitObj.put("postId", postId);
                socket.emit("hideComment", emitObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
        blackFade.addView(blockView);
        blackFade.setVisibility(View.VISIBLE);
        setupUI(mainHomeView);
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void reportComments(String comID) {
        String[] opts = new String[]{
                "It's annoying",
                "It's abusive",
                "It's a spam",
                "It contains illegal contents",
                "It contains sexual explicit contents"
        };
        RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
        LinearLayout optLayer =  postOptView.findViewById(R.id.optLayer);
        LinearLayout tvw = (LinearLayout) getLayoutInflater().inflate(R.layout.text_view, null);
        TextView txtHead =  tvw.findViewById(R.id.head);
        TextView txtBody =  tvw.findViewById(R.id.body);
        txtHead.setText("Help Us Understand What's Happening");
        txtBody.setText("What's wrong with this comment?");
        optLayer.addView(tvw);
        JSONObject emitObj = new JSONObject();
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
                    emitObj.put("dataId", comID);
                    emitObj.put("type", "comment");
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
        setupUI(mainHomeView);
    }

    private void editComment(TextView view, String comID) throws JSONException {
        curEditableView = view;
        String curTxt = commentHtmlText.getString(comID);
        Intent intent = new Intent(cntxt, EditCommentActivity.class);
        Bundle userParams = new Bundle();
        userParams.putString("postID", postId);
        userParams.putString("comID", comID);
        userParams.putString("curTxt", curTxt);
        userParams.putInt("activityInd", 2);
        intent.putExtras(userParams);
        startActivity(intent);
    }

    public static void saveEdition(Context cntxt, String comment, String cmmID) throws JSONException {
        if(curEditableView != null){
            @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            comment = HtmlParser.parseString(comment);
            commentHtmlText.put(cmmID, comment);
            String cmmtX = EmojiParser.parseToUnicode(comment);
            comment = EmojiParser.parseToAliases(comment);
            cmmtX = HtmlParser.parseSpan(cmmtX);
            CharSequence sequence = Html.fromHtml(cmmtX);
            SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
            URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
            for(URLSpan span : urls) {
                makeLinkClickable(cntxt, strBuilder, span);
            }
            curEditableView.setText(strBuilder);
            curEditableView.setMovementMethod(LinkMovementMethod.getInstance());
            JSONObject emitObj = new JSONObject();
            try {
                emitObj.put("user", myId);
                emitObj.put("comId", cmmID);
                emitObj.put("postId", postId);
                emitObj.put("comment", comment);
                emitObj.put("userName", myUserName);
                emitObj.put("date", date);
                socket.emit("editComment", emitObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void deleteComment(View view, String comID, boolean real) {
        View blockViewX = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        TextView txterX = blockViewX.findViewById(R.id.txter);
        Button cnclBtnX = blockViewX.findViewById(R.id.cancel);
        Button agreeBtnX = blockViewX.findViewById(R.id.agree);
        txterX.setText("Are you sure you want to delete this comment?");
        agreeBtnX.setOnClickListener(v -> {
            view.setVisibility(View.GONE);
            try {
                if(real)
                    emitCommentDelete(comID);
                else
                    pendingDel.put(comID, real);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        cnclBtnX.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
        blackFade.addView(blockViewX);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void deletePending(String comID) throws JSONException {
        emitCommentDelete(comID);
    }

    private void emitCommentDelete(String comID) throws JSONException {
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        if(commentHtmlText.has(comID))
            commentHtmlText.remove(comID);
        JSONObject emitObj = new JSONObject();
        emitObj.put("user", myId);
        emitObj.put("comId", comID);
        emitObj.put("postId", postId);
        emitObj.put("date", date);
        socket.emit("deleteComment", emitObj);
    }

    private void copyContent(TextView textView) {
        CharSequence charSequence = textView.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", charSequence);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(cntxt, "Copied to Clipboard", Toast.LENGTH_LONG).show();
    }

    private void setCommenter(String posterId, String posterType, String photo, String name, String uName) {
        blackFade.setVisibility(View.GONE);
        poster = posterId;
        postType = posterType;
        posterImg = photo;
        posterName = name;
        posterUName = uName;
        submitComment();
    }

    private void scrollToComment(View view) {
        view.post(() -> {
            int scrllTo = view.getBottom();
            if(scrllTo < scrollView.getHeight())
                scrllTo = view.getTop();
            scrollView.scrollTo(0, scrllTo);
        });
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void submitComment() {
        String commentR = editText.getText().toString();
        if(!StringUtils.isEmpty(commentR)){
            if(vary && postType == null && poster == null){
                try {
                    RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
                    LinearLayout optLayer =  postOptView.findViewById(R.id.optLayer);
                    if(blackFade.getChildCount() > 0){
                        blackFade.removeAllViews();
                    }
                    LinearLayout tvw = (LinearLayout) getLayoutInflater().inflate(R.layout.text_view, null);
                    TextView txtHead =  tvw.findViewById(R.id.head);
                    TextView txtBody =  tvw.findViewById(R.id.body);
                    txtBody.setVisibility(View.GONE);
                    txtHead.setText("Make Comment As:");
                    optLayer.addView(tvw);
                    LinearLayout tabletR = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.tablet, null, false);
                    LinearLayout layR =  tabletR.findViewById(R.id.lay);
                    ImageView imageViewR =  tabletR.findViewById(R.id.image);
                    TextView txtNameR =  tabletR.findViewById(R.id.name);
                    TextView txtUserNameR =  tabletR.findViewById(R.id.userName);
                    imageLoader.displayImage(myPht, imageViewR);
                    txtNameR.setText(myName);
                    txtUserNameR.setText("@"+myUserName);
                    optLayer.addView(tabletR);
                    tabletR.setOnClickListener(v -> setCommenter(String.valueOf(myId), "profile", myPht, myName, myUserName));
                    imageViewR.setOnClickListener(v -> setCommenter(String.valueOf(myId), "profile", myPht, myName, myUserName));
                    txtNameR.setOnClickListener(v -> setCommenter(String.valueOf(myId), "profile", myPht, myName, myUserName));
                    txtUserNameR.setOnClickListener(v -> setCommenter(String.valueOf(myId), "profile", myPht, myName, myUserName));
                    layR.setOnClickListener(v -> setCommenter(String.valueOf(myId), "profile", myPht, myName, myUserName));
                    for (int r = 0; r < userPages.length(); r++){
                        JSONObject pageInfo = new JSONObject(userPages.getString(r));
                        String pageId = pageInfo.getString("id");
                        if(pageId.equals(pagerId)) {
                            String pageName = pageInfo.getString("pageName");
                            String pagePhoto = Constants.www + pageInfo.getString("photo");
                            LinearLayout tablet = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.tablet, null, false);
                            LinearLayout lay =  tablet.findViewById(R.id.lay);
                            ImageView imageView =  tablet.findViewById(R.id.image);
                            TextView txtName =  tablet.findViewById(R.id.name);
                            TextView txtUserName =  tablet.findViewById(R.id.userName);
                            txtUserName.setVisibility(View.GONE);
                            imageLoader.displayImage(pagePhoto, imageView);
                            txtName.setText(pageName);
                            optLayer.addView(tablet);
                            tablet.setOnClickListener(v -> setCommenter(pageId, "page", pagePhoto, pageName, null));
                            imageView.setOnClickListener(v -> setCommenter(pageId, "page", pagePhoto, pageName, null));
                            txtName.setOnClickListener(v -> setCommenter(pageId, "page", pagePhoto, pageName, null));
                            lay.setOnClickListener(v -> setCommenter(pageId, "page", pagePhoto, pageName, null));
                        }
                    }
                    blackFade.addView(postOptView);
                    blackFade.setVisibility(View.VISIBLE);
                    setupUI(mainHomeView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            }
            String htmlText = Html.toHtml(editText.getText());
            editText.setText("");
            String comment = HtmlParser.parseString(htmlText);
            String comSpan = HtmlParser.parseSpan(comment);
            RelativeLayout comLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.comment_layout, null);
            LinearLayout helder =  comLayout.findViewById(R.id.helder);
            ImageView imgView =  comLayout.findViewById(R.id.profPic);
            TextView commerName =  comLayout.findViewById(R.id.posterName);
            TextView commerUName =  comLayout.findViewById(R.id.posterUName);
            TextView commentTxtVw =  comLayout.findViewById(R.id.commentText);
            TextView commentDateVw =  comLayout.findViewById(R.id.commentDate);
            imgView.setOnClickListener(v -> {
                switch (postType){
                    case "profile":
                        visitUserProfile(cntxt, Integer.parseInt(poster));
                        break;
                    case "page":
                        visitPage(cntxt, Integer.parseInt(poster));
                        break;
                }
            });
            commerName.setOnClickListener(v -> {
                switch (postType){
                    case "profile":
                        visitUserProfile(cntxt, Integer.parseInt(poster));
                        break;
                    case "page":
                        visitPage(cntxt, Integer.parseInt(poster));
                        break;
                }
            });
            commerUName.setOnClickListener(v -> {
                switch (postType){
                    case "profile":
                        visitUserProfile(cntxt, Integer.parseInt(poster));
                        break;
                    case "page":
                        visitPage(cntxt, Integer.parseInt(poster));
                        break;
                }
            });
            commentDateVw.setText("Just Now");
            imageLoader.displayImage(posterImg, imgView);
            commerName.setText(posterName);
            if(posterUName == null)
                commerUName.setVisibility(View.GONE);
            else
                commerUName.setText("@"+posterUName);
            comSpan = EmojiParser.parseToUnicode(comSpan);
            CharSequence sequence = Html.fromHtml(comSpan);
            SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
            URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
            for(URLSpan span : urls) {
                makeLinkClickable(cntxt, strBuilder, span);
            }
            commentTxtVw.setText(strBuilder);
            commentTxtVw.setMovementMethod(LinkMovementMethod.getInstance());
            comment = EmojiParser.parseToAliases(comment);
            layout.addView(comLayout);
            comLayouts.add(comLayout);
            scrollToComment(comLayout);
            count++;
            boolean verif = userVerified;
            if(posterUName == null)
                verif = false;
            if (verif)
                commerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
            comLayout.setOnLongClickListener(v -> {
                openCommentOptions(String.valueOf(count), true, comLayout, commentTxtVw, false);
                return false;
            });
            helder.setOnLongClickListener(v -> {
                openCommentOptions(String.valueOf(count), true, comLayout, commentTxtVw, false);
                return false;
            });
            commentTxtVw.setOnLongClickListener(v -> {
                openCommentOptions(String.valueOf(count), true, comLayout, commentTxtVw, false);
                return false;
            });
            commentDateVw.setOnLongClickListener(v -> {
                openCommentOptions(String.valueOf(count), true, comLayout, commentTxtVw, false);
                return false;
            });
            setupUI(mainHomeView);
            JSONObject emitObj = new JSONObject();
            @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            try {
                emitObj.put("user", poster);
                emitObj.put("postID", postId);
                emitObj.put("name", posterName);
                emitObj.put("userName", posterUName);
                emitObj.put("photo", posterImg);
                emitObj.put("verified", verif);
                emitObj.put("comment", comment);
                emitObj.put("type", postType);
                emitObj.put("date", date);
                emitObj.put("count", count);
                socket.emit("submitComment", emitObj);
                reset();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void reset() {
        if(vary) {
            poster = null;
            postType = null;
            posterUName = null;
            posterName = null;
            posterImg = null;
        }
    }

    private static void makeLinkClickable(Context cntxt, SpannableStringBuilder strBuilder, URLSpan span) {
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

    public static void savePostEdit(Context cntxt, ArrayList<String> originalImages, ArrayList<File> addedImages, ArrayList<String> editedImages, String htmlText, String postId, String type, String pageId) {
        int totalFilesSize = originalImages.size() + addedImages.size();
        @SuppressLint("InflateParams") RelativeLayout progressView = (RelativeLayout) LayoutInflater.from(cntxt).inflate(R.layout.progress_bar, null, false);
        TextView postProgressText =  progressView.findViewById(R.id.postProgressText);
        ProgressBar postProgressBar =  progressView.findViewById(R.id.postProgressBar);
        LinearLayout postTextLayout =  postView.findViewById(R.id.postTextLayout);
        LinearLayout postFilesLayout =  postView.findViewById(R.id.postFilesLayout);
        ViewPager viewPager =  postView.findViewById(R.id.viewPager);
        TextView pstText =  postView.findViewById(R.id.postText);
        htmlText = HtmlParser.parseString(htmlText);
        String text = HtmlParser.parseSpan(htmlText);
        CharSequence sequence = Html.fromHtml(text);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(cntxt, strBuilder, span);
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
            View imgNum =  postView.findViewById(R.id.imgNum);
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
        multipartBody.addFormDataPart("user", String.valueOf(myId))
                .addFormDataPart("post", htmlText)
                .addFormDataPart("postId", postId);

        @SuppressLint("SetTextI18n") final CountingRequestBody.Listener progressListener = (bytesRead, contentLength) -> {
            if(bytesRead < contentLength && contentLength > 0) {
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
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String mMessage = Objects.requireNonNull(response.body()).string();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    postProgressBar.setProgress(100, true);
                else
                    postProgressBar.setProgress(100);
                postProgressText.setText("100%");
                for(int c = 0; c < editedImages.size(); c++){
                    String filePath = editedImages.get(c);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        File file = new File(filePath);
                        Files.delete(file.toPath());
                    }
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
                        ArrayList<String> postFilesLists = new ArrayList<>();
                        for (int j = 0; j < totalFilesSize; j++){
                            String filePath = www + filesList.getString(j);
                            postFilesLists.add(filePath);
                        }
                        Objects.requireNonNull(viewPager.getAdapter()).notifyDataSetChanged();
                        viewPager.setOffscreenPageLimit(totalFilesSize);
                        PhotoAdapter photoAdapter = new PhotoAdapter(postFilesLists, cntxt);
                        viewPager.setAdapter(photoAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUI(() -> progressView.setVisibility(View.GONE));
            }
        });
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

    private static void visitUserProfile(Context cntxt, int user) {
        Intent intent = new Intent(cntxt, ProfileAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("userID", user);
        intent.putExtras(userParams);
        cntxt.startActivity(intent);
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            if(!(view == scrllView)){
                view.setOnTouchListener((v, event) -> {
                    scrllView.setVisibility(View.GONE);
                    hideSoftKeyboard(v);
                    return false;
                });
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
}