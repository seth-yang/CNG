package com.cng.android;

/**
 * Created by game on 2016/2/26
 */
public class CNGException extends RuntimeException {
    private int resourceId;

    public CNGException (int resourceId) {
        this.resourceId = resourceId;
    }

    public CNGException (String detailMessage, int resourceId) {
        super (detailMessage);
        this.resourceId = resourceId;
    }

    public CNGException (String detailMessage, Throwable throwable, int resourceId) {
        super (detailMessage, throwable);
        this.resourceId = resourceId;
    }

    public CNGException (Throwable throwable, int resourceId) {
        super (throwable);
        this.resourceId = resourceId;
    }

    public int getResourceId () {
        return resourceId;
    }
}