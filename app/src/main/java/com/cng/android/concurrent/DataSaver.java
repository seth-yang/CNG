package com.cng.android.concurrent;

import android.util.Log;

import com.cng.android.data.EnvData;
import com.cng.android.data.Result;
import com.cng.android.data.SetupItem;
import com.cng.android.db.DBService;
import com.cng.android.util.HttpUtil;
import com.cng.android.util.Keys;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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
    private List<EnvData> transformers = new ArrayList<> (60);
    private long touch = System.currentTimeMillis ();
    private String url, hostId;
    private Gson g = new Gson ();
    private Type type = new TypeToken<Result<Object>> () {}.getType ();

    public DataSaver () {
        super ("DataSaver", true);
    }

    @Override
    protected void beforeCancel () {
        queue.offer (QUIT);
    }

    public void write (EnvData transformer) {
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
            EnvData transformer = (EnvData) object;

            List<EnvData> copy = null;
            synchronized (locker) {
                transformers.add (transformer);

                if (transformers.size () >= 60 || System.currentTimeMillis () - touch >= 60000) {
                    touch = System.currentTimeMillis ();
                    copy = new ArrayList<> (transformers);
                    transformers.clear ();
                }
            }

            if (copy != null) {
                try {
                    uploadData (copy);
                } catch (Exception ex) {
                    Log.w (ex.getMessage (), ex);
                    DBService.saveData (copy);
                }
            }
        } catch (InterruptedException ex) {
            Log.w (TAG, ex.getMessage (), ex);
        }
    }

    private void uploadData (List<EnvData> data) throws IOException {
        if (url == null) {
            SetupItem item = DBService.getSetupItem (Keys.CLOUD_URL);
            if (item != null) {
                url = item.getValue () + "upload";
            }
        }

        if (hostId == null) {
            SetupItem item = DBService.getSetupItem (Keys.APP_UUID);
            if (item != null) {
                hostId = (String) item.getValue ();
                url += "?" + hostId;
            }
        }

/*
        List<Map<String, Object>> list = new ArrayList<> (data.size ());
        for (EnvData transformer : data) {
            Map<String, Object> map = new HashMap<> ();
            map.put ("hostId", hostId);
            map.put ("TS", transformer.timestamp);
            if (transformer.humidity != null) {
                map.put ("H", transformer.humidity);
            }
            if (transformer.temperature != null) {
                map.put ("T", transformer.temperature);
            }
            if (transformer.smoke != null) {
                map.put ("S", transformer.smoke);
            }
            list.add (map);
        }
*/

        String content = g.toJson (data);
        Result<Object> result = HttpUtil.post (url, content, type);
        if (result.getState () != Result.State.ok) {
            throw new IOException ("upload data fail");
        }
    }
}