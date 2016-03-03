package com.cng.android.data;

/**
 * Created by game on 2016/2/27
 */
public enum EventType {
    Door ('D'), CardAccessed ('A'), DeviceDown ('d')
    ;

    public int code;

    EventType (int code) {
        this.code = code;
    }

    public static EventType parse (int code) {
        for (EventType type : values ()) {
            if (type.code == code)
                return type;
        }

        return null;
    }
}
