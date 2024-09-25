package com.pixtanta.android.Utils;

import java.util.Objects;

public class StringUtils {

    private static final String emptyString = "";
    private static final String whiteSpace = " ";
    private static final String lineBreaks = "\\n";
    private static final String nullString = "null";

    public static boolean isEmpty(String s){
        if(!(s == null) && !Objects.equals(s, nullString)) {
            s = s.replace(whiteSpace, emptyString);
            s = s.replaceAll(lineBreaks, emptyString);
            int len = s.length();
            return len == 0;
        }
        return true;
    }
}
