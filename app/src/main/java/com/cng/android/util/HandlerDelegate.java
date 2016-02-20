package com.cng.android.util;

import android.os.Handler;
import android.os.Message;

import com.cng.android.util.IMessageHandler;

/**
 * Created by game on 2016/2/20
 */
public class HandlerDelegate extends Handler {
    private IMessageHandler handler;

    public HandlerDelegate (IMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleMessage (Message message) {
        handler.handleMessage (message);
    }
}
