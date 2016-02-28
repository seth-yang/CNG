package com.cng.android.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.cng.android.CNG;
import com.cng.android.R;
import com.cng.android.activity.DashboardActivity;
import com.cng.android.concurrent.BluetoothWriter;
import com.cng.android.concurrent.DataSaver;
import com.cng.android.data.ExchangeData;
import com.cng.android.data.SetupItem;
import com.cng.android.data.EnvData;
import com.cng.android.db.DBService;
import com.cng.android.receiver.BroadcastReceiverDelegate;
import com.cng.android.receiver.IBroadcastHandler;
import com.cng.android.util.BluetoothDiscover;
import com.cng.android.util.IBluetoothListener;
import com.cng.android.util.Keys;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
//import java.util.UUID;

import static com.cng.android.CNG.D;

public class StateMonitorService extends IntentService
        implements IBluetoothListener, IBroadcastHandler {
//    public static final UUID SPP_UUID = UUID.fromString ("00001101-0000-1000-8000-00805F9B34FB");
//    public static final UUID SPP_UUID = UUID.fromString ("a60f35f0-b93a-11de-8a39-08002009c666");

    private static final String TAG = StateMonitorService.class.getSimpleName ();
    private static final int STATE_START_MAIN_ACTIVITY = 0;
    private static final Object locker = new byte [0];

    private static boolean running = false;

    private IBinder binder;
    private DataSaver saver;
    private BroadcastReceiverDelegate receiver;

    private BluetoothDevice device;
    private BluetoothDiscover discover;
    private BluetoothSocket socket;
    private boolean connected;
    private String savedMac;
    private EnvData data;

    public StateMonitorService () {
        super ("StateMonitorService");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate () {
        super.onCreate ();
        Log.d (TAG, "++++++++++++ Monitor Service Create ++++++++++++");

        receiver = new BroadcastReceiverDelegate (this);
        binder = new MonitorServiceBinder (this);
        discover = new BluetoothDiscover (this, this);

        if (D)
            Log.d (TAG, "Try setting up the notification");
        Intent target = new Intent (this, DashboardActivity.class);
        target.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pi = PendingIntent.getActivity (this, 2, target, 0);
        Notification notification = new Notification.Builder (this)
                .setContentTitle (getString (R.string.app_name))
                .setSmallIcon (R.drawable.dashboard)
                .setContentIntent (pi)
                .build ();
        startForeground (1, notification);
        if (D) {
            Log.d (TAG, "the notification setup done.");
            Log.d (TAG, "Try registering the broadcast receiver");
        }
        IntentFilter filter = new IntentFilter (Keys.ACTION_NETWORK_STATE_CHANGED);
        registerReceiver (receiver, filter);
        if (D)
            Log.d (TAG, "The broadcast receiver register success.");
    }

    @Override
    public IBinder onBind (Intent intent) {
        if (D)
            Log.d (TAG, "someone bound to me.");
        return binder;
    }

    @Override
    protected void onHandleIntent (Intent intent) {
        if (D) {
            Log.d (TAG, "handle intent");
        }

        synchronized (locker) {
            if (running) {
                if (D)
                    Log.d (TAG, "There's another instance is running, do nothing");
                return;
            }

            running = true;
        }

        if (D)
            Log.d (TAG, "first time in the service, discovery it");

        try {
            SetupItem item = DBService.getSetupItem (Keys.SAVED_MAC);
            if (item != null) {
                savedMac = (String) item.getValue ();
                if (D)
                    Log.d (TAG, "saved mac is: " + savedMac + ", starting to discover it...");

                discover.discovery ();
                synchronized (locker) {
                    try {
                        if (D)
                            Log.d (TAG, "Waiting for connect to bluetooth device ...");
                        locker.wait ();
                    } catch (InterruptedException e) {
                        e.printStackTrace ();
                    }
                }

                while (running)
                    connect ();
            }
        } finally {
            synchronized (locker) {
                running = false;
            }
        }
    }

    @Override
    public void onDestroy () {
        if (receiver != null)
            unregisterReceiver (receiver);
        super.onDestroy ();
        if (D)
            Log.d (TAG, "State Monitor Service destroyed.");
        Intent intent = new Intent (this, getClass ());
        startService (intent);
    }

    @Override
    public void onDeviceFound (BluetoothDevice device) {
        this.device = device;
        if (D) {
            Log.d (TAG, "find BT device = " + device);
            Log.d (TAG, "Current Thread = " + Thread.currentThread ().getName ());
        }

        if (device.getAddress ().equals (savedMac)) synchronized (locker) {
            locker.notifyAll ();
        }
    }

    @Override
    public void onReceive (Context context, Intent intent) {
        if (CNG.isNetworkConnected (context)) {
            if (saver != null && saver.isPaused ()) {
                saver.proceed ();
            }
        } else {
            if (saver != null && !saver.isPaused ()) {
                saver.pause ();
            }
        }
    }

    EnvData getData () {
        return data;
    }

    synchronized boolean isConnected () {
        return connected;
    }

    private boolean connectToDevice () throws IOException {
        int retry = 3;
        while (retry > 0) {
            if (D)
                Log.d (TAG, "Trying to connect to BT device ...");

            socket = null;
            try {
                Method m = device.getClass ().getDeclaredMethod ("createRfcommSocket", int.class);
                socket = (BluetoothSocket) m.invoke (device, 1);
            } catch (Exception e) {
                e.printStackTrace ();
            }
//            socket = device.createRfcommSocketToServiceRecord (SPP_UUID);
            try {
                socket.connect ();
                discover.cancel ();
                if (D)
                    Log.d (TAG, "The BT Device Connected!!!");
                return true;
            } catch (IOException ex) {
                retry --;
                if (D) {
                    Log.d (TAG, "Fail to connect to BT device, try again");
                }
                Log.w (TAG, ex.getMessage (), ex);
                try {
                    Thread.sleep (1000);
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
                if (socket != null) try {
                    socket.close ();
                } catch (IOException ioe) {
                    Log.w (TAG, ioe.getMessage (), ioe);
                }
            }
        }

        return false;
    }

    private void connect () {
        if (D)
            Log.d (TAG, "now, we got the bluetooth device, connect to it and listen data");
        BluetoothWriter writer = null;

        try {
            if (connectToDevice ()) {
                synchronized (this) {
                    connected = true;
                }

                InputStream in = socket.getInputStream ();
                writer = new BluetoothWriter ("BTHeartbeat", socket.getOutputStream ());

                saver = new DataSaver ();

                BufferedReader reader = new BufferedReader (new InputStreamReader (in));
                CNG.runInNonUIThread (writer);
                Gson g = new GsonBuilder ().create ();
                while (socket.isConnected ()) {
                    String line = reader.readLine ();
                    if (D)
                        Log.d (TAG, "Got a message: " + line);
                    try {
                        ExchangeData trans = g.fromJson (line.trim (), ExchangeData.class);
                        saver.write (trans);
                        synchronized (locker) {
                            data = trans.data;
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            }
        } catch (IOException ex) {
            Log.w (TAG, ex.getMessage (), ex);
        } finally {
            synchronized (this) {
                connected = false;
            }
            if (socket != null) try {
                socket.close ();
            } catch (IOException ex) {
                Log.w (TAG, ex.getMessage (), ex);
            }
            if (writer != null) {
                writer.shutdown ();
            }
            if (saver != null) {
                DataSaver temp = saver;
                this.saver = null;
                temp.cancel (true);
            }
        }
    }
}