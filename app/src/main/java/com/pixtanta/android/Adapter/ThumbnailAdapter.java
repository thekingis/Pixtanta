package com.pixtanta.android.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pixtanta.android.Interface.FiltersListFragmentListener;
import com.pixtanta.android.R;
import com.pixtanta.android.Functions;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.MyViewHolder> {

    private final List<ThumbnailItem> thumbnailItems;
    private final FiltersListFragmentListener listener;
    private final Context context;
    private int selectedIndex = 0;

    public ThumbnailAdapter(List<ThumbnailItem> thumbnailItems, FiltersListFragmentListener listener, Context context) {
        this.thumbnailItems = thumbnailItems;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.thumbnail_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ThumbnailItem thumbnailItem = thumbnailItems.get(position);
        Bitmap bitmap = Functions.decodeBitmap(thumbnailItem.image);
        holder.thumbnail.setImageBitmap(bitmap);
        holder.thumbnail.setOnClickListener(v -> {
            listener.onFilterSelected(thumbnailItem.filter);
            selectedIndex = position;
            notifyDataSetChanged();
        });
        LinearLayout thumbnailHolder = (LinearLayout) holder.thumbnail.getParent();

        holder.filterName.setText(thumbnailItem.filterName);
        if(selectedIndex == position) {
            holder.filterName.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryRed));
            thumbnailHolder.setBackgroundResource(R.drawable.border_box);
        } else {
            holder.filterName.setTextColor(ContextCompat.getColor(context, R.color.normal_filter));
            thumbnailHolder.setBackgroundResource(R.drawable.null_border);
        }
    }

    @Override
    public int getItemCount() {
        return thumbnailItems.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView filterName;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            filterName = (TextView) itemView.findViewById(R.id.filterName);
        }
    }
}
