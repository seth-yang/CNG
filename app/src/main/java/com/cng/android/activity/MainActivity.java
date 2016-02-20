package com.cng.android.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cng.android.R;

import java.util.UUID;

/**
 * Created by game on 2016/2/20
 */
public class MainActivity extends Activity {
    public static final UUID SPP_UUID = UUID.fromString ("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothDevice device;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        if (savedInstanceState != null) {
            device = savedInstanceState.getParcelable ("device");
        } else {

        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        return super.onCreateOptionsMenu (menu);
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu (menu, v, menuInfo);
        getMenuInflater ().inflate (R.menu.main, menu);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item) {
        switch (item.getItemId ()) {
            case R.id.menu_choose_device :
                break;
        }
        return super.onContextItemSelected (item);
    }
}