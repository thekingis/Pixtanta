package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pixtanta.android.Adapter.ViewPagerAdapter;
import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.socketUrl;

public class SearchActivity extends ThemeActivity {

    public static int myId;
    Context cntxt;
    public static String searchText;
    RelativeLayout mainHomeView;
    EditText search;
    LinearLayout layout, blackFade, loadingLayer;
    TabLayout tabLayout;
    ViewPager viewPager;
    int viewPagerPosition;
    AllFragment allFragment;
    PagesFragment pagesFragment;
    PeopleFragment peopleFragment;
    PostsFragment postsFragment;
    boolean loading = true, shakeOpt, changedSearch = true;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    SharedPrefMngr sharedPrefMngr;
    String[] categories = new String[]{"search", "people", "pages", "posts"};
    static Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(this, LoginAct.class));
            return;
        }

        Bundle bundle = getIntent().getExtras();
        searchText = bundle.getString("word");
        viewPagerPosition = bundle.getInt("position");
        myId = sharedPrefMngr.getMyId();

        try {
            socket = IO.socket(socketUrl);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> socket.emit("connected", myId)));
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(() -> {
            shakeOpt = sharedPrefMngr.checkShakeOption();
            if(shakeOpt)
                openReportDialog();
        });

        mainHomeView = findViewById(R.id.mainHomeView);
        search = findViewById(R.id.search);
        loadingLayer = findViewById(R.id.loadingLayer);
        layout = findViewById(R.id.layout);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        blackFade = findViewById(R.id.blackFade);

        search.setText(searchText);
        search.setSelection(searchText.length());
        tabLayout.setupWithViewPager(viewPager);
        search.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_UP && keyCode == 66)
                performSearch();
            return false;
        });

        allFragment = new AllFragment(cntxt);
        pagesFragment = new PagesFragment(cntxt);
        peopleFragment = new PeopleFragment(cntxt);
        postsFragment = new PostsFragment(cntxt);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(allFragment, "All");
        adapter.addFragment(peopleFragment, "People");
        adapter.addFragment(pagesFragment, "Pages");
        adapter.addFragment(postsFragment, "Posts");
        viewPager.setAdapter(adapter);

        new android.os.Handler().postDelayed(this::getSearchResults, 1000);
        setupUI(mainHomeView);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(!loading)
                    viewPagerPosition = position;
                boolean[] searches = new boolean[]{AllFragment.searchedAlF, PeopleFragment.searchedPeF, PagesFragment.searchedPaF, PostsFragment.searchedPoF};
                boolean searched = searches[position];
                if(changedSearch && !searched){
                    switch(position){
                        case 0:
                            AllFragment.getSearchResultsAlF(cntxt);
                            break;
                        case 1:
                            PeopleFragment.getSearchResultsPeF(cntxt);
                            break;
                        case 2:
                            PagesFragment.getSearchResultsPaF(cntxt);
                            break;
                        case 3:
                            PostsFragment.getSearchResultsPoF(cntxt);
                            break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void getSearchResults() {
        loading = true;
        while (loading){
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(myId))
                    .addFormDataPart("searchText", searchText)
                    .addFormDataPart("category", categories[viewPagerPosition])
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.searchUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    loading = false;
                    loadingLayer.setVisibility(View.GONE);
                    layout.setVisibility(View.VISIBLE);
                    JSONObject jsonObject = new JSONObject(responseString);
                    boolean allLoaded = jsonObject.getBoolean("allLoaded");
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    if(!(viewPager.getCurrentItem() == viewPagerPosition))
                        viewPager.setCurrentItem(viewPagerPosition, false);
                    switch (viewPagerPosition){
                        case 0:
                            AllFragment.displayResultAlF(cntxt, jsonArray, true);
                            AllFragment.searchedAlF = true;
                            AllFragment.allLoaded = allLoaded;
                            break;
                        case 1:
                            PeopleFragment.displayResultPeF(cntxt, jsonArray, true);
                            PeopleFragment.searchedPeF = true;
                            PeopleFragment.allLoaded = allLoaded;
                            break;
                        case 2:
                            PagesFragment.displayResultPaF(cntxt, jsonArray, true);
                            PagesFragment.searchedPaF = true;
                            PagesFragment.allLoaded = allLoaded;
                            break;
                        case 3:
                            PostsFragment.displayResultPoF(cntxt, jsonArray, true);
                            PostsFragment.searchedPoF = true;
                            PostsFragment.allLoaded = allLoaded;
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void performSearch() {
        String searchTxt = search.getText().toString();
        if(!StringUtils.isEmpty(searchTxt) && !searchText.equals(searchTxt)){
            searchText = searchTxt;
            changedSearch = true;
            AllFragment.searchedAlF = false;
            PeopleFragment.searchedPeF = false;
            PagesFragment.searchedPaF = false;
            PostsFragment.searchedPoF = false;
            AllFragment.allLoaded = false;
            PeopleFragment.allLoaded = false;
            PagesFragment.allLoaded = false;
            PostsFragment.allLoaded = false;
            AllFragment.jArrayAlF = new JSONArray();
            PeopleFragment.jArrayPeF = new JSONArray();
            PagesFragment.jArrayPaF = new JSONArray();
            PostsFragment.jArrayPoF = new JSONArray();
            layout.setVisibility(View.GONE);
            loadingLayer.setVisibility(View.VISIBLE);
            getSearchResults();
        }
    }

    public static void saveSearch(Context context, JSONObject object) throws JSONException {
        int dataId = object.getInt("dataId");
        String type = object.getString("type");
        String txt = object.getString("txt");
        JSONObject emitObj = new JSONObject();
        emitObj.put("user", myId);
        emitObj.put("category", "search");
        emitObj.put("type", type);
        emitObj.put("dataId", dataId);
        emitObj.put("txt", txt);
        emitObj.put("count", 20);
        socket.emit("saveSearchHistory", emitObj);
        if(type.equals("page"))
            visitPage(context, dataId);
        else
            visitUserProfile(context, dataId);
    }

    private void openReportDialog() {
        @SuppressLint("InflateParams") RelativeLayout view = (RelativeLayout) getLayoutInflater().inflate(R.layout.report_box, null);
        Button sendBtn = view.findViewById(R.id.sendBtn);
        EditText reportText = view.findViewById(R.id.reportText);
        TextView toogle = view.findViewById(R.id.toogle);
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

    public static void visitUserProfile(Context cntxt, int user) {
        Intent intent = new Intent(cntxt, ProfileAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("userID", user);
        intent.putExtras(userParams);
        cntxt.startActivity(intent);
    }

    public static void visitPage(Context cntxt, int pageId) {
        Intent intent = new Intent(cntxt, PageActivity.class);
        Bundle pageParams = new Bundle();
        pageParams.putInt("pageId", pageId);
        intent.putExtras(pageParams);
        cntxt.startActivity(intent);
    }

    public void onBackPressed(){
        AllFragment.searchedAlF = false;
        PeopleFragment.searchedPeF = false;
        PagesFragment.searchedPaF = false;
        PostsFragment.searchedPoF = false;
        AllFragment.loadingAlF = false;
        PeopleFragment.loadingPeF = false;
        PagesFragment.loadingPaF = false;
        PostsFragment.loadingPoF = false;
        AllFragment.allLoaded = false;
        PeopleFragment.allLoaded = false;
        PagesFragment.allLoaded = false;
        PostsFragment.allLoaded = false;
        AllFragment.jArrayAlF = null;
        PeopleFragment.jArrayPeF = null;
        PagesFragment.jArrayPaF = null;
        PostsFragment.jArrayPoF = null;
        finish();
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) cntxt.getSystemService(Activity.INPUT_METHOD_SERVICE);
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