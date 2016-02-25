package com.cng.android.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by game on 2016/2/26
 */
public class Result<T> implements Serializable {
    public enum State {
        ok, fail
    }

    @Expose
    @SerializedName ("data")
    private T userData;

    @Expose
    private State state;

    public Result () {};

    public Result (State state, T userData) {
        this.state = state;
        this.userData = userData;
    }

    public T getUserData () {
        return userData;
    }

    public void setUserData (T userData) {
        this.userData = userData;
    }

    public State getState () {
        return state;
    }

    public void setState (State state) {
        this.state = state;
    }
}