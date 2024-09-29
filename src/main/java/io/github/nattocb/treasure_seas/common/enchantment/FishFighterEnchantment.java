package io.github.nattocb.treasure_seas.common.enchantment;

import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class FishFighterEnchantment extends Enchantment {

    public FishFighterEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.FISHING_ROD, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int enchantmentLevel) {
        return 1;
    }

    @Override
    public int getMaxCost(int enchantmentLevel) {
        return 30;
    }

    @Override
    public int getMaxLevel() {
        return 1; // 附魔台只能附魔1级
    }

    @Override
    public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) && stack.getItem() instanceof FishingRodItem;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof FishingRodItem;
    }

    @Override
    public boolean isTreasureOnly() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return true;
    }
}