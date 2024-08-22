package io.github.nattocb.treasure_seas.eventsubscriber;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 用于检测玩家和鱼搏斗时的鼠标状态，控制滚动条的上下
 */
@Mod.EventBusSubscriber(modid = TreasureSeas.MOD_ID, value = Dist.CLIENT)
public class MouseEventHandler {
    private static boolean isMouseClicked = false;

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseInputEvent event) {
        // 左键
        if (event.getButton() == 0) {
            if (event.getAction() == 1) {
                isMouseClicked = true;
            } else if (event.getAction() == 0) {
                isMouseClicked = false;
            }
        }
    }

    public static boolean isMouseClicked() {
        return isMouseClicked;
    }

    public static void resetStatus() {
        isMouseClicked = false;
    }

}