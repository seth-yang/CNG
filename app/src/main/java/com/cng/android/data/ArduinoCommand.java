package com.cng.android.data;

/**
 * Created by game on 2016/2/29
 */
public class ArduinoCommand {
    public static final byte
            // commands
            CMD_SET              = 'S',
            CMD_RESET            = 'R',
            CMD_TOGGLE           = 'T',

            // types
            TYPE_DATA_TIMEOUT    = 'D',
            TYPE_HELLO_TIMEOUT   = 'H',

            // target
            TARGET_FAN           = 'F',
            TARGET_IR            = 'I',
            TARGET_REMOTE        = 'R',
            TARGET_KEY           = 'K';

    public static final byte[] CMD_HELLO = new byte[] {'H', 'E', 'L', 'O'};

    public static byte[] set (byte type, int value) {
        byte[] command = new byte[4];
        command[0] = CMD_SET;
        command[1] = type;
        setIntToBytes (command, value);
        return command;
    }

    public static byte[] toggle (byte target) {
        byte[] command = new byte[4];
        command [0] = CMD_TOGGLE;
        command [1] = target;
        return command;
    }

    private static void setIntToBytes (byte[] buff, int value) {
        buff [2] = (byte) ((value >> 8) & 0xff);
        buff [3] = (byte) (value & 0xff);
    }
}