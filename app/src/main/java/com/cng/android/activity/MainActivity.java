package com.cng.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cng.android.CNG;
import com.cng.android.R;
import com.cng.android.db.DBService;
import com.cng.android.service.MonitorServiceBinder;
import com.cng.android.service.StateMonitorService;
import com.cng.android.ui.ChartView;
import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.IMessageHandler;

import static com.cng.android.CNG.D;

/**
 * Created by game on 2016/2/20
 */
public class MainActivity extends Activity implements Runnable, IMessageHandler {
    public static final String TAG = MainActivity.class.getSimpleName ();

    private static final int REQ_CODE                    = 1;
    private static final int SET_DIALOG_TITLE_CONNECTING = 2;
    private static final int DEVICE_CONNECTED            = 3;
    private static final int START_SERVICE               = 4;

    private static final int LOCAL_STATE_DEFAULT         = 0;
    private static final int LOCAL_STATE_CONNECTING      = 1;

    private ProgressDialog dialog;
    private Handler handler;
    private Message local = new Message ();
    private MonitorServiceBinder binder;
    private Intent service;
    private boolean serviceStarted = false, running = false;

    private ServiceConnection conn = new ServiceConnection () {
        @Override
        public void onServiceConnected (ComponentName name, IBinder service) {
            binder = (MonitorServiceBinder) service;
            if (CNG.D) {
                Log.d (TAG, "ServiceConnection.onServiceConnected::name = " + name);
                Log.d (TAG, "The binder is: " + binder);
            }
        }

        @Override
        public void onServiceDisconnected (ComponentName name) {
            binder = null;
            if (D)
                Log.d (TAG, "Service disconnected. name = " + name);
        }
    };

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        if (savedInstanceState != null) {
            serviceStarted = savedInstanceState.getBoolean ("serviceStarted");
        }

        handler = new HandlerDelegate (this);
        service = new Intent (this, StateMonitorService.class);

        dialog = new ProgressDialog (this);
        dialog.setTitle (R.string.title_processing);
        dialog.show ();

        Intent intent = getIntent ();
        String value = intent.getStringExtra (CNG.ACTION_NOT_BIND);
        Log.d (TAG, "value = " + value);
    }

    @Override
    protected void onResume () {
        super.onResume ();
        Log.d (TAG, "++++++++++++ Main Activity onResume ++++++++++++");
        dialog.show ();

        CNG.runInNonUIThread (this);
    }

    @Override
    protected void onPause () {
        if (binder != null)
            unbindService (conn);
        running = false;
        super.onPause ();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState (outState);
        if (serviceStarted) {
            if (D)
                Log.d (TAG, "The service is started. save it");
            outState.putBoolean ("serviceStarted", serviceStarted);
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater ().inflate (R.menu.main, menu);
        return super.onCreateOptionsMenu (menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        processMenuItem (item);
        return super.onOptionsItemSelected (item);
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater ().inflate (R.menu.main, menu);
        super.onCreateContextMenu (menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item) {
        processMenuItem (item);
        return super.onContextItemSelected (item);
    }

    @Override
    public void run () {
        String savedMac = DBService.getSavedBTMac ();
        if (savedMac == null) {
            handler.sendEmptyMessage (REQ_CODE);
        } else {
            if (!serviceStarted) {
                handler.sendEmptyMessage (START_SERVICE);
                if (D)
                    Log.d (TAG, "Starting the service, wait for it started.");
                try {
                    Thread.sleep (3000);
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
            }

            if (D)
                Log.d (TAG, "trying to bind the service ");
            if (binder == null) {
                if (bindService (service, conn, BIND_AUTO_CREATE)) {
                    if (D) {
                        Log.d (TAG, "The service bound.");
                        Log.d (TAG, "Now, The binder is: " + binder);
                    }
                } else if (D) {
                    Log.w (TAG, "Can't bind to the service!!!");
                }
            }

            Log.d (TAG, "++++++++++++ waiting for connect to BT device ++++++++++++");
            Log.d (TAG, "Binder = " + binder);
            running = true;
            while (running) {
//                Log.d (TAG, "binder = " + binder + ", connected = " + (binder != null && binder.isConnected ()));
                if (binder != null && binder.isConnected ()) {
                    if (CNG.D)
                        Log.d (TAG, "The bluetooth device is connected.");
                    handler.sendEmptyMessage (DEVICE_CONNECTED);

                    return;
                }

                try {
                    Thread.sleep (10);
                } catch (InterruptedException ex) {
                    Log.w (TAG, ex.getMessage (), ex);
                }
            }

            if (D)
                Log.d (TAG, "The Local Thread stopped.");
        }
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case REQ_CODE :
                showFindActivity ();
                break;
            case SET_DIALOG_TITLE_CONNECTING :
                dialog.setTitle (R.string.title_connecting);
                dialog.show ();
                break;
            case DEVICE_CONNECTED :
                dialog.dismiss ();
                ((ChartView) findViewById (R.id.chart)).setProvider (binder);
                break;
            case START_SERVICE :
                Intent intent = new Intent (this, StateMonitorService.class);
                startService (intent);
                serviceStarted = true;
                break;
        }
    }

    private void showFindActivity () {
        Intent intent = new Intent (this, ShowBTDeviceActivity.class);
        startActivity (intent);
    }

    private void processMenuItem (MenuItem item) {
        switch (item.getItemId ()) {
            case R.id.menu_choose_device :
                showFindActivity ();
                break;
        }
    }
}