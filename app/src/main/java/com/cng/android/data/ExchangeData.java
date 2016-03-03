package com.cng.android.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by game on 2016/2/28
 */
public class ExchangeData {
    @Expose
    @SerializedName ("D")
    public EnvData data;

    @Expose
    @SerializedName ("E")
    public Event   event;

    @Expose
    @SerializedName ("I")
    public IRCommand ir;

    @Override
    public String toString () {
        return "{" +
                "    data : " + data + "," +
                "    event: " + event + "," +
                "    IR   : " + ir + "}";
    }
}