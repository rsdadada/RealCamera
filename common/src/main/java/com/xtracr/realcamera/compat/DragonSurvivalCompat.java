package com.xtracr.realcamera.compat;

import com.xtracr.realcamera.RealCamera;
import com.xtracr.realcamera.config.BindTarget;
import com.xtracr.realcamera.config.DisableConfig;
import com.xtracr.realcamera.renderer.state.BuiltModelRecord;
import com.xtracr.realcamera.renderer.state.VertexData;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class DragonSurvivalCompat {
    private static final String NAMESPACE = "dragonsurvival:";
    private static volatile Method isDragon;
    private static volatile Method createUIRenderState;
    private static boolean failureWarned;

    private DragonSurvivalCompat() {
    }

    static void register() {
        try {
            Class<?> provider = Class.forName("by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider");
            isDragon = provider.getMethod("isDragon", Entity.class);
        } catch (Exception | LinkageError e) {
            failDragonState(e);
        }
        try {
            Class<?> renderer = Class.forName("by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer");
            createUIRenderState = renderer.getMethod("createUIRenderState", LivingEntity.class, float.class, double.class, double.class, double.class);
        } catch (Exception | LinkageError e) {
            failUI(e);
        }
    }

    private static boolean isDragon(Player player) {
        Method method = isDragon;
        if (method == null) return false;
        try {
            return (boolean) method.invoke(null, player);
        } catch (Exception | LinkageError e) {
            failDragonState(e);
            return false;
        }
    }

    public static @Nullable EntityRenderState createUIRenderState(Entity entity, float partialTicks) {
        Method method = createUIRenderState;
        if (!(entity instanceof Player player) || method == null || !isDragon(player)) return null;
        try {
            Object result = method.invoke(null, player, partialTicks, 0.0,
                    Mth.wrapDegrees(player.yHeadRot - player.yBodyRot), player.getXRot());
            if (!(result instanceof EntityRenderState renderState)) return null;
            if (renderState instanceof LivingEntityRenderState livingState) {
                livingState.bodyRot = 180.0f;
                livingState.yRot = 0.0f;
                livingState.xRot = 0.0f;
            }
            return renderState;
        } catch (Exception | LinkageError e) {
            failUI(e);
            return null;
        }
    }

    public static boolean applyDisableConfigs(
            List<BuiltModelRecord> records,
            BindTarget target,
            Set<String> hiddenNames) {
        if (!isTarget(target)) return false;
        for (int i = 0; i < records.size(); i++) {
            BuiltModelRecord record = records.get(i);
            boolean dragonLayer = record.textureId().contains(NAMESPACE);
            DisableConfig[] configs = target.filteredDisableConfigs(config -> hiddenNames.contains(config.name())
                    && (record.containsTextureId(config.textureId())
                    || dragonLayer && target.textureId().contains(config.textureId())));
            List<VertexData[]> primitives = new ArrayList<>();
            if (!disablesAll(configs)) for (VertexData[] primitive : record.primitives()) {
                if (!disabled(primitive, configs)) primitives.add(primitive);
            }
            records.set(i, new BuiltModelRecord(
                    record.renderType(),
                    record.textureId(),
                    record.vertices(),
                    primitives.toArray(new VertexData[0][])));
        }
        return true;
    }

    public static List<BuiltModelRecord> focusCandidates(
            List<BuiltModelRecord> records,
            BindTarget target,
            String textureId) {
        if (!isTarget(target)) return records;
        String focusTextureId = textureId.isBlank() ? target.textureId() : textureId;
        if (focusTextureId.isBlank()) return records;
        List<BuiltModelRecord> exactMatches = records.stream()
                .filter(record -> record.textureId().equals(focusTextureId))
                .toList();
        return exactMatches.isEmpty()
                ? records.stream().filter(record -> record.containsTextureId(focusTextureId)).toList()
                : exactMatches;
    }

    private static boolean isTarget(BindTarget target) {
        return isDragon != null && target != null && !target.isEmpty() && target.textureId().contains(NAMESPACE);
    }

    private static boolean disabled(VertexData[] primitive, DisableConfig[] configs) {
        for (VertexData vertex : primitive) {
            for (DisableConfig config : configs) if (config.disable(vertex)) return true;
        }
        return false;
    }

    private static boolean disablesAll(DisableConfig[] configs) {
        for (DisableConfig config : configs) if (config.disableAll()) return true;
        return false;
    }

    private static synchronized void failDragonState(Throwable throwable) {
        isDragon = null;
        warn(throwable);
    }

    private static synchronized void failUI(Throwable throwable) {
        createUIRenderState = null;
        warn(throwable);
    }

    private static void warn(Throwable throwable) {
        if (failureWarned) return;
        failureWarned = true;
        Throwable cause = throwable instanceof InvocationTargetException && throwable.getCause() != null
                ? throwable.getCause()
                : throwable;
        RealCamera.LOGGER.warn("Compatibility with Dragon Survival failed: [{}] {}", cause.getClass().getName(), cause.getMessage());
    }
}
