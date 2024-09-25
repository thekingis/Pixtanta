package com.pixtanta.android.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonObject extends JSONObject {

    public JsonObject prepend(String key, Object object) throws JSONException {
        JSONArray keysArray = this.names();
        JSONArray objArray = new JSONArray();
        Iterator iterator = this.keys();
        while (iterator.hasNext()){
            String k = (String) iterator.next();
            objArray.put(this.get(k));
        }
        this.removeAll();
        this.put(key, object);
        assert keysArray != null;
        if(keysArray.length() > 0) {
            for(int i = 0; i < keysArray.length(); i++) {
                String exKey = keysArray.getString(i);
                Object o = objArray.get(i);
                if(!key.equals(exKey))
                    this.put(exKey, o);
            }
        }
        return this;
    }

    public JsonObject putAt(int index, String key, Object object) throws JSONException {
        JSONArray keysArray = this.names();
        JSONArray objArray = new JSONArray();
        Iterator iterator = this.keys();
        while (iterator.hasNext()){
            String k = (String) iterator.next();
            objArray.put(this.get(k));
        }
        this.removeAll();
        assert keysArray != null;
        if(keysArray.length() > 0) {
            for(int i = 0; i < keysArray.length(); i++) {
                String exKey = keysArray.getString(i);
                Object o = objArray.get(i);
                if(i == index)
                    this.put(key, object);
                if(!key.equals(exKey))
                    this.put(exKey, o);
            }
        } else
            this.put(key, object);
        return this;
    }

    public void removeAll() throws JSONException {
        JSONArray array = this.names();
        assert array != null;
        if(array.length() > 0) {
            for(int i = 0; i < array.length(); i++) {
                String key = array.getString(i);
                this.remove(key);
            }
        }
    }

}
