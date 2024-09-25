package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingsAct extends ThemeActivity {

    String lang;
    ProgressBar progressBar;
    LinearLayout settingsLayer;
    int[] drawableLefts;
    private int myId;
    RelativeLayout mainHomeView;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        sharedPrefMngr = new SharedPrefMngr(this);
        mainHomeView =  findViewById(R.id.mainHomeView);
        setupUI(mainHomeView);

        lang = sharedPrefMngr.getSelectedLanguage();
        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(SettingsAct.this, LoginAct.class));
            return;
        }

        drawableLefts = new int[] {
                R.drawable.ic_settings,
                R.drawable.ic_lock,
                R.drawable.ic_privacy,
                R.drawable.ic_postprf,
                R.drawable.ic_tandt,
                R.drawable.ic_note,
                R.drawable.ic_postp,
                R.drawable.ic_block
        };

        myId = sharedPrefMngr.getMyId();
        settingsLayer =  findViewById(R.id.settingsLayer);
        progressBar =  findViewById(R.id.progressBar);
        String url = Constants.settingsUrl;
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            progressBar.setVisibility(View.GONE);
            try {
                JSONObject settingsObj = new JSONObject(response);
                for(int i = 0; i < Objects.requireNonNull(settingsObj.names()).length(); i++){
                    String menuKey = Objects.requireNonNull(settingsObj.names()).getString(i);
                    String objKey = Objects.requireNonNull(settingsObj.names()).getString(i);
                    String objVal = settingsObj.getString(objKey);
                    @SuppressLint("InflateParams") LinearLayout settingsListView = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_list, null);
                    TextView settingsListText = settingsListView.findViewById(R.id.tcc);
                    settingsListText.setText(objVal);
                    int bgIcon = drawableLefts[i];
                    settingsListText.setCompoundDrawablesWithIntrinsicBounds(bgIcon, 0, 0, 0);
                    settingsListView.setOnClickListener(v -> settingsOptionSelectClick(menuKey));
                    settingsLayer.addView(settingsListView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            progressBar.setVisibility(View.GONE);

        }){
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String > param = new HashMap<>();
                String userId = Integer.toString(myId);
                param.put("lang", lang);
                param.put("user", userId);
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(SettingsAct.this).addToReqQ(request);
    }

    public void settingsOptionSelectClick(String key){
        Intent intent = new Intent(SettingsAct.this, SettingAct.class);
        Bundle tab = new Bundle();
        tab.putString("tab", key);
        intent.putExtras(tab);
        startActivity(intent);
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
