package com.cng.android.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cng.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by game on 2016/2/20
 */
public class BluetoothDeviceListAdapter  extends BaseAdapter {
    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice> ();

    private Context context;
    public BluetoothDeviceListAdapter (Context context) {
        this.context = context;
    }

    @Override
    public int getCount () {
        return devices.size ();
    }

    public void addItem (BluetoothDevice device) {
        devices.add (device);
        notifyDataSetChanged ();
    }

    @Override
    public BluetoothDevice getItem (int position) {
        return devices.get (position);
    }

    @Override
    public long getItemId (int position) {
        return position;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from (context).inflate (R.layout.bluetooth_device_list_item, null);
            holder = new ViewHolder ();
            holder.name = (TextView) convertView.findViewById (R.id.name);
            holder.mac = (TextView) convertView.findViewById (R.id.mac);
            convertView.setTag (holder);
        } else {
            holder = (ViewHolder) convertView.getTag ();
        }

        BluetoothDevice device = getItem (position);
        holder.name.setText (device.getName ());
        holder.mac.setText (device.getAddress ());
        return convertView;
    }

    public void fireDataChanged () {
        super.notifyDataSetChanged ();
    }

    private static class ViewHolder {
        TextView name, mac;
    }
}
