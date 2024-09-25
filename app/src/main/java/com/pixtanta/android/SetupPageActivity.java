package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
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
import org.json.JSONObject;

import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class SetupPageActivity extends ThemeActivity {

    Context cntxt;
    RelativeLayout mainHomeView;
    LinearLayout blackFade, catLayout, createLayer;
    EditText pageName;
    Button createPage;
    private int myId;
    int pageType, pageCat = 0;
    String[][] categories;
    String[] category;
    TextView[] textViews;
    boolean categotySelected = false;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    boolean shakeOpt;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_page);
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
        Bundle params = getIntent().getExtras();
        pageType = params.getInt("pageType");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(() -> {
            shakeOpt = sharedPrefMngr.checkShakeOption();
            if(shakeOpt)
                openReportDialog();
        });

        mainHomeView =  findViewById(R.id.mainHomeView);
        blackFade =  findViewById(R.id.blackFade);
        catLayout =  findViewById(R.id.catLayout);
        createLayer =  findViewById(R.id.createLayer);
        pageName =  findViewById(R.id.pageName);
        createPage =  findViewById(R.id.createPage);
        setupUI(mainHomeView);

        categories = new String[][]{
                Constants.pageForLocal,
                Constants.pageForCom,
                Constants.pageForBrnd,
                Constants.pageForArt,
                Constants.pageForEnt,
        };

        if(pageType < 6){
            category = categories[pageType];
            textViews = new TextView[category.length];
            for(int i = 0; i < category.length; i++){
                String cat = category[i];
                @SuppressLint("InflateParams") LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.page_categories, null, false);
                TextView textView =  linearLayout.findViewById(R.id.text);
                textViews[i] = textView;
                textView.setText(cat);
                catLayout.addView(linearLayout);
                int finalI = i;
                textView.setOnClickListener(v -> selectPageCategory(textView, finalI));
            }
        } else categotySelected = true;

        createPage.setOnClickListener(v -> createPager());
        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });

    }

    @SuppressLint("InflateParams")
    private void openReportDialog() {
        RelativeLayout view = (RelativeLayout) getLayoutInflater().inflate(R.layout.report_box, null);
        Button sendBtn =  view.findViewById(R.id.sendBtn);
        EditText reportText =  view.findViewById(R.id.reportText);
        TextView toogle =  view.findViewById(R.id.toogle);
        shakeOpt = sharedPrefMngr.checkShakeOption();
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
        sendBtn.setOnClickListener(v -> {
            String text = reportText.getText().toString();
            if(!StringUtils.isEmpty(text)) {
                try {
                    String actName = cntxt.getClass().getSimpleName();
                    Functions.sendReport(myId, actName, text);
                    Toast.makeText(cntxt, "Report Sent!", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                blackFade.setVisibility(View.GONE);
            }
        });
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        blackFade.addView(view);
        blackFade.setVisibility(View.VISIBLE);

    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void createPager() {
        String pageNamer = pageName.getText().toString();
        if(!categotySelected){
            View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
            if(blackFade.getChildCount() > 0){
                blackFade.removeAllViews();
            }
            TextView txter = blockView.findViewById(R.id.txter);
            Button cnclBtn = blockView.findViewById(R.id.cancel);
            Button agreeBtn = blockView.findViewById(R.id.agree);
            txter.setText("Please Select a Category Type");
            cnclBtn.setVisibility(View.GONE);
            agreeBtn.setText("OK");
            agreeBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
            blackFade.addView(blockView);
            blackFade.setVisibility(View.VISIBLE);
        } else if(StringUtils.isEmpty(pageNamer)){
            View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
            if(blackFade.getChildCount() > 0){
                blackFade.removeAllViews();
            }
            TextView txter = blockView.findViewById(R.id.txter);
            Button cnclBtn = blockView.findViewById(R.id.cancel);
            Button agreeBtn = blockView.findViewById(R.id.agree);
            txter.setText("Please type in Your Page Name");
            cnclBtn.setVisibility(View.GONE);
            agreeBtn.setText("OK");
            agreeBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
            blackFade.addView(blockView);
            blackFade.setVisibility(View.VISIBLE);
        } else {
            createLayer.setVisibility(View.VISIBLE);
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(myId))
                    .addFormDataPart("pageType", String.valueOf(pageType))
                    .addFormDataPart("pageCat", String.valueOf(pageCat))
                    .addFormDataPart("pageName", pageNamer)
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(Constants.createPageUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            try (okhttp3.Response response = okHttpClient.newCall(request).execute()){
                if (response.isSuccessful()) {
                    int pageId = Integer.parseInt(Objects.requireNonNull(response.body()).string());
                    JSONObject object = new JSONObject();
                    object.put("id", String.valueOf(pageId));
                    object.put("pageName", pageNamer);
                    object.put("photo", "/pages/default.png");
                    HomeAct.userPages.put(object);
                    HomeAct.hasPage = true;
                    Intent intent = new Intent(cntxt, PageActivity.class);
                    Bundle params = new Bundle();
                    params.putInt("pageId", pageId);
                    intent.putExtras(params);
                    startActivity(intent);
                    finish();
                    //CreatePageAct.finish();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void selectPageCategory(TextView t, int i) {
        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        int color;
        if(darkThemeEnabled)
            color = R.color.lightRed;
        else
            color = R.color.green;
        categotySelected = true;
        pageCat = i;
        t.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
        t.setTextColor(getResources().getColor(color));
        for(int x = 0; x < textViews.length; x++){
            if(!(x == i)) {
                textViews[x].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_false, 0, 0, 0);
                textViews[x].setTextColor(getResources().getColor(R.color.ashBlack));
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
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
        } else {
            finish();
        }
    }

    private boolean getDefaultDarkThemeEnabled(){
        int defaultThemeMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return defaultThemeMode == Configuration.UI_MODE_NIGHT_YES || defaultThemeMode == Configuration.UI_MODE_NIGHT_MASK;
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean currentTheme = getDefaultDarkThemeEnabled();
        if(!(currentTheme == defaultDarkThemeEnabled)){
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(activity.getPackageName());
            activity.finishAffinity();
            startActivity(new Intent(this, MainActivity.class));
            return;
        }
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }
}
