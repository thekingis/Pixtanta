package com.pixtanta.android;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ThemeActivity extends AppCompatActivity {

    Activity activity;
    SharedPrefMngr sharedPrefMngr;
    boolean darkThemeEnabled, defaultDarkThemeEnabled;
    int theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        sharedPrefMngr = new SharedPrefMngr(this);
        darkThemeEnabled = sharedPrefMngr.getDarkThemeEnabled();
        defaultDarkThemeEnabled = getDefaultDarkThemeEnabled();

        if(darkThemeEnabled || defaultDarkThemeEnabled)
            theme = R.style.DarkTheme;
        else
            theme = R.style.LightTheme;

        setTheme(theme);

    }

    private boolean getDefaultDarkThemeEnabled(){
        int defaultThemeMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return defaultThemeMode == Configuration.UI_MODE_NIGHT_YES || defaultThemeMode == Configuration.UI_MODE_NIGHT_MASK;
    }

}
