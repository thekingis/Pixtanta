package com.pixtanta.android.Interface;

public interface EditImageFragmentListener {

    void onBrightnessChanged(int brightness);
    void onConstraintChanged(float constraint);
    void onSaturationChanged(float saturation);
    void onEditStart();
    void onEditComplete();

}
