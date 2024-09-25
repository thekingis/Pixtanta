package com.pixtanta.android;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


/**
 * A simple {@link Fragment} subclass.
 */
public class CropImageFragment extends Fragment {

    Context context;
    TextView cropImage;
    UCrop.Options options;
    boolean darkThemeEnabled;
    SharedPrefMngr sharedPrefMngr;

    public CropImageFragment(Context cntxt) {
        context = cntxt;
        options = new UCrop.Options();
        sharedPrefMngr = new SharedPrefMngr(cntxt);
        darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_crop_image, container, false);
        cropImage = itemView.findViewById(R.id.cropImage);
        options.setToolbarTitle("");
        options.setActiveControlsWidgetColor(ContextCompat.getColor(context, R.color.colorPrimaryRed));
        options.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryRed));
        options.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryRed));
        options.setToolbarWidgetColor(ContextCompat.getColor(context, R.color.white));
        options.setRootViewBackgroundColor(ContextCompat.getColor(context, R.color.white));
        options.setRootViewBackgroundColor(ContextCompat.getColor(context, R.color.white));
        if(darkThemeEnabled)
            options.setRootViewBackgroundColor(ContextCompat.getColor(context, R.color.black));
        cropImage.setOnClickListener(v -> startCrop());

        return itemView;
    }

    private void startCrop() {
        Uri bitmapUri = Uri.parse(PhotoEditorAct.cropPath);
        @SuppressLint("SimpleDateFormat") String filePath,
                timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()),
                tempDir = PhotoEditorAct.tempDir,
                randStr = UUID.randomUUID().toString();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            RandomString gen = new RandomString(8, ThreadLocalRandom.current());
            randStr = gen.toString();
        }
        filePath = tempDir + "/IMG_" + randStr + timeStamp +".jpg";
        File file = new File(filePath);
        UCrop uCrop = UCrop.of(bitmapUri, Uri.fromFile(file)).withOptions(options);
        uCrop.start((Activity) context);
    }

}
