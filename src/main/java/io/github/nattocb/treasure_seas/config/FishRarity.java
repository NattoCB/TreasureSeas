package io.github.nattocb.treasure_seas.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

public enum FishRarity {

    ORDINARY(new TranslatableComponent("fish.rarity.ordinary"), 2.0, 1.0),
    UNCOMMON(new TranslatableComponent("fish.rarity.uncommon"), 3.0, 1.2),
    RARE(new TranslatableComponent("fish.rarity.rare"), 3.5, 1.5),
    SUPERIOR(new TranslatableComponent("fish.rarity.superior"), 4.0, 2.0),
    EXCEPTIONAL(new TranslatableComponent("fish.rarity.exceptional"), 5.0, 2.5),
    LEGEND(new TranslatableComponent("fish.rarity.legend"), 7.0, 3.0),
    MYTHIC(new TranslatableComponent("fish.rarity.mythic"), 8.0, 4.0),
    DIVINE(new TranslatableComponent("fish.rarity.divine"), 10.0, 5.0);

    private final Component name;
    private final double experienceMultiplier;
    private final double priceMultiplier; // Add this field

    FishRarity(Component name, double experienceMultiplier, double priceMultiplier) {
        this.name = name;
        this.experienceMultiplier = experienceMultiplier;
        this.priceMultiplier = priceMultiplier; // Initialize the field
    }

    public Component getName() {
        return name;
    }

    public double getExperienceMultiplier() {
        return experienceMultiplier;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    // 根据概率百分比返回对应的枚举类
    public static FishRarity getRarity(double percentage) {
        if (percentage <= 50) {
            return FishRarity.ORDINARY;
        } else if (percentage <= 60) {
            return FishRarity.UNCOMMON;
        } else if (percentage <= 75) {
            return FishRarity.RARE;
        } else if (percentage <= 90) {
            return FishRarity.SUPERIOR;
        } else if (percentage <= 95) {
            return FishRarity.EXCEPTIONAL;
        } else if (percentage <= 97.5) {
            return FishRarity.LEGEND;
        } else if (percentage <= 99) {
            return FishRarity.MYTHIC;
        } else {
            return FishRarity.DIVINE;
        }
    }

    @Nullable
    public static FishRarity fromName(String name) {
        try {
            return FishRarity.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

}
