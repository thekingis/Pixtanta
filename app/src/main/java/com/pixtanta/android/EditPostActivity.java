package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;

import com.pixtanta.android.Utils.CompatEditText;
import com.pixtanta.android.Utils.ContextData;
import com.pixtanta.android.Utils.HtmlParser;
import com.pixtanta.android.Utils.StringUtils;

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
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.socketUrl;
import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.HomeAct.updateMessageDelivery;

public class EditPostActivity extends ThemeActivity implements View.OnClickListener {

    public Context cntxt;
    public static ArrayList<File> itemLists;
    public static ArrayList<String> selectedImages, editedImages, postImages;
    ContextData contextData;
    RelativeLayout mainHomeView;
    LinearLayout blackFade;
    TextView ups;
    String postId, type, pageId;
    ImageAdaptor imageAdaptor;
    LinearLayout loader, mentionLayer;
    Button postBtn;
    ScrollView scrllView;
    CompatEditText postTxt, newEditText;
    int width, height, mCount, reqFrm, selStart, selEnd, prevLen, nextLen;
    static int imgW;
    private int myId;
    String lang, myIDtoString, myName, myUserName;
    DisplayMetrics displayMetrics;
    @SuppressLint("StaticFieldLeak")
    static GridView gridView;
    Socket socket;
    static ImageLoader imageLoader;
    boolean shakeOpt, hashed = false, keyPressed = false;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        sharedPrefMngr = new SharedPrefMngr(this);

        sharedPrefMngr.initializeSmartLogin();
        lang = sharedPrefMngr.getSelectedLanguage();
        cntxt = this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(this, LoginAct.class));
            return;
        }
        Bundle userParams = getIntent().getExtras();
        postId = userParams.getString("postId");
        reqFrm = userParams.getInt("reqFrm");
        myId = sharedPrefMngr.getMyId();

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
        imgW = Math.round((widthPadding/3) - 10);
        itemLists = new ArrayList<>();
        selectedImages = new ArrayList<>();
        editedImages = new ArrayList<>();
        postImages = new ArrayList<>();

        myName = sharedPrefMngr.getMyName();
        myUserName = sharedPrefMngr.getMyUserName();
        myIDtoString = Integer.toString(myId);
        imageLoader = new ImageLoader(cntxt);

        newEditText = new CompatEditText(this);
        mainHomeView = findViewById(R.id.mainHomeView);
        gridView = findViewById(R.id.gridView);
        blackFade = findViewById(R.id.blackFade);
        loader = findViewById(R.id.loader);
        mentionLayer = findViewById(R.id.mentionLayer);
        postBtn = findViewById(R.id.postBtn);
        postTxt = findViewById(R.id.postTxt);
        scrllView = findViewById(R.id.scrllView);
        ups = findViewById(R.id.ups);

        setupUI(mainHomeView);

        ups.setOnClickListener(this);
        postBtn.setOnClickListener(this);
        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });

        loadPostContents();
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
                    scrllView.setVisibility(View.VISIBLE);
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
                    listView.setOnClickListener(v -> addTextToView(postTxt, finalName, tab, id));
                    runOnUiThread(() -> mentionLayer.addView(listView));
                }
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

    private void overrideBackspace(CompatEditText editText, CharSequence s) throws JSONException {
        hashed = true;
        String htmlText = Html.toHtml(newEditText.getText());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            editText.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT));
        else
            editText.setText(Html.fromHtml(htmlText));
        int caretEnd = selEnd;
        if (caretEnd == 0) {
            reverseOverride(editText, 0, 0);
            return;
        }
        int caretStart = selStart;
        if (caretStart == caretEnd)
            caretStart = caretEnd - 1;
        htmlText = Html.toHtml((Spanned) editText.getText().subSequence(caretStart, caretEnd));
        Document doc = Jsoup.parse(htmlText, "UTF-8");
        Elements anchorElement = doc.getElementsByTag("a");
        if (anchorElement == null) {
            reverseOverride(editText, caretStart, caretEnd);
            return;
        }
        String href = anchorElement.attr("href");
        if (href.length() == 0) {
            reverseOverride(editText, caretStart, caretEnd);
            return;
        }
        String startStrOne = "Friend";
        String startStrTwo = "Page";
        if (!(href.startsWith(startStrOne) || href.startsWith(startStrTwo))) {
            reverseOverride(editText, caretStart, caretEnd);
            return;
        }
        String allHtmlText = Html.toHtml(editText.getText());
        Document allDoc = Jsoup.parse(allHtmlText, "UTF-8");
        Elements parentAnchorElement = allDoc.select("a[href=\"" + href + "\"]");
        String anchorTextHtml = parentAnchorElement.html();
        String anchorText = Jsoup.parse(anchorTextHtml).text();
        int anchorTextLen = anchorText.length();
        int charIndex = textWalker(editText, href, caretStart, caretEnd, 0);
        if (anchorTextLen == charIndex) {
            JSONArray anchorTextArr = new JSONArray(anchorText.split(" "));
            int lastIndex = anchorTextArr.length() - 1;
            anchorTextArr.remove(lastIndex);
            String newAnchorText = Functions.joinJSONArray(anchorTextArr, " ");
            Spanned newHtmlText;
            boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
            String bColor = "#F9F9F9";
            if(darkThemeEnabled)
                bColor = "#585858";
            String str = " <a href=\"" + href + "\"><b><span style=\"background-color:"+bColor+";\">" + newAnchorText + "</span></b></a>";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                newHtmlText = Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT);
            else
                newHtmlText = Html.fromHtml(str);
            int indexStart = caretEnd - anchorTextLen;

            int newCaretPos = indexStart + newAnchorText.length();
            editText.setText(editText.getText().delete(indexStart, caretEnd));
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
            editText.setText(editText.getText().delete(indexStart, caretEnd));
            editText.getText().insert(indexStart, anchorText);
            editText.setSelection(newCaretPos);
        }
        //searchForMentions(s);
        hashed = false;
    }

    private void reverseOverride(CompatEditText editText, int caretStart, int caretEnd) {
        editText.setText(Objects.requireNonNull(editText.getText()).delete(caretStart, caretEnd));
        editText.setSelection(caretStart);
        new android.os.Handler().postDelayed(() -> hashed = false, 500);
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
        scrllView.setVisibility(View.GONE);
        hashed = true;
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
        String allHtmlText = Html.toHtml(editText.getText());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            newEditText.setText(Html.fromHtml(allHtmlText, Html.FROM_HTML_MODE_COMPACT));
        else
            newEditText.setText(Html.fromHtml(allHtmlText));
        hashed = false;
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

    private void loadPostContents() {
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("user", myIDtoString)
                .addFormDataPart("postId", postId)
                .build();
        Request request = new Request.Builder()
                .url(Constants.postContentUrl)
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
                runOnUiThread(() -> {
                    loader.setVisibility(View.GONE);
                    try {
                        JSONObject object = new JSONObject(responseString);
                        String postText = object.getString("postText");
                        String postFiles = object.getString("postFiles");
                        type = object.getString("type");
                        pageId = object.getString("pageId");
                        boolean hasFiles = object.getBoolean("hasFiles");
                        mCount = object.getInt("mCount");
                        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                        postText = HtmlParser.parseTheme(postText, darkThemeEnabled);
                        Spanned htmlText;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            htmlText = Html.fromHtml(postText, Html.FROM_HTML_MODE_COMPACT);
                        else
                            htmlText = Html.fromHtml(postText);
                        if(!StringUtils.isEmpty(postText))
                            postTxt.setText(htmlText);
                        if(hasFiles){
                            JSONArray files = new JSONArray(postFiles);
                            String[] selectedFiles = new String[files.length()];
                            for(int i = 0; i < files.length(); i++){
                                String filePath = Constants.www + files.getString(i);
                                postImages.add(filePath);
                                selectedFiles[i] = filePath;
                            }
                            imageAdaptor = new ImageAdaptor(cntxt, selectedFiles, imgW);
                            gridView.setAdapter(imageAdaptor);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    initializeTextbox();
                });
            }
        });
    }

    private void initializeTextbox() {
        postTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevLen = postTxt.getCaretStart();
                if(!hashed && keyPressed) {
                    selEnd = postTxt.getCaretStart();
                    String htmlText = Html.toHtml(postTxt.getText());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        newEditText.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT));
                    else
                        newEditText.setText(Html.fromHtml(htmlText));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nextLen = postTxt.getCaretStart();
                if(!hashed) {
                    selStart = postTxt.getCaretEnd();
                    if (prevLen > nextLen && keyPressed) {
                        try {
                            overrideBackspace(postTxt, s);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else
                        searchForMentions(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        postTxt.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN)
                keyPressed = true;
            if(event.getAction() == KeyEvent.ACTION_UP)
                keyPressed = false;
            return false;
        });
    }

    private void searchForMentions(CharSequence s) {
        try {
            String allText = s.toString();
            int caretPos = postTxt.getSelectionStart();
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
                    changeWordToHashTag(postTxt, lastWord);
                    return;
                }
                String htmlText = Html.toHtml((Spanned) postTxt.getText().subSequence(caretPos - lastWordLen, caretPos));
                Document doc = Jsoup.parse(htmlText, "UTF-8");
                Elements anchorElement = doc.getElementsByTag("a");
                String href = anchorElement.attr("href");
                if(href.length() == 0) {
                    JSONObject emitObj = new JSONObject();
                    emitObj.put("user", myId);
                    emitObj.put("advanced", false);
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
                        emitObj.put("advanced", false);
                        emitObj.put("lastWord", "");
                        socket.emit("checkMention", emitObj);
                        return;
                    }
                    if(lastWord.equals("#")) {
                        String htmlText = Html.toHtml((Spanned) postTxt.getText().subSequence(caretPos - 1 , caretPos));
                        htmlText = htmlText.replace("<p dir=\"ltr\">", "");
                        htmlText = htmlText.replace("</p>", "");
                        String startStr = "<b><span style=\"background-color:#F9F9F9;\">";
                        if(htmlText.startsWith(startStr)) {
                            postTxt.setText(postTxt.getText().delete(caretPos - 1, caretPos));
                            postTxt.getText().insert(caretPos - 1, "#");
                            postTxt.setSelection(caretPos);
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

    @SuppressLint({"SetTextI18n", "NonConstantResourceId"})
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ups:
                Intent intent = new Intent(cntxt, PhotoGridAct.class);
                Bundle fileParams = new Bundle();
                fileParams.putInt("act", 1);
                intent.putExtras(fileParams);
                startActivity(intent);
                break;
            case R.id.postBtn:
                String writeUp = postTxt.getText().toString();
                if(StringUtils.isEmpty(writeUp) && itemLists.size() == 0 && postImages.size() == 0){
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
                    agreeBtn.setOnClickListener(v1 -> blackFade.setVisibility(View.GONE));
                    blackFade.addView(blockView);
                    blackFade.setVisibility(View.VISIBLE);
                } else {
                    savePostEdit();
                }
                break;
            default:
                break;
        }
    }

    private void savePostEdit() {
        String text = Html.toHtml(postTxt.getText());
        try {
            if(reqFrm == 0)
                HomeAct.savePostEdit(cntxt, postImages, itemLists, editedImages, text, postId, type, pageId);
            else if(reqFrm == 1)
                HomeFragment.savePostEdit(cntxt, postImages, itemLists, editedImages, text, postId, type, pageId);
            else if(reqFrm == 2)
                PageHomeFragment.savePostEdit(cntxt, postImages, itemLists, editedImages, text, postId, type, pageId);
            else if(reqFrm == 3)
                PostDisplayAct.savePostEdit(cntxt, postImages, itemLists, editedImages, text, postId, type, pageId);
            contextData = null;
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class ImageAdaptor extends BaseAdapter {

        private final Context context;
        private final int imageWidth;
        String[] itemList;
        Bitmap bitmap;

        public ImageAdaptor(Context cntxt, String[] itemList, int imageWidth) {
            this.itemList = itemList;
            this.imageWidth = imageWidth;
            context = cntxt;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.image_box, parent, false);
            convertView.setLayoutParams(new GridView.LayoutParams(imageWidth ,imageWidth ));
            final String filePath = itemList[position];
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
                try {
                    if(filePath.startsWith("http")) {
                        String vidTime = Functions.getMediaTime(filePath);
                        videoTime.setText(vidTime);
                        bitmap = Functions.getVideoThumbnail(filePath, true);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        File thisFile = new File(filePath);
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(context, Uri.fromFile(thisFile));
                        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        long timeInMillisec = Long.parseLong(time);
                        String vidTime = Functions.convertMilliTime(timeInMillisec);
                        videoTime.setText(vidTime);
                        retriever.release();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            } else {
                if (filePath.startsWith("http"))
                    imageLoader.displayImage(filePath, imageView);
                else {
                    bitmap = Functions.decodeFiles(filePath, fileType, false);
                    imageView.setImageBitmap(bitmap);
                }
            }

            editPhoto.setOnClickListener(v -> openPhotoEditor(context, position, fileType));
            deletePhoto.setOnClickListener(v -> {
                int perm = 1;
                if (filePath.startsWith("http"))
                    perm = 0;
                requestDeletePhoto(context, position, perm);
            });

            return convertView;
        }
    }

    private static void openPhotoEditor(Context context, int fileId, String fileType) {
        String thisFilePath;
        if(postImages.size() > fileId)
            thisFilePath = postImages.get(fileId);
        else
            thisFilePath = selectedImages.get(fileId - postImages.size());
        Intent intent = new Intent(context, PhotoEditorAct.class);
        if(fileType.equals("video"))
            intent = new Intent(context, VideoEditorAct.class);
        Bundle fileParams = new Bundle();
        fileParams.putInt("act", 1);
        fileParams.putInt("fileId", fileId);
        fileParams.putString("filePath", thisFilePath);
        intent.putExtras(fileParams);
        context.startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    private void requestDeletePhoto(Context cntxt, int fileId, int perm) {
        @SuppressLint("InflateParams") View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        TextView txter = blockView.findViewById(R.id.txter);
        Button cnclBtn = blockView.findViewById(R.id.cancel);
        Button agreeBtn = blockView.findViewById(R.id.agree);
        txter.setText("Are you sure you want to delete this photo?");
        agreeBtn.setOnClickListener(v -> {
            if(perm == 1) {
                File thisFile = itemLists.get(fileId - postImages.size());
                String thisFilePath = selectedImages.get(fileId - postImages.size());
                itemLists.remove(thisFile);
                selectedImages.remove(thisFilePath);
                if (editedImages.contains(thisFilePath) && thisFile.exists()) {
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
            }
            if(perm == 0) {
                String thisFilePath = postImages.get(fileId);
                postImages.remove(thisFilePath);
            }
            setImageGridViews(cntxt);
            blackFade.setVisibility(View.GONE);
        });
        cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
        blackFade.addView(blockView);
        blackFade.setVisibility(View.VISIBLE);
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
        int totalSize = selectedImages.size() + postImages.size();
        if(totalSize > 0){
            String selectedNumTxt = String.valueOf(totalSize);
            selectedNumTxt += " Selected";
            selectionsTxt.setText(selectedNumTxt);
        } else {
            selectionsTxt.setText("");
        }
    }

    public void setImageGridViews(Context cntxt) {
        int piSize = postImages.size(), ilSize = itemLists.size(), totalSize = piSize + ilSize;
        String[] selectedFiles = new String[totalSize];
        for (int x = 0; x < piSize; x++){
            String selectedFile = postImages.get(x);
            selectedFiles[x] = selectedFile;
        }
        for (int y = piSize; y < totalSize; y++){
            int z = y - piSize;
            File selectedFileX = itemLists.get(z);
            String selectedFile = selectedFileX.getAbsolutePath();
            selectedFiles[y] = selectedFile;
        }
        imageAdaptor = new ImageAdaptor(cntxt, selectedFiles, imgW);
        gridView.setAdapter(imageAdaptor);
    }

    @SuppressLint("SetTextI18n")
    public void  onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE)
            blackFade.setVisibility(View.GONE);
        else {
            @SuppressLint("InflateParams") View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
            if (blackFade.getChildCount() > 0) {
                blackFade.removeAllViews();
            }
            TextView txter = blockView.findViewById(R.id.txter);
            Button cnclBtn = blockView.findViewById(R.id.cancel);
            Button agreeBtn = blockView.findViewById(R.id.agree);
            txter.setText("Are you sure you want to discard post edit?");
            agreeBtn.setOnClickListener(v -> {
                deleteTempFiles();
                gridView = null;
                postImages.clear();
                socket.emit("disconnected", myId);
                finish();
            });
            cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
            blackFade.addView(blockView);
            blackFade.setVisibility(View.VISIBLE);
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
