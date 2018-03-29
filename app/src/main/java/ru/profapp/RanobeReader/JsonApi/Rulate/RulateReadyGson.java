package ru.profapp.RanobeReader.JsonApi.Rulate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RulateReadyGson {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("mBooks")
    @Expose
    private List<RulateBook> mBooks = null;

    public RulateReadyGson() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<RulateBook> getBooks() {
        return mBooks;
    }

    public void setBooks(List<RulateBook> books) {
        this.mBooks = books;
    }
}

