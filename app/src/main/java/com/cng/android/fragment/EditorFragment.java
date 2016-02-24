package com.cng.android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.cng.android.R;
import com.cng.android.data.SetupItem;
import com.cng.android.util.IEditorListener;

/**
 * Created by seth.yang on 2016/2/24
 */
public class EditorFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "EditorFragment";

    private String key;
    private TextView title;
    private EditText value;
    private View root;
    private IEditorListener listener;

    private boolean showing = false;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate (R.layout.editor_dialog, container, false);
        title = (TextView) root.findViewById (R.id.title);
        value = (EditText) root.findViewById (R.id.value);

        root.findViewById (R.id.btn_ok).setOnClickListener (this);
        root.findViewById (R.id.btn_cancel).setOnClickListener (this);
        return root;
    }

    @Override
    public void onClick (View v) {
        if (listener != null) {
            switch (v.getId ()) {
                case R.id.btn_ok :
                    listener.onConfirm (key, value.getText ().toString ());
                    break;
                case R.id.btn_cancel :
                    listener.onCancel ();
                    break;
            }
        }
        hide ();
    }

    public void show () {
        root.setVisibility (View.VISIBLE);
        int width = root.getWidth ();
        root.setMinimumWidth (3 * width / 4);
        showing = true;
    }

    public void hide () {
        root.setVisibility (View.GONE);
        showing = false;
        key = null;
    }

    public boolean isShowing () {
        return showing;
    }

    public String getTitle () {
        return title.getText ().toString ();
    }

    public void setItem (SetupItem item) {
        if (item == null) {
            key = null;
            title.setText ("");
            value.setText ("");
            return;
        }

        this.key = item.getName ();

        switch (item.getType ()) {
            case Integer :
                value.setInputType (InputType.TYPE_CLASS_PHONE);
                break;
            case Double:
                value.setInputType (InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case Uri:
                value.setInputType (InputType.TYPE_TEXT_VARIATION_URI);
                break;
            case Password:
                value.setInputType (InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
            case Email:
                value.setInputType (InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            default:
                value.setInputType (InputType.TYPE_CLASS_TEXT);
                break;
        }
        title.setText (item.getChinese ());
        Object o = item.getValue ();
        if (o != null) {
            value.setText (String.valueOf (o));
        } else {
            value.setText ("");
        }
    }

    public void setEditorListener (IEditorListener listener) {
        this.listener = listener;
    }
}