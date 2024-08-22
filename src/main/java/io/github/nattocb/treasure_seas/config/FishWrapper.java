package io.github.nattocb.treasure_seas.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FishWrapper {
    public enum AllowedTime {
        MORNING, AFTERNOON, EVENING, NIGHT, ALL
    }

    public enum AllowedWeather {
        CLEAR, RAIN, STORM, ALL
    }

    private int lowestLootableEnchantmentLevel;
    private int ticksToWin;
    private float speedModifier;
    private float[] flatSegmentRandomRange;
    private float[] fluxSegmentRandomRange;
    private AllowedTime allowedTime;
    private AllowedWeather allowedWeather;
    private String[] possibleBiomes;
    private String[] possibleWorlds;
    private String modNamespace;
    private String fishItemName;
    private double minLength;
    private double maxLength;
    private double mostCommonLength;
    private double lengthDispersion;
    private int minAppearDepth;
    private int maxAppearDepth;
    private int sampleWeight;
    private boolean caveOnly;
    private int basePrice;

    private boolean isTreasure;
    private boolean isJunk;
    private boolean isUltimateTreasure;

    public FishWrapper(int lowestLootableEnchantmentLevel, int ticksToWin, float speedModifier,
                       float[] flatSegmentRandomRange, float[] fluxSegmentRandomRange, AllowedTime allowedTime,
                       AllowedWeather allowedWeather, String[] possibleBiomes, String[] possibleWorlds,
                       String modNamespace, String fishItemName, double minLength, double maxLength,
                       double mostCommonLength, double lengthDispersion, int minAppearDepth, int maxAppearDepth,
                       int sampleWeight, boolean caveOnly, int basePrice, boolean isTreasure, boolean isJunk,
                       boolean isUltimateTreasure) {
        this.lowestLootableEnchantmentLevel = lowestLootableEnchantmentLevel;
        this.ticksToWin = ticksToWin;
        this.speedModifier = speedModifier;
        this.flatSegmentRandomRange = flatSegmentRandomRange;
        this.fluxSegmentRandomRange = fluxSegmentRandomRange;
        this.allowedTime = allowedTime;
        this.allowedWeather = allowedWeather;
        this.possibleBiomes = possibleBiomes;
        this.possibleWorlds = possibleWorlds;
        this.modNamespace = modNamespace;
        this.fishItemName = fishItemName;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.mostCommonLength = mostCommonLength;
        this.lengthDispersion = lengthDispersion;
        this.minAppearDepth = minAppearDepth;
        this.maxAppearDepth = maxAppearDepth;
        this.sampleWeight = sampleWeight;
        this.caveOnly = caveOnly;
        this.basePrice = basePrice;

        this.isTreasure = isTreasure;
        this.isJunk = isJunk;
        this.isUltimateTreasure = isUltimateTreasure;
    }

    @NotNull
    public static List<FishWrapper> fromConfig(FileConfig config) {
        List<FishWrapper> fishes = new ArrayList<>();

        CommentedConfig fishConfigs = config.get("Fishes");
        if (fishConfigs != null)
            buildFishWrappers(fishConfigs, fishes, false, false, false);

        CommentedConfig treasureConfigs = config.get("Treasures");
        if (treasureConfigs != null)
            buildFishWrappers(treasureConfigs, fishes, true, false, false);

        CommentedConfig junkConfigs = config.get("Junks");
        if (junkConfigs != null)
            buildFishWrappers(junkConfigs, fishes, false, true, false);

        CommentedConfig ultimateTreasuresConfig = config.get("UltimateTreasures");
        if (ultimateTreasuresConfig != null)
            buildFishWrappers(ultimateTreasuresConfig, fishes, false, false, true);

        return fishes;
    }

    private static void buildFishWrappers(CommentedConfig fishConfigs, List<FishWrapper> fishes,
                                          boolean isTreasure, boolean isJunk, boolean isUltimateTreasure) {
        for (Map.Entry<String, Object> entry : fishConfigs.valueMap().entrySet()) {
            String fishKey = entry.getKey();
            CommentedConfig fishConfig = fishConfigs.get(fishKey);

            String modNamespace = fishConfig.get("modNamespace");
            String fishItemName = fishConfig.get("fishItemName");

            // check target mod existence
            if (!ModList.get().isLoaded(modNamespace)) {
                TreasureSeas.getLogger().warn("FishWrapper.buildFishWrappers: fetch item [{}] failed as this mod has not been registered in FML env", modNamespace + ":" + fishItemName);
                continue;
            }
            ResourceLocation itemLocation = new ResourceLocation(modNamespace, fishItemName);
            if (!ForgeRegistries.ITEMS.containsKey(itemLocation)) {
                TreasureSeas.getLogger().warn("FishWrapper.buildFishWrappers: fetch item [{}] failed as this itam has not been registered in target mod", modNamespace + ":" + fishItemName);
                continue;
            }

            int lowestLootableEnchantmentLevel = fishConfig.getInt("lowestLootableEnchantmentLevel");
            int ticksToWin = fishConfig.getInt("ticksToWin");
            float speedModifier = ((Number) fishConfig.get("speedModifier")).floatValue();
            float flatSegmentRandomRangeMin = ((Number) fishConfig.get("flatSegmentRandomRangeMin")).floatValue();
            float flatSegmentRandomRangeMax = ((Number) fishConfig.get("flatSegmentRandomRangeMax")).floatValue();
            float fluxSegmentRandomRangeMin = ((Number) fishConfig.get("fluxSegmentRandomRangeMin")).floatValue();
            float fluxSegmentRandomRangeMax = ((Number) fishConfig.get("fluxSegmentRandomRangeMax")).floatValue();
            AllowedTime allowedTime = AllowedTime.valueOf(fishConfig.get("allowedTime").toString().toUpperCase());
            AllowedWeather allowedWeather = AllowedWeather.valueOf(fishConfig.get("allowedWeather").toString().toUpperCase());
            List<String> possibleBiomes = fishConfig.get("possibleBiomes");
            List<String> possibleWorlds = fishConfig.get("possibleWorlds");
            double minLength = ((Number) fishConfig.get("minLength")).doubleValue();
            double maxLength = ((Number) fishConfig.get("maxLength")).doubleValue();
            double mostCommonLength = ((Number) fishConfig.get("mostCommonLength")).doubleValue();
            double lengthDispersion = ((Number) fishConfig.get("lengthDispersion")).doubleValue();
            int minAppearDepth = fishConfig.getInt("minAppearDepth");
            int maxAppearDepth = fishConfig.getInt("maxAppearDepth");
            int sampleWeight = fishConfig.getInt("sampleWeight");
            boolean caveOnly = fishConfig.get("caveOnly");
            int basePrice = fishConfig.getInt("basePrice");

            float[] flatSegmentRandomRange = {flatSegmentRandomRangeMin, flatSegmentRandomRangeMax};
            float[] fluxSegmentRandomRange = {fluxSegmentRandomRangeMin, fluxSegmentRandomRangeMax};

            FishWrapper fishWrapper = new FishWrapper(lowestLootableEnchantmentLevel, ticksToWin, speedModifier,
                    flatSegmentRandomRange, fluxSegmentRandomRange, allowedTime, allowedWeather,
                    possibleBiomes.toArray(new String[0]), possibleWorlds.toArray(new String[0]),
                    modNamespace, fishItemName, minLength, maxLength, mostCommonLength, lengthDispersion,
                    minAppearDepth, maxAppearDepth, sampleWeight, caveOnly, basePrice, isTreasure, isJunk, isUltimateTreasure);
            fishes.add(fishWrapper);
        }
    }

    public boolean matches(String biome, String world, AllowedWeather currentWeather, AllowedTime currentTime,
                           int enchantmentLevel, int currentDepth, boolean isCave, boolean isTreasure, boolean isJunk,
                           boolean isUltimateTreasure) {
        // 结果类别匹配
        if (this.isTreasure != isTreasure) return false;
        TreasureSeas.getLogger().dev("  - matches, pass isTreasure check");
        if (this.isJunk != isJunk) return false;
        TreasureSeas.getLogger().dev("  - matches, pass isJunk check");
        if (this.isUltimateTreasure != isUltimateTreasure) return false;
        TreasureSeas.getLogger().dev("  - matches, pass isUltimateTreasure check");

        // 检查附魔等级是否满足
        if (enchantmentLevel < this.lowestLootableEnchantmentLevel) return false;
        TreasureSeas.getLogger().dev("  - matches, pass enchantmentLevel check, {} >= {}", enchantmentLevel, this.lowestLootableEnchantmentLevel);

        // 检查时间条件是否满足
        if (this.allowedTime != AllowedTime.ALL && this.allowedTime != currentTime) return false;
        TreasureSeas.getLogger().dev("  - matches, pass allowedTime check");

        // 检查天气条件是否满足
        if (this.allowedWeather != AllowedWeather.ALL && this.allowedWeather != currentWeather) return false;
        TreasureSeas.getLogger().dev("  - matches, pass allowedWeather check");

        // 检查当前深度是否在允许的范围内
        // todo 改成蓄力决定深度？在0~最大容深之间？- 下个版本
        if (currentDepth < this.minAppearDepth || currentDepth > this.maxAppearDepth) return false;
        TreasureSeas.getLogger().dev("  - matches, pass maxAppearDepth check");

        // 检查生物群系是否匹配
        boolean biomeMatch = false;
        if (this.possibleBiomes.length == 0) {
            biomeMatch = true;
        } else {
            for (String b : this.possibleBiomes) {
                if (biome.equalsIgnoreCase(b)) {
                    biomeMatch = true;
                    break;
                }
            }
        }
        if (!biomeMatch) return false;
        TreasureSeas.getLogger().dev("  - matches, pass biomeMatch check");

        // 检查世界是否匹配
        boolean worldMatch = false;
        if (this.possibleWorlds.length == 0) {
            worldMatch = true;
        } else {
            for (String w : this.possibleWorlds) {
                if (world.equalsIgnoreCase(w)) {
                    worldMatch = true;
                    break;
                }
            }
        }
        if (!worldMatch) return false;
        TreasureSeas.getLogger().dev("  - matches, pass worldMatch check");

        // 检测是否在洞穴
        if (this.caveOnly && !isCave) return false;
        TreasureSeas.getLogger().dev("  - matches, pass isCave check");
        TreasureSeas.getLogger().dev("  - matches, pass ALL check");
        return true;
    }

    public int getLowestLootableEnchantmentLevel() {
        return lowestLootableEnchantmentLevel;
    }

    public int getTicksToWin() {
        return ticksToWin;
    }

    public float getSpeedModifier() {
        return speedModifier;
    }

    public float[] getFlatSegmentRandomRange() {
        return flatSegmentRandomRange;
    }

    public float[] getFluxSegmentRandomRange() {
        return fluxSegmentRandomRange;
    }

    public AllowedTime getAllowedTime() {
        return allowedTime;
    }

    public AllowedWeather getAllowedWeather() {
        return allowedWeather;
    }

    public String[] getPossibleBiomes() {
        return possibleBiomes;
    }

    public String[] getPossibleWorlds() {
        return possibleWorlds;
    }

    public String getModNamespace() {
        return modNamespace;
    }

    public String getFishItemName() {
        return fishItemName;
    }

    public double getMinLength() {
        return minLength;
    }

    public double getMaxLength() {
        return maxLength;
    }

    public double getMostCommonLength() {
        return mostCommonLength;
    }

    public double getLengthDispersion() {
        return lengthDispersion;
    }

    public int getMinAppearDepth() {
        return minAppearDepth;
    }

    public int getMaxAppearDepth() {
        return maxAppearDepth;
    }

    public int getSampleWeight() {
        return sampleWeight;
    }

    public boolean isCaveOnly() {
        return caveOnly;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public boolean isTreasure() {
        return isTreasure;
    }
    public boolean isJunk() {
        return isJunk;
    }
    public boolean isUltimateTreasure() {
        return isUltimateTreasure;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(lowestLootableEnchantmentLevel);
        buffer.writeInt(ticksToWin);
        buffer.writeFloat(speedModifier);
        writeFloatArray(buffer, flatSegmentRandomRange);
        writeFloatArray(buffer, fluxSegmentRandomRange);
        buffer.writeEnum(allowedTime);
        buffer.writeEnum(allowedWeather);
        writeStringArray(buffer, possibleBiomes);
        writeStringArray(buffer, possibleWorlds);
        buffer.writeUtf(modNamespace);
        buffer.writeUtf(fishItemName);
        buffer.writeDouble(minLength);
        buffer.writeDouble(maxLength);
        buffer.writeDouble(mostCommonLength);
        buffer.writeDouble(lengthDispersion);
        buffer.writeInt(minAppearDepth);
        buffer.writeInt(maxAppearDepth);
        buffer.writeInt(sampleWeight);
        buffer.writeBoolean(caveOnly);
        buffer.writeInt(basePrice);
        buffer.writeBoolean(isTreasure);
        buffer.writeBoolean(isJunk);
        buffer.writeBoolean(isUltimateTreasure);
    }

    public static FishWrapper readFromBuffer(FriendlyByteBuf buffer) {
        int lowestLootableEnchantmentLevel = buffer.readInt();
        int ticksToWin = buffer.readInt();
        float speedModifier = buffer.readFloat();
        float[] flatSegmentRandomRange = readFloatArray(buffer);
        float[] fluxSegmentRandomRange = readFloatArray(buffer);
        AllowedTime allowedTime = buffer.readEnum(AllowedTime.class);
        AllowedWeather allowedWeather = buffer.readEnum(AllowedWeather.class);
        String[] possibleBiomes = readStringArray(buffer);
        String[] possibleWorlds = readStringArray(buffer);
        String modNamespace = buffer.readUtf(32767);
        String fishItemName = buffer.readUtf(32767);
        double minLength = buffer.readDouble();
        double maxLength = buffer.readDouble();
        double mostCommonLength = buffer.readDouble();
        double lengthDispersion = buffer.readDouble();
        int minAppearDepth = buffer.readInt();
        int maxAppearDepth = buffer.readInt();
        int sampleWeight = buffer.readInt();
        boolean caveOnly = buffer.readBoolean();
        int basePrice = buffer.readInt();
        boolean isTreasure = buffer.readBoolean();
        boolean isJunk = buffer.readBoolean();
        boolean isUltimateTreasure = buffer.readBoolean();

        return new FishWrapper(
                lowestLootableEnchantmentLevel, ticksToWin, speedModifier,
                flatSegmentRandomRange, fluxSegmentRandomRange, allowedTime,
                allowedWeather, possibleBiomes, possibleWorlds, modNamespace,
                fishItemName, minLength, maxLength, mostCommonLength,
                lengthDispersion, minAppearDepth, maxAppearDepth, sampleWeight,
                caveOnly, basePrice, isTreasure, isJunk, isUltimateTreasure
        );
    }

    private static void writeFloatArray(FriendlyByteBuf buffer, float[] array) {
        buffer.writeInt(array.length);
        for (float value : array) {
            buffer.writeFloat(value);
        }
    }

    private static float[] readFloatArray(FriendlyByteBuf buffer) {
        int length = buffer.readInt();
        float[] array = new float[length];
        for (int i = 0; i < length; i++) {
            array[i] = buffer.readFloat();
        }
        return array;
    }

    private static void writeStringArray(FriendlyByteBuf buffer, String[] array) {
        buffer.writeInt(array.length);
        for (String value : array) {
            buffer.writeUtf(value);
        }
    }

    private static String[] readStringArray(FriendlyByteBuf buffer) {
        int length = buffer.readInt();
        String[] array = new String[length];
        for (int i = 0; i < length; i++) {
            array[i] = buffer.readUtf(32767);
        }
        return array;
    }

    @Override
    public String toString() {
        return "FishWrapper{" +
                "lowestLootableEnchantmentLevel=" + lowestLootableEnchantmentLevel +
                ", ticksToWin=" + ticksToWin +
                ", speedModifier=" + speedModifier +
                ", flatSegmentRandomRange=" + java.util.Arrays.toString(flatSegmentRandomRange) +
                ", fluxSegmentRandomRange=" + java.util.Arrays.toString(fluxSegmentRandomRange) +
                ", allowedTime=" + allowedTime +
                ", allowedWeather=" + allowedWeather +
                ", possibleBiomes=" + java.util.Arrays.toString(possibleBiomes) +
                ", possibleWorlds=" + java.util.Arrays.toString(possibleWorlds) +
                ", modNamespace='" + modNamespace + '\'' +
                ", fishItemName='" + fishItemName + '\'' +
                ", minLength=" + minLength +
                ", maxLength=" + maxLength +
                ", mostCommonLength=" + mostCommonLength +
                ", lengthDispersion=" + lengthDispersion +
                ", minAppearDepth=" + minAppearDepth +
                ", maxAppearDepth=" + maxAppearDepth +
                ", sampleWeight=" + sampleWeight +
                ", caveOnly=" + caveOnly +
                ", basePrice=" + basePrice +
                ", isTreasure=" + isTreasure +
                ", isJunk=" + isJunk +
                '}';
    }

}