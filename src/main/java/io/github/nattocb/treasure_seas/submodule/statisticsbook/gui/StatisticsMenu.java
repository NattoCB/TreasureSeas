package io.github.nattocb.treasure_seas.submodule.statisticsbook.gui;

import io.github.nattocb.treasure_seas.config.FishWrapper;
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
import java.util.HashMap;
import java.util.List;

public class StatisticsMenu extends AbstractContainerMenu {

    // todo add sort function for: recommended level, name&mod, price
    // todo change required level to recommended level

    private final int width = 3;
    public final int visibleRows = 6;
    public final int totalRows;
    public int scrollOffset = 0;

    private static final List<ItemStack> itemList = new ArrayList<>();
    private final Container showcaseContainer;

    public StatisticsMenu(MenuType<?> type, int id, HashMap<String, FishWrapper> fishWrapperMap) {
        super(type, id);
        if (itemList.isEmpty()) {
            fishWrapperMap.forEach((k, v) -> {
                ResourceLocation itemLocation = new ResourceLocation(v.getModNamespace(), v.getFishItemName());
                Item item = ForgeRegistries.ITEMS.getValue(itemLocation);
                itemList.add(new ItemStack(item));
            });
        }

        // Calculate total rows based on item list size
        this.totalRows = (int) Math.ceil(fishWrapperMap.size() / (float) width);

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
                    this.addSlot(new ShowcaseSlot(showcaseContainer, index, 8 + col * 18, 18 + row * 18));
                    showcaseContainer.setItem(index, itemList.get(index));
                }
            }
        }
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
        public ShowcaseSlot(Container container, int index, int xPosition, int yPosition) {
            super(container, index, xPosition, yPosition);
        }
        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }
        @Override
        public boolean mayPickup(@NotNull Player player) {
            // todo show fish info on the right side
            return false;
        }
    }

}