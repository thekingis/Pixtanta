package com.pixtanta.android.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pixtanta.android.R;

import org.jetbrains.annotations.NotNull;

public class CustomEditText extends androidx.appcompat.widget.AppCompatEditText {

    boolean searchAll;

    public CustomEditText(@NonNull @NotNull Context context) {
        super(context);
    }

    public CustomEditText(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        setAttrValues(context, attrs, 0);
    }

    public CustomEditText(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttrValues(context, attrs, defStyleAttr);
    }

    private void setAttrValues(Context context, AttributeSet attrs, int defStyleAttr){
        TypedArray a = context.getTheme().obtainStyledAttributes( attrs, R.styleable.CustomView, defStyleAttr, 0);
        try {
            searchAll = a.getBoolean(R.styleable.CustomEditText_searchAll, false);
        } finally {
            a.recycle();
        }
    }

    public boolean getSearchType(){
        return searchAll;
    }

    public void setSearchType(boolean searchAll){
        this.searchAll = searchAll;
        invalidate();
        requestLayout();
    }
}
