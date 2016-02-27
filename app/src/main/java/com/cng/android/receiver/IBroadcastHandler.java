package com.cng.android.receiver;

import android.content.Context;
import android.content.Intent;

/**
 * Created by game on 2016/2/28
 */
public interface IBroadcastHandler {
    void onReceive (Context context, Intent intent);
}