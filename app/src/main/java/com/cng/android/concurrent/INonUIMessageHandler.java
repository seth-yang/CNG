package com.cng.android.concurrent;

import android.os.Message;

/**
 * Created by game on 2016/3/4
 */
public interface INonUIMessageHandler {
    void handleNonUIMessage (Message message);
}