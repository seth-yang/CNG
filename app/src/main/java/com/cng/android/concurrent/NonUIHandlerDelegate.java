package com.cng.android.concurrent;

import android.os.Message;

/**
 * Created by game on 2016/3/4
 */
public class NonUIHandlerDelegate extends NonUIHandler {
    private INonUIMessageHandler handler;

    public NonUIHandlerDelegate (INonUIMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleNonUIMessage (Message message) {
        handler.handleNonUIMessage (message);
    }
}