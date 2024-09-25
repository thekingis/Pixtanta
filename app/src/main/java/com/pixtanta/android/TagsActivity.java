package com.pixtanta.android;

import android.annotation.SuppressLint;
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
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.pixtanta.android.Utils.HtmlParser;
import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.www;

public class TagsActivity extends ThemeActivity {

    int myId, verifiedIcon;
    Context cntxt;
    DisplayMetrics displayMetrics;
    ProgressBar progressBar;
    LinearLayout linearLayout, blackFade;
    ScrollView scrllVw;
    boolean loadingPost = true, allLoaded = false, firstLoad = true;
    ArrayList<String> selectedDatasPost = new ArrayList<>(), selectedDatasCom = new ArrayList<>(), selectedDatasRep = new ArrayList<>();
    ImageLoader imageLoader;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    boolean shakeOpt;
    SharedPrefMngr sharedPrefMngr;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);

        sharedPrefMngr.initializeSmartLogin();
        myId = sharedPrefMngr.getMyId();
        verifiedIcon = R.drawable.ic_verified_user;
        imageLoader = new ImageLoader(this);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(() -> {
            shakeOpt = sharedPrefMngr.checkShakeOption();
            if(shakeOpt)
                openReportDialog();
        });

        progressBar = findViewById(R.id.progressBar);
        linearLayout = findViewById(R.id.layout);
        blackFade = findViewById(R.id.blackFade);
        scrllVw = findViewById(R.id.scrllView);

        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });

        getTaggedPosts();
        scrllVw.setSmoothScrollingEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrllVw.setOnScrollChangeListener((scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int scrollH = linearLayout.getHeight() - scrllVw.getHeight() - 200;
                if(scrollY > scrollH && !loadingPost && !allLoaded){
                    loadingPost = true;
                    progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.image_loader, null);
                    linearLayout.addView(progressBar);
                    progressBar.post(this::getTaggedPosts);
                }
            });
        }
    }

    private void getTaggedPosts() {
        loadingPost = true;
        while (loadingPost) {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(myId))
                    .addFormDataPart("selectedDatasPost", selectedDatasPost.toString().intern())
                    .addFormDataPart("selectedDatasCom", selectedDatasCom.toString().intern())
                    .addFormDataPart("selectedDatasRep", selectedDatasRep.toString().intern())
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.tagsUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try (Response response = call.execute()) {
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    loadingPost = false;
                    progressBar.setVisibility(View.GONE);
                    JSONArray responseArr = new JSONArray(responseString);
                    allLoaded = responseArr.getBoolean(0);
                    JSONArray dataArr = responseArr.getJSONArray(1);
                    if(firstLoad && allLoaded && dataArr.length() == 0){
                        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                        int color;
                        if(darkThemeEnabled)
                            color = R.color.white;
                        else
                            color = R.color.black;
                        String text = "You have no tags";
                        TextView newTextView = new TextView(cntxt);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.topMargin = 300;
                        newTextView.setPadding(0, 15, 0, 15);
                        newTextView.setLayoutParams(params);
                        newTextView.setTextColor(ContextCompat.getColor(cntxt, color));
                        newTextView.setText(text);
                        newTextView.setTextSize(18f);
                        newTextView.setGravity(Gravity.CENTER);
                        linearLayout.addView(newTextView);
                    } else if(dataArr.length() > 0) {
                        for (int p = 0; p < dataArr.length(); p++) {
                            JSONObject postObj = dataArr.getJSONObject(p);
                            String id = postObj.getString("id");
                            String tab = postObj.getString("tab");
                            String postId = postObj.getString("postId");
                            String comId = postObj.getString("comId");
                            String repId = postObj.getString("repId");
                            String photo = postObj.getString("photo");
                            String name = postObj.getString("name");
                            String userName = postObj.getString("userName");
                            String type = postObj.getString("type");
                            String text = postObj.getString("text");
                            String files = postObj.getString("files");
                            String date = postObj.getString("date");
                            int fromId = postObj.getInt("fromId");
                            boolean verified = postObj.getBoolean("verified");
                            String viewText = "View " + tab;
                            @SuppressLint("InflateParams") RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.tags_layout, null);
                            ImageView imageView = relativeLayout.findViewById(R.id.posterPht);
                            TextView nameTextVw = relativeLayout.findViewById(R.id.posterName);
                            TextView nameUTextVw = relativeLayout.findViewById(R.id.posterUName);
                            TextView textTextVw = relativeLayout.findViewById(R.id.postText);
                            TextView filesTextVw = relativeLayout.findViewById(R.id.files);
                            TextView dateTextVw = relativeLayout.findViewById(R.id.postDate);
                            TextView clickTextVw = relativeLayout.findViewById(R.id.clickView);
                            switch (tab){
                                case "Post":
                                    selectedDatasPost.add(id);
                                    break;
                                case "Comment":
                                    selectedDatasCom.add(id);
                                    break;
                                case "Reply":
                                    selectedDatasRep.add(id);
                                    break;
                            }
                            photo = www + photo;
                            imageLoader.displayImage(photo, imageView);
                            nameTextVw.setText(name);
                            if(verified)
                                nameTextVw.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                            if(StringUtils.isEmpty(userName))
                                nameUTextVw.setVisibility(View.GONE);
                            else {
                                userName = "@" + userName;
                                nameUTextVw.setText(userName);
                            }
                            String htmlText = HtmlParser.parseSpan(text);
                            CharSequence sequence = Html.fromHtml(htmlText);
                            SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                            URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                            for(URLSpan span : urls) {
                                makeLinkClickable(strBuilder, span);
                            }
                            textTextVw.setText(strBuilder);
                            textTextVw.setMovementMethod(LinkMovementMethod.getInstance());
                            filesTextVw.setText(files);
                            dateTextVw.setText(date);
                            clickTextVw.setText(viewText);
                            nameTextVw.setOnClickListener(v -> {
                                switch (type){
                                    case "page":
                                        visitPage(fromId);
                                        break;
                                    case "profile":
                                        visitUserProfile(fromId);
                                        break;
                                }
                            });
                            clickTextVw.setOnClickListener(v -> openTaggedPage(tab, postId, comId, repId));
                            linearLayout.addView(relativeLayout);
                        }
                    }
                    firstLoad = false;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void openTaggedPage(String key, String postId, String comId, String repId){
        Intent intent;
        Bundle bundle = new Bundle();
        switch (key){
            case "Post":
                intent = new Intent(cntxt, PostDisplayAct.class);
                bundle.putString("postId", postId);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case "Comment":
                intent = new Intent(cntxt, CommentsActivity.class);
                bundle.putString("postID", postId);
                bundle.putString("commentID", comId);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case "Reply":
                intent = new Intent(cntxt, RepliesActivity.class);
                bundle.putString("postID", postId);
                bundle.putString("comID", comId);
                bundle.putString("replyID", repId);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
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

    private void makeLinkClickable(SpannableStringBuilder strBuilder, URLSpan span) {
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
                        visitUserProfile(Integer.parseInt(dataId));
                    if (dataType.equals("Page"))
                        visitPage(Integer.parseInt(dataId));
                    if (dataType.equals("search"))
                        visitSearch(dataId);
                } else
                    openLink(href);
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    private void visitUserProfile(int user) {
        Intent intent = new Intent(cntxt, ProfileAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("userID", user);
        intent.putExtras(userParams);
        startActivity(intent);
    }

    private void visitPage(int pageId) {
        Intent intent = new Intent(cntxt, PageActivity.class);
        Bundle pageParams = new Bundle();
        pageParams.putInt("pageId", pageId);
        intent.putExtras(pageParams);
        startActivity(intent);
    }

    private void visitSearch(String word) {
        Intent intent = new Intent(cntxt, SearchActivity.class);
        Bundle pageParams = new Bundle();
        pageParams.putString("word", word);
        pageParams.putInt("position", 3);
        intent.putExtras(pageParams);
        startActivity(intent);
    }

    private void openLink(String linkUrl) {
        Uri uri = Uri.parse(linkUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        startActivity(intent);
    }

    public  void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
            blackFade.removeAllViews();
        } else finish();
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