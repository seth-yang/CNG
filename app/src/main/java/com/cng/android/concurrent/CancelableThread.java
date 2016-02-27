package com.cng.android.concurrent;

import android.util.Log;

import static com.cng.android.CNG.D;
/**
 * Created by seth on 16-1-14
 */
public abstract class CancelableThread extends Thread implements ICancelable {
    protected boolean running = false, paused = false;

    protected static final Object locker = new byte [0];

    private static final String TAG = "CancelableThread";
    private static final long TIMEOUT = 30000;
    private static int INDEX = 0;

    protected abstract void doWork ();
    protected void beforeCancel () {}

    public CancelableThread () {
        this ("CancelableThread#" + (++ INDEX), false, false);
    }

    public CancelableThread (String name) {
        this (name, false, false);
    }

    public CancelableThread (String name, boolean running) {
        this (name, running, false);
    }

    public CancelableThread (String name, boolean running, boolean paused) {
        super (name);
        this.running = running;
        this.paused = paused;
        if (running) {
            start ();
        }
    }

    public boolean isPaused () {
        synchronized (locker) {
            return paused;
        }
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
    public void cancel (boolean block) {
        beforeCancel ();
        if (D) {
            Log.d (TAG, "trying to stop " + getName () + "...");
        }

        if (isPaused ()) {
            proceed ();
        }
        running = false;

        if (block && (Thread.currentThread () != this)) try {
            this.join ();
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }
        if (D)
            Log.d (TAG, "Server [" + getName () + "] stopped.");
    }

    @Override
    public boolean isCanceled () {
        return running;
    }

    @Override
    public void run () {
        if (D)
            Log.d (TAG, "Starting thread[" + getName () + "]");
        while (running) {
            while (isPaused ()) {
                synchronized (locker) {
                    try {
                        locker.wait (TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace ();
                    }
                }
            }

            try {
                doWork ();
            } catch (Exception ex) {
                Log.w (TAG, ex.getMessage (), ex);
            }
        }
        if (D)
            Log.d (TAG, "Thread[" + getName () + "] stopped.");
    }
}