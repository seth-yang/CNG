package com.cng.android.data;

import java.io.Serializable;

/**
 * Created by seth.yang on 2016/2/23
 */
public class SetupItem implements Serializable {
    public enum Type {
        Text, Integer, Boolean, Double, URI
    }

    private String name, chinese;
    private Type type = Type.Text;
    private Object value;
    private boolean editable = false, visible = false;

    public SetupItem () {
    }

    public SetupItem (boolean editable, Type type) {
        this.editable = editable;
        this.type = type;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getChinese () {
        return chinese;
    }

    public void setChinese (String chinese) {
        this.chinese = chinese;
    }

    public Object getValue () {
        return value;
    }

    public void setValue (Object value) {
        this.value = value;
    }

    public boolean isEditable () {
        return editable;
    }

    public void setEditable (boolean editable) {
        this.editable = editable;
    }

    public Type getType () {
        return type;
    }

    public void setType (Type type) {
        this.type = type;
    }

    public boolean isVisible () {
        return visible;
    }

    public void setVisible (boolean visible) {
        this.visible = visible;
    }

    public Object[] toParameters () {
        return new Object[] {
                name,
                chinese,
                value != null ? value :
                        type == Type.Boolean ? false : null,
                type.name (),
                editable,
                visible
        };
    }

    @Override
    public String toString () {
        return "{name:" + name + ", chinese:" + chinese + ", value:" +
                value + ", type:" + type + ", editable:" + editable +
                ", visible:" + visible + '}';
    }
}