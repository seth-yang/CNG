package com.cng.android.service;

import android.os.Binder;

import com.cng.android.data.EnvData;
import com.cng.android.data.IDataProvider;

/**
 * Created by game on 2016/2/21
 */
public class MonitorServiceBinder extends Binder implements IDataProvider {
    private StateMonitorService service;

    MonitorServiceBinder (StateMonitorService service) {
        this.service = service;
    }

    public boolean isConnected () {
        return service.isConnected ();
    }

    @Override
    public EnvData getData () {
        return service.getData ();
    }
}