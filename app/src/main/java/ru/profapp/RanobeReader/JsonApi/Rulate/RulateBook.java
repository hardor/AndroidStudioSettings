package ru.profapp.RanobeReader.JsonApi.Rulate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RulateBook {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("book_id")
    @Expose
    private Integer bookId;
    @SerializedName("s_title")
    @Expose
    private String sTitle;
    @SerializedName("t_title")
    @Expose
    private String tTitle;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("img")
    @Expose
    private String img;
    @SerializedName("ready_date")
    @Expose
    private String readyDate;
    @SerializedName("lang")
    @Expose
    private String lang;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getSTitle() {
        return sTitle;
    }

    public void setSTitle(String sTitle) {
        this.sTitle = sTitle;
    }

    public String getTTitle() {
        return tTitle;
    }

    public void setTTitle(String tTitle) {
        this.tTitle = tTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getReadyDate() {
        return readyDate;
    }

    public void setReadyDate(String readyDate) {
        this.readyDate = readyDate;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

}
