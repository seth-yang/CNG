package com.cng.android.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cng.android.R;
import com.cng.android.arduino.ArduinoCommand;
import com.cng.android.arduino.IArduino;
import com.cng.android.arduino.IArduinoListener;
import com.cng.android.concurrent.INonUIMessageHandler;
import com.cng.android.concurrent.NonUIHandler;
import com.cng.android.concurrent.NonUIHandlerDelegate;
import com.cng.android.data.EnvData;
import com.cng.android.data.Event;
import com.cng.android.db.DBService;
import com.cng.android.service.BluetoothDataProvider;
import com.cng.android.service.StateMonitorService;
import com.cng.android.ui.CartoonView;
import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.IMessageHandler;

import static com.cng.android.CNG.D;

/**
 * Created by game on 2016/3/14
 */
public class LearnIRCodeFragment extends Fragment implements IMessageHandler, INonUIMessageHandler, IArduinoListener {
    private TextView message;
    private View root;
    private CartoonView cartoon;
    private Handler handler;
    private NonUIHandler nonUIHandler;
    private Context context;

    private long code;
    private int match_times = 0;
    private boolean visible = true;
    private boolean bound = false;
    private String ir_name;
    private IArduino arduino;
    private BluetoothDataProvider provider = new BluetoothDataProvider ();

    private static final String TAG = "IArduino";
    private static final int SET_TEXT                  = 0;
    private static final int WAITING_FOR_SERVICE_BOUND = 1;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate (R.layout.ir_learn_dialog, container, false);
        message = (TextView) root.findViewById (R.id.message);
        handler = new HandlerDelegate (this);
        nonUIHandler = new NonUIHandlerDelegate (this);
        cartoon = (CartoonView) root.findViewById (R.id.cartoon);
        return root;
    }

    @Override
    public void onResume () {
        super.onResume ();
        if (!bound) {
            Activity activity = getActivity ();
            Intent intent = new Intent (activity, StateMonitorService.class);
            activity.getApplicationContext ().bindService (intent, provider, Activity.BIND_AUTO_CREATE);
            nonUIHandler.sendMessage (WAITING_FOR_SERVICE_BOUND);
        }
    }

    @Override
    public void onPause () {
        if (bound) {
            // remove the arduino listener
            if (arduino != null)
                arduino.setArduinoListener (null);

            getActivity ().getApplicationContext ().unbindService (provider);
            bound = false;
            if (D) {
                Log.d (TAG, "The service unbound");
            }
        }
        match_times = 0;
        code = 0;
        super.onPause ();
    }

    public void show () {
        this.visible = true;
        if (root != null) {
            root.setVisibility (View.VISIBLE);
        }
    }

    public void hide () {
        this.visible = false;
        if (root != null) {
            root.setVisibility (View.GONE);
        }
        this.ir_name = null;
        arduino.write (ArduinoCommand.CMD_IR_SILENT);
    }

    public boolean isShown () {
        return visible;
    }

    public String getIrName () {
        return ir_name;
    }

    public void setIrName (String ir_name) {
        this.ir_name = ir_name;
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case SET_TEXT : {
                Bundle bundle = message.getData ();
                String label  = bundle.getString ("label");
                this.message.setText (label);
                break;
            }
        }
    }

    @Override
    public void onDataReceived (EnvData data) {

    }

    @Override
    public void onEventRaised (Event event) {
        switch (event.type) {
            case Mode :
                char ch = event.data.charAt (0);
                if (ch == 'L') {
                    setText (R.string.msg_press_ir_control);
                }
                break;
        }
    }

    @Override
    public void onIRCodeReceived (long code) {
        if (code == 0xffffffffL) return;

        if (match_times == 0 || this.code == (code & 0xffffffffL)) {
            match_times ++;
            this.code = code;
            cartoon.blink (CartoonView.BlinkMode.Matched);
            setText (getActivity ().getString (R.string.msg_press_ir_again));

            if (match_times >= 3) {
                DBService.IRCode.update (ir_name, (int) code);
                hide ();
            }
        } else {
            this.code = -1;
            match_times = 0;
            cartoon.blink (CartoonView.BlinkMode.Mismatched);
            setText (getActivity ().getString (R.string.err_ir_code_mismatch));
        }
    }

    public void setText (String text) {
        if (Looper.getMainLooper () == Looper.myLooper ()) {
            message.setText (text);
        } else {
            Message msg = new Message ();
            msg.what = SET_TEXT;
            Bundle bundle = new Bundle (1);
            bundle.putString ("label", text);
            msg.setData (bundle);
            handler.sendMessage (msg);
        }
    }

    public void setText (int resourceId) {
        setText (getActivity ().getString (resourceId));
    }

    @Override
    public void handleNonUIMessage (Message message) {
        switch (message.what) {
            case WAITING_FOR_SERVICE_BOUND :
                waitForServiceBound ();
                break;
        }
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
//        setText (R.string.msg_press_ir_control);
        arduino.write (ArduinoCommand.CMD_LEARN_IR_CODE);
    }
}