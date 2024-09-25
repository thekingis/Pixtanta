package com.pixtanta.android;

import java.io.InputStream;
import java.io.OutputStream;

public class Util {

    public static void copyStream(InputStream is, OutputStream os){
        final int bufferSize = 1024;
        try {
            byte[] bytes = new byte[bufferSize];
            while (true){
                int count = is.read(bytes, 0, bufferSize);
                if(count == -1){
                    break;
                }
                os.write(bytes, 0, count);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
