package com.xtracr.realcamera.compat;

import com.tom.cpm.client.PlayerProfile;
import com.xtracr.realcamera.internal.CameraEntityRenderContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CPMCompatTest {
    @BeforeEach
    void resetPlayerProfile() {
        PlayerProfile.reset();
    }

    @Test
    void registersTheRealCameraSupplierWithCpm() {
        CPMCompat.register();

        assertNotNull(PlayerProfile.registeredSupplier());
        assertFalse(PlayerProfile.registeredSupplier().getAsBoolean());
        CameraEntityRenderContext.run(() ->
                assertTrue(PlayerProfile.registeredSupplier().getAsBoolean()));
        assertFalse(PlayerProfile.registeredSupplier().getAsBoolean());
    }

    @Test
    void missingCpmClassDoesNotAbortInitialization() {
        assertDoesNotThrow(() ->
                CPMCompat.register("com.tom.cpm.client.MissingPlayerProfile", () -> false));
    }
}
