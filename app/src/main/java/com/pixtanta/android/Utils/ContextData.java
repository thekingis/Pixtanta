package com.pixtanta.android.Utils;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;

import io.socket.client.Socket;

public class ContextData {
    public Context context;
    public int dataId, width, height, pageCount;
    public LinearLayout blackFade, comMentionLayer, viewHolder;
    public ScrollView comScrllView;
    public RelativeLayout mainHomeView;
    public NestedScrollView nest;
    public boolean aBoolean, loadedPhotos, loadedVideos;
    public Socket socket;
    public String photo, pageName;
}
