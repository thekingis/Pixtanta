package com.pixtanta.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;

public class TermsAct extends ThemeActivity {

    LinearLayout header;
    WebView webView;
    ProgressBar progressBar;
    String lang, htmlText, preText, appText;
    AssetManager assetManager;
    InputStream inputStream;
    boolean darkModeEnabled;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        sharedPrefMngr = new SharedPrefMngr(this);

        lang = sharedPrefMngr.getSelectedLanguage();
        darkModeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        header =  findViewById(R.id.header);
        webView =  findViewById(R.id.webView);
        progressBar =  findViewById(R.id.progressBar);
        assetManager = getAssets();
        try {
            inputStream = assetManager.open("lang/"+lang+"/tac.html");
            int size = inputStream.available();
            byte[] bytes = new byte[size];
            inputStream.read(bytes);
            htmlText = new String(bytes);
            if(darkModeEnabled)
                preText = "<html><head></head><body style=\"background-color:#000;color:#fff;\">";
            else
                preText = "<html><head></head><body style=\"background-color:#fff;color:#000;\">";
            appText = "</body></html>";
            webView.loadData(preText + htmlText + appText, "text/html", "UTF-8");
            webView.setWebViewClient(new WebViewClient(){
                public void onPageFinished(WebView view, String url) {
                    progressBar.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    header.setVisibility(View.VISIBLE);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}