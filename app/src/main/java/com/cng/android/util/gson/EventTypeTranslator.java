package com.cng.android.util.gson;

import com.cng.android.data.EventTarget;
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
public class EventTypeTranslator implements JsonSerializer<EventTarget>, JsonDeserializer<EventTarget> {
    @Override
    public EventTarget deserialize (JsonElement e, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (type == EventTarget.class) {
            int code = e.getAsCharacter ();
            return EventTarget.parse (code);
        }

        return context.deserialize (e, type);
    }

    @Override
    public JsonElement serialize (EventTarget target, Type type, JsonSerializationContext context) {
        return null == target ?
                context.serialize (null) :
                context.serialize (target.code);
    }
}