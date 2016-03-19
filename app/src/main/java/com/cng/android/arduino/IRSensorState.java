package com.cng.android.arduino;

/**
 * Created by game on 2016/3/19
 */
public enum IRSensorState {
    Alarm ('A'),
    Silent ('S')
    ;

    public char code;

    IRSensorState (char code) {
        this.code = code;
    }

    public static IRSensorState parse (char code) {
        for (IRSensorState state : values ()) {
            if (code == state.code) {
                return state;
            }
        }

        return null;
    }
}