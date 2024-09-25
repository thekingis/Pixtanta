package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class ActsActivity extends ThemeActivity {

    Context context;
    DisplayMetrics displayMetrics;
    static Socket socket;
    ProgressBar progressBar;
    LinearLayout linearLayout, blackFade;
    ScrollView scrllVw;
    boolean loadingPost = true, allLoaded = false;
    ArrayList<String> selectedDatas = new ArrayList<>();
    ImageLoader imageLoader;
    int myId;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    boolean shakeOpt;
    SharedPrefMngr sharedPrefMngr;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acts);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = this;

        sharedPrefMngr = new SharedPrefMngr(this);
        sharedPrefMngr.initializeSmartLogin();
        myId = sharedPrefMngr.getMyId();
        imageLoader = new ImageLoader(this);

        try {
            socket = IO.socket(socketUrl);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> socket.emit("connected", myId)));
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(() -> {
            shakeOpt = sharedPrefMngr.checkShakeOption();
            if(shakeOpt)
                openReportDialog();
        });

        progressBar = findViewById(R.id.progressBar);
        linearLayout = findViewById(R.id.layout);
        blackFade = findViewById(R.id.blackFade);
        scrllVw = findViewById(R.id.scrllView);

        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });

        getActivities();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrllVw.setOnScrollChangeListener((scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int scrollH = linearLayout.getHeight() - scrllVw.getHeight() - 200;
                if(scrollY > scrollH && !loadingPost && !allLoaded){
                    loadingPost = true;
                    progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.image_loader, null);
                    linearLayout.addView(progressBar);
                    progressBar.post(this::getActivities);
                }
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
                    String actName = context.getClass().getSimpleName();
                    Functions.sendReport(myId, actName, text);
                    Toast.makeText(context, "Report Sent!", Toast.LENGTH_SHORT).show();
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

    private void getActivities(){
        loadingPost = true;
        while (loadingPost){
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(myId))
                    .addFormDataPart("selectedDatas", selectedDatas.toString().intern())
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.activityLogUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    loadingPost = false;
                    progressBar.setVisibility(View.GONE);
                    JSONObject responseObj = new JSONObject(responseString);
                    allLoaded = responseObj.getBoolean("allLoaded");
                    JSONArray array = responseObj.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        String id = object.getString("id");
                        int actId = object.getInt("actId");
                        int dataId = object.getInt("dataId");
                        int extraId = object.getInt("extraId");
                        String paramStr = object.getString("paramStr");
                        String note = object.getString("note");
                        String date = object.getString("date");
                        selectedDatas.add(id);
                        int paramId = 0;
                        if(!StringUtils.isEmpty(paramStr))
                            paramId = Integer.parseInt(paramStr);
                        @SuppressLint("InflateParams") LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.act_list, null);
                        //ImageView iconView = layout.findViewById(R.id.iconView);
                        TextView noteText = layout.findViewById(R.id.noteText);
                        TextView noteDate = layout.findViewById(R.id.noteDate);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            noteText.setText(Html.fromHtml(note, Html.FROM_HTML_MODE_COMPACT));
                        else
                            noteText.setText(Html.fromHtml(note));
                        noteDate.setText(date);
                        int finalParamId = paramId;
                        layout.setOnClickListener(v -> {
                            try {
                                validateOption(actId, finalParamId, extraId, dataId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                        linearLayout.addView(layout);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void validateOption(int actId, int paramId, int extraId, int dataId) throws JSONException {
        if(!(paramId == 0)){
            Class activityClass = null;
            Bundle param = new Bundle();
            boolean emitter = false;
            String emitStr = null;
            String date;
            JSONObject object = new JSONObject();
            switch (actId){
                case 0:
                case 3:
                case 6:
                case 7:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                    activityClass = PostDisplayAct.class;
                    param.putString("postId", String.valueOf(paramId));
                    break;
                case 1:
                case 4:
                case 8:
                case 9:
                case 21:
                    activityClass = CommentsActivity.class;
                    param.putString("postID", String.valueOf(paramId));
                    param.putString("commentID", String.valueOf(extraId));
                    break;
                case 2:
                case 5:
                case 10:
                case 11:
                case 23:
                    activityClass = RepliesActivity.class;
                    param.putString("postID", String.valueOf(paramId));
                    param.putString("comID", String.valueOf(extraId));
                    param.putString("replyID", String.valueOf(dataId));
                    break;
                case 24:
                case 25:
                case 37:
                case 39:
                case 41:
                    activityClass = PageActivity.class;
                    param.putInt("pageId", paramId);
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 38:
                    activityClass = ProfileAct.class;
                    param.putInt("userID", paramId);
                    break;
                case 44:
                case 45:
                    activityClass = MessageAct.class;
                    param.putInt("user", paramId);
                    break;
                case 20:
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    emitter = true;
                    emitStr = "unhideComment";
                    object.put("user", myId);
                    object.put("comId", dataId);
                    object.put("date", date);
                    break;
                case 22:
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    emitter = true;
                    emitStr = "unhideReply";
                    object.put("user", myId);
                    object.put("repId", dataId);
                    object.put("date", date);
                    break;
            }
            if(!(activityClass == null) || emitter)
                openOptions(activityClass, param, emitter, emitStr, object);
        }
    }

    @SuppressLint("InflateParams")
    private void openOptions(Class activityClass, Bundle param, boolean emitter, String emitStr, JSONObject object) {
        RelativeLayout postOptView = (RelativeLayout) getLayoutInflater().inflate(R.layout.post_options, null);
        LinearLayout optLayer = postOptView.findViewById(R.id.optLayer);
        if(blackFade.getChildCount() > 0){
            blackFade.removeAllViews();
        }
        String text;
        int drawableLeft;
        if(emitter){
            text = "Unhide";
            drawableLeft = R.drawable.ic_hide_post;
        } else {
            text = "View";
            drawableLeft = R.drawable.ic_article;
        }
        TextView optionList = (TextView) getLayoutInflater().inflate(R.layout.options_list, null);
        optionList.setText(text);
        optionList.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
        optionList.setOnClickListener(v -> executeOption(activityClass, param, emitter, emitStr, object));
        optLayer.addView(optionList);
        blackFade.addView(postOptView);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void executeOption(Class activityClass, Bundle param, boolean emitter, String emitStr, JSONObject object) {
        blackFade.setVisibility(View.GONE);
        if(emitter) {
            Toast.makeText(context, "You've unhide this comment", Toast.LENGTH_LONG).show();
            socket.emit(emitStr, object);
        } else {
            Intent intent = new Intent(this, activityClass);
            intent.putExtras(param);
            startActivity(intent);
        }
    }

    public  void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
            blackFade.removeAllViews();
        } else {
            socket.emit("disconnected", myId);
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
