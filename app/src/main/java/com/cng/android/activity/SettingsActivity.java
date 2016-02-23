package com.cng.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cng.android.R;
import com.cng.android.adapter.SetupItemAdapter;
import com.cng.android.data.SetupItem;
import com.cng.android.db.DBService;
import com.cng.android.util.IMessageHandler;

import java.util.List;

public class SettingsActivity extends Activity implements IMessageHandler, Runnable, ListView.OnItemClickListener {
    private Handler handler;
    private SetupItemAdapter adapter;
    private ProgressDialog progress;

    private static final int ON_DATA_FETCHED = 0;
    private static final int ON_DATA_SAVED   = 1;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_settings);

        adapter = new SetupItemAdapter (this);
        ListView listView = ((ListView) findViewById (R.id.listView));
        listView.setAdapter (adapter);
        listView.setOnItemClickListener (this);
    }

    @Override
    public void handleMessage (Message message) {
        adapter.notifyDataSetChanged ();
        if (progress.isShowing ())
            progress.dismiss ();
    }

    @Override
    public void run () {
        notifyDataChanged ();
    }

    @Override
    public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
        SetupItem item = adapter.getItem (position);
        if (item.isEditable ()) {
        }
    }

    private void notifyDataChanged () {
        List<SetupItem> items = DBService.getSetupItems (true);
        adapter.setItems (items);
        handler.sendEmptyMessage (ON_DATA_FETCHED);
    }
}