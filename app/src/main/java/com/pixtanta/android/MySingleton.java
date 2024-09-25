package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

public class MySingleton {

    @SuppressLint("StaticFieldLeak")
    private static MySingleton mInstance;
    private RequestQueue mReqQ;
    private final Context mCtx;

    public MySingleton(Context mCtx){
        this.mCtx = mCtx;
        mReqQ = getmReqQ();
    }

    public RequestQueue getmReqQ(){
        if(mReqQ == null){
            Cache cache = new DiskBasedCache(mCtx.getCacheDir(), 1024 * 1024);
            Network ntwk = new BasicNetwork(new HurlStack());
            mReqQ = new RequestQueue(cache, ntwk);
            mReqQ = Volley.newRequestQueue(mCtx.getApplicationContext());
        }

        return  mReqQ;
    }

    public static synchronized MySingleton getmInstance(Context cntx){
        if(mInstance == null){
            mInstance = new MySingleton(cntx);
        }
        return  mInstance;
    }

    public <T> void addToReqQ(Request<T> reqst){
        mReqQ.add(reqst);
    }
}
