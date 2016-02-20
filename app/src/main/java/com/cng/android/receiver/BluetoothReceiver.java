package com.cng.android.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cng.android.util.IBluetoothListener;

import java.util.HashSet;
import java.util.Set;

import static com.cng.android.CNG.D;

/**
 * Created by game on 2016/2/20
 */
public class BluetoothReceiver extends BroadcastReceiver {
    private Set<String> keys = new HashSet<> ();
    private IBluetoothListener listener;

    private static final String TAG = BluetoothReceiver.class.getSimpleName ();

    public BluetoothReceiver (IBluetoothListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive (Context context, Intent intent) {
        String action = intent.getAction ();
        if (BluetoothDevice.ACTION_FOUND.equals (action)) {
            BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
            String mac = device.getAddress ();
            if (D) {
                Log.d (TAG, "receive a device: " + device + ", mac = " + mac);
            }
            if (!keys.contains (mac)) {
                keys.add (mac);
                listener.onDeviceFound (device);
                if (D)
                    Log.d (TAG, "device " + device + " not in list, add it.");
            } else if (D) {
                Log.d (TAG, "device " + device + " is already in list, ignore it.");
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals (action)) {
            if (D)
                Log.d (TAG, "finish discovery");
        }
    }
}
