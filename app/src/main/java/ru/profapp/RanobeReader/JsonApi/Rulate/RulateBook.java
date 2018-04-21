package ru.profapp.RanobeReader.JsonApi.Rulate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

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

    @SerializedName("n_chapters")
    @Expose
    private Integer nChapters;

    @SerializedName("last_activity")
    @Expose
    private Long lastActivity;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("rating")
    @Expose
    private String rating;
    @SerializedName("author")
    @Expose
    private String author;

    @SerializedName("writer")
    @Expose
    private String writer;
    @SerializedName("publisher")
    @Expose
    private String publisher;
    @SerializedName("year")
    @Expose
    private String year;
    @SerializedName("chapters_total")
    @Expose
    private Integer chaptersTotal;
    @SerializedName("adult")
    @Expose
    private Integer adult;
    @SerializedName("team")
    @Expose
    private String team;
    @SerializedName("chapters")
    @Expose
    private List<RulateChapter> chapters = null;
    @SerializedName("comments")
    @Expose
    private List<RulateComment> comments = null;
    @SerializedName("bookmark")
    @Expose
    private Integer bookmark;

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

    public Integer getNChapters() {
        return nChapters;
    }

    public void setNChapters(Integer nChapters) {
        this.nChapters = nChapters;
    }

    public Long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Long lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Integer getChaptersTotal() {
        return chaptersTotal;
    }

    public void setChaptersTotal(Integer chaptersTotal) {
        this.chaptersTotal = chaptersTotal;
    }

    public Integer getAdult() {
        return adult;
    }

    public void setAdult(Integer adult) {
        this.adult = adult;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public List<RulateChapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<RulateChapter> chapters) {
        this.chapters = chapters;
    }

    public List<RulateComment> getComments() {
        return comments;
    }

    public void setComments(List<RulateComment> comments) {
        this.comments = comments;
    }

    public Integer getBookmark() {
        return bookmark;
    }

    public void setBookmark(Integer bookmark) {
        this.bookmark = bookmark;
    }

}
