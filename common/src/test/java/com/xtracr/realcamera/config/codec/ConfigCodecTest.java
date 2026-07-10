package com.xtracr.realcamera.config.codec;

import com.xtracr.realcamera.config.BindTarget;
import com.xtracr.realcamera.config.BindTarget.BindConfig;
import com.xtracr.realcamera.config.BindTarget.TargetConfig;
import com.xtracr.realcamera.config.OffsetConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigCodecTest {
    @Test
    void currentCodecRoundTripsIndependentRotationAxes() {
        BindTarget expected = target(new BindConfig(true, false, true, true, false, true));
        ByteBuf buffer = Unpooled.buffer();
        try {
            ConfigCodec.writeWithVersion(buffer, expected);

            assertEquals(704, buffer.getShort(0));
            BindTarget actual = ConfigCodec.readWithVersion(buffer);
            assertEquals(expected.name(), actual.name());
            assertEquals(expected.textureId(), actual.textureId());
            assertEquals(expected.priority(), actual.priority());
            assertEquals(expected.disablingDepth(), actual.disablingDepth());
            assertEquals(expected.targetConfig(), actual.targetConfig());
            assertEquals(expected.bindConfig(), actual.bindConfig());
            assertEquals(expected.disableConfigs(), actual.disableConfigs());
        } finally {
            buffer.release();
        }
    }

    @Test
    void legacyCodecMapsCombinedRotationFlagToAllAxes() {
        ByteBuf buffer = Unpooled.buffer();
        try {
            buffer.writeShort(703);
            ConfigCodec.CODEC_703.encode(buffer, target(new BindConfig(false, true, false, true)));

            BindConfig decoded = ConfigCodec.readWithVersion(buffer).bindConfig();

            assertTrue(decoded.bindPitch());
            assertTrue(decoded.bindYaw());
            assertTrue(decoded.bindRoll());
        } finally {
            buffer.release();
        }
    }

    @Test
    void legacyCodecMapsDisabledRotationFlagToAllAxesDisabled() {
        ByteBuf buffer = Unpooled.buffer();
        try {
            buffer.writeShort(703);
            ConfigCodec.CODEC_703.encode(buffer, target(new BindConfig(false, true, false, false)));

            BindConfig decoded = ConfigCodec.readWithVersion(buffer).bindConfig();

            assertFalse(decoded.bindPitch());
            assertFalse(decoded.bindYaw());
            assertFalse(decoded.bindRoll());
        } finally {
            buffer.release();
        }
    }

    private static BindTarget target(BindConfig bindConfig) {
        return new BindTarget(
                "test",
                "minecraft:textures/entity/player/",
                1,
                0.2f,
                new TargetConfig(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f),
                bindConfig,
                new OffsetConfig(),
                List.of()
        );
    }
}
