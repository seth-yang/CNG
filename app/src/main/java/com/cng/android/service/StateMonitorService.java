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
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.cng.android.CNG;
import com.cng.android.R;
import com.cng.android.activity.DashboardActivity;
import com.cng.android.arduino.IArduino;
import com.cng.android.concurrent.BluetoothWriter;
import com.cng.android.concurrent.DataSaver;
import com.cng.android.data.ArduinoCommand;
import com.cng.android.data.CardRecord;
import com.cng.android.data.EventType;
import com.cng.android.data.ExchangeData;
import com.cng.android.data.SetupItem;
import com.cng.android.data.EnvData;
import com.cng.android.db.DBService;
import com.cng.android.receiver.BroadcastReceiverDelegate;
import com.cng.android.receiver.IBroadcastHandler;
import com.cng.android.util.BluetoothDiscover;
import com.cng.android.util.IBluetoothListener;
import com.cng.android.util.Keys;
import com.cng.android.util.gson.EventTypeTranslator;
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
        implements IBluetoothListener, IBroadcastHandler, IArduino {
//    public static final UUID SPP_UUID = UUID.fromString ("00001101-0000-1000-8000-00805F9B34FB");
//    public static final UUID SPP_UUID = UUID.fromString ("a60f35f0-b93a-11de-8a39-08002009c666");

    private static final String TAG = StateMonitorService.class.getSimpleName ();
    private static final int STATE_START_MAIN_ACTIVITY = 0;
    private static final Object locker = new byte [0];

    private static boolean running = false;

    private IBinder binder;
    private DataSaver saver;
    private BluetoothWriter writer;
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
        IntentFilter filter = new IntentFilter (ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction (Keys.Actions.SET_ARDUINO);
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
            SetupItem item = DBService.SetupItem.getItem (Keys.DataNames.SAVED_MAC);
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

                if (D)
                    Log.d (TAG, "The bluetooth device connected, listening to it.");
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
        running = false;
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
        String action = intent.getAction ();
        switch (action) {
            case ConnectivityManager.CONNECTIVITY_ACTION :
                onNetworkStateChanged ();
                break;
            case Keys.Actions.SET_ARDUINO :
                onWriteToArduino (intent);
                break;
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
                Gson g = new GsonBuilder ().registerTypeAdapter (EventType.class, new EventTypeTranslator ()).create ();
                Intent intent = new Intent (Keys.Actions.ON_RECEIVE_IR_COMMAND);
                while (socket.isConnected ()) {
                    String line = reader.readLine ();
                    if (D)
                        Log.d (TAG, "Got a message: " + line);
                    try {
                        ExchangeData trans = g.fromJson (line.trim (), ExchangeData.class);
                        if (D)
                            System.out.println (trans);
                        if (trans.ir != null) {
                            intent.putExtra (Keys.IR_COMMAND, trans.ir);
                            sendBroadcast (intent);
                        }
                        if (trans.event != null) {
                            if (trans.event.type == EventType.CardAccessed) {
                                CardRecord record = CardRecord.parse (trans.event.data);
                                if (authenticate (record)) {
                                    // 卡认证通过，通知 arduino 开门
                                    if (D)
                                        Log.d (Keys.TAG_ARDUINO, "authenticate card success, open the door");
                                    write (ArduinoCommand.CMD_OPEN_DOOR);
                                }
                            }
                        }
                        saver.write (trans);
                        synchronized (locker) {
                            data = trans.data;
                        }
                    } catch (Exception ex) {
//                        Log.w (TAG, ex.getMessage (), ex);
                        // ignore
                    }
                }
            }
        } catch (IOException ex) {
            Log.w (TAG, ex.getMessage (), ex);
        } finally {
            synchronized (this) {
                if (D)
                    Log.d (TAG, "set the flag::connected to false");
                connected = false;
            }

            if (socket != null) try {
                if (D)
                    Log.d (TAG, "close the socket.");
                socket.close ();
            } catch (IOException ex) {
                Log.w (TAG, ex.getMessage (), ex);
            }

            if (writer != null) {
                BluetoothWriter temp = this.writer;
                this.writer = null;
                temp.shutdown ();
            }

            if (saver != null) {
                DataSaver temp = saver;
                this.saver = null;
                temp.cancel (true);
            }
        }
    }

    private void onNetworkStateChanged () {
        if (CNG.isNetworkConnected (this)) {
            if (D)
                Log.d (TAG, "The network is active, turn the saver into ethernet mode.");
            if (saver != null && saver.isPaused ()) {
                saver.flushDatabase ();
                saver.setMode (DataSaver.Mode.Ethernet);
            }
        } else {
            if (D)
                Log.d (TAG, "The network is down, turn the saver into local mode.");
            if (saver != null && !saver.isPaused ()) {
                saver.setMode (DataSaver.Mode.Local);
            }
        }
    }

    private void onWriteToArduino (Intent intent) {
        if (D)
            Log.d (TAG, "receive an arduino command.");
        if (writer != null) {
            byte[] command = intent.getByteArrayExtra ("command");
            if (command != null) {
                writer.write (command);
            }
        }
    }

    /**
     * 认证卡信息.
     *
     * <h3>认证步骤</h3>
     * <ul>
     *     <li>1. 卡信息是否为空</li>
     *     <li>2. 验证卡片数据的版本号</li>
     *     <li>3. 若卡片的 <code>admin</code> 标识为真, 直接返回真</li>
     *     <li>4. 卡号是否和本地数据匹配</li>
     *     <li>5. 验证有效期</li>
     * </ul>
     * @param card 卡信息
     * @return 指定的卡片信息是否具备开门的权限
     */
    private boolean authenticate (CardRecord card) {
        // step 1.
        if (card == null) return false;

        // step 2. check the version
        int major_version = DBService.SetupItem.getIntValue (Keys.DataNames.CARD_MAJOR_VERSION, 0);
        int minor_version = DBService.SetupItem.getIntValue (Keys.DataNames.CARD_MINOR_VERSION, 0);
        if (card.minorVersion > minor_version || card.majorVersion > major_version)
            return false;

        // step 3. check if the card is admin card.
        return card.admin || DBService.Card.isCardValid (card.cardNo) && card.expire.getTime () >= System.currentTimeMillis ();
    }

    @Override
    public void write (byte[] data) {
        if (writer != null) {
            writer.write (data);
        }
    }
}