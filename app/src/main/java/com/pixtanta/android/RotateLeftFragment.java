package com.pixtanta.android;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class RotateLeftFragment extends Fragment {

    TextView rotateLeft;


    public RotateLeftFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_rotate_left, container, false);
        rotateLeft =  itemView.findViewById(R.id.rotateLeft);

        rotateLeft.setOnClickListener(v -> PhotoEditorAct.rotateImage(getContext(), -90));

        return itemView;
    }

}
