package io.github.nattocb.treasure_seas.shop.gui;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

public class FishShopScreen extends AbstractContainerScreen<FishShopInv> {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(TreasureSeas.MOD_ID, "textures/gui/fish_shop.png");

    public FishShopScreen(FishShopInv container, Inventory inv, Component title) {
        super(container, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        Minecraft.getInstance().player.playSound(SoundEvents.SHULKER_BOX_OPEN, 0.7F, 1.2F);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().player.playSound(SoundEvents.BARREL_OPEN, 1.1F, 0.9F);
        super.onClose();
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, 8.0F, 6.0F, 4210752);
        this.font.draw(poseStack, this.playerInventoryTitle, 8.0F, this.imageHeight - 94.0F, 4210752);
    }
}
