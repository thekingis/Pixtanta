package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.HomeAct.changeTabTitle;
import static com.pixtanta.android.HomeAct.myIDtoString;
import static com.pixtanta.android.HomeAct.noteViewPager;
import static com.pixtanta.android.HomeAct.tabLayout;
import static com.pixtanta.android.RequestsFragment.getRequestsContents;

public class NotificationsFragment extends Fragment {

    int[] allIcons;
    Context cntxt;
    NestedScrollView nestedScrollView;
    ScrollView scrollView;
    LinearLayout layout;
    ArrayList<Integer> selectedNotes;
    ImageLoader imageLoader;
    Socket socket;
    ProgressBar progressBar;
    static JSONArray reqArrays;
    static boolean fragmentLoaded = false;
    static int reqNum;
    public static Handler UIHandler;
    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public NotificationsFragment(Context context, ProgressBar progressBar) {
        cntxt = context;
        this.progressBar = progressBar;
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        socket = HomeAct.socket;
        imageLoader = new ImageLoader(cntxt);
        selectedNotes = new ArrayList<>();
        allIcons = new int[]{
                R.drawable.tag_icon,
                R.drawable.like_icon,
                R.drawable.comment_icon,
                R.drawable.reply_icon
        };
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        nestedScrollView =  view.findViewById(R.id.nestedScrollView);
        scrollView =  view.findViewById(R.id.scrollView);
        layout =  view.findViewById(R.id.layout);

        socket.on("notify", args -> {
            if(HomeAct.noteLoaded) {
                try {
                    JSONObject object = new JSONObject(args[0].toString());
                    boolean isRequest = object.getBoolean("isRequest");
                    if (!isRequest)
                        setupNoteViews(object, 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        loadNotifications();

        return view;
    }

    public void loadNotifications() {
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("user", myIDtoString)
                .addFormDataPart("noteLoaded", String.valueOf(HomeAct.noteLoaded))
                .addFormDataPart("selectedDatas", selectedNotes.toString())
                .build();
        Request request = new Request.Builder()
                .url(Constants.notificationsUrl)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        try (Response response = call.execute()){
            if (response.isSuccessful()) {
                String data = Objects.requireNonNull(response.body()).string();
                JSONObject dataObj = new JSONObject(data);
                if(!HomeAct.noteLoaded){
                    HomeAct homeAct = new HomeAct();
                    homeAct.listenToNoteLoad(progressBar);
                    tabLayout.setVisibility(View.VISIBLE);
                    noteViewPager.setVisibility(View.VISIBLE);
                    reqNum = dataObj.getInt("reqNum");
                    String newTitle = "Requests(" + reqNum + ")";
                    changeTabTitle(newTitle, 1);
                    reqArrays = dataObj.getJSONArray("reqArrays");
                    if(fragmentLoaded)
                        getRequestsContents(cntxt, reqArrays, reqNum);
                    HomeAct.noteLoaded = true;
                }
                JSONArray jsonArray = dataObj.getJSONArray("notifications");
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    setupNoteViews(object, i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("InflateParams")
    private void setupNoteViews(JSONObject object, int i) throws JSONException {
        int id = object.getInt("id");
        String photo = object.getString("photo");
        String date = object.getString("date");
        String note = object.getString("note");
        boolean seen = object.getBoolean("seen");
        int type = object.getInt("type");
        int action = object.getInt("action");
        int dataId = object.getInt("dataId");
        int noteId = object.getInt("noteId");
        int extraId = object.getInt("extraId");
        int paramId = object.getInt("paramId");
        selectedNotes.add(id);
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.note_list, null);
        ImageView photoView = linearLayout.findViewById(R.id.photoView);
        ImageView iconView = linearLayout.findViewById(R.id.iconView);
        TextView noteText = linearLayout.findViewById(R.id.noteText);
        TextView noteDate = linearLayout.findViewById(R.id.noteDate);
        if(type < 4) {
            int imageIcon = allIcons[type];
            if ((type == 2 || type == 3) && (action == 4 || action == 5))
                imageIcon = R.drawable.tag_icon;
            iconView.setImageResource(imageIcon);
            iconView.setVisibility(View.VISIBLE);
        } else {
            photo = www + photo;
            imageLoader.displayImage(photo, photoView);
            photoView.setVisibility(View.VISIBLE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            noteText.setText(Html.fromHtml(note, Html.FROM_HTML_MODE_COMPACT));
        else
            noteText.setText(Html.fromHtml(note));
        noteDate.setText(date);
        if(!seen) {
            boolean darkThemeEnabled = new SharedPrefMngr(cntxt).getDarkThemeEnabled();
            int color;
            if(darkThemeEnabled)
                color = R.color.blash;
            else
                color = R.color.ash;
            linearLayout.setBackgroundResource(color);
        }
        linearLayout.setOnClickListener(v -> openNotification(linearLayout, id, type, action, dataId, noteId, extraId, paramId));
        runOnUI(() -> layout.addView(linearLayout, i));
    }

    private void openNotification(LinearLayout linearLayout, int id, int type, int action, int dataId, int noteId, int extraId, int paramId) {
        socket.emit("seenNote", id);
        Bundle params = new Bundle();
        Class activityClass;
        if(type == 0){
            activityClass = PostDisplayAct.class;
            params.putString("postId", String.valueOf(noteId));
        } else if(type == 1){
            if(action < 5){
                activityClass = PostDisplayAct.class;
                params.putString("postId", String.valueOf(noteId));
            } else if(action < 9){
                activityClass = CommentsActivity.class;
                params.putString("postID", String.valueOf(extraId));
                params.putString("commentID", String.valueOf(noteId));
            } else {
                activityClass = RepliesActivity.class;
                params.putString("postID", String.valueOf(paramId));
                params.putString("comID", String.valueOf(extraId));
                params.putString("replyID", String.valueOf(noteId));
            }
        } else if(type == 2){
            activityClass = CommentsActivity.class;
            params.putString("postID", String.valueOf(extraId));
            params.putString("commentID", String.valueOf(noteId));
        } else if(type == 3){
            activityClass = RepliesActivity.class;
            params.putString("postID", String.valueOf(paramId));
            params.putString("comID", String.valueOf(extraId));
            params.putString("replyID", String.valueOf(noteId));
        } else if(type == 4){
            if(action == 0) {
                activityClass = PageActivity.class;
                params.putInt("pageId", dataId);
            } else {
                activityClass = ProfileAct.class;
                params.putInt("userID", dataId);
            }
        } else {
            activityClass = ProfileAct.class;
            params.putInt("userID", dataId);
        }
        Intent intent = new Intent(cntxt, activityClass);
        intent.putExtras(params);
        startActivity(intent);
        boolean darkThemeEnabled = new SharedPrefMngr(cntxt).getDarkThemeEnabled();
        int color;
        if(darkThemeEnabled)
            color = R.color.black;
        else
            color = R.color.white;
        linearLayout.setBackgroundResource(color);
    }

    public static void getFragmentState(Context cntxt) throws Exception {
        fragmentLoaded = true;
        if(!(reqArrays == null))
            getRequestsContents(cntxt, reqArrays, reqNum);
    }

}