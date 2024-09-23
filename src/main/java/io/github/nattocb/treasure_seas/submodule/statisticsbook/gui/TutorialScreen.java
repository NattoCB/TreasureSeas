package io.github.nattocb.treasure_seas.submodule.statisticsbook.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.registry.ModContainerTypes;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class TutorialScreen extends AbstractContainerScreen<TutorialMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(TreasureSeas.MOD_ID, "textures/gui/info_book.png");

    public TutorialScreen(TutorialMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        // 添加返回按钮
        this.addRenderableWidget(new Button(this.leftPos + 50, this.topPos + 140, 80, 20, new TextComponent("<"), button -> {
           // todo 改为关闭界面吧，数据包有些信息除非重新传递了
        }));
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        // 渲染背景
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        // 渲染标签
        font.draw(poseStack, "Tutorial GUI", this.titleLabelX, this.titleLabelY, 4210752);
    }
}
