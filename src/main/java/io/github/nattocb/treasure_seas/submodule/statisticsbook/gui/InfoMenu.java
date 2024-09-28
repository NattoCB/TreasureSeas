package io.github.nattocb.treasure_seas.submodule.statisticsbook.gui;

import io.github.nattocb.treasure_seas.config.FishWrapper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfoMenu extends AbstractContainerMenu {

    private final int width = 3;

    public final int visibleRows = 5;

    public final int totalRows;

    public int scrollOffset = 0;

    private static final List<ItemStack> itemList = new ArrayList<>();

    private final Container showcaseContainer;

    private final Map<String, FishWrapper> fishWrapperMap;

    private FishWrapper selectedFishWrapper;

    public CompoundTag playerNbtFishes;

    public InfoMenu(MenuType<?> type, int id, Map<String, FishWrapper> fishWrapperConfigs, CompoundTag playerNbtRecordedFishes) {

        super(type, id);

        this.fishWrapperMap = fishWrapperConfigs;
        this.playerNbtFishes = playerNbtRecordedFishes;

        if (itemList.isEmpty()) {
            fishWrapperConfigs.forEach((k, v) -> {
                ResourceLocation itemLocation = new ResourceLocation(v.getModNamespace(), v.getFishItemName());
                Item item = ForgeRegistries.ITEMS.getValue(itemLocation);
                itemList.add(new ItemStack(item));
            });
            sortByCategoryAndName();
        }

        // Calculate total rows based on item list size
        this.totalRows = (int) Math.ceil(fishWrapperConfigs.size() / (float) width);

        // Create a simple container to hold the slots
        this.showcaseContainer = new SimpleContainer(width * totalRows);

        // Add slots to the container, initially displaying items according to scrollOffset
        updateVisibleSlots();
    }

    // Method to update the visible slots based on scrollOffset
    public void updateVisibleSlots() {
        this.slots.clear();
        for (int row = 0; row < visibleRows; row++) {
            for (int col = 0; col < width; col++) {
                int index = (row + scrollOffset) * width + col;
                if (index < itemList.size()) {
                    this.addSlot(new ShowcaseSlot(showcaseContainer, index, 8 + col * 18, 18 + row * 18, this));
                    showcaseContainer.setItem(index, itemList.get(index));
                }
            }
        }
    }


    // 保存点击的FishWrapper
    public void setSelectedFishWrapper(ItemStack clickedStack) {
        String key = ForgeRegistries.ITEMS.getKey(clickedStack.getItem()).toString();
        this.selectedFishWrapper = fishWrapperMap.get(key);
    }


    // 获取被点击的FishWrapper
    public FishWrapper getSelectedFishWrapper() {
        return selectedFishWrapper;
    }

    // Handle scrolling by adjusting the scrollOffset
    public void scroll(int amount) {
        this.scrollOffset = Math.max(0, Math.min(totalRows - visibleRows, this.scrollOffset + amount));
        updateVisibleSlots();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    private static class ShowcaseSlot extends Slot {

        public ShowcaseSlot(Container container, int index, int xPosition, int yPosition, InfoMenu menu) {
            super(container, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(@NotNull Player player) {
            return false;
        }
    }

    public void sortByBasePrice() {
        itemList.sort((itemStack1, itemStack2) -> {
            FishWrapper fish1 = fishWrapperMap.get(ForgeRegistries.ITEMS.getKey(itemStack1.getItem()).toString());
            FishWrapper fish2 = fishWrapperMap.get(ForgeRegistries.ITEMS.getKey(itemStack2.getItem()).toString());
            return Integer.compare(fish1.getBasePrice(), fish2.getBasePrice());
        });
    }

    public void sortByEnchantmentLevel() {
        itemList.sort((itemStack1, itemStack2) -> {
            FishWrapper fish1 = fishWrapperMap.get(ForgeRegistries.ITEMS.getKey(itemStack1.getItem()).toString());
            FishWrapper fish2 = fishWrapperMap.get(ForgeRegistries.ITEMS.getKey(itemStack2.getItem()).toString());
            return Integer.compare(fish1.getLowestLootableEnchantmentLevel(), fish2.getLowestLootableEnchantmentLevel());
        });
    }

    public void sortByCategoryAndName() {
        itemList.sort((itemStack1, itemStack2) -> {
            FishWrapper fish1 = fishWrapperMap.get(ForgeRegistries.ITEMS.getKey(itemStack1.getItem()).toString());
            FishWrapper fish2 = fishWrapperMap.get(ForgeRegistries.ITEMS.getKey(itemStack2.getItem()).toString());
            // 定义每个类别的优先级，越小优先级越高
            int categoryPriority1 = getCategoryPriority(fish1);
            int categoryPriority2 = getCategoryPriority(fish2);
            // 先按类别优先级排序
            int categoryComparison = Integer.compare(categoryPriority1, categoryPriority2);
            if (categoryComparison == 0) {
                // 如果类别相同，按本地化名称排序
                String name1 = I18n.get(itemStack1.getDescriptionId()); // 获取本地化名称
                String name2 = I18n.get(itemStack2.getDescriptionId()); // 获取本地化名称
                return name1.compareTo(name2);
            }
            return categoryComparison;
        });
    }

    // 返回类别的优先级：Fish -> Junk -> Treasure -> Ultimate Treasure
    private int getCategoryPriority(FishWrapper fish) {
        if (fish.isUltimateTreasure()) {
            return 3; // Ultimate Treasure 优先级最低
        } else if (fish.isTreasure()) {
            return 2; // Treasure
        } else if (fish.isJunk()) {
            return 1; // Junk
        } else {
            return 0; // Fish 优先级最高
        }
    }

}