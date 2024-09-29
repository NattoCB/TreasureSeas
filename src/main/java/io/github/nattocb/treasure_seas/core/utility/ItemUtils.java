package io.github.nattocb.treasure_seas.core.utility;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemUtils {

    /**
     * Updates or inserts a lore line at the specified index.
     * If the index is within bounds, it replaces the existing line.
     * Otherwise, it inserts the lore at the specified index.
     */
    public static void updateLore(ItemStack stack, int index, Component lore) {
        ListTag loreList = getLoreList(stack);
        StringTag loreTag = StringTag.valueOf(Component.Serializer.toJson(lore));

        if (index >= 0 && index < loreList.size()) {
            // Replace existing line
            loreList.set(index, loreTag);
        } else {
            // Insert new line if index is out of bounds
            loreList.add(index, loreTag);
        }
        setLoreList(stack, loreList);
    }

    /**
     * Deletes a lore line at the specified index.
     */
    public static void deleteLore(ItemStack stack, int index) {
        ListTag loreList = getLoreList(stack);
        if (index >= 0 && index < loreList.size()) {
            loreList.remove(index);
            setLoreList(stack, loreList);
        }
    }

    /**
     * Inserts a lore line at the end.
     */
    public static void insertLoreAtEnd(ItemStack stack, Component lore) {
        ListTag loreList = getLoreList(stack);
        loreList.add(StringTag.valueOf(Component.Serializer.toJson(lore)));
        setLoreList(stack, loreList);
    }

    /**
     * Inserts a lore line at the beginning.
     */
    public static void insertLoreAtBeginning(ItemStack stack, Component lore) {
        ListTag loreList = getLoreList(stack);
        loreList.add(0, StringTag.valueOf(Component.Serializer.toJson(lore)));
        setLoreList(stack, loreList);
    }

    /**
     * Inserts a lore line at the specified index, pushing existing lines down.
     */
    public static void insertLoreAtIndex(ItemStack stack, int index, Component lore) {
        ListTag loreList = getLoreList(stack);
        if (index < 0) {
            index = 0;
        }
        if (index > loreList.size()) {
            index = loreList.size();
        }
        loreList.add(index, StringTag.valueOf(Component.Serializer.toJson(lore)));
        setLoreList(stack, loreList);
    }

    /**
     * Clears all lore from the item.
     */
    public static void clearLore(ItemStack stack) {
        setLoreList(stack, new ListTag());
    }

    // Helper method to get the lore list from the item stack
    public static ListTag getLoreList(ItemStack stack) {
        CompoundTag displayTag = stack.getOrCreateTagElement("display");
        // 8 is the tag type for strings
        return displayTag.getList("Lore", 8);
    }

    // Helper method to set the lore list to the item stack
    public static void setLoreList(ItemStack stack, ListTag loreList) {
        CompoundTag displayTag = stack.getOrCreateTagElement("display");
        displayTag.put("Lore", loreList);
    }

    /**
     * Creates an ItemStack for the specified item registry name.
     *
     * @return The created ItemStack, or null if the item was not found.
     */
    @Nullable
    public static ItemStack createItemStack(@NotNull String namespace, @NotNull String itemName, int count) {
        ResourceLocation itemResource = new ResourceLocation(namespace + ":" + itemName);
        Item item = ForgeRegistries.ITEMS.getValue(itemResource);
        if (item != null) {
            return new ItemStack(item, count);
        } else {
            return null;
        }
    }

}
