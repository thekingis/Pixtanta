package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class VideoEditorAct extends ThemeActivity {

    FFmpeg ffmpeg;
    LinearLayout rightPin, leftPin, seekBar, discardLayout, blackFade;
    RelativeLayout seeker;
    VideoView video;
    ImageButton videoPlayPause;
    ImageView videoThumbnail;
    TextView discardEdit, saveEdit, trimmerStart, trimmerEnd;
    Button cancelDiscard, agreeDiscard;
    Context cntxt;
    String lang;
    int myId, deltaX, seekerW, width, videoDuration,count;
    RelativeLayout.LayoutParams layoutParams, layoutParamsLeft, layoutParamsRight;
    DisplayMetrics displayMetrics;
    static int fileId;
    public static String filePath, tempDir;
    Bitmap bitmap;
    long duration;
    MediaMetadataRetriever retriever;
    boolean trimmed = false;
    int act;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    boolean shakeOpt;
    SharedPrefMngr sharedPrefMngr;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);
        sharedPrefMngr = new SharedPrefMngr(this);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;

        sharedPrefMngr.initializeSmartLogin();
        lang = sharedPrefMngr.getSelectedLanguage();
        cntxt = this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(VideoEditorAct.this, LoginAct.class));
            return;
        }
        myId = sharedPrefMngr.getMyId();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(() -> {
            shakeOpt = sharedPrefMngr.checkShakeOption();
            if(shakeOpt)
                openReportDialog();
        });
        Bundle fileParams = getIntent().getExtras();
        act = fileParams.getInt("act");
        fileId = fileParams.getInt("fileId");
        filePath = fileParams.getString("filePath");
        tempDir = StorageUtils.getStorageDirectories(cntxt)[0];
        //alternative if storage don't exist... but it definately will
                /*if(!(new File(tempDir).exists()))
                    tempDir = StorageUtils.getStorageDirectories(cntxt)[1];*/
        tempDir += "/Pictures";

        video =  findViewById(R.id.video);
        seeker =  findViewById(R.id.seeker);
        blackFade =  findViewById(R.id.blackFade);
        seekBar =  findViewById(R.id.seekBar);
        leftPin =  findViewById(R.id.leftPin);
        rightPin =  findViewById(R.id.rightPin);
        videoPlayPause =  findViewById(R.id.videoPlayPause);
        videoThumbnail =  findViewById(R.id.videoThumbnail);
        discardEdit =  findViewById(R.id.discardEdit);
        saveEdit =  findViewById(R.id.saveEdit);
        trimmerStart =  findViewById(R.id.trimmerStart);
        trimmerEnd =  findViewById(R.id.trimmerEnd);
        cancelDiscard =  findViewById(R.id.cancelDiscard);
        agreeDiscard =  findViewById(R.id.agreeDiscard);
        discardLayout =  findViewById(R.id.discardLayout);
        bitmap = Functions.decodeFiles(filePath, "video", false);
        video.setVideoPath(filePath);
        videoThumbnail.setImageBitmap(bitmap);
        retriever = new MediaMetadataRetriever();
        duration = Functions.getVideoDuration(retriever, filePath, this);
        videoDuration = (int)duration / 1000;
        String endTimer = Functions.convertMilliTime(duration);
        trimmerEnd.setText(endTimer);
        layoutParams = (RelativeLayout.LayoutParams) seekBar.getLayoutParams();
        seekerW = (width - (layoutParams.leftMargin * 2)) - 50;
        layoutParamsLeft = (RelativeLayout.LayoutParams) leftPin.getLayoutParams();
        leftPin.setLayoutParams(layoutParamsLeft);
        layoutParamsRight = (RelativeLayout.LayoutParams) rightPin.getLayoutParams();
        layoutParamsRight.leftMargin = seekerW;
        rightPin.setLayoutParams(layoutParamsRight);

        videoPlayPause.setOnClickListener(v -> {
            if(video.isPlaying()){
                pauseVideo();
            } else {
                videoThumbnail.setVisibility(View.GONE);
                playVideo();
            }
        });
        leftPin.setOnTouchListener((v, event) -> {
            final int X = (int)event.getRawX();
            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    layoutParamsLeft = (RelativeLayout.LayoutParams) leftPin.getLayoutParams();
                    deltaX = X - layoutParamsLeft.leftMargin;
                    break;
                case MotionEvent.ACTION_MOVE:
                    trimmed = true;
                    layoutParamsLeft = (RelativeLayout.LayoutParams) leftPin.getLayoutParams();
                    layoutParamsLeft.leftMargin = X - deltaX;
                    if(!(layoutParamsLeft.leftMargin < 0) && !(layoutParamsLeft.leftMargin > seekerW)) {
                        leftPin.setLayoutParams(layoutParamsLeft);
                        if(layoutParamsRight.leftMargin > seekerW){
                            layoutParamsRight.leftMargin = seekerW;
                            rightPin.setLayoutParams(layoutParamsRight);
                        }
                        showTrimmedVideoPath();
                    }
                    break;
            }
            seeker.invalidate();
            return true;
        });
        rightPin.setOnTouchListener((v, event) -> {
            final int X = (int)event.getRawX();
            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    layoutParamsRight = (RelativeLayout.LayoutParams) rightPin.getLayoutParams();
                    layoutParamsLeft = (RelativeLayout.LayoutParams) leftPin.getLayoutParams();
                    deltaX = X - layoutParamsRight.leftMargin;
                    break;
                case MotionEvent.ACTION_MOVE:
                    trimmed = true;
                    layoutParamsRight = (RelativeLayout.LayoutParams) rightPin.getLayoutParams();
                    layoutParamsRight.leftMargin = X - deltaX;
                    if(!(layoutParamsRight.leftMargin < 0) && !(layoutParamsRight.leftMargin > seekerW)) {
                        rightPin.setLayoutParams(layoutParamsRight);
                        if(layoutParamsLeft.leftMargin < 0){
                            layoutParamsLeft.leftMargin = 0;
                            leftPin.setLayoutParams(layoutParamsLeft);
                        }
                        showTrimmedVideoPath();
                    }
                    break;
            }
            seeker.invalidate();
            return true;
        });
        saveEdit.setOnClickListener(v -> saveVideoEdit());
        discardEdit.setOnClickListener(v -> discardLayout.setVisibility(View.VISIBLE));
        cancelDiscard.setOnClickListener(v -> discardLayout.setVisibility(View.GONE));
        agreeDiscard.setOnClickListener(v -> finish());
        blackFade.setOnClickListener(v -> {
            blackFade.setVisibility(View.GONE);
            return;
        });

        loadFFMpegBinary();

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

    private void saveVideoEdit() {
        if(!trimmed){
            finish();
        } else {
            int leftPinner = layoutParamsLeft.leftMargin, rightPinner = layoutParamsRight.leftMargin, ratio, trimStart, trimEnd, trim;
            if(leftPinner > rightPinner){
                leftPinner = layoutParamsRight.leftMargin;
                rightPinner = layoutParamsLeft.leftMargin;
            }
            ratio = seekerW / videoDuration;
            trimStart = leftPinner / ratio;
            trimEnd = rightPinner / ratio;
            trim = trimEnd - trimStart;
            File externalStoragePublicDirectory = new File(tempDir);
            if (externalStoragePublicDirectory.exists() || externalStoragePublicDirectory.mkdir()) {
                String vidExt = filePath.substring(filePath.lastIndexOf("."));
                String randStr = UUID.randomUUID().toString();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    RandomString gen = new RandomString(8, ThreadLocalRandom.current());
                    randStr = gen.toString();
                }
                @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                String destFileName = "/VID_" + randStr + timeStamp + vidExt;
                File dest = new File(externalStoragePublicDirectory, destFileName + vidExt);
                String outputFilePath = dest.getAbsolutePath();
                String[] complexCommand = { "-y", "-i", filePath,"-ss", "" + trimStart, "-t", "" + trim, "-c","copy", outputFilePath};
                execFFmpegBinary(complexCommand);
            } else {
                Log.i("vex", "true");
            }
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(Arrays.toString(command), new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.i("onFailure", s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.i("onSuccess", s);
                }

                @Override
                public void onProgress(String s) {
                    Log.i("onProgress", s);
                }

                @Override
                public void onStart() {
                    Log.i("onStart", "onStart");
                }

                @Override
                public void onFinish() {
                    Log.i("onFinish", "onFinish");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException ignored) {}
    }

    private void showTrimmedVideoPath() {
        pauseVideo();
        int leftPinner = layoutParamsLeft.leftMargin, rightPinner = layoutParamsRight.leftMargin, ratio, trimStart, trimEnd;
        if(leftPinner > rightPinner){
            leftPinner = layoutParamsRight.leftMargin;
            rightPinner = layoutParamsLeft.leftMargin;
        }
        ratio = seekerW / videoDuration;
        trimStart = leftPinner / ratio;
        trimEnd = rightPinner / ratio;
        trimStart *= 1000;
        trimEnd *= 1000;
        String startTimer = Functions.convertMilliTime((long) trimStart);
        String endTimer = Functions.convertMilliTime((long) trimEnd);
        trimmerStart.setText(startTimer);
        trimmerEnd.setText(endTimer);
    }

    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                ffmpeg = FFmpeg.getInstance(cntxt);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.i("success", "false");
                    //showUnsupportedExceptionDialog();
                }

                @Override
                public void onSuccess() {
                    Log.i("success", "true");
                }
            });
        } catch (Exception ignored) {
        }
    }

    private void playVideo() {
        if(trimmed){
            int leftPinner = layoutParamsLeft.leftMargin, rightPinner = layoutParamsRight.leftMargin, ratio, trimStart, trimEnd, trim;
            if(leftPinner > rightPinner){
                leftPinner = layoutParamsRight.leftMargin;
                rightPinner = layoutParamsLeft.leftMargin;
            }
            ratio = seekerW / videoDuration;
            trimStart = leftPinner / ratio;
            trimEnd = rightPinner / ratio;
            trim = trimEnd - trimStart;
            trimStart *= 1000;
            video.seekTo(trimStart);
            startStopTimerForTrimmer(trim);
        }
        video.start();
        videoPlayPause.setBackgroundResource(R.drawable.ic_pause);
    }

    private void startStopTimerForTrimmer(int trim) {
        Timer timer = new Timer();
        count = 0;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(count == trim){
                    pauseVideo();
                    timer.cancel();
                }
                count++;
            }
        };
        timer.schedule(task, 0, 1000);
    }

    private void pauseVideo(){
        runOnUiThread(() -> {
            video.pause();
            videoPlayPause.setBackgroundResource(R.drawable.ic_play);
        });
    }

    public void onBackPressed(){
        if(discardLayout.getVisibility() == View.VISIBLE){
            discardLayout.setVisibility(View.GONE);
        } else if(discardLayout.getVisibility() == View.VISIBLE){
            discardLayout.setVisibility(View.GONE);
        } else {
            discardLayout.setVisibility(View.VISIBLE);
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
