package ru.profapp.RanobeReader.JsonApi.Ranoberf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResultBookInfo {

    @SerializedName("book")
    @Expose
    private RfBook book;
    @SerializedName("parts")
    @Expose
    private List<RfChapter> parts = null;
    @SerializedName("genres")
    @Expose
    private List<Genre> genres = null;
    @SerializedName("mTimerNextUpdate")
    @Expose
    private TimerNextUpdate mTimerNextUpdate;
    @SerializedName("readPart")
    @Expose
    private String readPart;

    public RfBook getBook() {
        return book;
    }

    public void setBook(RfBook book) {
        this.book = book;
    }

    public List<RfChapter> getParts() {
        return parts;
    }

    public void setParts(List<RfChapter> parts) {
        this.parts = parts;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public TimerNextUpdate getTimerNextUpdate() {
        return mTimerNextUpdate;
    }

    public void setTimerNextUpdate(TimerNextUpdate timerNextUpdate) {
        this.mTimerNextUpdate = timerNextUpdate;
    }

    public String getReadPart() {
        return readPart;
    }

    public void setReadPart(String readPart) {
        this.readPart = readPart;
    }

}
