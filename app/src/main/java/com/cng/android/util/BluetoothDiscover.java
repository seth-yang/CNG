package com.cng.android.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import com.cng.android.CNG;
import com.cng.android.receiver.BluetoothReceiver;

/**
 * Created by game on 2016/2/20
 */
public class BluetoothDiscover {
    private Context context;
    private IBluetoothListener listener;
    private BluetoothAdapter adapter;
    private BluetoothReceiver receiver;

    private static final String TAG = BluetoothDiscover.class.getSimpleName ();

    public BluetoothDiscover (Context context, IBluetoothListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void discovery () {
        receiver = new BluetoothReceiver (listener);
        IntentFilter filter = new IntentFilter (BluetoothDevice.ACTION_FOUND);
        context.registerReceiver (receiver, filter);
        adapter = BluetoothAdapter.getDefaultAdapter ();
        if (!adapter.isEnabled ())
            adapter.enable ();

        adapter.startDiscovery ();
    }

    public void cancel () {
        if (receiver != null) {
            if (CNG.D)
                Log.d (TAG, "unregister receiver...");
            context.unregisterReceiver (receiver);
            receiver = null;
            if (CNG.D)
                Log.d (TAG, "receiver released.");
        }

        if (adapter != null) {
            if (CNG.D)
                Log.d (TAG, "canceling discovering");
            adapter.cancelDiscovery ();
            adapter = null;
            if (CNG.D)
                Log.d (TAG, "BT adapter released.");
        }
    }
}
