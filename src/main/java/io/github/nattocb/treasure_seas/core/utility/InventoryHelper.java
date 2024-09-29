package io.github.nattocb.treasure_seas.core.utility;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryHelper {

    private static int countAvailablePlayerInventorySlots(Player player) {
        int availableSlots = 0;
        for (int i = 0; i < player.getInventory().items.size(); ++i) {
            if (player.getInventory().items.get(i).isEmpty()) {
                availableSlots++;
            }
        }
        return availableSlots;
    }

    public static void moveOrDropItem(ItemStack itemStack, Player player) {
        if (countAvailablePlayerInventorySlots(player) > 0) {
            player.getInventory().add(itemStack);
        } else {
            player.drop(itemStack, false);
        }
    }

}
