package com.cng.android.service;

import android.os.Binder;

import com.cng.android.data.Node;

import java.util.Queue;

/**
 * Created by game on 2016/2/21
 */
public class MonitorServiceBinder extends Binder {
    private StateMonitorService service;

    MonitorServiceBinder (StateMonitorService service) {
        this.service = service;
    }

/*
    public StateMonitorService getService () {
        return service;
    }
*/
    public boolean isConnected () {
        return service.isConnected ();
    }

    public Queue<Node> getNodes () {
        return service.copyData ();
    }
}