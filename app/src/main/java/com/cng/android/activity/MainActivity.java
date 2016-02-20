package com.cng.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    private static final String ACTION_MATCH_BT_DEVICE = "action.match.bluetooth.device";

    private static final int REQ_CODE = 1;

    private BluetoothDiscover discover;
    private BluetoothDevice device;
    private ProgressDialog dialog;
    private Handler handler;
    private String savedMac;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        handler = new HandlerDelegate (this);

        dialog = new ProgressDialog (this);
        dialog.setTitle (R.string.title_processing);
    }

    @Override
    protected void onResume () {
        super.onResume ();
        if (device == null) {
            dialog.show ();
            CNG.runInNonUIThread (this);
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
        super.onCreateContextMenu (menu, v, menuInfo);
        getMenuInflater ().inflate (R.menu.main, menu);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item) {
        processMenuItem (item);
        return super.onContextItemSelected (item);
    }

    @Override
    public void run () {
        savedMac = DBService.getSavedBTMac ();
        if (savedMac == null) {
            handler.sendEmptyMessage (REQ_CODE);
        } else {
            if (discover == null)
                discover = new BluetoothDiscover (this, this);

            discover.discovery ();
        }
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case REQ_CODE :
                showFindActivity ();
                break;
        }
    }

    @Override
    public void onDeviceFound (BluetoothDevice device) {
        if (device.getAddress ().equals (savedMac)) {
            this.device = device;
            discover.cancel ();
            discover = null;
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
//        super.onActivityResult (requestCode, resultCode, data);
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
        intent.setAction (ACTION_MATCH_BT_DEVICE);
        startActivityForResult (intent, REQ_CODE);
    }

    private void processMenuItem (MenuItem item) {
        switch (item.getItemId ()) {
            case R.id.menu_choose_device :
                showFindActivity ();
                break;
        }
    }
}