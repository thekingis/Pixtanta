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
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
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

import androidx.appcompat.app.AppCompatActivity;

import com.pixtanta.android.Utils.EditableAccommodatingLatinIMETypeNullIssues;
import com.pixtanta.android.Utils.HtmlParser;
import com.pixtanta.android.Utils.StringUtils;
import com.vdurmont.emoji.EmojiParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Pattern;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.socketUrl;
import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.HomeAct.showReactors;
import static com.pixtanta.android.HomeAct.updateMessageDelivery;
import static com.pixtanta.android.HomeAct.userPages;

public class CommentsActivity extends ThemeActivity {

    Context cntxt;
    String prevHtmlTxt;
    public static String lang, postID, commentID, myPht, myName, myIDtoString, poster, postType, posterImg, posterName, posterUName, pagerId;
    static String myUserName;
    RelativeLayout mainHomeView, header, comView;
    static int myId;
    int verifiedIcon, width, height, sort = -1, count = 0, mCount = 0;
    ImageView myComPht;
    ProgressBar progressBar;
    EditText commentBox;
    @SuppressLint("StaticFieldLeak")
    static TextView curEditableView;
    ImageButton submitCommentBtn;
    ImageLoader imageLoader;
    DisplayMetrics displayMetrics;
    boolean userVerified;
    LinearLayout commentsLayout, deleteLayout, blackFade, commentBx, mentionLayer, menu;
    Button cancelDelete, confirmDelete;
    ScrollView scrollView, scrllView;
    boolean loadingPost, vary, allLoaded, shakeOpt, hashed, scrllVIState, changingTxt, afterAnchor, btwnHtml, addingTxt;
    ArrayList<String> selectedComms = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    public static View currView;
    static Socket socket;
    ArrayList<RelativeLayout> comLayouts = new ArrayList<>();
    int[][] reactions;
    int selStart, selEnd, strLn, charLn;
    static JSONObject comLayoutsX, commentHtmlText = new JSONObject();
    JSONObject optIcons = new JSONObject(), pendingDel = new JSONObject(), likesDisplay = new JSONObject(), repsDisplay = new JSONObject(), emitObject = new JSONObject();
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    SharedPrefMngr sharedPrefMngr;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);

        lang = sharedPrefMngr.getSelectedLanguage();
        mainHomeView = findViewById(R.id.mainHomeView);
        setupUI(mainHomeView);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        verifiedIcon = R.drawable.ic_verified_user;
        comLayoutsX = new JSONObject();
        commentHtmlText = new JSONObject();

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(CommentsActivity.this, LoginAct.class));
            return;
        }

        changingTxt = false;
        vary = false;
        allLoaded = false;
        hashed = false;
        scrllVIState = false;

        myId = sharedPrefMngr.getMyId();
        myPht = Constants.www + sharedPrefMngr.getMyPht();
        myName = sharedPrefMngr.getMyName();
        myUserName = sharedPrefMngr.getMyUserName();
        myIDtoString = String.valueOf(myId);
        userVerified = sharedPrefMngr.getMyVerification();
        Bundle userParams = getIntent().getExtras();
        postID = userParams.getString("postID");
        commentID = userParams.getString("commentID");

        try {
            optIcons.put("copy", R.drawable.ic_copy);
            optIcons.put("edit", R.drawable.ic_edit);
            optIcons.put("reply", R.drawable.ic_reply_ash);
            optIcons.put("hide", R.drawable.ic_hide_post);
            optIcons.put("report", R.drawable.ic_report_post);
            optIcons.put("delete", R.drawable.ic_delete);
            emitObject.put("user", myId);
            emitObject.put("postID", postID);
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
        reactions = new int[][]{
                new int[]{1, R.drawable.ic_loved},
                new int[]{0, R.drawable.ic_unreacted}
        };


        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

        imageLoader = new ImageLoader(this);
        myComPht = findViewById(R.id.myComPht);
        progressBar = findViewById(R.id.progressBar);
        menu = findViewById(R.id.menu);
        commentBox = findViewById(R.id.commentBox);
        submitCommentBtn = findViewById(R.id.submitCommentBtn);
        commentsLayout = findViewById(R.id.commentsLayout);
        header = findViewById(R.id.header);
        deleteLayout = findViewById(R.id.deleteLayout);
        blackFade = findViewById(R.id.blackFade);
        commentBx = findViewById(R.id.commentBx);
        mentionLayer = findViewById(R.id.mentionLayer);
        scrllView = findViewById(R.id.comScrllView);
        scrollView = findViewById(R.id.scrllView);
        cancelDelete = findViewById(R.id.cancelDelete);
        confirmDelete = findViewById(R.id.confirmDelete);
        imageLoader.displayImage(myPht, myComPht);

        menu.setOnClickListener((v) -> openOptions());
        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });
        submitCommentBtn.setOnClickListener(v -> submitComment());
        cancelDelete.setOnClickListener(v -> deleteLayout.setVisibility(View.GONE));
        getCommentDisplay();
        commentBox.setAccessibilityDelegate(new View.AccessibilityDelegate(){
            @Override
            public void sendAccessibilityEvent(View host, int eventType) {
                super.sendAccessibilityEvent(host, eventType);
                if(eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
                    new android.os.Handler().postDelayed(() -> {
                        selStart = commentBox.getSelectionStart();
                        selEnd = commentBox.getSelectionEnd();
                        afterAnchor = getCursorPosition(commentBox);
                        btwnHtml = getCursorState(commentBox);
                    }, 50);
                }
            }
        });
        commentBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                strLn = s.length();
                prevHtmlTxt = Html.toHtml(commentBox.getText());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charLn = count;
                if(!changingTxt) {
                    try {
                        String allText = s.toString();
                        int caretPos = commentBox.getSelectionStart();
                        String text = allText.substring(0, caretPos);
                        int textLen = text.length(), textL = textLen - 1;
                        if(allText.length() > caretPos) {
                            char nextChar = allText.charAt(caretPos);
                            if (!Character.isWhitespace(nextChar))
                                scrllView.setVisibility(View.GONE);
                        }
                        if (textLen > 0) {
                            char lastChar = text.charAt(textL);
                            if (Character.isWhitespace(lastChar))
                                scrllView.setVisibility(View.GONE);
                        }
                        String[] textArr = text.split("[^\\w_#]");
                        String lastWord = text;
                        if(textArr.length > 0) {
                            int lastWordIndex = textArr.length - 1;
                            lastWord = textArr[lastWordIndex];
                        }
                        int lastWordLen = lastWord.length();
                        String regex = "^#[\\w_]+$";
                        boolean matchedRegex = Pattern.matches(regex, lastWord) || lastWord.equals("#");
                        if (!hashed && matchedRegex) {
                            changeWordToHashTag(commentBox, lastWord);
                            return;
                        }
                        if (lastWordLen > 1) {
                            String htmlText = Html.toHtml((Spanned) commentBox.getText().subSequence(caretPos - lastWordLen, caretPos));
                            Document doc = Jsoup.parse(htmlText, "UTF-8");
                            Elements anchorElement = doc.getElementsByTag("a");
                            String href = anchorElement.attr("href");
                            if (href.length() == 0) {
                                JSONObject emitObj = new JSONObject();
                                emitObj.put("user", myId);
                                emitObj.put("advanced", true);
                                emitObj.put("table", "comments");
                                emitObj.put("dataId", postID);
                                emitObj.put("lastWord", lastWord);
                                socket.emit("checkMention", emitObj);
                                return;
                            }
                            scrllView.setVisibility(View.GONE);
                        } else {
                            if(lastWordLen == 1 && lastWord.equals("@")){
                                JSONObject emitObj = new JSONObject();
                                emitObj.put("user", myId);
                                emitObj.put("advanced", true);
                                emitObj.put("table", "comments");
                                emitObj.put("dataId", postID);
                                emitObj.put("lastWord", "");
                                socket.emit("checkMention", emitObj);
                                return;
                            }
                            scrllView.setVisibility(View.GONE);
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
                    int end = commentBox.getSelectionEnd();
                    boolean minCheck = selStart > 0, maxCheck = end < s.length();
                    if(minCheck || maxCheck)
                        removeAnchor(commentBox, selStart, end, charLn, minCheck, maxCheck);
                } else if(backSpaced && !changingTxt && !addingTxt && afterAnchor){
                    try {
                        overrideBackspace(commentBox, selStart, selEnd);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        scrllView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if(scrllView.getVisibility() == View.VISIBLE && !scrllVIState)
                scrllView.scrollTo(0, 0);
            scrllVIState = scrllView.getVisibility() == View.VISIBLE;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener((scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int scrollH = commentsLayout.getHeight() - scrllVw.getHeight() - height;
                if(scrollY > scrollH && !loadingPost && !allLoaded){
                    loadingPost = true;
                    progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.image_loader, null);
                    commentsLayout.addView(progressBar);
                    setupUI(mainHomeView);
                    progressBar.post(this::getCommentDisplay);
                }
            });
        }

        
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
        socket.on("submitComment", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                String postId = String.valueOf(argsArr.getInt(1));
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
                    LinearLayout helder = commentView.findViewById(R.id.helder);
                    ImageView userImageView = commentView.findViewById(R.id.profPic);
                    TextView commentText = commentView.findViewById(R.id.commentText);
                    TextView nameTxtVw = commentView.findViewById(R.id.posterName);
                    TextView likeNum = commentView.findViewById(R.id.likeNum);
                    TextView userNameTxtVw = commentView.findViewById(R.id.posterUName);
                    TextView commentDateVw = commentView.findViewById(R.id.commentDate);
                    ImageButton reply = commentView.findViewById(R.id.reply);
                    ImageButton react = commentView.findViewById(R.id.react);
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
                        commentsLayout.addView(commentView);
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
                LinearLayout helder = comLayout.findViewById(R.id.helder);
                ImageButton reply = comLayout.findViewById(R.id.reply);
                ImageButton react = comLayout.findViewById(R.id.react);
                TextView replyNum = comLayout.findViewById(R.id.replyNum);
                TextView likeNum = comLayout.findViewById(R.id.likeNum);
                TextView commentTxtVw = comLayout.findViewById(R.id.commentText);
                TextView commentDateVw = comLayout.findViewById(R.id.commentDate);
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
                        int newTag = reactions[tag][0];
                        int newReact = reactions[tag][1];
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
        socket.on("reverseSubmitComment", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                int counter = argsArr.getInt(1);
                if(!(counter == -1) && !(comLayouts.get(counter) == null)) {
                    RelativeLayout comLayout = comLayouts.get(counter);
                    runOnUiThread(() -> commentsLayout.removeView(comLayout));
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
                    TextView textView = relativeLayout.findViewById(R.id.replyNum);
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
                    TextView textView = relativeLayout.findViewById(R.id.commentText);
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
        socket.on("mentionList", args -> {
            try {
                JSONArray listArray = new JSONArray(args[0].toString());
                if(listArray.length() == 0){
                    runOnUiThread(() -> scrllView.setVisibility(View.GONE));
                    return;
                }
                runOnUiThread(() -> {
                    if(mentionLayer.getChildCount() > 0)
                        mentionLayer.removeAllViews();
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
                    listView.setOnClickListener(v -> addTextToView(commentBox, finalName, tab, id));
                    runOnUiThread(() -> mentionLayer.addView(listView));
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

    @SuppressLint("InflateParams")
    private void openOptions() {
        try {
            if (blackFade.getChildCount() > 0) blackFade.removeAllViews();
            RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
            LinearLayout optLayer = relativeLayout.findViewById(R.id.optLayer);
            JSONObject object = new JSONObject();
            JSONObject objectIcon = new JSONObject();
            object.put("viewPost", "View Post");
            object.put("sort", "Sort Comments By");
            objectIcon.put("viewPost", R.drawable.ic_post);
            objectIcon.put("sort", R.drawable.ic_sort);
            JSONArray objKeys = object.names();
            for (int r = 0; r < Objects.requireNonNull(objKeys).length(); r++) {
                TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
                String key = objKeys.getString(r);
                String option = object.getString(key);
                int drawableLeft = objectIcon.getInt(key);
                optionList.setText(option);
                optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
                optionList.setOnClickListener(v -> {
                    switch (key){
                        case "viewPost":
                            visitPost();
                            break;
                        case "sort":
                            openSorter();
                            break;
                    }
                });
                optLayer.addView(optionList);
            }
            blackFade.addView(relativeLayout);
            blackFade.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void visitPost() {
        blackFade.setVisibility(View.GONE);
        Intent intent = new Intent(this, PostDisplayAct.class);
        Bundle bundle = new Bundle();
        bundle.putString("postId", postID);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @SuppressLint("InflateParams")
    private void openSorter() {
        if (blackFade.getChildCount() > 0) blackFade.removeAllViews();
        RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
        LinearLayout optLayer = relativeLayout.findViewById(R.id.optLayer);
        String[] strings = new String[]{"Sort By Oldest", "Sort By Newest", "Sort By Most Relevant"};
        for (int x = 0; x < strings.length; x++) {
            TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
            String option = strings[x];
            int drawableLeft = R.drawable.ic_check_false;
            if(x == sort)
                drawableLeft = R.drawable.ic_check_true_red;
            optionList.setText(option);
            optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
            int finalX = x;
            optionList.setOnClickListener(v -> {
                blackFade.setVisibility(View.GONE);
                resortComments(finalX);
            });
            optLayer.addView(optionList);
        }
        blackFade.addView(relativeLayout);
        blackFade.setVisibility(View.VISIBLE);
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
                boolean nxtCharIsSpace1 = spc1.equals(" "), nxtCharIsSpace2 = spc2.equals(" ");
                firstIsSpace = !(spc1 == null) && nxtCharIsSpace1;
                secondIsSpace = !(spc2 == null) && nxtCharIsSpace2;
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

    @SuppressLint("InflateParams")
    private void openReportDialog() {
        RelativeLayout view = (RelativeLayout) getLayoutInflater().inflate(R.layout.report_box, null);
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
        setupUI(mainHomeView);
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
            emitObj.put("postId", postID);
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

    private void resortComments(int reSorter) {
        if(!loadingPost && !(sort == reSorter)){
            sort = reSorter;
            commentsLayout.removeAllViews();
            commentsLayout.addView(progressBar);
            progressBar.setVisibility(View.VISIBLE);
            selectedComms.clear();
            getCommentDisplay();
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
                        visitSearch(cntxt,dataId);
                } else
                    openLink(cntxt, href);
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void getCommentDisplay(){
        loadingPost = true;
        while (loadingPost){
            MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("lang", lang)
                    .addFormDataPart("user", myIDtoString)
                    .addFormDataPart("selectedComms", selectedComms.toString().intern())
                    .addFormDataPart("postID", postID);
            if(!(commentID == null))
                multipartBody.addFormDataPart("commentID", commentID);
            if(!(sort == -1))
                multipartBody.addFormDataPart("sort", String.valueOf(sort));
            RequestBody requestBody = multipartBody.build();
            Request request = new Request.Builder()
                    .url(Constants.commentUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if(response.isSuccessful()){
                    String responseString = Objects.requireNonNull(response.body()).string();
                    loadingPost = false;
                    progressBar.setVisibility(View.GONE);
                    JSONArray responseArr = new JSONArray(responseString);
                    boolean perm = responseArr.getBoolean(2);
                    pagerId = responseArr.getString(4);
                    vary = responseArr.getBoolean(3);
                    allLoaded = responseArr.getBoolean(5);
                    if(!vary){
                        poster = String.valueOf(myId);
                        postType = "profile";
                        posterImg = myPht;
                        posterName = myName;
                        posterUName = myUserName;
                    }
                    if(!perm)
                        commentBx.setVisibility(View.GONE);
                    sort = responseArr.getInt(0);
                    JSONArray commentArr = new JSONArray(responseArr.getString(1));
                    int arrLen = commentArr.length(), comCount = 0;
                    header.setVisibility(View.VISIBLE);
                    for (int p = 0; p < arrLen; p++) {
                        JSONObject commentObj = new JSONObject(commentArr.getString(p));
                        String comID = commentObj.getString("id");
                        String commentUser = commentObj.getString("user");
                        String commentName = commentObj.getString("name");
                        String commentUserName = commentObj.getString("userName");
                        String commentPhoto = Constants.www + commentObj.getString("photo");
                        String comment = commentObj.getString("comment");
                        String commentDate = commentObj.getString("date");
                        String type = commentObj.getString("type");
                        boolean verified = commentObj.getBoolean("verified");
                        boolean access = commentObj.getBoolean("access");
                        int likeNum = commentObj.getInt("likeNum");
                        int repNum = commentObj.getInt("repNum");
                        boolean liked = commentObj.getBoolean("liked");
                        commentHtmlText.put(comID, comment);
                        selectedComms.add(postID);
                        RelativeLayout commentView = (RelativeLayout) getLayoutInflater().inflate(R.layout.comment_layout, null);
                        if(commentID != null && commentID.equals(comID))
                            comView = commentView;
                        ImageView userImageView = commentView.findViewById(R.id.profPic);
                        LinearLayout helder = commentView.findViewById(R.id.helder);
                        TextView commentText = commentView.findViewById(R.id.commentText);
                        TextView nameTxtVw = commentView.findViewById(R.id.posterName);
                        TextView userNameTxtVw = commentView.findViewById(R.id.posterUName);
                        TextView commentDateVw = commentView.findViewById(R.id.commentDate);
                        TextView likeNumTV = commentView.findViewById(R.id.likeNum);
                        TextView replyNumTV = commentView.findViewById(R.id.replyNum);
                        ImageButton reply = commentView.findViewById(R.id.reply);
                        ImageButton react = commentView.findViewById(R.id.react);
                        comLayoutsX.put(comID, commentView);
                        repsDisplay.put(comID, replyNumTV);
                        likesDisplay.put(comID, likeNumTV);
                        imageLoader.displayImage(commentPhoto, userImageView);
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
                        nameTxtVw.setText(commentName);
                        commentDateVw.setText(commentDate);
                        if(liked) {
                            react.setBackgroundResource(R.drawable.ic_loved);
                            react.setTag("1");
                        }
                        if (!StringUtils.isEmpty(commentUserName))
                            userNameTxtVw.setText("@" + commentUserName);
                        else
                            userNameTxtVw.setVisibility(View.GONE);
                        if(verified)
                            nameTxtVw.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                        if (likeNum > 0) {
                            String likes = Functions.convertToText(likeNum);
                            likeNumTV.setText(likes);
                        }
                        if (repNum > 0) {
                            String reps = Functions.convertToText(repNum);
                            replyNumTV.setText(reps);
                        }
                        likeNumTV.setOnClickListener(v -> showReactors(cntxt, comID, "comments", likeNum));
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
                        userImageView.setOnClickListener(v -> {
                            switch (type){
                                case "profile":
                                    visitUserProfile(cntxt, Integer.parseInt(commentUser));
                                    break;
                                case "page":
                                    visitPage(cntxt, Integer.parseInt(commentUser));
                                    break;
                            }
                        });
                        userNameTxtVw.setOnClickListener(v -> {
                            switch (type){
                                case "profile":
                                    visitUserProfile(cntxt, Integer.parseInt(commentUser));
                                    break;
                                case "page":
                                    visitPage(cntxt, Integer.parseInt(commentUser));
                                    break;
                            }
                        });
                        nameTxtVw.setOnClickListener(v -> {
                            switch (type){
                                case "profile":
                                    visitUserProfile(cntxt, Integer.parseInt(commentUser));
                                    break;
                                case "page":
                                    visitPage(cntxt, Integer.parseInt(commentUser));
                                    break;
                            }
                        });
                        commentsLayout.addView(commentView);
                        comCount++;
                        if(commentID != null && comView != null && comCount == arrLen){
                            scrollToComment(comView);
                        }
                    }
                    setupUI(mainHomeView);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void hideComments(RelativeLayout commentView, String comID) {
        @SuppressLint("InflateParams") View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
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
                emitObj.put("postId", postID);
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

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void reportComments(String comID) {
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

    public static void hideLayout(String comID) throws JSONException {
        RelativeLayout relativeLayout = (RelativeLayout) comLayoutsX.get(comID);
        relativeLayout.setVisibility(View.GONE);
    }

    private void replyComment(View view, String comID) {
        currView = view;
        Intent intent = new Intent(CommentsActivity.this, RepliesActivity.class);
        Bundle userParams = new Bundle();
        userParams.putString("postID", postID);
        userParams.putString("comID", comID);
        intent.putExtras(userParams);
        startActivity(intent);
    }

    private void editComment(TextView view, String comID) throws JSONException {
        curEditableView = view;
        String curTxt = commentHtmlText.getString(comID);
        Intent intent = new Intent(CommentsActivity.this, EditCommentActivity.class);
        Bundle userParams = new Bundle();
        userParams.putString("postID", postID);
        userParams.putString("comID", comID);
        userParams.putString("curTxt", curTxt);
        userParams.putInt("activityInd", 0);
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
            LinearLayout optLayer = postOptView.findViewById(R.id.optLayer);
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
                emitObj.put("postId", postID);
                emitObj.put("comment", comment);
                emitObj.put("userName", myUserName);
                emitObj.put("date", date);
                socket.emit("editComment", emitObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static void visitPage(Context cntxt, int pageId) {
        Intent intent = new Intent(cntxt, PageActivity.class);
        Bundle pageParams = new Bundle();
        pageParams.putInt("pageId", pageId);
        intent.putExtras(pageParams);
        cntxt.startActivity(intent);
    }

    private void deleteComment(View view, String comID, boolean real) {
        deleteLayout.setVisibility(View.VISIBLE);
        confirmDelete.setOnClickListener(v -> {
            deleteLayout.setVisibility(View.GONE);
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
        emitObj.put("postId", postID);
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

    private void scrollToComment(View view) {
        view.post(() -> {
            int scrllTo = view.getBottom();
            if(scrllTo < scrollView.getHeight())
                scrllTo = view.getTop();
            scrollView.scrollTo(0, scrllTo);
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
            commentID = null;
        });
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void submitComment() {
        String commentR = commentBox.getText().toString();
        if(!StringUtils.isEmpty(commentR)){
            if(vary && postType == null && poster == null){
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
                    tabletR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName));
                    imageViewR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName));
                    txtNameR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName));
                    txtUserNameR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName));
                    layR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName));
                    for (int r = 0; r < userPages.length(); r++){
                        JSONObject pageInfo = new JSONObject(userPages.getString(r));
                        String pageId = pageInfo.getString("id");
                        if(pageId.equals(pagerId)) {
                            String pageName = pageInfo.getString("pageName");
                            String pagePhoto = Constants.www + pageInfo.getString("photo");
                            LinearLayout tablet = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.tablet, null, false);
                            LinearLayout lay = tablet.findViewById(R.id.lay);
                            ImageView imageView = tablet.findViewById(R.id.image);
                            TextView txtName = tablet.findViewById(R.id.name);
                            TextView txtUserName = tablet.findViewById(R.id.userName);
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
            String htmlText = Html.toHtml(commentBox.getText());
            commentBox.setText("");
            String comment = HtmlParser.parseString(htmlText);
            String comSpan = HtmlParser.parseSpan(comment);
            RelativeLayout comLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.comment_layout, null);
            LinearLayout helder = comLayout.findViewById(R.id.helder);
            ImageView imgView = comLayout.findViewById(R.id.profPic);
            TextView commerName = comLayout.findViewById(R.id.posterName);
            TextView commerUName = comLayout.findViewById(R.id.posterUName);
            TextView commentTxtVw = comLayout.findViewById(R.id.commentText);
            TextView commentDateVw = comLayout.findViewById(R.id.commentDate);
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
            commentsLayout.addView(comLayout);
            comLayouts.add(comLayout);
            scrollToComment(comLayout);
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
                reset();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static void openLink(Context context, String linkUrl) {
        Uri uri = Uri.parse(linkUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        context.startActivity(intent);
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

    private void reset() {
        count++;
        if(vary) {
            poster = null;
            postType = null;
            posterUName = null;
            posterName = null;
            posterImg = null;
        }
    }

    private static void visitUserProfile(Context cntxt, int user) {
        Intent intent = new Intent(cntxt, ProfileAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("userID", user);
        intent.putExtras(userParams);
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

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        view.setOnTouchListener((v, event) -> {
            if (!(view instanceof EditText)) {
                if(!(view == scrllView)) {
                    scrllView.setVisibility(View.GONE);
                    hideSoftKeyboard(v);
                }
            }
            return false;
        });

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public  void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
        } else if(deleteLayout.getVisibility() == View.VISIBLE){
            deleteLayout.setVisibility(View.GONE);
        } else if(scrllView.getVisibility() == View.VISIBLE){
            scrllView.setVisibility(View.GONE);
        } else {
            socket.emit("disconnected", myId);
            socket.emit("removeCommentPage", emitObject);
            finish();
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
