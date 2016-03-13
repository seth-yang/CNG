package com.cng.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.cng.android.CNG;
import com.cng.android.R;
import com.cng.android.adapter.ControlGridAdapter;
import com.cng.android.arduino.ArduinoCommand;
import com.cng.android.data.ControlPanelItem;
import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.IMessageHandler;
import com.cng.android.util.Keys;

import java.util.ArrayList;
import java.util.List;

public class ControlPanel extends Activity
        implements Runnable, IMessageHandler, GridView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "ControlPanel";

    private static final int RELOAD_GRID = 0;

    private Handler handler;
    private ControlGridAdapter adapter;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.control_panel);

        handler = new HandlerDelegate (this);
        adapter = new ControlGridAdapter (this);

        GridView grid = (GridView) findViewById (R.id.grid);
        grid.setAdapter (adapter);
        grid.setOnItemClickListener (this);

        findViewById (R.id.btn_back).setOnClickListener (this);
    }

    @Override
    protected void onResume () {
        super.onResume ();
        CNG.runInNonUIThread (this);
    }

    @Override
    public void run () {
        List<ControlPanelItem> items = new ArrayList<> (5);
        items.add (new ControlPanelItem (0, R.string.gv_fan, R.drawable.gv_fan, R.drawable.gv_fan_activated));
        items.add (new ControlPanelItem (1, R.string.gv_ir, R.drawable.gv_infrared));
        items.add (new ControlPanelItem (2, R.string.gv_key, R.drawable.gv_key));
        items.add (new ControlPanelItem (3, R.string.gv_remote, R.drawable.gv_remote_control, R.drawable.gv_remote_control_activated));
        items.add (new ControlPanelItem (4, R.string.gv_other, R.drawable.gv_electrical_sensor));
        adapter.setData (items);
        handler.sendEmptyMessage (RELOAD_GRID);
    }

    @Override
    public void handleMessage (Message message) {
        switch (message.what) {
            case RELOAD_GRID :
                adapter.notifyDataSetChanged ();
                break;
        }
    }

    @Override
    public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
        ControlPanelItem item = adapter.getItem (position);
        item.toggle ();
        adapter.notifyDataSetChanged ();

        switch (item.getId ()) {
            case 0 : {       // fan
                Intent intent = new Intent (Keys.Actions.SET_ARDUINO);
                if (item.isToggled ())
                    intent.putExtra ("command", ArduinoCommand.CMD_CLOSE_FAN);
                else
                    intent.putExtra ("command", ArduinoCommand.CMD_OPEN_FAN);
                sendBroadcast (intent);
                break;
            }
            case 1 :
                break;
            case 2 :
                break;
            case 3 :        // 红外遥控器
                break;
            case 4 :
                break;
        }
    }

    @Override
    public void onClick (View v) {
        switch (v.getId ()) {
            case R.id.btn_back : {
                finish ();
                break;
            }
        }
    }
}