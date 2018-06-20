package ru.profapp.RanobeReader.Helpers;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;

/**
 * Created by Ruslan on 08.02.2018.
 */

public class StringHelper {

    private static volatile StringHelper instance;

    private StringHelper() {
    }

    public static StringHelper getInstance() {
        if (instance == null) {
            synchronized (StringHelper.class) {
                if (instance == null) {
                    instance = new StringHelper();
                }
            }
        }
        return instance;
    }

    public String cleanJson(String string) {

        String teststr = "\"\" \\ &lt; &gt; &quot; '";
        String str = StringEscapeUtils.escapeJson(teststr);
        String str1 = StringEscapeUtils.escapeJava(teststr);
        String str2= StringEscapeUtils.escapeHtml4(teststr);
        String str3= StringEscapeUtils.unescapeHtml3(teststr);
        String str4= StringEscapeUtils.unescapeHtml4(teststr);

        string = string.substring(0, string.lastIndexOf("}") + 1);
        string = StringEscapeUtils.unescapeHtml4(string);
 

        // Ранобэ.Рф
        String s1 = "\"content\":\"";
        String s2 = "\",\"payment\"";
        int a1 = string.indexOf(s1);
        int a2 = 0;
        if (a1 > 0) {
            a2 = string.indexOf(s2, a1);
        }
        if (a1 > 0 && a2 > 0) {
            String midS = string.substring(a1 + s1.length(), a2);

            midS = midS.replaceAll("([^\\\\])\"", "$1\\\\\"").replaceAll("([^\\\\])\"", "$1\\\\\"");
            return string.substring(0, a1) + s1 + midS + string.substring(
                    a2, string.length());
        }

        // Рулейт

        //title
        s1 = "\"title\":\"";
        s2 = "\",\"text\"";
        a1 = string.indexOf(s1);
        a2 = 0;
        if (a1 > 0) {
            a2 = string.indexOf(s2, a1);
        }
        if (a1 > 0 && a2 > 0) {
            String midS = string.substring(a1 + s1.length(), a2);
            midS = midS.replaceAll("([^\\\\])\"", "$1\\\\\"").replaceAll("([^\\\\])\"", "$1\\\\\"");
            return string.substring(0, a1) + s1 + midS + string.substring(
                    a2, string.length());
        }
        //text
        s1 = "\"text\":\"";
        s2 = "\",\"comments\"";
        a1 = string.indexOf(s1);
        a2 = 0;
        if (a1 > 0) {
            a2 = string.indexOf(s2, a1);
        }
        if (a1 > 0 && a2 > 0) {
            String midS = string.substring(a1 + s1.length(), a2);
            midS = midS.replaceAll("([^\\\\])\"", "$1\\\\\"").replaceAll("([^\\\\])\"", "$1\\\\\"");
            return string.substring(0, a1) + s1 + midS + string.substring(
                    a2, string.length());
        }

        return string.replaceAll("([^\"][^,\\[{\\\\])\"([^,\\]:}])", "$1\\\\\"$2").replaceAll(
                "([^\"][^,\\[{\\\\])\"([^,\\]:}])", "$1\\\\\"$2");
    }

    public String removeTags(String string) {

        return Jsoup.parse(string).text();

    }

    public String cleanAdditionalInfo(String string) {

        //string = string.replace("\n",", ");
        return Jsoup.parse(string).text();
    }
}
