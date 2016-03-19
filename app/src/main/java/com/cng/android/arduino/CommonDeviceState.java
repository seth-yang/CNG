package com.cng.android.arduino;

/**
 * Created by game on 2016/3/19
 */
public enum CommonDeviceState {
    On ('U'), Off ('d')
    ;

    public final char code;
    CommonDeviceState (char code) {
        this.code = code;
    }

    public static CommonDeviceState parse (int code) {
        for (CommonDeviceState state : values ()) {
            if ((char) code == state.code)
                return state;
        }

        return null;
    }
}