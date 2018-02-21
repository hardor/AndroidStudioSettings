package ru.profapp.ranobereader.Common;

/**
 * Created by Ruslan on 12.02.2018.
 */

public class Constans {

    public static  String fragmentBundle = "fragmentType";
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
        RanobeRfGetBookInfo
    }
}
