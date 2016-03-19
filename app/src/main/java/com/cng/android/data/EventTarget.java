package com.cng.android.data;

/**
 * Created by game on 2016/2/27
 */
public enum EventTarget {
//  android name    arduino value     android   arduino
    CardAccessed    ('C'),         //   <------------      The card accessed event (<), the ir-code was included in it.
    Door            ('D'),         //   <----------->      The door target(>)/state(<)
    Fan             ('F'),         //   <----------->      The fan target(>)/state(<)
    IR              ('I'),         //   <------------      The IR-Sensor event(<)
    Lock            ('K'),         //   <----------->      The lock target(>)/state(<)
    Mode            ('M'),         //   <----------->      The ir-remote mode target(>)/state(<)
    Light           ('L'),         //   <----------->      The light target(>)/state(<)
    Remote          ('R')          //   ------------>      The remote code, only from android to arduino
    ;

    public final int code;

    EventTarget (int code) {
        this.code = code;
    }

    public static EventTarget parse (int code) {
        for (EventTarget type : values ()) {
            if (type.code == code)
                return type;
        }

        return null;
    }
}
