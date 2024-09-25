package com.pixtanta.android.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.pixtanta.android.R;

public class CustomView extends View {

    boolean excluded;

    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setAttrValues(context, attrs, 0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttrValues(context, attrs, defStyleAttr);
    }

    private void setAttrValues(Context context, AttributeSet attrs, int defStyleAttr){
        TypedArray a = context.getTheme().obtainStyledAttributes( attrs, R.styleable.CustomView, defStyleAttr, 0);
        try {
            excluded = a.getBoolean(R.styleable.CustomView_excluded, false);
        } finally {
            a.recycle();
        }
    }

    public boolean getExcluded(){
        return excluded;
    }

    public void setExcluded(boolean excluded){
        this.excluded = excluded;
        invalidate();
        requestLayout();
    }

}
