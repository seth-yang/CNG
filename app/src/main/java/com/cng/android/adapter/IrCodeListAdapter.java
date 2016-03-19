package com.cng.android.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
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
public class IrCodeListAdapter extends BaseAdapter implements View.OnClickListener {
    private List<IRCode> list;
    private Context context;
    private SparseArray<View> managedViews = new SparseArray<> ();

    private View.OnClickListener listener = null;

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
        View view = managedViews.get (position);
        Holder holder;
        if (view == null) {
            convertView = LayoutInflater.from (context).inflate (R.layout.ir_code_list_item, null);
            holder = new Holder ();
            holder.title = (TextView) convertView.findViewById (R.id.title);
            holder.button = (TextView) convertView.findViewById (R.id.btnLearn);
            holder.btnExecute = (TextView) convertView.findViewById (R.id.btnExecute);
            holder.button.setOnClickListener (this);
            holder.btnExecute.setOnClickListener (this);
            holder.button.setTag (position);
            holder.btnExecute.setTag (position);
            convertView.setTag (holder);
            managedViews.put (position, convertView);
        } else {
            convertView = view;
            holder = (Holder) convertView.getTag ();
        }

        IRCode code = getItem (position);
        holder.title.setText (code.chinese);
        if (code.code == null) {
            holder.button.setText (R.string.btn_learn);
            holder.btnExecute.setText ("");
            holder.btnExecute.setVisibility (View.GONE);
        } else {
            holder.button.setText (R.string.btn_re_learn);
            holder.btnExecute.setText (R.string.btn_action);
        }
        return convertView;
    }

    public void setCodes (List<IRCode> data) {
        this.list = data;
    }

    public void setOnClickListener (View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick (View v) {
        if (this.listener != null)
            listener.onClick (v);
    }

    private static class Holder {
        TextView title, button, btnExecute;
        int position;
    }
}
