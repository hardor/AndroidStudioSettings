package ru.profapp.RanobeReader.Models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

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
    private String Text;

    @Ignore
    public TextChapter(@NonNull String charpterUrl, String text) {
        this.ChapterUrl = charpterUrl;
        this.Text = text;
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
}
