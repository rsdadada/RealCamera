package com.xtracr.realcamera.compat;

import com.xtracr.realcamera.RealCamera;

import java.lang.reflect.Method;

public final class IrisCompat {
    private static Method getInstance;
    private static Method isRenderingShadowPass;
    private static boolean warned;

    private IrisCompat() {
    }

    public static boolean isRenderingShadowPass() {
        if (!CompatibilityHelper.isModLoaded("iris")) return false;
        try {
            if (getInstance == null || isRenderingShadowPass == null) {
                Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                getInstance = irisApi.getMethod("getInstance");
                isRenderingShadowPass = irisApi.getMethod("isRenderingShadowPass");
            }
            Object irisApi = getInstance.invoke(null);
            Object value = isRenderingShadowPass.invoke(irisApi);
            return value instanceof Boolean shadowPass && shadowPass;
        } catch (ReflectiveOperationException | ClassCastException exception) {
            if (!warned) {
                warned = true;
                RealCamera.LOGGER.warn("Compatibility with Iris is outdated: [{}] {}", exception.getClass().getName(), exception.getMessage());
            }
            return false;
        }
    }
}
