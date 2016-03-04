package com.cng.android.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cng.android.R;
import com.cng.android.arduino.IArduino;
import com.cng.android.data.ArduinoCommand;
import com.cng.android.service.BluetoothDataProvider;
import com.cng.android.service.StateMonitorService;

import java.util.HashMap;
import java.util.Map;

import static com.cng.android.CNG.D;

/**
 * Created by game on 2016/3/4
 */
public class ControlPanel2 extends BaseActivity {
    private static final String TAG = "ControlPanel";

    private static final int WAITING_FOR_SERVICE_BOUND = 0;

    private Integer selectedView = null;
    private boolean bound = false;

    private Map<Integer, TextView> cachedView = new HashMap<> ();
    private Map<Integer, StateMapping> mapping = new HashMap<> ();
    private BluetoothDataProvider provider = new BluetoothDataProvider ();
    private IArduino arduino;

    public ControlPanel2 () {
//        mapping.put (R.id.fan, new StateMapping (R.drawable.gv_fan, R.drawable.gv_fan_activated));
        mapping.put (R.id.fan, new StateMapping (R.drawable.gv_fan, R.drawable.gv_fan_activated));
        mapping.put (R.id.remote, new StateMapping (R.drawable.gv_remote_control, R.drawable.gv_remote_control_activated));
        mapping.put (R.id.ir, new StateMapping (R.drawable.gv_infrared, R.drawable.gv_infrared_activated));
        mapping.put (R.id.door, new StateMapping (R.drawable.gv_key, R.drawable.gv_key_activated));
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.control_panel_2);

        TextView fan = (TextView) findViewById (R.id.fan);
        TextView remote = (TextView) findViewById (R.id.remote);
        TextView ir = (TextView) findViewById (R.id.ir);
        TextView door = (TextView) findViewById (R.id.door);

        cachedView.put (R.id.fan, fan);
        cachedView.put (R.id.remote, remote);
        cachedView.put (R.id.ir, ir);
        cachedView.put (R.id.door, door);

        for (TextView view : cachedView.values ())
            view.setOnClickListener (this);

        if (savedInstanceState != null) {
            selectedView = savedInstanceState.getInt ("selectedView");
        }
    }

    @Override
    protected void onResume () {
        super.onResume ();
        if (selectedView != null) {
            int backup = selectedView;
            selectedView = null;
            changeState (backup);
        }

        if (!bound) {
            Intent intent = new Intent (this, StateMonitorService.class);
            getApplicationContext ().bindService (intent, provider, BIND_AUTO_CREATE);
            nonUIHandler.sendMessage (WAITING_FOR_SERVICE_BOUND);
        }
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
        super.onPause ();
    }

    @Override
    public void onClick (View v) {
        switch (v.getId ()) {
            case R.id.fan :
                changeState (v.getId ());
                arduino.write (ArduinoCommand.CMD_OPEN_FAN);
                break;
            case R.id.remote :
                changeState (v.getId ());
                arduino.write (ArduinoCommand.CMD_CLOSE_FAN);
                break;
            case R.id.ir :
                changeState (v.getId ());
                break;
            case R.id.door :
                changeState (v.getId ());
                break;
            default :
                super.onClick (v);
        }
    }

    @Override
    public void handleNonUIMessage (Message message) {
        switch (message.what) {
            case WAITING_FOR_SERVICE_BOUND :
                waitForServiceBound ();
                break;
            default:
                super.handleNonUIMessage (message);
        }
    }

    @Override
    public void handleMessage (Message message) {
        super.handleMessage (message);
    }

    private void changeState (int id) {
        if (selectedView != null) {
            TextView view = cachedView.get (selectedView);
            StateMapping state = mapping.get (selectedView);
            Drawable drawable = getDrawable (state.icon);
            view.setCompoundDrawablesWithIntrinsicBounds (null, drawable, null, null);
            view.setTextColor (0xFF333333);
            view.setBackgroundColor (Color.TRANSPARENT);
        }

        TextView view = cachedView.get (id);
        StateMapping state = mapping.get (id);
        Drawable drawable = getDrawable (state.activatedIcon);
        view.setCompoundDrawablesWithIntrinsicBounds (null, drawable, null, null);
        view.setTextColor (0xFFCCCCCC);
        view.setBackgroundColor (0xFF333333);
        selectedView = id;
    }

    private void waitForServiceBound () {
        while (!provider.isConnected ()) {
            try {
                Thread.sleep (5);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }

        if (D)
            Log.d (TAG, "The Service bound!!!");
        bound = true;

        arduino = provider.getBinder ().getArduino ();
        if (D)
            Log.d (TAG, "fetch arduino as: " + arduino);
    }

    private static final class StateMapping {
        int icon, activatedIcon;

        StateMapping (int icon, int activatedIcon) {
            this.icon = icon;
            this.activatedIcon = activatedIcon;
        }
    }
}