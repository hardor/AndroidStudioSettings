package ru.profapp.RanobeReader.JsonApi.Rulate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChapterTextGson {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("response")
    @Expose
    private RulateText response;

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

    public RulateText getResponse() {
        return response;
    }

    public void setResponse(RulateText response) {
        this.response = response;
    }

}