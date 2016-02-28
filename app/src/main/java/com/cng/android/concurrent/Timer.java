package com.cng.android.concurrent;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.cng.android.CNG.D;

/**
 * Created by game on 2016/2/28
 */
public class Timer extends Thread {
    private boolean canceled = false;
    private long timeout;
    private ITimeoutListener listener;

    private static final String TAG = "Timer";

    public Timer (String name, long timeout, ITimeoutListener listener) {
        super (name);
        this.timeout  = timeout;
        this.listener = listener;
    }

    public Timer (String name, int timeout, TimeUnit unit, ITimeoutListener listener) {
        super (name);
        this.timeout  = unit.toMillis (timeout);
        this.listener = listener;
    }

    public void timing () {
        super.start ();
    }

    public void cancel () {
        canceled = true;
    }

    @Override
    public void run () {
        long now = System.currentTimeMillis ();
        if (D)
            Log.d (TAG, "Timer[" + getName () + "] watch at " + now);
        try {
            while (!canceled) {
                if (System.currentTimeMillis () - now >= timeout) {
                    if (D)
                        Log.d (TAG, "Timer[" + getName () + "] time out at " + System.currentTimeMillis () + ", raise the event to listener");
                    listener.onTimeout ();
                    return;
                }
            }
            if (D)
                Log.d (TAG, "Timer[" + getName () + "] canceled by user at " + System.currentTimeMillis ());
        } finally {
            if (pool.containsValue (this)) {
                pool.remove (this.getName ());
            }
        }
    }

    private static Map<String, Timer> pool = new HashMap<> ();

    public static void timing (String name, long timeout, ITimeoutListener listener) {
        if (!pool.containsKey (name)) {
            Timer timer = new Timer (name, timeout, listener);
            pool.put (name, timer);
            timer.timing ();
        } else {
            throw new IllegalStateException ("The timer named " + name + " is in pool");
        }
    }

    public static void timing (String name, int timeout, TimeUnit unit, ITimeoutListener listener) {
        if (!pool.containsKey (name)) {
            Timer timer = new Timer (name, timeout, unit, listener);
            pool.put (name, timer);
            timer.timing ();
        } else {
            throw new IllegalStateException ("The timer named " + name + " is in pool");
        }
    }

    public static void cancel (String name) {
        if (pool.containsKey (name)) {
            Timer timer = pool.remove (name);
            timer.cancel ();
        }
    }

    private static void timing (Timer timer) {
        if (!pool.containsKey (timer.getName ())) {
            pool.put (timer.getName (), timer);
            timer.timing ();
        } else {
            throw new IllegalStateException ("The timer named " + timer.getName () + " is in pool");
        }
    }
}