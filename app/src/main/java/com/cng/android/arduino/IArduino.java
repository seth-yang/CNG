package com.cng.android.arduino;

/**
 * Created by game on 2016/3/4
 */
public interface IArduino {
    void write (byte[] data);
    void setArduinoListener (IArduinoListener listener);
    IRRemoteMode getIrRemoteMode ();
    CommonDeviceState getFanState ();
    IRSensorState getIrSensorState ();
    CommonDeviceState getDoorState ();
    CommonDeviceState getLockState ();
    CommonDeviceState getLightState ();
}