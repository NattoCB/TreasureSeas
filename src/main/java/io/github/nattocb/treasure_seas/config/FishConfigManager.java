package io.github.nattocb.treasure_seas.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import io.github.nattocb.treasure_seas.TreasureSeas;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class FishConfigManager {

    /**
     * common-config
     */

    private List<FishWrapper> fishConfigs;

    private Map<String, FishWrapper> fishWrapperMap;

    private FishWrapper defaultFishConfig;

    public List<FishWrapper> getFishConfigs() {
        return this.fishConfigs;
    }

    public FishWrapper getDefaultFishConfig() {
        return this.defaultFishConfig;
    }

    public Map<String, FishWrapper> getFishWrapperMap() {
        return this.fishWrapperMap;
    }

    /**
     * client-config
     */

    private boolean hudFishingInfoEnable;
    private boolean hudFishingInfoEnableCustomPosition;
    private int hudFishingInfoCustomX;
    private int hudFishingInfoCustomY;
    private boolean logDebugModeEnable;

    public boolean isHudFishingInfoEnable() {
        return hudFishingInfoEnable;
    }

    public void setHudFishingInfoEnable(boolean flag) {
        hudFishingInfoEnable = flag;
    }


    public void setHudFishingInfoEnableCustomPosition(boolean flag) {
        hudFishingInfoEnableCustomPosition = flag;
    }


    public void setLogDebugModeEnable(boolean flag) {
        logDebugModeEnable = flag;
    }

    public void setHudFishingInfoCustomX(int value) {
        hudFishingInfoCustomX = value;
    }
    public void setHudFishingInfoCustomY(int value) {
        hudFishingInfoCustomY = value;
    }
    public boolean isHudFishingInfoEnableCustomPosition() {
        return hudFishingInfoEnableCustomPosition;
    }

    public int getHudFishingInfoCustomX() {
        return hudFishingInfoCustomX;
    }

    public int getHudFishingInfoCustomY() {
        return hudFishingInfoCustomY;
    }

    public boolean isLogDebugModeEnable() {
        return logDebugModeEnable;
    }

    /**
     * config-init
     */
    public void loadCommonConfig() {
        String commonConfigFilePath = "config/treasureseas-common.toml";
        Path commonConfigPath = new File(commonConfigFilePath).toPath();
        preCheckConfigExistence(commonConfigPath, commonConfigFilePath);

        // load config from game config folder
        FileConfig config = FileConfig.builder(commonConfigPath.toString())
                .autoreload()
                .autosave()
                .build();
        config.load();

        // parse config
        this.fishConfigs = FishWrapper.fromConfig(config);
        Optional<FishWrapper> firstFish = this.fishConfigs.stream()
                .filter(fishWrapper -> !fishWrapper.isTreasure()
                        && !fishWrapper.isJunk()
                        && !fishWrapper.isUltimateTreasure())
                .findFirst();
        if (firstFish.isPresent()) {
            this.defaultFishConfig = firstFish.get();
        } else {
            this.defaultFishConfig = createDefaultFishConfig();
            this.fishConfigs.add(this.defaultFishConfig);
        }

        // 初始化双向指针表
        this.fishWrapperMap = new HashMap<>(64);
        this.fishConfigs.forEach(fishWrapper -> {
            this.fishWrapperMap.put(fishWrapper.getModNamespace() + ":" + fishWrapper.getFishItemName(), fishWrapper);
        });

        doLog();
    }

    public void loadClientConfig() {
        String clientConfigFilePath = "config/treasureseas-client.properties";
        Path clientConfigPath = new File(clientConfigFilePath).toPath();
        preCheckConfigExistence(clientConfigPath, clientConfigFilePath);
        // Load properties file
        Properties clientProperties = new Properties();
        try (InputStream inputStream = Files.newInputStream(clientConfigPath)) {
            clientProperties.load(inputStream);
            // Parse and assign the properties to member variables
            hudFishingInfoEnable = Boolean.parseBoolean(clientProperties.getProperty("hud.fishing_info.enable", "true"));
            hudFishingInfoEnableCustomPosition = Boolean.parseBoolean(clientProperties.getProperty("hud.fishing_info.custom_position.enable", "false"));
            hudFishingInfoCustomX = Integer.parseInt(clientProperties.getProperty("hud.fishing_info.custom_position.x", "0"));
            hudFishingInfoCustomY = Integer.parseInt(clientProperties.getProperty("hud.fishing_info.custom_position.y", "0"));
            logDebugModeEnable = Boolean.parseBoolean(clientProperties.getProperty("log.debug_mode.enable", "false"));
            TreasureSeas.getLogger().info("Client configuration loaded successfully.");
        } catch (Exception e) {
            TreasureSeas.getLogger().error("Failed to load client configuration: " + e.getMessage());
        }
    }

    public void saveClientConfig() {
        String clientConfigFilePath = "config/treasureseas-client.properties";
        Path clientConfigPath = new File(clientConfigFilePath).toPath();
        Properties clientProperties = new Properties();
        clientProperties.setProperty("hud.fishing_info.enable", Boolean.toString(hudFishingInfoEnable));
        clientProperties.setProperty("hud.fishing_info.custom_position.enable", Boolean.toString(hudFishingInfoEnableCustomPosition));
        clientProperties.setProperty("hud.fishing_info.custom_position.x", Integer.toString(hudFishingInfoCustomX));
        clientProperties.setProperty("hud.fishing_info.custom_position.y", Integer.toString(hudFishingInfoCustomY));
        clientProperties.setProperty("log.debug_mode.enable", Boolean.toString(logDebugModeEnable));
        try (OutputStream outputStream = Files.newOutputStream(clientConfigPath)) {
            clientProperties.store(outputStream, "TreasureSeas Client Configuration");
            TreasureSeas.getLogger().info("Client configuration saved successfully.");
        } catch (Exception e) {
            TreasureSeas.getLogger().error("Failed to save client configuration: " + e.getMessage());
        }
    }

    private void doLog() {
        long treasureCount = this.fishConfigs.stream().filter(FishWrapper::isTreasure).count();
        long junkCount = this.fishConfigs.stream().filter(FishWrapper::isJunk).count();
        long ultimateTreasureCount = this.fishConfigs.stream().filter(FishWrapper::isUltimateTreasure).count();
        long normalFishCount = this.fishConfigs.stream()
                .filter(fishWrapper -> !fishWrapper.isTreasure()
                        && !fishWrapper.isJunk()
                        && !fishWrapper.isUltimateTreasure())
                .count();
        TreasureSeas.getLogger().info("TreasureSeasConfig: Loaded fish types count:");
        TreasureSeas.getLogger().info("TreasureSeasConfig:   - Treasure: " + treasureCount);
        TreasureSeas.getLogger().info("TreasureSeasConfig:   - Junk: " + junkCount);
        TreasureSeas.getLogger().info("TreasureSeasConfig:   - Ultimate Treasure: " + ultimateTreasureCount);
        TreasureSeas.getLogger().info("TreasureSeasConfig:   - Normal Fish: " + normalFishCount);
    }

    private void preCheckConfigExistence(Path configPath, String resourcePath) {
        if (!Files.exists(configPath)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (in != null) {
                    Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    throw new RuntimeException("Default configuration file not found in resources");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to copy default configuration file", e);
            }
        }
    }

    private FishWrapper createDefaultFishConfig() {
        return new FishWrapper(
                1,
                300,
                1.75f,
                new float[]{0.3f, 5.5f},
                new float[]{5.0f, 7.5f},
                FishWrapper.AllowedTime.ALL,
                FishWrapper.AllowedWeather.ALL,
                new String[]{},
                new String[]{},
                "minecraft",
                "salmon",
                5.0,
                95.0,
                31.0,
                10.0,
                1,
                50,
                10,
                false,
                4,
                false,
                false,
                false
        );
    }

}
