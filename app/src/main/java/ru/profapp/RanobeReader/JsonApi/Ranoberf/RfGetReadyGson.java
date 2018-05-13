package ru.profapp.RanobeReader.JsonApi.Ranoberf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RfGetReadyGson {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("result")
    @Expose
    private RfResult result;
    @SerializedName("message")
    @Expose
    private String message;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RfResult getResult() {
        return result;
    }

    public void setResult(RfResult result) {
        this.result = result;
    }
}



