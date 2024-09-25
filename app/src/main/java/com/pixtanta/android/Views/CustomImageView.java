package com.pixtanta.android.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import ja.burhanrashid52.photoeditor.PhotoEditorView;

public class CustomImageView extends PhotoEditorView {

    public CustomImageView(Context context) {
        super(context);
        initializeView();
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView();
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView();
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView();
    }

    private void initializeView(){
        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                resetViewSize();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public void setImageBitmap(Bitmap bitmap){
        this.getSource().setImageBitmap(bitmap);
        resetViewSize();
    }

    public void setImageDrawable(Drawable drawable){
        this.getSource().setImageDrawable(drawable);
        resetViewSize();
    }

    public void setImageURI(Uri uri){
        this.getSource().setImageURI(uri);
        resetViewSize();
    }

    public void resetViewSize(){
        Drawable drawable = this.getSource().getDrawable();
        if (!(drawable == null)) {
            double btmpW = drawable.getIntrinsicWidth();
            double btmpH = drawable.getIntrinsicHeight();
            double viewW = this.getWidth();
            double viewH = this.getHeight();
            if(viewW > 0 && viewH > 0) {
                double width, height;
                double wRatio = btmpW / viewW;
                width = viewW;
                height = btmpH / wRatio;
                if (height > viewH) {
                    double hRatio = height / viewH;
                    height = viewH;
                    width = width / hRatio;
                }
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.getLayoutParams();
                params.width = (int) width;
                params.height = (int) height;
                this.setLayoutParams(params);
                requestLayout();
            }
        }
    }

}
