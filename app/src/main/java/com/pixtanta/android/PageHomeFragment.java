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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.pixtanta.android.Utils.BlurBitmap;
import com.pixtanta.android.Utils.ContextData;
import com.pixtanta.android.Utils.EditableAccommodatingLatinIMETypeNullIssues;
import com.pixtanta.android.Utils.HtmlParser;
import com.pixtanta.android.Utils.StaticSaver;
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
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Pattern;

import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.HomeAct.showReactors;
import static com.pixtanta.android.HomeAct.userPages;

public class PageHomeFragment extends Fragment {

    LinearLayout postShow, comMentionLayer, blackFade, viewHolder, loadingLayer;
    String lang, myName, myUserName, myPht, poster, postType, posterImg, posterName, posterUName, myIDtoString, pageIDtoString, photo, pageName;
    int[][] reactions;
    ArrayList<String> selectedPosts = new ArrayList<>();
    static ArrayList<String> postFilesLists;
    NestedScrollView postScrllVw, nest;
    ScrollView comScrllView;
    RelativeLayout mainHomeView;
    JSONObject postsOptions = new JSONObject(), commentsLayoutObj = new JSONObject(), reactorView = new JSONObject(), postsOptionsVal = new JSONObject(), postsOptionsIcons, likesDisplay = new JSONObject(), comsDisplay = new JSONObject(), miniComDisplay = new JSONObject(), onPagerScrolled = new JSONObject();
    ArrayList<LinearLayout> miniComLayouts = new ArrayList<>();
    static private int myId;
    static JSONObject postLayouts;
    EditText curEditor;
    static int[] numberOfImages;
    int verifiedIcon, mCount = 0, count = 0, pageId, viewPagerHeight, height, pageCount;
    Context cntxt;
    static Socket socket;
    ImageLoader imageLoader;
    boolean userVerified, loadingPost = false, allLoaded = false, hashed = false, aBoolean, scrllVIState;
    @SuppressLint("StaticFieldLeak")
    static PhotoAdapter photoAdapter;
    ClipboardManager clipboard;
    SharedPrefMngr sharedPrefMngr;
    static Handler UIHandler;

    static {
        UIHandler = new Handler(Looper.getMainLooper());
    }

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public PageHomeFragment(ContextData contextData) {
        // Required empty public constructor
        cntxt = contextData.context;
        pageId = contextData.dataId;
        height = contextData.height;
        viewPagerHeight = contextData.width;
        blackFade = (LinearLayout) contextData.blackFade;
        comMentionLayer = (LinearLayout) contextData.comMentionLayer;
        viewHolder = (LinearLayout) contextData.viewHolder;
        mainHomeView = (RelativeLayout) contextData.mainHomeView;
        comScrllView = (ScrollView) contextData.comScrllView;
        nest = (NestedScrollView) contextData.nest;
        aBoolean = contextData.aBoolean;
        photo = contextData.photo;
        pageName = contextData.pageName;
        socket = contextData.socket;
        pageCount = contextData.pageCount;
        sharedPrefMngr = new SharedPrefMngr(cntxt);
    }

    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        lang = sharedPrefMngr.getSelectedLanguage();
        imageLoader = new ImageLoader(cntxt);
        verifiedIcon = R.drawable.ic_verified_user;
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        myId = sharedPrefMngr.getMyId();
        myPht = Constants.www + sharedPrefMngr.getMyPht();
        myName = sharedPrefMngr.getMyName();
        myUserName = sharedPrefMngr.getMyUserName();
        userVerified = sharedPrefMngr.getMyVerification();
        sharedPrefMngr.storeUserVerification(myId, userVerified);
        clipboard = (ClipboardManager) cntxt.getSystemService(Context.CLIPBOARD_SERVICE);
        myIDtoString = Integer.toString(myId);
        pageIDtoString = Integer.toString(pageId);
        postLayouts = new JSONObject();
        scrllVIState = false;

        postsOptionsIcons = new JSONObject();
        try {
            postsOptionsIcons.put("savePost", R.drawable.ic_save_post);
            postsOptionsIcons.put("editPost", R.drawable.ic_edit_post);
            postsOptionsIcons.put("deletePost", R.drawable.ic_delete_post);
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

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_page_home, container, false);
        postScrllVw =  view.findViewById(R.id.postScrllVw);
        postShow =  view.findViewById(R.id.postShow);
        loadingLayer =  view.findViewById(R.id.loadingLayer);
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

        new android.os.Handler().postDelayed(this::getPostDisplay, 500);

        postScrllVw.setSmoothScrollingEnabled(true);
        nest.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int gH = viewHolder.getHeight() - nest.getHeight();
            aBoolean = gH == scrollY;
        });

        postScrllVw.setOnScrollChangeListener((View.OnScrollChangeListener) (scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if(!aBoolean){
                scrllVw.scrollTo(0, 0);
                int nScrllTop = nest.getScrollY();
                int scrllTop = nScrllTop + scrollY;
                nest.scrollTo(0, scrllTop);
                return;
            }
            int scrollH = postShow.getHeight() - scrllVw.getHeight() - height;
            if(scrollY > scrollH && !loadingPost && !allLoaded){
                loadingPost = true;
                loadingLayer = (LinearLayout) getLayoutInflater().inflate(R.layout.loading_layer, null, false);
                postShow.addView(loadingLayer);
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
                    runOnUI(() -> {
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
                    runOnUI(() -> {
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
                        runOnUI(() -> {
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
                    TextView textView =  linearLayout.findViewById(R.id.commentView);
                    String newCom = String.valueOf(argsArr.getString(1));
                    String comUserName = String.valueOf(argsArr.getString(2));
                    newCom = EmojiParser.parseToUnicode(newCom);
                    newCom = HtmlParser.parseBreaks(newCom);
                    String htmlText = "<font><b>@"+comUserName+"</b> "+newCom+"</font>";
                    runOnUI(() -> textView.setText(Html.fromHtml(htmlText)));
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
                    LinearLayout miniComLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.mini_comments, null);
                    ImageView imgView =  miniComLayout.findViewById(R.id.profPic);
                    TextView commentTxtVw =  miniComLayout.findViewById(R.id.commentView);
                    imageLoader.displayImage(userPht, imgView);
                    String htmlText = "<font><b>@"+userUName+"</b> "+miniCom+"</font>";
                    commentTxtVw.setText(Html.fromHtml(htmlText));
                    LinearLayout commentsLayout = (LinearLayout) commentsLayoutObj.get(postId);
                    miniComDisplay.put(commentID, miniComLayout);
                    int finalLikeNum = likeNum;
                    runOnUI(() -> {
                        commentsLayout.addView(miniComLayout);
                        textView.setText(Functions.convertToText(finalLikeNum));
                        miniComLayout.setOnClickListener(v -> openComments(postId, commentID));
                    });
                }
                setupUI(cntxt, mainHomeView);
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
                    LinearLayout miniComLayout = (LinearLayout) miniComLayouts.get(counter);
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
                    runOnUI(() -> {
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
                    runOnUI(() -> relativeLayout.setVisibility(View.GONE));
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
                    runOnUI(() -> relativeLayout.setVisibility(View.GONE));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("mentionList", args -> {
            try {
                JSONArray listArray = new JSONArray(args[0].toString());
                if(listArray.length() == 0){
                    runOnUI(() -> comScrllView.setVisibility(View.GONE));
                    return;
                }
                runOnUI(() -> {
                    if(comMentionLayer.getChildCount() > 0)
                        comMentionLayer.removeAllViews();
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
                    LinearLayout listView = (LinearLayout) getLayoutInflater().inflate(R.layout.show_users, null);
                    ImageView imageView =  listView.findViewById(R.id.photo);
                    TextView nameTV =  listView.findViewById(R.id.name);
                    TextView userNameTV =  listView.findViewById(R.id.userName);
                    imageLoader.displayImage(photo, imageView);
                    nameTV.setText(name);
                    userNameTV.setText(lash);
                    if(verified)
                        nameTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user, 0);
                    String finalName = name;
                    listView.setOnClickListener(v -> addTextToView(curEditor, finalName, tab, id));
                    runOnUI(() -> comMentionLayer.addView(listView));
                }
                runOnUI(() -> comScrllView.setVisibility(View.VISIBLE));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return view;
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
        comScrllView.setVisibility(View.GONE);
    }

    private void getPostDisplay(){
        loadingPost = true;
        try {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("lang", lang)
                    .addFormDataPart("pageId", String.valueOf(pageId))
                    .addFormDataPart("myId", String.valueOf(myId))
                    .addFormDataPart("selectedPosts", selectedPosts.toString().intern())
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.pageDisplayUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                loadingPost = false;
                postShow.removeView(loadingLayer);
                String responseString = Objects.requireNonNull(response.body()).string();
                JSONArray responseArr = new JSONArray(responseString);
                allLoaded = responseArr.getBoolean(0);
                JSONArray postsArr = new JSONArray(responseArr.getString(1));
                poster = String.valueOf(myId);
                postType = "profile";
                posterImg = myPht;
                posterName = myName;
                posterUName = myUserName;
                setupUI(cntxt, mainHomeView);
                poster = String.valueOf(myId);
                postType = "profile";
                posterImg = myPht;
                posterName = myName;
                posterUName = myUserName;
                for (int p = 0; p < postsArr.length(); p++) {
                    JSONObject postObj = new JSONObject(postsArr.getString(p));
                    String postID = postObj.getString("id");
                    String postText = postObj.getString("post");
                    String files = postObj.getString("files");
                    String postDate = postObj.getString("date");
                    String postOptions = postObj.getString("options");
                    String postOptionsVal = postObj.getString("optionsVal");
                    String postComments = postObj.getString("comments");
                    String pagerId = postObj.getString("pagerId");
                    boolean hasFiles = postObj.getBoolean("hasFiles");
                    boolean liked = postObj.getBoolean("liked");
                    boolean vary = postObj.getBoolean("vary");
                    int likeNum = postObj.getInt("likeNum");
                    int comNum = postObj.getInt("comNum");
                    JSONObject object = new JSONObject(postOptionsVal);
                    postsOptions.put(postID, postOptions);
                    postsOptionsVal.put(postID, object);
                    selectedPosts.add(postID);
                    @SuppressLint("InflateParams") RelativeLayout postView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post, null);
                    LinearLayout postTextLayout =  postView.findViewById(R.id.postTextLayout);
                    LinearLayout postFilesLayout =  postView.findViewById(R.id.postFilesLayout);
                    LinearLayout commentsLayout =  postView.findViewById(R.id.commentsLayout);
                    ImageView userImageView =  postView.findViewById(R.id.posterPht);
                    ImageView myComPht =  postView.findViewById(R.id.myComPht);
                    ImageButton postOpt =  postView.findViewById(R.id.postOpt);
                    ImageButton likePost =  postView.findViewById(R.id.likePost);
                    ImageButton comments =  postView.findViewById(R.id.comments);
                    TextView pstText =  postView.findViewById(R.id.postText);
                    TextView nameTxtVw =  postView.findViewById(R.id.posterName);
                    TextView postDateVw =  postView.findViewById(R.id.postDate);
                    TextView comNumTV =  postView.findViewById(R.id.comNum);
                    TextView likeNumTV =  postView.findViewById(R.id.likeNum);
                    TextView likers =  postView.findViewById(R.id.likers);
                    ImageButton submitCommentBtn =  postView.findViewById(R.id.submitCommentBtn);
                    EditText commentBox =  postView.findViewById(R.id.commentBox);
                    initializeEditText(commentBox, postID);
                    StaticSaver.saveObject(pageCount, postLayouts);
                    ViewPager viewPager =  postView.findViewById(R.id.viewPager);
                    postLayouts.put(postID, postView);
                    likesDisplay.put(postID, likeNumTV);
                    comsDisplay.put(postID, comNumTV);
                    commentsLayoutObj.put(postID, commentsLayout);
                    reactorView.put(postID, likers);
                    imageLoader.displayImage(photo, userImageView);
                    imageLoader.displayImage(myPht, myComPht);
                    String htmlText = HtmlParser.parseSpan(postText);
                    CharSequence sequence = Html.fromHtml(htmlText);
                    SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                    URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                    for(URLSpan span : urls) {
                        makeLinkClickable(cntxt, strBuilder, span);
                    }
                    pstText.setText(strBuilder);
                    pstText.setMovementMethod(LinkMovementMethod.getInstance());
                    nameTxtVw.setText(pageName);
                    postDateVw.setText(postDate);
                    if (StringUtils.isEmpty(postText))
                        postTextLayout.setVisibility(View.GONE);
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
                    if(hasFiles) {
                        onPagerScrolled.put(postID, false);
                        JSONArray postFiles = new JSONArray(files);
                        postFilesLists = new ArrayList<>();
                        int numOfImgs = postFiles.length();
                        if (postFiles.length() > 9)
                            numOfImgs = 0;
                        int numDis = numberOfImages[numOfImgs];
                        View imgNum =  postView.findViewById(R.id.imgNum);
                        imgNum.setBackgroundResource(numDis);
                        for (int h = 0; h < postFiles.length(); h++) {
                            String filePath = Constants.www + postFiles.getString(h);
                            postFilesLists.add(filePath);
                        }
                        photoAdapter = new PhotoAdapter(postFilesLists, cntxt);
                        ViewGroup.LayoutParams params = postFilesLayout.getLayoutParams();
                        params.height = viewPagerHeight;
                        viewPager.setLayoutParams(params);
                        viewPager.setOffscreenPageLimit(postFiles.length());
                        viewPager.setAdapter(photoAdapter);
                        viewPager.setPageTransformer(true, new PageTransformer());
                        viewPager.post(() -> {
                            View view = viewPager.getChildAt(0);
                            ImageView imageView =  view.findViewById(R.id.image);
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
                    postShow.addView(postView);
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
                        String comPhoto = Constants.www + commentsObj.getString("photo");
                        String miniCom = commentsObj.getString("comment");
                        @SuppressLint("InflateParams") LinearLayout miniComLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.mini_comments, null);
                        miniComDisplay.put(commentID, miniComLayout);
                        ImageView imgView =  miniComLayout.findViewById(R.id.profPic);
                        TextView commentTxtVw =  miniComLayout.findViewById(R.id.commentView);
                        imageLoader.displayImage(comPhoto, imgView);
                        miniCom = HtmlParser.parseBreaks(miniCom);
                        miniCom = EmojiParser.parseToUnicode(miniCom);
                        String htmlTextCom = "<font><b>@"+comUserName+"</b> "+miniCom+"</font>";
                        commentTxtVw.setText(Html.fromHtml(htmlTextCom));
                        miniComLayout.setOnClickListener(v -> openComments(postID, commentID));
                        commentsLayout.addView(miniComLayout);
                    }
                    socket.emit("postScratch", postID);
                }
                setupUI(cntxt, mainHomeView);
            }

        } catch (Exception e) {
            e.printStackTrace();
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

    @SuppressLint("InflateParams")
    public static void savePostEdit(Context cntxt, ArrayList<String> originalImages, ArrayList<File> addedImages, ArrayList<String> editedImages, String htmlText, String postId, String type, String pagerId) throws JSONException {
        int totalFilesSize = originalImages.size() + addedImages.size();
        RelativeLayout postView = (RelativeLayout) postLayouts.get(postId);
        RelativeLayout progressView = (RelativeLayout) LayoutInflater.from(cntxt).inflate(R.layout.progress_bar, null, false);
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
            if (bytesRead >= contentLength && contentLength > 0) {
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
                        emitObj.put("pageId", pagerId);
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

    private void reactToPost(int postID, View v) {
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        int tag = Integer.parseInt(v.getTag().toString());
        JSONObject emitObj = new JSONObject();
        try {
            TextView textView = (TextView) likesDisplay.get(String.valueOf(postID));
            String text = textView.getText().toString();
            emitObj.put("user", myId);
            emitObj.put("postId", postID);
            emitObj.put("tag", tag);
            emitObj.put("date", date);
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
                runOnUI(() -> likesTextView.setVisibility(View.VISIBLE));
            } else {
                runOnUI(() -> likesTextView.setVisibility(View.GONE));
            }
            socket.emit("postLike", emitObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initializeEditText(EditText editText, String dataId) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    curEditor = editText;
                    String allText = s.toString();
                    int caretPos = editText.getSelectionStart();
                    String text = allText.substring(0, caretPos);
                    int textLen = text.length(), textL = textLen - 1;
                    if(allText.length() > caretPos){
                        char nextChar = allText.charAt(caretPos);
                        if (!Character.isWhitespace(nextChar)) {
                            comScrllView.setVisibility(View.GONE);
                            return;
                        }
                    }
                    if(textLen > 0) {
                        char lastChar = text.charAt(textL);
                        if (Character.isWhitespace(lastChar)) {
                            comScrllView.setVisibility(View.GONE);
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
                            emitObj.put("dataId", dataId);
                            emitObj.put("lastWord", lastWord);
                            socket.emit("checkMention", emitObj);
                            return;
                        }
                        comScrllView.setVisibility(View.GONE);
                    } else {
                        if(lastWordLen == 1){
                            if(lastWord.equals("@")) {
                                JSONObject emitObj = new JSONObject();
                                emitObj.put("user", myId);
                                emitObj.put("advanced", true);
                                emitObj.put("table", "comments");
                                emitObj.put("dataId", dataId);
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
                        comScrllView.setVisibility(View.GONE);
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
            curEditor = editText;
            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                try {
                    if(event.getUnicodeChar() == (int) EditableAccommodatingLatinIMETypeNullIssues.ONE_UNPROCESSED_CHARACTER.charAt(0)){
                        return true;
                    } else if(keyCode == KeyEvent.KEYCODE_DEL) {
                        return overrideBackspace(editText);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });
    }

    private static void openLink(Context context, String linkUrl) {
        Uri uri = Uri.parse(linkUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        context.startActivity(intent);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void submitComment(EditText commentBox, String postID, LinearLayout commentsLayout, boolean vary, String pagerId) {
        String comment = commentBox.getText().toString();
        if(!StringUtils.isEmpty(comment)){
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
                    tabletR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName, commentBox, postID, commentsLayout));
                    imageViewR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName, commentBox, postID, commentsLayout));
                    txtNameR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName, commentBox, postID, commentsLayout));
                    txtUserNameR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName, commentBox, postID, commentsLayout));
                    layR.setOnClickListener(v -> setCommenter(myIDtoString, "profile", myPht, myName, myUserName, commentBox, postID, commentsLayout));
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
            ImageView imgView =  miniComLayout.findViewById(R.id.profPic);
            TextView commentTxtVw =  miniComLayout.findViewById(R.id.commentView);
            imageLoader.displayImage(posterImg, imgView);
            htmlText = "<font><b>@"+posterUName+"</b> "+miniCom+"</font>";
            commentTxtVw.setText(Html.fromHtml(htmlText));
            commentsLayout.addView(miniComLayout);
            miniComLayouts.add(count, miniComLayout);
            comment = EmojiParser.parseToAliases(comment);
            JSONObject emitObj = new JSONObject();
            setupUI(cntxt, mainHomeView);
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
                count++;
                reset();
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
        Intent intent = new Intent(cntxt, CommentsActivity.class);
        Bundle userParams = new Bundle();
        userParams.putString("postID", postID);
        userParams.putString("commentID", commentID);
        intent.putExtras(userParams);
        startActivity(intent);
    }

    @SuppressLint("InflateParams")
    private void displayPostsOptions(String postID) throws JSONException {
        RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
        LinearLayout optLayer =  postOptView.findViewById(R.id.optLayer);
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

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void executePostOption(String postId, String key) throws JSONException {
        JSONObject optionsValObj, optionsObj, emitObj;
        boolean val, newVal;
        String newTxt, optionArr;
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
                @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                emitObj = new JSONObject();
                emitObj.put("date", date);
                emitObj.put("user", myId);
                emitObj.put("postId", postId);
                emitObj.put("val", val);
                socket.emit("savePost", emitObj);
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
                txter.setText("Are you sure you want to report this post?");
                emitObj = new JSONObject();
                agreeBtn.setOnClickListener(v -> {
                    blackFade.setVisibility(View.GONE);
                    try {
                        RelativeLayout relativeLayout = (RelativeLayout) postLayouts.get(postId);
                        runOnUI(() -> relativeLayout.setVisibility(View.GONE));
                        Toast.makeText(cntxt, "Post Hidden", Toast.LENGTH_LONG).show();
                        @SuppressLint("SimpleDateFormat") String date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        emitObj.put("date", date1);
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
                Intent intent = new Intent(cntxt, EditPostActivity.class);
                Bundle userParams = new Bundle();
                userParams.putString("postId", postId);
                userParams.putInt("reqFrm", 2);
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
                    try {
                        RelativeLayout relativeLayout = (RelativeLayout) postLayouts.get(postId);
                        runOnUI(() -> relativeLayout.setVisibility(View.GONE));
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
                            @SuppressLint("SimpleDateFormat") String date13 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            emitObj.put("date", date13);
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
            default:
                throw new IllegalStateException("Unexpected value: " + key);
        }
    }

    private static void visitUserProfile(Context cntxt, int userx) {
        Intent intent = new Intent(cntxt, ProfileAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("userID", userx);
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

    private void reset() {
        poster = null;
        postType = null;
        posterUName = null;
        posterName = null;
        posterImg = null;
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
}