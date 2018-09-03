package ru.profapp.RanobeReader.JsonApi.RanobeHub;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RanobeHubReadyGson {

    @SerializedName("raw")
    @Expose
    private Raw raw;

    public Raw getRaw() {
        return raw;
    }

    public void setRaw(Raw raw) {
        this.raw = raw;
    }


}

