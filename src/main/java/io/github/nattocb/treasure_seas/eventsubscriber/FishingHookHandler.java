package io.github.nattocb.treasure_seas.eventsubscriber;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.config.FishConfigManager;
import io.github.nattocb.treasure_seas.gui.FishingTooltipRenderer;
import io.github.nattocb.treasure_seas.utils.FishUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
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


    /**
     * 鱼之战斗垂钓时的 actionBar、hotBar 中间的 HUD 文字提示
     */

    private static boolean wasFishing = false;
    private static String hudMessage = null;
    private static int hudTickCounter = 0;
    private static final int HUD_DISPLAY_DURATION_TICKS = 65;
    private static final int HUD_FADE_OUT_START_TICK = 42;
    private static float hudAlpha = 1.0f;
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        boolean isFishingNow = FishUtils.isPlayerFishing(mc.player);
        if (!isFishingNow) {
            // 停止钓鱼时清除信息
            wasFishing = false;
            clearHudMessage();
            return;
        }
        int enchantmentLevel = FishUtils.getFishRodFighterEnchantLevel(mc.player);
        if (enchantmentLevel == 0) return;
        if (!wasFishing) {
            // 玩家刚刚开始垂钓，设置 HUD
            hudMessage = new TranslatableComponent("message.use.scroll").getString();
            hudTickCounter = 0;
            hudAlpha = 1.0f;
        }
        hudTickCounter++;
        // 从 FADE_OUT_START 之后开始逐渐减少透明度
        if (hudTickCounter >= HUD_FADE_OUT_START_TICK && hudTickCounter <= HUD_DISPLAY_DURATION_TICKS) {
            float fadeProgress = (float)(hudTickCounter - HUD_FADE_OUT_START_TICK) / (HUD_DISPLAY_DURATION_TICKS - HUD_FADE_OUT_START_TICK);
            hudAlpha = 1.0f - fadeProgress;
        }
        // 清除 HUD 信息
        if (hudTickCounter > HUD_DISPLAY_DURATION_TICKS) {
            clearHudMessage();
        }
        wasFishing = true;
    }
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = event.getMatrixStack();
        String message = FishingHookHandler.getHudMessage();
        if (message != null && mc.player != null) {
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int textWidth = mc.font.width(message);
            int yPosition = screenHeight - 55;
            int alphaValue = (int)(hudAlpha * 255.0f) << 24;
            int color = 0xFFFFFF | alphaValue;
            GuiComponent.drawString(poseStack, mc.font, message, (screenWidth - textWidth) / 2, yPosition, color);
        }
    }
    public static String getHudMessage() {
        return hudMessage;
    }
    public static void clearHudMessage() {
        // 先清空内容
        hudMessage = null;
        // 再重置透明度
        hudAlpha = 1.0f;
    }


    /**
     * 使用鱼之战斗钓竿垂钓时的鼠标滚轮事件 handle
     */

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (FishUtils.isPlayerFishing(mc.player)) {

            // 拦截 fish fighter 附魔钓竿的 scroll event
            ItemStack fishingRod = FishUtils.getFishRodItemFromInv(mc.player);
            if (fishingRod == null) return;
            int enchantmentLevel = FishUtils.getFishRodFighterEnchantLevel(mc.player);
            if (enchantmentLevel == 0) return;
            event.setCanceled(true);

            // nbt 检测与更新（垂钓深度控制）
            FishingHook hook = FishUtils.getPlayerFishingHook(mc.player);
            if (hook == null) return;
            CompoundTag nbtData = fishingRod.getOrCreateTag();
            int waterDepth = FishUtils.calculateFluidDepth(hook.getOnPos(), hook.getLevel());
            int depthCapacity = FishUtils.getRodDepthCapacity(enchantmentLevel);
            int preferredDepth = nbtData.getInt("preferredDepth");
            preferredDepth = preferredDepth == 0 ? Math.min(waterDepth, depthCapacity) : Math.min(waterDepth, preferredDepth);
            double scrollDelta = event.getScrollDelta();
            if (scrollDelta > 0) {
                // 滚轮向上
                preferredDepth = Math.min(waterDepth, preferredDepth + 1);
                preferredDepth = Math.min(depthCapacity, preferredDepth);
            } else if (scrollDelta < 0) {
                // 滚轮向下
                preferredDepth = Math.max(1, preferredDepth - 1);
            }
            nbtData.putInt("preferredDepth", preferredDepth);
            fishingRod.setTag(nbtData);
        }
    }

}
