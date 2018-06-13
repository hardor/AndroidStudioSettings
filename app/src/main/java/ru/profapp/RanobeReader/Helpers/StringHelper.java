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
        return string.substring(0, string.lastIndexOf("}") + 1).replace("\\&quot;",
                "").replace("&lt;","<").replace("&gt;",">").replaceAll(
                "([^\"][^,\\[{\\\\])\"([^,\\]:}])", "$1\\\\\"$2").replaceAll(
                "([^\"][^,\\[{\\\\])\"([^,\\]:}])", "$1\\\\\"$2").replace("\\>","\\\\>");
    }

    public String removeTags(String string) {

        return Jsoup.parse(string).text();

    }

    public String cleanAdditionalInfo(String string) {

        //string = string.replace("\n",", ");
        return Jsoup.parse(string).text();
    }
}
