package io.github.nattocb.treasure_seas.common.recipe;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.common.FishRarity;
import io.github.nattocb.treasure_seas.common.registry.ModRecipeSerializers;
import io.github.nattocb.treasure_seas.common.registry.ModRecipeTags;
import io.github.nattocb.treasure_seas.core.utility.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GyotakuShapelessRecipe extends ShapelessRecipe {


    public GyotakuShapelessRecipe(ResourceLocation id,
                                  String group,
                                  ItemStack result,
                                  NonNullList<Ingredient> ingredients) {
        super(id, group, result, ingredients);
    }
    @Override
    public boolean matches(CraftingContainer inv, @NotNull Level world) {

        boolean hasPaper = false;
        boolean hasFish = false;
        boolean hasInkSac = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(Items.PAPER)) {
                hasPaper = true;
            } else if (stack.is(Items.INK_SAC)) {
                hasInkSac = true;
            } else if (stack.is(ModRecipeTags.FISHES)) {
                hasFish = true;
            }
        }

        return hasPaper && hasInkSac && hasFish;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer inv) {
        ItemStack output = getResultItem().copy();
        FishRarity highestRarity = FishRarity.ORDINARY;
        boolean isShiny = false;
        int length = 0;
        long timestamp = 0;
        String world = "";
        String location = "";
        String fisher = "";
        ItemStack lastFishItem = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack input = inv.getItem(i);
            CompoundTag tag = input.getTag();
            if (tag != null) {
                if (tag.contains("rarity")) {
                    FishRarity rarity = FishRarity.valueOf(tag.getString("rarity"));
                    if (rarity.ordinal() > highestRarity.ordinal()) {
                        highestRarity = rarity;
                    }
                }
                if (tag.getBoolean("isShiny")) {
                    isShiny = true;
                }
                if (tag.contains("length")) {
                    length = tag.getInt("length");
                }
                if (tag.contains("timestamp")) {
                    timestamp = tag.getLong("timestamp");
                }
                if (tag.contains("world")) {
                    world = tag.getString("world");
                }
                if (tag.contains("location")) {
                    location = tag.getString("location");
                }
                if (tag.contains("fisher")) {
                    fisher = tag.getString("fisher");
                }
            }
            if (input.is(ModRecipeTags.FISHES)) {
                lastFishItem = input;
            }
        }

        // 使用工具类更新物品的 Lore
        ItemUtils.updateLore(output, 0, new TranslatableComponent("fish.quality", highestRarity.getName()).withStyle(ChatFormatting.GRAY));
        if (isShiny) {
            ItemUtils.insertLoreAtEnd(output, new TranslatableComponent("fish.shiny"));
        }
        if (!lastFishItem.isEmpty()) {
            ItemUtils.insertLoreAtEnd(output, new TranslatableComponent("fish.species", lastFishItem.getItem().getRegistryName().getPath()).withStyle(ChatFormatting.GRAY));
        }
        if (timestamp > 0) {
            Date date = new Date(timestamp);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(date);
            ItemUtils.insertLoreAtEnd(output, new TranslatableComponent("fish.timestamp", dateString).withStyle(ChatFormatting.GRAY));
        }
        if (length > 0) {
            ItemUtils.insertLoreAtEnd(output, new TranslatableComponent("fish.length", length).withStyle(ChatFormatting.GRAY));
        }
        if (!world.isEmpty()) {
            ItemUtils.insertLoreAtEnd(output, new TranslatableComponent("fish.world", world).withStyle(ChatFormatting.GRAY));
        }
        if (!location.isEmpty()) {
            ItemUtils.insertLoreAtEnd(output, new TranslatableComponent("fish.location", location).withStyle(ChatFormatting.GRAY));
        }
        if (!fisher.isEmpty()) {
            ItemUtils.insertLoreAtEnd(output, new TranslatableComponent("fish.fisher", fisher).withStyle(ChatFormatting.GRAY));
        }

        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

}
