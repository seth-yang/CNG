package com.cng.android.util.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by game on 2016/2/26
 */
public class EnumTranslator implements JsonSerializer<Enum<?>>, JsonDeserializer<Enum<?>> {
    @SuppressWarnings ("unchecked")
    public Enum<?> deserialize (JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if ((typeOfT instanceof Class) && Enum.class.isAssignableFrom ((Class<?>) typeOfT)) {
            Class<Enum> et = (Class<Enum>) typeOfT;
            return Enum.valueOf (et, json.getAsString ());
        }
        return context.deserialize (json, typeOfT);
    }

    public JsonElement serialize (Enum<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ?
                context.serialize (null) :
                context.serialize (src.toString ());
    }
}