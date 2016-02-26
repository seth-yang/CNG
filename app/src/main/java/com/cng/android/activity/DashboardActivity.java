package com.cng.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cng.android.CNG;
import com.cng.android.R;
import com.cng.android.data.SetupItem;
import com.cng.android.data.Transformer;
import com.cng.android.db.DBService;
import com.cng.android.service.BluetoothDataProvider;
import com.cng.android.service.StateMonitorService;
import com.cng.android.ui.DashboardView;
import com.cng.android.util.Keys;

import java.text.DecimalFormat;

import static com.cng.android.CNG.D;

public class DashboardActivity extends Activity implements Runnable {
    private static final String TAG = DashboardActivity.class.getSimpleName ();

    private DashboardView temperature, humidity;

    private boolean running = false, bound = false;

    private BluetoothDataProvider provider = new BluetoothDataProvider ();

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_dashboard);
        if (D)
            Log.d (TAG, "+++++++++++++++++++ [" + TAG + "] OnCreate ++++++++++++++++++++");
        guiSetup ();
    }

    @Override
    protected void onResume () {
        super.onResume ();

        if (!bound) {
            Intent intent = new Intent (this, StateMonitorService.class);
            getApplicationContext ().bindService (intent, provider, BIND_AUTO_CREATE);
            bound = true;
        }
        running = true;
        CNG.runInNonUIThread (this);
    }

    @Override
    protected void onPause () {
        if (bound) {
            getApplicationContext ().unbindService (provider);
            bound = false;
        }

        running = false;
        super.onPause ();
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
        SetupItem item = DBService.getSetupItem (Keys.DASHBOARD_INTERVAL);
        long interval;
        if (item != null) {
            interval = 1000 * (Integer) item.getValue ();
        } else {
            interval = 1000;
        }

        if (D)
            Log.d (TAG, "The dashboard interval = " + interval);
        while (running) {
            if (provider.isConnected ()) {
                Transformer data = provider.getData ();
                if (data != null) {
                    if (data.humidity != null)
                        humidity.setValue (data.humidity);
                    if (data.temperature != null)
                        temperature.setValue (data.temperature);
                }
            }

            try {
                Thread.sleep (interval);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
        if (D)
            Log.d (TAG, "The local thread stopped.");
    }

    private void guiSetup () {
        temperature = (DashboardView) findViewById (R.id.temperature);
        temperature.setTitle (getString (R.string.title_temperature))
                .setMin (-25.0).setMax (125.0)
                .setFormatter (new DecimalFormat ("#0.00"));

        humidity = (DashboardView) findViewById (R.id.humidity);
        humidity.setTitle (getString (R.string.title_humidity));
    }

    private void showFindActivity () {
        Intent intent = new Intent (this, ShowBTDeviceActivity.class);
        startActivity (intent);
    }

    private void showSettings () {
        Intent intent = new Intent (this, SettingsActivity.class);
        startActivity (intent);
    }

    private void processMenuItem (MenuItem item) {
        switch (item.getItemId ()) {
            case R.id.menu_choose_device :
                showFindActivity ();
                break;
            case R.id.menu_setting :
                showSettings ();
                break;
        }
    }
}