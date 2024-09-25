package com.pixtanta.android.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class CompatEditText extends androidx.appcompat.widget.AppCompatEditText {

    private int caretStart = 0, caretEnd = 0;

    public CompatEditText(@NonNull @NotNull Context context) {
        super(context);
    }

    public CompatEditText(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CompatEditText(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int[] getCaretPoints(){
        return new int[]{getSelectionStart(), getSelectionEnd()};
    }

    public int getCaretStart(){
        return getSelectionStart();
    }

    public int getCaretEnd(){
        return getSelectionEnd();
    }

    private void setCaretPositions(){
        setSelection(caretStart, caretEnd);
    }

    public boolean setCaret(int caret){
        setSelection(caret);
        return true;
    }

    public boolean setCaret(int caretS, int caretE){
        setSelection(caretS, caretE);
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
            caretStart = getSelectionStart();
            caretEnd = getSelectionEnd();
            setCaretPositions();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        //Passing FALSE as the SECOND ARGUMENT (fullEditor) to the constructor
        // will result in the key events continuing to be passed in to this
        // view.  Use our special BaseInputConnection-derived view
        InputConnectionAccommodatingLatinIMETypeNullIssues baseInputConnection =
                new InputConnectionAccommodatingLatinIMETypeNullIssues(this, false);

        //In some cases an IME may be able to display an arbitrary label for a
        // command the user can perform, which you can specify here.  A null value
        // here asks for the default for this key, which is usually something
        // like Done.
        outAttrs.actionLabel = null;

        //Special content type for when no explicit type has been specified.
        // This should be interpreted (by the IME that invoked
        // onCreateInputConnection())to mean that the target InputConnection
        // is not rich, it can not process and show things like candidate text
        // nor retrieve the current text, so the input method will need to run
        // in a limited "generate key events" mode.  This disables the more
        // sophisticated kinds of editing that use a text buffer.
        outAttrs.inputType = InputType.TYPE_NULL;

        //This creates a Done key on the IME keyboard if you need one
        //outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;

        return baseInputConnection;
    }

}

