package com.cng.android.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cng.android.CNG;
import com.cng.android.R;
import com.cng.android.adapter.SetupListAdapter;
import com.cng.android.data.SetupItem;
import com.cng.android.db.DBService;
import com.cng.android.fragment.EditorFragment;
import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.IEditorListener;
import com.cng.android.util.IMessageHandler;

import static com.cng.android.CNG.D;

public class SettingsActivity extends Activity
        implements IMessageHandler, Runnable,
        ListView.OnItemClickListener, IEditorListener {
    private SetupListAdapter adapter;
    private Handler handler;
    private EditorFragment editor;

    private Message localMessage;

    private static final int LOCAL_STATE_FETCH_DATA = 0;
    private static final int LOCAL_STATE_UPDATE_ITEM = 1;
    private static final String TAG = "SettingActivity";

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_setting);

        ListView listView = (ListView) findViewById (R.id.listView);
        editor = (EditorFragment) getFragmentManager ().findFragmentById (R.id.editor);
        editor.setEditorListener (this);

        adapter = new SetupListAdapter (this);
        listView.setOnItemClickListener (this);
        listView.setAdapter (adapter);

        handler = new HandlerDelegate (this);

        localMessage = new Message ();
        localMessage.what = LOCAL_STATE_FETCH_DATA;

        ActionBar bar = getActionBar ();
        if (bar != null) {
            bar.show ();
            bar.setHomeButtonEnabled (true);
        }
    }

    @Override
    protected void onResume () {
        super.onResume ();
        CNG.runInNonUIThread (this);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        return true;
//        return super.onCreateOptionsMenu (menu);
    }

    @Override
    public void handleMessage (Message message) {
        adapter.notifyDataSetChanged ();
    }

    @Override
    public void run () {
        switch (localMessage.what) {
            case LOCAL_STATE_FETCH_DATA : {
                adapter.setItems (DBService.getSetupItems (false));
                handler.sendEmptyMessage (0);
                break;
            }
            case LOCAL_STATE_UPDATE_ITEM : {
                Bundle bundle = localMessage.getData ();
                String name  = bundle.getString ("name");
                String value = bundle.getString ("value");
                updateItem (name, value);
                break;
            }
        }
    }

    @Override
    public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
        SetupItem item = adapter.getItem (position);
        SetupItem.Type type = item.getType ();
        if (type != SetupItem.Type.Boolean && item.isEditable ()) {
            editor.setItem (item);
            editor.show ();
        }
    }

    @Override
    public void onConfirm (String key, String value) {
        if (D)
            Log.d (TAG, "dialog.confirm, key = " + key + ", value = " + value);

        if (key != null && key.trim ().length () > 0) {
            Bundle bundle = new Bundle (2);
            bundle.putString ("name", key);
            bundle.putString ("value", value);
            localMessage.setData (bundle);
            localMessage.what = LOCAL_STATE_UPDATE_ITEM;
            CNG.runInNonUIThread (this);
        }
    }

    @Override
    public void onCancel () {

    }

    private void updateItem (String name, String value) {
        SetupItem item = DBService.getSetupItem (name);
        if (item == null)
            return;

        SetupItem.Type type = item.getType ();
        Object target = null;
        switch (type) {
            case Double : {
                try {
                    target = Double.parseDouble (value);
                } catch (Exception ex) {
                    Log.w (TAG, ex.getMessage (), ex);
                }
            }
            break;
            case Integer: {
                try {
                    target = Integer.parseInt (value);
                } catch (Exception ex) {
                    Log.w (TAG, ex.getMessage (), ex);
                }
            }
            break;
            case Boolean: {
                try {
                    target = Boolean.parseBoolean (value);
                } catch (Exception ex) {
                    Log.w (TAG, ex.getMessage (), ex);
                }
            }
            break;
            default :
                target = value;
                break;
        }
        if (target != null) {
            DBService.updateSetupItem (String.valueOf (target), name);
            adapter.setItems (DBService.getSetupItems (true));
            handler.sendEmptyMessage (0);
        }
    }
}