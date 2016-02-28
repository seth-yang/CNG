package com.cng.android.data;

import java.io.Serializable;

/**
 * Created by game on 2016/2/29
 */
public class ControlPanelItem implements Serializable {
    private int id;
    private int textId;
    private int drawableId;
    private int activatedDrawableId;
    private boolean toggled = false;

    public ControlPanelItem () {}

    public ControlPanelItem (int id, int textId, int drawableId) {
        this (id, textId, drawableId, drawableId);
    }

    public ControlPanelItem (int id, int textId, int drawableId, int activatedDrawableId) {
        this.id = id;
        this.textId = textId;
        this.drawableId = drawableId;
        this.activatedDrawableId = activatedDrawableId;
    }

    public int getId () {
        return id;
    }

    public void setId (int id) {
        this.id = id;
    }

    public int getTextId () {
        return textId;
    }

    public void setTextId (int textId) {
        this.textId = textId;
    }

    public int getDrawableId () {
        return drawableId;
    }

    public void setDrawableId (int drawableId) {
        this.drawableId = drawableId;
    }

    public int getActivatedDrawableId () {
        return activatedDrawableId;
    }

    public void setActivatedDrawableId (int activatedDrawableId) {
        this.activatedDrawableId = activatedDrawableId;
    }

    public boolean isToggled () {
        return toggled;
    }

    public void toggle () {
        toggled = !toggled;
    }
}