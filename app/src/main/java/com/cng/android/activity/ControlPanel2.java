package com.cng.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cng.android.R;
import com.cng.android.arduino.IArduino;
import com.cng.android.arduino.ArduinoCommand;
import com.cng.android.arduino.IArduinoListener;
import com.cng.android.data.EnvData;
import com.cng.android.data.Event;
import com.cng.android.service.BluetoothDataProvider;
import com.cng.android.service.StateMonitorService;

import java.io.Serializable;

import static com.cng.android.CNG.D;

/**
 * Created by game on 2016/3/4
 */
public class ControlPanel2 extends BaseActivity implements IArduinoListener {
    private static final String TAG = "ControlPanel";

    private static final int WAITING_FOR_SERVICE_BOUND = 0;
    private static final int CACHE_UI                  = 1;
    private static final int TURN_COMPOUND_BUTTON      = 2;

    private boolean bound = false;

    private BluetoothDataProvider provider = new BluetoothDataProvider ();
    private IArduino arduino;

    private SparseArray<CompoundButton> cachedButtons = new SparseArray<> (5);

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.control_panel_2);
    }

    @Override
    protected void onResume () {
        super.onResume ();

        if (!bound) {
            Intent intent = new Intent (this, StateMonitorService.class);
            getApplicationContext ().bindService (intent, provider, BIND_AUTO_CREATE);
            nonUIHandler.sendMessage (WAITING_FOR_SERVICE_BOUND);
        }

        nonUIHandler.sendMessage (CACHE_UI);
    }

    @Override
    protected void onPause () {
        if (bound) {
            // remove the arduino listener
            if (arduino != null)
                arduino.setArduinoListener (null);

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
        CompoundButton cb;
        switch (v.getId ()) {
            case R.id.fan :
                cb = cachedButtons.get (R.id.fan);
                arduino.write (cb.state ? ArduinoCommand.CMD_CLOSE_FAN : ArduinoCommand.CMD_OPEN_FAN);
                break;
            case R.id.remote :
                Intent intent = new Intent (this, IRControlPanelActivity.class);
                startActivity (intent);
                finish ();
                break;
            case R.id.ir :
                break;
            case R.id.door :
                arduino.write (ArduinoCommand.CMD_OPEN_LOCK);
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
            case CACHE_UI :
                cacheUI ();
                break;
            default:
                super.handleNonUIMessage (message);
        }
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case TURN_COMPOUND_BUTTON : {
                Bundle bundle = message.getData ();
                int id = bundle.getInt ("id", -1);
                int code = bundle.getInt ("data", -1);
                if (id != -1) {
                    CompoundButton button = cachedButtons.get (id);
                    if (button != null) {
                        button.turn (code == 'U' || code == 'C');
                    }
                }
            }
        }
        super.handleMessage (message);
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
        arduino.setArduinoListener (this);
        if (D)
            Log.d (TAG, "fetch arduino as: " + arduino);
    }

    private void cacheUI () {
        TextView fan_on  = (TextView) findViewById (R.id.fan_on);
        TextView fan_off = (TextView) findViewById (R.id.fan_off);
        ImageButton fan  = (ImageButton) findViewById (R.id.fan);
        cachedButtons.put (R.id.fan, new CompoundButton (fan_on, fan_off, fan, this));

        TextView remote_on  = (TextView) findViewById (R.id.remote_on);
        TextView remote_off = (TextView) findViewById (R.id.remote_off);
        ImageButton remote  = (ImageButton) findViewById (R.id.remote);
        cachedButtons.put (R.id.remote, new CompoundButton (remote_on, remote_off, remote, this));

        TextView ir_on  = (TextView) findViewById (R.id.ir_on);
        TextView ir_off = (TextView) findViewById (R.id.ir_off);
        ImageButton ir  = (ImageButton) findViewById (R.id.ir);
        CompoundButton cb = new CompoundButton (ir_on, ir_off, ir, this);
        cb.turn (true);
        cachedButtons.put (R.id.ir, cb);

        TextView card_on  = (TextView) findViewById (R.id.card_on);
        TextView card_off = (TextView) findViewById (R.id.card_off);
        ImageButton card  = (ImageButton) findViewById (R.id.door);
        cachedButtons.put (R.id.door, new CompoundButton (card_on, card_off, card, this));

        TextView other_on  = (TextView) findViewById (R.id.other_on);
        TextView other_off = (TextView) findViewById (R.id.other_off);
        ImageButton other  = (ImageButton) findViewById (R.id.rf_id);
        cachedButtons.put (R.id.rf_id, new CompoundButton (other_on, other_off, other, this));

        fan.setOnClickListener (this);
        remote.setOnClickListener (this);
        ir.setOnClickListener (this);
        card.setOnClickListener (this);
        other.setOnClickListener (this);
    }

    @Override
    public void onDataReceived (EnvData data) {

    }

    @Override
    public void onEventRaised (Event event) {
        int id = -1;
        switch (event.type) {
            case Fan:
                id = R.id.fan;
                break;
            case Door :
                id = R.id.door;
                break;
            case IR:
                id = R.id.ir;
                break;
            default:
        }

        if (id != -1) {
            Message message = new Message ();
            message.what = TURN_COMPOUND_BUTTON;
            Bundle bundle = new Bundle (2);
            bundle.putInt ("id", id);
            bundle.putInt ("data", event.data.charAt (0));
            message.setData (bundle);
            handler.sendMessage (message);
        }
    }

    @Override
    public void onIRCodeReceived (long code) {

    }

    private static final class CompoundButton implements Serializable {
        TextView on, off;
        ImageButton button;
        boolean state;

        Context context;

        public CompoundButton (TextView on, TextView off, ImageButton button, Context context) {
            this.on = on;
            this.off = off;
            this.button = button;
            this.context = context;
        }

        void turn (boolean on) {
            if (on) {
                this.on.setTextAppearance (context, R.style.switch_current);
                this.off.setTextAppearance (context, R.style.cp_item);
                this.button.setImageResource (R.drawable.switch_on);
            } else {
                this.on.setTextAppearance (context, R.style.cp_item);
                this.off.setTextAppearance (context, R.style.switch_current);
                this.button.setImageResource (R.drawable.switch_off);
            }
            this.state = on;
        }
    }
}