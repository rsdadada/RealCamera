package com.xtracr.realcamera.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.xtracr.realcamera.config.BindTarget.BindConfig;

import java.lang.reflect.Type;

final class BindConfigJsonAdapter implements JsonDeserializer<BindConfig> {
    @Override
    public BindConfig deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        boolean legacyRotation = getBoolean(object, "bindRotation", false);
        return new BindConfig(
                getBoolean(object, "bindX", false),
                getBoolean(object, "bindY", false),
                getBoolean(object, "bindZ", false),
                getBoolean(object, "bindPitch", legacyRotation),
                getBoolean(object, "bindYaw", legacyRotation),
                getBoolean(object, "bindRoll", legacyRotation)
        );
    }

    private static boolean getBoolean(JsonObject object, String name, boolean fallback) {
        JsonElement value = object.get(name);
        return value == null || value.isJsonNull() ? fallback : value.getAsBoolean();
    }
}
