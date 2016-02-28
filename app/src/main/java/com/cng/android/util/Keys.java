package com.cng.android.util;

/**
 * Created by game on 2016/2/24
 */
public interface Keys {
    String APP_VERSION                    = "app.version";
    String APP_UUID                       = "app.uuid";
    String SAVED_MAC                      = "saved.mac";
    String CLOUD_URL                      = "url.cloud";
    String DASHBOARD_INTERVAL             = "dashboard.interval";

    String ACTION_NETWORK_STATE_CHANGED   = "com.cng.android.actions.NETWORK_STATE_CHANGED";

    String FILTER_NETWORK_STATE_CHANGED   = "com.cng.android.filters.NETWORK_STATE_CHANGED";

    int RESULT_CODE_OK                    = 0;

    interface Actions {
        String SET_ARDUINO                = "com.cng.android.actions.SET_ARDUINO";
    }
}
