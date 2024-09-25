package com.pixtanta.android;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.pixtanta.android.Adapter.ColorAdapter;
import com.pixtanta.android.Interface.AddTextFragmentListener;
import com.pixtanta.android.Utils.StringUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddTextFragment extends Fragment implements ColorAdapter.ColorAdapterListener {

    Button addBtn;
    EditText addText;
    ImageView colorPalette;
    AddTextFragmentListener listener;
    @SuppressLint("StaticFieldLeak")
    static AddTextFragment instance;
    int defaultColor = Color.parseColor("#000000");
    Bitmap bitmap;

    public static AddTextFragment getInstance(){
        if(instance == null)
            instance = new AddTextFragment();
        return instance;
    }

    public void setListener(AddTextFragmentListener listener) {
        this.listener = listener;
    }

    public AddTextFragment() {
        // Required empty public constructor
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_add_text, container, false);
        addText = itemView.findViewById(R.id.addText);
        addBtn = itemView.findViewById(R.id.addBtn);
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
            int pxl = bitmap.getPixel(x, y);
            int colorRed = Color.red(pxl);
            int colorBlue = Color.blue(pxl);
            int colorGreen = Color.green(pxl);
            String hex = String.format("#%02X%02X%02X", colorRed, colorGreen, colorBlue);
            onColorSelected(Color.parseColor(hex));
            return false;
        });

        addBtn.setOnClickListener(v -> {
            String text = addText.getText().toString();
            if(!StringUtils.isEmpty(text)){
                listener.onAddtextButtonClick(text, defaultColor);
                addText.setText("");
            }
        });

        return itemView;
    }

    @Override
    public void onColorSelected(int color) {
        defaultColor = color;
        addText.setTextColor(color);
    }
}
