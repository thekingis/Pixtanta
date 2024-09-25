package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class PhotoOpenerAdapter extends PagerAdapter {

    private final ArrayList<String> postFilesLists;
    private final Context context;
    ImageLoader imageLoader;
    public static Handler UIHandler;
    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public PhotoOpenerAdapter(ArrayList<String> postFilesLists, Context context) {
        this.postFilesLists = postFilesLists;
        this.context = context;
        imageLoader = new ImageLoader(context);
    }
    @Override
    public int getCount() {
        return postFilesLists.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    public ArrayList<String> getAllItems() {
        return postFilesLists;
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        String imageUrl = postFilesLists.get(position);
        String fileType = Functions.checkFileType(imageUrl.toLowerCase());
        View view;
        if(Objects.equals(fileType, "video")) {
            view = layoutInflater.inflate(R.layout.video_player, container, false);
            VideoView videoView =  view.findViewById(R.id.videoPlayer);
            TextView videoTime =  view.findViewById(R.id.videoTime);
            TextView videoDuration =  view.findViewById(R.id.videoDuration);
            SeekBar videoProgress =  view.findViewById(R.id.videoProgress);
            ImageButton videoPlayPause =  view.findViewById(R.id.videoPlayPause);
            LinearLayout controlsLayout =  view.findViewById(R.id.controlsLayout);
            ImageView videoThumbnail =  view.findViewById(R.id.videoThumbnail);
            ProgressBar progressBar =  view.findViewById(R.id.progressBar);
            try {
                Bitmap bitmap = Functions.getVideoThumbnail(imageUrl, false);
                videoThumbnail.setImageBitmap(bitmap);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            Uri uri = Uri.parse(imageUrl);
            videoView.setVideoURI(uri);
            CountDownTimer countDownTimerTouch = new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    videoPlayPause.setVisibility(View.GONE);
                    controlsLayout.setVisibility(View.GONE);
                }
            }.start();
            view.setOnTouchListener((v, event) -> {
                if(videoPlayPause.getVisibility() == View.VISIBLE){
                    videoPlayPause.setVisibility(View.GONE);
                    controlsLayout.setVisibility(View.GONE);
                    countDownTimerTouch.cancel();
                } else {
                    videoPlayPause.setVisibility(View.VISIBLE);
                    controlsLayout.setVisibility(View.VISIBLE);
                    countDownTimerTouch.start();
                }
                return false;
            });
            videoView.setOnCompletionListener(mp -> {
                videoProgress.setProgress(0);
                videoTime.setText("00:00:00");
                videoPlayPause.setBackgroundResource(R.drawable.ic_play);
                runOnUI(() -> videoThumbnail.setVisibility(View.VISIBLE));
            });
            videoView.setOnPreparedListener(mp -> {
                long timeInMillisec = videoView.getDuration();
                String vidTime = Functions.convertMilliTime(timeInMillisec);
                videoDuration.setText(vidTime);
                videoProgress.setMax((int)timeInMillisec / 1000);
                runOnUI(() -> {
                    videoThumbnail.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                });

                videoProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser) {
                            int curProg = progress * 1000;
                            videoView.seekTo(curProg);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                timerCounter(videoView, videoProgress, videoTime);

                videoPlayPause.setOnClickListener(v -> {
                    runOnUI(() -> videoThumbnail.setVisibility(View.GONE));
                    if(videoView.isPlaying()){
                        videoView.pause();
                        videoPlayPause.setBackgroundResource(R.drawable.ic_play);
                    } else {
                        videoView.start();
                        videoPlayPause.setBackgroundResource(R.drawable.ic_pause);
                        runOnUI(() -> videoThumbnail.setVisibility(View.VISIBLE));
                    }
                });

            });
        } else{
            view = layoutInflater.inflate(R.layout.photo_opener, container, false);
            ImageView imageView =  view.findViewById(R.id.image);
            imageLoader.displayImage(imageUrl, imageView);
        }
        container.addView(view);
        return view;
    }

    private void timerCounter(VideoView videoView, SeekBar seekBar, TextView textView){
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int seekBarMax = seekBar.getMax();
                int seekBarPos = seekBar.getProgress();
                long videoCurPos = videoView.getCurrentPosition();
                if(seekBarPos < seekBarMax && videoView.isPlaying()) {
                    int seekBarPrgTo = (int)videoCurPos / 1000;
                    seekBar.setProgress(seekBarPrgTo);
                    String prgTime = Functions.convertMilliTime(videoCurPos);
                    runOnUI(() -> textView.setText(prgTime));
                }
            }
        };
        timer.schedule(task, 0, 1000);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
