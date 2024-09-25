package com.pixtanta.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LinksAdapter extends RecyclerView.Adapter<LinksAdapter.LinksViewHolder> {

    Context context;
    JSONArray jsonArray;
    int count;
    static JSONArray array;
    static int treeCount = 0;

    public LinksAdapter(Context context, JSONArray jsonArray, int count) {
        this.context = context;
        this.jsonArray = jsonArray;
        this.count = count;
        array = new JSONArray();
    }

    @NonNull
    @Override
    public LinksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.links, parent, false);
        return new LinksViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LinksViewHolder holder, int position) {
        try {
            treeCount++;
            String object = jsonArray.getString(position);
            JSONObject object1 = new JSONObject();
            JSONObject jsonObject = new JSONObject(object);
            String imageUrl = jsonObject.getString("imageUrl");
            String title = jsonObject.getString("title");
            String description = jsonObject.getString("description");
            String host = jsonObject.getString("host");
            String linkUrl = jsonObject.getString("linkUrl");
            holder.linkTitle.setText(title);
            holder.linkDesc.setText(description);
            holder.linkHost.setText(host);
            if(!StringUtils.isEmpty(imageUrl)) {
                object1.put("imageUrl", imageUrl);
                object1.put("imageView", holder.linkImg);
                array.put(object1);
            }
            holder.itemView.setOnClickListener(v -> MessageAct.openLink(context, linkUrl));
            if(treeCount == count)
                loadImages(holder.itemView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadImages(View view) {
        if(array.length() > 0){
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver.OnGlobalLayoutListener that = this;
                    new android.os.Handler().postDelayed(() -> {
                        try {
                            for (int i = 0; i < array.length(); i++){
                                JSONObject object = array.getJSONObject(i);
                                String imageUrl = object.getString("imageUrl");
                                ImageView imageView = (ImageView) object.get("imageView");
                                Bitmap bitmap = Functions.getBitmapFromURL(imageUrl, true);
                                if(!(bitmap == null))
                                    imageView.setImageBitmap(bitmap);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        array = new JSONArray();
                        treeCount = 0;
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(that);
                    }, 500);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return jsonArray.length();
    }

    public static class LinksViewHolder extends RecyclerView.ViewHolder {
        ImageView linkImg;
        TextView linkTitle, linkDesc, linkHost;

        public LinksViewHolder(@NonNull View itemView) {
            super(itemView);
            linkImg = itemView.findViewById(R.id.linkImg);
            linkTitle = itemView.findViewById(R.id.linkTitle);
            linkDesc = itemView.findViewById(R.id.linkDesc);
            linkHost = itemView.findViewById(R.id.linkHost);
        }
    }
}
