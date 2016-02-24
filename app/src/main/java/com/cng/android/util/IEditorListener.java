package com.cng.android.util;

/**
 * Created by seth.yang on 2016/2/24
 */
public interface IEditorListener {
    void onConfirm (String key, String value);
    void onCancel ();
}