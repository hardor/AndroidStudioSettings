package ru.profapp.RanobeReader.Helpers;

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

        string = string.substring(0, string.lastIndexOf("}") + 1).replace("\\&quot;",
                "").replace("&lt;", "<").replace("&gt;", ">")
                .replace("\\>", "\\\\>");

        // Ранобэ.Рф
        int a1 = string.indexOf("\"content\":\"");
        int b1 = 0;
        if (a1 > 0) {
            b1 = string.indexOf("\",\"payment\"", a1);
        }
        if (a1 > 0 && b1 > 0) {
            String midS = string.substring(a1 + "\"content\":\"".length(), b1);
            midS = midS.replaceAll("([^\\\\])\"", "$1\\\\\"");
            return string.substring(0, a1) + "\"content\":\"" + midS + string.substring(
                    b1, string.length());
        }

        // Рулейт
         a1 = string.indexOf("\"text\":\"");
         b1 = 0;
        if (a1 > 0) {
            b1 = string.indexOf("\",\"comments\"", a1);
        }
        if (a1 > 0 && b1 > 0) {
            String midS = string.substring(a1 + "\"text\":\"".length(), b1);
            midS = midS.replaceAll("([^\\\\])\"", "$1\\\\\"");
            return string.substring(0, a1) + "\"text\":\"" + midS + string.substring(
                    b1, string.length());
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
