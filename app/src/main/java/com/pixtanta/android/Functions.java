package com.pixtanta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import com.pixtanta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import io.socket.client.Socket;


public class Functions {

    static Socket socket;

    public static String joinJSONArray(JSONArray jsonArray, String d) {
        try {
            int lastIndex = jsonArray.length() - 1;
            StringBuilder str = new StringBuilder();
            for(int i = 0; i < jsonArray.length(); i++){
                if(lastIndex == i)
                    d = "";
                String s = jsonArray.getString(i);
                str.append(s).append(d);
            }
            return str.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertMilliTime(long timeInMillisec) {
        int sec = (int) (timeInMillisec / 1000);
        int min = sec / 60;
        int hr = sec / 3600;
        sec -= (min * 60);
        String strTime = "";

        String hrStr = hr + ":";
        if(hr < 10)
            hrStr = "0" + hrStr;
        strTime += hrStr;

        String minStr = min + ":";
        if(min < 10)
            minStr = "0" + minStr;
        strTime += minStr;

        String secStr = String.valueOf(sec);
        if(sec < 10)
            secStr = "0" + secStr;
        strTime += secStr;

        return strTime;

    }

    public static String checkFileType(String filePth){
        for (String ext : Constants.allowedExtImg){
            if(filePth.endsWith(ext))
                return "image";
        }
        for (String ext : Constants.allowedExtVid){
            if(filePth.endsWith(ext))
                return "video";
        }
        return "";
    }

    public static String timeConverter(long timestamp, boolean addStr){
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String dateStr = null;
        Date now = new Date();
        long milliDate = timestamp * 1000;
        long currTime = now.getTime() / 1000;
        long seconds = currTime - timestamp;
        long minutes = seconds / 60;
        long hours = seconds / 3600;
        long days = seconds / 86400;
        Date timeAgo = new Date(milliDate);
        String[] daysArr = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String[] monthArr = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timeAgo);
        Calendar curCalendar = Calendar.getInstance();
        curCalendar.setTime(now);
        String prompt = "am";
        int currY = curCalendar.get(Calendar.YEAR);
        int timeY = calendar.get(Calendar.YEAR);
        int monOfDate = calendar.get(Calendar.MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int dayOfWk = curCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int dayDiff = dayOfWk - dayOfWeek;
        String mon = monthArr[monOfDate];
        String w = daysArr[dayOfWeek];
        String dy = String.valueOf(d);
        String mn = String.valueOf(m);
        if(m < 10)
            mn = "0" + m;
        if(d < 10)
            dy = "0" + d;
        if(h > 12){
            prompt = "pm";
            h -= 12;
        }
        if(h == 12)
            prompt = "noon";
        String str = " at " + h + ":" + mn + prompt;
        if(!addStr)
            str = "";
        if(days > 6)
            dateStr = mon + " " + dy + str;
        if(timeY < currY)
            dateStr = timeY + " " + mon + " " + dy + str;
        if(days < 7)
            dateStr = w + str;
        if(hours < 24)
            dateStr = hours + "hrs ago";
        if(hours == 1)
            dateStr = hours + "hr ago";
        if(days < 2 && dayDiff == 1)
            dateStr = "Yesterday" + str;
        if(minutes < 60)
            dateStr = minutes + "mins ago";
        if(minutes == 1)
            dateStr = minutes + "min ago";
        if(seconds < 60)
            dateStr = "Just Now";
        return dateStr;
    }

    public static Bitmap decodeFiles(String path, String fileType, boolean resize) {
        Bitmap bitmap;
        if(fileType.equals("image")) {
            bitmap = BitmapFactory.decodeFile(path);
        } else {
            bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
        }
        if(resize)
            return decodeBitmap(bitmap);
        else
            return bitmap;
    }

    public static File getOutputMediaFile(String tempDir){
        File mediaStorageDir = new File(tempDir);

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        String randStr = UUID.randomUUID().toString();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            RandomString gen = new RandomString(8, ThreadLocalRandom.current());
            randStr = gen.toString();
        }
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        File mediaFile;
        String imageName = tempDir + "/IMG_" + randStr + timeStamp +".jpg";
        mediaFile = new File(imageName);
        return mediaFile;
    }

    public static Bitmap decodeBitmap(Bitmap bitmap) {
        int btmpW, btmpH, cropSize, startX = 0, startY = 0;
        btmpW = bitmap.getWidth();
        btmpH = bitmap.getHeight();
        cropSize = btmpH;
        if(btmpW >= btmpH){
            startX = (btmpW / 2) - (btmpH / 2);
        } else {
            startY = (btmpH / 2) - (btmpW / 2);
            cropSize = btmpW;
        }
        return Bitmap.createBitmap(bitmap, startX, startY, cropSize, cropSize);
    }

    public static Bitmap getBitmapFromSource(String urlSrc, String fileType, boolean resize){
        Bitmap bitmap = null;
        if(fileType.equals("image"))
            bitmap = getBitmapFromURL(urlSrc, resize);
        if(fileType.equals("video")) {
            try {
                bitmap = getVideoThumbnail(urlSrc, resize);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return bitmap;
    }

    public static Bitmap getBitmapFromURL(String src, boolean resize) {
        if(StringUtils.isEmpty(src))
            return null;
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            if(resize)
                return decodeBitmap(myBitmap);
            else
                return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public static Bitmap getVideoThumbnail(String videoPath, boolean resize)throws Throwable {
        Bitmap bitmap;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            // For SDK versions < 14 use
            // mediaMetadataRetriever.setDataSource(videoPath);
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<>());
            bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)"+ e.getMessage());
        }
        finally {
            if(mediaMetadataRetriever != null){
                mediaMetadataRetriever.release();
            }
        }

        if(resize)
            return decodeBitmap(bitmap);
        else
            return bitmap;
    }

    public static String getMediaTime(String videoPath)throws Throwable {
        String time;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            // For SDK versions < 14 use
            // mediaMetadataRetriever.setDataSource(videoPath);
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<>());
            String timeMS = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillisec = Long.parseLong(timeMS);
            time = convertMilliTime(timeInMillisec);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)"+ e.getMessage());
        }
        finally {
            if(mediaMetadataRetriever != null){
                mediaMetadataRetriever.release();
            }
        }
        return time;
    }

    public static String convertToText(int number){
        String text = String.valueOf(number);
        if(number > 999){
            int thsnd = number/1000;
            int remd = number % 1000;
            int hund = remd / 100;
            if(hund > 0)
                text = thsnd + "." + hund + "k";
            else
                text = thsnd + "k";
        }
        if(number > 999999){
            int mlln = number/1000000;
            int remd = number % 1000000;
            int thsnd = remd / 100000;
            if(thsnd > 0)
                text = mlln + "." + thsnd + "m";
            else
                text = mlln + "m";
        }
        if(number > 999999999){
            int blln = number/1000000000;
            int remd = number % 1000000000;
            int thsnd = remd / 100000000;
            if(thsnd > 0)
                text = blln + "." + thsnd + "b";
            else
                text = blln + "b";
        }
        if(number < 1)
            text = "";
        return text;
    }

    public static String convertToTextPlus(int number){
        String text = String.valueOf(number);
        if(number > 99)
            text = "99+";
        return text;
    }

    public static boolean isJSONValid(String str){
        try {
            new JSONObject(str);
        } catch (JSONException e) {
            try {
                new JSONArray(str);
            } catch (JSONException ex) {
                return false;
            }
        }
        return true;
    }

    public static int convertToNumber(String text){
        int number;
        double multiply = 1.0;
        String num = text;
        if(StringUtils.isEmpty(text))
            number = 0;
        else {
            if (text.endsWith("k"))
                multiply = 1000.0;
            if (text.endsWith("m"))
                multiply = 1000000.0;
            if (text.endsWith("b"))
                multiply = 1000000000.0;
            if (multiply > 1.0)
                num = text.substring(0, text.length() - 1);
            double d = Double.parseDouble(num);
            d *= multiply;
            number = (int) d;
        }
        return number;
    }

    public static long getVideoDuration(MediaMetadataRetriever retriever, String videoPath, Context context){
        long timeInMillisec;
        if(!videoPath.startsWith("http")) {
            File file = new File(videoPath);
            retriever.setDataSource(context, Uri.fromFile(file));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillisec = Long.parseLong(time);
        } else {
            retriever.setDataSource(videoPath, new HashMap<>());
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillisec = Long.parseLong(time);
        }
        return timeInMillisec;
    }

    public static void sendReport(int user, String page, String report) throws JSONException {
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        JSONObject emitObj = new JSONObject();
        emitObj.put("user", user);
        emitObj.put("page", page);
        emitObj.put("report", report);
        emitObj.put("date", date);
        socket.emit("reportProblem", emitObj);
    }

    public static boolean endsWithBr(String string){
        return string.endsWith("<br></p>");
    }

}
