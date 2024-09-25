package com.pixtanta.android.Views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pixtanta.android.R;

public class SliderView extends FrameLayout {

    private int laidOutWidth, sliderIcon, animeDuration, iconWidth, iconHeight;
    private static final int DEFAULT_BACKGROUND = 0, DEFAULT_DURATION = 0, DEFAULT_DIMENSION = 0;

    private final View view;
    boolean visible = false;

    public SliderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.slider_view, this);
        setAttributes(attrs);
        view = findViewById(R.id.view);
        view.setBackgroundResource(sliderIcon);
        LayoutParams params = new LayoutParams(iconWidth, iconHeight);
        view.setLayoutParams(params);

        getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if(SliderView.this.getVisibility() == View.VISIBLE && !visible) {
                laidOutWidth = SliderView.this.getWidth();
                ValueAnimator animator = ValueAnimator.ofInt(0, 2 * laidOutWidth);
                animator.setInterpolator(new LinearInterpolator());
                animator.setRepeatMode(ValueAnimator.RESTART);
                animator.setDuration(animeDuration);
                animator.addUpdateListener(updateListener);
                animator.start();
            }
            visible = SliderView.this.getVisibility() == View.VISIBLE;
        });
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SliderView);
        iconWidth = typedArray.getDimensionPixelSize(R.styleable.SliderView_iconWidth, DEFAULT_DIMENSION);
        iconHeight = typedArray.getDimensionPixelSize(R.styleable.SliderView_iconHeight, DEFAULT_DIMENSION);
        animeDuration = typedArray.getInt(R.styleable.SliderView_animeDuration, DEFAULT_DURATION);
        sliderIcon = typedArray.getResourceId(R.styleable.SliderView_sliderIcon, DEFAULT_BACKGROUND);
        typedArray.recycle();
    }

    private final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int offset = (int) valueAnimator.getAnimatedValue();
            view.setTranslationX(calculateOneTranslationX(laidOutWidth, offset));
        }
    };

    private int calculateOneTranslationX(int width, int offset) {
        return (-1 * width) + offset;
    }
}
