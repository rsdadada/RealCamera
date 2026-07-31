package com.xtracr.realcamera.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraEntityRenderContextTest {
    @Test
    void reportsRenderingOnlyInsideTheScope() {
        assertFalse(CameraEntityRenderContext.isRendering());
        CameraEntityRenderContext.run(() -> assertTrue(CameraEntityRenderContext.isRendering()));
        assertFalse(CameraEntityRenderContext.isRendering());
    }

    @Test
    void nestedScopeDoesNotClearTheOuterScope() {
        CameraEntityRenderContext.run(() -> {
            CameraEntityRenderContext.run(() -> assertTrue(CameraEntityRenderContext.isRendering()));
            assertTrue(CameraEntityRenderContext.isRendering());
        });
        assertFalse(CameraEntityRenderContext.isRendering());
    }

    @Test
    void restoresStateWhenRenderingThrows() {
        IllegalStateException expected = new IllegalStateException("render failed");

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> CameraEntityRenderContext.run(() -> {
                    throw expected;
                }));

        assertSame(expected, thrown);
        assertFalse(CameraEntityRenderContext.isRendering());
    }
}
