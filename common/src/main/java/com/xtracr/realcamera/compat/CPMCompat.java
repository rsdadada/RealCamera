package com.xtracr.realcamera.compat;

import com.xtracr.realcamera.RealCamera;
import com.xtracr.realcamera.internal.CameraEntityRenderContext;

import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

final class CPMCompat {
    private static final String PLAYER_PROFILE_CLASS = "com.tom.cpm.client.PlayerProfile";

    private CPMCompat() {
    }

    static void register() {
        register(PLAYER_PROFILE_CLASS, CameraEntityRenderContext::isRendering);
    }

    static void register(String playerProfileClassName, BooleanSupplier inFirstPerson) {
        try {
            Class<?> playerProfile = Class.forName(playerProfileClassName);
            Method addInFirstPerson = playerProfile.getMethod("addInFirstPerson", BooleanSupplier.class);
            addInFirstPerson.invoke(null, inFirstPerson);
        } catch (Exception | LinkageError e) {
            RealCamera.LOGGER.warn("Compatibility with Customizable Player Models is outdated: [{}] {}",
                    e.getClass().getName(), e.getMessage());
        }
    }
}
