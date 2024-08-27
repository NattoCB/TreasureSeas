package io.github.nattocb.treasure_seas.enchantment;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.utils.FishUtils;
import io.github.nattocb.treasure_seas.PlayerMessageManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class AnvilOperationHandler {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() instanceof FishingRodItem && right.getItem() == Items.ENCHANTED_BOOK) {
            int currentRodLevel = FishUtils.getFishRodFighterEnchantLevel(left);
            int bookLevel = EnchantmentHelper.getEnchantments(right).getOrDefault(TreasureSeas.FISH_FIGHTER.get(), 0);
            if (bookLevel == 0) {
                // 不是 FISH FIGHTER 附魔，直接 copy nbt 到 output
                ItemStack newRod = new ItemStack(Items.FISHING_ROD);
                CompoundTag oldTag = left.getTag();
                if (oldTag != null) {
                    newRod.setTag(oldTag.copy());
                }
            } else {
                // 是 FISH FIGHTER 附魔，需要判断经验是否满足升级要求，和计算下一等级的 nbt 信息
                int currentCount = FishUtils.getCurrentFishingCount(left);
                if (bookLevel - currentRodLevel >= 1) {
                    int neededCounts = calculateNeededCounts(currentRodLevel);
                    if (currentCount >= neededCounts || event.getPlayer().isCreative()) {
                        event.setMaterialCost(1);
                        event.setCost(calculateExperienceCost(currentRodLevel + 1));
                        event.setOutput(upgradeRodWithNBT(left, currentRodLevel + 1));
                    } else {
                        PlayerMessageManager.sendMessageOnce(event.getPlayer(),
                                new TranslatableComponent("message.treasure_seas.insufficient_fishing_count", neededCounts - currentCount)
                        );
                        event.setCanceled(true);
                    }
                } else {
                    // 附魔书等级不足
                    event.setCanceled(true);
                }
            }
        }
    }

    private static ItemStack upgradeRodWithNBT(ItemStack oldRod, int newLevel) {
        ItemStack newRod = new ItemStack(oldRod.getItem());
        CompoundTag oldTag = oldRod.getTag();
        if (oldTag != null) {
            // 更新lore，重新计算下一级所需经验
            CompoundTag newTag = oldTag.copy();
            int count = newTag.getInt("FishingCount");
            CompoundTag displayTag = newTag.getCompound("display");
            // 8是String的NBT类型，参照 ItemUtils.getLoreList(itemStack)
            ListTag loreList = displayTag.getList("Lore", 8);
            if (!loreList.isEmpty()) {
                // 更新第0行
                int requiredExperienceForNextLvl = FishingRodUpgradeRequirement.getRequiredExperienceForLevel(newLevel);
                ListTag updatedLores = FishUtils.addFishCountLoreIntoItem(loreList, count, requiredExperienceForNextLvl);
                displayTag.put("Lore", updatedLores);
                newTag.put("display", displayTag);
                newRod.setTag(newTag);
            }
        }
        // 保留旧附魔并更新 Fish Fighter 附魔等级
        Map<Enchantment, Integer> enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(oldRod));
        enchantments.put(TreasureSeas.FISH_FIGHTER.get(), newLevel);
        EnchantmentHelper.setEnchantments(enchantments, newRod);
        return newRod;
    }

    private static int calculateNeededCounts(int currentLevel) {
        return FishingRodUpgradeRequirement.getRequiredExperienceForLevel(currentLevel);
    }

    private static int calculateExperienceCost(int nextLevel) {
        return switch (nextLevel) {
            case 1 -> 10;
            case 2 -> 15;
            case 3 -> 25;
            case 4 -> 35;
            case 5 -> 45;
            default -> Integer.MAX_VALUE;
        };
    }

}