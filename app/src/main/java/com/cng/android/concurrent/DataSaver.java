package com.cng.android.concurrent;

import android.util.Log;

import com.cng.android.data.EventType;
import com.cng.android.data.ExchangeData;
import com.cng.android.data.Result;
import com.cng.android.data.SetupItem;
import com.cng.android.db.DBService;
import com.cng.android.util.DataUtil;
import com.cng.android.util.GsonHelper;
import com.cng.android.util.HttpUtil;
import com.cng.android.util.Keys;
import com.cng.android.util.gson.EventTypeTranslator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.cng.android.CNG.D;
/**
 * Created by game on 2016/2/27
 */
public class DataSaver extends CancelableThread {
    public enum Mode {
        Ethernet, Local
    }

    private static final String TAG = "DataSaver";
    private static final int TIMEOUT = 20;

    private static final Object QUIT = "QUIT", FLUSH_DATABASE = "FLUSH_DATABASE";
    private static final Object locker = new byte[0];

    private BlockingQueue<Object> queue = new ArrayBlockingQueue<> (32);
    private List<ExchangeData> transformers = new ArrayList<> (60);
    private long touch = System.currentTimeMillis ();
    private String url, hostId;
    private Mode mode = Mode.Ethernet;
    private Gson g = new GsonBuilder ()
                         .registerTypeAdapter (EventType.class, new EventTypeTranslator ())
                         .create ();
    private Type type = new TypeToken<Result<Object>> () {}.getType ();

    public DataSaver () {
        super ("DataSaver", true);
    }

    @Override
    protected void beforeCancel () {
        queue.offer (QUIT);
    }

    public void write (ExchangeData transformer) {
        try {
            queue.offer (transformer, TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Log.w (TAG, ex.getMessage (), ex);
        }
    }

    public void flushDatabase () {
        try {
            queue.offer (FLUSH_DATABASE, TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Log.w (TAG, ex.getMessage (), ex);
        }
    }

    public Mode getMode () {
        return mode;
    }

    public void setMode (Mode mode) {
        this.mode = mode;
    }

    @Override
    protected void doWork () {
        try {
            Object object = queue.poll (TIMEOUT, TimeUnit.SECONDS);
            if (object == QUIT) {
                if (D)
                    Log.d (TAG, "receive a quit signal. kill myself :(");
                cancel (true);
            } else if (object == FLUSH_DATABASE) {
                if (D)
                    Log.d (TAG, "receive a flush database signal, process it.");
                Map<String, Object> map = DBService.getData ();
                if (map != null && !map.isEmpty ()) {
                    try {
                        if (D)
                            Log.d (TAG, "Trying to upload the saved data.");
                        uploadData (map);
                        if (D) {
                            Log.d (TAG, "Upload the saved data success. now clean the local backup up.");
                        }
                        DBService.flushData ();
                    } catch (IOException ex) {
                        ex.printStackTrace ();
                    }
                }
            } else if (object instanceof ExchangeData) {
                if (D) {
                    Log.d (TAG, "receive a exchange data.");
                    Log.d (TAG, "data = " + (GsonHelper.getGson (true, true).toJson (object)));
                }
                ExchangeData transformer = (ExchangeData) object;

                List<ExchangeData> copy = null;
                synchronized (locker) {
                    transformers.add (transformer);
                    if (D)
                        Log.d (TAG, "append the data into cache.");

                    if (transformers.size () >= 60 || System.currentTimeMillis () - touch >= 60000) {
                        if (D)
                            Log.d (TAG, "the cache is full or timeout, upload them to cloud server.");
                        touch = System.currentTimeMillis ();
                        copy = new ArrayList<> (transformers);
                        transformers.clear ();
                    }
                }

                if (copy != null) {
                    if (mode == Mode.Ethernet) {
                        try {
                            uploadData (copy);
                        } catch (Exception ex) {
                            Log.w (ex.getMessage (), ex);
                            DBService.saveData (copy);
                        }
                    } else if (mode == Mode.Local) {
                        DBService.saveData (copy);
                    }
                }
            }
        } catch (InterruptedException ex) {
            Log.w (TAG, ex.getMessage (), ex);
        }
    }

    private void uploadData (List<ExchangeData> data) throws IOException {
        Map<String, Object> map = DataUtil.toMap (data);
        uploadData (map);
    }

    private void uploadData (Map<String, Object> map) throws IOException {
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

        String content = g.toJson (map);
        Result<Object> result = HttpUtil.post (url, content, type);
        if (result.getState () != Result.State.ok) {
            throw new IOException ("upload data fail");
        }
    }
}