package com.pixtanta.android;

import android.graphics.Bitmap;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MemoryCache {

    public static Map<String, Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap<>(10, 1.5f, true));
    private static long size = 0;
    private static long limit = 1000000;

    public MemoryCache(){
        setLimit(Runtime.getRuntime().maxMemory() / 4);
    }

    public void setLimit(long newLimit) {
        limit = newLimit;
    }

    public Bitmap get(String id){
        try {
            if(!cache.containsKey(id)){
                return null;
            }
            return cache.get(id);
        } catch (NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }

    public void put(String id, Bitmap bitmap){
        try{
            if(cache.containsKey(id)){
                size -= getSizeInBytes(cache.get(id));
            }
            cache.put(id, bitmap);
            size += getSizeInBytes(bitmap);
            checkSize();
        } catch (Throwable e){
            e.printStackTrace();
        }
    }

    public static void checkSize(){
        if(size > limit){
            Iterator<Map.Entry<String, Bitmap>> iter = cache.entrySet().iterator();
            while (iter.hasNext()){
                Map.Entry<String, Bitmap> entry = iter.next();
                size -= getSizeInBytes(entry.getValue());
                iter.remove();
                if(size <= limit){
                    break;
                }
            }
        }
    }

    public static void clear(){
        try {
            cache.clear();
            size = 0;
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    static long getSizeInBytes(Bitmap bitmap){
        if(bitmap == null){
            return  0;
        }
        return  bitmap.getRowBytes() * bitmap.getHeight();
    }

}
