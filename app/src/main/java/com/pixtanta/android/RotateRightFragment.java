package com.pixtanta.android;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RotateRightFragment extends Fragment {

    TextView rotateRight;


    public RotateRightFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_rotate_right, container, false);
        rotateRight =  itemView.findViewById(R.id.rotateRight);

        rotateRight.setOnClickListener(v -> PhotoEditorAct.rotateImage(getContext(), 90));

        return itemView;
    }

}
