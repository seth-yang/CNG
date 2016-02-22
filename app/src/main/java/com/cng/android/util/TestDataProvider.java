package com.cng.android.util;

import com.cng.android.data.IDataProvider;
import com.cng.android.data.Transformer;

/**
 * Created by game on 2016/2/22
 */
public class TestDataProvider extends Thread implements IDataProvider {
    private static final Object locker = new byte [0];

    private Transformer data;
    private boolean running = true;

    public TestDataProvider () {
        start ();
    }

    @Override
    public Transformer getData () {
        synchronized (locker) {
            return data;
        }
    }

    @Override
    public void run () {
        double seed = Math.random () * 150 - 25;

        while (running) {
            Transformer transformer = new Transformer ();
            double h = Math.random () * 99 + 1, t;
//                   t = Math.random () * 150 - 25;
            transformer.setHumidity (h);
            double w = Math.random ();
            if (w < .4)
                t = ++seed;
            else if (w > .6)
                t = --seed;
            else
                t = seed;
            transformer.setTemperature (t);
            synchronized (locker) {
                data = transformer;
            }
            try {
                sleep (1000);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
    }

    public void shutdown () {
        running = false;
    }
}
