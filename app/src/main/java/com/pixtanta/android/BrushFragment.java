package com.pixtanta.android;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.pixtanta.android.Adapter.ColorAdapter;
import com.pixtanta.android.Interface.BrushFragmentListener;

import static com.pixtanta.android.PhotoEditorAct.photoEditor;


/**
 * A simple {@link Fragment} subclass.
 */
public class BrushFragment extends Fragment implements ColorAdapter.ColorAdapterListener {

    SeekBar seekBarSize, seekBarOpacity;
    ImageView colorPalette;
    BrushFragmentListener listener;
    @SuppressLint("StaticFieldLeak")
    static BrushFragment instance;
    Bitmap bitmap;

    public static BrushFragment getInstance(){
        if(instance == null)
            instance = new BrushFragment();
        return instance;
    }

    public void setListener(BrushFragmentListener listener) {
        this.listener = listener;
    }

    public BrushFragment() {
        // Required empty public constructor
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_brush, container, false);
        seekBarSize = itemView.findViewById(R.id.seekBarSize);
        seekBarOpacity = itemView.findViewById(R.id.seekBarOpacity);
        colorPalette = itemView.findViewById(R.id.colorPalette);
        bitmap = ((BitmapDrawable) colorPalette.getDrawable()).getBitmap();
        colorPalette.setOnTouchListener((v, event) -> {
            double imgW = colorPalette.getWidth();
            double btmpW = bitmap.getWidth();
            double evtX = (int) event.getX();
            double evtY = (int) event.getY();
            int x, y;
            if(!(btmpW == imgW)){
                double a = btmpW / imgW;
                evtX *= a;
                evtY *= a;
            }
            x = (int)Math.round(evtX);
            y = (int)Math.round(evtY);
            int pxl = bitmap.getPixel(x, 0); // use y variable here instead of 0
            int colorRed = Color.red(pxl);
            int colorBlue = Color.blue(pxl);
            int colorGreen = Color.green(pxl);
            String hex = String.format("#%02X%02X%02X", colorRed, colorGreen, colorBlue);
            onColorSelected(Color.parseColor(hex));
            return false;
        });
        seekBarSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                listener.onBrushSizeChangedListener(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                listener.onBrushOpacityChangedListener(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarSize.setProgress(30);
        seekBarOpacity.setProgress(100);
        return itemView;
    }

    @Override
    public void onColorSelected(int color) {
        listener.onBrushColorChangedListener(color);
    }
}
