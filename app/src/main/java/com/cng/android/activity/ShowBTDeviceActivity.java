package com.cng.android.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
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
import com.cng.android.util.BluetoothDiscover;
import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.IBluetoothListener;
import com.cng.android.util.IMessageHandler;
import com.cng.android.R;

import static com.cng.android.CNG.D;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ShowBTDeviceActivity extends Activity implements Runnable, IMessageHandler, ListView.OnItemClickListener, IBluetoothListener {
    private static final String TAG = ShowBTDeviceActivity.class.getSimpleName ();

    private static final int LOCAL_STATE_SAVE_MAC = 0;
    private static final int RETURN   = 1;

    private Handler handler;
    private BluetoothDeviceListAdapter adapter;
    private Message localMessage = new Message ();
    private BluetoothDiscover discover;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_show_bt_devices);

        adapter = new BluetoothDeviceListAdapter (this);
        ListView listView = (ListView) findViewById (R.id.listView);
        listView.setAdapter (adapter);
        listView.setOnItemClickListener (this);

        handler = new HandlerDelegate (this);
        discover = new BluetoothDiscover (this, this);
    }

    @Override
    protected void onResume () {
        super.onResume ();

        if (!discover.isDiscovering ())
            discover.discovery ();
    }

    @Override
    protected void onPause () {
        if (discover.isDiscovering ())
            discover.cancel ();
        super.onPause ();
    }

    @Override
    public void onBackPressed () {
        setResult (CNG.RESULT_CODE_CANCEL);
        super.onBackPressed ();
    }

    @Override
    public void run () {
        Bundle bundle = localMessage.getData ();
        switch (localMessage.what) {
            case LOCAL_STATE_SAVE_MAC:
                BluetoothDevice device = bundle.getParcelable ("device");
                if (device != null) {
                    String mac = device.getAddress ();
                    DBService.saveOrUpdateBTMac (mac);
                    handler.sendEmptyMessage (RETURN);
                }
                break;
        }
        localMessage.setData (null);
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case RETURN :
                finish ();
                break;
        }
    }

    @Override
    public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
        discover.cancel ();
        BluetoothDevice device = adapter.getItem (position);
        Bundle bundle = new Bundle (1);
        bundle.putParcelable ("device", device);
        localMessage.what = LOCAL_STATE_SAVE_MAC;
        localMessage.setData (bundle);
        CNG.runInNonUIThread (this);
    }

    @Override
    public void onDeviceFound (BluetoothDevice device) {
        if (D)
            Log.d (TAG, "a bluetooth device found. mac = " + device.getAddress ());
        adapter.addItem (device);
    }
}