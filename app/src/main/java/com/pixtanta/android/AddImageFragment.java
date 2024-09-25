package com.pixtanta.android;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddImageFragment extends Fragment {

    TextView insertImage;
    Context cntxt;

    public AddImageFragment(Context context) {
        cntxt = context;
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_add_image, container, false);
        insertImage = itemView.findViewById(R.id.insertImage);

        insertImage.setOnClickListener(v -> {
            Intent intent = new Intent(cntxt, PickEditImage.class);
            startActivity(intent);
        });

        return itemView;
    }

}
