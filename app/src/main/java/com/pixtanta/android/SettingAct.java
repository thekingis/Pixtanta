package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.pixtanta.android.Utils.CustomEditText;
import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.pixtanta.android.Constants.socketUrl;
import static com.pixtanta.android.Constants.www;

public class SettingAct extends ThemeActivity {

    ProgressBar progressBar;
    LinearLayout blackFade, settingLayer, deactivateLayout, chngeNameLayout, progressLayout, chngePassLayout, toggleSmrtLayout, mentionLayer, curLayout;
    Button editBtn, cancelDeactivation, agreeDeactivation, cancelEditName, cancelEditPass, savePassBtn, cancelEditSmart, saveSmartBtn;
    EditText fName, lName, cPass, oPass, nPass;
    TextView errLog, progTxt, txtNames, errLogx, passwordToggle, smartLoginToggle, smrtLgStatus, resTxt, noBlckList, noMsgBlckList;
    String[] keyArr, ntfKeyArr, pstKeyArr, prefKeyArr;
    boolean sending = false, smartPass = false, scrllVIState = false, smartLogin, defSmart, defLogin, userVerified, shakeOpt;
    int profYes, profNo, reqYes, reqNo, frndYes, frndNo, fllwYes, fllwNo, fllwerYes, fllwerNo, srchYes, srchNo;
    int reqSel, frndSel, fllwSel, fllwerSel, srchSel, profSel, prefSel,  sortSel, tagSel, pshSel, sndSel, rplySel, comSel;
    int[][] prvSetArr, allArrs, ntfArrs, prefArrs, pstArrs;
    int[] profArrs, comArrs, rplyArrs, pshArrs, sndArrs, prefSelArrs, ntfSelArrs, pstSelArrs, reqArrs, frndArrs, fllwArrs, fllwerArrs, srchArrs, selArrs, tagArrs, postPrefArrs, sortPrefArrs;
    Context cntxt;
    CustomEditText curEditor;
    ScrollView scrllView;
    View settingListView;
    private int myId;
    RelativeLayout mainHomeView;
    String myPht, myName, myUserName, lang, tab;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    SharedPrefMngr sharedPrefMngr;
    Socket socket;
    ImageLoader imageLoader;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);
        mainHomeView =  findViewById(R.id.mainHomeView);
        setupUI(mainHomeView);


        myId = sharedPrefMngr.getMyId();
        lang = sharedPrefMngr.getSelectedLanguage();
        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(SettingAct.this, LoginAct.class));
            return;
        }

        try {
            socket = IO.socket(socketUrl);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> socket.emit("connected", myId)));
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        imageLoader = new ImageLoader(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(() -> {
            shakeOpt = sharedPrefMngr.checkShakeOption();
            if(shakeOpt)
                openReportDialog();
        });
        myPht = Constants.www + sharedPrefMngr.getMyPht();
        myName = sharedPrefMngr.getMyName();
        myUserName = sharedPrefMngr.getMyUserName();
        userVerified = sharedPrefMngr.getMyVerification();
        smartLogin = sharedPrefMngr.checkSmartLogin(myId);
        Bundle bundle = getIntent().getExtras();
        tab = bundle.getString("tab");
        blackFade =  findViewById(R.id.blackFade);
        settingLayer =  findViewById(R.id.settingLayer);
        chngePassLayout =  findViewById(R.id.chngePassLayout);
        chngeNameLayout =  findViewById(R.id.chngeNameLayout);
        deactivateLayout =  findViewById(R.id.deactivateLayout);
        progressLayout =  findViewById(R.id.progressLayout);
        toggleSmrtLayout =  findViewById(R.id.toggleSmrtLayout);
        agreeDeactivation =  findViewById(R.id.agreeDeactivation);
        cancelDeactivation =  findViewById(R.id.cancelDeactivation);
        cancelEditName =  findViewById(R.id.cancelEditName);
        editBtn =  findViewById(R.id.editBtn);
        cancelEditPass =  findViewById(R.id.cancelEditPass);
        savePassBtn =  findViewById(R.id.savePassBtn);
        cancelEditSmart =  findViewById(R.id.cancelEditSmart);
        saveSmartBtn =  findViewById(R.id.saveSmartBtn);
        progressBar =  findViewById(R.id.progressBar);
        fName =  findViewById(R.id.fName);
        lName =  findViewById(R.id.lName);
        oPass =  findViewById(R.id.oldPass);
        nPass =  findViewById(R.id.newPass);
        cPass =  findViewById(R.id.cPass);
        errLog =  findViewById(R.id.errLog);
        progTxt =  findViewById(R.id.progTxt);
        errLogx =  findViewById(R.id.errLogx);
        smartLoginToggle =  findViewById(R.id.smartLoginToggle);
        passwordToggle =  findViewById(R.id.passwordToggle);
        prvSetArr = new int[][]{
                new int[]{profNo, profYes},
                new int[]{reqNo, reqYes},
                new int[]{frndYes, frndNo},
                new int[]{fllwYes, fllwNo},
                new int[]{fllwerYes, fllwerNo},
                new int[]{srchYes, srchNo}
        };
        selArrs = new int[] {profSel, reqSel, frndSel, fllwSel, fllwerSel, srchSel};
        ntfSelArrs = new int[] {sndSel, pshSel};
        pstSelArrs = new int[] {comSel, rplySel};
        prefSelArrs = new int[] {prefSel, sortSel};
        profArrs = new int[] {R.id.profNo, R.id.profYes};
        reqArrs = new int[] {R.id.reqNo, R.id.reqYes};
        frndArrs = new int[] {R.id.frndYes, R.id.frndNo};
        fllwArrs = new int[] {R.id.fllwYes, R.id.fllwNo};
        fllwerArrs = new int[] {R.id.fllwerYes, R.id.fllwerNo};
        srchArrs = new int[] {R.id.srchYes, R.id.srchNo};
        tagArrs = new int[] {R.id.tagYes, R.id.tagNo};
        sndArrs = new int[] {R.id.sndYes, R.id.sndNo};
        pshArrs = new int[] {R.id.pshYes, R.id.pshNo};
        comArrs = new int[] {R.id.comEvry, R.id.comFllw};
        rplyArrs = new int[] {R.id.rplyEvry, R.id.rplyFllw};
        postPrefArrs = new int[] {R.id.prefYes, R.id.prefNo};
        sortPrefArrs = new int[] {R.id.oldest, R.id.newest, R.id.relevance};
        prefArrs = new int[][] {postPrefArrs, sortPrefArrs};
        allArrs = new int[][] {profArrs, reqArrs, frndArrs, fllwArrs, fllwerArrs, srchArrs};
        ntfArrs = new int[][] {sndArrs, pshArrs};
        pstArrs = new int[][] {comArrs, rplyArrs};
        keyArr = new String[] {"prof", "req", "frnd", "fllw", "fllwer", "srch"};
        ntfKeyArr = new String[] {"snd", "psh"};
        pstKeyArr = new String[] {"com", "rply"};
        prefKeyArr = new String[] {"pref", "sort"};

        if(smartLogin){
            smartLoginToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
            smartPass = sharedPrefMngr.checkSmartPassOff(myId);
            if(smartPass)
                passwordToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
        }
        defSmart = smartLogin;
        defLogin = smartPass;

        saveSmartBtn.setOnClickListener(v -> {
            defSmart = smartLogin;
            defLogin = smartPass;
            sharedPrefMngr.saveSmartLogin(myId, smartLogin, smartPass);
            toggleSmrtLayout.setVisibility(View.GONE);
            curLayout = null;
            int color, inline;
            String lgTxt;
            boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
            if(!smartLogin) {
                color = R.color.reder;
                inline = R.drawable.ic_offline;
                lgTxt = "Disabled";
                if(darkThemeEnabled)
                    color = R.color.lightRed;
            } else {
                color = R.color.green;
                inline = R.drawable.ic_online;
                lgTxt = "Enabled";
                if(darkThemeEnabled)
                    color = R.color.lightGreen;
            }
            smrtLgStatus.setCompoundDrawablesWithIntrinsicBounds(inline, 0, 0, 0);
            smrtLgStatus.setTextColor(ContextCompat.getColor(cntxt, color));
            if(smartPass)
                lgTxt += " (Password Required)";
            smrtLgStatus.setText(lgTxt);
            lgTxt = "Smart Login " + lgTxt;
            Toast.makeText(SettingAct.this, lgTxt, Toast.LENGTH_LONG).show();
        });
        smartLoginToggle.setOnClickListener(v -> smartLoginMet());
        passwordToggle.setOnClickListener(v -> {
            if(smartLogin){
                smartPassMet();
            }
        });
        agreeDeactivation.setOnClickListener(v -> {
            deactivateLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            curLayout = progressLayout;
            progTxt.setText("Deactivating Your Account");
            sending = true;
            String url = Constants.actionsUrl;
            StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                try {
                    JSONObject actionObj = new JSONObject(response);
                    boolean res = actionObj.getBoolean("data");
                    if(res){
                        deactivate();
                    } else
                        sending = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> sending = false){
                @Override
                protected Map<String, String> getParams() {
                    HashMap<String, String > param = new HashMap<>();
                    String userId = Integer.toString(myId);
                    param.put("user", userId);
                    param.put("lang", lang);
                    param.put("action", "deactivate");
                    return param;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MySingleton.getmInstance(SettingAct.this).addToReqQ(request);
        });
        cancelEditSmart.setOnClickListener(v -> {
            toggleSmrtLayout.setVisibility(View.GONE);
            curLayout = null;
            resetSmartLogin();
        });
        cancelEditName.setOnClickListener(v -> {
            chngeNameLayout.setVisibility(View.GONE);
            curLayout = null;
        });
        cancelDeactivation.setOnClickListener(v -> {
            deactivateLayout.setVisibility(View.GONE);
            curLayout = null;
        });
        cancelEditPass.setOnClickListener(v -> {
            chngePassLayout.setVisibility(View.GONE);
            curLayout = null;
        });
        editBtn.setOnClickListener(v -> {
            errLog.setText("");
            errLog.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            String txtFName = fName.getText().toString();
            String txtLName = lName.getText().toString();
            if(StringUtils.isEmpty(txtFName)){
                errLog.setText("Your First Name cannot be empty");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(StringUtils.isEmpty(txtLName)){
                errLog.setText("Your Last Name cannot be empty");
                errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else {
                saveNameEdit(txtFName, txtLName);
            }
        });
        savePassBtn.setOnClickListener(v -> {
            errLogx.setText("");
            errLogx.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            String newPass = nPass.getText().toString();
            String conPass = cPass.getText().toString();
            String oldPass = oPass.getText().toString();
            if(StringUtils.isEmpty(conPass)){
                errLogx.setText("Your old password cannot be empty");
                errLogx.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(StringUtils.isEmpty(newPass)){
                errLogx.setText("Your new password cannot be empty");
                errLogx.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else if(StringUtils.isEmpty(conPass)){
                errLogx.setText("Please confirm your password");
                errLogx.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
            } else {
                savePassEdit(newPass, conPass, oldPass);
            }
        });
        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });

        getSettings();

        if(tab.equals("block")) {
            socket.on("searchResult", args -> {
                try {
                    JSONObject listObject = new JSONObject(args[0].toString());
                    boolean searchAll = listObject.getBoolean("searchAll");
                    JSONArray listArray = listObject.getJSONArray("data");
                    if (listArray.length() == 0) {
                        runOnUiThread(() -> scrllView.setVisibility(View.GONE));
                        return;
                    }
                    runOnUiThread(() -> {
                        if (mentionLayer.getChildCount() > 0)
                            mentionLayer.removeAllViews();
                    });
                    for (int i = 0; i < listArray.length(); i++) {
                        String data = listArray.getString(i);
                        JSONObject dataObj = new JSONObject(data);
                        int user = dataObj.getInt("user");
                        int msgId = dataObj.getInt("msgId");
                        String name = dataObj.getString("name");
                        String userName = dataObj.getString("userName");
                        String photo = www + dataObj.getString("photo");
                        boolean verified = dataObj.getBoolean("verified");
                        @SuppressLint("InflateParams") LinearLayout listView = (LinearLayout) getLayoutInflater().inflate(R.layout.show_users, null);
                        ImageView imageView = listView.findViewById(R.id.photo);
                        TextView nameTV = listView.findViewById(R.id.name);
                        TextView userNameTV = listView.findViewById(R.id.userName);
                        imageLoader.displayImage(photo, imageView);
                        nameTV.setText(name);
                        userNameTV.setText(userName);
                        if (verified)
                            nameTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user, 0);
                        listView.setOnClickListener(v -> {
                            blockUser(user, name, searchAll, msgId);
                            hideSoftKeyboard(mainHomeView);
                            runOnUiThread(() -> scrllView.setVisibility(View.INVISIBLE));
                        });
                        runOnUiThread(() -> mentionLayer.addView(listView));
                    }
                    runOnUiThread(() -> scrllView.setVisibility(View.VISIBLE));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
            socket.on("blocked", args -> {
                try {
                    JSONArray jsonArray = new JSONArray(args[0].toString());
                    int user = jsonArray.getInt(0);
                    boolean blocking = jsonArray.getBoolean(1);
                    String name = jsonArray.getString(2);
                    String tag = "blck-" + user;
                    String[] names = name.split(" ");
                    String fCharName = names[0];
                    if (!blocking) {
                        LinearLayout linearLayout = settingLayer.findViewWithTag(tag);
                        runOnUiThread(() -> {
                            Toast.makeText(cntxt, "You have unblocked " + fCharName, Toast.LENGTH_LONG).show();
                            if (!(linearLayout == null)) {
                                LinearLayout parentView = (LinearLayout) linearLayout.getParent();
                                parentView.removeView(linearLayout);
                            }
                            blackFade.setVisibility(View.GONE);
                        });
                    } else {
                        @SuppressLint("InflateParams") LinearLayout blockList = (LinearLayout) getLayoutInflater().inflate(R.layout.block_list, null);
                        TextView textView = blockList.findViewById(R.id.textView);
                        Button button = blockList.findViewById(R.id.button);
                        blockList.setTag(tag);
                        textView.setText(name);
                        button.setOnClickListener((v) -> unblockUser("blockUser", user, 0, name));
                        LinearLayout blockLists = settingListView.findViewById(R.id.blockLists);
                        runOnUiThread(() -> {
                            if (!(blockLists == null))
                                blockLists.addView(blockList);
                            Toast.makeText(cntxt, "You have blocked " + fCharName, Toast.LENGTH_LONG).show();
                            blackFade.setVisibility(View.GONE);
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            socket.on("blockMsg", args -> {
                try {
                    JSONArray jsonArray = new JSONArray(args[0].toString());
                    int user = jsonArray.getInt(0);
                    int msgId = jsonArray.getInt(3);
                    boolean blocking = jsonArray.getBoolean(1);
                    String name = jsonArray.getString(2);
                    String tag = "mBlck-" + user;
                    String[] names = name.split(" ");
                    String fCharName = names[0];
                    if (!blocking) {
                        LinearLayout linearLayout = settingLayer.findViewWithTag(tag);
                        runOnUiThread(() -> {
                            Toast.makeText(cntxt, "You have unblocked " + fCharName + " from chatting you", Toast.LENGTH_LONG).show();
                            if (!(linearLayout == null)) {
                                LinearLayout parentView = (LinearLayout) linearLayout.getParent();
                                parentView.removeView(linearLayout);
                            }
                            blackFade.setVisibility(View.GONE);
                        });
                    } else {
                        @SuppressLint("InflateParams") LinearLayout blockList = (LinearLayout) getLayoutInflater().inflate(R.layout.block_list, null);
                        TextView textView = blockList.findViewById(R.id.textView);
                        Button button = blockList.findViewById(R.id.button);
                        blockList.setTag(tag);
                        textView.setText(name);
                        button.setOnClickListener((v) -> unblockUser("blockChat", user, msgId, name));
                        LinearLayout mBlockLists = settingListView.findViewById(R.id.mBlockLists);
                        runOnUiThread(() -> {
                            Toast.makeText(cntxt, "You have blocked " + fCharName + " from chatting you", Toast.LENGTH_LONG).show();
                            if (!(mBlockLists == null))
                                mBlockLists.addView(blockList);
                            blackFade.setVisibility(View.GONE);
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void blockUser(int user, String name, boolean searchAll, int msgId) {
        scrllView.setVisibility(View.GONE);
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String[] names = name.split(" ");
        String fCharName = names[0];
        if(searchAll){
            try {
                JSONObject object = new JSONObject();
                JSONObject blockOpt = new JSONObject();
                object.put("unfollow", "Unfollow " + fCharName);
                object.put("unfriend", "Unfriend " + fCharName);
                object.put("tags", "Remove All Tags in Posts and Comments Between You and " + fCharName);
                object.put("comments", "Delete All Comments in Posts and Comments Between You and " + fCharName);
                object.put("likes", "Delete All Reactions in Posts and Comments Between You and " + fCharName);
                blockOpt.put("unfollow", true);
                blockOpt.put("unfriend", true);
                blockOpt.put("tags", false);
                blockOpt.put("comments", false);
                blockOpt.put("likes", false);
                RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
                LinearLayout optLayer =  postOptView.findViewById(R.id.optLayer);
                optLayer.setOnClickListener((v3) -> {return;});
                JSONArray objKeys = object.names();
                for (int r = 0; r < Objects.requireNonNull(objKeys).length(); r++){
                    int drawableLeft = R.drawable.ic_check_false;
                    String key = objKeys.getString(r);
                    String option = object.getString(key);
                    TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
                    if(key.equals("unfollow") || key.equals("unfriend"))
                        drawableLeft = R.drawable.ic_check_true_red;
                    else
                        optionList.setOnClickListener(v -> {
                            try {
                                TextView textView = (TextView) v;
                                boolean selOpt = blockOpt.optBoolean(key);
                                int dLeft = R.drawable.ic_check_false;
                                if(!selOpt)
                                    dLeft = R.drawable.ic_check_true_red;
                                textView.setCompoundDrawablesWithIntrinsicBounds(dLeft, 0, 0, 0);
                                blockOpt.put(key, !selOpt);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                    optionList.setText(option);
                    optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
                    optLayer.addView(optionList);
                }
                Button button = new Button(cntxt);
                button.setPadding(20, 0, 20, 0);
                button.setBackgroundResource(R.color.colorPrimaryRed);
                button.setTextColor(ContextCompat.getColor(cntxt, R.color.white));
                button.setText("Continue");
                button.setOnClickListener((v) -> {
                    String text = "<span style=\"color:#b00000;\"><b>Blocking " + fCharName + " will delete all activities between the both of you.</b></span><br>";
                    text += "Are you sure you want to block " + fCharName + "?";
                    View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
                    if(blackFade.getChildCount() > 0)
                        blackFade.removeAllViews();
                    TextView txter = blockView.findViewById(R.id.txter);
                    Button cnclBtn = blockView.findViewById(R.id.cancel);
                    Button agreeBtn = blockView.findViewById(R.id.agree);
                    Spanned spanned;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        spanned = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
                    else
                        spanned = Html.fromHtml(text);
                    txter.setText(spanned);
                    agreeBtn.setOnClickListener(v1 -> {
                        try {
                            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.requesting_layout, null);
                            TextView textView =  linearLayout.findViewById(R.id.textView);
                            textView.setText("Blocking...");
                            if(blackFade.getChildCount() > 0)
                                blackFade.removeAllViews();
                            blackFade.addView(linearLayout);
                            blackFade.setVisibility(View.VISIBLE);
                            @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            JSONObject emitObj = new JSONObject();
                            emitObj.put("date", date);
                            emitObj.put("userFrom", myId);
                            emitObj.put("userTo", user);
                            emitObj.put("name", name);
                            emitObj.put("blockOpt", blockOpt);
                            emitObj.put("blocking", true);
                            socket.emit("blockUser", emitObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                    cnclBtn.setOnClickListener(v2 -> blackFade.setVisibility(View.GONE));
                    blackFade.addView(blockView);
                    blackFade.setVisibility(View.VISIBLE);
                });
                optLayer.addView(button);
                blackFade.addView(postOptView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String text = "Are you sure you want to block " + fCharName + " from chatting you?";
            View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
            TextView txter = blockView.findViewById(R.id.txter);
            Button cnclBtn = blockView.findViewById(R.id.cancel);
            Button agreeBtn = blockView.findViewById(R.id.agree);
            txter.setText(text);
            agreeBtn.setOnClickListener(v1 -> {
                try {
                    LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.requesting_layout, null);
                    TextView textView =  linearLayout.findViewById(R.id.textView);
                    textView.setText("Blocking...");
                    if(blackFade.getChildCount() > 0)
                        blackFade.removeAllViews();
                    blackFade.addView(linearLayout);
                    blackFade.setVisibility(View.VISIBLE);
                    @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    JSONObject emitObj = new JSONObject();
                    emitObj.put("userFrom", myId);
                    emitObj.put("userTo", user);
                    emitObj.put("value", false);
                    emitObj.put("msgId", msgId);
                    emitObj.put("date", date);
                    emitObj.put("name", name);
                    socket.emit("blockChat", emitObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            cnclBtn.setOnClickListener(v2 -> blackFade.setVisibility(View.GONE));
            blackFade.addView(blockView);
        }
        blackFade.setVisibility(View.VISIBLE);
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void unblockUser(String emitter, int user, int msgId, String name) {
        String[] names = name.split(" ");
        String fCharName = names[0];
        String text = "Are you sure you want to unblock " + fCharName;
        if(emitter.equals("blockChat"))
            text += " from chatting you";
        text += "?";
        View blockView = LayoutInflater.from(cntxt).inflate(R.layout.request_layer, null, false);
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        TextView txter = blockView.findViewById(R.id.txter);
        Button cnclBtn = blockView.findViewById(R.id.cancel);
        Button agreeBtn = blockView.findViewById(R.id.agree);
        txter.setText(text);
        agreeBtn.setOnClickListener(v1 -> {
            try {
                LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.requesting_layout, null);
                TextView textView =  linearLayout.findViewById(R.id.textView);
                textView.setText("Unblocking...");
                if(blackFade.getChildCount() > 0)
                    blackFade.removeAllViews();
                blackFade.addView(linearLayout);
                blackFade.setVisibility(View.VISIBLE);
                @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                JSONObject emitObj = new JSONObject();
                emitObj.put("userFrom", myId);
                emitObj.put("userTo", user);
                emitObj.put("blocking", false);
                emitObj.put("value", true);
                emitObj.put("msgId", msgId);
                emitObj.put("date", date);
                emitObj.put("name", name);
                socket.emit(emitter, emitObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        cnclBtn.setOnClickListener(v2 -> blackFade.setVisibility(View.GONE));
        blackFade.addView(blockView);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void initializeEditText(CustomEditText customEditText) {
        customEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                curEditor = customEditText;
                if(s.length() > 1){
                    boolean searchAll = curEditor.getSearchType();
                    try {
                        String searchText = s.toString();
                        JSONObject emitObj = new JSONObject();
                        emitObj.put("user", myId);
                        emitObj.put("searchText", searchText);
                        emitObj.put("searchAll", searchAll);
                        socket.emit("searchUser", emitObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else scrllView.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void getSettings(){
        try {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("lang", lang)
                    .addFormDataPart("user", String.valueOf(myId))
                    .addFormDataPart("tab", tab)
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(Constants.settingUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseString = Objects.requireNonNull(response.body()).string();
                progressBar.setVisibility(View.GONE);
                JSONObject settingsObj;
                boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                int color;
                if(darkThemeEnabled)
                    color = R.color.lightRed;
                else
                    color = R.color.green;
                switch (tab){
                    case "general":
                        settingsObj = new JSONObject(responseString);
                        String fstName = settingsObj.getString("fName");
                        String lstName = settingsObj.getString("lName");
                        String names = fstName + " " + lstName;
                        fName.setText(fstName);
                        lName.setText(lstName);
                        settingListView = getLayoutInflater().inflate(R.layout.setting_general, null);
                        txtNames = settingListView.findViewById(R.id.names);
                        txtNames.setText(names);
                        LinearLayout editNames = settingListView.findViewById(R.id.editNames);
                        LinearLayout deactivateAcc = settingListView.findViewById(R.id.deactivateAcc);
                        editNames.setOnClickListener(v -> {
                            chngeNameLayout.setVisibility(View.VISIBLE);
                            curLayout = chngeNameLayout;
                        });
                        deactivateAcc.setOnClickListener(v -> {
                            deactivateLayout.setVisibility(View.VISIBLE);
                            curLayout = deactivateLayout;
                        });
                        break;
                    case "security":
                        settingListView = getLayoutInflater().inflate(R.layout.setting_security, null);
                        LinearLayout editPass = settingListView.findViewById(R.id.editPass);
                        LinearLayout smLogin = settingListView.findViewById(R.id.smLogin);
                        smrtLgStatus = settingListView.findViewById(R.id.smrtLgStatus);
                        updateSLState();
                        editPass.setOnClickListener(v -> {
                            chngePassLayout.setVisibility(View.VISIBLE);
                            curLayout = chngePassLayout;
                        });
                        smLogin.setOnClickListener(v -> {
                            toggleSmrtLayout.setVisibility(View.VISIBLE);
                            curLayout = toggleSmrtLayout;
                        });
                        break;
                    case "privacy":
                        settingsObj = new JSONObject(responseString);
                        settingListView = getLayoutInflater().inflate(R.layout.setting_privacy, null);
                        resTxt = settingListView.findViewById(R.id.resTxt);
                        Button savePrvBtn = settingListView.findViewById(R.id.savePrvBtn);
                        for(int y = 0; y < allArrs.length; y++){
                            String yStr = keyArr[y];
                            final int[] viewArr = allArrs[y];
                            int key1 = settingsObj.getInt(yStr);
                            selArrs[y] = key1;
                            TextView selOpt = settingListView.findViewById(viewArr[key1]);
                            selOpt.setTextColor(ContextCompat.getColor(cntxt, color));
                            selOpt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
                            for(int x = 0; x < viewArr.length; x++){
                                int viewKey = allArrs[y][x];
                                TextView txtView = settingListView.findViewById(viewKey);
                                final int finalY = y;
                                final int finalX = x;
                                txtView.setOnClickListener(v -> selectSettingsOption(allArrs, selArrs, finalY, finalX));
                            }
                        }
                        savePrvBtn.setOnClickListener(v -> saveSettingsOption(keyArr, selArrs));
                        break;
                    case "postPrf":
                        settingsObj = new JSONObject(responseString);
                        settingListView = getLayoutInflater().inflate(R.layout.setting_pref, null);
                        resTxt = settingListView.findViewById(R.id.resTxt);
                        Button savePrfBtn = settingListView.findViewById(R.id.savePrfBtn);
                        for(int y = 0; y < prefArrs.length; y++){
                            String yStr = prefKeyArr[y];
                            final int[] viewArr = prefArrs[y];
                            int key1 = settingsObj.getInt(yStr);
                            prefSelArrs[y] = key1;
                            TextView selOpt = settingListView.findViewById(viewArr[key1]);
                            selOpt.setTextColor(ContextCompat.getColor(cntxt, color));
                            selOpt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
                            for(int x = 0; x < viewArr.length; x++){
                                int viewKey = prefArrs[y][x];
                                TextView txtView = settingListView.findViewById(viewKey);
                                final int finalY = y;
                                final int finalX = x;
                                txtView.setOnClickListener(v -> selectSettingsOption(prefArrs, prefSelArrs, finalY, finalX));
                            }
                        }
                        savePrfBtn.setOnClickListener(v -> saveSettingsOption(prefKeyArr, prefSelArrs));
                        break;
                    case "tandt":
                        settingsObj = new JSONObject(responseString);
                        settingListView = getLayoutInflater().inflate(R.layout.setting_tag, null);
                        resTxt = settingListView.findViewById(R.id.resTxt);
                        Button saveTagBtn = settingListView.findViewById(R.id.saveTagBtn);
                        tagSel = settingsObj.getInt("tag");
                        for (int a = 0; a < tagArrs.length; a++){
                            int viewId = tagArrs[a];
                            TextView selOpt = settingListView.findViewById(viewId);
                            if(a == tagSel){
                                selOpt.setTextColor(ContextCompat.getColor(cntxt, color));
                                selOpt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
                            }
                            final int finalA = a;
                            selOpt.setOnClickListener(v -> {
                                int[][] not = new int[][]{};
                                selectSettingsOption(not, tagArrs, finalA, -1);
                            });
                        }
                        saveTagBtn.setOnClickListener(v -> saveSettingsOption(new String[]{"tag"}, new int[]{tagSel}));
                        break;
                    case "notifications":
                        settingsObj = new JSONObject(responseString);
                        settingListView = getLayoutInflater().inflate(R.layout.setting_notify, null);
                        resTxt = settingListView.findViewById(R.id.resTxt);
                        Button saveNtfBtn = settingListView.findViewById(R.id.saveNtfBtn);
                        for(int k = 0; k < ntfArrs.length; k++){
                            String yStr = ntfKeyArr[k];
                            final int[] viewArr = ntfArrs[k];
                            int key1 = settingsObj.getInt(yStr);
                            ntfSelArrs[k] = key1;
                            TextView selOpt = settingListView.findViewById(viewArr[key1]);
                            selOpt.setTextColor(ContextCompat.getColor(cntxt, color));
                            selOpt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
                            for(int l = 0; l < viewArr.length; l++){
                                int viewKey = ntfArrs[k][l];
                                TextView txtView = settingListView.findViewById(viewKey);
                                final int finalK = k;
                                final int finalL = l;
                                txtView.setOnClickListener(v -> selectSettingsOption(ntfArrs, ntfSelArrs, finalK, finalL));
                            }
                        }
                        saveNtfBtn.setOnClickListener(v -> saveSettingsOption(ntfKeyArr, ntfSelArrs));
                        break;
                    case "postPrvcy":
                        settingsObj = new JSONObject(responseString);
                        settingListView = getLayoutInflater().inflate(R.layout.setting_post, null);
                        resTxt = settingListView.findViewById(R.id.resTxt);
                        Button savePstBtn = settingListView.findViewById(R.id.savePstBtn);
                        for(int m = 0; m < pstArrs.length; m++){
                            String yStr = pstKeyArr[m];
                            final int[] viewArr = pstArrs[m];
                            int key1 = settingsObj.getInt(yStr);
                            pstSelArrs[m] = key1;
                            TextView selOpt = settingListView.findViewById(viewArr[key1]);
                            selOpt.setTextColor(ContextCompat.getColor(cntxt, color));
                            selOpt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
                            for(int n = 0; n < viewArr.length; n++){
                                int viewKey = pstArrs[m][n];
                                TextView txtView = settingListView.findViewById(viewKey);
                                final int finalM = m;
                                final int finalN = n;
                                txtView.setOnClickListener(v -> selectSettingsOption(pstArrs, pstSelArrs, finalM, finalN));
                            }
                        }
                        savePstBtn.setOnClickListener(v -> saveSettingsOption(pstKeyArr, pstSelArrs));
                        break;
                    case "block":
                        settingsObj = new JSONObject(responseString);
                        settingListView = getLayoutInflater().inflate(R.layout.setting_block, null);
                        CustomEditText blockEditText = settingListView.findViewById(R.id.blockEditText);
                        CustomEditText mBlockEditText = settingListView.findViewById(R.id.mBlockEditText);
                        LinearLayout blockLists = settingListView.findViewById(R.id.blockLists);
                        LinearLayout mBlockLists = settingListView.findViewById(R.id.mBlockLists);
                        scrllView = settingListView.findViewById(R.id.scrllView);
                        mentionLayer = settingListView.findViewById(R.id.mentionLayer);
                        JSONArray blockArray = settingsObj.getJSONArray("blocked");
                        JSONArray mBlockArray = settingsObj.getJSONArray("mBlocked");
                        blockEditText.setSearchType(true);
                        mBlockEditText.setSearchType(false);
                        initializeEditText(blockEditText);
                        initializeEditText(mBlockEditText);
                        scrllView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                            if(!(curEditor == null)){
                                int[] location = new int[2];
                                curEditor.getLocationOnScreen(location);
                                int offsetTop = location[1] - 100;
                                int scrllViewHeight = scrllView.getHeight();
                                if(offsetTop < scrllViewHeight)
                                    offsetTop += 170;
                                else
                                    offsetTop -= scrllViewHeight - 120;
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) scrllView.getLayoutParams();
                                layoutParams.setMargins(0, offsetTop, 0, 0);
                                scrllView.setLayoutParams(layoutParams);
                                if(scrllView.getVisibility() == View.VISIBLE && !scrllVIState)
                                    scrllView.scrollTo(0, 0);
                                scrllVIState = scrllView.getVisibility() == View.VISIBLE;
                            }
                        });
                        if(blockArray.length() == 0){
                            noBlckList = new TextView(cntxt);
                            noBlckList.setTextSize(16f);
                            noBlckList.setText("No blocked user");
                            blockLists.addView(noBlckList);
                        } else {
                            for (int i = 0; i < blockArray.length(); i++){
                                JSONObject blockObject = blockArray.getJSONObject(i);
                                int user = blockObject.getInt("user");
                                String name = blockObject.getString("name");
                                String tag = "blck-" + user;
                                LinearLayout blockList = (LinearLayout) getLayoutInflater().inflate(R.layout.block_list, null);
                                TextView textView = blockList.findViewById(R.id.textView);
                                Button button = blockList.findViewById(R.id.button);
                                blockList.setTag(tag);
                                textView.setText(name);
                                button.setOnClickListener((v) -> unblockUser("blockUser", user, 0, name));
                                blockLists.addView(blockList);
                            }
                        }
                        if(mBlockArray.length() == 0){
                            noMsgBlckList = new TextView(cntxt);
                            noMsgBlckList.setTextSize(16f);
                            noMsgBlckList.setText("No blocked user");
                            mBlockLists.addView(noMsgBlckList);
                        } else {
                            for (int x = 0; x < mBlockArray.length(); x++){
                                JSONObject mBlockObject = mBlockArray.getJSONObject(x);
                                int user = mBlockObject.getInt("user");
                                int msgId = mBlockObject.getInt("msgId");
                                String name = mBlockObject.getString("name");
                                String tag = "mBlck-" + user;
                                LinearLayout blockList = (LinearLayout) getLayoutInflater().inflate(R.layout.block_list, null);
                                TextView textView = blockList.findViewById(R.id.textView);
                                Button button = blockList.findViewById(R.id.button);
                                blockList.setTag(tag);
                                textView.setText(name);
                                button.setOnClickListener((v) -> unblockUser("blockChat", user, msgId, name));
                                mBlockLists.addView(blockList);
                            }
                        }
                        break;
                }
                settingLayer.addView(settingListView);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openReportDialog() {
        @SuppressLint("InflateParams") RelativeLayout view = (RelativeLayout) getLayoutInflater().inflate(R.layout.report_box, null);
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

    @SuppressLint("SetTextI18n")
    public void saveNameEdit(final String firstN, final String lastN){
        errLog.setText("");
        errLog.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        chngeNameLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        curLayout = progressLayout;
        progTxt.setText("Saving...");
        sending = true;
        String url = Constants.actionsUrl;
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject actionObj = new JSONObject(response);
                boolean res = actionObj.getBoolean("data");
                if(res){
                    boolean error = actionObj.getBoolean("error");
                    if(!error){
                        progressLayout.setVisibility(View.GONE);
                        curLayout = null;
                        myName = firstN + " " + lastN;
                        HomeAct.myName = myName;
                        txtNames.setText(myName);
                        sharedPrefMngr.storeUserInfo(myId, myPht, myName, myUserName, userVerified);
                        Toast.makeText(SettingAct.this, "Change Successful", Toast.LENGTH_LONG).show();
                    } else {
                        sending = false;
                        String errMsg = actionObj.getString("errMsg");
                        progressLayout.setVisibility(View.GONE);
                        chngeNameLayout.setVisibility(View.VISIBLE);
                        curLayout = chngeNameLayout;
                        errLog.setText(errMsg);
                        errLog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
                    }
                } else
                    sending = false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> sending = false){
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String > param = new HashMap<>();
                String userId = Integer.toString(myId);
                param.put("user", userId);
                param.put("lang", lang);
                param.put("fName", firstN);
                param.put("lName", lastN);
                param.put("action", "updateName");
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(SettingAct.this).addToReqQ(request);
    }

    @SuppressLint("SetTextI18n")
    public void savePassEdit(final String nwPass, final String cnPass, final String olPass){
        errLogx.setText("");
        errLogx.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        chngePassLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        curLayout = progressLayout;
        progTxt.setText("Saving...");
        sending = true;
        String url = Constants.actionsUrl;
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject actionObj = new JSONObject(response);
                boolean res = actionObj.getBoolean("data");
                if(res){
                    boolean error = actionObj.getBoolean("error");
                    if(!error){
                        progressLayout.setVisibility(View.GONE);
                        curLayout = null;
                        oPass.setText("");
                        cPass.setText("");
                        nPass.setText("");
                        Toast.makeText(SettingAct.this, "Password Saved", Toast.LENGTH_LONG).show();
                    } else {
                        sending = false;
                        String errMsg = actionObj.getString("errMsg");
                        progressLayout.setVisibility(View.GONE);
                        chngePassLayout.setVisibility(View.VISIBLE);
                        curLayout = chngePassLayout;
                        errLogx.setText(errMsg);
                        errLogx.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
                    }
                } else
                    sending = false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> sending = false){
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String > param = new HashMap<>();
                String userId = Integer.toString(myId);
                param.put("user", userId);
                param.put("lang", lang);
                param.put("newPass", nwPass);
                param.put("conPass", cnPass);
                param.put("oldPass", olPass);
                param.put("directAccess", "false");
                param.put("action", "changePass");
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(SettingAct.this).addToReqQ(request);
    }

    public void deactivate(){
        sharedPrefMngr.loggedOut();
        startActivity(new Intent(SettingAct.this, LoginAct.class));
        finish();
    }

    public void smartPassMet(){
        if(smartPass){
            passwordToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_false, 0, 0, 0);
            smartPass = false;
        } else {
            passwordToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
            smartPass = true;
        }
    }

    public void smartLoginMet(){
        if(smartLogin){
            smartLoginToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_false, 0, 0, 0);
            passwordToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_false, 0, 0, 0);
            smartLogin = false;
            smartPass = false;
        } else {
            smartLoginToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
            smartLogin = true;
        }
    }

    public void resetSmartLogin(){
        smartLogin = defSmart;
        smartPass = defLogin;
        if(smartLogin){
            smartLoginToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
            if(smartPass)
                passwordToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
            else
                passwordToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_false, 0, 0, 0);
        } else {
            smartLoginToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_false, 0, 0, 0);
            passwordToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_false, 0, 0, 0);
        }
    }

    public void updateSLState(){
        int color, inline;
        String lgTxt;
        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        if(!smartLogin) {
            color = R.color.reder;
            inline = R.drawable.ic_offline;
            lgTxt = "Disabled";
            if(darkThemeEnabled)
                color = R.color.lightRed;
        } else {
            color = R.color.green;
            inline = R.drawable.ic_online;
            lgTxt = "Enabled";
            if(darkThemeEnabled)
                color = R.color.lightGreen;
        }
        smrtLgStatus.setCompoundDrawablesWithIntrinsicBounds(inline, 0, 0, 0);
        smrtLgStatus.setTextColor(ContextCompat.getColor(cntxt, color));
        if(smartPass)
            lgTxt += " (Password Required)";
        smrtLgStatus.setText(lgTxt);
    }

    public void selectSettingsOption(int[][] tangArr, int[] cosArr, int key, int valU){
        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        int color;
        if(darkThemeEnabled)
            color = R.color.lightRed;
        else
            color = R.color.green;
        int oth = 0, val = valU, viewId;
        int[] othViewIds;
        if(valU == -1)
            val = key;
        if(val == oth)
            oth = 1;
        if(valU == -1){
            if(cosArr == tagArrs)
                tagSel = key;
            viewId = cosArr[key];
            othViewIds = new int[]{cosArr[oth]};
        } else {
            int[] othArr = new int[tangArr[key].length - 1];
            int v = 0;
            for (int a = 0; a < tangArr[key].length; a++){
                if(!(a == val)) {
                    othArr[v] = a;
                    v++;
                }
            }
            cosArr[key] = val;
            viewId = tangArr[key][val];
            othViewIds = new int[othArr.length];
            for (int c = 0; c < othArr.length; c++){
                int newKey = othArr[c];
                othViewIds[c] = tangArr[key][newKey];
            }
        }
        TextView selTxtVw = settingListView.findViewById(viewId);
        selTxtVw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_true, 0, 0, 0);
        selTxtVw.setTextColor(ContextCompat.getColor(cntxt, color));
        for (int othViewId : othViewIds) {
            TextView othTxtVw = settingListView.findViewById(othViewId);
            othTxtVw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_false, 0, 0, 0);
            othTxtVw.setTextColor(ContextCompat.getColor(cntxt, R.color.ashBlack));
        }
    }

    @SuppressLint("SetTextI18n")
    public void saveSettingsOption(final String[] strKeys, final int[] strVals){
        resTxt.setText("");
        progTxt.setText("Saving...");
        progressLayout.setVisibility(View.VISIBLE);
        curLayout = progressLayout;
        sending = true;
        boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        int color, tColor;
        if(darkThemeEnabled) {
            color = R.color.lightGreen;
            tColor = R.color.lightRed;
        } else {
            color = R.color.green;
            tColor = R.color.colorPrimaryRed;
        }
        String url = Constants.actionsUrl;
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject actionObj = new JSONObject(response);
                boolean res = actionObj.getBoolean("data");
                if(res){
                    progressLayout.setVisibility(View.GONE);
                    curLayout = null;
                    sending = false;
                    resTxt.setText("Saved");
                    resTxt.setTextColor(ContextCompat.getColor(cntxt, color));
                } else
                    sending = false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            sending = false;
            resTxt.setText("Connection Error");
            resTxt.setTextColor(ContextCompat.getColor(cntxt, tColor));
            progressLayout.setVisibility(View.GONE);
            curLayout = null;
        }){
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String > param = new HashMap<>();
                String userId = Integer.toString(myId);
                param.put("user", userId);
                param.put("lang", lang);
                param.put("action", "prvSet");
                for (int e = 0; e < strKeys.length; e++){
                    String prmKey = strKeys[e];
                    String prmVal = String.valueOf(strVals[e]);
                    param.put(prmKey, prmVal);
                }
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(SettingAct.this).addToReqQ(request);
    }

    public void onBackPressed(){
        if(curLayout == null) {
            if(blackFade.getVisibility() == View.VISIBLE)
                blackFade.setVisibility(View.GONE);
            else
                finish();
        } else {
            if(!sending){
                curLayout.setVisibility(View.GONE);
                curLayout = null;
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
        if (!(view instanceof EditText) && !(view == scrllView)) {
            view.setOnTouchListener((v, event) -> {
                if(!(scrllView == null))
                    scrllView.setVisibility(View.GONE);
                hideSoftKeyboard(v);
                if(curEditor != null && curEditor.hasFocus())
                    curEditor.clearFocus();
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
