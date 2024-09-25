package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
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

import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.socketUrl;

public class DeletedpostsAct extends ThemeActivity {

    int myId, verifiedIcon;
    Context cntxt;
    DisplayMetrics displayMetrics;
    static Socket socket;
    ProgressBar progressBar;
    LinearLayout linearLayout, blackFade;
    ScrollView scrllVw;
    boolean loadingPost = true, allLoaded = false, firstLoad = true, shakeOpt;
    ArrayList<String> selectedDatas = new ArrayList<>();
    ImageLoader imageLoader;
    String activePostId;
    JSONObject optionsIcons = new JSONObject(), linearLayouts = new JSONObject();
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    SharedPrefMngr sharedPrefMngr;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deletedposts);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);

        sharedPrefMngr.initializeSmartLogin();
        myId = sharedPrefMngr.getMyId();
        verifiedIcon = R.drawable.ic_verified_user;
        imageLoader = new ImageLoader(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(() -> {
            shakeOpt = sharedPrefMngr.checkShakeOption();
            if(shakeOpt)
                openReportDialog();
        });

        try {
            socket = IO.socket(socketUrl);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> socket.emit("connected", myId)));
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        progressBar = findViewById(R.id.progressBar);
        linearLayout = findViewById(R.id.layout);
        blackFade = findViewById(R.id.blackFade);
        scrllVw = findViewById(R.id.scrllView);

        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });

        try {
            optionsIcons.put("viewPost", R.drawable.ic_article);
            optionsIcons.put("undeletePost", R.drawable.ic_delete);
            optionsIcons.put("deletePost", R.drawable.ic_undelete);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getDeletedPosts();
        scrllVw.setSmoothScrollingEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrllVw.setOnScrollChangeListener((scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int scrollH = linearLayout.getHeight() - scrllVw.getHeight() - 200;
                if(scrollY > scrollH && !loadingPost && !allLoaded){
                    loadingPost = true;
                    progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.image_loader, null);
                    linearLayout.addView(progressBar);
                    progressBar.post(this::getDeletedPosts);
                }
            });
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
        socket.on("addToFriend", args -> {
            String theUser = args[0].toString();
            socket.emit("addToFriend", theUser);
        });
        socket.on("removeFriend", args -> {
            String theUser = args[0].toString();
            socket.emit("removeFriend", theUser);
        });

    }

    @SuppressLint("SetTextI18n")
    private void getDeletedPosts(){
        loadingPost = true;
        while (loadingPost) {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(myId))
                    .addFormDataPart("selectedDatas", selectedDatas.toString().intern())
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.deletedPoststUrl)
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
                    JSONArray dataArr = new JSONArray(responseArr.getString(1));
                    if(firstLoad) {
                        firstLoad = false;
                        if(dataArr.length() == 0) {
                            boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                            int textColor, bgColor;
                            if(darkThemeEnabled) {
                                textColor = R.color.white;
                                bgColor = R.color.black;
                            } else {
                                textColor = R.color.black;
                                bgColor = R.color.white;
                            }
                            String text = "You have no Deleted Post";
                            TextView newTextView = new TextView(cntxt);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.topMargin = 300;
                            newTextView.setPadding(0, 15, 0, 15);
                            newTextView.setLayoutParams(params);
                            newTextView.setTextColor(ContextCompat.getColor(cntxt, textColor));
                            newTextView.setText(text);
                            newTextView.setTextSize(18f);
                            newTextView.setGravity(Gravity.CENTER);
                            scrllVw.setBackgroundColor(ContextCompat.getColor(cntxt, bgColor));
                            linearLayout.addView(newTextView);
                        }
                    }
                    if(dataArr.length() > 0) {
                        for (int p = 0; p < dataArr.length(); p++) {
                            JSONObject postObj = new JSONObject(dataArr.getString(p));
                            String postId = postObj.getString("postId");
                            String name = postObj.getString("name");
                            String postText = postObj.getString("postText");
                            String files = postObj.getString("files");
                            String fileUrl = Constants.www + postObj.getString("photo");
                            boolean verified = postObj.getBoolean("verified");
                            @SuppressLint("InflateParams") LinearLayout postView = (LinearLayout) getLayoutInflater().inflate(R.layout.saved_posts, null);
                            ImageView imageView = postView.findViewById(R.id.imgView);
                            TextView textName = postView.findViewById(R.id.name);
                            TextView text = postView.findViewById(R.id.text);
                            TextView media = postView.findViewById(R.id.media);
                            linearLayouts.put(postId, postView);
                            selectedDatas.add(postId);
                            textName.setText(name);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                text.setText(Html.fromHtml(postText, Html.FROM_HTML_MODE_COMPACT));
                            else
                                text.setText(Html.fromHtml(postText));
                            if (verified)
                                textName.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                            if (StringUtils.isEmpty(files))
                                media.setVisibility(View.GONE);
                            else {
                                if (StringUtils.isEmpty(postText))
                                    text.setVisibility(View.GONE);
                                JSONArray mediaFiles = new JSONArray(files);
                                int numFiles = mediaFiles.length();
                                String m = " media file";
                                if (numFiles > 1)
                                    m += "s";
                                media.setText(numFiles + m);
                                fileUrl = Constants.www + mediaFiles.getString(0);
                            }
                            String fileType = Functions.checkFileType(fileUrl.toLowerCase());
                            
                            Bitmap bitmap = Functions.getBitmapFromSource(fileUrl, fileType, true);
                            imageView.setImageBitmap(bitmap);
                            postView.setOnClickListener(v -> openPostPage(postId));
                            postView.setOnClickListener(v -> {
                                activePostId = postId;
                                try {
                                    displayOptions();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                            linearLayout.addView(postView);
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("InflateParams")
    public void displayOptions() throws JSONException {
        RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
        LinearLayout optLayer = postOptView.findViewById(R.id.optLayer);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        JSONObject optionsObj = new JSONObject();
        optionsObj.put("viewPost", "View Post");
        optionsObj.put("undeletePost", "Undelete Post");
        optionsObj.put("deletePost", "Delete Post Permanently");
        JSONArray objKeys = optionsObj.names();
        for (int r = 0; r < Objects.requireNonNull(objKeys).length(); r++){
            TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
            String key = objKeys.getString(r);
            String option = optionsObj.getString(key);
            int drawableLeft = optionsIcons.getInt(key);
            optionList.setText(option);
            optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
            optionList.setOnClickListener(v -> {
                try {
                    executeOption(key);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            optLayer.addView(optionList);
        }
        blackFade.addView(postOptView);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void executeOption(String key) throws JSONException {
        blackFade.setVisibility(View.GONE);
        blackFade.removeAllViews();
        switch (key){
            case "viewPost":
                openPostPage(activePostId);
                break;
            case "undeletePost":
                undeletePost();
                break;
            case "deletePost":
                deletePost();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    public void undeletePost() throws JSONException {
        @SuppressLint("InflateParams") View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        JSONObject emitObj = new JSONObject();
        LinearLayout view = (LinearLayout) linearLayouts.get(activePostId);
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        emitObj.put("date", date);
        emitObj.put("user", myId);
        emitObj.put("postId", activePostId);
        TextView txter = blockView.findViewById(R.id.txter);
        Button cnclBtn = blockView.findViewById(R.id.cancel);
        Button agreeBtn = blockView.findViewById(R.id.agree);
        txter.setText("Are you sure you want to undelete this post?");
        agreeBtn.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            runOnUiThread(() -> view.setVisibility(View.GONE));
            socket.emit("undeletePost", emitObj);
        });
        cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
        blackFade.addView(blockView);
        blackFade.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    public void deletePost() throws JSONException {
        @SuppressLint("InflateParams") View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        JSONObject emitObj = new JSONObject();
        LinearLayout view = (LinearLayout) linearLayouts.get(activePostId);
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        emitObj.put("date", date);
        emitObj.put("user", myId);
        emitObj.put("postId", activePostId);
        emitObj.put("val", true);
        TextView txter = blockView.findViewById(R.id.txter);
        Button cnclBtn = blockView.findViewById(R.id.cancel);
        Button agreeBtn = blockView.findViewById(R.id.agree);
        txter.setText("Are you sure you want to delete this post permanently?");
        agreeBtn.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            runOnUiThread(() -> view.setVisibility(View.GONE));
            socket.emit("deletePostP", emitObj);
        });
        cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
        blackFade.addView(blockView);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void openPostPage(String postId) {
        Intent intent = new Intent(cntxt, PostDisplayAct.class);
        Bundle userParams = new Bundle();
        userParams.putString("postId", postId);
        intent.putExtras(userParams);
        cntxt.startActivity(intent);
    }

    public  void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            activePostId = null;
            blackFade.setVisibility(View.GONE);
            blackFade.removeAllViews();
        } else {
            socket.emit("disconnected", myId);
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