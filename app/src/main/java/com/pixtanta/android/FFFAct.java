package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FFFAct extends ThemeActivity {

    Context cntxt;
    LinearLayout resultLayout, searchLayout, loadingLayer;
    TextView reactNum;
    String tab;
    EditText search;
    boolean loading = false, allLoaded;
    ArrayList<String> selectedDatas = new ArrayList<>();
    ImageLoader imageLoader;
    ScrollView scrllView;
    int myId, user;
    SharedPrefMngr sharedPrefMngr;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fffact);
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
        user = userParams.getInt("user");
        tab = userParams.getString("tab");

        resultLayout = findViewById(R.id.resultLayout);
        searchLayout = findViewById(R.id.searchLayout);
        scrllView = findViewById(R.id.scrllView);
        search = findViewById(R.id.search);
        reactNum = findViewById(R.id.reactNum);
        loadingLayer = findViewById(R.id.loadingLayer);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString();
                if(!StringUtils.isEmpty(searchText)){
                    searchLayout.setVisibility(View.VISIBLE);
                    performSearch(searchText);
                } else
                    searchLayout.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String searchText = search.getText().toString();
                if(!StringUtils.isEmpty(searchText)) {
                    searchLayout.setVisibility(View.VISIBLE);
                    performSearch(searchText);
                } else
                    searchLayout.setVisibility(View.GONE);
                return true;
            }
            return false;
        });

        getContents();

        scrllView.setSmoothScrollingEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrllView.setOnScrollChangeListener((scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int scrollH = resultLayout.getHeight() - scrllVw.getHeight() - 200;
                if(scrollY > scrollH && !loading && !allLoaded){
                    loading = true;
                    loadingLayer = (LinearLayout) getLayoutInflater().inflate(R.layout.users_loader, null, false);
                    resultLayout.addView(loadingLayer);
                    ViewTreeObserver viewTreeObserver = loadingLayer.getViewTreeObserver();
                    viewTreeObserver.addOnGlobalLayoutListener(this::getContents);
                }
            });
        }

    }

    @SuppressLint("SetTextI18n")
    private void getContents() {
        loading = true;
        while (loading) {
            MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(user))
                    .addFormDataPart("selectedDatas", selectedDatas.toString().intern())
                    .addFormDataPart("tab", tab)
                    .addFormDataPart("myId", String.valueOf(myId));
            RequestBody requestBody = multipartBody.build();
            Request request = new Request.Builder()
                    .url(Constants.listerUrl)
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
                    int dtNum = jsonArray.getInt(1);
                    String tabH = jsonArray.getString(2);
                    String data = jsonArray.getString(3);
                    String dtStr = Functions.convertToText(dtNum);
                    reactNum.setText(tabH + " - " + dtStr);
                    JSONArray dataArray = new JSONArray(data);
                    for (int i = 0; i < dataArray.length(); i++) {
                        String dataStr = dataArray.getString(i);
                        JSONObject dataObj = new JSONObject(dataStr);
                        LinearLayout listView = setupView(dataObj);
                        resultLayout.addView(listView);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void performSearch(String searchText) {
        if(searchLayout.getChildCount() > 0)
            searchLayout.removeAllViews();
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("category", "search")
                .addFormDataPart("searchText", searchText)
                .addFormDataPart("user", String.valueOf(myId))
                .build();
        Request request = new Request.Builder()
                .url(Constants.searchUrl)
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String searchData = Objects.requireNonNull(response.body()).string();
                JSONArray jsonArray = new JSONArray(searchData);
                if(jsonArray.length() > 0){
                    for (int i = 0; i < jsonArray.length(); i++){
                        String searchStr = jsonArray.getString(i);
                        JSONObject jsonObject = new JSONObject(searchStr);
                        LinearLayout linearLayout = setupView(jsonObject);
                        searchLayout.addView(linearLayout);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LinearLayout setupView(JSONObject dataObj) throws JSONException {
        int userId = dataObj.getInt("user");
        String name = dataObj.getString("name");
        String userName = dataObj.getString("userName");
        String photo = dataObj.getString("photo");
        boolean verified = dataObj.getBoolean("verified");
        selectedDatas.add(String.valueOf(userId));
        userName = "@"+userName;
        photo = Constants.www + photo;
        @SuppressLint("InflateParams") LinearLayout listView = (LinearLayout) getLayoutInflater().inflate(R.layout.show_users, null);
        ImageView imageView = listView.findViewById(R.id.photo);
        TextView nameTV = listView.findViewById(R.id.name);
        TextView userNameTV = listView.findViewById(R.id.userName);
        imageLoader.displayImage(photo, imageView);
        nameTV.setText(name);
        userNameTV.setText(userName);
        if(verified)
            nameTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user, 0);
        listView.setOnClickListener(v -> visitUserProfile(userId));
        return listView;
    }

    private void visitUserProfile(int user) {
        Intent intent = new Intent(cntxt, ProfileAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("userID", user);
        intent.putExtras(userParams);
        startActivity(intent);
    }

    public void onBackPressed(){
        if(searchLayout.getVisibility() == View.VISIBLE) {
            search.setText("");
            searchLayout.setVisibility(View.GONE);
        } else
            finish();
    }
}