package com.pixtanta.android;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pixtanta.android.Adapter.EmojiAdapter;
import com.pixtanta.android.Interface.EmojiFragmentListener;

import ja.burhanrashid52.photoeditor.PhotoEditor;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmojiFragment extends Fragment implements EmojiAdapter.EmojiAdapterListener {

    RecyclerView emojiCycle;
    EmojiFragmentListener listener;
    static EmojiFragment instance;

    public static EmojiFragment getInstance(){
        if(instance == null)
            instance = new EmojiFragment();
        return instance;
    }

    public void setListener(EmojiFragmentListener listener) {
        this.listener = listener;
    }

    public EmojiFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_emoji, container, false);
        emojiCycle = itemView.findViewById(R.id.emojiCycle);
        emojiCycle.setHasFixedSize(true);
        emojiCycle.setLayoutManager(new GridLayoutManager(getActivity(), 6));

        EmojiAdapter adapter = new EmojiAdapter(getContext(), PhotoEditor.getEmojis(requireContext()), this);
        emojiCycle.setAdapter(adapter);

        return itemView;
    }

    @Override
    public void onEmojiItemSelected(String emoji) {
        listener.onEmojiSelected(emoji);
    }
}
