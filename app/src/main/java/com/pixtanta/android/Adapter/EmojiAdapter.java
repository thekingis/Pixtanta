package com.pixtanta.android.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pixtanta.android.R;

import java.util.List;

import io.github.rockerhieu.emojicon.EmojiconTextView;

public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder> {

    Context context;
    List<String> emojiList;
    EmojiAdapterListener listener;

    public EmojiAdapter(Context context, List<String> emojiList, EmojiAdapterListener listener) {
        this.context = context;
        this.emojiList = emojiList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.emoji_layout, parent, false);
        return new EmojiViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        holder.emojiconTextView.setText(emojiList.get(position));
    }

    @Override
    public int getItemCount() {
        return emojiList.size();
    }

    public class EmojiViewHolder extends RecyclerView.ViewHolder {
        EmojiconTextView emojiconTextView;
        public EmojiViewHolder(@NonNull View itemView) {
            super(itemView);
            emojiconTextView = (EmojiconTextView) itemView.findViewById(R.id.emojiconTextView);
            itemView.setOnClickListener(v -> listener.onEmojiItemSelected(emojiList.get(getAdapterPosition())));
        }

    }

    public interface EmojiAdapterListener {
        void onEmojiItemSelected(String emoji);
    }

}
