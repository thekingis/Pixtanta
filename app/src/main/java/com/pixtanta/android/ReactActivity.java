package com.pixtanta.android;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReactActivity extends ThemeActivity {

    Context cntxt;
    LinearLayout reactLayout, loadingLayer;
    TextView reactNum;
    String dataId, dataType, likes;
    boolean loading = false, allLoaded;
    ArrayList<String> selectedDatas = new ArrayList<>();
    ImageLoader imageLoader;
    ScrollView scrllView;
    int likeNum, myId;
    SharedPrefMngr sharedPrefMngr;

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_react);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);
		
        imageLoader = new ImageLoader(this);
        myId = sharedPrefMngr.getMyId();

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(this, LoginAct.class));
            return;
        }

        Bundle userParams = getIntent().getExtras();
        dataId = userParams.getString("dataId");
        dataType = userParams.getString("dataType");
        likeNum = userParams.getInt("likeNum");
        likes = Functions.convertToText(likeNum);

        reactLayout =  findViewById(R.id.reactLayout);
        scrllView =  findViewById(R.id.scrllView);
        reactNum =  findViewById(R.id.reactNum);
        loadingLayer =  findViewById(R.id.loadingLayer);
        reactNum.setText("Reactions ("+likes+")");

        getContents();

        scrllView.setSmoothScrollingEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrllView.setOnScrollChangeListener((scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int scrollH = reactLayout.getHeight() - scrllVw.getHeight() - 200;
                if(scrollY > scrollH && !loading && !allLoaded){
                    loading = true;
                    loadingLayer = (LinearLayout) getLayoutInflater().inflate(R.layout.users_loader, null, false);
                    reactLayout.addView(loadingLayer);
                    ViewTreeObserver viewTreeObserver = loadingLayer.getViewTreeObserver();
                    viewTreeObserver.addOnGlobalLayoutListener(this::getContents);
                }
            });
        }

    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void getContents() {
        loading = true;
        while (loading) {
            MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("dataId", dataId)
                    .addFormDataPart("selectedDatas", selectedDatas.toString().intern())
                    .addFormDataPart("dataType", dataType)
                    .addFormDataPart("myId", String.valueOf(myId));
            RequestBody requestBody = multipartBody.build();
            Request request = new Request.Builder()
                    .url(Constants.reactstUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    loading = false;
                    String responseString = Objects.requireNonNull(response.body()).string();
                    loadingLayer.setVisibility(View.GONE);
                    JSONArray jsonArray = new JSONArray(responseString);
                    allLoaded = jsonArray.getBoolean(0);
                    likeNum = jsonArray.getInt(1);
                    String data = jsonArray.getString(2);
                    likes = Functions.convertToText(likeNum);
                    reactNum.setText("Reactions (" + likes + ")");
                    JSONArray dataArray = new JSONArray(data);
                    for (int i = 0; i < dataArray.length(); i++) {
                        String dataStr = dataArray.getString(i);
                        JSONObject dataObj = new JSONObject(dataStr);
                        String id = dataObj.getString("id");
                        int userId = dataObj.getInt("user");
                        String name = dataObj.getString("name");
                        String userName = dataObj.getString("userName");
                        String photo = dataObj.getString("photo");
                        boolean verified = dataObj.getBoolean("verified");
                        selectedDatas.add(id);
                        userName = "@" + userName;
                        photo = Constants.www + photo;
                        LinearLayout listView = (LinearLayout) getLayoutInflater().inflate(R.layout.show_users, null);
                        ImageView imageView =  listView.findViewById(R.id.photo);
                        TextView nameTV =  listView.findViewById(R.id.name);
                        TextView userNameTV =  listView.findViewById(R.id.userName);
                        imageLoader.displayImage(photo, imageView);
                        nameTV.setText(name);
                        userNameTV.setText(userName);
                        if(verified)
                            nameTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user, 0);
                        reactLayout.addView(listView);
                        listView.setOnClickListener(v -> visitUserProfile(userId));
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void visitUserProfile(int user) {
        Intent intent = new Intent(cntxt, ProfileAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("userID", user);
        intent.putExtras(userParams);
        startActivity(intent);
    }
}