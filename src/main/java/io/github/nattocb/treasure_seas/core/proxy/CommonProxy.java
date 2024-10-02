package io.github.nattocb.treasure_seas.core.proxy;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.common.FishGender;
import io.github.nattocb.treasure_seas.core.FishWrapper;
import io.github.nattocb.treasure_seas.common.FishingRodUpgradeRequirement;
import io.github.nattocb.treasure_seas.common.item.FireproofItemEntity;
import io.github.nattocb.treasure_seas.core.packet.PacketHandler;
import io.github.nattocb.treasure_seas.common.FishRarity;
import io.github.nattocb.treasure_seas.core.utility.AchievementHelper;
import io.github.nattocb.treasure_seas.core.utility.FishUtils;
import io.github.nattocb.treasure_seas.core.utility.ItemUtils;
import io.github.nattocb.treasure_seas.core.utility.MathUtils;
import io.github.nattocb.treasure_seas.core.utility.random.RandomEnchantmentUtil;
import io.github.nattocb.treasure_seas.core.utility.random.RandomPotionUtil;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.*;

/**
 * Server-side logics
 */
public class CommonProxy {


    public CommonProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    public void handleFightingResult(
            final NetworkEvent.Context ctx,
            final Vec3 bobberPos,
            final boolean success,
            final FishWrapper fishWrapper,
            final boolean isRaining,
            final boolean isThundering,
            final boolean isNightTime) {

        final ServerPlayer player = ctx.getSender();
        if (player == null) return;
        if (success) {

            // gen itemStack
            String namespace = fishWrapper.getModNamespace();
            String itemName = fishWrapper.getFishItemName();
            ItemStack itemStack = ItemUtils.createItemStack(namespace, itemName, 1);
            if (itemStack == null) {
                player.sendMessage(new TextComponent(String.format("Fish item %s:%s not found, please check your config and modlist", namespace, itemName)), player.getUUID());
                return;
            }

            // handle special NBT item (potion, enchant book)
            itemStack = handleSpecialNbtItem(player, itemStack);

            // get length, shiny info, rarity
            // todo add fish weight in kg
            Pair<Double, FishRarity> lengthRarityPair = generateLengthAndRarity(player, fishWrapper, isRaining, isThundering, isNightTime);
            int length = lengthRarityPair.getA().intValue();
            int shinyFrequency = TreasureSeas.getInstance().getConfigManager().getShinyFrequency();
            boolean isShiny = TreasureSeas.RANDOM.nextInt(shinyFrequency) == 0;
            FishRarity rarity = lengthRarityPair.getB();
            int weightG = FishUtils.estimateWeight(fishWrapper.getMaxLength(), length);
            FishGender gender = TreasureSeas.RANDOM.nextBoolean() ? FishGender.MALE : FishGender.FEMALE;

            checkOrGiveAdvancements(player, fishWrapper, length, isShiny, rarity);

            recordFishingResultToFishItem(player, fishWrapper, itemStack, length, rarity, isShiny, weightG, gender);

            generateRewardItemToWorld(bobberPos, player, itemStack);

            generateExperienceOrb(player, rarity);

            recordFishingResultToRodItem(player);

            updatePlayerFishingNBT(player, fishWrapper, length, isShiny, weightG);

            player.awardStat(Stats.FISH_CAUGHT, 1);
        }
    }

    @NotNull
    private static ItemStack handleSpecialNbtItem(ServerPlayer player, ItemStack itemStack) {
        if (itemStack.getItem() instanceof PotionItem ||
            itemStack.getItem() instanceof SplashPotionItem ||
            itemStack.getItem() instanceof LingeringPotionItem) {
            int entLvl = FishUtils.getFishFighterRodEnchantLevel(player);
            switch (entLvl) {
                case 1 -> itemStack = RandomPotionUtil.getRandomPotion(itemStack, 1);
                case 2 -> itemStack = RandomPotionUtil.getRandomPotion(itemStack, 2);
                case 3 -> itemStack = RandomPotionUtil.getRandomPotion(itemStack, 2);
                case 4 -> itemStack = RandomPotionUtil.getRandomPotion(itemStack, 3);
                case 5 -> itemStack = RandomPotionUtil.getRandomPotion(itemStack, 3);
                default -> itemStack = RandomPotionUtil.getRandomPotion(itemStack, 1);
            }
        }
        if (itemStack.getItem() instanceof EnchantedBookItem) {
            int entLvl = FishUtils.getFishFighterRodEnchantLevel(player);
            switch (entLvl) {
                case 1 -> itemStack = RandomEnchantmentUtil.getRandomEnchantmentBook(1);
                case 2 -> itemStack = RandomEnchantmentUtil.getRandomEnchantmentBook(2);
                case 3 -> itemStack = RandomEnchantmentUtil.getRandomEnchantmentBook(3);
                case 4 -> itemStack = RandomEnchantmentUtil.getRandomEnchantmentBook(4);
                case 5 -> itemStack = RandomEnchantmentUtil.getRandomEnchantmentBook(4);
                default -> itemStack = RandomEnchantmentUtil.getRandomEnchantmentBook(1);
            }
        }
        return itemStack;
    }

    private static void updatePlayerFishingNBT(
            ServerPlayer player, FishWrapper fishWrapper, int length, boolean isShiny, int weightG) {
        CompoundTag playerData = player.getPersistentData();
        CompoundTag treasureSeasTag = playerData.getCompound("treasureSeas");
        if (treasureSeasTag.isEmpty()) {
            treasureSeasTag = new CompoundTag();
            playerData.put("treasureSeas", treasureSeasTag);
        }
        String fishKey = fishWrapper.getModNamespace() + ":" + fishWrapper.getFishItemName();
        CompoundTag fishTag = treasureSeasTag.getCompound(fishKey);
        int storedLength = fishTag.getInt("maxLength");
        if (length > storedLength) {
            fishTag.putInt("maxLength", length);
        }
        int storedWeight = fishTag.getInt("maxWeight");
        if (weightG > storedWeight) {
            fishTag.putInt("maxWeight", weightG);
        }
        if (isShiny) {
            fishTag.putBoolean("isShiny", true);
        }
        int count = fishTag.getInt("cnt");
        fishTag.putInt("cnt", count + 1);
        treasureSeasTag.put(fishKey, fishTag);
        playerData.put("treasureSeas", treasureSeasTag);
    }

    private static void checkOrGiveAdvancements(ServerPlayer player, FishWrapper fishWrapper, int length, boolean isShiny, FishRarity rarity) {
        if (!fishWrapper.isTreasure() && !fishWrapper.isJunk() && !fishWrapper.isUltimateTreasure()) {
            // is fish
            if (length >= 200) {
                AchievementHelper.grantAdvancement(player, "length_200_fish", "fishing");
            } else if (length >= 150) {
                AchievementHelper.grantAdvancement(player, "length_150_fish", "fishing");
            } else if (length >= 100) {
                AchievementHelper.grantAdvancement(player, "length_100_fish", "fishing");
            } else if (length >= 50) {
                AchievementHelper.grantAdvancement(player, "length_50_fish", "fishing");
            }
            if (isShiny) AchievementHelper.grantAdvancement(player, "shiny_fish", "fishing");
            switch (rarity) {
                case RARE -> AchievementHelper.grantAdvancement(player, "rare_fish", "fishing");
                case EXCEPTIONAL -> AchievementHelper.grantAdvancement(player, "exceptional_fish", "fishing");
                case UNCOMMON -> AchievementHelper.grantAdvancement(player, "uncommon_fish", "fishing");
                case SUPERIOR -> AchievementHelper.grantAdvancement(player, "superior_fish", "fishing");
                case LEGEND -> AchievementHelper.grantAdvancement(player, "legend_fish", "fishing");
                case MYTHIC -> AchievementHelper.grantAdvancement(player, "mythic_fish", "fishing");
                case DIVINE -> AchievementHelper.grantAdvancement(player, "divine_fish", "fishing");
            }
        }
        if (TreasureSeas.MOD_ID.equals(fishWrapper.getModNamespace())) {
            if ("pirate_treasure".equals(fishWrapper.getFishItemName())) {
                AchievementHelper.grantAdvancement(player, "pirate_treasure", "fishing");
            }
            if ("power_fruit".equals(fishWrapper.getFishItemName())) {
                AchievementHelper.grantAdvancement(player, "power_fruit", "fishing");
            }
            if ("life_fruit".equals(fishWrapper.getFishItemName())) {
                AchievementHelper.grantAdvancement(player, "life_fruit", "fishing");
            }
        }
    }

    private static void recordFishingResultToFishItem(
            Player player, FishWrapper fishWrapper, ItemStack itemStack,
            int length, FishRarity rarity, boolean isShiny, int weightG, FishGender gender) {
        if (fishWrapper.isUltimateTreasure() || fishWrapper.isTreasure() || fishWrapper.isJunk()) {
            return;
        }
        // nbt
        CompoundTag fishTag = itemStack.getOrCreateTag();
        fishTag.putInt("length", length);
        fishTag.putInt("weight", weightG);
        fishTag.putString("gender", gender.getGenderAsString());
        fishTag.putString("rarity", rarity.name());
        fishTag.putBoolean("isShiny", isShiny);
        fishTag.putLong("timestamp", System.currentTimeMillis());
        fishTag.putString("world", player.level.dimension().location().getPath());
        fishTag.putString("location", String.format("%d,%d,%d", player.getBlockX(), player.getBlockY(), player.getBlockZ()));
        fishTag.putString("fisher", player.getScoreboardName());
        // lore
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.treasure_seas.quality", rarity.getName()))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.treasure_seas.length", "§7" + length))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.treasure_seas.gender", "§7" + I18n.get(gender.getTranslatableComponent().getKey())))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.treasure_seas.weight", "§7" + MathUtils.convertWeight(weightG)))));
        if (isShiny) {
            lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.treasure_seas.shiny"))));
        }
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("item.treasure_seas.fish.lore3"))));
        itemStack.getOrCreateTagElement("display").put("Lore", lore);
    }

    @NotNull
    private static Pair<Double, FishRarity> generateLengthAndRarity(ServerPlayer player, FishWrapper fishWrapper, boolean isRaining, boolean isThundering, boolean isNightTime) {
        double minLength = fishWrapper.getMinLength();
        double maxLength = fishWrapper.getMaxLength();
        double mostCommonLength = fishWrapper.getMostCommonLength();
        double lengthDispersion = fishWrapper.getLengthDispersion();
        double length = MathUtils.randomFishLength(minLength, maxLength, mostCommonLength, lengthDispersion);

        // Adjust length based on weather and time conditions
        if (isRaining) {
            length += (maxLength - minLength) * 0.025;
        }
        if (isThundering) {
            length += (maxLength - minLength) * 0.05;
        }
        if (isNightTime) {
            length += (maxLength - minLength) * 0.025;
        }
        // Adjust length based on fish fighter ent lvl
        int entLvl = FishUtils.getFishFighterRodEnchantLevel(player);
        float multiplier = entLvl * 0.0125F;
        length = length * (1 + multiplier);
        // Adjust length based on whether the fisher is on a boat
        if (player.getVehicle() instanceof Boat) {
            length = length * (1 + 0.0125F);
        }
        // 避免增幅超过极值
        length = Math.min(length, maxLength);

        // Determine rarity based on the CDF percentage
        double modeNormalized = (mostCommonLength - minLength) / (maxLength - minLength);
        double a = modeNormalized * (lengthDispersion - 2) + 1;
        double b = (1 - modeNormalized) * (lengthDispersion - 2) + 1;
        double normalizedLength = (length - minLength) / (maxLength - minLength);
        double percentage = MathUtils.betaCDF(normalizedLength, a, b) * 100.0;
        FishRarity rarity = FishRarity.getRarity(percentage);
        return new Pair<>(length, rarity);
    }

    private static void generateRewardItemToWorld(Vec3 bobberPos, ServerPlayer player, ItemStack itemStack) {
        ItemEntity entity;
        if (player.level.dimension() == Level.NETHER) {
            entity = new FireproofItemEntity(player.level, bobberPos.x(), bobberPos.y(), bobberPos.z(), itemStack);
            entity.getPersistentData().putLong("FireImmuneUntil", player.level.getGameTime() + (5 * 60 * 20));
        } else {
            entity = new ItemEntity(player.level, bobberPos.x(), bobberPos.y(), bobberPos.z(), itemStack);
        }
        entity.setPos(bobberPos.x(), bobberPos.y(), bobberPos.z());
        final double distX = player.getX() - bobberPos.x();
        final double distY = player.getY() - bobberPos.y();
        final double distZ = player.getZ() - bobberPos.z();
        entity.setDeltaMovement(
                distX * 0.1,
                distY * 0.1 + Math.sqrt(Math.sqrt(distX * distX + distY * distY + distZ * distZ)) * 0.08,
                distZ * 0.1);
        player.level.addFreshEntity(entity);
    }

    private static void generateExperienceOrb(ServerPlayer player, FishRarity rarity) {
        int baseExperience = player.getRandom().nextInt(5) + 1;
        double multiplier = rarity.getExperienceMultiplier();
        AABB nearbyArea = new AABB(
                player.getX() - 3.5, player.getY() - 1.5, player.getZ() - 3.5,
                player.getX() + 3.5, player.getY() + 1.5, player.getZ() + 3.5
        );
        // 周围有其余玩家在钓鱼时，一起获得 +15% 经验
        boolean isFishingTogether;
        List<Player> nearbyPlayers = player.level.getNearbyPlayers(TargetingConditions.DEFAULT, player, nearbyArea);
        if (nearbyPlayers.size() > 1) {
            multiplier *= 1.15;
            isFishingTogether = true;
        } else {
            isFishingTogether = false;
        }
        double finalMultiplier = multiplier;
        int experienceAmount = (int) (baseExperience * finalMultiplier);
        nearbyPlayers.stream()
                .filter(p -> p.fishing != null)
                .forEach(p -> {
                    player.level.addFreshEntity(
                            new ExperienceOrb(
                                    player.level,
                                    player.getX(),
                                    player.getY() + 0.5,
                                    player.getZ() + 0.5,
                                    experienceAmount
                            )
                    );
                    if (isFishingTogether && p instanceof ServerPlayer) {
                        AchievementHelper.grantAdvancement((ServerPlayer) p, "co_fishing", "fishing");
                    }
                });
        player.level.addFreshEntity(
                new ExperienceOrb(
                        player.level,
                        player.getX(),
                        player.getY() + 0.5,
                        player.getZ() + 0.5,
                        experienceAmount
                )
        );
    }

    private static void recordFishingResultToRodItem(ServerPlayer player) {
        ItemStack fishRod = FishUtils.getFishRodItemFromInv(player);
        int enchantLevel = FishUtils.getFishFighterRodEnchantLevel(player);
        int nextLvlExp = FishingRodUpgradeRequirement.getRequiredFishingCntForLevel(enchantLevel);
        if (fishRod != null) {
            // 更新 NBT 标签
            CompoundTag tag = fishRod.getOrCreateTag();
            int count = tag.getInt("FishingCount");
            tag.putInt("FishingCount", ++count);

            // 直接更新 lore 的第0行
            ListTag lores = ItemUtils.getLoreList(fishRod);
            ListTag updatedLores = FishUtils.addFishCountLoreIntoItem(lores, count, nextLvlExp);

            // 更新 itemStack 的 lore
            ItemUtils.setLoreList(fishRod, updatedLores);
        }
    }


    /**
     * Server-side no need to do anything
     */
    public void handleOpenFightingGui(final NetworkEvent.Context ctx, final Vec3 bobberPos, final FishWrapper fishWrapper) {
    }

    public void handleOpenInfoGui(final NetworkEvent.Context ctx, final Map<String, FishWrapper> fishWrapperMap, CompoundTag treasureSeasData) {
    }

}
