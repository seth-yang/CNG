package com.cng.android.concurrent;

import android.util.Log;

import com.cng.android.CNG;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by game on 2016/2/22
 */
public class BluetoothWriter implements Runnable {
    private static final String TAG = "BluetoothWriter";
    private static final byte[] HELLO = {'H', 'E', 'L', 'L', 'O'};

    private String name;
    private OutputStream out;
    private long interval = 8000;
    private boolean running = true;

    public BluetoothWriter (String name, OutputStream out) {
        this.name = name;
        this.out = out;
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
        if (CNG.D)
            Log.d (TAG, "I'll say HELLO to BT device every " + interval + "milliseconds, let's go!");
        while (running) {
            try {
                out.write (HELLO);
            } catch (IOException ex) {
                Log.w (TAG, ex.getMessage (), ex);
            }

            try {
                Thread.sleep (interval);
            } catch (InterruptedException ex) {
                Log.w (TAG, ex.getMessage (), ex);
            }
        }
        if (CNG.D)
            Log.d (TAG, "shutdown the thread.");
    }

    public void shutdown () {
        synchronized (this) {
            running = false;
        }
    }

    public void setInterval (long interval) {
        this.interval = interval;
    }
}
