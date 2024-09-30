package io.github.nattocb.treasure_seas.core.utility;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class AchievementHelper {

    private static final Map<String, ResourceLocation> ADVANCEMENT_CACHE = new HashMap<>();

    public static void grantAdvancement(ServerPlayer player, String advancementKey, String criteria) {
        ResourceLocation advancementId = ADVANCEMENT_CACHE.computeIfAbsent(advancementKey, key -> new ResourceLocation(TreasureSeas.MOD_ID, key));
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerAdvancementManager advancements = server.getAdvancements();
        Advancement advancement = advancements.getAdvancement(advancementId);
        if (advancement == null) return;
        player.getAdvancements().award(advancement, criteria);
    }

}
