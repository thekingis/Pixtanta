package com.pixtanta.android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pixtanta.android.Adapter.ThumbnailAdapter;
import com.pixtanta.android.Interface.FiltersListFragmentListener;
import com.pixtanta.android.Utils.SpaceItemDecoration;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.List;

import static com.pixtanta.android.PhotoEditorAct.saveEditState;

public class FiltersListFragment extends Fragment implements FiltersListFragmentListener {

    RecyclerView recyclerView;
    ThumbnailAdapter adapter;
    List<ThumbnailItem> thumbnailItems;
    FiltersListFragmentListener listener;
    Bitmap editorBitmap = PhotoEditorAct.bitmap;

    public void setListener(FiltersListFragmentListener listener) {
        this.listener = listener;
    }

    public FiltersListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        View itemView = inflater.inflate(R.layout.fragment_filter_list, container, false);
        thumbnailItems = new ArrayList<>();
        adapter = new ThumbnailAdapter(thumbnailItems, this, getActivity());
        recyclerView = itemView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new SpaceItemDecoration(space));
        recyclerView.setAdapter(adapter);
        displayThumbnail();

        return  itemView;
    }

    private void displayThumbnail() {
        Runnable runnable = () -> {
            Bitmap thumbImg;
            if(editorBitmap == null)
                return;
            thumbImg = editorBitmap;
            thumbImg = Functions.decodeBitmap(thumbImg);
            ThumbnailsManager.clearThumbs();
            thumbnailItems.clear();
            ThumbnailItem thumbnailItem = new ThumbnailItem();
            thumbnailItem.filterName = "Normal";
            thumbnailItem.image = thumbImg;
            ThumbnailsManager.addThumb(thumbnailItem);
            List<Filter> filters = FilterPack.getFilterPack(requireActivity());
            for (Filter filter : filters){
                ThumbnailItem ti = new ThumbnailItem();
                ti.image = thumbImg;
                ti.filterName = filter.getName();
                ti.filter = filter;
                ThumbnailsManager.addThumb(ti);
            }
            thumbnailItems.addAll(ThumbnailsManager.processThumbs(getActivity()));
            requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

        };
        new Thread(runnable).start();
    }

    @Override
    public void onFilterSelected(Filter filter) {
        if(listener != null) {
            listener.onFilterSelected(filter);
            saveEditState(getContext());
        }
    }
}
