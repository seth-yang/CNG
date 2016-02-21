package com.cng.android;

import android.app.Application;
import android.util.Log;

import com.cng.android.db.DBService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by game on 2016/2/20
 */
public class CNG extends Application {
    private static final ExecutorService service = Executors.newCachedThreadPool ();
    private static CNG instance;

    public static final boolean D = true;
    public static final int RESULT_CODE_OK         = 0;
    public static final int RESULT_CODE_CANCEL     = 1;
    public static final int RUNNING_MODE_NORMAL    = 0;
    public static final int RUNNING_MODE_REQUESTED = 1;

    public static final String ACTION_MATCH_BT_DEVICE = "action.match.bluetooth.device";
    public static final String ACTION_START_CONNECT   = "action.start.connect";
    public static final String ACTION_NOT_BIND        = "do-not-bind-service";

    private static final String TAG = "CNG";

    public static void runInNonUIThread (Runnable runnable) {
        service.execute (runnable);
    }

//    public static void runInNonUIThread ()

    @Override
    public void onCreate () {
        super.onCreate ();
        if (D)
            Log.d (TAG, "CNG Application create.");
        instance = this;
        runInNonUIThread (new Runnable () {
            @Override
            public void run () {
                DBService.init (instance);
            }
        });
    }
}