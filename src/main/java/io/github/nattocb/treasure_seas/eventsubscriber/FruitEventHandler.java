package io.github.nattocb.treasure_seas.eventsubscriber;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.item.EdibleFruitItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TreasureSeas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FruitEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemPickup(EntityItemPickupEvent event) {
        ItemStack stack = event.getItem().getItem();
        Player player = event.getPlayer();

        if (stack.getItem() instanceof EdibleFruitItem) {
            if (EdibleFruitItem.getOwner(stack) == null) {
                EdibleFruitItem.setOwner(stack, player);
            } else if (!EdibleFruitItem.isOwner(stack, player)) {
                // Prevent other players from picking up
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemInteract(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getPlayer();

        if (stack.getItem() instanceof EdibleFruitItem && !EdibleFruitItem.isOwner(stack, player)) {
            // Prevent interaction by non-owners
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onInventoryClick(PlayerContainerEvent.Open event) {
        Player player = (Player) event.getEntity();
        player.getInventory().items.forEach(stack -> {
            if (stack.getItem() instanceof EdibleFruitItem && !EdibleFruitItem.isOwner(stack, player)) {
                // Prevent taking the item out of the container by non-owners
                if (event.isCancelable()) event.setCanceled(true);
            }
        });
    }
}