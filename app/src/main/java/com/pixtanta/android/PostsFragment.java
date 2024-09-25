package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.pixtanta.android.Utils.HtmlParser;
import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
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
import static com.pixtanta.android.SearchActivity.searchText;
import static com.pixtanta.android.SearchActivity.visitPage;
import static com.pixtanta.android.SearchActivity.visitUserProfile;

public class PostsFragment extends Fragment {

    Context context;
    @SuppressLint("StaticFieldLeak")
    static ScrollView scrollViewPoF;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout layoutPoF, loadingLayerPoF;
    public static boolean searchedPoF = false, loadingPoF = false, allLoaded = false;
    static int verifiedIcon;
    public static JSONArray jArrayPoF;
    Animation rotation;
    static ArrayList<Integer> selectedDatasPost = new ArrayList<>(), selectedDatasCom = new ArrayList<>(), selectedDatasRep = new ArrayList<>();

    public PostsFragment(Context context) {
        this.context = context;
        // Required empty public constructor
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        scrollViewPoF = view.findViewById(R.id.scrollView);
        layoutPoF = view.findViewById(R.id.layout);
        loadingLayerPoF = view.findViewById(R.id.loadingLayer);
        verifiedIcon = R.drawable.ic_verified_user;

        if(jArrayPoF == null)
            jArrayPoF = new JSONArray();
        else {
            try {
                if(layoutPoF.getChildCount() > 0)
                    layoutPoF.removeAllViews();
                displayResultPoF(context, jArrayPoF, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        rotation = AnimationUtils.loadAnimation(context, R.anim.rotate);
        rotation.setFillAfter(true);

        scrollViewPoF.setSmoothScrollingEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollViewPoF.setOnScrollChangeListener((scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int scrollH = layoutPoF.getHeight() - scrllVw.getHeight() - 200;
                if(scrollY > scrollH && !loadingPoF && !allLoaded){
                    loadingPoF = true;
                    loadingLayerPoF = (LinearLayout) getLayoutInflater().inflate(R.layout.loading_layer, null, false);
                    layoutPoF.addView(loadingLayerPoF);
                    ViewTreeObserver viewTreeObserver = loadingLayerPoF.getViewTreeObserver();
                    viewTreeObserver.addOnGlobalLayoutListener(() -> getSearchResultsPoF(context));
                }
            });
        }

        return view;
    }

    public static void displayResultPoF(Context context, JSONArray jsonArray, boolean merge) throws Exception {
        if(layoutPoF.getChildCount() > 0)
            layoutPoF.removeAllViews();
        searchedPoF = true;
        scrollViewPoF.setVisibility(View.VISIBLE);
        ImageLoader imageLoader = new ImageLoader(context);
        if(jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if (merge)
                    jArrayPoF.put(object);
                int id = object.getInt("id");
                String tab = object.getString("tab");
                String postId = object.getString("postId");
                String comId = object.getString("comId");
                String repId = object.getString("repId");
                String photo = object.getString("photo");
                String name = object.getString("name");
                String userName = object.getString("userName");
                String type = object.getString("type");
                String text = object.getString("text");
                String files = object.getString("files");
                String date = object.getString("date");
                int fromId = object.getInt("fromId");
                boolean verified = object.getBoolean("verified");
                String viewText = "View " + tab;
                @SuppressLint("InflateParams") RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.tags_layout, null);
                ImageView imageView = relativeLayout.findViewById(R.id.posterPht);
                TextView nameTextVw = relativeLayout.findViewById(R.id.posterName);
                TextView nameUTextVw = relativeLayout.findViewById(R.id.posterUName);
                TextView textTextVw = relativeLayout.findViewById(R.id.postText);
                TextView filesTextVw = relativeLayout.findViewById(R.id.files);
                TextView dateTextVw = relativeLayout.findViewById(R.id.postDate);
                TextView clickTextVw = relativeLayout.findViewById(R.id.clickView);
                switch (tab) {
                    case "Post":
                        selectedDatasPost.add(id);
                        break;
                    case "Comment":
                        selectedDatasCom.add(id);
                        break;
                    case "Reply":
                        selectedDatasRep.add(id);
                        break;
                }
                photo = www + photo;
                imageLoader.displayImage(photo, imageView);
                nameTextVw.setText(name);
                if (verified)
                    nameTextVw.setCompoundDrawablesWithIntrinsicBounds(0, 0, verifiedIcon, 0);
                if (StringUtils.isEmpty(userName))
                    nameUTextVw.setVisibility(View.GONE);
                else {
                    userName = "@" + userName;
                    nameUTextVw.setText(userName);
                }
                String htmlText = HtmlParser.parseSpan(text);
                CharSequence sequence = Html.fromHtml(htmlText);
                SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                textTextVw.setText(strBuilder);
                textTextVw.setMovementMethod(LinkMovementMethod.getInstance());
                filesTextVw.setText(files);
                dateTextVw.setText(date);
                clickTextVw.setText(viewText);
                nameTextVw.setOnClickListener(v -> {
                    switch (type) {
                        case "page":
                            visitPage(context, fromId);
                            break;
                        case "profile":
                            visitUserProfile(context, fromId);
                            break;
                    }
                });
                clickTextVw.setOnClickListener(v -> openPostPage(context, tab, postId, comId, repId));
                layoutPoF.addView(relativeLayout);
            }
        } else if(jsonArray.length() == 0 && jArrayPoF.length() == 0){
            boolean darkThemeEnabled = new SharedPrefMngr(context).getDarkThemeEnabled();
            int color;
            if(darkThemeEnabled)
                color = ContextCompat.getColor(context, R.color.white);
            else
                color = ContextCompat.getColor(context, R.color.black);
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
            layoutPoF.addView(newTextView);
        }
    }

    private static void openPostPage(Context context, String key, String postId, String comId, String repId){
        Intent intent;
        Bundle bundle = new Bundle();
        switch (key){
            case "Post":
                intent = new Intent(context, PostDisplayAct.class);
                bundle.putString("postId", postId);
                intent.putExtras(bundle);
                context.startActivity(intent);
                break;
            case "Comment":
                intent = new Intent(context, CommentsActivity.class);
                bundle.putString("postID", postId);
                bundle.putString("commentID", comId);
                intent.putExtras(bundle);
                context.startActivity(intent);
                break;
            case "Reply":
                intent = new Intent(context, RepliesActivity.class);
                bundle.putString("postID", postId);
                bundle.putString("comID", comId);
                bundle.putString("replyID", repId);
                intent.putExtras(bundle);
                context.startActivity(intent);
                break;
        }
    }

    public static void getSearchResultsPoF(Context context) {
        searchedPoF = true;
        scrollViewPoF.setVisibility(View.GONE);
        loadingLayerPoF.setVisibility(View.VISIBLE);
        loadingPoF = true;
        while (loadingPoF){
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(myId))
                    .addFormDataPart("searchText", searchText)
                    .addFormDataPart("category", "posts")
                    .addFormDataPart("selectedDatasPost", selectedDatasPost.toString())
                    .addFormDataPart("selectedDatasRep", selectedDatasRep.toString())
                    .addFormDataPart("selectedDatasCom", selectedDatasCom.toString())
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
                    loadingPoF = false;
                    loadingLayerPoF.setVisibility(View.GONE);
                    scrollViewPoF.setVisibility(View.VISIBLE);
                    JSONObject jsonObject = new JSONObject(responseString);
                    allLoaded = jsonObject.getBoolean("allLoaded");
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    displayResultPoF(context, jsonArray, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}