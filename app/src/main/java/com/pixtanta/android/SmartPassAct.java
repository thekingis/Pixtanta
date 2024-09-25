package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class SmartPassAct extends ThemeActivity {

    public Activity activity;
    ImageView photo;
    String lang, txtPass;
    Button loginBtn;
    EditText pass;
    TextView errLog, name, userName, forgotPass;
    LinearLayout logoutLayer;
    int user;
    boolean returnPass;
    ImageLoader imageLoader;
    RelativeLayout mainHomeView;
    LoginAct loginAct;
    SmartLoginAct smartLoginAct;
    SharedPrefMngr sharedPrefMngr;
    boolean loading = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_pass);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        SaveOpenedMessages.resetInstance();
        activity = this;
        sharedPrefMngr = new SharedPrefMngr(this);
		
        lang = sharedPrefMngr.getSelectedLanguage();
        mainHomeView =  findViewById(R.id.mainHomeView);
        setupUI(mainHomeView);
        loginAct = new LoginAct();
        smartLoginAct = new SmartLoginAct();

        if(sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(SmartPassAct.this, HomeAct.class));
            return;
        }

        Bundle bundle = getIntent().getExtras();
        user = bundle.getInt("user");
        returnPass = bundle.getBoolean("returnPass", false);
        String[] userData = sharedPrefMngr.getSmartLoginInfo(String.valueOf(user));
        String photoU = Constants.www + userData[0];
        String nameU = userData[1];
        String userNameU = "@"+userData[2];
        boolean verified = sharedPrefMngr.checkUserVerified(user);

        imageLoader = new ImageLoader(this);
        logoutLayer =  findViewById(R.id.logoutLayer);
        photo =  findViewById(R.id.photo);
        forgotPass =  findViewById(R.id.forgotPass);
        errLog =  findViewById(R.id.errLog);
        name =  findViewById(R.id.name);
        userName =  findViewById(R.id.userName);
        pass =  findViewById(R.id.pass);
        loginBtn =  findViewById(R.id.loginBtn);
        imageLoader.displayImage(photoU, photo);
        name.setText(nameU);
        userName.setText(userNameU);
        if(verified)
            name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user, 0);

        if(returnPass){
            TextView textView =  findViewById(R.id.textView);
            textView.setText("Confirming...");
            loginBtn.setText("Confirm Password");
        }

        loginBtn.setOnClickListener(v -> {
            errLog.setText("");
            errLog.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            txtPass = pass.getText().toString();
            if(StringUtils.isEmpty(txtPass)){
                errLog.setText("Your Password cannot be empty");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else
                validatePassword();
        });

        forgotPass.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            Bundle bundleX = new Bundle();
            bundleX.putInt("user", user);
            intent.putExtras(bundleX);
            startActivity(intent);
        });

    }
    @SuppressLint("SetTextI18n")
    private void validatePassword(){
        loading = true;
        logoutLayer.setVisibility(View.VISIBLE);
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("lang", lang)
                .addFormDataPart("user", String.valueOf(user))
                .addFormDataPart("password", txtPass)
                .build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(Constants.loginUrl)
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        try (okhttp3.Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful()) {
                String responseString = Objects.requireNonNull(response.body()).string();
                JSONObject obj = new JSONObject(responseString);
                boolean error = obj.getBoolean("error");
                if(error){
                    loading = false;
                    logoutLayer.setVisibility(View.GONE);
                    String errMsg = obj.getString("errorMsg");
                    errLog.setText(errMsg);
                    errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
                } else {
                    if(returnPass){
                        Toast.makeText(getApplicationContext(), "Account has been set not to request for password", Toast.LENGTH_LONG).show();
                        sharedPrefMngr.setSmartPassOff(user, false);
                    } else {
                        sharedPrefMngr.storeUserInfo(user, obj.getString("photo"), obj.getString("name"), obj.getString("userName"), obj.getBoolean("verified"));
                        startActivity(new Intent(SmartPassAct.this, HomeAct.class));
                        if (!(smartLoginAct.activity == null))
                            smartLoginAct.activity.finish();
                        if (!(loginAct.activity == null))
                            loginAct.activity.finish();
                    }
                    finish();
                }
            } else {
                loading = false;
                logoutLayer.setVisibility(View.GONE);
                errLog.setText("Connection Error");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public void onBackPressed(){
        if(!loading)
            finish();
    }
}
