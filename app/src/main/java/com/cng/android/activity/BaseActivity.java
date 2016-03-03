package com.cng.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.cng.android.R;
import com.cng.android.concurrent.INonUIMessageHandler;
import com.cng.android.concurrent.NonUIHandler;
import com.cng.android.concurrent.NonUIHandlerDelegate;
import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.IMessageHandler;

/**
 * Created by game on 2016/3/4
 */
public class BaseActivity extends Activity implements IMessageHandler, INonUIMessageHandler, View.OnClickListener {
    protected Handler handler;
    protected NonUIHandler nonUIHandler;

    protected static final int SHOW_ERROR_MESSAGE = 0xff00ff00;

    private static final String ERROR_MESSAGE = "com.cng.android.activity.ERROR_MESSAGE";

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        handler      = new HandlerDelegate (this);
        nonUIHandler = new NonUIHandlerDelegate (this);
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case SHOW_ERROR_MESSAGE :
                break;
            default:
                break;
        }
    }

    @Override
    public void handleNonUIMessage (Message message) {

    }

    protected void showError (String text) {
        if (Looper.getMainLooper () == Looper.myLooper ()) {
            Toast.makeText (this, text, Toast.LENGTH_LONG).show ();
        } else {
            Message message = new Message ();
            message.what = SHOW_ERROR_MESSAGE;
            Bundle bundle = new Bundle (1);
            bundle.putString (ERROR_MESSAGE, text);
            handler.sendMessage (message);
        }
    }

    protected void showError (int resourceId) {
        showError (getString (resourceId));
    }

    @Override
    public void onClick (View v) {
        switch (v.getId ()) {
            case R.id.btn_back :
                finish ();
                break;
        }
    }
}