package com.cng.android.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by game on 2016/2/21
 */
public class EnvData implements Serializable {
    @Expose
    @SerializedName ("T")
    public Double temperature;
    @Expose
    @SerializedName ("H")
    public Double humidity;
    @Expose
    @SerializedName ("S")
    public Double smoke;
    @Expose
    @SerializedName ("TS")
    public long timestamp;

    public EnvData () {
        timestamp = System.currentTimeMillis ();
    }

    public EnvData (Double temperature, Double humidity) {
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

    public Double getSmoke () {
        return smoke;
    }

    public void setSmoke (Double smoke) {
        this.smoke = smoke;
    }

    @Override
    public String toString () {
        String temp = super.toString ();
        int pos = temp.indexOf ('@');
        return "{temp:" + temperature + ",humidity:" + humidity + ",smoke:" + smoke + "}@" + temp.substring (pos + 1);
    }
}