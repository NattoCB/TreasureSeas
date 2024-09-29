package io.github.nattocb.treasure_seas.core.eventsubscriber;

import io.github.nattocb.treasure_seas.common.FishRarity;
import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 如果用模组鱼烹饪、合成，则携带 nbt 到输出物品
 */
@Mod.EventBusSubscriber(modid = TreasureSeas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FishRecipeHandler {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        handleItemTransfer(event.getCrafting(), event.getInventory());
    }

    private static void handleItemTransfer(ItemStack output, Container craftMatrix) {

        // 检查输出物品是否是 gyotaku 或者可食用的食物
        boolean isGyotaku = output.getItem() == ModItems.GYOTAKU.get();
        if (!output.isEdible() && !isGyotaku) {
            return;
        }

        FishRarity highestRarity = FishRarity.ORDINARY;
        boolean isShiny = false;
        int length = 0;
        long timestamp = 0;
        String world = "";
        String location = "";
        String fisher = "";
        ItemStack lastFishItem = null;

        // If craftMatrix is not null, we are handling a crafting event
        if (craftMatrix != null) {
            for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
                ItemStack input = craftMatrix.getItem(i);
                // todo nbt without modid, need to add another layer
                CompoundTag tag = input.getTag();
                if (tag != null) {
                    // for food
                    if (tag.contains("rarity")) {
                        FishRarity rarity = FishRarity.valueOf(tag.getString("rarity"));
                        if (rarity.ordinal() > highestRarity.ordinal()) {
                            highestRarity = rarity;
                        }
                    }
                    if (tag.getBoolean("isShiny")) {
                        isShiny = true;
                    }
                    // for gyotaku
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
                if (input.getItem() != Items.AIR && input.getItem() != Items.INK_SAC && input.getItem() != Items.PAPER) {
                    lastFishItem = input;
                }
            }

            // Transfer the NBT data to the output item
            CompoundTag outputTag = output.getOrCreateTag();
            outputTag.putString("rarity", highestRarity.name());
            outputTag.putBoolean("isShiny", isShiny);
            outputTag.putString("rarity", highestRarity.name());
            outputTag.putBoolean("isShiny", isShiny);
            if (isGyotaku) {
                outputTag.putInt("length", length);
                outputTag.putLong("timestamp", timestamp);
                outputTag.putString("world", world);
                outputTag.putString("location", location);
                outputTag.putString("fisher", fisher);
            }

            // Update lore based on nbt
            // todo check other withStyle issues for i18n formatting
            ListTag lore = getLore(outputTag);
            lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.quality", highestRarity.getName()).withStyle(ChatFormatting.GRAY))));
            if (isShiny) {
                lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.shiny"))));
            }
            // Update lore for Gyotaku item
            if (isGyotaku) {
                if (lastFishItem != null) {
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.species", lastFishItem.getItem().getRegistryName().getPath()).withStyle(ChatFormatting.GRAY))));
                }
                if (timestamp > 0) {
                    Date date = new Date(timestamp);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateString = formatter.format(date);
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.timestamp", dateString).withStyle(ChatFormatting.GRAY))));
                }
                if (length > 0) {
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.length", length).withStyle(ChatFormatting.GRAY))));
                }
                if (!StringUtils.isEmpty(world)) {
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.world", world).withStyle(ChatFormatting.GRAY))));
                }
                if (!StringUtils.isEmpty(location)) {
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.location", location).withStyle(ChatFormatting.GRAY))));
                }
                if (!StringUtils.isEmpty(fisher)) {
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.fisher", fisher).withStyle(ChatFormatting.GRAY))));
                }
            }
            output.getOrCreateTagElement("display").put("Lore", lore);
        }
    }

    @NotNull
    private static ListTag getLore(CompoundTag outputTag) {
        ListTag lore;
        if (outputTag.contains("display") && outputTag.getCompound("display").contains("Lore")) {
            // 8 is the ID for StringTag
            lore = outputTag.getCompound("display").getList("Lore", 8);
        } else {
            lore = new ListTag();
        }
        return lore;
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
