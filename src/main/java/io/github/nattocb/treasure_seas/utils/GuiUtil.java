package io.github.nattocb.treasure_seas.utils;


import net.minecraft.client.gui.components.Button;

public class GuiUtil {

    // Utility method to check if mouse is hovering over a button
    public static boolean isButtonHovering(Button button, int mouseX, int mouseY) {
        return mouseX >= button.x && mouseX <= button.x + button.getWidth() &&
                mouseY >= button.y && mouseY <= button.y + button.getHeight();
    }

}
