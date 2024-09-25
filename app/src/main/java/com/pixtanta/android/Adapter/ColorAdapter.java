package com.pixtanta.android.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pixtanta.android.R;

import java.util.ArrayList;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    Context context;
    ArrayList<Integer> colorList = new ArrayList<>();
    ColorAdapterListener listener;

    public ColorAdapter(Context context, ColorAdapterListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.color_picker, parent, false);
        return new ColorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        holder.colorPicker.setBackgroundColor(colorList.get(position));
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public class ColorViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout colorPicker;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorPicker = (LinearLayout) itemView.findViewById(R.id.colorPicker);
            itemView.setOnClickListener(v -> listener.onColorSelected(colorList.get(getAdapterPosition())));
        }
    }

    public interface ColorAdapterListener {
        void onColorSelected(int color);
    }

}
