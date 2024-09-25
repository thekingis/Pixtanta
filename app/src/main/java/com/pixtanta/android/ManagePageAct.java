package com.pixtanta.android;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class ManagePageAct extends ThemeActivity {

    Context cntxt;
    LinearLayout linearLayout;
    ProgressBar progressBar;
    int myId;
    ImageLoader imageLoader;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_page);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(this, LoginAct.class));
            return;
        }
        myId = sharedPrefMngr.getMyId();

        linearLayout = findViewById(R.id.linearLayout);
        progressBar = findViewById(R.id.progressBar);
        imageLoader = new ImageLoader(this);

        loadPages();

    }

    @SuppressLint("InflateParams")
    private void loadPages() {
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("user", String.valueOf(myId))
                .build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(Constants.loadPagesUrl)
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        try(okhttp3.Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                progressBar.setVisibility(View.GONE);
                String responseString = Objects.requireNonNull(response.body()).string();
                JSONArray responseArr = new JSONArray(responseString);
                if(responseArr.length() > 0){
                    for(int i = 0; i < responseArr.length(); i++) {
                        JSONObject object = new JSONObject(responseArr.getString(i));
                        int pageId = object.getInt("pageId");
                        String pagePhoto = Constants.www + object.getString("pagePhoto");
                        String pageName = object.getString("pageName");
                        LinearLayout tablet = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.tablet, null, false);
                        LinearLayout lay = tablet.findViewById(R.id.lay);
                        ImageView imageView = tablet.findViewById(R.id.image);
                        TextView txtName = tablet.findViewById(R.id.name);
                        TextView txtUserName = tablet.findViewById(R.id.userName);
                        txtUserName.setVisibility(View.GONE);
                        imageLoader.displayImage(pagePhoto, imageView);
                        txtName.setText(pageName);
                        linearLayout.addView(tablet);
                        lay.setOnClickListener(v -> openPage(pageId));
                    }
                } else {
                    LinearLayout view = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.null_page, null, false);
                    Button createPage = view.findViewById(R.id.createPage);
                    createPage.setOnClickListener(v -> startActivity(new Intent(cntxt, CreatePageAct.class)));
                    linearLayout.addView(view);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openPage(int pageId){
        Intent intent = new Intent(cntxt, PageActivity.class);
        Bundle params = new Bundle();
        params.putInt("pageId", pageId);
        intent.putExtras(params);
        startActivity(intent);
    }

}
