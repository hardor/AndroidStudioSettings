package ru.profapp.RanobeReader.JsonApi.Ranoberf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RfResult {

    @SerializedName("sequence")
    @Expose
    private List<Sequence> sequence = null;
    @SerializedName("hasMore")
    @Expose
    private Boolean hasMore;
    @SerializedName("books")
    @Expose
    private List<RfBook> books = null;

    public List<Sequence> getSequence() {
        return sequence;
    }

    public void setSequence(List<Sequence> sequence) {
        this.sequence = sequence;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }

    public List<RfBook> getBooks() {
        return books;
    }

    public void setBooks(List<RfBook> books) {
        this.books = books;
    }

}

