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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class SmartLoginAct extends ThemeActivity {

    String lang;
    Button loginBtn, regBtn;
    LinearLayout layer, blackFade;
    JSONObject availableSmartLogin;
    ImageLoader imageLoader;
    RelativeLayout mainHomeView;
    public Activity activity;
    SmartPassAct smartPassAct;
    SharedPrefMngr sharedPrefMngr;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_login);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        SaveOpenedMessages.resetInstance();
        activity = this;
        sharedPrefMngr = new SharedPrefMngr(this);
		
        lang = sharedPrefMngr.getSelectedLanguage();
        mainHomeView =  findViewById(R.id.mainHomeView);
        setupUI(mainHomeView);
        smartPassAct = new SmartPassAct();

        if(sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(SmartLoginAct.this, HomeAct.class));
            return;
        }

        imageLoader = new ImageLoader(this);
        layer =  findViewById(R.id.layer);
        blackFade =  findViewById(R.id.blackFade);
        loginBtn =  findViewById(R.id.loginBtn);
        regBtn =  findViewById(R.id.regBtn);
        availableSmartLogin = sharedPrefMngr.getAvailableSmartLogin();

        if(!(availableSmartLogin == null)){
            for(int i = 0; i < Objects.requireNonNull(availableSmartLogin.names()).length(); i++){
                try {
                    String key = Objects.requireNonNull(availableSmartLogin.names()).getString(i);
                    String user = availableSmartLogin.getString(key);
                    String[] userData = sharedPrefMngr.getSmartLoginInfo(user);
                    String photoU = userData[0];
                    String nameU = userData[1];
                    String userNameU = userData[2];
                    boolean verified = sharedPrefMngr.checkUserVerified(Integer.parseInt(user));
                    String tag = "wll-"+user;
                    @SuppressLint("InflateParams") RelativeLayout smartListView = (RelativeLayout) getLayoutInflater().inflate(R.layout.smart_log, null);
                    ImageView imgView =  smartListView.findViewById(R.id.photoU);
                    LinearLayout holder =  smartListView.findViewById(R.id.holder);
                    TextView textView =  smartListView.findViewById(R.id.nameU);
                    TextView userName =  smartListView.findViewById(R.id.userNameU);
                    ImageView menu =  smartListView.findViewById(R.id.menu);
                    smartListView.setTag(tag);
                    imageLoader.displayImage(Constants.www + photoU, imgView);
                    textView.setText(nameU);
                    userName.setText("@"+userNameU);
                    if(verified)
                        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user_white, 0);
                    menu.setOnClickListener(v -> {
                        boolean reqPass = sharedPrefMngr.checkSmartPassOff(Integer.parseInt(user));
                        openOptions(reqPass, Integer.parseInt(user));
                    });
                    holder.setOnClickListener(v -> {
                        boolean reqPass = sharedPrefMngr.checkSmartPassOff(Integer.parseInt(user));
                        smartLogin(reqPass, Integer.parseInt(user), photoU, nameU, userNameU, verified);
                    });
                    layer.addView(smartListView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        regBtn.setOnClickListener(v -> startActivity(new Intent(SmartLoginAct.this, SignupAct.class)));
        loginBtn.setOnClickListener(v -> startActivity(new Intent(SmartLoginAct.this, LoginAct.class)));
    }

    @SuppressLint("InflateParams")
    private void openOptions(boolean reqPass, int user) {
        try {
            JSONObject object = new JSONObject();
            JSONObject objectIcon = new JSONObject();
            if(reqPass)
                object.put("removePswd", "Remove Password from Account");
            else
                object.put("requestPswd", "Request Password for Account");
            object.put("removeAcc", "Remove this Account");
            objectIcon.put("removePswd", R.drawable.ic_cancel_fill);
            objectIcon.put("requestPswd", R.drawable.ic_check_fill);
            objectIcon.put("removeAcc", R.drawable.ic_delete);
            RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
            LinearLayout optLayer = relativeLayout.findViewById(R.id.optLayer);
            if(blackFade.getChildCount() > 0)
                blackFade.removeAllViews();
            JSONArray objKeys = object.names();
            for (int r = 0; r < Objects.requireNonNull(objKeys).length(); r++){
                TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
                String key = objKeys.getString(r);
                String option = object.getString(key);
                int drawableLeft = objectIcon.getInt(key);
                optionList.setText(option);
                optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
                optionList.setOnClickListener(v -> executeOption(user, key));
                optLayer.addView(optionList);
            }
            blackFade.addView(relativeLayout);
            blackFade.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void executeOption(int user, String key) {
        switch (key){
            case "removePswd":
                View remPassView = getLayoutInflater().inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                TextView txterRP = remPassView.findViewById(R.id.txter);
                Button cnclBtnRP = remPassView.findViewById(R.id.cancel);
                Button agreeBtnRP = remPassView.findViewById(R.id.agree);
                txterRP.setText("Are you sure you want to remove password for this account?");
                agreeBtnRP.setOnClickListener(v -> {
                    getUserPassword(user);
                });
                cnclBtnRP.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
                blackFade.addView(remPassView);
                blackFade.setVisibility(View.VISIBLE);
                break;
            case "requestPswd":
                View reqPassView = getLayoutInflater().inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                TextView txterRPs = reqPassView.findViewById(R.id.txter);
                Button cnclBtnRPs = reqPassView.findViewById(R.id.cancel);
                Button agreeBtnRPs = reqPassView.findViewById(R.id.agree);
                txterRPs.setText("Are you sure you want to request password for this account?");
                agreeBtnRPs.setOnClickListener(v -> {
                    blackFade.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Account has been set to request for password", Toast.LENGTH_LONG).show();
                    sharedPrefMngr.setSmartPassOff(user, true);
                });
                cnclBtnRPs.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
                blackFade.addView(reqPassView);
                blackFade.setVisibility(View.VISIBLE);
                break;
            case "removeAcc":
                View remAccView = getLayoutInflater().inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0)
                    blackFade.removeAllViews();
                TextView txterRA = remAccView.findViewById(R.id.txter);
                Button cnclBtnRA = remAccView.findViewById(R.id.cancel);
                Button agreeBtnRA = remAccView.findViewById(R.id.agree);
                txterRA.setText("Are you sure you want to remove this account?");
                agreeBtnRA.setOnClickListener(v -> {
                    String tag = "wll-"+user;
                    RelativeLayout relativeLayout = layer.findViewWithTag(tag);
                    layer.removeView(relativeLayout);
                    blackFade.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Account removed", Toast.LENGTH_LONG).show();
                    sharedPrefMngr.saveSmartLogin(user, false, false);
                });
                cnclBtnRA.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
                blackFade.addView(remAccView);
                blackFade.setVisibility(View.VISIBLE);
                break;
        }
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void getUserPassword(int user) {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        blackFade.setVisibility(View.GONE);
        Intent intent = new Intent(SmartLoginAct.this, SmartPassAct.class);
        Bundle bundle = new Bundle();
        bundle.putInt("user", user);
        bundle.putBoolean("returnPass", true);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void smartLogin(boolean passReq, int user, String photo, String name, String userName, boolean verified){
        if(!passReq){
            sharedPrefMngr.storeUserInfo(user, photo, name, userName, verified);
            startActivity(new Intent(SmartLoginAct.this, HomeAct.class));
            if(!(smartPassAct.activity == null))
                smartPassAct.activity.finish();
            finish();
        } else {
            Intent intent = new Intent(SmartLoginAct.this, SmartPassAct.class);
            Bundle bundle = new Bundle();
            bundle.putInt("user", user);
            bundle.putBoolean("returnPass", false);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE)
            blackFade.setVisibility(View.GONE);
        else
            finish();
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

}
