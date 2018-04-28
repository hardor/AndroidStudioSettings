package ru.profapp.RanobeReader.Common;

/**
 * Created by Ruslan on 12.02.2018.
 */

public class RanobeConstans {

    public static final String fragmentBundle = "fragmentType";
    public static final Integer chapterCount = 100;

    public enum FragmentType {
        Favorite,
        Rulate,
        Ranoberf,
        Search,
        History
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
