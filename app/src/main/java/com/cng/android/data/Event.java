package com.cng.android.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by game on 2016/2/27
 */
public class Event {
    @Expose
    @SerializedName ("TS")
    public long timestamp;

    @Expose
    @SerializedName ("T")
    public EventType type;

    @Expose
    @SerializedName ("D")
    public String data;
}