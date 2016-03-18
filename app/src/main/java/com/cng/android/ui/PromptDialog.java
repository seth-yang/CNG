package com.cng.android.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cng.android.R;

/**
 * Created by game on 2016/3/19
 */
public class PromptDialog extends Dialog implements View.OnClickListener {
    private TextView message;
    private String text;

    private IPromptDialogListener listener;

    public PromptDialog (Context context) {
        super (context);
    }

    public PromptDialog (Context context, int theme) {
        super (context, theme);
    }

    protected PromptDialog (Context context, boolean cancelable, OnCancelListener cancelListener) {
        super (context, cancelable, cancelListener);
    }

    public void setPromptDialogListener (IPromptDialogListener listener) {
        this.listener = listener;
    }

    public void setMessage (String text) {
        this.text = text;
    }

    public void setMessage (int resourceId) {
        this.text = getContext ().getString (resourceId);
    }

    @Override
    public void show () {
        super.show ();
        this.message.setText (text);
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.prompt_dialog);

        findViewById (R.id.btn_ok).setOnClickListener (this);
        findViewById (R.id.btn_cancel).setOnClickListener (this);
        message = (TextView) findViewById (R.id.title);
        setCancelable (false);
    }

    @Override
    public void onClick (View v) {
        if (this.listener != null) {
            switch (v.getId ()) {
                case R.id.btn_ok:
                    listener.onConfirm ();
                    break;
                case R.id.btn_cancel :
                    listener.onCancel ();
                    break;
            }
        }
        dismiss ();
    }

    public interface IPromptDialogListener {
        void onConfirm ();
        void onCancel ();
    }
}