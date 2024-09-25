package com.pixtanta.android;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ImageSelectorActivity extends ThemeActivity {

    ArrayList<String> allMediaList = new ArrayList<>();
    String[] allPath;
    File[] allFiles;
    File selectedFile;
    LinearLayout discardLayout;
    Button agreeDiscard, cancelDiscard;
    TextView done;
    @SuppressLint("StaticFieldLeak")
    public static GridView gridView;
    boolean discardShow = false;
    ImageAdaptor imageAdapter;
    Context cntxt;
    int index, activityReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selector);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cntxt = this;

        Bundle userParams = getIntent().getExtras();
        index = userParams.getInt("index");
        activityReq = userParams.getInt("activityReq");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels - 5;
        int imgW = (width/3) - 10;
        gridView = findViewById(R.id.gridView);
        discardLayout = findViewById(R.id.discardLayout);
        agreeDiscard = findViewById(R.id.agreeDiscard);
        cancelDiscard = findViewById(R.id.cancelDiscard);
        done = findViewById(R.id.done);

        allPath = StorageUtils.getStorageDirectories(this);
        for(String path: allPath){
            File storage = new File(path);
            loadDirectoryFiles(storage);
        }

        allFiles = new File[allMediaList.size()];
        for (int x = 0; x < allMediaList.size(); x++){
            String filePth = allMediaList.get(x);
            allFiles[x] = new File(filePth);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Arrays.sort(allFiles, Comparator.comparingLong(File::lastModified).reversed());
        }
        imageAdapter = new ImageAdaptor(allFiles, imgW);
        gridView.setAdapter(imageAdapter);

        done.setOnClickListener(v -> {
            if(!(selectedFile == null))
                sendSelectedImage();
        });

        agreeDiscard.setOnClickListener(v -> finish());

        cancelDiscard.setOnClickListener(v -> {
            discardShow = false;
            discardLayout.setVisibility(View.GONE);
        });
    }

    private void sendSelectedImage() {
        if(activityReq == 0)
            ProfileAct.handleSelectedImage(selectedFile, index);
        else if(activityReq == 1)
            PageActivity.handleSelectedImage(selectedFile, index);
        finish();
    }

    public void loadDirectoryFiles(File directory){
        boolean notForbidden = false;
        String[] forbiddenPaths = new String[]{
                "/Android/data",
                "/Android/obb",
                "/LOST.DIR",
                "/.thumbnail"
        };
        File[] fileList = directory.listFiles();
        if(fileList != null && fileList.length > 0){
            for (File file : fileList) {
                String filePath = file.getAbsolutePath();
                if (file.isDirectory()) {
                    for (String forbiddenPath : forbiddenPaths) {
                        if (filePath.contains(forbiddenPath)) {
                            notForbidden = true;
                            break;
                        }
                    }
                    if (!notForbidden)
                        loadDirectoryFiles(file);
                } else {
                    String pthPar = file.getParent();
                    
                    String[] pthPars = pthPar.split("/");
                    if (!(pthPars[pthPars.length - 1].equals("LOST.DIR") || pthPars[pthPars.length - 1].equals(".thumbnails"))) {
                        String name = file.getName().toLowerCase();
                        for (String ext : Constants.allowedExtImg) {
                            if (name.endsWith(ext)) {
                                allMediaList.add(filePath);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public  void  onBackPressed(){
        if(discardShow){
            discardLayout.setVisibility(View.GONE);
            discardShow = false;
        } else {
            if(!(selectedFile == null)){
                discardShow = true;
                discardLayout.setVisibility(View.VISIBLE);
            } else {
                finish();
            }
        }
    }

    private class ImageAdaptor extends BaseAdapter {


        private final int imageWidth;
        File[] itemList;
        Bitmap bitmap;

        public ImageAdaptor(File[] itemList, int imageWidth) {
            this.itemList = itemList;
            this.imageWidth = imageWidth;

        }

        @Override
        public int getCount() {
            return this.itemList.length;
        }

        @Override
        public Object getItem(int position) {
            return this.itemList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.image_box, parent, false);
            convertView.setLayoutParams(new GridView.LayoutParams(imageWidth ,imageWidth ));
            File thisFile = itemList[position];
            final String filePath = String.valueOf(thisFile);
            String fileType = Functions.checkFileType(filePath.toLowerCase());
            final ImageView imageView = convertView.findViewById(R.id.imgView);
            convertView.setContentDescription(filePath);

            if (selectedFile == thisFile)
                convertView.setBackgroundResource(R.drawable.border_box);
            else
                convertView.setBackgroundResource(R.drawable.null_border);
            
            bitmap = Functions.decodeFiles(filePath, fileType, true);
            imageView.setImageBitmap(bitmap);
            View finalConvertView = convertView;
            imageView.setOnClickListener(v -> {
                for (int i = 0; i < gridView.getChildCount(); i++){
                    View view = gridView.getChildAt(i);
                    if(!(view == finalConvertView))
                        view.setBackgroundResource(R.drawable.null_border);
                }
                selectedFile = thisFile;
                finalConvertView.setBackgroundResource(R.drawable.border_box);
            });
            return convertView;
        }
    }
}
