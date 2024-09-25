package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pixtanta.android.Utils.ContextData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pixtanta.android.PageActivity.openEmail;
import static com.pixtanta.android.PageActivity.openLink;

public class AboutPageFragment extends Fragment {

    Context cntxt;
    int pageId;
    LinearLayout layout;
    TextView textView;
    public static JSONObject object, jsonObject;
    boolean loading;

    public AboutPageFragment(ContextData contextData) {
        // Required empty public constructor
        cntxt = contextData.context;
        pageId = contextData.dataId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        loading = false;
        jsonObject = new JSONObject();
        object = new JSONObject();
        View view = inflater.inflate(R.layout.fragment_about_page, container, false);
        layout = view.findViewById(R.id.layout);
        textView = view.findViewById(R.id.textView);

        try {
            object.put("email", R.drawable.ic_email);
            object.put("phone", R.drawable.ic_call);
            object.put("website", R.drawable.ic_web);
            object.put("description", R.drawable.ic_info);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    @SuppressLint("ResourceAsColor")
    public void loadInfo(){
        if(!loading){
            loading = true;
            while (loading) {
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("pageId", String.valueOf(pageId))
                        .build();
                Request request = new Request.Builder()
                        .url(Constants.aboutPageUrl)
                        .post(requestBody)
                        .build();

                OkHttpClient okHttpClient = new OkHttpClient();
                Call call = okHttpClient.newCall(request);
                try (Response response = call.execute()) {
                    if (response.isSuccessful()) {
                        String responseStr = Objects.requireNonNull(response.body()).string();
                        JSONArray jsonArray = new JSONArray(responseStr);
                        boolean empty = jsonArray.getBoolean(0);
                        if (!empty) {
                            String data = jsonArray.getString(1);
                            textView.setVisibility(View.GONE);
                            jsonObject = new JSONObject(data);
                            for (int i = 0; i < jsonObject.length(); i++) {
                                String key = Objects.requireNonNull(jsonObject.names()).getString(i);
                                int icon = object.getInt(key);
                                String str = jsonObject.getString(key);
                                @SuppressLint("InflateParams") LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(cntxt).inflate(R.layout.about_text, null);
                                TextView text = linearLayout.findViewById(R.id.text);
                                text.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
                                text.setText(str);
                                if (key.equals("description")) {
                                    boolean darkThemeEnabled = new SharedPrefMngr(cntxt).getDarkThemeEnabled();
                                    int color;
                                    if(darkThemeEnabled)
                                        color = R.color.white;
                                    else
                                        color = R.color.black;
                                    text.setTextColor(ContextCompat.getColor(cntxt, color));
                                }
                                text.setOnClickListener(v -> openInfo(cntxt, key, str));
                                layout.addView(linearLayout);
                            }
                        } else {
                            textView.setVisibility(View.VISIBLE);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void openInfo(Context cntxt, String key, String str) {
        switch (key) {
            case "email":
                openEmail(cntxt, str);
                break;
            case "phone":
                //callNumber(str);
                break;
            case "website":
                openLink(cntxt, str);
                break;
        }
    }

}