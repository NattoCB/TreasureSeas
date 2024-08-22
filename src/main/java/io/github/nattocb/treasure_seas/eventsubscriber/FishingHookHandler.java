package io.github.nattocb.treasure_seas.eventsubscriber;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.config.FishConfigManager;
import io.github.nattocb.treasure_seas.gui.FishingTooltipRenderer;
import io.github.nattocb.treasure_seas.utils.FishUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * 钓竿相关功能
 */
@EventBusSubscriber(modid = TreasureSeas.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class FishingHookHandler {

    /**
     * 用于在玩家垂钓时给玩家显示 tooltip 水深、钓竿等级等信息
     */
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        // pre-check
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        // 确保玩家客户端开启了配置
        FishConfigManager fishConfigManager = TreasureSeas.getInstance().getFishConfigManager();
        if (!fishConfigManager.isHudFishingInfoEnable()) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        int enchantmentLevel = FishUtils.getFishRodFighterEnchantLevel(player);
        if (enchantmentLevel == 0) return;

        // main logic
        PoseStack poseStack = event.getMatrixStack();
        int tooltipX, tooltipY;
        if (!fishConfigManager.isHudFishingInfoEnableCustomPosition()) {
            tooltipX = event.getWindow().getGuiScaledWidth() / 2 + 10;
            tooltipY = event.getWindow().getGuiScaledHeight() / 2 + 10;
        } else {
            tooltipX = fishConfigManager.getHudFishingInfoCustomX();
            tooltipY = fishConfigManager.getHudFishingInfoCustomY();
        }
        FishingTooltipRenderer.checkAndRenderTooltip(poseStack, tooltipX, tooltipY);
    }

}
