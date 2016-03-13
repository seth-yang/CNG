package com.cng.android.arduino;

import com.cng.android.data.EnvData;
import com.cng.android.data.Event;

/**
 * Created by game on 2016/3/13
 */
public interface IArduinoListener {
    void onDataReceived (EnvData data);
    void onEventRaised (Event event);
    void onIRCodeReceived ();
}