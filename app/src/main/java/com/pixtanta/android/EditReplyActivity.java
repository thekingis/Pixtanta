package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pixtanta.android.Utils.EditableAccommodatingLatinIMETypeNullIssues;
import com.pixtanta.android.Utils.HtmlParser;
import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URISyntaxException;
import java.util.regex.Pattern;

import io.socket.client.IO;
import io.socket.client.Socket;

import static com.pixtanta.android.Constants.socketUrl;
import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.RepliesActivity.saveEditionR;

public class EditReplyActivity extends ThemeActivity {

    Context cntxt;
    String lang, commentID, myIDtoString, comment;
    RelativeLayout mainHomeView;
    ScrollView scrllView;
    LinearLayout mentionLayer, discardLayout;
    @SuppressLint("StaticFieldLeak")
    static EditText commentBox;
    Button saveBtn, confirmDiscard, cancelDiscard;
    int myId, mCount = 0;
    boolean hashed = false;
    ImageLoader imageLoader;
    static Socket socket;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reply);
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);

        lang = sharedPrefMngr.getSelectedLanguage();
        mainHomeView = findViewById(R.id.mainHomeView);
        setupUI(mainHomeView);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(EditReplyActivity.this, LoginAct.class));
            return;
        }

        myId = sharedPrefMngr.getMyId();
        myIDtoString = String.valueOf(myId);
        Bundle userParams = getIntent().getExtras();
        comment = userParams.getString("curTxt");
        commentID = userParams.getString("comID");

        try {
            socket = IO.socket(socketUrl);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> socket.emit("connected", myId)));
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        imageLoader = new ImageLoader(this);
        scrllView = findViewById(R.id.scrllView);
        mentionLayer = findViewById(R.id.mentionLayer);
        discardLayout = findViewById(R.id.discardLayout);
        commentBox = findViewById(R.id.commentBox);
        saveBtn = findViewById(R.id.saveBtn);
        confirmDiscard = findViewById(R.id.confirmDiscard);
        cancelDiscard = findViewById(R.id.cancelDiscard);

        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        comment = HtmlParser.parseTheme(comment, darkThemeEnabled);
        commentBox.setText(Html.fromHtml(comment));
        saveBtn.setOnClickListener(v -> saveEdit());
        confirmDiscard.setOnClickListener(v -> finish());
        cancelDiscard.setOnClickListener(v -> discardLayout.setVisibility(View.GONE));
        commentBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String allText = s.toString();
                    int caretPos = commentBox.getSelectionStart();
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
                            changeWordToHashTag(commentBox, lastWord);
                            return;
                        }
                        String htmlText = Html.toHtml((Spanned) commentBox.getText().subSequence(caretPos - lastWordLen, caretPos));
                        Document doc = Jsoup.parse(htmlText, "UTF-8");
                        Elements anchorElement = doc.getElementsByTag("a");
                        String href = anchorElement.attr("href");
                        if(href.length() == 0) {
                            JSONObject emitObj = new JSONObject();
                            emitObj.put("user", myId);
                            emitObj.put("advanced", true);
                            emitObj.put("table", "replies");
                            emitObj.put("dataId", commentID);
                            emitObj.put("lastWord", lastWord);
                            socket.emit("checkMention", emitObj);
                            return;
                        }
                    } else {
                        if(lastWordLen == 1){
                            if(lastWord.equals("@")) {
                                JSONObject emitObj = new JSONObject();
                                emitObj.put("user", myId);
                                emitObj.put("advanced", true);
                                emitObj.put("table", "replies");
                                emitObj.put("dataId", commentID);
                                emitObj.put("lastWord", "");
                                socket.emit("checkMention", emitObj);
                                return;
                            }
                            if(lastWord.equals("#")) {
                                String htmlText = Html.toHtml((Spanned) commentBox.getText().subSequence(caretPos - 1 , caretPos));
                                htmlText = htmlText.replace("<p dir=\"ltr\">", "");
                                htmlText = htmlText.replace("</p>", "");
                                String startStr = "<b><span style=\"background-color:#F9F9F9;\">";
                                if(htmlText.startsWith(startStr)) {
                                    commentBox.setText(commentBox.getText().delete(caretPos - 1, caretPos));
                                    commentBox.getText().insert(caretPos - 1, "#");
                                    commentBox.setSelection(caretPos);
                                }
                                return;
                            }
                        }
                    }
                    scrllView.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        commentBox.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                try {
                    if(event.getUnicodeChar() == (int) EditableAccommodatingLatinIMETypeNullIssues.ONE_UNPROCESSED_CHARACTER.charAt(0)){
                        return true;
                    } else if(keyCode == KeyEvent.KEYCODE_DEL) {
                        return overrideBackspace(commentBox);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
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
                        lash = "Commentor";
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
            
            int newCaretPos = indexStart + newAnchorText.length();
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

    private void saveEdit() {
        try {
            String comTxt = commentBox.getText().toString();
            if(!StringUtils.isEmpty(comTxt)) {
                comTxt = Html.toHtml(commentBox.getText());
                saveEditionR(cntxt, comTxt, commentID);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        if(scrllView.getVisibility() == View.VISIBLE){
            scrllView.setVisibility(View.GONE);
        } else if(discardLayout.getVisibility() == View.VISIBLE){
            discardLayout.setVisibility(View.GONE);
        } else if(discardLayout.getVisibility() == View.GONE || discardLayout.getVisibility() == View.INVISIBLE){
            discardLayout.setVisibility(View.VISIBLE);
        } else {
            socket.emit("disconnected", myId);
            finish();
        }
    }
}
