package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignupAct extends ThemeActivity implements AdapterView.OnItemSelectedListener {

    EditText fName, lName, email, userName, pass, cPass;
    String monS, dayS, yearS, gender, lang;
    RadioGroup radio;
    Button regBtn;
    LinearLayout logoutLayer;
    TextView errLog;
    RelativeLayout mainHomeView;
    Spinner dropdown, dropdownDays, dropdownY;
    boolean requesting = false;
    SharedPrefMngr sharedPrefMngr;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        SaveOpenedMessages.resetInstance();
        sharedPrefMngr = new SharedPrefMngr(this);
		
        lang = sharedPrefMngr.getSelectedLanguage();
        mainHomeView =  findViewById(R.id.mainHomeView);
        setupUI(mainHomeView);

        if(sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(SignupAct.this, HomeAct.class));
            return;
        }

        logoutLayer =  findViewById(R.id.logoutLayer);
        String[] months = new String[]{"Month", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        List<String> days = new ArrayList<>(), years = new ArrayList<>();
        String dy = "Day", yr = "Year";
        years.add(yr);
        days.add(dy);
        Date date = new Date();
        Calendar calndr = new GregorianCalendar();
        calndr.setTime(date);
        int surYr, curYr = calndr.get(Calendar.YEAR);
        surYr = curYr - 13;
        for(int i = 1; i < 32; i++){
            String day = Integer.toString(i);
            days.add(day);
        }
        for(int i = surYr; i > 1899; i--){
            String year = Integer.toString(i);
            years.add(year);
        }
        dropdown = findViewById(R.id.month);
        dropdownDays = findViewById(R.id.day);
        dropdownY = findViewById(R.id.year);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapterD = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days);
        dropdownDays.setAdapter(adapterD);
        dropdownDays.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapterY = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years);
        dropdownY.setAdapter(adapterY);
        dropdownY.setOnItemSelectedListener(this);

        errLog =  findViewById(R.id.errLog);
        fName =  findViewById(R.id.fName);
        lName =  findViewById(R.id.lName);
        email =  findViewById(R.id.email);
        userName =  findViewById(R.id.userName);
        pass =  findViewById(R.id.pass);
        cPass =  findViewById(R.id.cPass);
        regBtn =  findViewById(R.id.regBtn);
        radio =  findViewById(R.id.gender);

        regBtn.setOnClickListener(v -> {
            errLog.setText("");
            errLog.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            int radioId = radio.getCheckedRadioButtonId();
            RadioButton selGnd = radio.findViewById(radioId);
            String txtFName = fName.getText().toString();
            String txtLName = lName.getText().toString();
            String txtEmail = email.getText().toString();
            String txtUName = userName.getText().toString();
            String txtPass = pass.getText().toString();
            String txtCPass = cPass.getText().toString();
            if(StringUtils.isEmpty(txtFName)){
                errLog.setText("Your First name cannot be empty");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(StringUtils.isEmpty(txtLName)){
                errLog.setText("Your Last name cannot be empty");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(StringUtils.isEmpty(txtEmail)){
                errLog.setText("Your Email cannot be empty");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(StringUtils.isEmpty(txtUName)){
                errLog.setText("Your Username cannot be empty");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(StringUtils.isEmpty(txtPass)){
                errLog.setText("Your Password cannot be empty");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(StringUtils.isEmpty(txtCPass)){
                errLog.setText("Please confirm Your Password");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(monS.equals("Month")){
                errLog.setText("Please select Your month of birth");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(dayS.equals("Day")){
                errLog.setText("Please select Your day of birth");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(yearS.equals("Year")){
                errLog.setText("Please select Your year of birth");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(selGnd == null){
                errLog.setText("Please select Your gender");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else {
                gender = selGnd.getText().toString();
                regNewAcc(txtFName, txtLName, txtEmail, txtUName, txtPass, txtCPass, gender);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void regNewAcc(final String fName, final String lName, final String email, final String userName, final String pass, final String cPass, final String gender){
        logoutLayer.setVisibility(View.VISIBLE);
        requesting = true;
        String url = Constants.regUrl;
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                boolean error = obj.getBoolean("error");
                if(error){
                    requesting = false;
                    logoutLayer.setVisibility(View.GONE);
                    String errMsg = obj.getString("errorMsg");
                    errLog.setText(errMsg);
                    errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
                } else {
                    sharedPrefMngr.storeUserInfo(obj.getInt("id"), obj.getString("photo"), obj.getString("name"), obj.getString("userName"), false);
                    startActivity(new Intent(SignupAct.this, HomeAct.class));
                    finish();
                }
            } catch (JSONException e){
                e.printStackTrace();
                errLog.setText("Connection Error");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            }
        }, error -> {
            requesting = false;
            logoutLayer.setVisibility(View.GONE);
            errLog.setText("Connection Error");
        }){
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String > param = new HashMap<>();
                param.put("lang", lang);
                param.put("fName", fName);
                param.put("lName", lName);
                param.put("email", email);
                param.put("userName", userName);
                param.put("pass", pass);
                param.put("cPass", cPass);
                param.put("month", monS);
                param.put("day", dayS);
                param.put("year", yearS);
                param.put("gender", gender);
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(SignupAct.this).addToReqQ(request);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        int color;
        if(darkThemeEnabled)
            color = R.color.white;
        else
            color = R.color.black;
        ((TextView)parent.getChildAt(0)).setTextColor(ContextCompat.getColor(this, color));
        String selTxt = parent.getItemAtPosition(position).toString();
        String[] months = new String[]{"Month", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        List<String> mnthLst = Arrays.asList(months);
        if(!selTxt.equals("Month") && !selTxt.equals("Day") && !selTxt.equals("Year")){
            if(mnthLst.contains(selTxt))
                monS = selTxt;
            else {
                int num = Integer.parseInt(selTxt);
                if(num < 32)
                    dayS = Integer.toString(num);
                else
                    yearS = Integer.toString(num);
            }
        } else if(selTxt.equals("Month"))
            monS = selTxt;
        else if(selTxt.equals("Day"))
            dayS = selTxt;
        else yearS = selTxt;
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

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
