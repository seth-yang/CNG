package com.cng.android.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by game on 2016/2/21
 */
public class Transformer implements Serializable {
    @SerializedName ("T")
    public Double temperature = 0d;
    @SerializedName ("H")
    public Double humidity = 0d;

    public final long timestamp;

    public Transformer () {
        timestamp = System.currentTimeMillis ();
    }

    public Transformer (Double temperature, Double humidity) {
        this.temperature = temperature;
        this.humidity = humidity;
        timestamp = System.currentTimeMillis ();
    }

    public Double getTemperature () {
        return temperature;
    }

    public void setTemperature (Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity () {
        return humidity;
    }

    public void setHumidity (Double humidity) {
        this.humidity = humidity;
    }
}