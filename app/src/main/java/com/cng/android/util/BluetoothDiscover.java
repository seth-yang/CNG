package com.cng.android.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;

import com.cng.android.receiver.BluetoothReceiver;

/**
 * Created by game on 2016/2/20
 */
public class BluetoothDiscover {
    private Context context;
    private IBluetoothListener listener;
    private BluetoothAdapter adapter;
    private BluetoothReceiver receiver;

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
        if (receiver != null)
            context.unregisterReceiver (receiver);

        if (adapter != null)
            adapter.cancelDiscovery ();
    }
}