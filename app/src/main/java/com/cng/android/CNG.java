package com.cng.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;

import com.cng.android.db.DBService;
import com.cng.android.service.StateMonitorService;
import com.cng.android.util.IMessageHandler;
import com.cng.android.util.Keys;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by game on 2016/2/20
 */
public class CNG extends Application implements Runnable, IMessageHandler {
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

    @Override
    public void onCreate () {
        super.onCreate ();
        if (D)
            Log.d (TAG, "CNG Application create.");
        instance = this;
        runInNonUIThread (this);
    }

    public static void checkAndSetupNetwork (final Activity activity) {
        if (!isNetworkConnected (activity)) {
            new AlertDialog.Builder (activity).setPositiveButton (activity.getString (R.string.btn_yes), new DialogInterface.OnClickListener () {
                @Override
                public void onClick (DialogInterface dialogInterface, int i) {
                    Intent intent;

                    try {
                        String sdkVersion = android.os.Build.VERSION.SDK;
                        if(Integer.valueOf (sdkVersion) > 10) {
                            intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                        }else {
                            intent = new Intent ();
                            ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                            intent.setComponent(comp);
                            intent.setAction ("android.intent.action.VIEW");
                        }
                        activity.startActivity (intent);
                    } catch (Exception e) {
                        Log.w (TAG, "open network settings failed, please check...");
                        e.printStackTrace ();
                    }
                }
            }).setNegativeButton (activity.getString (R.string.btn_no), new DialogInterface.OnClickListener () {
                @Override
                public void onClick (DialogInterface dialog, int i) {
                    dialog.cancel ();
                    activity.finish ();
                }
            }).setTitle (activity.getString (R.string.no_network))
                    .setMessage (activity.getString (R.string.setup_network_prompt)).show ();
        }
    }

    public static boolean isNetworkConnected (Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo ();
        return info != null && info.isAvailable ();
    }

    @Override
    public void run () {
        DBService.init (this);
/*
        if (!DBService.exist (Keys.APP_VERSION)) {

        }
*/


    }

    @Override
    public void handleMessage (Message message) {
        Intent intent = new Intent (this, StateMonitorService.class);
        startService (intent);
    }
}