package com.pixtanta.android;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import io.socket.client.Socket;

import static com.pixtanta.android.Constants.www;


/**
 * A simple {@link Fragment} subclass.
 */
public class ActiveFragment extends Fragment {

    static int myId;
    Context cntxt;
    ScrollView scrllView;
    LinearLayout usersDis;
    JSONObject jsonObject = new JSONObject();
    TextView actText;
    Socket socket;
    public static Handler UIHandler;
    static ImageLoader imageLoader;
    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public ActiveFragment(Context context) {
        cntxt = context;
        // Required empty public constructor
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        socket = InboxAct.socket;
        myId = InboxAct.myId;
        View view = inflater.inflate(R.layout.fragment_active, container, false);
        scrllView = view.findViewById(R.id.scrllView);
        usersDis = view.findViewById(R.id.usersDis);
        actText = (TextView) getLayoutInflater().inflate(R.layout.tells, null, false);
        imageLoader = new ImageLoader(cntxt);
        usersDis.addView(actText);

        socket.on("activeUsers", args -> {
            try {
                JSONObject jsonObject = new JSONObject(args[0].toString());
                if(usersDis.getChildCount() > 0) {
                    runOnUI(() -> usersDis.removeAllViews());
                }
                if(jsonObject.length() == 0) {
                    runOnUI(() -> usersDis.addView(actText));
                } else {
                    JSONArray jsonArray = jsonObject.names();
                    int ic_online = R.drawable.ic_online;
                    for(int i = 0; i < Objects.requireNonNull(jsonArray).length(); i++){
                        String userName = jsonArray.getString(i);
                        JSONArray userArray = jsonObject.getJSONArray(userName);
                        int user = userArray.getInt(0);
                        String photo = userArray.getString(1);
                        String fName = userArray.getString(2);
                        String lName = userArray.getString(3);
                        String name = fName + " " + lName;
                        photo = www + photo;
                        jsonObject.put(userName, user);
                        @SuppressLint("InflateParams") LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.search_lists, null);
                        LinearLayout layout = linearLayout.findViewById(R.id.layout);
                        ImageView imageView = linearLayout.findViewById(R.id.photo);
                        TextView textView = linearLayout.findViewById(R.id.name);
                        linearLayout.setTag(userName);
                        imageLoader.displayImage(photo, imageView);
                        textView.setText(name);
                        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, ic_online, 0);
                        layout.setOnClickListener(v -> openMessage(cntxt, user));
                        runOnUI(() -> usersDis.addView(linearLayout));
                    }
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        });
        socket.on("active", args -> {
            try {
                JSONArray jsonArray = new JSONArray(args[0].toString());
                String userName = jsonArray.getString(1);
                if(!jsonObject.has(userName)) {
                    TextView textViewX = usersDis.findViewById(R.id.actText);
                    int ic_online = R.drawable.ic_online;
                    int user = jsonArray.getInt(0);
                    String photo = jsonArray.getString(2);
                    String fName = jsonArray.getString(3);
                    String lName = jsonArray.getString(4);
                    String name = fName + " " + lName;
                    photo = www + photo;
                    jsonObject.put(userName, user);
                    @SuppressLint("InflateParams") LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.search_lists, null);
                    LinearLayout layout = linearLayout.findViewById(R.id.layout);
                    ImageView imageView = linearLayout.findViewById(R.id.photo);
                    TextView textView = linearLayout.findViewById(R.id.name);
                    linearLayout.setTag(userName);
                    imageLoader.displayImage(photo, imageView);
                    textView.setText(name);
                    textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, ic_online, 0);
                    layout.setOnClickListener(v -> openMessage(cntxt, user));
                    runOnUI(() -> {
                        usersDis.removeView(textViewX);
                        usersDis.addView(linearLayout);
                    });
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        });
        socket.on("inactive", args -> {
            String userName = args[0].toString();
            LinearLayout linearLayout = usersDis.findViewWithTag(userName);
            if(jsonObject.has(userName))
                jsonObject.remove(userName);
            if(!(linearLayout == null)) {
                runOnUI(() -> {
                    usersDis.removeView(linearLayout);
                    if(usersDis.getChildCount() == 0)
                        usersDis.addView(actText);
                });
            }
        });

        return view;
    }

    private static void openMessage(Context cntxt, int user) {
        Intent intent = new Intent(cntxt, MessageAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("user", user);
        intent.putExtras(userParams);
        cntxt.startActivity(intent);
    }

}
