package io.github.nattocb.treasure_seas.utils;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber
public class PlayerMessageManager {

    private static final Set<UUID> notifiedPlayers = new HashSet<>();

    /**
     * Send a message to the player if they haven't been notified yet.
     *
     * @param player  The player to send the message to.
     * @param message The message to send.
     */
    public static void sendMessageOnce(@Nullable Player player, Component message) {
        if (player == null) {
            TreasureSeas.getLogger().warn("ModMessageManager.sendMessageOnce: player is null for message: {}", message);
            return;
        }
        if (!hasNotified(player)) {
            player.sendMessage(message, player.getUUID());
            notifyPlayer(player);
        }
    }

    private static boolean hasNotified(Player player) {
        return notifiedPlayers.contains(player.getUUID());
    }

    private static void notifyPlayer(Player player) {
        notifiedPlayers.add(player.getUUID());
    }

    /**
     * Remove a player from the notified set, typically when they interact with their inventory
     * or when a new logical context is started.
     *
     * @param player The player whose notification status should be cleared.
     */
    public static void removePlayerNotification(Player player) {
        notifiedPlayers.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent event) {
        removePlayerNotification(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        removePlayerNotification(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        removePlayerNotification(event.getPlayer());
    }

}
