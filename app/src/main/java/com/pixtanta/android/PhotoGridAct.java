package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
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

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class PhotoGridAct extends ThemeActivity {

    ArrayList<String> allMediaList = new ArrayList<>();
    String[] allPath;
    File[] allFiles;
    LinearLayout discardLayout;
    Button agreeDiscard, cancelDiscard;
    TextView done;
    GridView gridView;
    boolean discardShow = false;
    TextView selectionsTxt;
    ImageAdaptor imageAdapter;
    Context cntxt;
    int act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_grid);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cntxt = this;

        Bundle userParams = getIntent().getExtras();
        act = userParams.getInt("act");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels - 5;
        int imgW = (width/3) - 10;
        gridView =  findViewById(R.id.gridView);
        selectionsTxt =  findViewById(R.id.selectionsTxt);
        discardLayout =  findViewById(R.id.discardLayout);
        agreeDiscard =  findViewById(R.id.agreeDiscard);
        cancelDiscard =  findViewById(R.id.cancelDiscard);
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
        imageAdapter = new ImageAdaptor(this, allFiles, imgW);
        gridView.setAdapter(imageAdapter);

        done.setOnClickListener(v -> {
            if(act == 0) {
                HomeAct homeAct = new HomeAct();
                homeAct.setImageGridViews(cntxt);
            } else if(act == 1) {
                EditPostActivity editPostActivity = new EditPostActivity();
                editPostActivity.setImageGridViews(cntxt);
            }
            finish();
        });
        discardLayout.setOnClickListener(v -> {
            return;
        });
        agreeDiscard.setOnClickListener(v -> {
            if(act == 0) {
                HomeAct homeAct = new HomeAct();
                HomeAct.selectedImages.clear();
                HomeAct.itemLists.clear();
                homeAct.setImageGridViews(cntxt);
            } else if(act == 1){
                EditPostActivity editPostActivity = new EditPostActivity();
                EditPostActivity.selectedImages.clear();
                EditPostActivity.itemLists.clear();
                editPostActivity.setImageGridViews(cntxt);
            }
            finish();
        });
        cancelDiscard.setOnClickListener(v -> {
            discardShow = false;
            discardLayout.setVisibility(View.GONE);
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
                    String[] pthPars = Objects.requireNonNull(pthPar).split("/");
                    if (!(pthPars[pthPars.length - 1].equals("LOST.DIR") || pthPars[pthPars.length - 1].equals(".thumbnails"))) {
                        String name = file.getName().toLowerCase();
                        for (String ext : Constants.allowedExt) {
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
            int listSize = HomeAct.selectedImages.size();
            if(act == 1)
                listSize = EditPostActivity.selectedImages.size();
            if (listSize > 0) {
                discardShow = true;
                discardLayout.setVisibility(View.VISIBLE);
            } else {
                finish();
            }
        }
    }

    private class ImageAdaptor extends BaseAdapter {


        private final Context context;
        private final int imageWidth;
        File[] itemList;
        Bitmap bitmap;

        public ImageAdaptor(Context context, File[] itemList, int imageWidth) {
            this.context = context;
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
            
            if(fileType.equals("video")){
                LinearLayout video = convertView.findViewById(R.id.video);
                TextView videoTime = convertView.findViewById(R.id.videoTime);
                video.setVisibility(View.VISIBLE);
                video.setVisibility(View.VISIBLE);
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                long timeInMillisec = Functions.getVideoDuration(retriever, filePath, context);
                String vidTime = Functions.convertMilliTime(timeInMillisec);
                videoTime.setText(vidTime);
                try {
                    retriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if(act == 0) {
                if (HomeAct.itemLists.contains(thisFile))
                    convertView.setBackgroundResource(R.drawable.border_box);
                else
                    convertView.setBackgroundResource(R.drawable.null_border);
                if (HomeAct.selectedImages.contains(filePath)) {
                    TextView txtNum = convertView.findViewById(R.id.numTxt);
                    int n = HomeAct.selectedImages.indexOf(filePath) + 1;
                    txtNum.setText(String.valueOf(n));
                    txtNum.setVisibility(View.VISIBLE);
                }
                if (HomeAct.selectedImages.size() > 0) {
                    String selectedNumTxt = String.valueOf(HomeAct.selectedImages.size());
                    selectedNumTxt += " Selected";
                    selectionsTxt.setText(selectedNumTxt);
                } else {
                    selectionsTxt.setText("");
                }
            }
            if(act == 1) {
                if (EditPostActivity.itemLists.contains(thisFile))
                    convertView.setBackgroundResource(R.drawable.border_box);
                else
                    convertView.setBackgroundResource(R.drawable.null_border);
                if (EditPostActivity.selectedImages.contains(filePath)) {
                    TextView txtNum = convertView.findViewById(R.id.numTxt);
                    int n = EditPostActivity.selectedImages.indexOf(filePath) + 1;
                    txtNum.setText(String.valueOf(n));
                    txtNum.setVisibility(View.VISIBLE);
                }
                int totalSize = EditPostActivity.selectedImages.size() + EditPostActivity.postImages.size();
                if (totalSize > 0) {
                    String selectedNumTxt = String.valueOf(totalSize);
                    selectedNumTxt += " Selected";
                    selectionsTxt.setText(selectedNumTxt);
                } else {
                    selectionsTxt.setText("");
                }
            }
            bitmap = Functions.decodeFiles(filePath, fileType, true);
            imageView.setImageBitmap(bitmap);
            View finalConvertView = convertView;
            imageView.setOnClickListener(v -> {
                if(act == 0) {
                    if (HomeAct.itemLists.contains(thisFile)) {
                        HomeAct.itemLists.remove(thisFile);
                        HomeAct.selectedImages.remove(filePath);
                        finalConvertView.setBackgroundResource(R.drawable.null_border);
                    } else {
                        HomeAct.itemLists.add(thisFile);
                        HomeAct.selectedImages.add(filePath);
                        finalConvertView.setBackgroundResource(R.drawable.border_box);
                    }
                    HomeAct.numberSelectedImages(gridView, selectionsTxt);
                }
                if(act == 1) {
                    if (EditPostActivity.itemLists.contains(thisFile)) {
                        EditPostActivity.itemLists.remove(thisFile);
                        EditPostActivity.selectedImages.remove(filePath);
                        finalConvertView.setBackgroundResource(R.drawable.null_border);
                    } else {
                        EditPostActivity.itemLists.add(thisFile);
                        EditPostActivity.selectedImages.add(filePath);
                        finalConvertView.setBackgroundResource(R.drawable.border_box);
                    }
                    EditPostActivity.numberSelectedImages(gridView, selectionsTxt);
                }
            });
            return convertView;
        }
    }
}
