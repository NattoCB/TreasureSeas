package io.github.nattocb.treasure_seas.proxy;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.config.FishWrapper;
import io.github.nattocb.treasure_seas.enchantment.FishingRodUpgradeRequirement;
import io.github.nattocb.treasure_seas.item.FireproofItemEntity;
import io.github.nattocb.treasure_seas.packet.PacketHandler;
import io.github.nattocb.treasure_seas.config.FishRarity;
import io.github.nattocb.treasure_seas.utils.FishUtils;
import io.github.nattocb.treasure_seas.utils.ItemUtils;
import io.github.nattocb.treasure_seas.utils.MathUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
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

    private static final Map<String, ResourceLocation> ADVANCEMENT_CACHE = new HashMap<>();

    // todo 可配置
    private static final int SHINY_PROBABILITY = 1800;

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

            // get length, shiny info, rarity
            Pair<Double, FishRarity> lengthRarityPair = generateLengthAndRarity(player, fishWrapper, isRaining, isThundering, isNightTime);
            int length = lengthRarityPair.getA().intValue();
            boolean isShiny = TreasureSeas.RANDOM.nextInt(SHINY_PROBABILITY) == 0;
            FishRarity rarity = lengthRarityPair.getB();

            checkOrGiveAdvancements(player, fishWrapper, length, isShiny, rarity);

            recordFishingResultToFishItem(fishWrapper, itemStack, length, rarity, isShiny);

            generateRewardItemToWorld(bobberPos, player, itemStack);

            generateExperienceOrb(player, rarity);

            recordFishingResultToRodItem(player);

            player.awardStat(Stats.FISH_CAUGHT, 1);
        }
    }

    private static void checkOrGiveAdvancements(ServerPlayer player, FishWrapper fishWrapper, int length, boolean isShiny, FishRarity rarity) {
        if (!fishWrapper.isTreasure() && !fishWrapper.isJunk() && !fishWrapper.isUltimateTreasure()) {
            // is fish
            if (length >= 200) {
                grantAdvancement(player, "length_200_fish", "fishing");
            } else if (length >= 150) {
                grantAdvancement(player, "length_150_fish", "fishing");
            } else if (length >= 100) {
                grantAdvancement(player, "length_100_fish", "fishing");
            } else if (length >= 50) {
                grantAdvancement(player, "length_50_fish", "fishing");
            }
            if (isShiny) grantAdvancement(player, "shiny_fish", "fishing");
            switch (rarity) {
                case RARE -> grantAdvancement(player, "rare_fish", "fishing");
                case EXCEPTIONAL -> grantAdvancement(player, "exceptional_fish", "fishing");
                case UNCOMMON -> grantAdvancement(player, "uncommon_fish", "fishing");
                case SUPERIOR -> grantAdvancement(player, "superior_fish", "fishing");
                case LEGEND -> grantAdvancement(player, "legend_fish", "fishing");
                case MYTHIC -> grantAdvancement(player, "mythic_fish", "fishing");
                case DIVINE -> grantAdvancement(player, "divine_fish", "fishing");
            }
        }
        if (TreasureSeas.MOD_ID.equals(fishWrapper.getModNamespace())) {
            if ("pirate_treasure".equals(fishWrapper.getFishItemName())) {
                grantAdvancement(player, "pirate_treasure", "fishing");
            }
            if ("power_fruit".equals(fishWrapper.getFishItemName())) {
                grantAdvancement(player, "power_fruit", "fishing");
            }
            if ("life_fruit".equals(fishWrapper.getFishItemName())) {
                grantAdvancement(player, "life_fruit", "fishing");
            }
        }
    }

    private static void grantAdvancement(ServerPlayer player, String advancementKey, String criteria) {
        ResourceLocation advancementId = ADVANCEMENT_CACHE.computeIfAbsent(advancementKey, key -> new ResourceLocation(TreasureSeas.MOD_ID, key));
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerAdvancementManager advancements = server.getAdvancements();
        Advancement advancement = advancements.getAdvancement(advancementId);
        if (advancement == null) return;
        player.getAdvancements().award(advancement, criteria);
    }

    private static void recordFishingResultToFishItem(FishWrapper fishWrapper, ItemStack itemStack, int length, FishRarity rarity, boolean isShiny) {
        if (fishWrapper.isUltimateTreasure() || fishWrapper.isTreasure() || fishWrapper.isJunk()) {
            return;
        }
        // nbt
        CompoundTag fishTag = itemStack.getOrCreateTag();
        fishTag.putInt("length", length);
        fishTag.putString("rarity", rarity.name());
        fishTag.putBoolean("isShiny", isShiny);
        // lore
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.length", "§7" + length))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.quality", rarity.getName()))));
        if (isShiny) {
            lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("fish.shiny"))));
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
        //  Adjust length based on fish fighter ent lvl
        int entLvl = FishUtils.getFishRodFighterEnchantLevel(player);
        float multiplier = entLvl * 0.0125F;
        length = length * (1 + multiplier);
        // Adjust length based on weather the fisher is on a boat
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

    private static void generateRewardItemToWorld(Vec3 bobberPosition, ServerPlayer player, ItemStack itemStack) {
        ItemEntity entity;
        if (player.level.dimension() == Level.NETHER) {
            entity = new FireproofItemEntity(player.level, bobberPosition.x(), bobberPosition.y(), bobberPosition.z(), itemStack);
            entity.getPersistentData().putLong("FireImmuneUntil", player.level.getGameTime() + (5 * 60 * 20));
        } else {
            entity = new ItemEntity(player.level, bobberPosition.x(), bobberPosition.y(), bobberPosition.z(), itemStack);
        }
        entity.setPos(bobberPosition.x(), bobberPosition.y(), bobberPosition.z());
        final double distX = player.getX() - bobberPosition.x();
        final double distY = player.getY() - bobberPosition.y();
        final double distZ = player.getZ() - bobberPosition.z();
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
                        grantAdvancement((ServerPlayer) p, "co_fishing", "fishing");
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
        int enchantLevel = FishUtils.getFishRodFighterEnchantLevel(player);
        int nextLvlExp = FishingRodUpgradeRequirement.getRequiredExperienceForLevel(enchantLevel);
        if (fishRod != null) {
            // 更新 NBT 标签
            CompoundTag tag = fishRod.getOrCreateTag();
            int count = tag.getInt("FishingCount");
            tag.putInt("FishingCount", ++count);

            // 直接更新 lore 的第0行
            ListTag lores = ItemUtils.getLoreList(fishRod);
            ListTag updatedLores = FishUtils.prepareLoreInfo(lores, count, nextLvlExp);

            // 更新 itemStack 的 lore
            ItemUtils.setLoreList(fishRod, updatedLores);
        }
    }


    /**
     * Server-side no need to do anything
     */
    public void handleOpenFightingGui(final NetworkEvent.Context ctx, final Vec3 bobberPos, final FishWrapper fishWrapper) {
    }

}
