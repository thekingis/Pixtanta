package com.pixtanta.android.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.pixtanta.android.R;

public class BackgroundChangingView extends View {

    private int enterFadeDuration;
    private int exitFadeDuration;

    Path mPath;
    float borderRadius;

    public BackgroundChangingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setAttributes(attrs);

        AnimationDrawable animationDrawable = (AnimationDrawable) this.getBackground();

        if(!(animationDrawable == null)){
            animationDrawable.setEnterFadeDuration(enterFadeDuration);
            animationDrawable.setExitFadeDuration(exitFadeDuration);
            animationDrawable.start();
        }
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BackgroundChangingView);
        enterFadeDuration = typedArray.getInt(R.styleable.BackgroundChangingView_enterFadeDuration, 1000);
        exitFadeDuration = typedArray.getInt(R.styleable.BackgroundChangingView_exitFadeDuration, 1000);
        borderRadius = typedArray.getInt(R.styleable.BackgroundChangingView_borderRadius, 0);
        typedArray.recycle();
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(mPath);
        super.draw(canvas);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        RectF r = new RectF(0, 0, w, h);
        mPath = new Path();
        mPath.addRoundRect(r, borderRadius, borderRadius, Path.Direction.CW);
        mPath.close();
    }

}
