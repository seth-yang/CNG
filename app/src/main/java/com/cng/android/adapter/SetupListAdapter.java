package com.cng.android.adapter;

import android.app.Service;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cng.android.R;
import com.cng.android.data.SetupItem;

import java.util.ArrayList;
import java.util.List;

import static com.cng.android.CNG.D;

/**
 * Created by seth.yang on 2016/2/23
 */
public class SetupListAdapter extends BaseAdapter {
    private static final String TAG = SetupListAdapter.class.getSimpleName ();
    private List<SetupItem> items = new ArrayList<> ();
    private Context context;

    private SparseArray<Holder> managedViews = new SparseArray<> ();

    public SetupListAdapter (Context context) {
        this.context = context;
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
        SetupItem item = getItem (position);
        SetupItem.Type type = item.getType ();
        if (D)
            Log.d (TAG, "position=" + position + ", item=" + item);

        Holder holder;
        if (managedViews.indexOfKey (position) < 0) {
            holder = new Holder ();
            View view;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
            if (type == SetupItem.Type.Boolean) {
                view = inflater.inflate (R.layout.setup_item_boolean, null);
                holder.checkBox = (CheckBox) view.findViewById (R.id.checkBox);
            } else {
                view = inflater.inflate (R.layout.setup_item_text, null);
                holder.value = (TextView) view.findViewById (R.id.value);
            }
            holder.label = (TextView) view.findViewById (R.id.label);
            holder.root = view;
            managedViews.put (position, holder);
        } else
            holder = managedViews.get (position);

        holder.label.setText (item.getChinese () + " : ");
        try {
            if (type == SetupItem.Type.Boolean) {
                Boolean value = (Boolean) item.getValue ();
                holder.checkBox.setChecked (value);
            } else {
                Object o = item.getValue ();
                String value = String.valueOf (o);
                holder.value.setText (value);
                if (item.isEditable ()) {
                    Drawable icon = context.getDrawable  (android.R.drawable.ic_menu_edit);
                    holder.value.setCompoundDrawables (null, null, icon, null);
                } else {
                    holder.value.setCompoundDrawables (null, null, null, null);
                }
            }
        } catch (Exception ex) {
            Log.e (TAG, ex.getMessage (), ex);
            Log.i (TAG, "position: " + position + ", item:" + item);
        }

        return holder.root;
    }

    public void setItems (List<SetupItem> items) {
        this.items = items;
    }

    private static class Holder {
        View root;
        TextView label, value;
        CheckBox checkBox;
    }
}