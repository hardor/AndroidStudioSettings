package ru.profapp.RanobeReader.Common;

/**
 * Created by Ruslan on 12.02.2018.
 */

public class RanobeConstans {

    public static String fragmentBundle = "fragmentType";
    public static Integer chapterCount = 100;

    public enum FragmentType {
        Favorite,
        Rulate,
        Ranoberf,
        Search
    }

    public enum JsonObjectFrom {
        RulateGetReady,
        RulateGetBookInfo,
        RulateGetChapterText,
        RanobeRfGetReady,
        RanobeRfGetBookInfo,
        RanobeRfGetChapterText,
        RanobeRfSearch,
        RulateSearch,
        RulateFavorite,

    }
}
