package ru.profapp.RanobeReader.Helpers;

import ru.profapp.RanobeReader.Models.Ranobe;

/**
 * Created by Ruslan on 03.03.2018.
 */

public class RanobeKeeper {
    private static volatile RanobeKeeper instance;

    private Ranobe ranobe;

    private Integer ChapterTextSize;
    private Boolean HideUnavailableChapters;

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

    public Ranobe getRanobe() {
        return ranobe ;
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

    public Boolean getHideUnavailableChapters() {
        return HideUnavailableChapters ==null ? false : HideUnavailableChapters;
    }

    public void setHideUnavailableChapters(Boolean hideUnavailableChapters) {
        HideUnavailableChapters = hideUnavailableChapters;
    }

}