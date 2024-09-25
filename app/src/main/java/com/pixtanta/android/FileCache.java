package com.pixtanta.android;

import android.content.Context;

import java.io.File;

public class FileCache {
    private static File cacheDir;

    public FileCache(Context cntxt){
        cacheDir = cntxt.getCacheDir();
        if(!cacheDir.mkdirs()){
            cacheDir.mkdirs();
        }
    }

    public File getFile(String url){
        String filename = String.valueOf(url.hashCode());
        return new File(cacheDir, filename);
    }

    public static void clear(){
        File[] files = cacheDir.listFiles();
        if(files == null){
            return;
        }
        for(File f : files){
            f.delete();
        }
    }

}
