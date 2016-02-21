package com.cng.android.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.cng.android.CNG;
import com.cng.android.R;
import com.cng.android.activity.MainActivity;
import com.cng.android.data.Node;
import com.cng.android.db.DBService;
import com.cng.android.util.BluetoothDiscover;
import com.cng.android.util.FixedSizeQueue;
import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.IBluetoothListener;
import com.cng.android.util.IMessageHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import static com.cng.android.CNG.D;

public class StateMonitorService extends IntentService implements IBluetoothListener, IMessageHandler {
    public static final UUID SPP_UUID = UUID.fromString ("00001101-0000-1000-8000-00805F9B34FB");

    private static final String TAG = StateMonitorService.class.getSimpleName ();
    private static final int STATE_START_MAIN_ACTIVITY = 0;

    private IBinder binder;
    private Handler handler;
    private BluetoothDevice device;
    private BluetoothDiscover discover;
    private BluetoothSocket socket;
    private boolean connected;

    private static int COUNT = 0;

    private FixedSizeQueue<Node> queue;
//    private NotificationManager manager;

    public StateMonitorService () {
        super ("StateMonitorService");
        Log.d (TAG, String.valueOf (COUNT++));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate () {
        super.onCreate ();
        Log.d (TAG, "++++++++++++ Monitor Service Create ++++++++++++");
        Log.d (TAG, this.toString ());

        binder = new MonitorServiceBinder (this);
        handler = new HandlerDelegate (this);
        discover = new BluetoothDiscover (this, this);

        Intent target = new Intent (this, MainActivity.class);
        target.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pi = PendingIntent.getActivity (this, 2, target, 0);
        Notification notification = new Notification.Builder (this)
                .setContentTitle (getString (R.string.app_name))
                .setSmallIcon (R.mipmap.ic_launcher)
                .setContentIntent (pi)
                .build ();
        startForeground (1, notification);
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.d (TAG, "++++++++++++ Monitor Service on start command ++++++++++++");
        return super.onStartCommand (intent, START_STICKY, startId);
    }

    @Override
    public IBinder onBind (Intent intent) {
        return binder;
    }

    @Override
    protected void onHandleIntent (Intent intent) {
        if (D) {
            Log.d (TAG, "handle intent");
        }

        if (queue == null) {
            if (D) {
                Log.d (TAG, "trying to init database.");
                Log.d (TAG, "trying to init fixed queue.");
            }
            DBService.init (this);

            int capacity = DBService.getQueueCapacity ();
            queue = new FixedSizeQueue<> (capacity);
            if (D)
                Log.d (TAG, "queue init as FixedQueue<" + capacity + ">");
        }

        if (D)
            Log.d (TAG, "finding bluetooth device...");
        if (device == null) {
            if (D)
                Log.d (TAG, "bluetooth is null, try to find it in the intent.");
            device = intent.getParcelableExtra ("device");
            if (D) {
                if (device == null) {
                    Log.d (TAG, "there is no device in the intent");
                } else {
                    Log.d (TAG, "find the device in the intent");
                }
            }
        }

        if (device == null) { // 首次进入服务
            if (D)
                Log.d (TAG, "first time in the service, discovery it");

            String savedMac = DBService.getSavedBTMac ();
            if (savedMac == null) {
                if (D)
                    Log.d (TAG, "the device's mac is not saved. active the main activity to config it.");

                handler.sendEmptyMessage (STATE_START_MAIN_ACTIVITY);
                return;
            } else {
                if (D)
                    Log.d (TAG, "saved mac is: " + savedMac + ", starting to discover it...");

                discover.discovery ();
                synchronized (SPP_UUID) {
                    try {
                        if (D)
                            Log.d (TAG, "Waiting for connect to bluetooth device ...");
                        SPP_UUID.wait ();
                    } catch (InterruptedException e) {
                        e.printStackTrace ();
                    }
                }
            }
        }

        if (D)
            Log.d (TAG, "now, we got the bluetooth device, connect to it and listen data");
        try {
            socket = device.createRfcommSocketToServiceRecord (SPP_UUID);
            socket.connect ();
            synchronized (this) {
                connected = true;
            }
            InputStream in = socket.getInputStream ();
            BufferedReader reader = new BufferedReader (new InputStreamReader (in));
            while (socket.isConnected ()) {
                String line = reader.readLine ();
                Log.d (TAG, line);
            }
        } catch (IOException ex) {
            Log.w (TAG, ex.getMessage (), ex);
        } finally {
            if (socket != null) try {
                socket.close ();
            } catch (IOException ex) {
                Log.w (TAG, ex.getMessage (), ex);
            }
        }
    }

    @Override
    public void onDestroy () {
        Intent intent = new Intent (this, getClass ());
        startService (intent);
    }

    @Override
    public void onDeviceFound (BluetoothDevice device) {
        discover.cancel ();
        this.device = device;
        if (D)
            Log.d (TAG, "find BT device = " + device);
        Log.d (TAG, "Current Thread = " + Thread.currentThread ().getName ());
        synchronized (SPP_UUID) {
            SPP_UUID.notifyAll ();
        }
/*
        Intent intent = new Intent (this, this.getClass ());
        startService (intent);
*/
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case STATE_START_MAIN_ACTIVITY :
                Intent intent = new Intent (this, MainActivity.class);
                intent.putExtra (CNG.ACTION_NOT_BIND, "true");
                intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity (intent);
                break;
        }
    }

    Queue<Node> copyData () {
        synchronized (this) {
            return new LinkedList<> (queue);
        }
    }

    synchronized boolean isConnected () {
        return connected;
    }
}