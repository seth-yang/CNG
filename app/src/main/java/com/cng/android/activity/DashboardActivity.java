package com.cng.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.cng.android.CNG;
import com.cng.android.R;
import com.cng.android.concurrent.ITimeoutListener;
import com.cng.android.concurrent.Timer;
import com.cng.android.data.SetupItem;
import com.cng.android.data.EnvData;
import com.cng.android.db.DBService;
import com.cng.android.service.BluetoothDataProvider;
import com.cng.android.service.StateMonitorService;
import com.cng.android.ui.DashboardView;
import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.IMessageHandler;
import com.cng.android.util.Keys;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import static com.cng.android.CNG.D;

public class DashboardActivity extends Activity
        implements Runnable, View.OnClickListener, ITimeoutListener, IMessageHandler {
    private static final String TAG = DashboardActivity.class.getSimpleName ();
    private static final int HIDE_ACTION_BAR = 0;

    private DashboardView temperature, humidity;
    private View actionBar;
    private Timer timer;

    private boolean running = false, bound = false;

    private BluetoothDataProvider provider = new BluetoothDataProvider ();
    private Handler handler;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_dashboard);
        if (D)
            Log.d (TAG, "+++++++++++++++++++ [" + TAG + "] OnCreate ++++++++++++++++++++");
        guiSetup ();
        handler = new HandlerDelegate (this);
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
            if (D) {
                Log.d (TAG, "The service unbound");
            }
        }

        running = false;
        super.onPause ();
    }

    @Override
    public void run () {
        SetupItem item = DBService.SetupItem.getItem (Keys.DataNames.GATHER_INTERVAL);
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
                EnvData data = provider.getData ();
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

    @Override
    public void onTimeout () {
        handler.sendEmptyMessage (HIDE_ACTION_BAR);
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case HIDE_ACTION_BAR :
                if (actionBar.getVisibility () != View.GONE) {
                    actionBar.setVisibility (View.GONE);
                }
                break;
        }
    }

    @Override
    public void onClick (View v) {
        if (v.getId () == R.id.root) {
            toggleActionBar ();
        } else {
            cancelTimer ();
            switch (v.getId ()) {
                case R.id.bluetooth:
                    showFindActivity ();
                    break;
                case R.id.config:
                    showSettings ();
                    break;
                case R.id.update:
                    break;
                case R.id.system: {
                    Intent intent = new Intent (this, ControlPanel2.class);
                    startActivity (intent);
                    break;
                }
            }
        }
    }

    private void guiSetup () {
        temperature = (DashboardView) findViewById (R.id.temperature);
        temperature.setTitle (getString (R.string.title_temperature))
                .setMin (-25.0).setMax (125.0)
                .setFormatter (new DecimalFormat ("#0.00"));

        humidity = (DashboardView) findViewById (R.id.humidity);
        humidity.setTitle (getString (R.string.title_humidity));

        findViewById (R.id.root).setOnClickListener (this);
        findViewById (R.id.bluetooth).setOnClickListener (this);
        findViewById (R.id.system).setOnClickListener (this);
        findViewById (R.id.update).setOnClickListener (this);
        findViewById (R.id.config).setOnClickListener (this);
        actionBar = findViewById (R.id.action_bar);
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

    private void toggleActionBar () {
        if (actionBar.getVisibility () == View.GONE) {
            actionBar.setVisibility (View.VISIBLE);
            timer = new Timer ("action_bar_timer", 5, TimeUnit.SECONDS, this);
            timer.timing ();
        } else {
            actionBar.setVisibility (View.GONE);
            cancelTimer ();
        }
    }

    private void cancelTimer () {
        if (this.timer != null) {
            Timer tmp = timer;
            timer = null;
            tmp.cancel ();
        }
    }
}