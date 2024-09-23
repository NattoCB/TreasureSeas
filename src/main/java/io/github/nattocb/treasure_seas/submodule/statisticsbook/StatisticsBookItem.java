package io.github.nattocb.treasure_seas.submodule.statisticsbook;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.config.FishWrapper;
import io.github.nattocb.treasure_seas.packet.PacketHandler;
import io.github.nattocb.treasure_seas.utils.FishUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsBookItem extends Item {

    public StatisticsBookItem(Properties properties) {
        super(properties);
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
                    new OpenStatisticMenuPacket(
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
