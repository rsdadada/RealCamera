package com.xtracr.realcamera.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xtracr.realcamera.RealCameraCore;
import com.xtracr.realcamera.compat.EMFCompat;
import com.xtracr.realcamera.compat.IrisCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {
    @Unique
    private final Deque<EMFCompat.VariableMapSnapshot> realcamera$emfVariableSnapshots = new ArrayDeque<>();

    @Inject(method = "render", at = @At("HEAD"))
    private void realcamera$saveEmfVariablesInShadowPass(Entity entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        realcamera$emfVariableSnapshots.push(realcamera$shouldRestoreEmfVariables(entity) ? EMFCompat.saveVariables(entity) : EMFCompat.saveVariables(null));
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void realcamera$restoreEmfVariablesInShadowPass(Entity entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (!realcamera$emfVariableSnapshots.isEmpty()) realcamera$emfVariableSnapshots.pop().restore();
    }

    @Unique
    private static boolean realcamera$shouldRestoreEmfVariables(Entity entity) {
        Minecraft client = Minecraft.getInstance();
        return RealCameraCore.isRendering() && IrisCompat.isRenderingShadowPass() && entity == client.getCameraEntity();
    }
}
