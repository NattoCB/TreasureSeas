package io.github.nattocb.treasure_seas.eventsubscriber;

import io.github.nattocb.treasure_seas.FishRarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemSmeltedEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 如果用模组鱼烹饪、合成，则携带 nbt 到输出物品
 */

@Mod.EventBusSubscriber
public class FishRecipeHandler {
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        handleItemTransfer(event.getCrafting(), event.getInventory());
    }

    @SubscribeEvent
    public static void onItemSmelted(ItemSmeltedEvent event) {
        ItemStack smeltedItem = event.getSmelting();
        handleItemTransfer(smeltedItem, null);
    }

    private static void handleItemTransfer(ItemStack output, Container craftMatrix) {
        // 检查输出物品是否可食用，如果不可食用则返回
        if (!output.isEdible()) {
            return;
        }
        FishRarity highestRarity = FishRarity.ORDINARY;
        boolean isShiny = false;

        // If craftMatrix is not null, we are handling a crafting event
        if (craftMatrix != null) {
            for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
                ItemStack input = craftMatrix.getItem(i);
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
                }
            }
        } else {
            // Handle smelting event; assume the output was directly influenced by a single input
            CompoundTag tag = output.getTag();
            if (tag != null) {
                if (tag.contains("rarity")) {
                    highestRarity = FishRarity.valueOf(tag.getString("rarity"));
                }
                if (tag.getBoolean("isShiny")) {
                    isShiny = true;
                }
            }
        }

        // Transfer the NBT data to the output item
        CompoundTag outputTag = output.getOrCreateTag();
        outputTag.putString("rarity", highestRarity.name());
        outputTag.putBoolean("isShiny", isShiny);
        // Update lore based on nbt
        ListTag lore;
        if (outputTag.contains("display") && outputTag.getCompound("display").contains("Lore")) {
            // 8 is the ID for StringTag
            lore = outputTag.getCompound("display").getList("Lore", 8);
        } else {
            lore = new ListTag();
        }
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.quality", highestRarity.getName()))));
        if (isShiny) {
            lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.shiny"))));
        }
        output.getOrCreateTagElement("display").put("Lore", lore);
    }

    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntityLiving();
        ItemStack itemStack = event.getItem();
        if (entity instanceof Player && itemStack.isEdible()) {
            CompoundTag tag = itemStack.getTag();
            if (tag != null && tag.contains("rarity")) {
                FishRarity rarity = FishRarity.valueOf(tag.getString("rarity"));
                int additionalFoodValue = (int) ((rarity.ordinal() + 1) * 0.5);
                Player player = (Player) entity;
                player.getFoodData().eat(additionalFoodValue, 0.0F);
            }
        }
    }

}
