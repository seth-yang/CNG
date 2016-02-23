package com.cng.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.cng.android.data.SetupItem;

import java.util.ArrayList;
import java.util.List;

import static com.cng.android.CNG.D;

/**
 * Created by game on 2016/2/24
 */
public class SetupItemAdapter extends BaseAdapter {
    private List<SetupItem> items = new ArrayList<> ();
    private Context context;

    private static final String TAG = SetupItemAdapter.class.getSimpleName ();

    public SetupItemAdapter (Context context) {
        this.context = context;
    }

    public List<SetupItem> getItems () {
        return items;
    }

    public void setItems (List<SetupItem> items) {
        this.items = items;
    }

    @Override
    public int getCount () {
        return items.size ();
    }

    @Override
    public SetupItem getItem (int position) {
        return items.get (position);
    }

    @Override
    public long getItemId (int position) {
        return position;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        return null;
    }
}
