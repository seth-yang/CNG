package com.cng.android.concurrent;

import android.os.Bundle;
import android.os.Message;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by game on 2016/3/4
 */
public abstract class NonUIHandler implements INonUIMessageHandler {
    private static final ExecutorService service = Executors.newCachedThreadPool ();

    public void sendMessage (int what) {
        Message message = new Message ();
        message.what = what;
        sendMessage (message);
    }

    public void sendMessage (int what, Bundle bundle) {
        Message message = new Message ();
        message.what = what;
        message.setData (bundle);
        sendMessage (message);
    }

    public void sendMessage (int what, String name, Serializable value) {
        Message message = new Message ();
        message.what = what;
        Bundle bundle = new Bundle (1);
        bundle.putSerializable (name, value);
        message.setData (bundle);
        sendMessage (message);
    }

    public void sendMessage (Message message) {
        service.execute (new NonUIRunner (this, message));
    }
}