package io.github.nattocb.treasure_seas.core.utility;


import net.minecraft.client.gui.components.Button;

public class GuiUtil {

    // Utility method to check if mouse is hovering over a button
    public static boolean isButtonHovering(Button button, int mouseX, int mouseY) {
        return mouseX >= button.x && mouseX <= button.x + button.getWidth() &&
                mouseY >= button.y && mouseY <= button.y + button.getHeight();
    }

}
