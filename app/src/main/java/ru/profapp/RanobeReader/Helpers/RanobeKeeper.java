package ru.profapp.RanobeReader.Helpers;

import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Models.Ranobe;

/**
 * Created by Ruslan on 03.03.2018.
 */

public class RanobeKeeper {
    private static volatile RanobeKeeper instance;
    public static Integer chapterCount = 100;
    private Ranobe ranobe;
    private String RanobeRfToken;

    private Integer ChapterTextSize;
    private Boolean HideUnavailableChapters;
    private Boolean AutoSaveText;

    private RanobeConstans.FragmentType mFragmentType;

    private RanobeKeeper() {
    }

    public static RanobeKeeper getInstance() {
        if (instance == null) {
            synchronized (RanobeKeeper.class) {
                if (instance == null) {
                    instance = new RanobeKeeper();
                }
            }
        }
        return instance;
    }

    public RanobeConstans.FragmentType getFragmentType() {
        return mFragmentType;
    }

    public void setFragmentType(RanobeConstans.FragmentType fragmentType) {
        mFragmentType = fragmentType;
    }

    public Ranobe getRanobe() {
        if(ranobe == null)
            return new Ranobe();
        return ranobe;
    }

    public void setRanobe(Ranobe ranobe) {
        this.ranobe = ranobe;
    }

    public Integer getChapterTextSize() {

        return ChapterTextSize;
    }

    public void setChapterTextSize(Integer chapterTextSize) {
        ChapterTextSize = chapterTextSize;
    }

    public Boolean HidePaidChapters() {
        return HideUnavailableChapters == null ? false : HideUnavailableChapters;
    }

    public void setHideUnavailableChapters(Boolean hideUnavailableChapters) {
        HideUnavailableChapters = hideUnavailableChapters;
    }

    public Boolean getAutoSaveText() {
        return AutoSaveText;
    }

    public void setAutoSaveText(Boolean autoSaveText) {
        AutoSaveText = autoSaveText;
    }

    public String getRanobeRfToken() {
        return RanobeRfToken;
    }

    public void setRanobeRfToken(String ranobeRfToken) {
        RanobeRfToken = ranobeRfToken;
    }
}
