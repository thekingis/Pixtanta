package com.pixtanta.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SaveOpenedMessages {

    static SaveOpenedMessages instance;
    public static JSONObject openedMessagesObject = new JSONObject(), relativeObject = new JSONObject(), linkDatas = new JSONObject();
    public static JSONArray messageArray = null;
    public static String connectedSocket = null;

    public static SaveOpenedMessages getInstance(){
        if(instance == null)
            instance = new SaveOpenedMessages();
        return instance;
    }

    public JSONObject getOpenedMessage(int user) throws JSONException {
        JSONObject messageObject = null;
        String objectKey = String.valueOf(user);
        if(!openedMessagesObject.isNull(objectKey))
            messageObject = openedMessagesObject.getJSONObject(objectKey);
        return messageObject;
    }

    public static void saveOpenedMessage(int user, JSONObject messageObject) throws JSONException {
        openedMessagesObject.put(String.valueOf(user), messageObject);
    }

    public static void resetInstance() {
        openedMessagesObject = new JSONObject();
        relativeObject = new JSONObject();
        linkDatas = new JSONObject();
        messageArray = null;
        connectedSocket = null;
    }

}
