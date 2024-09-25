package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class PickEditImage extends ThemeActivity {

    ArrayList<String> allMediaList = new ArrayList<>();
    String[] allPath;
    File[] allFiles;
    TextView done;
    @SuppressLint("StaticFieldLeak")
    public static GridView gridView;
    ImageAdaptor imageAdapter;
    Bitmap bitmapToAdd;
    Context cntxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_edit_image);
        cntxt = this;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels - 5;
        int imgW = (width/3) - 10;
        gridView =  findViewById(R.id.gridView);
        done =  findViewById(R.id.done);

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
            PhotoEditorAct.addImageToEdit(bitmapToAdd);
            finish();
        });
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
                //convertView.setBackgroundResource(R.drawable.border_box);
            bitmap = Functions.decodeFiles(filePath, Objects.requireNonNull(fileType), false);
            imageView.setImageBitmap(bitmap);
            if(bitmap == bitmapToAdd)
                convertView.setBackgroundResource(R.drawable.border_box);
            View finalConvertView = convertView;
            imageView.setOnClickListener(v -> {
                bitmapToAdd = bitmap;
                for(int x = 0; x < gridView.getChildCount(); x++){
                    View view = gridView.getChildAt(x);
                    view.setBackgroundResource(R.drawable.null_border);
                }
                finalConvertView.setBackgroundResource(R.drawable.border_box);
            });
            return convertView;
        }
    }
}
