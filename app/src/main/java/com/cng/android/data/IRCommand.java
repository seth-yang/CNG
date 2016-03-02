package com.cng.android.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by game on 2016/3/3
 */
public class IRCommand implements Serializable {
    @Expose
    @SerializedName ("C")
    public long code;

    @Expose
    @SerializedName ("T")
    public int  type;
}