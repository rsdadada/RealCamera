package com.xtracr.realcamera.util;

import org.joml.Matrix3fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class ContinuousEulerAngleTracker {
    private static final double HALF_PI = Math.PI / 2.0;
    private static final double TWO_PI = Math.PI * 2.0;
    private static final double GIMBAL_EPSILON = 1.0e-5;

    private final Vector3d angles = new Vector3d();
    private boolean initialized;

    public Vector3d update(Matrix3fc rotation, Vector3dc reference) {
        Vector3dc anchor = initialized ? angles : reference;
        double sinPitch = Math.clamp(-rotation.m21(), -1.0, 1.0);

        if (1.0 - Math.abs(sinPitch) <= GIMBAL_EPSILON) {
            updateAtGimbalLock(rotation, anchor, sinPitch);
        } else {
            double pitch = Math.asin(sinPitch);
            Vector3d principal = unwrap(new Vector3d(
                    pitch,
                    Math.atan2(rotation.m20(), rotation.m22()),
                    Math.atan2(rotation.m01(), rotation.m11())
            ), anchor);
            Vector3d alternate = unwrap(new Vector3d(
                    Math.PI - pitch,
                    principal.y + Math.PI,
                    principal.z + Math.PI
            ), anchor);
            angles.set(distanceSquared(principal, anchor) <= distanceSquared(alternate, anchor) ? principal : alternate);
        }

        initialized = true;
        return new Vector3d(angles);
    }

    public void reset() {
        angles.zero();
        initialized = false;
    }

    public boolean initialized() {
        return initialized;
    }

    private void updateAtGimbalLock(Matrix3fc rotation, Vector3dc anchor, double sinPitch) {
        double pitch = unwrap(Math.copySign(HALF_PI, sinPitch), anchor.x());
        if (sinPitch > 0.0) {
            double difference = unwrap(Math.atan2(rotation.m10(), rotation.m00()), anchor.y() - anchor.z());
            angles.set(
                    pitch,
                    (anchor.y() + anchor.z() + difference) / 2.0,
                    (anchor.y() + anchor.z() - difference) / 2.0
            );
        } else {
            double sum = unwrap(-Math.atan2(rotation.m10(), rotation.m00()), anchor.y() + anchor.z());
            angles.set(
                    pitch,
                    (anchor.y() - anchor.z() + sum) / 2.0,
                    (-anchor.y() + anchor.z() + sum) / 2.0
            );
        }
    }

    private static Vector3d unwrap(Vector3d value, Vector3dc reference) {
        return value.set(
                unwrap(value.x, reference.x()),
                unwrap(value.y, reference.y()),
                unwrap(value.z, reference.z())
        );
    }

    private static double unwrap(double value, double reference) {
        return value + TWO_PI * Math.rint((reference - value) / TWO_PI);
    }

    private static double distanceSquared(Vector3dc value, Vector3dc reference) {
        double x = value.x() - reference.x();
        double y = value.y() - reference.y();
        double z = value.z() - reference.z();
        return x * x + y * y + z * z;
    }
}
