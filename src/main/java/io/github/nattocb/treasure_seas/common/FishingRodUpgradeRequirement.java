package io.github.nattocb.treasure_seas.common;

public enum FishingRodUpgradeRequirement {
    LEVEL_0(0, 0),
    LEVEL_1(1, 25),
    LEVEL_2(2, 85),
    LEVEL_3(3, 175),
    LEVEL_4(4, 335);

    private final int level;
    private final int requiredFishingCnt;

    FishingRodUpgradeRequirement(int level, int requiredFishingCnt) {
        this.level = level;
        this.requiredFishingCnt = requiredFishingCnt;
    }

    public int getLevel() {
        return level;
    }

    public int getRequiredFishingCnt() {
        return requiredFishingCnt;
    }

    public static int getRequiredFishingCntForLevel(int level) {
        for (FishingRodUpgradeRequirement requirement : values()) {
            if (requirement.getLevel() == level) {
                return requirement.getRequiredFishingCnt();
            }
        }
        return Integer.MAX_VALUE;
    }

}