package com.cng.android.activity;

import android.os.Bundle;
import android.os.Message;
import android.widget.ListView;

import com.cng.android.R;
import com.cng.android.adapter.IrCodeListAdapter;
import com.cng.android.db.DBService;

public class IRControlPanelActivity extends BaseActivity {
    private IrCodeListAdapter adapter;

    private static final int LOAD_DATA = 0;
    private static final int UPDATE_UI = 1;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_ircontrol_panel);

        adapter = new IrCodeListAdapter (this);
        ((ListView) findViewById (R.id.listView)).setAdapter (adapter);

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
            default :
                super.handleNonUIMessage (message);
                break;
        }
    }

    private void loadData () {
        adapter.setCodes (DBService.IRCode.getIrCodes ());
        handler.sendEmptyMessage (UPDATE_UI);
    }
}