package com.cng.android.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cng.android.R;
import com.cng.android.data.ControlPanelItem;

import java.util.List;

/**
 * Created by game on 2016/2/29
 */
public class ControlGridAdapter extends BaseAdapter {
    private List<ControlPanelItem> data;
    private Context context;

    public ControlGridAdapter (Context context) {
        this.context = context;
    }

    @Override
    public int getCount () {
        return data == null ? 0 : data.size ();
    }

    @Override
    public ControlPanelItem getItem (int position) {
        return data == null ? null : data.get (position);
    }

    @Override
    public long getItemId (int position) {
        return position;
    }

    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams (-1, -1);

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        TextView view;
        if (convertView == null) {
            view = new TextView (context);
            view.setGravity (Gravity.CENTER);
            view.setLayoutParams (params);
            view.setPadding (10, 10, 10, 10);
            view.setTextSize (18);
            view.setBackgroundResource (R.drawable.grid_item_background);
            convertView = view;
        } else {
            view = (TextView) convertView;
        }

        ControlPanelItem item = getItem (position);
        view.setText (context.getString (item.getTextId ()));
        view.setCompoundDrawablePadding (10);
        Drawable drawable;
        if (item.isToggled ())
            drawable = context.getDrawable (item.getActivatedDrawableId ());
        else
            drawable = context.getDrawable (item.getDrawableId ());

        if (drawable != null) {
            drawable.setBounds (0, 0, drawable.getMinimumWidth (), drawable.getMinimumHeight ());
            view.setCompoundDrawables (null, drawable, null, null);
        }
        return convertView;
    }

    public List<ControlPanelItem> getData () {
        return data;
    }

    public void setData (List<ControlPanelItem> data) {
        this.data = data;
    }
}