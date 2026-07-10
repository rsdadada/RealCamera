package com.xtracr.realcamera.util;

import com.xtracr.realcamera.config.BindTarget.BindConfig;
import com.xtracr.realcamera.config.OffsetConfig;
import org.joml.Matrix3fc;
import org.joml.Vector3d;

public final class CameraRotationResolver {
    private final ContinuousEulerAngleTracker tracker = new ContinuousEulerAngleTracker();

    public Vector3d resolve(
            Matrix3fc modelRotation,
            double vanillaPitch,
            double vanillaYaw,
            double vanillaRoll,
            BindConfig bindConfig,
            OffsetConfig offsets
    ) {
        double unboundPitch = vanillaPitch + offsets.pitch;
        double unboundYaw = vanillaYaw - offsets.yaw;
        double unboundRoll = vanillaRoll + offsets.roll;
        Vector3d reference = new Vector3d(
                Math.toRadians(unboundPitch),
                Math.toRadians(-unboundYaw),
                Math.toRadians(unboundRoll)
        );
        Vector3d modelAngles = tracker.update(modelRotation, reference);
        double modelPitch = Math.toDegrees(modelAngles.x);
        double modelYaw = -Math.toDegrees(modelAngles.y);
        double modelRoll = Math.toDegrees(modelAngles.z);
        return new Vector3d(
                bindConfig.bindPitch() ? modelPitch : unboundPitch,
                bindConfig.bindYaw() ? modelYaw : unboundYaw,
                bindConfig.bindRoll() ? modelRoll : unboundRoll
        );
    }

    public void reset() {
        tracker.reset();
    }
}
