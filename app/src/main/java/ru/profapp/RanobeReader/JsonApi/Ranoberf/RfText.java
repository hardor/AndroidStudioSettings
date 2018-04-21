package ru.profapp.RanobeReader.JsonApi.Ranoberf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RfText {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("part")
    @Expose
    private TextPart part;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public TextPart getPart() {
        return part;
    }

    public void setPart(TextPart part) {
        this.part = part;
    }

}
