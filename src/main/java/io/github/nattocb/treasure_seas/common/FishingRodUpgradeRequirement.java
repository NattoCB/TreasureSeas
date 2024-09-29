package io.github.nattocb.treasure_seas.common;

public enum FishingRodUpgradeRequirement {
    LEVEL_0(0, 0),
    LEVEL_1(1, 25),
    LEVEL_2(2, 85),
    LEVEL_3(3, 175),
    LEVEL_4(4, 335);

    private final int level;
    private final int requiredExperience;

    FishingRodUpgradeRequirement(int level, int requiredExperience) {
        this.level = level;
        this.requiredExperience = requiredExperience;
    }

    public int getLevel() {
        return level;
    }

    public int getRequiredExperience() {
        return requiredExperience;
    }

    public static int getRequiredExperienceForLevel(int level) {
        for (FishingRodUpgradeRequirement requirement : values()) {
            if (requirement.getLevel() == level) {
                return requirement.getRequiredExperience();
            }
        }
        return Integer.MAX_VALUE;
    }

}