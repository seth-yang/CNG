package com.cng.android.data;

/**
 * Created by game on 2016/2/27
 */
public enum EventType {
/*
    TARGET_FAN           = 'F',
    TARGET_IR            = 'I',
    TARGET_REMOTE        = 'R',
    TARGET_KEY           = 'K',
    TARGET_DOOR          = 'D',
*/

    Fan ('F'), IR ('I'), Remote ('R'), Lock ('K'), Door ('D'), CardAccessed ('C'), Mode ('M');

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
