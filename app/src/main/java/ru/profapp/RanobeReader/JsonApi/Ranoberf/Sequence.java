package ru.profapp.RanobeReader.JsonApi.Ranoberf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sequence {
    @SerializedName("book_id")
    @Expose
    private Integer bookId;
    @SerializedName("parts")
    @Expose
    private String parts;

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getParts() {
        return parts;
    }

    public void setParts(String parts) {
        this.parts = parts;
    }
}
