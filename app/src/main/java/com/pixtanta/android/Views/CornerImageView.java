package com.pixtanta.android.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import pl.droidsonroids.gif.GifImageView;

public class CornerImageView extends GifImageView {

    private Path path;

    public CornerImageView(Context context) {
        super(context);
        init();
    }

    public CornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CornerImageView(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        init();
    }

    private void init() {
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        @SuppressLint("DrawAllocation") RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        float radius = 40f;
        path.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
