package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotPasswordActivity extends ThemeActivity {

    Context context;
    RelativeLayout mainHomeView;
    LinearLayout blackFade, layout;
    EditText editText, password, conPassword;
    TextView textClick, errorText;
    Button button;
    String lang;
    int user = 0;
    boolean loading = false;
    SharedPrefMngr sharedPrefMngr;
    ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        sharedPrefMngr = new SharedPrefMngr(this);

        if(sharedPrefMngr.loggedIn()){
            startActivity(new Intent(this, HomeAct.class));
            finish();
            return;
        }

        Bundle bundle = getIntent().getExtras();
        if(!(bundle == null))
            user = bundle.getInt("user");

        context = this;
        imageLoader = new ImageLoader(this);
        lang = sharedPrefMngr.getSelectedLanguage();
        mainHomeView = findViewById(R.id.mainHomeView);
        blackFade = findViewById(R.id.blackFade);
        layout = findViewById(R.id.layout);
        errorText = findViewById(R.id.errorText);
        editText = findViewById(R.id.editText);
        textClick = findViewById(R.id.textClick);
        button = findViewById(R.id.button);

        textClick.setOnClickListener((v) -> {
            user = 0;
            editText.setVisibility(View.VISIBLE);
            textClick.setVisibility(View.GONE);
            button.setText(R.string._continue);
            layout.removeAllViews();
            editText.setText("");
            button.setOnClickListener((v1) -> getUser());
        });
        blackFade.setOnClickListener((v) -> {
            blackFade.setVisibility(View.GONE);
            return;
        });

        if(user == 0){
            editText.setVisibility(View.VISIBLE);
            button.setText(R.string._continue);
            button.setOnClickListener((v) -> getUser());
        } else {
            button.setText(R.string.get_code);
            button.setOnClickListener((v) -> sendCode());
            getUser();
        }

        setupUI(mainHomeView);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void sendCode() {
        showRequesting(R.string.sending_code);
        errorText.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);
        textClick.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        loading = true;
        while (loading) {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(user))
                    .addFormDataPart("code", "true")
                    .addFormDataPart("vCode", "false")
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.forgotPassUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try (Response response = call.execute()) {
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    loading = false;
                    blackFade.setVisibility(View.GONE);
                    editText.setVisibility(View.VISIBLE);
                    button.setVisibility(View.VISIBLE);
                    button.setText(R.string.submit_code);
                    button.setOnClickListener((v) -> submitCode());
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setText("");
                    editText.setHint(R.string.code);
                    layout.removeAllViews();
                    View blockView = LayoutInflater.from(context).inflate(R.layout.request_layer, null, false);
                    if(blackFade.getChildCount() > 0) blackFade.removeAllViews();
                    TextView txter = blockView.findViewById(R.id.txter);
                    Button cnclBtn = blockView.findViewById(R.id.cancel);
                    Button agreeBtn = blockView.findViewById(R.id.agree);
                    txter.setText(responseString);
                    cnclBtn.setVisibility(View.GONE);
                    agreeBtn.setText(R.string.ok);
                    agreeBtn.setOnClickListener(v -> {
                        blackFade.setVisibility(View.GONE);
                        editText.requestFocus();
                    });
                    blackFade.addView(blockView);
                    blackFade.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("InflateParams")
    private void submitCode() {
        String codeText = editText.getText().toString();
        if(!StringUtils.isEmpty(codeText)) {
            showRequesting(R.string.validating_code);
            errorText.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            textClick.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
            loading = true;
            while (loading) {
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("user", String.valueOf(user))
                        .addFormDataPart("codeText", codeText)
                        .addFormDataPart("code", "true")
                        .addFormDataPart("vCode", "true")
                        .build();
                Request request = new Request.Builder()
                        .url(Constants.forgotPassUrl)
                        .post(requestBody)
                        .build();

                OkHttpClient okHttpClient = new OkHttpClient();
                Call call = okHttpClient.newCall(request);
                try (Response response = call.execute()) {
                    if (response.isSuccessful()) {
                        String responseString = Objects.requireNonNull(response.body()).string();
                        loading = false;
                        blackFade.setVisibility(View.GONE);
                        JSONObject responseObj = new JSONObject(responseString);
                        boolean error = responseObj.getBoolean("error");
                        if(error){
                            String errTxt = responseObj.getString("errTxt");
                            errorText.setText(errTxt);
                            errorText.setVisibility(View.VISIBLE);
                            editText.setVisibility(View.VISIBLE);
                            button.setVisibility(View.VISIBLE);
                        } else {
                            layout.removeAllViews();
                            LinearLayout newLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.new_password, null);
                            password = newLayout.findViewById(R.id.password);
                            conPassword = newLayout.findViewById(R.id.conPassword);
                            errorText = newLayout.findViewById(R.id.errorText);
                            layout.addView(newLayout, 0);
                            button.setVisibility(View.VISIBLE);
                            button.setText(R.string.change_password);
                            button.setOnClickListener((v) -> changePassword());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private void changePassword() {
        String passwordText = password.getText().toString();
        String conPasswordText = conPassword.getText().toString();
        if(!StringUtils.isEmpty(passwordText) && !StringUtils.isEmpty(conPasswordText)) {
            showRequesting(R.string.validating_password);
            errorText.setVisibility(View.GONE);
            loading = true;
            while (loading) {
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("action", "changePass")
                        .addFormDataPart("directAccess", "true")
                        .addFormDataPart("directAccess", "true")
                        .addFormDataPart("user", String.valueOf(user))
                        .addFormDataPart("newPass", passwordText)
                        .addFormDataPart("conPass", conPasswordText)
                        .addFormDataPart("lang", lang)
                        .build();
                Request request = new Request.Builder()
                        .url(Constants.actionsUrl)
                        .post(requestBody)
                        .build();

                OkHttpClient okHttpClient = new OkHttpClient();
                Call call = okHttpClient.newCall(request);
                try (Response response = call.execute()) {
                    if (response.isSuccessful()) {
                        String responseString = Objects.requireNonNull(response.body()).string();
                        loading = false;
                        blackFade.setVisibility(View.GONE);
                        JSONObject responseObj = new JSONObject(responseString);
                        boolean error = responseObj.getBoolean("error");
                        String respTxt = responseObj.getString("errMsg");
                        if(error){
                            errorText.setText(respTxt);
                            errorText.setVisibility(View.VISIBLE);
                        } else {
                            View blockView = LayoutInflater.from(context).inflate(R.layout.request_layer, null, false);
                            if(blackFade.getChildCount() > 0) blackFade.removeAllViews();
                            TextView txter = blockView.findViewById(R.id.txter);
                            Button cnclBtn = blockView.findViewById(R.id.cancel);
                            Button agreeBtn = blockView.findViewById(R.id.agree);
                            txter.setText(respTxt);
                            cnclBtn.setVisibility(View.GONE);
                            agreeBtn.setText(R.string.ok);
                            agreeBtn.setOnClickListener(v -> {
                                Intent intent = new Intent(ForgotPasswordActivity.this, LoginAct.class);
                                startActivity(intent);
                                finish();
                            });
                            blackFade.addView(blockView);
                            blackFade.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void getUser() {
        String emailText = editText.getText().toString();
        if(!StringUtils.isEmpty(emailText) || !(user == 0)) {
            showRequesting(R.string.searching_for_user);
            errorText.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            textClick.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
            loading = true;
            String postName = "user";
            String postStr = String.valueOf(user);
            if(user == 0){
                postName = "emailText";
                postStr = emailText;
            }
            while (loading) {
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart(postName, postStr)
                        .addFormDataPart("code", "false")
                        .build();
                Request request = new Request.Builder()
                        .url(Constants.forgotPassUrl)
                        .post(requestBody)
                        .build();

                OkHttpClient okHttpClient = new OkHttpClient();
                Call call = okHttpClient.newCall(request);
                try (Response response = call.execute()) {
                    if (response.isSuccessful()) {
                        String responseString = Objects.requireNonNull(response.body()).string();
                        loading = false;
                        blackFade.setVisibility(View.GONE);
                        JSONObject responseObj = new JSONObject(responseString);
                        boolean error = responseObj.getBoolean("error");
                        if(error){
                            String errTxt = responseObj.getString("errTxt");
                            errorText.setText(errTxt);
                            errorText.setVisibility(View.VISIBLE);
                            editText.setVisibility(View.VISIBLE);
                            button.setVisibility(View.VISIBLE);
                            button.setText(R.string._continue);
                            button.setOnClickListener((v) -> getUser());
                        } else {
                            user = responseObj.getInt("user");
                            String photo = responseObj.getString("photo");
                            String name = responseObj.getString("name");
                            String userName = responseObj.getString("userName");
                            boolean verified = responseObj.getBoolean("verified");
                            RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.smart_log, null);
                            ImageView imgView =  relativeLayout.findViewById(R.id.photoU);
                            TextView textView =  relativeLayout.findViewById(R.id.nameU);
                            TextView userNameTV =  relativeLayout.findViewById(R.id.userNameU);
                            ImageView menu =  relativeLayout.findViewById(R.id.menu);
                            menu.setVisibility(View.GONE);
                            imageLoader.displayImage(Constants.www + photo, imgView);
                            textView.setText(name);
                            userNameTV.setText("@"+userName);
                            if(verified)
                                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user_white, 0);
                            layout.addView(relativeLayout, 0);
                            textClick.setVisibility(View.VISIBLE);
                            button.setVisibility(View.VISIBLE);
                            button.setText(R.string.get_code);
                            button.setOnClickListener((v) -> sendCode());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private void showRequesting(int text){
        View view = LayoutInflater.from(context).inflate(R.layout.requesting_layout, null, false);
        if(blackFade.getChildCount() > 0) blackFade.removeAllViews();
        TextView txter = view.findViewById(R.id.textView);
        txter.setText(text);
        blackFade.addView(view);
        blackFade.setVisibility(View.VISIBLE);    
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        view.setOnTouchListener((v, event) -> {
            hideSoftKeyboard(v);
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

    @SuppressLint("InflateParams")
    public void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE)
            blackFade.setVisibility(View.GONE);
        else {
            if(blackFade.getChildCount() > 0)
                blackFade.removeAllViews();
            View blockView = LayoutInflater.from(context).inflate(R.layout.request_layer, null, false);
            TextView txter = blockView.findViewById(R.id.txter);
            Button cnclBtn = blockView.findViewById(R.id.cancel);
            Button agreeBtn = blockView.findViewById(R.id.agree);
            txter.setText(R.string.discard_forget_password);
            agreeBtn.setOnClickListener(v -> finish());
            cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
            blackFade.addView(blockView);
            blackFade.setVisibility(View.VISIBLE);
        }
    }
}