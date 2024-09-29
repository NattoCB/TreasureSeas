package io.github.nattocb.treasure_seas.core.utility;

import io.github.nattocb.treasure_seas.common.FluidShape;
import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.core.FishWrapper;
import io.github.nattocb.treasure_seas.common.RewardType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FishUtils {

    /**
     * 获取 fishingHook 鱼漂 location 下方的水位深度格数
     * 即：鱼漂到它正下方海底的 y 距离
     *
     * @param bobberPos 鱼漂位置
     * @param level     世界实例
     * @return 此处的海洋深度
     */
    public static int calculateFluidDepth(@NotNull BlockPos bobberPos, @NotNull Level level) {
        int depth = 0;
        BlockPos currentPos = bobberPos;
        BlockState blockState = level.getBlockState(currentPos);
        FluidState fluidState = blockState.getFluidState();
        while (!fluidState.isEmpty()) {
            depth++;
            currentPos = currentPos.below();
            blockState = level.getBlockState(currentPos);
            fluidState = blockState.getFluidState();
        }
        return depth;
    }

    @NotNull
    public static FishWrapper.AllowedTime getCurrentTimeEnum(@NotNull Level world) {
        LevelData levelData = world.getLevelData();
        long timeOfDay = levelData.getDayTime() % 24000;
        FishWrapper.AllowedTime currentTime;
        if (timeOfDay >= 0 && timeOfDay < 6000) {
            currentTime = FishWrapper.AllowedTime.MORNING;
        } else if (timeOfDay >= 6000 && timeOfDay < 12000) {
            currentTime = FishWrapper.AllowedTime.AFTERNOON;
        } else if (timeOfDay >= 12000 && timeOfDay < 18000) {
            currentTime = FishWrapper.AllowedTime.EVENING;
        } else {
            currentTime = FishWrapper.AllowedTime.NIGHT;
        }
        return currentTime;
    }

    @NotNull
    public static FishWrapper.AllowedWeather getCurrentWeatherEnum(@NotNull Level world) {
        FishWrapper.AllowedWeather currentWeather;
        if (world.isThundering()) {
            currentWeather = FishWrapper.AllowedWeather.STORM;
        } else if (world.isRaining()) {
            currentWeather = FishWrapper.AllowedWeather.RAIN;
        } else {
            currentWeather = FishWrapper.AllowedWeather.CLEAR;
        }
        return currentWeather;
    }

    @Nullable
    public static String getBiomeFullName(@NotNull ItemFishedEvent event, @NotNull Level world) {
        Biome biome = world.getBiome(event.getHookEntity().getOnPos()).value();
        ResourceLocation biomeName = world.registryAccess().registryOrThrow(ForgeRegistries.Keys.BIOMES).getKey(biome);
        if (biomeName == null) {
            TreasureSeas.getLogger().error("getBiomeFullName error: biomeName is null, biome: " + biome);
            return null;
        }
        String biomeNamespace = biomeName.getNamespace();
        String biomePath = biomeName.getPath();
        return String.format("%s:%s", biomeNamespace, biomePath);
    }

    /**
     * select fish based on weights from the matched fishes list
     */
    @NotNull
    public static FishWrapper chooseFishBySampleWeight(Player player, @NotNull List<FishWrapper> matchedFishes) {
        // deep clone to prevent modify global config's data on the blow logic
        List<FishWrapper> localFishes = CollectionUtils.deepClone(matchedFishes);
        // existence check
        if (localFishes.isEmpty()) {
            TreasureSeas.getLogger().dev("Matched fish: empty, use default fish");
            return TreasureSeas.getInstance().getFishConfigManager().getDefaultFishConfig();
        } else {
            // logs
            StringBuilder sb = new StringBuilder();
            sb.append("Matched fishes: ");
            localFishes.forEach(fish ->
                    sb.append(fish.getModNamespace()).append(":").append(fish.getFishItemName()).append(", ")
            );
            TreasureSeas.getLogger().dev(sb.substring(0, sb.length() - 2));
        }
        // decrease the weight for the fish that higher than rod's lvl
        int fishFighterRodEnchantLevel = FishUtils.getFishFighterRodEnchantLevel(player);
        localFishes.forEach(fish -> {
            int lvlDiff = fish.getLowestLootableEnchantmentLevel() - fishFighterRodEnchantLevel;
            if (lvlDiff > 0) {
                fish.setSampleWeight((int) (fish.getSampleWeight() * Math.max(0, 1 - (0.25 * lvlDiff))));
            }
        });
        // random sampling based on weights
        int totalWeight = localFishes.stream().mapToInt(FishWrapper::getSampleWeight).sum();
        if (totalWeight <= 0) {
            TreasureSeas.getLogger().warn("Matched fish: total weight must be positive");
            return TreasureSeas.getInstance().getFishConfigManager().getDefaultFishConfig();
        }
        int randomWeight = TreasureSeas.RANDOM.nextInt(totalWeight);
        int currentWeight = 0;
        for (FishWrapper fish : localFishes) {
            currentWeight += fish.getSampleWeight();
            if (randomWeight < currentWeight) {
                return fish;
            }
        }
        return TreasureSeas.getInstance().getFishConfigManager().getDefaultFishConfig();
    }

    /**
     * 获取玩家手持鱼竿的鱼之战斗附魔等级
     */
    public static int getFishFighterRodEnchantLevel(@NotNull Player player) {
        ItemStack heldItem = getFishRodItemFromInv(player);
        if (heldItem == null) return 0;
        return EnchantmentHelper.getItemEnchantmentLevel(TreasureSeas.FISH_FIGHTER.get(), heldItem);
    }

    public static int getFishFighterRodEnchantLevel(@NotNull ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(TreasureSeas.FISH_FIGHTER.get(), itemStack);
    }

    /**
     * 获取玩家手持的鱼竿 itemStack 对象
     */
    @Nullable
    public static ItemStack getFishRodItemFromInv(@NotNull Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof FishingRodItem) {
            return heldItem;
        } else {
            heldItem = player.getOffhandItem();
            if (heldItem.getItem() instanceof FishingRodItem) {
                return heldItem;
            } else {
                return null;
            }
        }
    }

    /**
     * 根据鱼竿附魔等级判断鱼竿的最大可垂钓深度（米）
     */
    public static int getRodDepthCapacity(int enchantLevel) {
        int result;
        switch (enchantLevel) {
            case 0 -> result = 0;
            case 1 -> result = 5;
            case 2 -> result = 15;
            case 3 -> result = 25;
            case 4 -> result = 40;
            case 5 -> result = 75;
            default -> {
                if (enchantLevel > 5) {
                    result = 75 + (enchantLevel - 5) * 10;
                } else {
                    result = -1;
                }
            }
        }
        return result;
    }

    /**
     * 判断给定的BlockPos是否位于海平面以下且没有太阳直射。
     *
     * @param world 世界对象。
     * @param pos   要检查的BlockPos。
     * @return 如果块位于海平面以下且没有太阳直射，返回true，否则返回false。
     */
    public static boolean isCave(@NotNull Level world, @NotNull BlockPos pos) {
        // 获取海平面的高度
        int seaLevel = world.getSeaLevel();
        // 检查位置是否在海平面以下 5 格
        if (pos.getY() > seaLevel - 5) {
            return false;
        }
        // 检查该位置上方是否有不可透光的方块
        BlockPos abovePos = pos.above();
        while (abovePos.getY() < world.getMaxBuildHeight()) {
            BlockState blockState = world.getBlockState(abovePos);
            // 检查当前位置上方是否有不可透光方块
            if (blockState.isSolidRender(world, abovePos)) {
                return true;
            }
            abovePos = abovePos.above();
        }
        // 如果上方没有发现不可透光的方块，返回false
        return false;
    }

    /**
     * Retrieves the current fishing count from the fishing rod's NBT tag.
     * Does not create a new NBT tag if the rod does not have the Fish Fighter enchantment.
     *
     * @param fishRod the fishing rod item stack
     * @return the current fishing count or 0 if no count is found or if the rod does not have the enchantment
     */
    public static int getCurrentFishingCount(@Nullable ItemStack fishRod) {
        if (fishRod == null || fishRod.isEmpty()) {
            return 0;
        }

        // Check if the fishing rod has the Fish Fighter enchantment
        if (EnchantmentHelper.getEnchantments(fishRod).containsKey(TreasureSeas.FISH_FIGHTER.get())) {
            CompoundTag tag = fishRod.getTag();
            if (tag != null && tag.contains("FishingCount")) {
                return tag.getInt("FishingCount");
            }
        }

        return 0;
    }

    /**
     * 各 FISH FIGHTER 附魔等级下的 TREASURE、JUNK、FISH、ULTIMATE 几率
     */
    private static final Map<Integer, List<Double>> rewardTypeProbabilitiesForCommonWorlds = new HashMap<>();
    private static final Map<Integer, List<Double>> rewardTypeProbabilitiesForNether = new HashMap<>();

    static {
        rewardTypeProbabilitiesForCommonWorlds.put(1, Arrays.asList(5.0, 10.0, 84.8, 0.2));
        rewardTypeProbabilitiesForCommonWorlds.put(2, Arrays.asList(5.0, 8.0, 86.6, 0.4));
        rewardTypeProbabilitiesForCommonWorlds.put(3, Arrays.asList(5.0, 6.0, 88.2, 0.8));
        rewardTypeProbabilitiesForCommonWorlds.put(4, Arrays.asList(5.0, 4.0, 89.8, 1.2));
        rewardTypeProbabilitiesForCommonWorlds.put(5, Arrays.asList(5.0, 2.0, 90.5, 2.5));

        rewardTypeProbabilitiesForNether.put(1, Arrays.asList(5.0, 65.0, 29.8, 0.2));
        rewardTypeProbabilitiesForNether.put(2, Arrays.asList(6.0, 57.0, 36.2, 0.8));
        rewardTypeProbabilitiesForNether.put(3, Arrays.asList(8.0, 48.0, 42.2, 1.8));
        rewardTypeProbabilitiesForNether.put(4, Arrays.asList(9.0, 42.0, 46.8, 2.2));
        rewardTypeProbabilitiesForNether.put(5, Arrays.asList(9.0, 33.0, 53.0, 5.0));
    }

    @NotNull
    public static RewardType getRandomRewardType(@NotNull Player player, @NotNull Level world, BlockPos hookPos, int value) {
        if (!rewardTypeProbabilitiesForCommonWorlds.containsKey(value)) {
            TreasureSeas.getLogger().error("Fish fighter enchant level could not be lower than 1 or greater than 5");
            return RewardType.FISH;
        }
        double chance = TreasureSeas.RANDOM.nextDouble() * 100;

        // 根据水域形状类型决定 chance 前后移比例
        FluidShape fluidShape = FluidShapeHelper.getFluidShape(world, hookPos);
        switch (fluidShape) {
            case NARROW -> chance = Math.max(0, chance - 12);
            case PONDLET -> chance = Math.max(0, chance - 5);
            case WELL -> chance = Math.max(0, chance - 2.5);
            case POND -> chance = Math.max(0, chance - 1);
            case SURFACE -> chance = Math.max(0, chance - 0.5);
            case NEAR_SHORE -> chance = Math.max(0, chance - 2);
            case OPEN_WATER -> chance = Math.min(100, chance + 1);
            case HOLE -> chance = Math.min(100, chance + 1.5);
            case UNKNOWN -> {
            }
        }

        List<Double> probs;
        if (world.dimension() == Level.NETHER) {
            probs = rewardTypeProbabilitiesForNether.get(value);
        } else {
            probs = rewardTypeProbabilitiesForCommonWorlds.get(value);
        }
        RewardType result = RewardType.FISH;
        double cumulativeProbability = 0.0;
        for (int i = 0; i < probs.size(); i++) {
            cumulativeProbability += probs.get(i);
            if (chance < cumulativeProbability) {
                TreasureSeas.getLogger().dev("reward type: " + RewardType.values()[i]);
                result = RewardType.values()[i];
                break;
            }
        }

        // 根据 luck of the sea 随机将 JUNK 转换为 TREASURE 类型
        ItemStack fishRod = getFishRodItemFromInv(player);
        if (result == RewardType.JUNK && fishRod != null) {
            int luckLvl = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FISHING_LUCK, fishRod);
            boolean convertJunkToTreasure = convertJunkToTreasure(luckLvl);
            if (convertJunkToTreasure) {
                return RewardType.TREASURE;
            }
        }

        return result;
    }

    // 传入海之眷顾等级，返回 true 如果本次将 junk 转换为 treasure
    private static boolean convertJunkToTreasure(int luckOfTheSeaLevel) {
        double chance;
        switch (luckOfTheSeaLevel) {
            case 1 -> chance = 0.021;
            case 2 -> chance = 0.042;
            case 3 -> chance = 0.063;
            default -> chance = 0.00;
        }
        return TreasureSeas.RANDOM.nextDouble() < chance;
    }

    @NotNull
    public static ListTag addFishCountLoreIntoItem(@NotNull ListTag loreList, int count, int nextLvlExp) {
        StringTag countLore;
        if (count >= nextLvlExp) {
            countLore = StringTag.valueOf(
                    Component.Serializer.toJson(
                            new TranslatableComponent("lore.treasure_seas.fishing_count_upgradable", "§6" + count + "/" + nextLvlExp)
                    )
            );
        } else if (Integer.MAX_VALUE != nextLvlExp) {
            countLore = StringTag.valueOf(
                    Component.Serializer.toJson(
                            new TranslatableComponent("lore.treasure_seas.fishing_count", "§8" + count + "/" + nextLvlExp)
                    )
            );
        } else {
            countLore = StringTag.valueOf(
                    Component.Serializer.toJson(
                            new TranslatableComponent("lore.treasure_seas.fishing_count", "§8" + count)
                    )
            );
        }
        if (loreList.isEmpty()) {
            loreList.add(countLore);
        } else {
            loreList.set(0, countLore);
        }
        return loreList;
    }

    public static boolean isPlayerFishing(@NotNull Player player) {
        return player.fishing != null && player.fishing.isAlive();
    }

    /**
     * 获取玩家正在使用的鱼钩实体（钓鱼漂）。
     *
     * @param player 玩家实例
     * @return 如果玩家正在钓鱼，返回钓鱼漂实体；否则返回 null。
     */
    public static FishingHook getPlayerFishingHook(Player player) {
        return player.fishing;
    }

    // Retrieve the maxLength from the player's NBT data
    public static int getFishMaxRecordedLength(CompoundTag treasureSeasTag, FishWrapper fishWrapper) {
        if (treasureSeasTag == null || treasureSeasTag.isEmpty()) {
            return 0;
        }
        String fishKey = fishWrapper.getModNamespace() + ":" + fishWrapper.getFishItemName();
        if (treasureSeasTag.contains(fishKey)) {
            CompoundTag fishTag = treasureSeasTag.getCompound(fishKey);
            return fishTag.getInt("maxLength");
        }
        return 0;
    }

    // Retrieve the shiny status from the player's NBT data
    public static boolean isFishShiny(CompoundTag treasureSeasTag, FishWrapper fishWrapper) {
        if (treasureSeasTag == null || treasureSeasTag.isEmpty()) {
            return false;
        }
        String fishKey = fishWrapper.getModNamespace() + ":" + fishWrapper.getFishItemName();
        if (treasureSeasTag.contains(fishKey)) {
            CompoundTag fishTag = treasureSeasTag.getCompound(fishKey);
            return fishTag.getBoolean("isShiny");
        }
        return false;
    }

    // Retrieve the catch count from the player's NBT data
    public static int getFishCatchCount(CompoundTag treasureSeasTag, FishWrapper fishWrapper) {
        if (treasureSeasTag == null || treasureSeasTag.isEmpty()) {
            return 0;
        }
        String fishKey = fishWrapper.getModNamespace() + ":" + fishWrapper.getFishItemName();
        if (treasureSeasTag.contains(fishKey)) {
            CompoundTag fishTag = treasureSeasTag.getCompound(fishKey);
            return fishTag.getInt("cnt");
        }
        return 0;
    }

    public static boolean isFish(FishWrapper fishWrapper) {
        return !(fishWrapper.isJunk() || fishWrapper.isTreasure() || fishWrapper.isUltimateTreasure());
    }

}
