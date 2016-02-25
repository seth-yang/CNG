package com.cng.android.util.gson;

/**
 * Created by game on 2016/2/26
 */
public class TypeAdapterWrapper {
    private Class<?> baseType;
    private AdapterType adapterType;
    private Object adapter;

    public enum AdapterType {
        Normal, Hierarchy
    }

    public TypeAdapterWrapper () {
    }

    public TypeAdapterWrapper (Class<?> baseType, AdapterType adapterType, Object adapter) {
        this.baseType = baseType;
        this.adapterType = adapterType;
        this.adapter = adapter;
    }

    public Class<?> getBaseType () {
        return baseType;
    }

    public void setBaseType (Class<?> baseType) {
        this.baseType = baseType;
    }

    public AdapterType getAdapterType () {
        return adapterType;
    }

    public void setAdapterType (AdapterType adapterType) {
        this.adapterType = adapterType;
    }

    public Object getAdapter () {
        return adapter;
    }

    public void setAdapter (Object adapter) {
        this.adapter = adapter;
    }
}