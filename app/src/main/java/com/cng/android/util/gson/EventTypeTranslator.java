package com.cng.android.util.gson;

import com.cng.android.data.EventType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by game on 2016/3/4
 */
public class EventTypeTranslator implements JsonSerializer<EventType>, JsonDeserializer<EventType> {
    @Override
    public EventType deserialize (JsonElement e, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (type == EventType.class) {
            int code = e.getAsCharacter ();
            return EventType.parse (code);
        }

        return context.deserialize (e, type);
    }

    @Override
    public JsonElement serialize (EventType eventType, Type type, JsonSerializationContext context) {
        return null == eventType ?
                context.serialize (null) :
                context.serialize (eventType.code);
    }
}