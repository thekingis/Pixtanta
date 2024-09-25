package com.pixtanta.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
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

import ja.burhanrashid52.photoeditor.PhotoEditorView;

public class ImageLoader {
    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private final Map<View, String> views = Collections.synchronizedMap(new WeakHashMap<>());
    ExecutorService executorService;

    public ImageLoader(Context cntxt){
        fileCache = new FileCache(cntxt);
        executorService = Executors.newFixedThreadPool(5);
    }


    public void displayImage(String url, View view){
        views.put(view, url);
        Bitmap bitmap = memoryCache.get(url);
        if(!(bitmap == null)){
            if(view instanceof ImageView){
                ImageView imageView = (ImageView) view;
                imageView.setImageBitmap(bitmap);
            }
            if(view instanceof PhotoEditorView){
                PhotoEditorView photoEditorView = (PhotoEditorView) view;
                photoEditorView.getSource().setImageBitmap(bitmap);
            }
        } else {
            queuePhoto(url, view);
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
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = 1;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, op);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    private static class PhotoToLoad {
        public String url;
        public View view;
        public PhotoToLoad(String u, View i){
            url = u;
            view = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad = photoToLoad;
        }
        @Override
        public void run(){
            if(viewReused(photoToLoad)){
                return;
            }
            Bitmap bmp = getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bmp);
            if(viewReused(photoToLoad)){
                return;
            }
            BitmapDisplayer bitmapDisplayer = new BitmapDisplayer(bmp, photoToLoad);
            Activity a = (Activity) photoToLoad.view.getContext();
            a.runOnUiThread(bitmapDisplayer);
        }
    }

    boolean viewReused(PhotoToLoad photoToLoad){
        String tag = views.get(photoToLoad.view);
        return tag == null || !tag.equals(photoToLoad.url);
    }

    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p){
            bitmap = b;
            photoToLoad = p;
        }

        @Override
        public void run(){
            if(viewReused(photoToLoad))
                return;
            if(!(bitmap == null)){
                if(photoToLoad.view instanceof ImageView){
                    ImageView imageView = (ImageView) photoToLoad.view;
                    imageView.setImageBitmap(bitmap);
                }
                if(photoToLoad.view instanceof PhotoEditorView){
                    PhotoEditorView photoEditorView = (PhotoEditorView) photoToLoad.view;
                    photoEditorView.getSource().setImageBitmap(bitmap);
                }
            }
        }
    }

    public void clearCache(){
        MemoryCache.clear();
        FileCache.clear();
    }

    private void queuePhoto(String url, View view){
        PhotoToLoad p = new PhotoToLoad(url, view);
        executorService.submit(new PhotosLoader(p));
    }

}
