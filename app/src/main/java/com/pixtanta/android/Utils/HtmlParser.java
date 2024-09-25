package com.pixtanta.android.Utils;

import android.content.Context;

public class HtmlParser {

    private static final String regexPattern = "(^|\\s)((?:https?://|[a-z0-9-_]\\d{0,3}[.]|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))";
    private static final String paragraphStartLeft = "<p dir=\"ltr\">";
    private static final String paragraphStartRight = "<p dir=\"rtl\">";
    private static final String paragraphEnd = "</p>";
    private static final String lineBreak = "<br>";
    private static final String doubleBreak = "<br><br>";
    private static final String tripleBreak = "<br><br><br>";
    private static final String emptyString = "";
    private static final String whiteSpace = " ";
    private static final String doubleSpace = "  ";
    private static final String spanStyleDark = "<span style=\"background-color:#585858;\">";
    private static final String spanStyleLight = "<span style=\"background-color:#F9F9F9;\">";
    private static final String span = "<span>";
    private static final String breakLine = lineBreak + paragraphEnd;
    private static final String regexReplace = "$1<a href=\"$2\">$2</a>";

    private static String removeWhitespaces(String htmlString){
        htmlString = removeDoubleSpace(htmlString);
        return htmlString.replace(paragraphStartLeft + whiteSpace, paragraphStartLeft)
                .replace(paragraphEnd + whiteSpace, paragraphEnd)
                .replace(whiteSpace + paragraphStartRight, paragraphStartRight)
                .replace(whiteSpace + paragraphEnd, paragraphEnd);
    }

    private static String removeDoubleBreaks(String htmlString){
        return htmlString.replace(doubleBreak, paragraphStartLeft + paragraphEnd);
    }

    private static String removeDoubleSpace(String htmlString){
        if(htmlString.contains(doubleSpace)){
            htmlString = htmlString.replace(doubleSpace, whiteSpace);
            return removeDoubleSpace(htmlString);
        }
        return htmlString;
    }

    private static String removeTripleBreaks(String htmlString){
        if(htmlString.contains(tripleBreak)){
            htmlString = htmlString.replace(tripleBreak, doubleBreak);
            return removeTripleBreaks(htmlString);
        }
        return htmlString;
    }

    private static String removeParagraphs(String htmlString){
        if(htmlString.contains(paragraphStartLeft + paragraphEnd)){
            htmlString = htmlString.replace(paragraphStartLeft + paragraphEnd, emptyString);
            return removeParagraphs(htmlString);
        }
        return htmlString;
    }

    private static String finalizeParse(String htmlString){
        htmlString = htmlString.replace(paragraphStartLeft, doubleBreak)
                .replace(paragraphEnd, emptyString);
        if(htmlString.startsWith(doubleBreak))
            htmlString = htmlString.substring(8);
        return htmlString;
    }

    public static String parseURL(String htmlString){
        return htmlString.replaceAll(regexPattern, regexReplace);
    }

    public static String parseSpan(String htmlString){
        htmlString = htmlString.replace(spanStyleLight, span)
                .replace(spanStyleDark, span);
        return parseURL(htmlString);
    }

    public static String parseTheme(String htmlString, boolean darkTheme){
        if(darkTheme)
            htmlString = htmlString.replace(span, spanStyleDark);
        else
            htmlString = htmlString.replace(span, spanStyleLight);
        return parseURL(htmlString);
    }

    public static String parseBreaks(String htmlString){
        htmlString = htmlString.replace(paragraphStartLeft, emptyString)
                .replace(paragraphStartRight, emptyString)
                .replace(paragraphEnd, whiteSpace)
                .replace(lineBreak, whiteSpace);
        htmlString = removeDoubleSpace(htmlString);
        return parseURL(htmlString);
    }

    public static String parseLineBreaks(String htmlString){
        return htmlString.replace(breakLine, paragraphEnd);
    }

    public static String removeLastWhitespaces(String htmlString){
        if(htmlString.endsWith(whiteSpace)){
            int strLn = htmlString.length() - 1;
            htmlString = htmlString.substring(0, strLn);
            return removeLastWhitespaces(htmlString);
        }
        return htmlString;
    }

    public static String parseString(String htmlString) {
        htmlString = removeDoubleSpace(htmlString);
        htmlString = removeWhitespaces(htmlString);
        htmlString = removeTripleBreaks(htmlString);
        htmlString = removeDoubleBreaks(htmlString);
        htmlString = removeParagraphs(htmlString);
        htmlString = finalizeParse(htmlString);
        htmlString = removeLastWhitespaces(htmlString);
        htmlString = parseSpan(htmlString);
        htmlString = htmlString.trim();
        return htmlString;
    }

}
