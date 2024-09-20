package io.github.nattocb.treasure_seas.submodule.statisticsbook;

import io.github.nattocb.treasure_seas.registry.ModContainerTypes;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.StatisticsMenu;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.StatisticsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import java.util.List;

public class OpenStatisticMenuPacket {
    private final List<ItemStack> itemList;

    public OpenStatisticMenuPacket(List<ItemStack> itemList) {
        this.itemList = itemList;
    }

    public static void encode(OpenStatisticMenuPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.itemList.size());  // 先写入物品列表的长度
        for (ItemStack stack : msg.itemList) {
            buf.writeItem(stack);  // 然后逐个写入物品
        }
    }

    public static OpenStatisticMenuPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();  // 读取列表长度
        NonNullList<ItemStack> itemList = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < size; i++) {
            itemList.set(i, buf.readItem());  // 逐个读取物品
        }
        return new OpenStatisticMenuPacket(itemList);
    }

    public static void handle(OpenStatisticMenuPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    Minecraft.getInstance().setScreen(new StatisticsScreen(
                            new StatisticsMenu(ModContainerTypes.STATISTICS_CONTAINER.get(), 0, msg.itemList),
                            player.getInventory(),
                            new TextComponent("Info Book")));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
