package com.xtracr.realcamera.internal;

public final class CameraEntityRenderContext {
    private static int depth;

    private CameraEntityRenderContext() {
    }

    public static boolean isRendering() {
        return depth > 0;
    }

    public static void run(Runnable action) {
        depth++;
        try {
            action.run();
        } finally {
            depth--;
        }
    }
}
