package com.cng.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by game on 2016/2/28
 */
public class BroadcastReceiverDelegate extends BroadcastReceiver {
    private IBroadcastHandler handler;

    public BroadcastReceiverDelegate (IBroadcastHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive (Context context, Intent intent) {
        handler.onReceive (context, intent);
    }
}