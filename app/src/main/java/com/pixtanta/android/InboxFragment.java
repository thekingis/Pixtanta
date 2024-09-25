package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.Constants.www;
import static com.pixtanta.android.HomeAct.popMessage;
import static com.pixtanta.android.HomeAct.updateMessageDelivery;
import static com.pixtanta.android.InboxAct.changeTabTitle;
import static com.pixtanta.android.InboxAct.hrzntlScrllVw;
import static com.pixtanta.android.InboxAct.saveSearch;
import static com.pixtanta.android.InboxAct.searchHistoryLayout;
import static com.pixtanta.android.InboxAct.setRectors;
import static com.pixtanta.android.InboxAct.setupUI;
import static com.pixtanta.android.InboxAct.showMessageOptions;
import static com.pixtanta.android.InboxAct.updatedArchs;
import static com.pixtanta.android.InboxAct.updatedBlocks;
import static com.pixtanta.android.InboxAct.updatedFavs;
import static com.pixtanta.android.InboxAct.updatedOffs;
import static com.pixtanta.android.InboxAct.updatedSounds;


/**
 * A simple {@link Fragment} subclass.
 */
public class InboxFragment extends Fragment {

    NestedScrollView postScrllVw;
    @SuppressLint("StaticFieldLeak")
    public static NestedScrollView favScrllVw, arcScrllVw;
    ProgressBar progressBar;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout msgDis, favDis, arcDis;
    static int myId;
    long maxTime;
    Context cntxt;
    Activity actvty;
    @SuppressLint("StaticFieldLeak")
    static TextView archText, favText;
    String lang = InboxAct.lang, maxDate = "0000-00-00 00:00:00";
    static ImageLoader imageLoader;
    public static boolean msgLoaded, firstLoad, maxReached, loading;
    Socket socket;
    static JSONObject jsonObject = new JSONObject(), typers = new JSONObject(), favouriteObj, archiveObj;
    static JSONArray messageArray, searchedHistory;
    public static Handler UIHandler;
    @SuppressLint("StaticFieldLeak")
    public static NestedScrollView nestedScrollView = null;
    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public InboxFragment(Context context, Activity activity) {
        cntxt = context;
        actvty = activity;
        // Required empty public constructor
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        msgLoaded = false;
        firstLoad = true;
        maxReached = false;
        loading = false;
        socket = InboxAct.socket;
        myId = InboxAct.myId;
        socket.emit("connectedInboxPages", myId);
        favouriteObj = new JSONObject();
        archiveObj = new JSONObject();
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        postScrllVw = view.findViewById(R.id.postScrllVw);
        favScrllVw = view.findViewById(R.id.favScrllVw);
        arcScrllVw = view.findViewById(R.id.arcScrllVw);
        msgDis = view.findViewById(R.id.msgDis);
        favDis = view.findViewById(R.id.favDis);
        arcDis = view.findViewById(R.id.arcDis);
        progressBar = view.findViewById(R.id.progressBar);
        archText = view.findViewById(R.id.archText);
        favText = view.findViewById(R.id.favText);
        messageArray = SaveOpenedMessages.messageArray;
        imageLoader = new ImageLoader(cntxt);
        if (!(messageArray == null)) {
            try {
                maxDate = messageArray.getString(0);
                maxReached = messageArray.getBoolean(1);
                maxTime = messageArray.getLong(3);
                favouriteObj = messageArray.getJSONObject(4);
                archiveObj = messageArray.getJSONObject(5);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setupUI(cntxt, postScrllVw);
        openSelected(cntxt, -1, -1, null, null);
        nonStopCounter();

        if(firstLoad)
            new android.os.Handler().postDelayed(this::loadMsgContents, 100);
        else {
            if (!(messageArray == null)) {
                progressBar.setVisibility(View.GONE);
                msgDis.removeView(progressBar);
                msgDis.removeAllViews();
                try {
                    JSONObject msgObj = new JSONObject(messageArray.getString(2));
                    displayChats(msgObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            postScrllVw.setOnScrollChangeListener((View.OnScrollChangeListener) (scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if(!InboxAct.aBoolean){
                    scrllVw.scrollTo(0, 0);
                    int nScrllTop = InboxAct.nest.getScrollY();
                    int scrllTop = nScrllTop + scrollY;
                    InboxAct.nest.scrollTo(0, scrllTop);
                    return;
                }
                if(!firstLoad){
                    int scrollH = msgDis.getHeight() - postScrllVw.getHeight() - InboxAct.height - 500;
                    if(scrollY > scrollH && !maxReached && !loading){
                        loading = true;
                        progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.image_loader, null);
                        msgDis.addView(progressBar);
                        progressBar.post(this::loadMsgContents);
                    }
                }
            });
            favScrllVw.setOnScrollChangeListener((View.OnScrollChangeListener) (scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if(!InboxAct.aBoolean){
                    scrllVw.scrollTo(0, 0);
                    int nScrllTop = InboxAct.nest.getScrollY();
                    int scrllTop = nScrllTop + scrollY;
                    InboxAct.nest.scrollTo(0, scrllTop);
                }
            });
            arcScrllVw.setOnScrollChangeListener((View.OnScrollChangeListener) (scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if(!InboxAct.aBoolean){
                    scrllVw.scrollTo(0, 0);
                    int nScrllTop = InboxAct.nest.getScrollY();
                    int scrllTop = nScrllTop + scrollY;
                    InboxAct.nest.scrollTo(0, scrllTop);
                }
            });
        }

        socket.on("submitMessage", args -> {
            try {
                JSONObject msgData = new JSONObject(args[0].toString());
                int msgId = msgData.getInt("msgId");
                long time = msgData.getLong("time");
                int userFrom = msgData.getInt("userFrom");
                int userTo = msgData.getInt("userTo");
                int msgCnt = msgData.getInt("msgCnt");
                int seen = msgData.getInt("seen");
                String msgBody = msgData.getString("msgBody");
                String date = msgData.getString("date");
                String files = msgData.getString("files");
                String userFromInfo = msgData.getString("userFromInfo");
                String userToInfo = msgData.getString("userToInfo");
                boolean audioType = msgData.getBoolean("audioType");
                boolean blocked = msgData.getBoolean("blocked");
                int dlvd = msgData.getInt("dlvd");
                if(archiveObj.has(String.valueOf(msgId)))
                    switchArchive(cntxt, actvty, String.valueOf(msgId), true, false);
                JSONObject userInfo = new JSONObject(userFromInfo);
                if(myId == userFrom)
                    userInfo = new JSONObject(userToInfo);
                String photo = userInfo.getString("photo");
                String name = userInfo.getString("name");
                String msgNum = "";
                if(userTo == myId && msgCnt > 0)
                    msgNum = "(" + msgCnt + ")";
                int user = userFrom;
                if(myId == userFrom)
                    user = userTo;
                JSONObject msgObject = new JSONObject();
                msgObject.put("msgId", msgId);
                msgObject.put("time", time);
                msgObject.put("photo", photo);
                msgObject.put("name", name);
                msgObject.put("msgdUser", userFrom);
                msgObject.put("lastMsg", msgBody);
                msgObject.put("files", files);
                msgObject.put("audioType", audioType);
                msgObject.put("msgNum", msgNum);
                msgObject.put("user", user);
                msgObject.put("seen", seen);
                msgObject.put("dlvd", dlvd);
                msgObject.put("date", date);
                msgObject.put("blocked", blocked);
                JSONObject object = messageArray.getJSONObject(2);
                JSONObject newObject = new JSONObject();
                newObject.put(String.valueOf(msgId), msgObject);
                LinearLayout linearLayout = msgDis.findViewWithTag(msgId);
                if(!(linearLayout == null)) {
                    object.remove(String.valueOf(msgId));
                    runOnUI(() -> msgDis.removeView(linearLayout));
                }
                for(int i = 0; i < object.length(); i++){
                    String key = Objects.requireNonNull(object.names()).getString(i);
                    JSONObject msgObjs = object.getJSONObject(key);
                    newObject.put(key, msgObjs);
                }
                messageArray.put(2, newObject);
                saveMessageContent();
                LinearLayout msgListView = setUpChat(cntxt, actvty, msgObject);
                runOnUI(() -> msgDis.addView(msgListView, 0));
                updateMessageDelivery(myId);
            } catch (JSONException e){
                e.printStackTrace();
            }
        });
        socket.on("deleteMessage", args -> {
            try {
                JSONObject argsArr = new JSONObject(args[0].toString());
                int msgId = argsArr.getInt("msgId");
                boolean legit = argsArr.getBoolean("legit");
                LinearLayout linearLayout = msgDis.findViewWithTag(msgId);
                if(!(linearLayout == null) && legit) {
                    JSONObject msgData = argsArr.getJSONObject("msgData");
                    long time = msgData.getLong("curTime");
                    JSONObject object1 = messageArray.getJSONObject(2);
                    runOnUI(() -> msgDis.removeView(linearLayout));
                    if(!(time > maxTime)) {
                        object1.remove(String.valueOf(msgId));
                        messageArray.put(2, object1);
                    } else {
                        int msgCnt = argsArr.getInt("msgCnt");
                        int userFrom = msgData.getInt("userFrom");
                        int userTo = msgData.getInt("userTo");
                        int seen = msgData.getInt("seen");
                        int dlvd = msgData.getInt("dlvd");
                        String msgBody = msgData.getString("msgBody");
                        String date = Functions.timeConverter(time, false);
                        String files = msgData.getString("files");
                        String userInfoX = msgData.getString("userInfo");
                        boolean audioType = Boolean.parseBoolean(msgData.getString("audioType"));
                        String msgNum = "";
                        int user = userTo;
                        if(userTo == myId){
                            user = userFrom;
                            if(msgCnt > 0)
                                msgNum = "(" + msgCnt + ")";
                        }
                        JSONObject userInfo = new JSONObject(userInfoX);
                        String photo = userInfo.getString("photo");
                        String name = userInfo.getString("name");
                        JSONObject msgObject = object1.getJSONObject(String.valueOf(msgId));
                        msgObject.put("msgId", msgId);
                        msgObject.put("user", user);
                        msgObject.put("photo", photo);
                        msgObject.put("name", name);
                        msgObject.put("lastMsg", msgBody);
                        msgObject.put("seen", seen);
                        msgObject.put("dlvd", dlvd);
                        msgObject.put("date", date);
                        msgObject.put("files", files);
                        msgObject.put("msgNum", msgNum);
                        msgObject.put("time", time);
                        msgObject.put("audioType", audioType);
                        msgObject.put("msgdUser", userFrom);
                        msgObject.put("audioType", audioType);
                        object1.put(String.valueOf(msgId), msgObject);
                        JSONObject newObj = new JSONObject();
                        JSONObject keys = new JSONObject();
                        JSONObject addedKeys = new JSONObject();
                        ArrayList<Long> times = new ArrayList<>();
                        int count = 0;
                        for(int x = 0; x < object1.length(); x++) {
                            String key = Objects.requireNonNull(object1.names()).getString(x);
                            JSONObject object = object1.getJSONObject(key);
                            long t = object.getLong("time");
                            String newKey = String.valueOf(t);
                            if(!keys.isNull(newKey)){
                                count++;
                                newKey += "-"+count;
                            }
                            times.add(t);
                            keys.put(newKey, key);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            Collections.sort(times, Comparator.comparing(Objects::toString).reversed());
                        count = 0;
                        for (int i = 0; i < times.size(); i++) {
                            long t = times.get(i);
                            String newKey = String.valueOf(t);
                            if(!addedKeys.isNull(newKey)){
                                count++;
                                newKey += "-"+count;
                            }
                            String key = keys.getString(newKey);
                            JSONObject object = object1.getJSONObject(key);
                            newObj.put(key, object);
                            addedKeys.put(newKey, true);
                        }
                        messageArray.put(2, newObj);
                        saveMessageContent();
                        LinearLayout msgListView = setUpChat(cntxt, actvty, msgObject);
                        runOnUI(() -> {
                            int index = times.indexOf(time);
                            msgDis.addView(msgListView, index);
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("seenMsg", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                int userFrom = argsArr.getInt(0);
                int msgId = argsArr.getInt(2);
                LinearLayout linearLayout = msgDis.findViewWithTag(msgId);
                if(!(linearLayout == null)) {
                    JSONObject object1 = messageArray.getJSONObject(2);
                    JSONObject object = object1.getJSONObject(String.valueOf(msgId));
                    object.put("seen", 1);
                    object1.put(String.valueOf(msgId), object);
                    messageArray.put(2, object1);
                    saveMessageContent();
                    boolean darkThemeEnabled = new SharedPrefMngr(cntxt).getDarkThemeEnabled();
                    int color;
                    if(darkThemeEnabled)
                        color = R.color.white;
                    else
                        color = R.color.black;
                    if(userFrom == myId) {
                        runOnUI(() -> {
                            LinearLayout linearLayout1 = favDis.findViewWithTag(msgId);
                            LinearLayout linearLayout2 = arcDis.findViewWithTag(msgId);
                            linearLayout.setBackgroundResource(color);
                            if(!(linearLayout1 == null))
                                linearLayout1.setBackgroundResource(color);
                            if(!(linearLayout2 == null))
                                linearLayout2.setBackgroundResource(color);
                        });
                    } else {
                        TextView msgSeen = linearLayout.findViewById(R.id.msgSeen);
                        runOnUI(() -> msgSeen.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_msg_seen, 0));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("msgDelivered", args -> {
            try {
                int msgId = Integer.parseInt(args[0].toString());
                LinearLayout linearLayout = msgDis.findViewWithTag(msgId);
                if(!(linearLayout == null)) {
                    JSONObject object1 = messageArray.getJSONObject(2);
                    JSONObject object = object1.getJSONObject(String.valueOf(msgId));
                    object.put("dlvd", 1);
                    object1.put(String.valueOf(msgId), object);
                    messageArray.put(2, object1);
                    saveMessageContent();
                    TextView msgSeen = linearLayout.findViewById(R.id.msgSeen);
                    runOnUI(() -> msgSeen.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_msg_dlvd, 0));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("deleteChat", args -> {
            try {
                int msgId = Integer.parseInt(args[0].toString());
                removeDeletedView(msgId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("typing", args -> {
            try {
                JSONObject argsArr = new JSONObject(args[0].toString());
                int msgId = argsArr.getInt("msgId");
                LinearLayout linearLayout = msgDis.findViewWithTag(msgId);
                if(!(linearLayout == null)){
                    boolean typing = argsArr.getBoolean("typing");
                    LinearLayout typingView;
                    LinearLayout firstLayer = linearLayout.findViewById(R.id.firstLayer);
                    LinearLayout secondLayer = linearLayout.findViewById(R.id.secondLayer);
                    TextView msgTxt = linearLayout.findViewById(R.id.msgTxt);
                    boolean typg = false;
                    if(typers.has(String.valueOf(msgId)))
                        typg = typers.getBoolean(String.valueOf(msgId));
                    typers.put(String.valueOf(msgId), typing);
                    if(typing && !typg){
                        boolean darkThemeEnabled = new SharedPrefMngr(cntxt).getDarkThemeEnabled();
                        int typingImage;
                        if(darkThemeEnabled)
                            typingImage = R.drawable.typing_white;
                        else
                            typingImage = R.drawable.typing_black;
                        typingView = (LinearLayout) getLayoutInflater().inflate(R.layout.black_typing, null);
                        ImageView typingImg =  typingView.findViewById(R.id.gifImg);
                        typingImg.setBackgroundResource(typingImage);
                        runOnUI(() -> {
                            msgTxt.setVisibility(View.GONE);
                            secondLayer.setVisibility(View.GONE);
                            firstLayer.addView(typingView);
                        });
                    } else if(!typing) {
                        typingView = linearLayout.findViewById(R.id.b_typing);
                        runOnUI(() -> {
                            msgTxt.setVisibility(View.VISIBLE);
                            secondLayer.setVisibility(View.VISIBLE);
                            if(!(typingView == null))
                                firstLayer.removeView(typingView);
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("sound", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                int msgId = argsArr.getInt(2);
                LinearLayout linearLayout = msgDis.findViewWithTag(msgId);
                if(!(linearLayout == null)) {
                    boolean value = argsArr.getBoolean(1);
                    JSONObject object = new JSONObject();
                    object.put("value", value);
                    updatedSounds.put(String.valueOf(msgId), object);
                    setRectors(String.valueOf(msgId), value, "sound");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("online", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                int msgId = argsArr.getInt(2);
                LinearLayout linearLayout = msgDis.findViewWithTag(msgId);
                if(!(linearLayout == null)) {
                    boolean value = argsArr.getBoolean(1);
                    JSONObject object = new JSONObject();
                    object.put("value", value);
                    updatedOffs.put(String.valueOf(msgId), object);
                    setRectors(String.valueOf(msgId), value, "online");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("favourite", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                int msgId = argsArr.getInt(0);
                LinearLayout linearLayout = msgDis.findViewWithTag(msgId);
                if (!(linearLayout == null)) {
                    boolean value = argsArr.getBoolean(1);
                    JSONObject object = new JSONObject();
                    object.put("value", value);
                    updatedFavs.put(String.valueOf(msgId), object);
                    setRectors(String.valueOf(msgId), value, "favourite");
                    runOnUI(() -> {
                        try {
                            if (!value) {
                                JSONObject object1 = messageArray.getJSONObject(2);
                                if (object1.has(String.valueOf(msgId))) {
                                    JSONObject msgObj = object1.getJSONObject(String.valueOf(msgId));
                                    LinearLayout layout = setUpChat(cntxt, actvty, msgObj);
                                    favDis.addView(layout);
                                }
                            } else {
                                LinearLayout linearLayoutFav = favDis.findViewWithTag(msgId);
                                favDis.removeView(linearLayoutFav);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("archive", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                int msgId = argsArr.getInt(0);
                LinearLayout linearLayout = msgDis.findViewWithTag(msgId);
                if (!(linearLayout == null)) {
                    boolean value = argsArr.getBoolean(1);
                    JSONObject object = new JSONObject();
                    object.put("value", value);
                    updatedArchs.put(String.valueOf(msgId), object);
                    setRectors(String.valueOf(msgId), value, "archive");
                    switchArchive(cntxt, actvty, String.valueOf(msgId), value, true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("blockChat", args -> {
            try {
                JSONArray argsArr = new JSONArray(args[0].toString());
                int msgId = argsArr.getInt(1);
                LinearLayout linearLayout = msgDis.findViewWithTag(msgId);
                if(!(linearLayout == null)) {
                    int userTo = argsArr.getInt(0);
                    boolean value = argsArr.getBoolean(2);
                    JSONObject object = new JSONObject();
                    object.put("userTo", userTo);
                    object.put("value", value);
                    updatedBlocks.put(String.valueOf(msgId), object);
                    if (!(myId == userTo))
                        setRectors(String.valueOf(msgId), value, "blockChat");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        return view;

    }

    public static void openSelected(Context context, int tabIndex, int tabIcon, String title, NestedScrollView scrollView){
        if(!(tabIndex < 0) && !firstLoad){
            if(!(nestedScrollView == null))
                nestedScrollView.setVisibility(View.GONE);
            changeTabTitle(context, tabIndex, tabIcon, title);
            scrollView.setVisibility(View.VISIBLE);
            nestedScrollView = scrollView;
        }
    }

    public static void removeDeletedView(int msgId) throws JSONException{
        LinearLayout linearLayout = msgDis.findViewWithTag(msgId);
        if(!(linearLayout == null)) {
            JSONObject object1 = messageArray.getJSONObject(2);
            object1.remove(String.valueOf(msgId));
            messageArray.put(2, object1);
            saveMessageContent();
            runOnUI(() -> msgDis.removeView(linearLayout));
        }
    }

    private void loadMsgContents() {
        if (!(messageArray == null) && firstLoad) {
            runOnUI(() -> {
                progressBar.setVisibility(View.GONE);
                msgDis.removeView(progressBar);
                msgDis.removeAllViews();
                try {
                    JSONObject msgObj = new JSONObject(messageArray.getString(2));
                    displayChats(msgObj);
                    popMessage();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } else {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("lang", lang)
                    .addFormDataPart("user", String.valueOf(myId))
                    .addFormDataPart("maxDate", maxDate)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.msgUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try (Response response = call.execute()){
                if (response.isSuccessful()) {
                    popMessage();
                    String responseString = Objects.requireNonNull(response.body()).string();
                    progressBar.setVisibility(View.GONE);
                    msgDis.removeView(progressBar);
                    msgLoaded = true;
                    loading = false;
                    JSONArray responseArray = new JSONArray(responseString);
                    maxDate = responseArray.getString(0);
                    maxReached = responseArray.getBoolean(1);
                    favouriteObj = responseArray.getJSONObject(4);
                    maxTime = responseArray.getLong(3);
                    archiveObj = responseArray.getJSONObject(5);
                    searchedHistory = responseArray.getJSONArray(6);
                    String objStr = responseArray.getString(2);
                    if(messageArray == null) {
                        messageArray = new JSONArray();
                        messageArray.put(2, new JSONObject());
                    }
                    messageArray.put(0, maxDate);
                    messageArray.put(1, maxReached);
                    messageArray.put(3, maxTime);
                    messageArray.put(4, favouriteObj);
                    messageArray.put(5, archiveObj);
                    if(!StringUtils.isEmpty(objStr)) {
                        JSONObject msgObj = new JSONObject(objStr);
                        displayChats(msgObj);
                    }
                    saveMessageContent();
                    if(searchedHistory.length() > 0){
                        hrzntlScrllVw.setVisibility(View.VISIBLE);
                        for(int i = 0; i < searchedHistory.length(); i++){
                            JSONObject object = searchedHistory.getJSONObject(i);
                            showSearchHistory(cntxt, actvty, object, i);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void showSearchHistory(Context cntxt, Activity activity, JSONObject object, int i) throws JSONException {
        int user = object.getInt("user");
        String name = object.getString("name");
        String photo = www + object.getString("photo");
        LinearLayout linearLayout = searchHistoryLayout.findViewWithTag(user);
        if(!(linearLayout == null))
            searchHistoryLayout.removeView(linearLayout);
        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams") LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.search_box, null);
        LinearLayout holder = layout.findViewById(R.id.holder);
        ImageView imageView = layout.findViewById(R.id.photo);
        TextView textView = layout.findViewById(R.id.name);
        layout.setTag(user);
        imageLoader.displayImage(photo, imageView);
        textView.setText(name);
        holder.setOnClickListener(v -> {
            runOnUI(() -> searchHistoryLayout.removeView(layout));
            try {
                saveSearch(cntxt, activity, user, name, object.getString("photo"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        searchHistoryLayout.addView(layout, i);
        if(searchHistoryLayout.getChildCount() > 10)
            searchHistoryLayout.removeViewAt(10);
    }

    private static void saveMessageContent() {
        SaveOpenedMessages.messageArray = messageArray;
    }

    private void displayChats(JSONObject object) throws JSONException {
        JSONObject object1 = messageArray.getJSONObject(2);
        int leng = object.length();
        for(int i = 0; i < leng; i++){
            String key = Objects.requireNonNull(object.names()).getString(i);
            String eachMsgStr = object.getString(key);
            JSONObject eachMsgObj = new JSONObject(eachMsgStr);
            int msgId = eachMsgObj.getInt("msgId");
            object1.put(String.valueOf(msgId), eachMsgObj);
            LinearLayout linearLayout = setUpChat(cntxt, actvty, eachMsgObj);
            if(favouriteObj.has(String.valueOf(msgId))){
                LinearLayout msgListView = setUpChat(cntxt, actvty, eachMsgObj);
                favDis.addView(msgListView);
                favText.setVisibility(View.GONE);
            }
            if(archiveObj.has(String.valueOf(msgId))){
                archText.setVisibility(View.GONE);
                arcDis.addView(linearLayout);
            } else {
                msgDis.addView(linearLayout);
            }
        }
        firstLoad = false;
        messageArray.put(2, object1);
    }

    @SuppressLint("SetTextI18n")
    private static LinearLayout setUpChat(Context cntxt, Activity activity, JSONObject eachMsgObj) throws JSONException {
        SharedPrefMngr sharedPrefMngr = new SharedPrefMngr(cntxt);
        int msgId = eachMsgObj.getInt("msgId");
        String photo = www + eachMsgObj.getString("photo");
        String name = eachMsgObj.getString("name");
        int msgdUser = eachMsgObj.getInt("msgdUser");
        String msgBody = eachMsgObj.getString("lastMsg");
        int seen = eachMsgObj.getInt("seen");
        int dlvd = eachMsgObj.getInt("dlvd");
        String date = eachMsgObj.getString("date");
        String files = eachMsgObj.getString("files");
        String msgNum = eachMsgObj.getString("msgNum");
        final int user = eachMsgObj.getInt("user");
        long time = eachMsgObj.getInt("time");
        boolean audioType = eachMsgObj.getBoolean("audioType");
        boolean blocked = eachMsgObj.getBoolean("blocked");
        @SuppressLint("InflateParams") LinearLayout msgListView = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.msg_list, null);
        ImageView msgerPht = msgListView.findViewById(R.id.msgerPht);
        TextView msgerName = msgListView.findViewById(R.id.msgerName);
        TextView msgTxt = msgListView.findViewById(R.id.msgTxt);
        TextView msgDate = msgListView.findViewById(R.id.msgDate);
        ImageButton msgSeen = msgListView.findViewById(R.id.msgSeen);
        jsonObject.put(String.valueOf(msgId), msgListView);
        msgListView.setTag(msgId);
        imageLoader.displayImage(photo, msgerPht);
        msgerName.setText(name + msgNum);
        if(blocked) msgerName.setTypeface(null, Typeface.NORMAL);
        int drawableLeft = 0;
        if(!StringUtils.isEmpty(files)){
            boolean fileImg = false, fileVid = false;
            JSONArray filesArr = new JSONArray(files);
            int filesCnt = filesArr.length();
            for(int x = 0; x < filesCnt; x++){
                String fileType = Functions.checkFileType(filesArr.getString(x));
                
                if(fileType.equals("image"))
                    fileImg = true;
                if(fileType.equals("video"))
                    fileVid = true;
            }
            if(fileVid){
                if(StringUtils.isEmpty(msgBody))
                    msgBody = "Video";
                drawableLeft = R.drawable.ic_video_black;
            }
            if(fileImg){
                if(StringUtils.isEmpty(msgBody)) {
                    msgBody = "Photo";
                    if (fileVid)
                        msgBody += " / Video";
                }
                drawableLeft = R.drawable.ic_cam;
            }
            if(audioType) {
                msgBody = "Recorded Audio";
                drawableLeft = R.drawable.ic_mic;
            }
        }
        msgTxt.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
        msgTxt.setText(msgBody);
        msgDate.setText(date);
        msgDate.setTag(time);
        if(myId == msgdUser){
            int pddRght = R.drawable.ic_msg_sent;
            if(dlvd == 1)
                pddRght = R.drawable.ic_msg_dlvd;
            if(seen == 1)
                pddRght = R.drawable.ic_msg_seen;
            msgSeen.setBackgroundResource(pddRght);
            msgSeen.setVisibility(View.VISIBLE);
        } else {
            if(seen == 0){
                boolean darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
                int color;
                if(darkThemeEnabled)
                    color = R.color.blash;
                else
                    color = R.color.ash;
                msgListView.setBackgroundResource(R.drawable.unseen);
            }
        }
        msgListView.setOnClickListener(v -> openMessage(cntxt, user));
        msgListView.setOnLongClickListener(v -> {
            try {
                showMessageOptions(cntxt, activity, String.valueOf(msgId), user, name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        });
        return msgListView;
    }

    public static void switchFavourites(Context cntxt, Activity activity, String msgId, boolean value) throws JSONException {
        if(!value) {
            JSONObject object1 = messageArray.getJSONObject(2);
            if(object1.has(msgId)){
                JSONObject msgObj = object1.getJSONObject(msgId);
                favouriteObj.put(msgId, msgObj);
                LinearLayout layout = setUpChat(cntxt, activity, msgObj);
                long time = msgObj.getLong("time");
                JSONObject newObj = new JSONObject();
                JSONObject keys = new JSONObject();
                JSONObject addedKeys = new JSONObject();
                ArrayList<Long> times = new ArrayList<>();
                int count = 0;
                for(int x = 0; x < object1.length(); x++) {
                    String key = Objects.requireNonNull(object1.names()).getString(x);
                    JSONObject object = object1.getJSONObject(key);
                    String mId = object.getString("msgId");
                    if(favouriteObj.has(mId)){
                        long t = object.getLong("time");
                        String newKey = String.valueOf(t);
                        if(!keys.isNull(newKey)){
                            count++;
                            newKey += "-"+count;
                        }
                        times.add(t);
                        keys.put(newKey, key);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    Collections.sort(times, Comparator.comparing(Objects::toString).reversed());
                count = 0;
                for (int i = 0; i < times.size(); i++) {
                    long t = times.get(i);
                    String newKey = String.valueOf(t);
                    if(!addedKeys.isNull(newKey)){
                        count++;
                        newKey += "-"+count;
                    }
                    String key = keys.getString(newKey);
                    JSONObject object = object1.getJSONObject(key);
                    newObj.put(key, object);
                    addedKeys.put(newKey, true);
                }
                int index = times.indexOf(time);
                runOnUI(() -> {
                    favText.setVisibility(View.GONE);
                    favDis.addView(layout, index);
                });
            }
        } else {
            favouriteObj.remove(msgId);
            LinearLayout linearLayoutFav = favDis.findViewWithTag(Integer.valueOf(msgId));
            runOnUI(() -> {
                favDis.removeView(linearLayoutFav);
                if(favDis.getChildCount() == 1)
                    favText.setVisibility(View.VISIBLE);
            });
        }
    }

    public static void switchArchive(Context cntxt, Activity activity, String msgId, boolean value, boolean setUp) throws JSONException {
        JSONObject object1 = messageArray.getJSONObject(2);
        if(object1.has(msgId)){
            if(!value) {
                JSONObject msgObj = object1.getJSONObject(msgId);
                archiveObj.put(msgId, msgObj);
                LinearLayout layout = setUpChat(cntxt, activity, msgObj);
                long time = msgObj.getLong("time");
                JSONObject newObj = new JSONObject();
                JSONObject keys = new JSONObject();
                JSONObject addedKeys = new JSONObject();
                ArrayList<Long> times = new ArrayList<>();
                int count = 0;
                for(int x = 0; x < object1.length(); x++) {
                    String key = Objects.requireNonNull(object1.names()).getString(x);
                    JSONObject object = object1.getJSONObject(key);
                    String mId = object.getString("msgId");
                    if(archiveObj.has(mId)){
                        long t = object.getLong("time");
                        String newKey = String.valueOf(t);
                        if(!keys.isNull(newKey)){
                            count++;
                            newKey += "-"+count;
                        }
                        times.add(t);
                        keys.put(newKey, key);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    Collections.sort(times, Comparator.comparing(Objects::toString).reversed());
                count = 0;
                for (int i = 0; i < times.size(); i++) {
                    long t = times.get(i);
                    String newKey = String.valueOf(t);
                    if(!addedKeys.isNull(newKey)){
                        count++;
                        newKey += "-"+count;
                    }
                    String key = keys.getString(newKey);
                    JSONObject object = object1.getJSONObject(key);
                    newObj.put(key, object);
                    addedKeys.put(newKey, true);
                }
                int index = times.indexOf(time);
                LinearLayout linearLayout = msgDis.findViewWithTag(Integer.valueOf(msgId));
                runOnUI(() -> {
                    archText.setVisibility(View.GONE);
                    arcDis.addView(layout, index);
                    msgDis.removeView(linearLayout);
                });
            } else {
                archiveObj.remove(msgId);
                LinearLayout linearLayoutFav = arcDis.findViewWithTag(Integer.valueOf(msgId));
                runOnUI(() -> {
                    arcDis.removeView(linearLayoutFav);
                    if(arcDis.getChildCount() == 1)
                        archText.setVisibility(View.VISIBLE);
                });
                if(setUp){
                    JSONObject msgObj = object1.getJSONObject(msgId);
                    LinearLayout layout = setUpChat(cntxt, activity, msgObj);
                    long time = msgObj.getLong("time");
                    JSONObject newObj = new JSONObject();
                    JSONObject keys = new JSONObject();
                    JSONObject addedKeys = new JSONObject();
                    ArrayList<Long> times = new ArrayList<>();
                    int count = 0;
                    for(int x = 0; x < object1.length(); x++) {
                        String key = Objects.requireNonNull(object1.names()).getString(x);
                        JSONObject object = object1.getJSONObject(key);
                        String mId = object.getString("msgId");
                        if(!archiveObj.has(mId)){
                            long t = object.getLong("time");
                            String newKey = String.valueOf(t);
                            if(!keys.isNull(newKey)){
                                count++;
                                newKey += "-"+count;
                            }
                            times.add(t);
                            keys.put(newKey, key);
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        Collections.sort(times, Comparator.comparing(Objects::toString).reversed());
                    count = 0;
                    for (int i = 0; i < times.size(); i++) {
                        long t = times.get(i);
                        String newKey = String.valueOf(t);
                        if(!addedKeys.isNull(newKey)){
                            count++;
                            newKey += "-"+count;
                        }
                        String key = keys.getString(newKey);
                        JSONObject object = object1.getJSONObject(key);
                        newObj.put(key, object);
                        addedKeys.put(newKey, true);
                    }
                    int index = times.indexOf(time);
                    msgDis.addView(layout, index);
                }
            }
        }
    }

    private static void openMessage(Context cntxt, int user) {
        Intent intent = new Intent(cntxt, MessageAct.class);
        Bundle userParams = new Bundle();
        userParams.putInt("user", user);
        intent.putExtras(userParams);
        cntxt.startActivity(intent);
    }

    private void nonStopCounter(){
        Timer timerX = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                for(int x = 0; x < jsonObject.length(); x++) {
                    try {
                        String key = Objects.requireNonNull(jsonObject.names()).getString(x);
                        LinearLayout linearLayout = (LinearLayout) jsonObject.get(key);
                        TextView dateTxt = linearLayout.findViewById(R.id.msgDate);
                        long oldTime = (long) dateTxt.getTag();
                        String date = Functions.timeConverter(oldTime, false);
                        runOnUI(() -> dateTxt.setText(date));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timerX.schedule(task, 0, 1000);
    }

}
