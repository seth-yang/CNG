package com.cng.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.cng.android.CNG;
import com.cng.android.CNGException;
import com.cng.android.R;
import com.cng.android.data.RegisterResult;
import com.cng.android.data.Result;
import com.cng.android.data.SetupItem;
import com.cng.android.db.DBService;
import com.cng.android.service.StateMonitorService;
import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.HttpUtil;
import com.cng.android.util.IMessageHandler;
import com.cng.android.util.Keys;
import com.cng.android.util.NetworkUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;

import static com.cng.android.CNG.D;

public class SplashActivity extends Activity implements IMessageHandler, Runnable {
    private static final Object locker                 = new byte[0];
    private static final String TAG                    = "SplashActivity";
    private static final String MESSAGE_ID             = "com.cng.android.SplashActivity.MESSAGE_ID";
    private static final int    SHOW_MAIN_ACTIVITY     = 0;
    private static final int    SHOW_BLUETOOTH_SETTING = 1;
    private static final int    SET_LABEL              = 2;
    private static final int    START_SERVICE          = 3;
    private static final int    SHOW_ERROR_DIALOG      = 4;

    private Handler handler;
    private TextView txtTitle;

    private String label;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_splash);
        handler = new HandlerDelegate (this);

        txtTitle = (TextView) findViewById (R.id.title);
        if (D)
            Log.d (TAG, "checking for network ");
        CNG.checkAndSetupNetwork (this);
        if (D)
            Log.d (TAG, "on create");
    }

    @Override
    protected void onResume () {
        super.onResume ();
        if (D)
            Log.d (TAG, "on resume");
        if (label != null) {
            if (D)
                Log.d (TAG, "set title to : " + label);
            txtTitle.setText (label);
        }

        CNG.runInNonUIThread (this);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        synchronized (locker) {
            locker.notifyAll ();
        }
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case SHOW_MAIN_ACTIVITY :
                goActivity (MainActivity.class, true);
                break;
            case SHOW_BLUETOOTH_SETTING :
                Intent intent = new Intent (this, ShowBTDeviceActivity.class);
                startActivityForResult (intent, SHOW_BLUETOOTH_SETTING);
                break;
            case SET_LABEL :
                txtTitle.setText (label);
                break;
            case START_SERVICE :
                startService ();
                break;
            case SHOW_ERROR_DIALOG :
                Bundle bundle = message.getData ();
                int messageId = bundle.getInt (MESSAGE_ID);
                showErrorDialog (messageId);
                break;
        }
    }

    @Override
    public void run () {
        Message message = new Message ();
        Bundle bundle = new Bundle (1);
        message.setData (bundle);

        try {
            if (D)
                Log.d (TAG, "init the database.");
            DBService.init (this);

            // step 0: check app version and import the data from assets if necessary.
            checkAndInitSettings ();

            // step 1: check app uuid and fetch it from the cloud server if necessary.
            checkAndFetchUUID ();

            // step 2: check update
            checkUpdate ();

            // step 3: check matched bluetooth device and matches it if necessary.
            checkAndMatchesDevice ();

            if (DBService.exist (Keys.SAVED_MAC)) {
                // step 4: start the monitor service
                if (D)
                    Log.d (TAG, "Preparing to start background service to connect to the bluetooth device");
                handler.sendEmptyMessage (START_SERVICE);
                Thread.sleep (1000); // waiting for the service start.

                // step 5: now, we can go to the main activity
                if (D)
                    Log.d (TAG, "Preparing to open main activity.");
                handler.sendEmptyMessage (SHOW_MAIN_ACTIVITY);
            } else {
                message.what = SHOW_ERROR_DIALOG;
                bundle.putInt (MESSAGE_ID, R.string.prompt_device_not_found);
                handler.sendMessage (message);
            }
        } catch (Exception ex) {
            Log.e (TAG, ex.getMessage (), ex);
            message.what = SHOW_ERROR_DIALOG;
            if (ex instanceof CNGException) {
                bundle.putInt (MESSAGE_ID, ((CNGException) ex).getResourceId ());
            } else if (ex instanceof IOException) {
                bundle.putInt (MESSAGE_ID, R.string.err_network_error);
            } else {
                bundle.putInt (MESSAGE_ID, R.string.err_internal);
            }
            handler.sendMessage (message);
        }
    }

    private void goActivity (Class<? extends Activity> type, boolean finished) {
        Intent intent = new Intent (this, type);
        startActivity (intent);
        if (finished)
            finish ();
    }

    private void startService () {
        Intent intent = new Intent (this, StateMonitorService.class);
        startService (intent);
    }

    private void checkAndInitSettings () {
        if (D)
            Log.d (TAG, "checking for app version ...");
        if (!DBService.exist (Keys.APP_VERSION)) {
            if (D)
                Log.d (TAG, "The app version is not set. set it from assets.");
            label = getString (R.string.title_first_run);
            handler.sendEmptyMessage (SET_LABEL);

            InputStream in = null;
            try {
                in = getAssets ().open ("default-config.sql");
                DBService.execute (in);
            } catch (IOException ex) {
                Log.w (TAG, ex.getMessage (), ex);
                throw new CNGException (ex.getMessage (), R.string.err_init_db_fail);
            } finally {
                if (in != null) try {
                    in.close ();
                } catch (IOException ex) {
                    Log.w (TAG, ex.getMessage (), ex);
                }
            }
        } else if (D) {
            Log.d (TAG, "The app version is set.");
        }
    }

    private void checkAndFetchUUID () throws IOException {
        if (D)
            Log.d (TAG, "checking for app uuid");
        if (!DBService.exist (Keys.APP_UUID)) {
            if (D)
                Log.d (TAG, "The app uuid is not set. fetch it from cloud server");

/*
            label = getString (R.string.title_fetch_from_cloud);
            handler.sendEmptyMessage (SET_LABEL);
*/

            SetupItem item = DBService.getSetupItem (Keys.CLOUD_URL);
            if (item != null) {
                String url = item.getValue () + "register";
                if (D) {
                    Log.d (TAG, "The cloud url is: " + item.getValue ());
                }
                String mac = getMac ();
                String request = "{\"mac\":\"" + mac + "\"}";
                RegisterResult result = HttpUtil.post (url, request, RegisterResult.class);
                if (result == null || result.getState () != Result.State.ok) {
                    if (D)
                        Log.d (TAG, "call cloud server fail");
                    throw new CNGException ("fetch uuid from cloud server fail.", R.string.err_register_fail);
                } else {
                    item = new SetupItem (false, SetupItem.Type.Text);
                    item.setName (Keys.APP_UUID);
                    item.setValue (result.getUserData ().getId ());
                    item.setVisible (true);
                    item.setChinese (getString (R.string.label_app_uuid));
                    DBService.saveSetupItem (item);
                    if (D)
                        Log.d (TAG, "save the app uuid as: " + result.getUserData ().getId ());
                }
            }
        } else if (D) {
            Log.d (TAG, "This device has registered.");
        }
    }

    private void checkAndMatchesDevice () throws InterruptedException {
        if (D)
            Log.d (TAG, "checking for matched bluetooth device.");
        if (!DBService.exist (Keys.SAVED_MAC)) {
            if (D)
                Log.d (TAG, "The bluetooth device is not matched. open the setting activity");
            label = getString (R.string.title_connecting);
            handler.sendEmptyMessage (SHOW_BLUETOOTH_SETTING);

            if (D)
                Log.d (TAG, "Waiting for user select the device.");
            synchronized (locker) {
                locker.wait ();
            }
            if (D)
                Log.d (TAG, "return from ShowBluetoothActivity.");
        } else if (D) {
            Log.d (TAG, "The bluetooth device is matched, skip this step.");
        }
    }

    private void checkUpdate () {
        if (D)
            Log.d (TAG, "check for application update");
        // todo: update apk and|or data from the cloud server
        if (D)
            Log.d (TAG, "check update done.");
    }

    private String getMac () throws SocketException {
        List<NetworkInterface> list = NetworkUtil.getNetworkInterfaces ();
        byte[] mac;
        if (list != null && !list.isEmpty ()) {
            NetworkInterface address = list.get (0);
            if (D)
                Log.d (TAG, "get the network interface: " + address);
            mac = address.getHardwareAddress ();
        } else {
            if (D)
                Log.d (TAG, "Can't get network interface, generate a random one.");
            mac = new byte[6];
            for (int i = 0; i < 6; i ++) {
                mac [i] = (byte) (Math.random () * 255);
            }
        }
        StringBuilder builder = new StringBuilder ();
        for (byte b : mac) {
            if (builder.length () > 0)
                builder.append (':');
            builder.append (String.format ("%02X", b));
        }
        if (D)
            Log.d (TAG, "The network interface mac is: " + builder);
        return builder.toString ();
    }

    private void showErrorDialog (int messageId) {
        new AlertDialog.Builder (this).setPositiveButton (getString (R.string.btn_ok), new DialogInterface.OnClickListener () {
            @Override
            public void onClick (DialogInterface dialogInterface, int i) {
                finish ();
            }
        }).setTitle (getString (R.string.title_error))
            .setMessage (getString (messageId)).show ();
    }
}