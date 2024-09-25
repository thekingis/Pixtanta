package com.pixtanta.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResizedImageLoader {
    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private final Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<>());
    ExecutorService executorService;

    public ResizedImageLoader(Context cntxt){
        fileCache = new FileCache(cntxt);
        executorService = Executors.newFixedThreadPool(5);
    }


    public void displayImage(String url, ImageView imageView){
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if(!(bitmap == null)){
            imageView.setImageBitmap(bitmap);
        } else {
            queuePhoto(url, imageView);
        }
    }

    private Bitmap getBitmap(String url){
        File f = fileCache.getFile(url);
        Bitmap b = decodeFile(f);
        if(!(b == null))
            return b;
        try {
            Bitmap bitmap;
            URL imageUrl = new URL(url);
            HttpURLConnection con = (HttpURLConnection) imageUrl.openConnection();
            con.setConnectTimeout(30000);
            con.setReadTimeout(30000);
            con.setInstanceFollowRedirects(true);
            InputStream is = con.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Util.copyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Throwable ex){
            ex.printStackTrace();
            return null;
        }
    }

    private Bitmap decodeFile(File f){
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            final int requiredSize = 70;
            int widthTmp = o.outWidth, heightTmp = o.outHeight, scale = 1;
            while (widthTmp / 2 >= requiredSize && heightTmp / 2 >= requiredSize) {
                widthTmp /= 2;
                heightTmp /= 2;
                scale *= 2;
            }
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, op);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    private static class PhotoToLoad {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            url = u;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        ResizedImageLoader.PhotoToLoad photoToLoad;
        PhotosLoader(ResizedImageLoader.PhotoToLoad photoToLoad){
            this.photoToLoad = photoToLoad;
        }
        @Override
        public void run(){
            if(imageViewReused(photoToLoad)){
                return;
            }
            Bitmap bmp = getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bmp);
            if(imageViewReused(photoToLoad)){
                return;
            }
            ResizedImageLoader.BitmapDisplayer bitmapDisplayer = new ResizedImageLoader.BitmapDisplayer(bmp, photoToLoad);
            Activity a = (Activity) photoToLoad.imageView.getContext();
            a.runOnUiThread(bitmapDisplayer);
        }
    }

    boolean imageViewReused(ResizedImageLoader.PhotoToLoad photoToLoad){
        String tag = imageViews.get(photoToLoad.imageView);
        return tag == null || !tag.equals(photoToLoad.url);
    }

    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        ResizedImageLoader.PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, ResizedImageLoader.PhotoToLoad p){
            bitmap = b;
            photoToLoad = p;
        }

        @Override
        public void run(){
            if(imageViewReused(photoToLoad))
                return;
            if(!(bitmap == null)){
                photoToLoad.imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void clearCache(){
        MemoryCache.clear();
        FileCache.clear();
    }

    private void queuePhoto(String url, ImageView imageView){
        ResizedImageLoader.PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new ResizedImageLoader.PhotosLoader(p));
    }
}
