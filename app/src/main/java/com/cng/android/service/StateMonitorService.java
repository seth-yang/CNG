package com.cng.android.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.cng.android.CNG;
import com.cng.android.R;
import com.cng.android.activity.MainActivity;
import com.cng.android.concurrent.BluetoothWriter;
import com.cng.android.data.Transformer;
import com.cng.android.db.DBService;
import com.cng.android.util.BluetoothDiscover;
import com.cng.android.util.FixedSizeQueue;
import com.cng.android.util.IBluetoothListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import static com.cng.android.CNG.D;

public class StateMonitorService extends IntentService implements IBluetoothListener {
//    public static final UUID SPP_UUID = UUID.fromString ("00001101-0000-1000-8000-00805F9B34FB");
    public static final UUID SPP_UUID = UUID.fromString ("a60f35f0-b93a-11de-8a39-08002009c666");

    private static final String TAG = StateMonitorService.class.getSimpleName ();
    private static final int STATE_START_MAIN_ACTIVITY = 0;

    private IBinder binder;
    private BluetoothDevice device;
    private BluetoothDiscover discover;
    private BluetoothSocket socket;
    private boolean connected;
    private String savedMac;

    private static int COUNT = 0;

    private FixedSizeQueue<Transformer> queue;

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
    public IBinder onBind (Intent intent) {
        return binder;
    }

    @Override
    protected void onHandleIntent (Intent intent) {
        if (D) {
            Log.d (TAG, "handle intent");
        }

        if (queue == null) {
            initFixedQueue ();
        }

        if (D)
            Log.d (TAG, "first time in the service, discovery it");

        savedMac = DBService.getSavedBTMac ();
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

        connect ();
    }

    @Override
    public void onDestroy () {
        super.onDestroy ();
        Intent intent = new Intent (this, getClass ());
        startService (intent);
    }

    @Override
    public void onDeviceFound (BluetoothDevice device) {
        this.device = device;
        if (D)
            Log.d (TAG, "find BT device = " + device);
        Log.d (TAG, "Current Thread = " + Thread.currentThread ().getName ());
        if (device.getAddress ().equals (savedMac)) synchronized (SPP_UUID) {
            SPP_UUID.notifyAll ();
        }
    }

    Queue<Transformer> copyData () {
        synchronized (SPP_UUID) {
            return new LinkedList<> (queue);
        }
    }

    synchronized boolean isConnected () {
        return connected;
    }

    private boolean connectToDevice () throws IOException {
        int retry = 3;
        while (retry > 0) {
            if (D)
                Log.d (TAG, "Trying to connect to BT device ...");

            try {
                Method m = device.getClass ().getDeclaredMethod ("createRfcommSocket", int.class);
                socket = (BluetoothSocket) m.invoke (device, 1);
            } catch (Exception e) {
                e.printStackTrace ();
            }
//            device.cr
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
                BufferedReader reader = new BufferedReader (new InputStreamReader (in));
                CNG.runInNonUIThread (writer);
                Gson g = new GsonBuilder ().create ();
                while (socket.isConnected ()) {
                    String line = reader.readLine ();
                    try {
                        Transformer trans = g.fromJson (line.trim (), Transformer.class);
                        synchronized (SPP_UUID) {
                            queue.add (trans);
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
        }
    }

    private void initFixedQueue () {
        if (D) {
            Log.d (TAG, "trying to init database.");
            Log.d (TAG, "trying to init fixed queue.");
        }
        DBService.init (this);

        int capacity = 60; //DBService.getQueueCapacity ();
        queue = new FixedSizeQueue<> (capacity);
        if (D)
            Log.d (TAG, "queue init as FixedQueue<" + capacity + ">");
    }
}