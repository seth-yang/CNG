package com.cng.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.cng.android.R;
import com.cng.android.adapter.IrCodeListAdapter;
import com.cng.android.arduino.ArduinoCommand;
import com.cng.android.arduino.IArduino;
import com.cng.android.data.IRCode;
import com.cng.android.db.DBService;
import com.cng.android.fragment.LearnIRCodeFragment;
import com.cng.android.service.BluetoothDataProvider;
import com.cng.android.service.StateMonitorService;
import com.cng.android.ui.PromptDialog;
import com.cng.android.util.Keys;

import static com.cng.android.CNG.D;

public class IRControlPanelActivity extends BaseActivity implements PromptDialog.IPromptDialogListener {
    private IrCodeListAdapter adapter;
    private LearnIRCodeFragment learning;
    private View placeHolder;

    private BluetoothDataProvider provider = new BluetoothDataProvider ();
    private IArduino arduino;
    private boolean bound = false;

    private static final int LOAD_DATA                 = 0;
    private static final int UPDATE_UI                 = 1;
    private static final int EXECUTE_IR                = 2;
    private static final int WAITING_FOR_SERVICE_BOUND = 3;

    private static final String TAG = IRControlPanelActivity.class.getSimpleName ();

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_ircontrol_panel);

        adapter = new IrCodeListAdapter (this);
        ListView listView = ((ListView) findViewById (R.id.listView));
        listView.setAdapter (adapter);
        adapter.setOnClickListener (this);

        learning = (LearnIRCodeFragment) getFragmentManager ().findFragmentById (R.id.learn_ir_code);

        nonUIHandler.sendMessage (LOAD_DATA);
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case UPDATE_UI :
                adapter.notifyDataSetChanged ();
                break;
            default:
                super.handleMessage (message);
                break;
        }
    }

    @Override
    public void handleNonUIMessage (Message message) {
        switch (message.what) {
            case LOAD_DATA :
                loadData ();
                break;
            case EXECUTE_IR :
                break;
            case WAITING_FOR_SERVICE_BOUND :
                Bundle bundle = message.getData ();
                Integer code = bundle.getInt (Keys.KEY_IR_CODE_NAME);
                waitForServiceBound (code);
                break;
            default :
                super.handleNonUIMessage (message);
                break;
        }
    }

    private void loadData () {
        adapter.setCodes (DBService.IRCode.getIrCodes ());
        handler.sendEmptyMessage (UPDATE_UI);
    }

    private String irName;

    @Override
    public void onClick (View view) {
        Integer tag = (Integer) view.getTag ();
        if (tag != null) {
            int position = tag;
            IRCode ir = adapter.getItem (position);
            irName = ir.name;
            if (ir.code == null) {
                learning.setIrName (ir.name);
                learning.show ();
            } else {
                switch (view.getId ()) {
                    case R.id.btnExecute :
                        Bundle bundle = new Bundle (1);
                        bundle.putInt (Keys.KEY_IR_CODE_NAME, ir.code);
                        Message message = new Message ();
                        message.what = WAITING_FOR_SERVICE_BOUND;
                        message.setData (bundle);
                        Intent intent = new Intent (this, StateMonitorService.class);
                        getApplicationContext ().bindService (intent, provider, BIND_AUTO_CREATE);
                        nonUIHandler.sendMessage (message);
                        break;
                    case R.id.btnLearn :
                        PromptDialog dialog = new PromptDialog (this);
                        dialog.setMessage (R.string.prompt_relearn);
                        dialog.setPromptDialogListener (this);
                        dialog.show ();
                        break;
                }
            }
        } else {
            switch (view.getId ()) {
                default:
                    super.onClick (view);
            }
        }
    }

    @Override
    public void onBackPressed () {
        if (learning.isShown ()) {
            learning.hide ();
        } else
            super.onBackPressed ();
    }

    @Override
    public void onConfirm () {
        learning.setIrName (irName);
        learning.show ();
    }

    @Override
    public void onCancel () {

    }

    private void waitForServiceBound (int code) {
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

        arduino.write (ArduinoCommand.buildIRCommand (code));
//        arduino.write (new byte[] {'D', 'R', 0, -1, 0x30, (byte) 0xCF});

        getApplicationContext ().unbindService (provider);
    }
}