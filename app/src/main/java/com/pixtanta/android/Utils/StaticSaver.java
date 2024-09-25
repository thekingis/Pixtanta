package com.pixtanta.android.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;

public class StaticSaver {

    static JSONObject object = new JSONObject();
    static JSONObject sockets = new JSONObject();

    public static void saveObject(int pageCount, JSONObject jsonObject) throws JSONException {
        object.put(String.valueOf(pageCount), jsonObject);
    }

    public static JSONObject getObject(int pageCount){
        try {
            return object.getJSONObject(String.valueOf(pageCount));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void removeObject(int pageCount){
        if(object.has(String.valueOf(pageCount)))
            object.remove(String.valueOf(pageCount));
    }

    public static void emptyObject(){
        object = new JSONObject();
    }

    public static void saveSocket(int pageCount, Socket socket) throws JSONException {
        sockets.put(String.valueOf(pageCount), socket);
    }

    public static Socket getSocket(int pageCount){
        try {
            return (Socket) sockets.get(String.valueOf(pageCount));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void removeSocket(int pageCount){
        if(sockets.has(String.valueOf(pageCount)))
            sockets.remove(String.valueOf(pageCount));
    }

    public static void emptySocket(){
        sockets = new JSONObject();
    }

}
