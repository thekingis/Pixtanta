package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.SearchActivity.myId;
import static com.pixtanta.android.SearchActivity.saveSearch;
import static com.pixtanta.android.SearchActivity.searchText;

public class AllFragment extends Fragment {

    Context context;
    @SuppressLint("StaticFieldLeak")
    static ScrollView scrollViewAlF;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout layoutAlF, loadingLayerAlF;
    public static boolean searchedAlF = false, loadingAlF = false, allLoaded = false;
    static int verifiedIcon;
    public static JSONArray jArrayAlF;
    Animation rotation;
    static ArrayList<String> selectedDataPeopleAlF = new ArrayList<>(), selectedDataPagesAlF = new ArrayList<>();

    public AllFragment(Context context) {
        this.context = context;
        // Required empty public constructor
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all, container, false);
        scrollViewAlF = view.findViewById(R.id.scrollView);
        layoutAlF = view.findViewById(R.id.layout);
        loadingLayerAlF = view.findViewById(R.id.loadingLayer);
        verifiedIcon = R.drawable.ic_verified_user;

        if(jArrayAlF == null)
            jArrayAlF = new JSONArray();
        else {
            try {
                if(layoutAlF.getChildCount() > 0)
                    layoutAlF.removeAllViews();
                displayResultAlF(context, jArrayAlF, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        rotation = AnimationUtils.loadAnimation(context, R.anim.rotate);
        rotation.setFillAfter(true);

        scrollViewAlF.setSmoothScrollingEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollViewAlF.setOnScrollChangeListener((scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int scrollH = layoutAlF.getHeight() - scrllVw.getHeight() - 200;
                if(scrollY > scrollH && !loadingAlF && !allLoaded){
                    loadingAlF = true;
                    loadingLayerAlF = (LinearLayout) getLayoutInflater().inflate(R.layout.users_loader, null, false);
                    layoutAlF.addView(loadingLayerAlF);
                    ViewTreeObserver viewTreeObserver = loadingLayerAlF.getViewTreeObserver();
                    viewTreeObserver.addOnGlobalLayoutListener(() -> getSearchResultsAlF(context));
                }
            });
        }

        return view;
    }

    public static void displayResultAlF(Context context, JSONArray jsonArray, boolean merge) throws Exception {
        if(layoutAlF.getChildCount() > 0)
            layoutAlF.removeAllViews();
        searchedAlF = true;
        scrollViewAlF.setVisibility(View.VISIBLE);
        ImageLoader imageLoader = new ImageLoader(context);
        if(jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if (merge)
                    jArrayAlF.put(object);
                int dataId = object.getInt("dataId");
                boolean verified = object.getBoolean("verified");
                String name = object.getString("name");
                String userName = object.getString("userName");
                String photo = object.getString("photo");
                @SuppressLint("InflateParams") LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.search_lists, null);
                LinearLayout layoutView = linearLayout.findViewById(R.id.layout);
                ImageView imageView = linearLayout.findViewById(R.id.photo);
                TextView textView = linearLayout.findViewById(R.id.name);
                TextView textViewUN = linearLayout.findViewById(R.id.userName);
                imageLoader.displayImage(www + photo, imageView);
                textView.setText(name);
                if (!StringUtils.isEmpty(userName)) {
                    userName = "@" + userName;
                    textViewUN.setVisibility(View.VISIBLE);
                    textViewUN.setText(userName);
                }
                if (verified)
                    textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                linearLayout.setTag(dataId);
                layoutView.setOnClickListener(v -> {
                    try {
                        saveSearch(context, object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                layoutAlF.addView(linearLayout);
            }
        } else if(jsonArray.length() == 0 && jArrayAlF.length() == 0){
            boolean darkThemeEnabled = new SharedPrefMngr(context).getDarkThemeEnabled();
            int color;
            if(darkThemeEnabled)
                color = R.color.white;
            else
                color = R.color.black;
            String text = "No result found";
            TextView newTextView = new TextView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = 300;
            newTextView.setPadding(0, 15, 0, 15);
            newTextView.setLayoutParams(params);
            newTextView.setTextColor(ContextCompat.getColor(context, color));
            newTextView.setText(text);
            newTextView.setTextSize(18f);
            newTextView.setGravity(Gravity.CENTER);
            layoutAlF.addView(newTextView);
        }
    }

    public static void getSearchResultsAlF(Context context) {
        searchedAlF = true;
        scrollViewAlF.setVisibility(View.GONE);
        loadingLayerAlF.setVisibility(View.VISIBLE);
        loadingAlF = true;
        while (loadingAlF){
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(myId))
                    .addFormDataPart("searchText", searchText)
                    .addFormDataPart("category", "search")
                    .addFormDataPart("selectedDataPeople", selectedDataPeopleAlF.toString())
                    .addFormDataPart("selectedDataPages", selectedDataPagesAlF.toString())
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.searchUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    loadingAlF = false;
                    loadingLayerAlF.setVisibility(View.GONE);
                    scrollViewAlF.setVisibility(View.VISIBLE);
                    JSONObject jsonObject = new JSONObject(responseString);
                    allLoaded = jsonObject.getBoolean("allLoaded");
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    displayResultAlF(context, jsonArray, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}