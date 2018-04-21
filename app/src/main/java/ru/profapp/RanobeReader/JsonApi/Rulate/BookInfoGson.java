package ru.profapp.RanobeReader.JsonApi.Rulate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BookInfoGson {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("response")
    @Expose
    private RulateBook response;

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

    public RulateBook getResponse() {
        return response;
    }

    public void setResponse(RulateBook response) {
        this.response = response;
    }

}