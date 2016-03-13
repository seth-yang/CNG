package com.cng.android.concurrent;

import android.util.Log;

import com.cng.android.CNG;
import com.cng.android.arduino.ArduinoCommand;
import com.cng.android.db.DBService;
import com.cng.android.util.Keys;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.cng.android.CNG.D;

/**
 * Created by game on 2016/2/22
 */
public class BluetoothWriter implements Runnable {
    private static final String TAG = "BluetoothWriter";

    public static final Object QUIT = new byte[0];
    private static final Object locker = new Object ();

    private String name;
    private OutputStream out;
    private boolean running = true, paused = true;

    private BlockingQueue<Object> queue = new ArrayBlockingQueue<> (8);

    public BluetoothWriter (String name, OutputStream out) {
        this.name = name;
        this.out = out;
    }

    public void setHelloInterval (int interval) {
        if (D)
            Log.d (TAG, "Set the hello interval to " + interval + " seconds.");
        byte[] command = new byte[4];
        command[0] = ArduinoCommand.CMD_SET;
        command[1] = ArduinoCommand.TYPE_HELLO_TIMEOUT;
        setIntToBytes (command, interval);
        queue.offer (command);
    }

    public void setDataInterval (int interval) {
        if (D)
            Log.d (TAG, "Set the data interval to " + interval + " seconds.");
        byte[] command = new byte[4];
        command[0] = ArduinoCommand.CMD_SET;
        command[1] = ArduinoCommand.TYPE_DATA_TIMEOUT;
        setIntToBytes (command, interval);
        queue.offer (command);
    }

    public void write (byte[] buff) {
        write (buff, 0, buff.length);
    }

    public void write (byte[] buff, int start, int length) {
        if (start != 0 || length != buff.length) {
            byte[] copy = new byte[length];
            System.arraycopy (buff, start, copy, 0, length);
            buff = copy;
        }
        queue.offer (buff);
    }

    public void pause () {
        synchronized (locker) {
            paused = true;
        }
    }

    public void proceed () {
        synchronized (locker) {
            paused = false;
            locker.notifyAll ();
        }
    }

    @Override
    public void run () {
        Thread.currentThread ().setName (name);
        if (CNG.D)
            Log.d (TAG, "Starting thread: " + name);
        if (out == null) {
            if (CNG.D) {
                Log.d (TAG, "There's no output stream to write. abort it.");
            }
            return;
        }

        long interval = DBService.SetupItem.getIntValue (Keys.DataNames.HELLO_INTERVAL, 10) * 1000L;
        if (CNG.D)
            Log.d (TAG, "I'll say HELLO to BT device every " + interval + " milliseconds, let's go!");
        while (running) {
            synchronized (locker) {
                while (paused) {
                    try {
                        if (CNG.D)
                            Log.d (TAG, "I'm paused. wait for signal");
                        locker.wait ();
                        if (CNG.D)
                            Log.d (TAG, "I'm wake up");
                    } catch (InterruptedException e) {
                        e.printStackTrace ();
                    }
                }
            }

            Object o = null;
            try {
                o = queue.poll (interval, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Log.w (TAG, ex.getMessage (), ex);
            }
            if (o != QUIT) {
                byte[] command;
                if (o != null) {
                    command = (byte[]) o;
/*
                } else {
                    command = ArduinoCommand.CMD_HELLO;
                }
*/
                    try {
                        out.write (command);
                        out.flush ();
                    } catch (IOException ex) {
                        Log.w (TAG, ex.getMessage (), ex);
                    }
                }
            } else {
                if (D)
                    Log.d (TAG, "receive a quit request. abort the thread.");
                break;
            }
        }
        if (CNG.D)
            Log.d (TAG, "shutdown the BT Writer thread.");
    }

    public void shutdown () {
        synchronized (this) {
            running = false;
            queue.offer (QUIT);
            if (paused)
                proceed ();
        }
    }

    private void setIntToBytes (byte[] buff, int value) {
        buff[2] = (byte) ((value >> 8) & 0xff);
        buff[3] = (byte) (value & 0xff);
    }
}
