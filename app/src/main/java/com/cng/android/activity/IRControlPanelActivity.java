package com.cng.android.activity;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cng.android.R;
import com.cng.android.adapter.IrCodeListAdapter;
import com.cng.android.data.IRCode;
import com.cng.android.db.DBService;
import com.cng.android.fragment.LearnIRCodeFragment;

public class IRControlPanelActivity extends BaseActivity implements ListView.OnItemClickListener {
    private IrCodeListAdapter adapter;
    private LearnIRCodeFragment learning;
    private View placeHolder;

    private static final int LOAD_DATA = 0;
    private static final int UPDATE_UI = 1;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_ircontrol_panel);

        adapter = new IrCodeListAdapter (this);
        ListView listView = ((ListView) findViewById (R.id.listView));
        listView.setAdapter (adapter);
        listView.setOnItemClickListener (this);

        placeHolder = findViewById (R.id.place_holder);

//        learning = (LearnIRCodeFragment) getFragmentManager ().findFragmentById (R.id.learn_ir_code);

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

    @Override
    public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
        IRCode ir = adapter.getItem (position);
        placeHolder.setVisibility (View.VISIBLE);
        if (ir.code == null) {
            if (learning == null) {
                learning = new LearnIRCodeFragment ();
                getFragmentManager ()
                        .beginTransaction ()
                        .replace (R.id.place_holder, learning)
                        .addToBackStack (null)
                        .commitAllowingStateLoss ();
//                learning.setArguments ();
            }
//            learning.show ();
        } else {

        }
    }
}