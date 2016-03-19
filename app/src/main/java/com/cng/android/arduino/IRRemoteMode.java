package com.cng.android.arduino;

/**
 * Created by game on 2016/3/19
 */
public enum IRRemoteMode {
    Learn ('L'), Silent ('S')
    ;

    public final char code;
    IRRemoteMode (char code) {
        this.code = code;
    }

    public static IRRemoteMode parse (int code) {
        for (IRRemoteMode mode : values ()) {
            if ((char) code == mode.code)
                return mode;
        }

        return null;
    }
}