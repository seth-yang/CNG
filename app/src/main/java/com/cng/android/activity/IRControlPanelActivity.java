package com.cng.android.activity;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import com.cng.android.R;
import com.cng.android.adapter.IrCodeListAdapter;
import com.cng.android.data.IRCode;
import com.cng.android.db.DBService;
import com.cng.android.fragment.LearnIRCodeFragment;
import com.cng.android.ui.PromptDialog;

public class IRControlPanelActivity extends BaseActivity implements PromptDialog.IPromptDialogListener {
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
    public void onClick (View view) {
        Integer tag = (Integer) view.getTag ();
        if (tag != null) {
            int position = tag;
            IRCode ir = adapter.getItem (position);

            if (ir.code == null) {
                learning.setIrName (ir.name);
                learning.show ();
            } else {
                switch (view.getId ()) {
                    case R.id.btnExecute :
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

    }

    @Override
    public void onCancel () {

    }
}