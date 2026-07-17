package com.xtracr.realcamera;

import com.xtracr.realcamera.compat.CompatibilityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class EventHandler {
    public static void addListeners() {
        NeoForge.EVENT_BUS.addListener(EventHandler::onClientTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true, EventHandler::onRenderPlayerPre);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        KeyMappings.handle(Minecraft.getInstance());
    }

    private static void onRenderPlayerPre(RenderPlayerEvent.Pre<?> event) {
        if (!RealCameraCore.isRendering() || CompatibilityHelper.isRenderingCameraEntity()) return;

        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null
                || client.getCameraEntity() != player
                || event.getRenderState().id != player.getId()
                || !CompatibilityHelper.DS_isDragon(player)) return;

        event.setCanceled(true);
    }
}
