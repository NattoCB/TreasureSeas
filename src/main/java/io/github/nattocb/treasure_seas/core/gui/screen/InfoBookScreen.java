package io.github.nattocb.treasure_seas.core.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.core.FishWrapper;
import io.github.nattocb.treasure_seas.core.gui.menu.InfoBookMenu;
import io.github.nattocb.treasure_seas.core.utility.FishUtils;
import io.github.nattocb.treasure_seas.core.utility.GuiUtil;
import io.github.nattocb.treasure_seas.core.utility.gui.ItemIconButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InfoBookScreen extends AbstractContainerScreen<InfoBookMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(TreasureSeas.MOD_ID, "textures/gui/info_book.png");

    private static final int SCROLLBAR_COLOR = 0xFFAAAAAA;
    private static final int SCROLLBAR_Y = 18;
    private static final int SCROLLBAR_WIDTH = 7;
    private static final int SCROLLBAR_HEIGHT = 95;
    private int scrollBarScaledHeight;
    private int scrollBarXPos;
    private int scrollBarYPos;
    private double mouseClickY = -1;
    private int indexWhenClicked;
    private int lastNumberOfMoves = 0;
    private TextPage textPage;
    private int currentPage = 0; // 当前页
    private int totalPages = 1;  // 总页数
    private Button nextPageButton;
    private Button prevPageButton;
    private Button sortByLvlButton;
    private Button sortByTypeButton;
    private Button sortByPriceButton;
    private Slot previousClickedSlot = null;
    private final CompoundTag playerFishesNbt;

    public InfoBookScreen(InfoBookMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 180;
        this.playerFishesNbt = menu.playerNbtFishes;
        recalculateScrollBar();
    }

    @Override
    public void init() {
        super.init();

        this.imageWidth = 231;
        this.imageHeight = 166;
        this.leftPos = (this.width - this.imageWidth) / 2;

        // todo search bar

        // 每页最多显示 8 行，每行宽度最多 140 像素
        this.textPage = new TextPage(8, 140);

        prevPageButton = this.addRenderableWidget(new Button(this.leftPos + 197, this.topPos + 4, 12, 12, new TextComponent("<"), button -> {
            if (currentPage > 0) {
                currentPage--;
            }
        }));

        nextPageButton = this.addRenderableWidget(new Button(this.leftPos + 210, this.topPos + 4, 12, 12, new TextComponent(">"), button -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
            }
        }));

        // Adding sort buttons
        sortByTypeButton = this.addRenderableWidget(new ItemIconButton(this.leftPos - 18, this.topPos + 18, 18, 18, new TextComponent(""), new ItemStack(Items.TROPICAL_FISH), button -> {
            this.menu.sortByCategoryAndName();
            this.menu.updateVisibleSlots();
        }));

        sortByPriceButton = this.addRenderableWidget(new ItemIconButton(this.leftPos - 18, this.topPos + 39, 18, 18, new TextComponent(""), new ItemStack(Items.EMERALD), button -> {
            this.menu.sortByBasePrice();
            this.menu.updateVisibleSlots();
        }));

        sortByLvlButton = this.addRenderableWidget(new ItemIconButton(this.leftPos - 18, this.topPos + 60, 18, 18, new TextComponent(""), new ItemStack(Items.ENCHANTED_BOOK), button -> {
            this.menu.sortByEnchantmentLevel();
            this.menu.updateVisibleSlots();
        }));

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
        menu.scroll(delta > 0 ? -1 : 1); // Scroll up or down
        recalculateScrollBar();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        // Check if the click is within the scroll bar's bounds
        if (mouseX >= scrollBarXPos && mouseX <= scrollBarXPos + SCROLLBAR_WIDTH && mouseY >= scrollBarYPos && mouseY <= scrollBarYPos + scrollBarScaledHeight) {
            mouseClickY = mouseY;
            indexWhenClicked = menu.scrollOffset; // Store the initial offset when clicked
        }

        // update clicked slot's info to show corresponding fish info at the screen
        if (button == 0) { // Left click
            Slot clickedSlot = this.getSlotUnderMouse(); // 获取当前鼠标下的 Slot
            if (clickedSlot != null && clickedSlot.hasItem()) {
                // 如果新点击的 Slot 和之前的不一样，重置到第一页
                if (clickedSlot != previousClickedSlot) {
                    previousClickedSlot = clickedSlot; // 更新上次点击的 Slot
                    this.menu.setSelectedFishWrapper(clickedSlot.getItem()); // 设置被点击的 FishWrapper
                    textPage.clear(); // 重置 TextPage 以触发新的渲染
                    currentPage = 0; // 重置页码为第一页
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
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

        // 控制翻页按钮的显示
        prevPageButton.active = currentPage > 0;
        nextPageButton.active = currentPage < totalPages - 1;

        // 按钮的 hover 提示
        if (GuiUtil.isButtonHovering(sortByTypeButton, mouseX, mouseY)) {
            renderTooltip(poseStack, new TranslatableComponent("gui.treasure_seas.infoscreen.sort_type"), mouseX, mouseY);
        }
        if (GuiUtil.isButtonHovering(sortByPriceButton, mouseX, mouseY)) {
            renderTooltip(poseStack, new TranslatableComponent("gui.treasure_seas.infoscreen.sort_price"), mouseX, mouseY);
        }
        if (GuiUtil.isButtonHovering(sortByLvlButton, mouseX, mouseY)) {
            renderTooltip(poseStack, new TranslatableComponent("gui.treasure_seas.infoscreen.sort_lvl"), mouseX, mouseY);
        }

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
        // 渲染屏幕标题
        font.draw(poseStack, title, (float) titleLabelX, (float) titleLabelY, 4210752);

        // 获取点击的 FishWrapper
        FishWrapper selectedFish = menu.getSelectedFishWrapper();
        if (selectedFish != null) {

            // Fetch and render the player NBT data from the utility methods
            int maxRecordedLength = FishUtils.getFishMaxRecordedLength(playerFishesNbt, selectedFish);
            boolean isShiny = FishUtils.isFishShiny(playerFishesNbt, selectedFish);
            int catchCount = FishUtils.getFishCatchCount(playerFishesNbt, selectedFish);
            if (FishUtils.isFish(selectedFish)) {
                font.draw(poseStack,
                        I18n.get("gui.treasure_seas.infoscreen.longest_seen") + maxRecordedLength + "cm",
                        this.titleLabelX + 15,
                        this.titleLabelY + 120,
                        4210752);
                font.draw(poseStack,
                        I18n.get("gui.treasure_seas.infoscreen.shiny_caught") +
                                (isShiny ? I18n.get("gui.treasure_seas.infoscreen.shiny_caught_yes") : ""),
                        this.titleLabelX + 15,
                        this.titleLabelY + 131,
                        4210752);
            }
            font.draw(poseStack,
                    I18n.get("gui.treasure_seas.infoscreen.cnt") + catchCount,
                        this.titleLabelX + 15,
                        this.titleLabelY + 142,
                        4210752);

            // 添加 FishWrapper 信息
            if (textPage.isEmpty()) {
                String possibleBiomes = String.join(", ", selectedFish.getPossibleBiomes());
                String possibleWorlds = String.join(", ", selectedFish.getPossibleWorlds());
                possibleBiomes = StringUtils.isEmpty(possibleBiomes) ? "ALL" : possibleBiomes;
                possibleWorlds = StringUtils.isEmpty(possibleWorlds) ? "ALL" : possibleWorlds;
                if (FishUtils.isFish(selectedFish)) {
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.species") + selectedFish.getFishItemName(), font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.length") + (int) selectedFish.getMinLength() + "-" + (int) selectedFish.getMaxLength() + " cm", font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.depth") + selectedFish.getMinAppearDepth() + "-" + selectedFish.getMaxAppearDepth() + " m", font);
                    textPage.addText(I18n.get(
                            "gui.treasure_seas.infoscreen.fish_habit") +
                            (selectedFish.isCaveOnly() ? I18n.get("gui.treasure_seas.infoscreen.fish_habit_cave") : I18n.get("gui.treasure_seas.infoscreen.fish_habit_normal")),
                            font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.appear_time") + selectedFish.getAllowedTime(), font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.appear_weather") + selectedFish.getAllowedWeather(), font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.fight_lvl") + selectedFish.getLowestLootableEnchantmentLevel(), font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.tenacity") + selectedFish.getTicksToWin(), font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.dexterity") + selectedFish.getSpeedModifier(), font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.biomes") + possibleBiomes, font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.worlds") + possibleWorlds, font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.base_price") + selectedFish.getBasePrice(), font);
                } else {
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.name") + selectedFish.getFishItemName(), font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.depth") + selectedFish.getMinAppearDepth() + "-" + selectedFish.getMaxAppearDepth() + " m", font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.require_lvl") + selectedFish.getLowestLootableEnchantmentLevel(), font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.biomes") + possibleBiomes, font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.worlds") + possibleWorlds, font);
                    textPage.addText(I18n.get("gui.treasure_seas.infoscreen.base_price") + selectedFish.getBasePrice(), font);
                }
            }

            // 计算总页数
            totalPages = textPage.getTotalPages();

            // 渲染当前页的内容
            int xPos = titleLabelX + 70;
            int yPos = titleLabelY + 15;
            int lineSpacing = 10;
            List<String> pageContent = textPage.getPage(currentPage);
            for (String line : pageContent) {
                font.draw(poseStack, line, xPos, yPos, 0xFFFFFF);
                yPos += lineSpacing;
            }
        } else {
            String tip1 = I18n.get("gui.treasure_seas.infoscreen.tip1");
            String tip2 = I18n.get("gui.treasure_seas.infoscreen.tip2");
            int fontWidth1 = font.width(tip1);
            int fontWidth2 = font.width(tip2);
            // 居中于 149 115
            int leftPos1 = 149 - (fontWidth1 / 2);
            int leftPos2 = 115 - (fontWidth2 / 2);
            font.draw(poseStack, tip1, leftPos1, titleLabelY + 51, 4210752);
            font.draw(poseStack, tip2, leftPos2 + 5, titleLabelY + 126, 4210752);
        }
    }

    static class TextPage {
        private final List<String> lines = new ArrayList<>();
        private final int maxLinesPerPage;
        private final int maxWidth;

        public TextPage(int maxLinesPerPage, int maxWidth) {
            this.maxLinesPerPage = maxLinesPerPage;
            this.maxWidth = maxWidth;
        }

        // 添加要渲染的文本，自动处理换行
        public void addText(String text, Font font) {
            String[] rawLines = text.split("\n");
            for (String line : rawLines) {
                addLineWithWordWrap(line, font);
            }
        }

        // 处理超长行文本的换行
        private void addLineWithWordWrap(String text, Font font) {
            while (font.width(text) > maxWidth) {
                int cutIndex = findLastFitIndex(text, font);
                lines.add(text.substring(0, cutIndex));
                text = text.substring(cutIndex);
            }
            lines.add(text);
        }

        // 找到符合最大宽度的最后字符位置
        private int findLastFitIndex(String text, Font font) {
            for (int i = text.length(); i > 0; i--) {
                if (font.width(text.substring(0, i)) <= maxWidth) {
                    return i;
                }
            }
            return text.length();
        }

        // 获取指定页的内容
        public List<String> getPage(int pageIndex) {
            int start = pageIndex * maxLinesPerPage;
            int end = Math.min(start + maxLinesPerPage, lines.size());
            return lines.subList(start, end);
        }

        // 获取总页数
        public int getTotalPages() {
            return (int) Math.ceil((double) lines.size() / maxLinesPerPage);
        }

        public boolean isEmpty() {
            return lines.isEmpty();
        }

        public void clear() {
            lines.clear();
        }
    }

}
