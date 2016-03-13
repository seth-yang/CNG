package com.cng.android.arduino;

import android.util.Log;

import com.cng.android.CNG;
import com.cng.android.util.DataUtil;
import com.cng.android.util.Keys;

/**
 * Created by game on 2016/2/29
 */
public class ArduinoCommand {
    public static final byte
            // commands
            CMD_SET              = 'S',
            CMD_RESET            = 'R',
            CMD_TOGGLE           = 'T',
            CMD_SEND_DATA        = 'D',

            // types
            TYPE_DATA_TIMEOUT    = 'D',
            TYPE_HELLO_TIMEOUT   = 'H',
            TYPE_IR_MODE         = 'M',

            // target
            TARGET_FAN           = 'F',
            TARGET_IR            = 'I',
            TARGET_REMOTE        = 'R',
            TARGET_KEY           = 'K',
            TARGET_DOOR          = 'D',

            // IR mode
            IR_MODE_LEARN        = 'L',
            IR_MODE_SILENT       = 'S',

            DATA_OPEN            = 1,
            DATA_CLOSE           = 0;

    public static final byte[] CMD_HELLO         = {'H', 'E', 'L', 'L', 'O', '!'};
    public static final byte[] CMD_LEARN_IR_CODE = { CMD_SET,       TYPE_IR_MODE, IR_MODE_LEARN,  0, 0, 0 };
    public static final byte[] CMD_IR_SILENT     = { CMD_SET,       TYPE_IR_MODE, IR_MODE_SILENT, 0, 0, 0 };
    public static final byte[] CMD_OPEN_FAN      = { CMD_SEND_DATA, TARGET_FAN,   0, 0, 0, 1 };
    public static final byte[] CMD_CLOSE_FAN     = { CMD_SEND_DATA, TARGET_FAN,   0, 0, 0, 0 };
    public static final byte[] CMD_OPEN_DOOR     = { CMD_SEND_DATA, TARGET_DOOR,  0, 0, 0, 1 };
    public static final byte[] CMD_ERROR_BEEP    = { CMD_SEND_DATA, TARGET_DOOR,  0, 0, 0, 0 };

    public static byte[] set (byte type, int value) {
        byte[] command = new byte[6];
        command[0] = CMD_SET;
        command[1] = type;
        setIntToBytes (command, value);

        if (CNG.D)
            Log.d (Keys.TAG_ARDUINO, DataUtil.toHex (command));
        return command;
    }

    public static byte[] toggle (byte target) {
        byte[] command = new byte[6];
        command [0] = CMD_TOGGLE;
        command [1] = target;
        return command;
    }

    private static void setIntToBytes (byte[] buff, int value) {
        buff [4] = (byte) ((value >> 8) & 0xff);
        buff [5] = (byte) (value & 0xff);
    }
}