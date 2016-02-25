package com.cng.android.util;

import com.cng.android.util.gson.DateTranslator;
import com.cng.android.util.gson.EnumTranslator;
import com.cng.android.util.gson.TypeAdapterWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by game on 2016/2/26
 */
public class GsonHelper {
    public static final Type TYPE_MAP_STRING_OBJECT = new TypeToken<Map<String, Object>> () {}.getType ();
    public static final Type TYPE_MAP_STRING_STRING = new TypeToken<Map<String, Object>> () {}.getType ();

    public static Gson getGson () {
        return getGson (false);
    }

    public static Gson getGson (boolean excludeNonExpose) {
        return getGson (excludeNonExpose, false);
    }

    public static Gson getGson (boolean excludeNonExpose, boolean dateAsLong, TypeAdapterWrapper... adapters) {
        GsonBuilder builder = new GsonBuilder ()
                .registerTypeHierarchyAdapter (Enum.class, new EnumTranslator ());

        if (dateAsLong)
            builder.registerTypeHierarchyAdapter (java.util.Date.class, new DateTranslator.LongDateTranslator ());
        else
            builder.registerTypeAdapter (java.util.Date.class, new DateTranslator.UtilDateTranslator ())
                    .registerTypeAdapter (java.sql.Date.class, new DateTranslator.SqlDateTranslator ())
                    .registerTypeAdapter (java.sql.Timestamp.class, new DateTranslator.TimestampTranslator ());

        for (TypeAdapterWrapper wrapper : adapters) {
            if (wrapper.getAdapterType () == TypeAdapterWrapper.AdapterType.Normal)
                builder.registerTypeAdapter (wrapper.getBaseType (), wrapper.getAdapter ());
            else if (wrapper.getAdapterType () == TypeAdapterWrapper.AdapterType.Hierarchy)
                builder.registerTypeHierarchyAdapter (wrapper.getBaseType (), wrapper.getAdapter ());
        }
        if (excludeNonExpose)
            builder.excludeFieldsWithoutExposeAnnotation ();
        return builder.create ();
    }
}