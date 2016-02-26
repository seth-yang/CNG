package com.cng.android.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.cng.android.CNG;
import com.cng.android.data.IDataProvider;
import com.cng.android.data.Transformer;

/**
 * Created by game on 2016/2/27
 */
public class BluetoothDataProvider implements IDataProvider, ServiceConnection {
    private boolean connected;
    private MonitorServiceBinder service;

    private static final String TAG = "BluetoothDataProvider";
    private static int count = 0;

    @Override
    public Transformer getData () {
        return service == null ? null : service.getData ();
    }

    @Override
    public void onServiceConnected (ComponentName name, IBinder service) {
        count ++;
        if (CNG.D) {
            Log.d (TAG, "================== on service connected");
            Log.d (TAG, "component name = " + name);
            Log.d (TAG, "count = " + count);
        }
        this.service = (MonitorServiceBinder) service;
        connected = true;
    }

    @Override
    public void onServiceDisconnected (ComponentName name) {
        count --;
        if (CNG.D) {
            Log.d (TAG, "================== on service disconnected.");
            Log.d (TAG, "component name = " + name);
            Log.d (TAG, "count = " + count);
        }
        this.service = null;
        this.connected = false;
    }

    public boolean isConnected () {
        return connected;
    }
}
