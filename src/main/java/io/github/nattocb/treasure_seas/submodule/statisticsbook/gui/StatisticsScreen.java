package io.github.nattocb.treasure_seas.submodule.statisticsbook.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class StatisticsScreen extends AbstractContainerScreen<StatisticsMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(TreasureSeas.MOD_ID, "textures/gui/info_book.png");

    private static final int SCROLLBAR_COLOR = 0xFFAAAAAA;
    private static final int SCROLLBAR_Y = 18;
    private static final int SCROLLBAR_WIDTH = 7;
    private static final int SCROLLBAR_HEIGHT = 128;
    private int scrollBarScaledHeight;
    private int scrollBarXPos;
    private int scrollBarYPos;
    private double mouseClickY = -1;
    private int indexWhenClicked;
    private int lastNumberOfMoves = 0;

    public StatisticsScreen(StatisticsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 180;
        recalculateScrollBar();
    }

    @Override
    public void init() {
        super.init();
        recalculateScrollBar();
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Render the scrollbar
        fill(poseStack, scrollBarXPos, scrollBarYPos, scrollBarXPos + SCROLLBAR_WIDTH, scrollBarYPos + scrollBarScaledHeight, SCROLLBAR_COLOR);

        // If the mouse is clicked and dragging the scroll bar, adjust the scrollOffset accordingly
        if (mouseClickY != -1) {
            float pixelsPerRow = (SCROLLBAR_HEIGHT - scrollBarScaledHeight) / (float) Math.max(1, menu.totalRows - menu.visibleRows);
            if (pixelsPerRow != 0) {
                int numberOfRowsMoved = (int) ((mouseY - mouseClickY) / pixelsPerRow);
                if (numberOfRowsMoved != lastNumberOfMoves) {
                    menu.scroll(indexWhenClicked + numberOfRowsMoved - menu.scrollOffset); // Apply the scroll increment
                    lastNumberOfMoves = numberOfRowsMoved;
                    recalculateScrollBar(); // Update scroll bar position
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta != 0) {
            menu.scroll(delta > 0 ? -1 : 1); // Scroll up or down
            recalculateScrollBar();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        // Check if the click is within the scroll bar's bounds
        if (mouseX >= scrollBarXPos && mouseX <= scrollBarXPos + SCROLLBAR_WIDTH && mouseY >= scrollBarYPos && mouseY <= scrollBarYPos + scrollBarScaledHeight) {
            mouseClickY = mouseY;
            indexWhenClicked = menu.scrollOffset; // Store the initial offset when clicked
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        boolean result = super.mouseReleased(mouseX, mouseY, state);
        // Reset drag state when mouse is released
        if (state != -1 && mouseClickY != -1) {
            mouseClickY = -1;
            indexWhenClicked = 0;
            lastNumberOfMoves = 0;
        }
        return result;
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    private void recalculateScrollBar() {
        int totalRows = menu.totalRows;
        int visibleRows = 4;
        int scrollBarTotalHeight = SCROLLBAR_HEIGHT - 1;
        this.scrollBarScaledHeight = (int) (scrollBarTotalHeight * Math.min(1f, ((float) visibleRows / totalRows)));
        this.scrollBarXPos = leftPos + 9 + 18 + 18 + 18 - 1;
        this.scrollBarYPos = topPos + SCROLLBAR_Y + ((scrollBarTotalHeight - scrollBarScaledHeight) * menu.scrollOffset / Math.max(1, totalRows - visibleRows));
    }

    @Override
    protected void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
    }

}
