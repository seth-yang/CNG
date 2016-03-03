package com.cng.android.concurrent;

import android.util.Log;

import com.cng.android.CNG;
import com.cng.android.data.SetupItem;
import com.cng.android.db.DBService;
import com.cng.android.util.Keys;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.cng.android.CNG.D;

/**
 * Created by game on 2016/2/22
 */
public class BluetoothWriter implements Runnable {
    private static final String TAG = "BluetoothWriter";
    private static final byte[] HELLO = {'H', 'E', 'L', 'O'};
    private static final byte CMD_SET              = 'S',
                              TYPE_DATA_TIMEOUT    = 'D',
                              TYPE_HELLO_TIMEOUT   = 'H';

    public static final Object QUIT = new byte [0];

    private String name;
    private OutputStream out;
    private long interval = 1000;
    private boolean running = true;

    private BlockingQueue<Object> queue = new ArrayBlockingQueue<> (8);

    public BluetoothWriter (String name, OutputStream out) {
        this.name = name;
        this.out = out;
    }

    public void setHelloInterval (int interval) {
        if (D)
            Log.d (TAG, "Set the hello interval to " + interval + " seconds.");
        byte[] command = new byte[4];
        command [0] = CMD_SET;
        command [1] = TYPE_HELLO_TIMEOUT;
        setIntToBytes (command, interval);
        queue.offer (command);
    }

    public void setDataInterval (int interval) {
        if (D)
            Log.d (TAG, "Set the data interval to " + interval + " seconds.");
        byte[] command = new byte[4];
        command [0] = CMD_SET;
        command [1] = TYPE_DATA_TIMEOUT;
        setIntToBytes (command, interval);
        queue.offer (command);
    }

    public void write (byte[] buff) {
        write (buff, 0, buff.length);
    }

    public void write (byte[] buff, int start, int length) {
        if (start != 0 || length != buff.length) {
            byte[] copy = new byte [length];
            System.arraycopy (buff, start, copy, 0, length);
            buff = copy;
        }
        queue.offer (buff);
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
        SetupItem item = DBService.getSetupItem (Keys.DASHBOARD_INTERVAL);
        if (item != null) {
            interval = ((Number) item.getValue ()).intValue () * 1000;
        }
        if (CNG.D)
            Log.d (TAG, "I'll say HELLO to BT device every " + interval + " milliseconds, let's go!");
        while (running) {
/*
            Object o = null;
            try {
                o = queue.poll (interval, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Log.w (TAG, ex.getMessage (), ex);
            }
*/
            Object o = queue.poll ();

            if (o != QUIT) {
                byte[] command;
/*
                if (o == null)
                    command = HELLO;
                else
*/
                if (o != null) {
                    command = (byte[]) o;

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
        }
    }

    public void setInterval (long interval) {
        this.interval = interval;
    }

    private void setIntToBytes (byte[] buff, int value) {
        buff [2] = (byte) ((value >> 8) & 0xff);
        buff [3] = (byte) (value & 0xff);
    }
}
