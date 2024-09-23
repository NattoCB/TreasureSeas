package io.github.nattocb.treasure_seas.submodule.statisticsbook.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.config.FishWrapper;
import io.github.nattocb.treasure_seas.utils.FishUtils;
import io.github.nattocb.treasure_seas.utils.gui.ItemIconButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StatisticsScreen extends AbstractContainerScreen<StatisticsMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(TreasureSeas.MOD_ID, "textures/gui/info_book.png");

    private static final int SCROLLBAR_COLOR = 0xFFAAAAAA;
    private static final int SCROLLBAR_Y = 18;
    private static final int SCROLLBAR_WIDTH = 7;
    private static final int SCROLLBAR_HEIGHT = 100;
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
    private Slot previousClickedSlot = null;
    private final CompoundTag playerFishesNbt;

    public StatisticsScreen(StatisticsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 180;
        this.playerFishesNbt = menu.playerNbtFishes;
        recalculateScrollBar();
    }

    @Override
    public void init() {
        super.init();

        // todo search bar

        this.textPage = new TextPage(8, 88); // 每页最多显示 8 行，每行宽度最多 88 像素

        nextPageButton = this.addRenderableWidget(new Button(this.leftPos + 157, this.topPos + 4, 12, 12, new TextComponent(">"), button -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
            }
        }));

        prevPageButton = this.addRenderableWidget(new Button(this.leftPos + 144, this.topPos + 4, 12, 12, new TextComponent("<"), button -> {
            if (currentPage > 0) {
                currentPage--;
            }
        }));

        // Adding sort buttons
        this.addRenderableWidget(new ItemIconButton(this.leftPos - 12, this.topPos + 18, 12, 12, new TextComponent(""), new ItemStack(Items.TROPICAL_FISH), button -> {
            this.menu.sortByCategoryAndName();
            this.menu.updateVisibleSlots();
        }));

        this.addRenderableWidget(new ItemIconButton(this.leftPos - 12, this.topPos + 33, 12, 12, new TextComponent(""), new ItemStack(Items.NAME_TAG), button -> {
            this.menu.sortByFishItemName();
            this.menu.updateVisibleSlots();
        }));

        this.addRenderableWidget(new ItemIconButton(this.leftPos - 12, this.topPos + 48, 12, 12, new TextComponent(""), new ItemStack(Items.EMERALD), button -> {
            this.menu.sortByBasePrice();
            this.menu.updateVisibleSlots();
        }));

        this.addRenderableWidget(new ItemIconButton(this.leftPos - 12, this.topPos + 63, 12, 12, new TextComponent(""), new ItemStack(Items.ENCHANTED_BOOK), button -> {
            this.menu.sortByEnchantmentLevel();
            this.menu.updateVisibleSlots();
        }));

        // Add a button with "?" text to open a new GUI
        this.addRenderableWidget(new Button(this.leftPos - 12, this.topPos + 91, 12, 12, new TextComponent("?"), button -> {
            // todo i18n
            this.minecraft.setScreen(new TutorialScreen(new TutorialMenu(0), this.minecraft.player.getInventory(), new TextComponent("New GUI")));
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
            // todo i18n
            font.draw(poseStack, "LongestSeen : " + maxRecordedLength + "cm",
                    this.titleLabelX,
                    this.titleLabelY + 120,
                    4210752);
            font.draw(poseStack, "ShinyCaught: " + (isShiny ? "●" : "○"),
                    this.titleLabelX,
                    this.titleLabelY + 130,
                    4210752);
            font.draw(poseStack, "CatchCnt: " + catchCount,
                    this.titleLabelX,
                    this.titleLabelY + 140,
                    4210752);

            // 添加 FishWrapper 信息
            if (textPage.isEmpty()) {
                // todo content (+ i18n length trim)
                textPage.addText("Mod: " + selectedFish.getModNamespace(), font);
                textPage.addText("Item: " + selectedFish.getFishItemName(), font);
                textPage.addText("Min Length: " + selectedFish.getMinLength(), font);
                textPage.addText("Max Length: " + selectedFish.getMaxLength(), font);
                textPage.addText("Most Common Length: " + selectedFish.getMostCommonLength(), font);
                textPage.addText("Length Dispersion: " + selectedFish.getLengthDispersion(), font);
                textPage.addText("Min Appear Depth: " + selectedFish.getMinAppearDepth(), font);
                textPage.addText("Max Appear Depth: " + selectedFish.getMaxAppearDepth(), font);
                textPage.addText("Sample Weight: " + selectedFish.getSampleWeight(), font);
                textPage.addText("Cave Only: " + selectedFish.isCaveOnly(), font);
                textPage.addText("Base Price: " + selectedFish.getBasePrice(), font);
                textPage.addText("Is Treasure: " + selectedFish.isTreasure(), font);
                textPage.addText("Is Junk: " + selectedFish.isJunk(), font);
                textPage.addText("Is Ultimate Treasure: " + selectedFish.isUltimateTreasure(), font);
                textPage.addText("Allowed Time: " + selectedFish.getAllowedTime(), font);
                textPage.addText("Allowed Weather: " + selectedFish.getAllowedWeather(), font);
                textPage.addText("Possible Biomes: " + String.join(", ", selectedFish.getPossibleBiomes()), font);
                textPage.addText("Possible Worlds: " + String.join(", ", selectedFish.getPossibleWorlds()), font);
                textPage.addText("Lowest Lootable Enchantment Level: " + selectedFish.getLowestLootableEnchantmentLevel(), font);
                textPage.addText("Ticks to Win: " + selectedFish.getTicksToWin(), font);
                textPage.addText("Speed Modifier: " + selectedFish.getSpeedModifier(), font);
                textPage.addText("Flat Segment Random Range: " + selectedFish.getFlatSegmentRandomRange()[0] + " - " + selectedFish.getFlatSegmentRandomRange()[1], font);
                textPage.addText("Flux Segment Random Range: " + selectedFish.getFluxSegmentRandomRange()[0] + " - " + selectedFish.getFluxSegmentRandomRange()[1], font);
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
            // todo show: select a fish item to show the details (i18n)
            // todo show: seslct a fish item to show the recorded info (i18n)
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
