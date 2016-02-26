package com.cng.android.concurrent;

import android.util.Log;

import static com.cng.android.CNG.D;
/**
 * Created by seth on 16-1-14
 */
public abstract class CancelableThread extends Thread implements ICancelable {
    protected boolean running = false;

    private static final String TAG = "CancelableThread";

    protected abstract void doWork ();
    protected void beforeCancel () {}

    public CancelableThread () {
    }

    public CancelableThread (String name) {
        super (name);
    }

    public CancelableThread (String name, boolean running) {
        super (name);
        this.running = running;
        if (running) {
            start ();
        }
    }

    @Override
    public void cancel (boolean block) {
        beforeCancel ();
        if (D) {
            Log.d (TAG, "trying to stop " + getName () + "...");
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