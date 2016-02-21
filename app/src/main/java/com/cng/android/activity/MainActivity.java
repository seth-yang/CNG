package com.cng.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cng.android.CNG;
import com.cng.android.R;
import com.cng.android.db.DBService;
import com.cng.android.util.BluetoothDiscover;
import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.IBluetoothListener;
import com.cng.android.util.IMessageHandler;

import java.util.UUID;

/**
 * Created by game on 2016/2/20
 */
public class MainActivity extends Activity implements Runnable, IMessageHandler, IBluetoothListener {
    public static final UUID SPP_UUID = UUID.fromString ("00001101-0000-1000-8000-00805F9B34FB");

    public static final String TAG = MainActivity.class.getSimpleName ();

    private static final int REQ_CODE                    = 1;
    private static final int SET_DIALOG_TITLE_CONNECTING = 2;

    private static final int LOCAL_STATE_DEFAULT         = 0;
    private static final int LOCAL_STATE_CONNECTING      = 1;

    private BluetoothDiscover discover;
    private BluetoothDevice device;
    private ProgressDialog dialog;
    private Handler handler;
    private String savedMac;
    private Message local = new Message ();

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        handler = new HandlerDelegate (this);

        dialog = new ProgressDialog (this);
        dialog.setTitle (R.string.title_processing);
        local.what = LOCAL_STATE_DEFAULT;
    }

    @Override
    protected void onResume () {
        super.onResume ();
        if (device == null) {
            dialog.show ();
            CNG.runInNonUIThread (this);
        } else {
            dialog.setTitle (R.string.title_connecting);
            dialog.show ();
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
        switch (local.what) {
            case LOCAL_STATE_CONNECTING :
                connectToDevice ();
                break;
            default :
                savedMac = DBService.getSavedBTMac ();
                if (savedMac == null) {
                    handler.sendEmptyMessage (REQ_CODE);
                } else {
                    if (discover == null)
                        discover = new BluetoothDiscover (this, this);

                    discover.discovery ();
                    if (CNG.D)
                        Log.d (TAG, "discovering bt devices...");
                    handler.sendEmptyMessage (SET_DIALOG_TITLE_CONNECTING);
                }
                break;
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
        }
    }

    @Override
    public void onDeviceFound (BluetoothDevice device) {
        if (CNG.D)
            Log.d (TAG, "a bluetooth device found, mac = " + device.getAddress ());
        if (device.getAddress ().equals (savedMac)) {
            if (CNG.D)
                Log.d (TAG, "The mac of bt device is match to saved one.");
            this.device = device;
            discover.cancel ();
            discover = null;
            if (CNG.D)
                Log.d (TAG, "BT discover released.");
            handler.sendEmptyMessage (SET_DIALOG_TITLE_CONNECTING);

            local.what = LOCAL_STATE_CONNECTING;
            CNG.runInNonUIThread (this);
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE) {
            if (resultCode == CNG.RESULT_CODE_OK) {
                Bundle bundle = data.getExtras ();
                device = bundle.getParcelable ("device");
            } else {
                Toast.makeText (this, getString (R.string.prompt_device_not_found), Toast.LENGTH_SHORT).show ();
            }
        }
    }

    private void showFindActivity () {
        Intent intent = new Intent (this, ShowBTDeviceActivity.class);
        intent.putExtra ("request_action", CNG.ACTION_MATCH_BT_DEVICE);
        startActivityForResult (intent, REQ_CODE);
    }

    private void processMenuItem (MenuItem item) {
        switch (item.getItemId ()) {
            case R.id.menu_choose_device :
                showFindActivity ();
                break;
        }
    }

    private void connectToDevice () {
//        dialog.setTitle (R.string.title_connecting);
    }
}