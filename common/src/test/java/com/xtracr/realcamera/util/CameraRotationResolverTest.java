package com.xtracr.realcamera.util;

import com.xtracr.realcamera.config.BindTarget.BindConfig;
import com.xtracr.realcamera.config.OffsetConfig;
import org.joml.Matrix3f;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CameraRotationResolverTest {
    private static final double EPSILON = 1.0e-3;

    @Test
    void resolvesAllEightRotationAxisCombinations() {
        Matrix3f modelRotation = rotation(30, 20, 10);
        OffsetConfig offsets = offsets(5, 7, 9);

        for (int mask = 0; mask < 8; mask++) {
            boolean bindPitch = (mask & 1) != 0;
            boolean bindYaw = (mask & 2) != 0;
            boolean bindRoll = (mask & 4) != 0;
            CameraRotationResolver resolver = new CameraRotationResolver();

            Vector3d result = resolver.resolve(
                    modelRotation,
                    -15,
                    40,
                    0,
                    new BindConfig(true, true, true, bindPitch, bindYaw, bindRoll),
                    offsets
            );

            assertEquals(bindPitch ? 20 : -10, result.x, EPSILON);
            assertEquals(bindYaw ? -30 : 33, result.y, EPSILON);
            assertEquals(bindRoll ? 10 : 9, result.z, EPSILON);
        }
    }

    @Test
    void continuesTrackingWhileEveryRotationAxisIsUnbound() {
        CameraRotationResolver resolver = new CameraRotationResolver();
        BindConfig unbound = new BindConfig(true, true, true, false, false, false);
        BindConfig pitchBound = new BindConfig(true, true, true, true, false, false);
        OffsetConfig offsets = new OffsetConfig();

        resolver.resolve(rotation(0, 89, 0), 0, 0, 0, unbound, offsets);
        Vector3d result = resolver.resolve(rotation(0, 91, 0), 0, 0, 0, pitchBound, offsets);

        assertEquals(91, result.x, EPSILON);
    }

    @Test
    void resetClearsTheTrackedBranch() {
        CameraRotationResolver resolver = new CameraRotationResolver();
        BindConfig bound = new BindConfig(true, true, true, true, true, true);
        OffsetConfig offsets = new OffsetConfig();
        Matrix3f upsideDown = rotation(0, 135, 0);

        resolver.resolve(upsideDown, 135, 0, 0, bound, offsets);
        resolver.reset();
        Vector3d result = resolver.resolve(upsideDown, 45, -180, 180, bound, offsets);

        assertEquals(45, result.x, EPSILON);
        assertEquals(-180, result.y, EPSILON);
        assertEquals(180, result.z, EPSILON);
    }

    private static Matrix3f rotation(double yaw, double pitch, double roll) {
        return new Matrix3f().rotationYXZ(
                (float) Math.toRadians(yaw),
                (float) Math.toRadians(pitch),
                (float) Math.toRadians(roll)
        );
    }

    private static OffsetConfig offsets(float pitch, float yaw, float roll) {
        OffsetConfig offsets = new OffsetConfig();
        offsets.pitch = pitch;
        offsets.yaw = yaw;
        offsets.roll = roll;
        return offsets;
    }
}
