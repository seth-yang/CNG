package com.cng.android.service;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;

import com.cng.android.util.BluetoothDiscover;
import com.cng.android.util.IBluetoothListener;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class BluetoothReconnectService extends IntentService implements IBluetoothListener {
    private BluetoothDiscover discover;
    private String savedMac;

    public static final String ACTION_DEVICE_FOUND = "action.bt.found";

    public BluetoothReconnectService () {
        super ("BluetoothReconnectService");
    }

    @Override
    public void onCreate () {
        super.onCreate ();

    }

    @Override
    protected void onHandleIntent (Intent intent) {
        if (intent == null) return;

        savedMac = intent.getStringExtra ("mac");
        if (savedMac == null) {
            throw new RuntimeException ("No saved mac!");
        }
        synchronized (BluetoothReconnectService.class) {
            if (discover == null)
                discover = new BluetoothDiscover (this, this);
        }
        discover.discovery ();
    }

    @Override
    public void onDeviceFound (BluetoothDevice device) {
        String mac = device.getAddress ();
        if (mac.equals (savedMac)) {
            Intent intent = new Intent (ACTION_DEVICE_FOUND);
            IntentFilter filter = new IntentFilter (ACTION_DEVICE_FOUND);
//            intent.set
//            sendBroadcast ();
            discover.cancel ();
        }
    }
}
