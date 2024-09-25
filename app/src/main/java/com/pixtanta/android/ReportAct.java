package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONException;

public class ReportAct extends ThemeActivity {

    Context cntxt;
    private int myId;
    String string, text;
    boolean shakeOpt;
    String[] strings = new String[]{"Camera", "Friend Request", "Menu", "Messenger", "Notifications", "Pages", "Photos", "Post Feeds", "Privacy", "Profile", "Search", "Settings", "Videos"};
    LinearLayout first, second, third, shaker, mainLayout;
    Button sendBtn, close;
    EditText reportText;
    TextView toogle;
    RelativeLayout mainView;
    SharedPrefMngr sharedPrefMngr;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(this, LoginAct.class));
            return;
        }

        myId = sharedPrefMngr.getMyId();
        shakeOpt = sharedPrefMngr.checkShakeOption();

        mainLayout =  findViewById(R.id.mainLayout);
        first =  findViewById(R.id.first);
        second =  findViewById(R.id.second);
        third =  findViewById(R.id.third);
        shaker =  findViewById(R.id.shaker);
        reportText =  findViewById(R.id.reportText);
        sendBtn =  findViewById(R.id.sendBtn);
        close =  findViewById(R.id.close);
        toogle =  findViewById(R.id.toogle);
        mainView =  findViewById(R.id.mainView);

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
        close.setOnClickListener(v -> {
            first.setVisibility(View.GONE);
            shaker.setVisibility(View.GONE);
            second.setVisibility(View.VISIBLE);
        });
        sendBtn.setOnClickListener(v -> sendReport());
        setupUI(mainView);

        for (String str : strings){
            TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
            optionList.setText(str);
            second.addView(optionList);
            optionList.setOnClickListener(v -> {
                string = str;
                second.setVisibility(View.GONE);
                third.setVisibility(View.VISIBLE);
            });
        }

    }

    private void sendReport() {
        text = reportText.getText().toString();
        if(!(string == null) && !StringUtils.isEmpty(text)) {
            try {
                reportText.setText("");
                Functions.sendReport(myId, string, text);
                Toast.makeText(cntxt, "Report Sent!", Toast.LENGTH_SHORT).show();
                third.setVisibility(View.GONE);
                first.setVisibility(View.VISIBLE);
                shaker.setVisibility(View.VISIBLE);
                string = null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onBackPressed(){
        if(third.getVisibility() == View.VISIBLE){
            reportText.setText("");
            third.setVisibility(View.GONE);
            second.setVisibility(View.VISIBLE);
            string = null;
        } else
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