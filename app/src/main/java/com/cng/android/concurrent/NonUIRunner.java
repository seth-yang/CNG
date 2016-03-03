package com.cng.android.concurrent;

import android.os.Message;

/**
 * Created by game on 2016/3/4
 */
class NonUIRunner implements Runnable {
    NonUIHandler handler;
    Message message;

    public NonUIRunner (NonUIHandler handler, Message message) {
        this.handler = handler;
        this.message = message;
    }

    @Override
    public void run () {
        handler.handleNonUIMessage (message);
    }
}