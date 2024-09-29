package io.github.nattocb.treasure_seas.core.utility.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemIconButton extends Button {

    private final ItemStack itemIcon;

    public ItemIconButton(int x, int y, int width, int height, Component message, ItemStack itemIcon, Button.OnPress onPress) {
        super(x, y, width, height, message, onPress);
        this.itemIcon = itemIcon;
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderAndDecorateItem(this.itemIcon, this.x + (this.width - 16) / 2, this.y + (this.height - 16) / 2);
    }

}
