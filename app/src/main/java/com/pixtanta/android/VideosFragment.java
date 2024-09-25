package com.pixtanta.android;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.pixtanta.android.Utils.ContextData;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.HomeAct.openImage;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideosFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public static GridView gridView;
    LinearLayout layout;
    ImageAdaptor imageAdaptor;
    Context cntxt;
    ArrayList<String> files = new ArrayList<>();
    ArrayList<String> selectedDatas = new ArrayList<>(), loadedArr = new ArrayList<>(), videosArr = new ArrayList<>();
    boolean loadingPost = false, allLoaded = false, firstLoad = true, loadedVideos;
    int imgW, gridViewPos, width, user;

    public VideosFragment(ContextData contextData) {
        cntxt = contextData.context;
        user = contextData.dataId;
        width = contextData.width;
        loadedVideos = contextData.loadedVideos;
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int w = width - 5;
        imgW = (w / 3) - 10;
        View view = inflater.inflate(R.layout.fragment_videos, container, false);
        gridView =  view.findViewById(R.id.gridView);
        layout =  view.findViewById(R.id.layout);

        //GetFileDisplay getFileDisplay = new GetFileDisplay();
        //getFileDisplay.execute(new String[]{});
        gridView.setSmoothScrollbarEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if(!allLoaded && loadedVideos && firstVisibleItem + visibleItemCount >= totalItemCount){
                        gridViewPos = (gridView.getLastVisiblePosition() + 1) / 3;
                        getFileDisplay();
                    }
                }
            });
        }

        return view;
    }

    public void getFileDisplay(){
        loadingPost = true;
        try {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("dataId", String.valueOf(user))
                    .addFormDataPart("dataType", "profile")
                    .addFormDataPart("fileType", "video")
                    .addFormDataPart("selectedDatas", selectedDatas.toString().intern())
                    .addFormDataPart("loadedArr", loadedArr.toString().intern())
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.fileDisplayUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseString = Objects.requireNonNull(response.body()).string();
                loadingPost = false;
                loadedVideos = true;
                JSONArray jsonArray = new JSONArray(responseString);
                String selectedIds = jsonArray.getString(0);
                String selectedFiles = jsonArray.getString(1);
                allLoaded = jsonArray.getBoolean(2);
                JSONArray selectedDatasArr = new JSONArray(selectedIds);
                JSONArray selectedFilesArr = new JSONArray(selectedFiles);
                if(firstLoad){
                    firstLoad = false;
                    if(selectedFilesArr.length() == 0){
                        layout.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.GONE);
                        return;
                    }
                }
                for(int i = 0; i < selectedDatasArr.length(); i++){
                    String id = selectedDatasArr.getString(i);
                    selectedDatas.add(id);
                }
                for(int x = 0; x < selectedFilesArr.length(); x++){
                    String file = selectedFilesArr.getString(x);
                    files.add(file);
                    videosArr.add(www+file);
                    String fileUrl = "\""+file+"\"";
                    loadedArr.add(fileUrl);
                }
                imageAdaptor = new ImageAdaptor(files, imgW);
                gridView.setAdapter(imageAdaptor);
                gridView.setSelection(gridViewPos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ImageAdaptor extends BaseAdapter {


        private final int imageWidth;
        ArrayList<String> itemList;

        public ImageAdaptor(ArrayList<String> itemList, int imageWidth) {
            this.itemList = itemList;
            this.imageWidth = imageWidth;

        }

        @Override
        public int getCount() {
            return this.itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return this.itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            String filePath = itemList.get(position);
            convertView = getLayoutInflater().inflate(R.layout.image_box, parent, false);
            convertView.setLayoutParams(new GridView.LayoutParams(imageWidth ,imageWidth ));
            ImageView imageView = convertView.findViewById(R.id.imgView);
            LinearLayout video = convertView.findViewById(R.id.video);
            TextView videoTime = convertView.findViewById(R.id.videoTime);
            video.setVisibility(View.VISIBLE);
            video.setVisibility(View.VISIBLE);
            try {
                Bitmap bitmap = Functions.getVideoThumbnail(Constants.www + filePath, true);
                imageView.setImageBitmap(bitmap);
                String vidTime = Functions.getMediaTime(Constants.www + filePath);
                videoTime.setText(vidTime);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            imageView.setOnClickListener(v -> openImage(videosArr, position, cntxt));
            return convertView;
        }
    }

}
