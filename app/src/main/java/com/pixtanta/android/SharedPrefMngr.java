package com.pixtanta.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.pixtanta.android.HomeAct.restartApp;

class SharedPrefMngr {

    private final Context mCtx;
    private static final String shrdPrefName = "myShrdPref";
    private static final String theme = "theme";
    private static final String lang = "lang";
    private static final String shake = "shake";
    private static final String myId = "myId";
    private static final String myPht = "myPht";
    private static final String myName = "myName";
    private static final String myUserName = "myUserName";
    private static final String myVerification = "myVerification";
    private static final String smartLogin = "smartLogin";
    private static final String smartPass = "smartPass";
    private static final String smartPhotos = "smartPhotos";
    private static final String smartNames = "smartNames";
    private static final String smartUserNames = "smartUserNames";
    private static final String usersVerification = "usersVerification";
    private static final String tempFilesPath = "tempFilesPath";
    public static final String english = "english";

    public SharedPrefMngr(Context mCtx){
        this.mCtx = mCtx;
    }

    public void storeUserVerification(int user, boolean verified){
        try {
            SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = shrdPrf.edit();
            String usersVrfctn = shrdPrf.getString(usersVerification, null);
            String userID = String.valueOf(user);
            JSONObject usersVrfctnArr = new JSONObject(usersVrfctn);
            usersVrfctnArr.put(userID, verified);
            String usersVrfctnStr = usersVrfctnArr.toString();
            editor.putString(usersVerification, usersVrfctnStr);
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stockTempFiles(String filePath){
        try {
            SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = shrdPrf.edit();
            String tempFilesPathStr = getTempFiles();
            JSONArray tempFileArray;
            if(tempFilesPathStr == null)
                tempFileArray = new JSONArray();
            else
                tempFileArray = new JSONArray(tempFilesPathStr);
            tempFileArray.put(filePath);
            tempFilesPathStr = tempFileArray.toString();
            editor.putString(tempFilesPath, tempFilesPathStr);
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTempFiles(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getString(tempFilesPath, null);
    }

    public void emptyTempFile(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putString(tempFilesPath, null);
        editor.apply();
    }

    public boolean checkUserVerified(int user){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        String usersVrfctn = shrdPrf.getString(usersVerification, null);
        String userID = String.valueOf(user);
        try {
            JSONObject usersVrfctnArr = new JSONObject(usersVrfctn);
            return usersVrfctnArr.getBoolean(userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void initializeSmartLogin(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        String smrtLg = shrdPrf.getString(smartLogin, null);
        String smrtPs = shrdPrf.getString(smartPass, null);
        String smrtPh = shrdPrf.getString(smartPhotos, null);
        String smrtNm = shrdPrf.getString(smartNames, null);
        String smrtUsNm = shrdPrf.getString(smartUserNames, null);
        String usersVrfctn = shrdPrf.getString(usersVerification, null);
        if(smrtLg == null){
            editor.putString(smartLogin, "{}");
        }
        if(smrtPs == null){
            editor.putString(smartPass, "{}");
        }
        if(smrtPh == null){
            editor.putString(smartPhotos, "{}");
        }
        if(smrtUsNm == null){
            editor.putString(smartUserNames, "{}");
        }
        if(smrtNm == null){
            editor.putString(smartNames, "{}");
        }
        if(usersVrfctn == null){
            editor.putString(usersVerification, "{}");
        }
        editor.apply();
    }

    public void storeUserInfo(int id, String photo, String name, String userName, boolean verified){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putInt(myId, id);
        editor.putString(myPht, photo);
        editor.putString(myName, name);
        editor.putString(myUserName, userName);
        editor.putBoolean(myVerification, verified);
        editor.apply();
    }

    public boolean saveLanguage(String langSel){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putString(lang, langSel);
        editor.apply();
        return true;
    }

    public void enabledDarkTheme(boolean dark, Activity activity){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putBoolean(theme, dark);
        editor.apply();
        restartApp(activity);
    }

    public void saveShakeOption(boolean sel){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putBoolean(shake, sel);
        editor.apply();
    }

    public boolean checkShakeOption(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getBoolean(shake, true);
    }

    public boolean loggedIn(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getInt(myId, 0) != 0;
    }

    public String[] getSmartLoginInfo(String user){
        String[] details;
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        String smrtPh = shrdPrf.getString(smartPhotos, null);
        String smrtNm = shrdPrf.getString(smartNames, null);
        String smrtUsNm = shrdPrf.getString(smartUserNames, null);
        try {
            JSONObject smrtPhArr = new JSONObject(smrtPh);
            JSONObject smrtNmArr = new JSONObject(smrtNm);
            JSONObject smrtUsNmArr = new JSONObject(smrtUsNm);
            details = new String[] {
                    smrtPhArr.getString(user),
                    smrtNmArr.getString(user),
                    smrtUsNmArr.getString(user)
            };
        } catch (JSONException e) {
            e.printStackTrace();
            return  null;
        }

        return details;
    }

    public void saveSmartLogin(int user, boolean smartLgn, boolean smartPss){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        String smrtLg = shrdPrf.getString(smartLogin, null);
        String smrtPs = shrdPrf.getString(smartPass, null);
        String smrtPh = shrdPrf.getString(smartPhotos, null);
        String smrtNm = shrdPrf.getString(smartNames, null);
        String smrtUsNm = shrdPrf.getString(smartUserNames, null);
        String smrtUsVr = shrdPrf.getString(usersVerification, null);
        String userPht = getMyPht();
        String userName = getMyName();
        String usersName = getMyUserName();
        boolean verified = getMyVerification();
        String userID = String.valueOf(user);
        try {
            JSONObject smrtLgArr = new JSONObject(smrtLg);
            JSONObject smrtPsArr = new JSONObject(smrtPs);
            JSONObject smrtPhArr = new JSONObject(smrtPh);
            JSONObject smrtNmArr = new JSONObject(smrtNm);
            JSONObject smrtUsNmArr = new JSONObject(smrtUsNm);
            JSONObject smrtVrArr = new JSONObject(smrtUsVr);
            String strName = "xyz" + userID;
            if(smartLgn){
                smrtLgArr.put(strName, userID);
                smrtPhArr.put(userID, userPht);
                smrtNmArr.put(userID, userName);
                smrtUsNmArr.put(userID, usersName);
                smrtVrArr.put(userID, verified);
                smrtPsArr.put(userID, smartPss);
            } else {
                if(smrtLgArr.has(strName)){
                    smrtLgArr.remove(strName);
                    smrtPhArr.remove(userID);
                    smrtNmArr.remove(userID);
                    smrtUsNmArr.remove(userID);
                    smrtVrArr.remove(userID);
                    smrtPsArr.remove(userID);
                }
            }
            String smartLoginStr = String.valueOf(smrtLgArr);
            String smartPassStr = String.valueOf(smrtPsArr);
            String smartPhotoStr = String.valueOf(smrtPhArr);
            String smartNameStr = String.valueOf(smrtNmArr);
            String smartUsNameStr = String.valueOf(smrtUsNmArr);
            String smartVerfdStr = String.valueOf(smrtVrArr);
            editor.putString(smartLogin, smartLoginStr);
            editor.putString(smartPass, smartPassStr);
            editor.putString(smartPhotos, smartPhotoStr);
            editor.putString(smartNames, smartNameStr);
            editor.putString(smartUserNames, smartUsNameStr);
            editor.putString(usersVerification, smartVerfdStr);
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean checkSmartLogin(int user){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        if(shrdPrf.getString(smartLogin,null) != null){
            String smrtLg = shrdPrf.getString(smartLogin, null);
            String userID = String.valueOf(user);
            String key = "xyz" + userID;
            try {
                JSONObject smrtLgArr = new JSONObject(smrtLg);
                if(smrtLgArr.has(key))
                    return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }
        return false;
    }

    public boolean checkAvailableSmartLogin(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        if(shrdPrf.getString(smartLogin,null) != null){
            String smrtLg = shrdPrf.getString(smartLogin, null);
            try {
                JSONObject smrtLgArr = new JSONObject(smrtLg);
                return smrtLgArr.length() > 0;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public JSONObject getAvailableSmartLogin(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        if(shrdPrf.getString(smartLogin,null) != null){
            String smrtLg = shrdPrf.getString(smartLogin, null);
            try {
                JSONObject smrtLgArr = new JSONObject(smrtLg);
                if(smrtLgArr.length() > 0)
                    return smrtLgArr;
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public boolean checkSmartPassOff(int user){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        String smrtPs = shrdPrf.getString(smartPass, null);
        if(checkSmartLogin(user)){
            try {
                JSONObject smrtPsArr = new JSONObject(smrtPs);
                String userID = String.valueOf(user);
                return smrtPsArr.getBoolean(userID);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public void setSmartPassOff(int user, boolean smrtPass){
        if(checkSmartLogin(user)){
            try {
                SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
                String smrtPs = shrdPrf.getString(smartPass, null);
                SharedPreferences.Editor editor = shrdPrf.edit();
                JSONObject smrtPsArr = new JSONObject(smrtPs);
                String userID = String.valueOf(user);
                smrtPsArr.put(userID, smrtPass);
                String smartPassStr = String.valueOf(smrtPsArr);
                editor.putString(smartPass, smartPassStr);
                editor.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSelectedLanguage(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        if(shrdPrf.getString(lang,null) == null){
            return  english;
        }
        return  shrdPrf.getString(lang, english);
    }

    public boolean getDarkThemeEnabled(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getBoolean(theme, false) || getDefaultDarkThemeEnabled();
    }

    private boolean getDefaultDarkThemeEnabled(){
        int defaultThemeMode = mCtx.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return defaultThemeMode == Configuration.UI_MODE_NIGHT_YES || defaultThemeMode == Configuration.UI_MODE_NIGHT_MASK;
    }

    public void loggedOut(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.remove(myId);
        editor.remove(myPht);
        editor.remove(myName);
        editor.remove(myUserName);
        editor.apply();
    }

    public int getMyId(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getInt(myId, 0);
    }

    public String getMyPht(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getString(myPht, null);
    }

    public String getMyName(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getString(myName, null);
    }

    public String getMyUserName(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getString(myUserName, null);
    }

    public boolean getMyVerification(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getBoolean(myVerification, false);
    }
}
