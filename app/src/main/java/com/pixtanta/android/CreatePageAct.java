package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class CreatePageAct extends ThemeActivity {

    Context cntxt;
    LinearLayout linearLayout;
    JSONObject jsonObject = new JSONObject();
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_page);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cntxt = this;
        sharedPrefMngr = new SharedPrefMngr(this);

        if(!sharedPrefMngr.loggedIn()){
            finish();
            startActivity(new Intent(this, LoginAct.class));
            return;
        }

        linearLayout = findViewById(R.id.linearLayout);
        try {
            jsonObject.put("Local Business", R.drawable.local_business);
            jsonObject.put("Company and Organization", R.drawable.company);
            jsonObject.put("Brands and Products", R.drawable.brand);
            jsonObject.put("Personality", R.drawable.personality);
            jsonObject.put("Entertainment", R.drawable.entertainment);
            jsonObject.put("Community", R.drawable.community);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < jsonObject.length(); i++){
            try {
                String text = Objects.requireNonNull(jsonObject.names()).getString(i);
                int image = jsonObject.getInt(text);
                @SuppressLint("InflateParams") LinearLayout cardView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.card_view, null, false);
                ImageView imageView = cardView.findViewById(R.id.image);
                TextView textView = cardView.findViewById(R.id.text);
                imageView.setBackgroundResource(image);
                textView.setText(text);
                int finalI = i;
                imageView.setOnClickListener(v -> createPage(finalI));
                textView.setOnClickListener(v -> createPage(finalI));
                cardView.setOnClickListener(v -> createPage(finalI));
                linearLayout.addView(cardView);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void createPage(int finalI) {
        Intent intent = new Intent(CreatePageAct.this, SetupPageActivity.class);
        Bundle params = new Bundle();
        params.putInt("pageType", finalI);
        intent.putExtras(params);
        startActivity(intent);
    }
}
