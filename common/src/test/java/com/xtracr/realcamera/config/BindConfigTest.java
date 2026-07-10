package com.xtracr.realcamera.config;

import com.xtracr.realcamera.config.BindTarget.BindConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BindConfigTest {
    @Test
    void supportsIndependentRotationAxisFlags() {
        BindConfig config = new BindConfig(true, false, true, true, false, true);

        assertTrue(config.bindX());
        assertFalse(config.bindY());
        assertTrue(config.bindZ());
        assertTrue(config.bindPitch());
        assertFalse(config.bindYaw());
        assertTrue(config.bindRoll());
        assertFalse(config.bindRotation());
    }

    @Test
    void legacyConstructorMapsRotationToAllAxes() {
        BindConfig bound = new BindConfig(false, true, false, true);
        BindConfig unbound = new BindConfig(false, true, false, false);

        assertTrue(bound.bindPitch());
        assertTrue(bound.bindYaw());
        assertTrue(bound.bindRoll());
        assertTrue(bound.bindRotation());
        assertFalse(unbound.bindPitch());
        assertFalse(unbound.bindYaw());
        assertFalse(unbound.bindRoll());
        assertFalse(unbound.bindRotation());
    }
}
