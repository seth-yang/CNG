package com.cng.android.concurrent;

import android.util.Log;

import com.cng.android.data.Transformer;
import com.cng.android.db.DBService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.cng.android.CNG.D;
/**
 * Created by game on 2016/2/27
 */
public class DataSaver extends CancelableThread {
    private static final String TAG = "DataSaver";
    private static final int TIMEOUT = 20;

    private static final Object QUIT = Void.class;
    private static final Object locker = new byte[0];

    private BlockingQueue<Object> queue = new ArrayBlockingQueue<> (32);
    private List<Transformer> transformers = new ArrayList<> (60);
    private long touch = System.currentTimeMillis ();

    public DataSaver () {
        super ("DataSaver", true);
    }

    @Override
    protected void beforeCancel () {
        queue.offer (QUIT);
    }

    public void write (Transformer transformer) {
        try {
            queue.offer (transformer, TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Log.w (TAG, ex.getMessage (), ex);
        }
    }

    @Override
    protected void doWork () {
        try {
            Object object = queue.poll (TIMEOUT, TimeUnit.SECONDS);
            if (object == QUIT) {
                if (D)
                    Log.d (TAG, "receive a quit signal. kill myself :(");
                return;
            }
            Transformer transformer = (Transformer) object;

            List<Transformer> copy = null;
            synchronized (locker) {
                transformers.add (transformer);

                if (transformers.size () >= 60 || System.currentTimeMillis () - touch >= 60000) {
                    touch = System.currentTimeMillis ();
                    copy = new ArrayList<> (transformers);
                    transformers.clear ();
                }
            }

            if (copy != null)
                DBService.saveData (copy);
        } catch (InterruptedException ex) {
            Log.w (TAG, ex.getMessage (), ex);
        }
    }
}