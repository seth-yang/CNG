package com.cng.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cng.android.R;
import com.cng.android.data.IRCode;

import java.util.List;

/**
 * Created by game on 2016/3/14
 */
public class IrCodeListAdapter extends BaseAdapter {
    private List<IRCode> list;
    private Context context;

    public IrCodeListAdapter (Context context) {
        this.context = context;
    }

    @Override
    public int getCount () {
        return list == null ? 0 : list.size ();
    }

    @Override
    public IRCode getItem (int position) {
        return list == null ? null : list.get (position);
    }

    @Override
    public long getItemId (int position) {
        return position;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from (context).inflate (R.layout.ir_code_list_item, null);
            holder = new Holder ();
            holder.title = (TextView) convertView.findViewById (R.id.title);
            holder.button = (TextView) convertView.findViewById (R.id.button);
            convertView.setTag (holder);
        } else {
            holder = (Holder) convertView.getTag ();
        }

        IRCode code = getItem (position);
        holder.title.setText (code.chinese);
        if (code.code == null)
            holder.button.setText (R.string.btn_learn);
        else
            holder.button.setText (R.string.btn_action);

        return convertView;
    }

    public void setCodes (List<IRCode> data) {
        this.list = data;
    }

    private static class Holder {
        TextView title, button;
    }
}
