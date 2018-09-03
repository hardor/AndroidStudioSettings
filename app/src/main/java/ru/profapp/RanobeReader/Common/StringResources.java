package ru.profapp.RanobeReader.Common;

/**
 * Created by Ruslan on 09.02.2018.
 */

public class StringResources {
    public static final String Rulate_Login_Pref = "preference_rulate_login";
    public static final String Ranoberf_Login_Pref = "preference_ranoberf_login";
    public static final String Last_readed_Pref = "preference_readed_history";
    public static final String is_readed_Pref = "preference_is_readed";
    public static final String KEY_Login = "login";
    public static final String KEY_Password = "password";
    public static final String KEY_Token = "token";
    public static final String Rulate_Site = "tl.rulate.ru";
    public static final String RanobeRf_Site = "https://xn--80ac9aeh6f.xn--p1ai";
    public static final String RanobeHub_Site = "https://ranobehub.org";
    public static final String Title_Site = "Title";

    public static final String Chapter_Position = "position";
    public static final String Chapter_Url = "chapterUrl";

    public static String CleanString(String ranobeUrl) {
        return ranobeUrl.replaceAll("[^a-zA-Z0-9]", "");
    }

    public enum LogType {
        ASSERT, DEBUG, ERROR, INFO, VERBOSE, WARN
    }

}
