package com.pixtanta.android.Views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;

import com.pixtanta.android.R;

import static android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT;

public class SlidingGradientView extends FrameLayout {

    private static final int DEFAULT_ANIMATION_DURATION = 2000;
    private static final int DEFAULT_START_COLOR = Color.TRANSPARENT;
    private static final int DEFAULT_END_COLOR = Color.GRAY;

    private final View one;
    private final View two;

    private int animationDuration;
    private int startColor;
    private int endColor;
    private int laidOutWidth;

    ValueAnimator animator;

    public SlidingGradientView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.horizontal_progress, this);
        readAttributes(attrs);

        one = findViewById(R.id.one);
        two = findViewById(R.id.two);

        ViewCompat.setBackground(one, new GradientDrawable(LEFT_RIGHT, new int[]{ startColor, endColor }));
        ViewCompat.setBackground(two, new GradientDrawable(LEFT_RIGHT, new int[]{ endColor, startColor }));

        getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            laidOutWidth = SlidingGradientView.this.getWidth();
            animator = ValueAnimator.ofInt(0, 2 * laidOutWidth);
            animator.setInterpolator(new LinearInterpolator());
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.setDuration(animationDuration);
            animator.addUpdateListener(updateListener);
            animator.start();
        });
    }

    public void startAnimation(){
        if(animator == null){
            laidOutWidth = SlidingGradientView.this.getWidth();
            animator = ValueAnimator.ofInt(0, 2 * laidOutWidth);
        }
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(updateListener);
        animator.start();
    }

    public void stopAnimation(){
        if(!(animator == null))
            animator.end();
    }

    private void readAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SlidingGradientView);
        animationDuration = a.getInt(R.styleable.SlidingGradientView_animationDuration, DEFAULT_ANIMATION_DURATION);
        startColor = a.getColor(R.styleable.SlidingGradientView_gradientStartColor, DEFAULT_START_COLOR);
        endColor = a.getColor(R.styleable.SlidingGradientView_gradientEndColor, DEFAULT_END_COLOR);
        a.recycle();
    }

    private final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int offset = (int) valueAnimator.getAnimatedValue();
            one.setTranslationX(calculateOneTranslationX(laidOutWidth, offset));
            two.setTranslationX(calculateTwoTranslationX(laidOutWidth, offset));
        }
    };

    private int calculateOneTranslationX(int width, int offset) {
        return (-1 * width) + offset;
    }

    private int calculateTwoTranslationX(int width, int offset) {
        if (offset <= width)
            return offset;
        else
            return (-2 * width) + offset;
    }

}