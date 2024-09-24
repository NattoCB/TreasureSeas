package io.github.nattocb.treasure_seas.utils.random;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomPotionUtil {

    // 定义不同难度的权重值
    private static final int[] WEIGHTS = {7, 8, 9};  // 1级、2级、3级的权重
    // 定义可用的持续时间数组
    private static final int[] DURATIONS = {15, 30, 45, 60, 120, 180, 300};  // 持续时间列表
    // 随机数生成器
    private static final Random RANDOM = new Random();

    /**
     * 根据输入等级生成一个随机药水，并根据传入的ItemStack类型（普通、喷溅、滞留）返回相同类型的药水。
     * @param inputStack 传入的药水类型ItemStack (Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION)
     * @param level 需要的药水难度等级(1~3)，3为最高
     * @return 返回一个构造好的同类型药水ItemStack
     */
    public static ItemStack getRandomPotion(ItemStack inputStack, int level) {
        List<PotionEntry> potionPool = new ArrayList<>();

        // 遍历所有已注册的药水效果，构建药水池
        for (Potion potion : ForgeRegistries.POTIONS.getValues()) {
            int potionDifficulty = getPotionDifficulty(potion);

            // 确保药水难度 <= 传入的等级
            if (potionDifficulty <= level) {
                int weight = WEIGHTS[potionDifficulty - 1]; // 获取对应难度的权重
                potionPool.add(new PotionEntry(potion, weight));
            }
        }

        // 根据权重进行随机选择
        PotionEntry selectedEntry = getRandomWeightedPotion(potionPool);

        // 随机选择一个时间段
        int maxDuration = getMaxDurationForLevel(level);
        int randomDuration = getRandomDuration(maxDuration);

        // 创建自定义 MobEffectInstance，带有随机化的持续时间
        List<MobEffectInstance> customEffects = new ArrayList<>();
        for (MobEffectInstance effectInstance : selectedEntry.potion.getEffects()) {
            // 复制并修改持续时间
            customEffects.add(new MobEffectInstance(effectInstance.getEffect(), randomDuration * 20, effectInstance.getAmplifier()));
        }

        // 创建新的 ItemStack
        ItemStack outputStack;
        if (inputStack.getItem() == Items.SPLASH_POTION) {
            outputStack = new ItemStack(Items.SPLASH_POTION);  // 喷溅药水
        } else if (inputStack.getItem() == Items.LINGERING_POTION) {
            outputStack = new ItemStack(Items.LINGERING_POTION);  // 滞留药水
        } else {
            outputStack = new ItemStack(Items.POTION);  // 普通药水
        }

        // 将自定义的效果应用到药水中
        PotionUtils.setCustomEffects(outputStack, customEffects);

        // 返回新的 ItemStack，包含指定效果
        return outputStack;
    }

    /**
     * 获取传入等级允许的最大持续时间
     * @param level 输入的难度等级
     * @return 最大持续时间（秒）
     */
    private static int getMaxDurationForLevel(int level) {
        if (level == 1) {
            return 60; // 1级最高只能返回60秒内的药水
        } else if (level == 2) {
            return 180; // 2级最高返回180秒的药水
        } else {
            return 300; // 3级最高返回300秒的药水
        }
    }

    /**
     * 根据最大持续时间，随机返回一个可用的持续时间
     * @param maxDuration 最大持续时间（秒）
     * @return 随机持续时间
     */
    private static int getRandomDuration(int maxDuration) {
        List<Integer> validDurations = new ArrayList<>();
        for (int duration : DURATIONS) {
            if (duration <= maxDuration) {
                validDurations.add(duration);
            }
        }
        return validDurations.get(RANDOM.nextInt(validDurations.size()));
    }

    /**
     * 从权重药水池中随机选择一个药水项。
     * @param potionPool 药水池，包含药水及其对应的权重
     * @return 随机选中的药水项
     */
    private static PotionEntry getRandomWeightedPotion(List<PotionEntry> potionPool) {
        int totalWeight = potionPool.stream().mapToInt(entry -> entry.weight).sum();
        int randomValue = RANDOM.nextInt(totalWeight);  // 随机生成0到总权重之间的值

        for (PotionEntry entry : potionPool) {
            randomValue -= entry.weight;
            if (randomValue < 0) {
                return entry; // 根据权重返回一个随机药水
            }
        }

        // 如果意外没有找到，返回默认（防御性编程）
        return potionPool.get(0);
    }

    /**
     * 获取药水的难度级别（自定义规则）
     * @param potion 药水对象
     * @return 药水难度（1~3）
     */
    private static int getPotionDifficulty(Potion potion) {
        // 这里可以自定义每个药水的难度判断逻辑
        if (potion == Potions.WATER_BREATHING || potion == Potions.SWIFTNESS) {
            return 1;  // 这些药水比较简单
        } else if (potion == Potions.INVISIBILITY || potion == Potions.REGENERATION) {
            return 2;  // 中等难度
        } else {
            return 3;  // 复杂或稀有的药水
        }
    }

    /**
     * 用于保存药水及其权重的内部类
     */
    private static class PotionEntry {
        public final Potion potion;
        public final int weight;

        public PotionEntry(Potion potion, int weight) {
            this.potion = potion;
            this.weight = weight;
        }
    }
}
