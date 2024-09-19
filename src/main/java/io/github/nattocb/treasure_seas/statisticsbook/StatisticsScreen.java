package io.github.nattocb.treasure_seas.statisticsbook;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class StatisticsScreen extends AbstractContainerScreen<StatisticsMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(TreasureSeas.MOD_ID, "textures/gui/fish_shop.png");

    private final int visibleRows = 4;

    public StatisticsScreen(StatisticsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 114; // Adjust for 4 rows of slots
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta != 0) {
            menu.scroll(delta > 0 ? -1 : 1); // Scroll up or down
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

}