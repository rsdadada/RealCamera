package com.xtracr.realcamera.compat;

import com.xtracr.realcamera.RealCamera;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EMFCompat {
    private static Method getVariableMap;
    private static boolean warned;

    private EMFCompat() {
    }

    public static VariableMapSnapshot saveVariables(Entity entity) {
        Map<String, Float> variableMap = getVariableMap(entity);
        return variableMap == null ? VariableMapSnapshot.EMPTY : new VariableMapSnapshot(variableMap, new LinkedHashMap<>(variableMap));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Float> getVariableMap(Entity entity) {
        if (entity == null || !CompatibilityHelper.isModLoaded("entity_model_features")) return null;
        try {
            if (getVariableMap == null) getVariableMap = entity.getClass().getMethod("emf$getVariableMap");
            Object value = getVariableMap.invoke(entity);
            return value instanceof Map<?, ?> map ? (Map<String, Float>) map : null;
        } catch (ReflectiveOperationException | ClassCastException exception) {
            if (!warned) {
                warned = true;
                RealCamera.LOGGER.warn("Compatibility with Entity Model Features is outdated: [{}] {}", exception.getClass().getName(), exception.getMessage());
            }
            return null;
        }
    }

    public record VariableMapSnapshot(Map<String, Float> variableMap, Map<String, Float> savedVariables) {
        private static final VariableMapSnapshot EMPTY = new VariableMapSnapshot(null, null);

        public void restore() {
            if (variableMap == null || savedVariables == null) return;
            variableMap.clear();
            variableMap.putAll(savedVariables);
        }
    }
}
