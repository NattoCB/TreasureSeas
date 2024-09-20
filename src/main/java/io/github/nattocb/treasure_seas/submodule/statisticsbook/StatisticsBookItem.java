package io.github.nattocb.treasure_seas.submodule.statisticsbook;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.config.FishWrapper;
import io.github.nattocb.treasure_seas.packet.PacketHandler;
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
import java.util.List;
import java.util.Map;

public class StatisticsBookItem extends Item {

    private static final List<ItemStack> itemList = new ArrayList<>();

    public StatisticsBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            List<ItemStack> itemList = getItemList();
            PacketHandler.CHANNEL.sendTo(new OpenStatisticMenuPacket(itemList), serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    // 需要展示的物品列表
    private List<ItemStack> getItemList() {
        if (itemList.isEmpty()) {
            Map<String, FishWrapper> fishWrapperMap = TreasureSeas.getInstance().getFishConfigManager().getFishWrapperMap();
            fishWrapperMap.forEach((k, v) -> {
                ResourceLocation itemLocation = new ResourceLocation(v.getModNamespace(), v.getFishItemName());
                Item item = ForgeRegistries.ITEMS.getValue(itemLocation);
                itemList.add(new ItemStack(item));
            });
        }
        return itemList;
    }
}
