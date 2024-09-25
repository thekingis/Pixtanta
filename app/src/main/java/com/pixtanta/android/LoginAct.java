package com.pixtanta.android;

import static com.pixtanta.android.HomeAct.popMessage;
import static com.pixtanta.android.InboxAct.hrzntlScrllVw;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.pixtanta.android.Utils.StringUtils;

import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginAct extends ThemeActivity {

    public Activity activity;
    EditText email, pass;
    Button loginBtn, regBtn;
    LinearLayout logoutLayer;
    ProgressBar progressBar;
    TextView errLog, forgotPass;
    String lang;
    RelativeLayout mainHomeView;
    boolean requesting = false;
    SmartPassAct smartPassAct;
    SmartLoginAct smartLoginAct;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        sharedPrefMngr = new SharedPrefMngr(this);
		
        lang = sharedPrefMngr.getSelectedLanguage();
        mainHomeView = findViewById(R.id.mainHomeView);
        setupUI(mainHomeView);
        activity = this;
        smartPassAct = new SmartPassAct();
        smartLoginAct = new SmartLoginAct();

        SaveOpenedMessages.resetInstance();
        if(sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(LoginAct.this, HomeAct.class));
            return;
        }
        logoutLayer = findViewById(R.id.logoutLayer);
        forgotPass =  findViewById(R.id.forgotPass);
        progressBar = findViewById(R.id.progressBar);
        errLog = findViewById(R.id.errLog);
        email = findViewById(R.id.email);
        pass = findViewById(R.id.pass);
        loginBtn = findViewById(R.id.loginBtn);
        regBtn = findViewById(R.id.regBtn);
        regBtn.setOnClickListener(v -> startActivity(new Intent(LoginAct.this, SignupAct.class)));
        loginBtn.setOnClickListener(v -> {
            errLog.setText("");
            errLog.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            String txtEmail = email.getText().toString();
            String txtPass = pass.getText().toString();
            if(StringUtils.isEmpty(txtEmail)){
                String s = "Your Email cannot be empty";
                errLog.setText(s);
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(StringUtils.isEmpty(txtPass)){
                String s = "Your Password cannot be empty";
                errLog.setText(s);
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else {
                validateLogin();
            }
        });
        email.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_UP && keyCode == 66){
                if(!StringUtils.isEmpty(email.getText().toString()) && !StringUtils.isEmpty(pass.getText().toString()))
                    validateLogin();
            }
            return false;
        });
        pass.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_UP && keyCode == 66){
                if(!StringUtils.isEmpty(email.getText().toString()) && !StringUtils.isEmpty(pass.getText().toString()))
                    validateLogin();
            }
            return false;
        });

        forgotPass.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void validateLogin(){
        if(!requesting) {
            requesting = true;
            logoutLayer.setVisibility(View.VISIBLE);
            String txtEmail = email.getText().toString();
            String txtPass = pass.getText().toString();
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("lang", lang)
                    .addFormDataPart("email", txtEmail)
                    .addFormDataPart("pass", txtPass)
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(Constants.loginUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try (Response response = call.execute()) {
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject obj = new JSONObject(responseString);
                        boolean error = obj.getBoolean("error");
                        if (error) {
                            requesting = false;
                            logoutLayer.setVisibility(View.GONE);
                            String errMsg = obj.getString("errorMsg");
                            errLog.setText(errMsg);
                            errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
                        } else {
                            sharedPrefMngr.storeUserInfo(obj.getInt("id"), obj.getString("photo"), obj.getString("name"), obj.getString("userName"), obj.getBoolean("verified"));
                            startActivity(new Intent(LoginAct.this, HomeAct.class));
                            if (!(smartLoginAct.activity == null))
                                smartLoginAct.activity.finish();
                            if (!(smartPassAct == null))
                                smartPassAct.finish();
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    requesting = false;
                    logoutLayer.setVisibility(View.GONE);
                    String s = "Connection Error";
                    errLog.setText(s);
                    errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                requesting = false;
                logoutLayer.setVisibility(View.GONE);
                String s = "Connection Error";
                errLog.setText(s);
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            }
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

    public  void onBackPressed(){
        if(!requesting){
            finish();
        }
    }
}
