package ru.profapp.RanobeReader.Models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Created by Ruslan on 21.02.2018.
 */
@Entity(tableName = "textChapter")
public class TextChapter {

    public TextChapter() {
    }

    @PrimaryKey
    @NonNull
    private String ChapterUrl;
    private String ChapterName;
    private String RanobeName;
    private String Text;
    private int Index;

    @Ignore
    public TextChapter(@NonNull String chapterUrl, String text, String chapterName, String ranobeName,int index) {
        this.ChapterUrl = chapterUrl;
        this.Text = text;
        ChapterName= chapterName;
        RanobeName = ranobeName;
        Index =index;
    }

    @NonNull
    public String getChapterUrl() {
        return ChapterUrl;
    }

    public void setChapterUrl(@NonNull String chapterUrl) {
        this.ChapterUrl = chapterUrl;
    }

    public String getText() {
        return Text == null? "" :Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getChapterName() {
        return ChapterName;
    }

    public void setChapterName(String chapterName) {
        ChapterName = chapterName;
    }

    public String getRanobeName() {
        return RanobeName;
    }

    public void setRanobeName(String ranobeName) {
        RanobeName = ranobeName;
    }

    public int getIndex() {
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }
}
