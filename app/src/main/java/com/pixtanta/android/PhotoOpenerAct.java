package com.pixtanta.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.Objects;

public class PhotoOpenerAct extends ThemeActivity {

    static ViewPager viewPager;
    Context cntxt;
    public static int filePosition;
    static ArrayList<String> allFiles;
    PhotoOpenerAdapter photoOpenerAdapter;
    boolean loadedInitial = false;
    @SuppressLint("StaticFieldLeak")
    static VideoView videoView;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_opener);
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedPrefMngr.initializeSmartLogin();
        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(PhotoOpenerAct.this, LoginAct.class));
            return;
        }

        Bundle fileParams = getIntent().getExtras();
        filePosition = fileParams.getInt("index");
        allFiles = fileParams.getStringArrayList("arrayList");
        photoOpenerAdapter = new PhotoOpenerAdapter(allFiles, cntxt);
        viewPager =  findViewById(R.id.viewPager);
        viewPager.setAdapter(photoOpenerAdapter);
        viewPager.setOffscreenPageLimit(allFiles.size());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                viewPager.post(() -> {
                    if(!loadedInitial) {
                        loadedInitial = true;
                        String filePath = allFiles.get(position);
                        String fileType = Functions.checkFileType(filePath);
                        if(Objects.equals(fileType, "video"))
                            startVideoPlayer(position);
                        setViewPagerCurrentItem();
                    }
                });
            }

            @Override
            public void onPageSelected(int position) {
                pauseVideoPlayer();
                String filePath = allFiles.get(position);
                String fileType = Functions.checkFileType(filePath);
                if(Objects.equals(fileType, "video")) {
                    videoView = null;
                    startVideoPlayer(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    private void setViewPagerCurrentItem() {
        viewPager.setCurrentItem(filePosition);
    }

    private void pauseVideoPlayer() {
        if(!(videoView == null)){
            videoView.pause();
        }
    }

    public static void startVideoPlayer(int position) {
        View videoLayout = viewPager.getChildAt(position);
        videoView =  videoLayout.findViewById(R.id.videoPlayer);
        if(!(videoView == null)) {
            ImageView videoThumbnail =  videoLayout.findViewById(R.id.videoThumbnail);
            ImageButton videoPlayPause =  videoLayout.findViewById(R.id.videoPlayPause);
            videoView.requestFocus();
            videoView.start();
            videoThumbnail.setVisibility(View.GONE);
            videoPlayPause.setBackgroundResource(R.drawable.ic_pause);
        }
    }
}
