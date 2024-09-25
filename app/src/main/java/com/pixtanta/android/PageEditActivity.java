package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.pageForArt;
import static com.pixtanta.android.Constants.pageForBrnd;
import static com.pixtanta.android.Constants.pageForCom;
import static com.pixtanta.android.Constants.pageForEnt;
import static com.pixtanta.android.Constants.pageForLocal;
import static com.pixtanta.android.Constants.pageTypes;
import static com.pixtanta.android.PageActivity.changePageInfo;

public class PageEditActivity extends ThemeActivity implements AdapterView.OnItemSelectedListener {

    Context cntxt;
    RelativeLayout mainHomeView;
    LinearLayout layout, blackFade, savingLayer;
    ScrollView scrllView;
    TextView errLog;
    EditText pageName, email, phone, website, description;
    Spinner pageType, pageCat;
    Button saveBtn;
    ArrayAdapter<String> pageTypeAdapter, pageCatAdapter;
    String pageNameTxt, emailTxt, phoneTxt, websiteTxt, descriptionTxt;
    String[][] allPageCats;
    int pageId, pageTypeNum, pageCatNum, iniPageTpe, iniPageCat;
    private int myId;
    boolean firstLoad = true;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_edit);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(this, LoginAct.class));
            return;
        }
        Bundle userParams = getIntent().getExtras();
        pageId = userParams.getInt("pageId");
        myId = sharedPrefMngr.getMyId();
        allPageCats = new String[][]{
                pageForLocal,
                pageForCom,
                pageForBrnd,
                pageForArt,
                pageForEnt
        };

        mainHomeView =  findViewById(R.id.mainHomeView);
        layout =  findViewById(R.id.layout);
        blackFade =  findViewById(R.id.blackFade);
        savingLayer =  findViewById(R.id.savingLayer);
        scrllView =  findViewById(R.id.scrllView);
        errLog =  findViewById(R.id.errLog);
        pageName =  findViewById(R.id.pageName);
        email =  findViewById(R.id.email);
        phone =  findViewById(R.id.phone);
        website =  findViewById(R.id.website);
        description =  findViewById(R.id.description);
        pageType =  findViewById(R.id.pageType);
        pageCat =  findViewById(R.id.pageCat);
        saveBtn =  findViewById(R.id.saveBtn);
        pageType.setOnItemSelectedListener(this);
        pageCat.setOnItemSelectedListener(this);
        saveBtn.setOnClickListener(v -> saveInfo());

        loadInfo();
        setupUI(mainHomeView);
    }

    @SuppressLint("SetTextI18n")
    private void saveInfo() {
        errLog.setText("");
        errLog.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        String txtPageName = pageName.getText().toString();
        if(StringUtils.isEmpty(txtPageName)){
            errLog.setText("Your Page Name cannot be empty");
            errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
        } else {
            savingLayer.setVisibility(View.VISIBLE);
            String txtEmail = email.getText().toString();
            String txtPhone = phone.getText().toString();
            String txtWebsite = website.getText().toString();
            String txtDescription = description.getText().toString();
            try {
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("user", String.valueOf(myId))
                        .addFormDataPart("action", "updatePageInfo")
                        .addFormDataPart("pageId", String.valueOf(pageId))
                        .addFormDataPart("pageName", txtPageName)
                        .addFormDataPart("email", txtEmail)
                        .addFormDataPart("phone", txtPhone)
                        .addFormDataPart("website", txtWebsite)
                        .addFormDataPart("description", txtDescription)
                        .addFormDataPart("pageType", String.valueOf(pageTypeNum))
                        .addFormDataPart("pageCat", String.valueOf(pageCatNum))
                        .build();
                Request request = new Request.Builder()
                        .url(Constants.actionsUrl)
                        .post(requestBody)
                        .build();

                OkHttpClient okHttpClient = new OkHttpClient();
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    changePageInfo(txtPageName, pageTypeNum, pageCatNum);
                    finish();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadInfo(){
        try {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("pageId", String.valueOf(pageId))
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.pageInfoUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseStr = Objects.requireNonNull(response.body()).string();
                JSONObject jsonObject = new JSONObject(responseStr);
                pageNameTxt = jsonObject.getString("pageName");
                emailTxt = jsonObject.getString("email");
                phoneTxt = jsonObject.getString("phone");
                websiteTxt = jsonObject.getString("website");
                descriptionTxt = jsonObject.getString("description");
                pageTypeNum = jsonObject.getInt("pageType");
                pageCatNum = jsonObject.getInt("pageCat");
                iniPageTpe = pageTypeNum;
                iniPageCat = pageCatNum;
                setupSpinnerAdapter();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSpinnerAdapter() {
        pageName.setText(pageNameTxt);
        email.setText(emailTxt);
        phone.setText(phoneTxt);
        website.setText(websiteTxt);
        description.setText(descriptionTxt);
        pageTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pageTypes);
        pageType.setAdapter(pageTypeAdapter);
        pageType.setSelection(pageTypeNum, false);
        if(pageTypeNum == 5)
            pageCat.setVisibility(View.GONE);
        else {
            String[] array = allPageCats[pageTypeNum];
            pageCatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, array);
            pageCat.setAdapter(pageCatAdapter);
            pageCat.setSelection(pageCatNum, true);
        }
        scrllView.setVisibility(View.VISIBLE);
        layout.setVisibility(View.GONE);
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(v);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(firstLoad){
            firstLoad = false;
            return;
        }
        String selTxt = parent.getItemAtPosition(position).toString();
        List<String> list = Arrays.asList(pageTypes);
        if(list.contains(selTxt)) {
            pageTypeNum = position;
            if (position == 5) {
                pageCatNum = 0;
                pageCat.setVisibility(View.GONE);
            } else {
                int i = 0;
                if(position == iniPageTpe)
                    i = iniPageCat;
                pageCat.setVisibility(View.VISIBLE);
                String[] array = allPageCats[position];
                pageCatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, array);
                pageCat.setAdapter(pageCatAdapter);
                pageCat.setSelection(i, true);
            }
        } else {
            pageCatNum = position;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @SuppressLint("SetTextI18n")
    public void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
        } else if(savingLayer.getVisibility() == View.GONE){
            @SuppressLint("InflateParams") View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
            if(blackFade.getChildCount() > 0){
                blackFade.removeAllViews();
            }
            TextView txter = blockView.findViewById(R.id.txter);
            Button cnclBtn = blockView.findViewById(R.id.cancel);
            Button agreeBtn = blockView.findViewById(R.id.agree);
            txter.setText("Are You Sure You Want To Discard?");
            agreeBtn.setOnClickListener(v -> {
                blackFade.setVisibility(View.GONE);
                finish();
            });
            cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
            blackFade.addView(blockView);
            blackFade.setVisibility(View.VISIBLE);
        }
    }
}