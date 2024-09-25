package com.pixtanta.android;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.pixtanta.android.Interface.EditImageFragmentListener;

import static com.pixtanta.android.PhotoEditorAct.saveEditState;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditImageFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private EditImageFragmentListener listener;
    SeekBar seekBarBrightness, seekBarConstraint, seekBarSaturation;

    public void setListener(EditImageFragmentListener listener) {
        this.listener = listener;
    }

    public EditImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_edit_image, container, false);
        seekBarBrightness = itemView.findViewById(R.id.seekBarBrightness);
        seekBarConstraint = itemView.findViewById(R.id.seekBarConstraint);
        seekBarSaturation = itemView.findViewById(R.id.seekBarSaturation);

        seekBarBrightness.setMax(200);
        seekBarBrightness.setProgress(100);
        seekBarConstraint.setMax(20);
        seekBarConstraint.setProgress(0);
        seekBarSaturation.setMax(30);
        seekBarSaturation.setProgress(10);

        seekBarBrightness.setOnSeekBarChangeListener(this);
        seekBarConstraint.setOnSeekBarChangeListener(this);
        seekBarSaturation.setOnSeekBarChangeListener(this);

        return itemView;

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(listener != null){
            if(seekBar.getId() == R.id.seekBarBrightness){
                listener.onBrightnessChanged(progress - 100);
            } else if(seekBar.getId() == R.id.seekBarConstraint){
                progress += 10;
                float value = .10f * progress;
                listener.onConstraintChanged(value);
            } else if(seekBar.getId() == R.id.seekBarSaturation){
                float value = .10f * progress;
                listener.onSaturationChanged(value);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if(listener != null)
            listener.onEditStart();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(listener != null) {
            listener.onEditComplete();
            saveEditState(getContext());
        }
    }

    public void resetControls(){
        seekBarBrightness.setProgress(100);
        seekBarConstraint.setProgress(0);
        seekBarSaturation.setProgress(10);
    }
}
