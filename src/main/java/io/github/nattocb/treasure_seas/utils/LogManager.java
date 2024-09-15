package io.github.nattocb.treasure_seas.utils;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class LogManager {

    Logger logger;

    private final static String COMMON_PREFIX = "[TreasureSeas] ";

    public LogManager(Logger logger) {
        this.logger = logger;
    }

    public void info(String message, Object... args) {
        logger.info(COMMON_PREFIX + message, args);
    }

    public void debug(String message, Object... args) {
        logger.debug(COMMON_PREFIX + message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn(COMMON_PREFIX + message, args);
    }

    public void error(String message, Object... args) {
        logger.error(COMMON_PREFIX + message, args);
    }

    public void dev(String message, Object... args) {
        if (TreasureSeas.getInstance().getFishConfigManager().isLogDebugModeEnable()) {
            logger.info(COMMON_PREFIX + message, args);
        }
    }

    public void dev(Player receiver, String message, Object... args) {
        if (TreasureSeas.getInstance().getFishConfigManager().isLogDebugModeEnable()) {
            String formattedMessage = formatMessage(COMMON_PREFIX + message, args);
            logger.info(formattedMessage);
            receiver.sendMessage(new TextComponent(formattedMessage), receiver.getUUID());
        }
    }

    private String formatMessage(String message, Object... args) {
        if (args == null) {
            return message;
        }
        for (Object arg : args) {
            message = message.replaceFirst("\\{\\}", arg != null ? arg.toString() : "null");
        }
        return message;
    }

}
