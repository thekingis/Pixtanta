package com.pixtanta.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PhotoAdapter extends PagerAdapter {

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

    public PhotoAdapter(ArrayList<String> postFilesLists, Context context) {
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

    @Override
    public int getItemPosition(@NotNull Object object) {
        return postFilesLists.indexOf(object);
    }

    public ArrayList<String> getAllItems() {
        return postFilesLists;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        getItemPosition(container);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.post_image, container, false);
        ImageView imageView =  view.findViewById(R.id.image);
        String imageUrl = postFilesLists.get(position);
        String fileType = Functions.checkFileType(imageUrl.toLowerCase());
        
        if(fileType.equals("video")) {
            LinearLayout videoPlay =  view.findViewById(R.id.videoPlay);
            videoPlay.setVisibility(View.VISIBLE);
            videoPlay.setOnClickListener(v -> HomeAct.openImage(getAllItems(), position, context));
        }
        if(imageUrl.startsWith("http")) {
            if(fileType.equals("image"))
                imageLoader.displayImage(imageUrl, imageView);
            else {
                Bitmap bitmap = null;
                try {
                    bitmap = Functions.getVideoThumbnail(imageUrl, false);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                imageView.setImageBitmap(bitmap);
            }
        } else {
            Bitmap bitmap = Functions.decodeFiles(imageUrl, fileType, false);
            imageView.setImageBitmap(bitmap);
        }
        imageView.post(() -> imageView.setTag("true"));
        imageView.setOnClickListener(v -> HomeAct.openImage(getAllItems(), position, context));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        runOnUI(() -> container.removeView((View)object));
    }

}
