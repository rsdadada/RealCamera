package com.xtracr.realcamera.util;

import org.joml.Matrix3f;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContinuousEulerAngleTrackerTest {
    private static final double EPSILON = 1.0e-3;

    @Test
    void tracksPitchContinuouslyPastPositiveGimbalLock() {
        ContinuousEulerAngleTracker tracker = new ContinuousEulerAngleTracker();
        double[] pitches = {80, 85, 89, 90, 91, 100, 135, 180};

        for (double pitch : pitches) {
            Vector3d result = tracker.update(rotation(0, pitch, 0), radians(pitch, 0, 0));

            assertAngles(result, pitch, 0, 0);
        }
    }

    @Test
    void tracksPitchContinuouslyPastNegativeGimbalLock() {
        ContinuousEulerAngleTracker tracker = new ContinuousEulerAngleTracker();
        double[] pitches = {-80, -85, -89, -90, -91, -100, -135, -180};

        for (double pitch : pitches) {
            assertAngles(tracker.update(rotation(0, pitch, 0), radians(pitch, 0, 0)), pitch, 0, 0);
        }
    }

    @Test
    void unwrapsYawAndRollPastOneHundredEightyDegrees() {
        ContinuousEulerAngleTracker yawTracker = new ContinuousEulerAngleTracker();
        ContinuousEulerAngleTracker rollTracker = new ContinuousEulerAngleTracker();
        double[] angles = {170, 179, 180, 181, 220, 270};

        for (double angle : angles) {
            assertAngles(yawTracker.update(rotation(angle, 0, 0), radians(0, angle, 0)), 0, angle, 0);
            assertAngles(rollTracker.update(rotation(0, 0, angle), radians(0, 0, angle)), 0, 0, angle);
        }
    }

    @Test
    void preservesYawRollSplitAtBothGimbalLocks() {
        ContinuousEulerAngleTracker positive = new ContinuousEulerAngleTracker();
        ContinuousEulerAngleTracker negative = new ContinuousEulerAngleTracker();

        positive.update(rotation(20, 89, 10), radians(89, 20, 10));
        negative.update(rotation(20, -89, 10), radians(-89, 20, 10));

        assertAngles(positive.update(rotation(20, 90, 10), radians(90, 20, 10)), 90, 20, 10);
        assertAngles(negative.update(rotation(20, -90, 10), radians(-90, 20, 10)), -90, 20, 10);
    }

    @Test
    void resetReinitializesUsingTheSuppliedReference() {
        ContinuousEulerAngleTracker tracker = new ContinuousEulerAngleTracker();
        Matrix3f upsideDown = rotation(0, 135, 0);

        assertAngles(tracker.update(upsideDown, radians(135, 0, 0)), 135, 0, 0);
        assertTrue(tracker.initialized());

        tracker.reset();

        assertFalse(tracker.initialized());
        assertAngles(tracker.update(upsideDown, radians(45, 180, 180)), 45, 180, 180);
    }

    @Test
    void trackedAnglesAlwaysReconstructTheInputRotation() {
        ContinuousEulerAngleTracker tracker = new ContinuousEulerAngleTracker();
        double[][] rotations = {
                {10, 30, -20},
                {40, 89, 15},
                {40, 90, 15},
                {40, 100, 15},
                {190, 135, -170},
                {250, 200, 220}
        };

        for (double[] input : rotations) {
            Matrix3f expected = rotation(input[0], input[1], input[2]);
            Vector3d tracked = tracker.update(expected, radians(input[1], input[0], input[2]));
            Matrix3f actual = new Matrix3f().rotationYXZ((float) tracked.y, (float) tracked.x, (float) tracked.z);

            assertMatrixEquals(expected, actual);
        }
    }

    @Test
    void tracksAFullPitchRevolutionInBothDirections() {
        ContinuousEulerAngleTracker tracker = new ContinuousEulerAngleTracker();

        for (int pitch = 0; pitch <= 360; pitch += 5) {
            assertAngles(tracker.update(rotation(20, pitch, 10), radians(pitch, 20, 10)), pitch, 20, 10);
        }
        for (int pitch = 355; pitch >= 0; pitch -= 5) {
            assertAngles(tracker.update(rotation(20, pitch, 10), radians(pitch, 20, 10)), pitch, 20, 10);
        }
    }

    private static Matrix3f rotation(double yaw, double pitch, double roll) {
        return new Matrix3f().rotationYXZ(
                (float) Math.toRadians(yaw),
                (float) Math.toRadians(pitch),
                (float) Math.toRadians(roll)
        );
    }

    private static Vector3d radians(double pitch, double yaw, double roll) {
        return new Vector3d(
                Math.toRadians(pitch),
                Math.toRadians(yaw),
                Math.toRadians(roll)
        );
    }

    private static void assertAngles(Vector3d actual, double pitch, double yaw, double roll) {
        assertEquals(pitch, Math.toDegrees(actual.x), EPSILON);
        assertEquals(yaw, Math.toDegrees(actual.y), EPSILON);
        assertEquals(roll, Math.toDegrees(actual.z), EPSILON);
    }

    private static void assertMatrixEquals(Matrix3f expected, Matrix3f actual) {
        assertEquals(expected.m00(), actual.m00(), EPSILON);
        assertEquals(expected.m01(), actual.m01(), EPSILON);
        assertEquals(expected.m02(), actual.m02(), EPSILON);
        assertEquals(expected.m10(), actual.m10(), EPSILON);
        assertEquals(expected.m11(), actual.m11(), EPSILON);
        assertEquals(expected.m12(), actual.m12(), EPSILON);
        assertEquals(expected.m20(), actual.m20(), EPSILON);
        assertEquals(expected.m21(), actual.m21(), EPSILON);
        assertEquals(expected.m22(), actual.m22(), EPSILON);
    }
}
