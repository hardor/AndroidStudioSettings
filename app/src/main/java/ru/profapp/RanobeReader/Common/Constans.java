package ru.profapp.RanobeReader.Common;

import java.text.Format;
import java.text.SimpleDateFormat;

/**
 * Created by Ruslan on 12.02.2018.
 */

public class Constans {

    public static  String fragmentBundle = "fragmentType";

    public static Format updateTimeFormatter = new SimpleDateFormat("d дней  H часов");
    public enum FragmentType{
        Favorite,
        Rulate,
        Ranoberf,
        Search
    }

    public enum JsonObjectFrom{
        RulateGetReady,
        RulateGetBookInfo,
        RulateGetChapterText,
        RanobeRfGetReady,
        RanobeRfGetBookInfo,
        RanobeRfGetChapterText
    }
}
