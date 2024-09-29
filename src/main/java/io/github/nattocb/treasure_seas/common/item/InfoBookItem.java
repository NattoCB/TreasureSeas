package io.github.nattocb.treasure_seas.common.item;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.core.FishWrapper;
import io.github.nattocb.treasure_seas.core.packet.PacketHandler;
import io.github.nattocb.treasure_seas.core.packet.OpenInfoMenuPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class InfoBookItem extends CustomLoreItem {

    public InfoBookItem(Properties properties, List<Component> lore) {
        super(properties, lore);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // param 1
            Map<String, FishWrapper> fishWrapperMap = TreasureSeas.getInstance().getFishConfigManager().getFishWrapperMap();
            // param 2
            CompoundTag playerData = player.getPersistentData();
            CompoundTag treasureSeasTag = playerData.getCompound("treasureSeas");
            if (treasureSeasTag.isEmpty()) {
                treasureSeasTag = new CompoundTag();
                playerData.put("treasureSeas", treasureSeasTag);
            }
            // send packet
            PacketHandler.CHANNEL.sendTo(
                    new OpenInfoMenuPacket(
                            fishWrapperMap, // data 1
                            treasureSeasTag // data 2
                    ),
                    serverPlayer.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

}
