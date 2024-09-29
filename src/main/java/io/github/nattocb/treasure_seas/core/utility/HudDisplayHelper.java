package io.github.nattocb.treasure_seas.core.utility;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 在 actionBar 和 hotBar 中间为玩家显示提示语
 * 该方法仅能在纯客户端类下调用，禁止在双端类调用
 */
@Mod.EventBusSubscriber(modid = TreasureSeas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HudDisplayHelper {

    private static Component currentMessage = null;
    private static int tickCounter = 0;
    private static int totalDurationTicks = 0;
    private static int fadeOutStartTick = 0;
    private static float alpha = 1.0f;

    /**
     * Displays a HUD message that automatically fades out after a specified duration.
     *
     * @param message           The message to display (e.g., TextComponent or TranslatableComponent).
     * @param totalDuration     The total duration to display the message, in ticks.
     * @param fadeOutStartTick  The tick at which the fade-out effect should start.
     */
    public static void showHudMessage(Component message, int totalDuration, int fadeOutStartTick) {
        HudDisplayHelper.currentMessage = message;
        HudDisplayHelper.totalDurationTicks = totalDuration;
        HudDisplayHelper.fadeOutStartTick = fadeOutStartTick;
        HudDisplayHelper.tickCounter = 0;
        HudDisplayHelper.alpha = 1.0f;
    }

    /**
     * Should be called every client tick to update the HUD message state.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (currentMessage != null) {
            tickCounter++;

            // Start fading out after the fadeOutStartTick
            if (tickCounter >= fadeOutStartTick && tickCounter <= totalDurationTicks) {
                float fadeProgress = (float) (tickCounter - fadeOutStartTick) / (totalDurationTicks - fadeOutStartTick);
                alpha = 1.0f - fadeProgress;
            }

            // Clear the message after the total duration
            if (tickCounter > totalDurationTicks || alpha < 0.05) {
                clearHudMessage();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = event.getMatrixStack();
        Component message = HudDisplayHelper.getHudMessage();
        if (message != null && mc.player != null) {
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int textWidth = mc.font.width(message);
            int yPosition = screenHeight - 55;
            // Calculate the alpha component of the color
            int alphaValue = (int) (HudDisplayHelper.getHudAlpha() * 255.0f) << 24;
            int color = 0xFFFFFF | alphaValue; // White color with varying alpha
            // Draw the message on the screen with the calculated alpha
            GuiComponent.drawString(poseStack, mc.font, message, (screenWidth - textWidth) / 2, yPosition, color);
        }
    }

    /**
     * Retrieves the current HUD message.
     *
     * @return The current HUD message, or null if none.
     */
    public static Component getHudMessage() {
        return currentMessage;
    }

    /**
     * Retrieves the current alpha (transparency) value of the HUD message.
     *
     * @return The current alpha value between 0.0f and 1.0f.
     */
    public static float getHudAlpha() {
        return alpha;
    }

    /**
     * Clears the current HUD message and resets the alpha value.
     */
    public static void clearHudMessage() {
        currentMessage = null;
    }

}
