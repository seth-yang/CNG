package com.cng.android.util;

import android.bluetooth.BluetoothDevice;

/**
 * Created by game on 2016/2/20
 */
public interface IBluetoothListener {
    void onDeviceFound (BluetoothDevice device);
}