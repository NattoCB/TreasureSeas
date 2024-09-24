package io.github.nattocb.treasure_seas.utils.random;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomEnchantmentUtil {

    // 定义不同稀有度的权重修正系数
    private static final int COMMON_WEIGHT = 10;
    private static final int UNCOMMON_WEIGHT = 8;
    private static final int RARE_WEIGHT = 6;
    private static final int VERY_RARE_WEIGHT = 4;

    // 基础权重值，代表附魔的等级
    private static final int[] LEVEL_WEIGHTS = {7, 8, 9, 10}; // 1级、2级、3级、4级的权重

    // 随机数生成器
    private static final Random RANDOM = new Random();

    /**
     * 根据输入等级生成一个随机附魔书，支持返回比该等级低的附魔书。
     * @param level 需要的附魔稀有度等级(1~4)，4为最高
     * @return 随机生成的附魔书ItemStack
     */
    public static ItemStack getRandomEnchantmentBook(int level) {
        List<EnchantmentEntry> enchantmentPool = new ArrayList<>();

        // 遍历所有附魔，构建附魔池
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
            Enchantment.Rarity rarity = enchantment.getRarity();
            int enchantMaxLevel = enchantment.getMaxLevel();

            // 根据最大等级和稀有度，加入不同等级的附魔
            for (int i = 1; i <= enchantMaxLevel; i++) {
                // 确保附魔等级 <= 传入等级
                if (i <= level) {
                    // 基于稀有度调整权重
                    int weight = getRarityWeight(rarity) + LEVEL_WEIGHTS[i - 1];
                    enchantmentPool.add(new EnchantmentEntry(enchantment, i, weight));
                }
            }
        }

        // 根据权重进行随机选择
        EnchantmentEntry selectedEntry = getRandomWeightedEnchantment(enchantmentPool);

        // 创建并返回附魔书ItemStack
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        Map<Enchantment, Integer> enchantMap = Map.of(selectedEntry.enchantment, selectedEntry.level);
        EnchantmentHelper.setEnchantments(enchantMap, enchantedBook);

        // 返回附魔书
        return enchantedBook;
    }

    /**
     * 从权重附魔池中随机选择一个附魔项。
     * @param enchantmentPool 附魔池，包含附魔及其对应的权重
     * @return 随机选中的附魔项
     */
    private static EnchantmentEntry getRandomWeightedEnchantment(List<EnchantmentEntry> enchantmentPool) {
        int totalWeight = enchantmentPool.stream().mapToInt(entry -> entry.weight).sum();
        int randomValue = RANDOM.nextInt(totalWeight);  // 随机生成0到总权重之间的值

        for (EnchantmentEntry entry : enchantmentPool) {
            randomValue -= entry.weight;
            if (randomValue < 0) {
                return entry; // 根据权重返回一个随机附魔
            }
        }

        // 如果意外没有找到，返回默认（防御性编程）
        return enchantmentPool.get(0);
    }

    /**
     * 根据附魔稀有度返回对应的权重修正值。
     * @param rarity 附魔的稀有度
     * @return 权重修正值
     */
    private static int getRarityWeight(Enchantment.Rarity rarity) {
        switch (rarity) {
            case COMMON:
                return COMMON_WEIGHT;
            case UNCOMMON:
                return UNCOMMON_WEIGHT;
            case RARE:
                return RARE_WEIGHT;
            case VERY_RARE:
                return VERY_RARE_WEIGHT;
            default:
                return 0;
        }
    }

    /**
     * 用于保存附魔及其权重的内部类
     */
    private static class EnchantmentEntry {
        public final Enchantment enchantment;
        public final int level;
        public final int weight;

        public EnchantmentEntry(Enchantment enchantment, int level, int weight) {
            this.enchantment = enchantment;
            this.level = level;
            this.weight = weight;
        }
    }
}
