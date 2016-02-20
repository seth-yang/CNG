package com.cng.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cng.android.CNG;
import com.cng.android.adapter.BluetoothDeviceListAdapter;
import com.cng.android.db.DBService;
import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.IMessageHandler;
import com.cng.android.R;

import java.util.HashSet;
import java.util.Set;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ShowBTDeviceActivity extends Activity implements Runnable, IMessageHandler, ListView.OnItemClickListener {
    private static final String TAG = ShowBTDeviceActivity.class.getSimpleName ();
    private static final boolean D = true;

    private static final int SAVE_MAC = 0;

    private BluetoothReceiver receiver;
    private BluetoothAdapter ba;
    private Handler handler;
    private BluetoothDeviceListAdapter adapter;
    private Message localMessage = new Message ();
    private ProgressDialog dialog;

    private Set<String> keys = new HashSet<> ();

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_show_bt_devices);

        adapter = new BluetoothDeviceListAdapter (this);
        ListView listView = (ListView) findViewById (R.id.listView);
        listView.setAdapter (adapter);
        listView.setOnItemClickListener (this);

        handler = new HandlerDelegate (this);
        dialog = new ProgressDialog (this);
        dialog.setTitle (R.string.title_processing);
    }

    @Override
    protected void onResume () {
        super.onResume ();

        IntentFilter filter = new IntentFilter (BluetoothDevice.ACTION_FOUND);
        receiver = new BluetoothReceiver ();
        registerReceiver (receiver, filter);

        ba = BluetoothAdapter.getDefaultAdapter ();
        if (!ba.isEnabled ()) {
            Log.d (TAG, "blue tooth is not enable, enable it.");
            ba.enable ();
            Log.d (TAG, "blue tooth is enabled.");
        }
        ba.startDiscovery ();
    }

    @Override
    protected void onPause () {
        super.onPause ();
        unregisterReceiver (receiver);
        if (ba != null) {
            ba.cancelDiscovery ();
        }
    }

    @Override
    public void run () {
        Bundle bundle = localMessage.getData ();
        switch (localMessage.what) {
            case SAVE_MAC :
                String mac = bundle.getString ("mac");
                DBService.saveOrUpdateBTMac (mac);
                handler.sendEmptyMessage (SAVE_MAC);
                break;
        }
        localMessage.setData (null);
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case SAVE_MAC :
                dialog.dismiss ();
                finish ();
                break;
        }
    }

    @Override
    public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
        ba.cancelDiscovery ();
        BluetoothDevice device = adapter.getItem (position);
        Bundle bundle = new Bundle (1);
        bundle.putString ("mac", device.getAddress ());
        localMessage.what = SAVE_MAC;
        localMessage.setData (bundle);
        CNG.runInNonUIThread (this);
    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction ();
            if (BluetoothDevice.ACTION_FOUND.equals (action)) {
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                String mac = device.getAddress ();
                if (D) {
                    Log.d (TAG, "receive a device: " + device + ", mac = " + mac);
                }
                if (!keys.contains (mac)) {
                    keys.add (mac);
                    adapter.addItem (device);
                    if (D)
                        Log.d (TAG, "device " + device + " not in list, add it.");
                } else if (D) {
                    Log.d (TAG, "device " + device + " is already in list, ignore it.");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals (action)) {
                if (D)
                    Log.d (TAG, "finish discovery");
            }
        }
    }
}