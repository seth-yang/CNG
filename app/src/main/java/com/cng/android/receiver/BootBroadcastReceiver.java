package com.cng.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cng.android.service.StateMonitorService;

public class BootBroadcastReceiver extends BroadcastReceiver {
    public BootBroadcastReceiver () {
    }

    @Override
    public void onReceive (Context context, Intent intent) {
        Intent service = new Intent (context, StateMonitorService.class);
        context.startService (service);
    }
}