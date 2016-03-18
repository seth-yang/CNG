package com.cng.android.util;

/**
 * Created by game on 2016/2/24
 */
public interface Keys {
    String IR_COMMAND                         = "com.cng.android.keys.IR_COMMAND";
    String ACTION_NETWORK_STATE_CHANGED       = "com.cng.android.actions.NETWORK_STATE_CHANGED";
    String FILTER_NETWORK_STATE_CHANGED       = "com.cng.android.filters.NETWORK_STATE_CHANGED";
    
    String KEY_IR_CODE_NAME                   = "com.cng.android.keys.KEY_IR_CODE_NAME";

    String TAG_ARDUINO                        = "arduino";

    int RESULT_CODE_OK                        = 0;

    interface Actions {
        String SET_ARDUINO                    = "com.cng.android.actions.SET_ARDUINO";
        String ON_RECEIVE_IR_COMMAND          = "com.cng.android.actions.ON_RECEIVE_IR_COMMAND";
    }

    interface DataNames {
        String APP_VERSION                    = "app.version";
        String APP_UUID                       = "app.uuid";
        String SAVED_MAC                      = "saved.mac";
        String CLOUD_URL                      = "url.cloud";
        String GATHER_INTERVAL                = "gather.interval";
        String UPLOAD_INTERVAL                = "upload.interval";
        String HELLO_INTERVAL                 = "hello.interval";
        String CARD_MAJOR_VERSION             = "card.major.version";
        String CARD_MINOR_VERSION             = "card.minor.version";

        String OPEN_AIR_CONTROL               = "air.control.open";
        String CLOSE_AIR_CONTROL              = "air.control.close";
    }
}
