package com.cng.android.util;

import com.cng.android.data.IDataProvider;
import com.cng.android.data.Transformer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by game on 2016/2/22
 */
public class TestDataProvider extends Thread implements IDataProvider {
    private static final Object locker = new byte [0];

    private FixedSizeQueue<Transformer> queue;
    private boolean running = true;

    public TestDataProvider () {
        queue = new FixedSizeQueue<> (60);
        start ();
    }

    @Override
    public Queue<Transformer> getNodes () {
        synchronized (locker) {
            return new LinkedList<> (queue);
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
                queue.add (transformer);
            }
            try {
                sleep (100);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
    }

    public void shutdown () {
        running = false;
    }
}
