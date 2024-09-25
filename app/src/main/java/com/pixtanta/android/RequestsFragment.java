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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.socket.client.Socket;

import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.HomeAct.changeTabTitle;
import static com.pixtanta.android.NotificationsFragment.getFragmentState;

public class RequestsFragment extends Fragment {

    Context cntxt;
    @SuppressLint("StaticFieldLeak")
    static RequestsFragment instance;
    NestedScrollView nestedScrollView;
    ScrollView scrollView;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout layout;
    static ImageLoader imageLoader;
    static Socket socket;
    static int reqNum;
    static int myId;
    SharedPrefMngr sharedPrefMngr;
    public static Handler UIHandler;
    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public static RequestsFragment getInstance(Context context){
        if(instance == null)
            instance = new RequestsFragment(context);
        return instance;
    }

    public RequestsFragment(Context context) {
        cntxt = context;
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        socket = HomeAct.socket;
        imageLoader = new ImageLoader(cntxt);
        sharedPrefMngr = new SharedPrefMngr(cntxt);
        myId = sharedPrefMngr.getMyId();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        nestedScrollView =  view.findViewById(R.id.nestedScrollView);
        scrollView =  view.findViewById(R.id.scrollView);
        layout =  view.findViewById(R.id.layout);
        if(layout.getChildCount() > 0)
            layout.removeAllViews();
        try {
            getFragmentState(cntxt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        socket.on("friend", args -> {
            if(HomeAct.noteLoaded) {
                try {
                    JSONObject emitObj = new JSONObject(args[0].toString());
                    int userFrom = emitObj.getInt("myId");
                    int tag = emitObj.getInt("tag");
                    if (userFrom == myId && tag == 2) {
                        int userTo = emitObj.getInt("user");
                        boolean rejected = emitObj.getBoolean("val");
                        String newText = "Request Accepted";
                        int textColor = R.color.green;
                        if (rejected) {
                            newText = "Request Rejected";
                            textColor = R.color.reder;
                        }
                        boolean darkThemeEnabled = new SharedPrefMngr(cntxt).getDarkThemeEnabled();
                        if(darkThemeEnabled)
                            textColor = R.color.white;
                        String viewTag = "friend-" + userTo;
                        LinearLayout linearLayout = (LinearLayout) layout.findViewWithTag(viewTag);
                        LinearLayout buttonHolder =  linearLayout.findViewById(R.id.buttonHolder);
                        TextView afterText =  linearLayout.findViewById(R.id.afterText);
                        reqNum--;
                        setTitle();
                        int finalTextColor = textColor;
                        String finalNewText = newText;
                        runOnUI(() -> {
                            afterText.setTextColor(ContextCompat.getColor(cntxt, finalTextColor));
                            afterText.setText(finalNewText);
                            afterText.setVisibility(View.VISIBLE);
                            buttonHolder.setVisibility(View.GONE);
                        });
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        socket.on("notify", args -> {
            if(HomeAct.noteLoaded) {
                try {
                    JSONObject emitObj = new JSONObject(args[0].toString());
                    boolean isRequest = emitObj.getBoolean("isRequest");
                    if (isRequest) {
                        reqNum++;
                        setTitle();
                        setupRequestView(cntxt, emitObj, 0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        socket.on("acceptFollow", args -> {
            if(HomeAct.noteLoaded) {
                try {
                    JSONObject emitObj = new JSONObject(args[0].toString());
                    int userFrom = emitObj.getInt("myId");
                    if (userFrom == myId) {
                        int userTo = emitObj.getInt("user");
                        boolean accepted = emitObj.getBoolean("val");
                        String newText = "Request Accepted";
                        int textColor = R.color.green;
                        if (!accepted) {
                            newText = "Request Rejected";
                            textColor = R.color.reder;
                        }
                        boolean darkThemeEnabled = new SharedPrefMngr(cntxt).getDarkThemeEnabled();
                        if(darkThemeEnabled)
                            textColor = R.color.white;
                        String viewTag = "acceptFollow-" + userTo;
                        LinearLayout linearLayout = (LinearLayout) layout.findViewWithTag(viewTag);
                        LinearLayout buttonHolder =  linearLayout.findViewById(R.id.buttonHolder);
                        TextView afterText =  linearLayout.findViewById(R.id.afterText);
                        reqNum--;
                        setTitle();
                        int finalTextColor = textColor;
                        String finalNewText = newText;
                        runOnUI(() -> {
                            afterText.setTextColor(ContextCompat.getColor(cntxt, finalTextColor));
                            afterText.setText(finalNewText);
                            afterText.setVisibility(View.VISIBLE);
                            buttonHolder.setVisibility(View.GONE);
                        });
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    public static void getRequestsContents(Context cntxt, JSONArray reqArrays, int rNum) throws Exception {
        reqNum = rNum;
        for(int i = 0; i < reqArrays.length(); i++){
            JSONObject object = reqArrays.getJSONObject(i);
            setupRequestView(cntxt, object, i);
        }
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private static void setupRequestView(Context cntxt, JSONObject object, int i) throws JSONException {
        final boolean[] val = new boolean[1];
        final int[] tag = new int[1], newTag = new int[1];
        final String tab = object.getString("tab");
        String photo = www + object.getString("photo");
        String note = object.getString("note");
        String date = object.getString("date");
        int userFrom = object.getInt("userFrom");
        boolean isFriendReq = object.getBoolean("isFriendReq");
        String viewTag = tab + "-" + userFrom;
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.request_list, null);
        LinearLayout buttonHolder =  linearLayout.findViewById(R.id.buttonHolder);
        ImageView photoView =  linearLayout.findViewById(R.id.photoView);
        TextView noteText =  linearLayout.findViewById(R.id.noteText);
        TextView afterText =  linearLayout.findViewById(R.id.afterText);
        TextView noteDate =  linearLayout.findViewById(R.id.noteDate);
        Button accept =  linearLayout.findViewById(R.id.accept);
        Button reject =  linearLayout.findViewById(R.id.reject);
        linearLayout.setTag(viewTag);
        imageLoader.displayImage(photo, photoView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            noteText.setText(Html.fromHtml(note, Html.FROM_HTML_MODE_COMPACT));
        else
            noteText.setText(Html.fromHtml(note));
        noteDate.setText(date);
        photoView.setOnClickListener(v -> visitUserProfile(userFrom, cntxt));
        accept.setOnClickListener(v -> {
            boolean darkThemeEnabled = new SharedPrefMngr(cntxt).getDarkThemeEnabled();
            int textColor;
            if(darkThemeEnabled)
                textColor = R.color.white;
            else
                textColor = R.color.green;
            afterText.setTextColor(ContextCompat.getColor(cntxt, textColor));
            afterText.setText("Request Accepted");
            afterText.setVisibility(View.VISIBLE);
            buttonHolder.setVisibility(View.GONE);
            tag[0] = 2;
            newTag[0] = 3;
            val[0] = !isFriendReq;
            boolean finalVal = val[0];
            int finalTag = tag[0];
            int finalNewTag = newTag[0];
            emitRequestAction(tab, userFrom, finalTag, finalNewTag, finalVal);
        });
        reject.setOnClickListener(v -> {
            boolean darkThemeEnabled = new SharedPrefMngr(cntxt).getDarkThemeEnabled();
            int textColor;
            if(darkThemeEnabled)
                textColor = R.color.white;
            else
                textColor = R.color.reder;
            afterText.setTextColor(ContextCompat.getColor(cntxt, textColor));
            afterText.setText("Request Rejected");
            afterText.setVisibility(View.VISIBLE);
            buttonHolder.setVisibility(View.GONE);
            tag[0] = 2;
            newTag[0] = 0;
            val[0] = isFriendReq;
            boolean finalVal = val[0];
            int finalTag = tag[0];
            int finalNewTag = newTag[0];
            emitRequestAction(tab, userFrom, finalTag, finalNewTag, finalVal);
        });
        runOnUI(() -> layout.addView(linearLayout, i));
    }

    private static void setTitle(){
        runOnUI(() -> {
            if(reqNum > -1){
                String newTitle = "Requests(" + reqNum + ")";
                changeTabTitle(newTitle, 1);
            }
        });
    }

    private static void emitRequestAction(String emitAction, int user, int tag, int newTag, boolean val){
        reqNum--;
        setTitle();
        try {
            JSONObject emitObj = new JSONObject();
            @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            emitObj.put("myId", myId);
            emitObj.put("user", user);
            emitObj.put("date", date);
            emitObj.put("tag", tag);
            emitObj.put("newTag", newTag);
            emitObj.put("val", val);
            socket.emit(emitAction, emitObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void visitUserProfile(int user, Context cntxt) {
        Intent intent = new Intent(cntxt, ProfileAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("userID", user);
        intent.putExtras(userParams);
        cntxt.startActivity(intent);
    }

}