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
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pixtanta.android.Adapter.ViewPagerAdapter;
import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.HomeAct.updateMessageDelivery;
import static com.pixtanta.android.InboxFragment.arcScrllVw;
import static com.pixtanta.android.InboxFragment.favScrllVw;
import static com.pixtanta.android.InboxFragment.nestedScrollView;
import static com.pixtanta.android.InboxFragment.openSelected;
import static com.pixtanta.android.InboxFragment.removeDeletedView;
import static com.pixtanta.android.InboxFragment.showSearchHistory;
import static com.pixtanta.android.InboxFragment.switchArchive;
import static com.pixtanta.android.InboxFragment.switchFavourites;

public class InboxAct extends ThemeActivity {

    Context cntxt;
    Activity activity;
    ImageView favourite, archive;
    RelativeLayout mainHomeView;
    @SuppressLint("StaticFieldLeak")
    public static LinearLayout blackFade, searchLayout, searchHistoryLayout;
    LinearLayout viewHolder, searchResult;
    @SuppressLint("StaticFieldLeak")
    public static HorizontalScrollView hrzntlScrllVw;
    CardView searchInbox;
    @SuppressLint("StaticFieldLeak")
    static EditText search;
    static TabLayout tabLayout;
    static ViewPager viewPager;
    @SuppressLint("StaticFieldLeak")
    static NestedScrollView nest;
    int[] tabIcons;
    int viewHolderH, tabH, blash, red;
    public static int myId, user, height;
    static String lang, fCharName, pronoun, myIDtoString;
    public static boolean aBoolean;
    ImageLoader imageLoader;
    static Socket socket;
    ActiveFragment activeFragment;
    InboxFragment inboxFragment;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    boolean shakeOpt;
    DisplayMetrics displayMetrics;
    static JSONObject iconObj, options, optionVals;
    public static JSONObject updatedBlocks, updatedSounds, updatedOffs, updatedFavs, updatedArchs;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        sharedPrefMngr = new SharedPrefMngr(this);
		
        lang = sharedPrefMngr.getSelectedLanguage();
        cntxt = this;
        activity = this;
        iconObj = new JSONObject();
        updatedBlocks = new JSONObject();
        updatedSounds = new JSONObject();
        updatedFavs = new JSONObject();
        updatedOffs = new JSONObject();
        updatedArchs = new JSONObject();
        aBoolean = false;
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(this, LoginAct.class));
            return;
        }
        myId = sharedPrefMngr.getMyId();
        myIDtoString = Integer.toString(myId);
        imageLoader = new ImageLoader(this);

        try {
            socket = IO.socket(socketUrl);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> socket.emit("connected", myId)));
            socket.connect();
            iconObj.put("sound", R.drawable.ic_notify);
            iconObj.put("online", R.drawable.ic_power);
            iconObj.put("favourite", R.drawable.ic_star_black);
            iconObj.put("archive", R.drawable.ic_archive_black);
            iconObj.put("report", R.drawable.ic_report_post);
            iconObj.put("deleteChat", R.drawable.ic_delete);
            iconObj.put("blockChat", R.drawable.ic_block);
        } catch (URISyntaxException | JSONException e) {
            e.printStackTrace();
        }
        updateMessageDelivery(myId);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(() -> {
            shakeOpt = sharedPrefMngr.checkShakeOption();
            if(shakeOpt)
                openReportDialog();
        });
        tabIcons = new int[]{
                R.drawable.ic_inbox,
                R.drawable.ic_active
        };

        mainHomeView = findViewById(R.id.mainHomeView);
        viewHolder = findViewById(R.id.viewHolder);
        blackFade = findViewById(R.id.blackFade);
        searchLayout = findViewById(R.id.searchLayout);
        searchHistoryLayout = findViewById(R.id.searchHistoryLayout);
        searchResult = findViewById(R.id.searchResult);
        search = findViewById(R.id.search);
        hrzntlScrllVw = findViewById(R.id.hrzntlScrllVw);
        nest = findViewById(R.id.nest);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        favourite = findViewById(R.id.favourite);
        searchInbox = findViewById(R.id.searchInbox);
        archive = findViewById(R.id.archive);
        tabLayout.post(() -> {
            tabH = tabLayout.getHeight();
            viewHolderH = mainHomeView.getHeight() - tabH;
            ViewGroup.LayoutParams params = viewPager.getLayoutParams();
            params.height = viewHolderH;
            viewPager.setLayoutParams(params);
        });
        searchInbox.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryRed));
        favourite.setOnClickListener(v -> openSelected(cntxt, 0, R.drawable.ic_star, "Favourites", favScrllVw));
        archive.setOnClickListener(v -> openSelected(cntxt, 0, R.drawable.ic_archive, "Archives", arcScrllVw));
        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });
        searchInbox.setOnClickListener(v -> {
            if(searchResult.getChildCount() > 0)
                searchResult.removeAllViews();
            search.requestFocus();
            searchLayout.setVisibility(View.VISIBLE);
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString();
                if(!StringUtils.isEmpty(searchText)){
                    performSearch(searchText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String searchText = search.getText().toString();
                if(!StringUtils.isEmpty(searchText))
                    performSearch(searchText);
                return true;
            }
            return false;
        });

        tabLayout.setupWithViewPager(viewPager);
        setupViewPager();
        setupTabIcons();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                if(darkThemeEnabled)
                    red = ContextCompat.getColor(cntxt, R.color.lightRed);
                else
                    red = ContextCompat.getColor(cntxt, R.color.colorPrimaryRed);
                Objects.requireNonNull(tab.getIcon()).setColorFilter(red, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                if(darkThemeEnabled)
                    blash = ContextCompat.getColor(cntxt, R.color.ash);
                else
                    blash = ContextCompat.getColor(cntxt, R.color.blash);
                Objects.requireNonNull(tab.getIcon()).setColorFilter(blash, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nest.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int gH = viewHolder.getHeight() - nest.getHeight();
                aBoolean = gH == scrollY;
            });
        }
        socket.on("addToFriend", args -> {
            String theUser = args[0].toString();
            socket.emit("addToFriend", theUser);
        });
        socket.on("removeFriend", args -> {
            String theUser = args[0].toString();
            socket.emit("removeFriend", theUser);
        });

    }

    private void performSearch(String searchText) {
        if(searchResult.getChildCount() > 0)
            searchResult.removeAllViews();
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("category", "inbox")
                .addFormDataPart("searchText", searchText)
                .addFormDataPart("user", String.valueOf(myId))
                .build();
        Request request = new Request.Builder()
                .url(Constants.searchUrl)
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        try(Response response = call.execute()) {
            if (response.isSuccessful()) {
                String searchData = Objects.requireNonNull(response.body()).string();
                JSONObject jObject = new JSONObject(searchData);
                JSONArray jsonArray = jObject.getJSONArray("data");
                if(jsonArray.length() > 0){
                    for (int i = 0; i < jsonArray.length(); i++){
                        String searchStr = jsonArray.getString(i);
                        JSONObject jsonObject = new JSONObject(searchStr);
                        int user = jsonObject.getInt("user");
                        String photo = www + jsonObject.getString("photo");
                        String name = jsonObject.getString("name");
                        String userName = jsonObject.getString("userName");
                        String Fname = jsonObject.getString("fCharName");
                        boolean verified = jsonObject.getBoolean("verified");
                        @SuppressLint("InflateParams") LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.search_lists, null);
                        LinearLayout layout = linearLayout.findViewById(R.id.layout);
                        ImageView imageView = linearLayout.findViewById(R.id.photo);
                        TextView textView = linearLayout.findViewById(R.id.name);
                        TextView textViewUN = linearLayout.findViewById(R.id.userName);
                        imageLoader.displayImage(photo, imageView);
                        textView.setText(name);
                        textViewUN.setText(userName);
                        if(verified)
                            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user, 0);
                        layout.setOnClickListener(v -> {
                            try {
                                saveSearch(cntxt, activity, user, Fname, jsonObject.getString("photo"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                        searchResult.addView(linearLayout);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveSearch(Context cntxt, Activity activity, int user, String name, String photo) throws JSONException {
        hrzntlScrllVw.setVisibility(View.VISIBLE);
        Intent intent = new Intent(cntxt, MessageAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("user", user);
        intent.putExtras(userParams);
        cntxt.startActivity(intent);
        search.setText("");
        searchLayout.setVisibility(View.GONE);
        JSONObject emitObj = new JSONObject();
        emitObj.put("user", myId);
        emitObj.put("category", "inbox");
        emitObj.put("type", "profile");
        emitObj.put("dataId", user);
        emitObj.put("txt", "");
        emitObj.put("count", 10);
        socket.emit("saveSearchHistory", emitObj);
        JSONObject object = new JSONObject();
        object.put("user", user);
        object.put("name", name);
        object.put("photo", photo);
        showSearchHistory(cntxt, activity, object, 0);
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

    private void setupViewPager() {
        viewPager.setOffscreenPageLimit(3);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        inboxFragment = new InboxFragment(cntxt, activity);
        activeFragment = new ActiveFragment(cntxt);
        adapter.addFragment(inboxFragment, "Inbox");
        adapter.addFragment(activeFragment, "Active");
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        for(int x = 0; x < tabIcons.length; x++){
            Objects.requireNonNull(tabLayout.getTabAt(x)).setIcon(tabIcons[x]);
        }
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        if(darkThemeEnabled)
            red = ContextCompat.getColor(cntxt, R.color.lightRed);
        else
            red = ContextCompat.getColor(cntxt, R.color.colorPrimaryRed);
        if (tab != null)
            Objects.requireNonNull(tab.getIcon()).setColorFilter(red, PorterDuff.Mode.SRC_IN);
    }

    public static void changeTabTitle(Context context, int tabIndex, int icon, String title){
        TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
        SharedPrefMngr sharedPrefMngr = new SharedPrefMngr(context);
        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        int red;
        if(darkThemeEnabled)
            red = ContextCompat.getColor(context, R.color.lightRed);
        else
            red = ContextCompat.getColor(context, R.color.colorPrimaryRed);
        Objects.requireNonNull(tab).setIcon(icon);
        Objects.requireNonNull(tab).setText(title);
        Objects.requireNonNull(tab.getIcon()).setColorFilter(red, PorterDuff.Mode.SRC_IN);
        viewPager.setCurrentItem(tabIndex, true);
    }

    public void showCardViews(){
        favourite.setVisibility(View.VISIBLE);
        searchInbox.setVisibility(View.VISIBLE);
        archive.setVisibility(View.VISIBLE);
    }

    @SuppressLint("InflateParams")
    public static void showMessageOptions(Context cntxt, Activity activity, String msgId, int userX, String name) throws JSONException {
        user = userX;
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        LayoutInflater inflater = activity.getLayoutInflater();
        RelativeLayout postOptView = (RelativeLayout) inflater.inflate(R.layout.post_options, null);
        LinearLayout optLayer = postOptView.findViewById(R.id.optLayer);
        JSONObject jsonObject = SaveOpenedMessages.messageArray.getJSONObject(2);
        JSONObject object = jsonObject.getJSONObject(msgId);
        fCharName = object.getString("fCharName");
        pronoun = object.getString("pronoun");
        options = new JSONObject(object.getString("options"));
        optionVals = new JSONObject(object.getString("optionVals"));
        JSONArray objKeys = options.names();
        for (int r = 0; r < Objects.requireNonNull(objKeys).length(); r++){
            TextView optionList = (TextView) inflater.inflate(R.layout.options_list, null);
            String key = objKeys.getString(r);
            String option = options.getString(key);
            int drawableLeft = iconObj.getInt(key);
            optionList.setText(option);
            optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
            optionList.setOnClickListener(v -> {
                try {
                    executeMessageOptions(cntxt, activity, msgId, key, name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            optLayer.addView(optionList);
        }
        blackFade.addView(postOptView);
        blackFade.setVisibility(View.VISIBLE);
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private static void executeMessageOptions(Context cntxt, Activity activity, String msgId, String key, String name) throws JSONException {
        JSONObject jsonObject = SaveOpenedMessages.messageArray.getJSONObject(2);
        JSONObject object = jsonObject.getJSONObject(msgId);
        LayoutInflater inflater = activity.getLayoutInflater();
        blackFade.setVisibility(View.GONE);
        String toastText = null, text = null;
        JSONObject emitObj = new JSONObject();
        boolean value = optionVals.getBoolean(key);
        switch (key){
            case "sound":
                toastText = "Chat Sound Enabled for " + fCharName;
                text = "Disable Chat Sound For " + fCharName;
                if(value) {
                    toastText = "Chat Sound Disabled for " + fCharName;
                    text = "Enable Chat Sound For " + fCharName;
                }
                emitObj.put("userFrom", myId);
                emitObj.put("userTo", user);
                emitObj.put("value", value);
                emitObj.put("msgId", Integer.valueOf(msgId));
                updatedSounds.put(msgId, emitObj);
                break;
            case "online":
                toastText = "You are offline for " + fCharName;
                text = "Go Online For " + fCharName;
                if(value) {
                    toastText = "You are online for " + fCharName;
                    text = "Go Offline For " + fCharName;
                }
                emitObj.put("userFrom", myId);
                emitObj.put("userTo", user);
                emitObj.put("value", value);
                emitObj.put("msgId", Integer.valueOf(msgId));
                updatedOffs.put(msgId, emitObj);
                break;
            case "favourite":
                toastText = "Marked as Favourite";
                text = "Remove from Favourites";
                if(value) {
                    toastText = "Removed from favourites";
                    text = "Mark as Favourite";
                }
                emitObj.put("userFrom", myId);
                emitObj.put("msgId", msgId);
                emitObj.put("value", value);
                updatedFavs.put(msgId, emitObj);
                switchFavourites(cntxt, activity,msgId, value);
                break;
            case "archive":
                toastText = "Moved to Archive";
                text = "Remove from Archive";
                if(value) {
                    toastText = "Removed from Archive";
                    text = "Move to Archive";
                }
                emitObj.put("userFrom", myId);
                emitObj.put("msgId", msgId);
                emitObj.put("value", value);
                updatedArchs.put(msgId, emitObj);
                switchArchive(cntxt, activity, msgId, value, true);
                break;
            case "report":
                String[] opts = new String[]{
                        fCharName + " is feeling depressed and might hurt " + pronoun,
                        fCharName + " is sending suspicious links",
                        fCharName + " is sending sexual explicit contents",
                        fCharName + " is sending insulting and abusive words",
                        "I don't want to chat with " + fCharName
                };
                RelativeLayout postOptView = (RelativeLayout) inflater.inflate(R.layout.post_options, null);
                LinearLayout optLayer = postOptView.findViewById(R.id.optLayer);
                LinearLayout tvw = (LinearLayout) inflater.inflate(R.layout.text_view, null);
                TextView txtHead = tvw.findViewById(R.id.head);
                TextView txtBody = tvw.findViewById(R.id.body);
                txtHead.setText("Help Us Understand What's Happening");
                txtBody.setText("Why do you want to report this conversation?");
                optLayer.addView(tvw);
                for (int r = 0; r < opts.length; r++){
                    TextView optionList = (TextView) inflater.inflate(R.layout.options_list, null);
                    String option = opts[r];
                    optionList.setText(option);
                    int finalR = r;
                    optionList.setOnClickListener(v -> {
                        blackFade.setVisibility(View.GONE);
                        Toast.makeText(cntxt, "Report Submitted", Toast.LENGTH_LONG).show();
                        try {
                            @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            emitObj.put("date", date);
                            emitObj.put("user", myId);
                            emitObj.put("dataId", user);
                            emitObj.put("type", "message");
                            emitObj.put("reportIndex", finalR);
                            socket.emit("report", emitObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                    optLayer.addView(optionList);
                }
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                blackFade.addView(postOptView);
                blackFade.setVisibility(View.VISIBLE);
                break;
            case "deleteChat":
                View blockViewX = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                TextView txterX = blockViewX.findViewById(R.id.txter);
                Button cnclBtnX = blockViewX.findViewById(R.id.cancel);
                Button agreeBtnX = blockViewX.findViewById(R.id.agree);
                txterX.setText("Deleting this chat is irreversible.\n\nAre you sure you want to delete this Chat?");
                agreeBtnX.setOnClickListener(v -> {
                    try {
                        removeDeletedView(Integer.parseInt(msgId));
                        blackFade.setVisibility(View.GONE);
                        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        emitObj.put("date", date);
                        emitObj.put("user", myId);
                        emitObj.put("msgId", msgId);
                        socket.emit("deleteChat", emitObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                cnclBtnX.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
                blackFade.addView(blockViewX);
                blackFade.setVisibility(View.VISIBLE);
                break;
            case "blockChat":
                View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
                if(blackFade.getChildCount() > 0){
                    blackFade.removeAllViews();
                }
                TextView txter = blockView.findViewById(R.id.txter);
                Button cnclBtn = blockView.findViewById(R.id.cancel);
                Button agreeBtn = blockView.findViewById(R.id.agree);
                String txt = "Are you sure you want to block " + fCharName + " from chatting You?";
                if(value)
                    txt = "Are you sure you want to unblock " + fCharName + " from chatting You?";
                txter.setText(txt);
                agreeBtn.setOnClickListener(v -> {
                    blackFade.setVisibility(View.GONE);
                    try {
                        String toastText1 = "You have blocked " + fCharName;
                        String text1 = "Unblock Messages from " + fCharName;
                        if(value) {
                            toastText1 = "You have unblocked " + fCharName;
                            text1 = "Block Messages from " + fCharName;
                        }
                        Toast.makeText(cntxt, toastText1, Toast.LENGTH_LONG).show();
                        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        emitObj.put("date", date);
                        emitObj.put("userFrom", myId);
                        emitObj.put("userTo", user);
                        emitObj.put("msgId", msgId);
                        emitObj.put("value", value);
                        emitObj.put("name", name);
                        socket.emit("blockChat", emitObj);
                        options.put(key, text1);
                        String newJsonObj = options.toString();
                        object.put("options", newJsonObj);
                        optionVals.put(key, !value);
                        String newJsonObjX = optionVals.toString();
                        object.put("optionVals", newJsonObjX);
                        jsonObject.put(msgId, object);
                        SaveOpenedMessages.messageArray.put(2, jsonObject);
                        updatedBlocks.put(msgId, emitObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                cnclBtn.setOnClickListener(v -> blackFade.setVisibility(View.GONE));
                blackFade.addView(blockView);
                blackFade.setVisibility(View.VISIBLE);
                break;
        }
        if(emitObj.length() > 0)
            socket.emit(key, emitObj);
        if(!(toastText == null))
            Toast.makeText(cntxt, toastText, Toast.LENGTH_LONG).show();
        if(!(text == null)) {
            options.put(key, text);
            String newJsonObj = options.toString();
            object.put("options", newJsonObj);
        }
        if(!(key.equals("blockChat"))){
            optionVals.put(key, !value);
            String newJsonObj = optionVals.toString();
            object.put("optionVals", newJsonObj);
        }
        jsonObject.put(msgId, object);
        SaveOpenedMessages.messageArray.put(2, jsonObject);
    }

    public static void setRectors(String msgId, boolean value, String key) throws JSONException {
        JSONObject jsonObject = SaveOpenedMessages.messageArray.getJSONObject(2);
        JSONObject object = jsonObject.getJSONObject(msgId);
        String XfCharName = object.getString("fCharName");
        JSONObject Xoptions = new JSONObject(object.getString("options"));
        JSONObject XoptionVals = new JSONObject(object.getString("optionVals"));
        String text = null;
        switch (key){
            case "sound":
                text = "Disable Chat Sound For " + XfCharName;
                if(value)
                    text = "Enable Chat Sound For " + XfCharName;
                break;
            case "online":
                text = "Go Online For " + XfCharName;
                if(value)
                    text = "Go Offline For " + XfCharName;
                break;
            case "favourite":
                text = "Remove from Favourites";
                if(value)
                    text = "Mark as Favourite";
                break;
            case "archive":
                text = "Remove from Archive";
                if(value)
                    text = "Move to Archive";
                break;
            case "blockChat":
                text = "Unblock Messages from " + XfCharName;
                if(value)
                    text = "Block Messages from " + XfCharName;
                break;
        }
        Xoptions.put(key, text);
        String newJsonObj = Xoptions.toString();
        object.put("options", newJsonObj);
        XoptionVals.put(key, !value);
        String newJsonObjX = XoptionVals.toString();
        object.put("optionVals", newJsonObjX);
        jsonObject.put(msgId, object);
        SaveOpenedMessages.messageArray.put(2, jsonObject);
    }

    public void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
        } else if(searchLayout.getVisibility() == View.VISIBLE){
            searchLayout.setVisibility(View.GONE);
        } else if(viewPager.getCurrentItem() > 0){
            viewPager.setCurrentItem(0, true);
        } else {
            if(!(nestedScrollView == null)) {
                nestedScrollView.setVisibility(View.GONE);
                nestedScrollView = null;
                changeTabTitle(cntxt, 0, R.drawable.ic_inbox, "Inbox");
            } else {
                socket.emit("removeInboxPage", myId);
                socket.emit("disconnected", myId);
                finish();
            }
        }
    }

    public static void hideSoftKeyboard(Context cntxt, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)cntxt.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void setupUI(Context cntxt, View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                /*if(event.getAction() == MotionEvent.ACTION_UP)
                    releasePress();*/
                hideSoftKeyboard(cntxt, v);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(cntxt, innerView);
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
