package io.github.nattocb.treasure_seas.submodule.statisticsbook;

import io.github.nattocb.treasure_seas.config.FishWrapper;
import io.github.nattocb.treasure_seas.registry.ModContainerTypes;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.StatisticsMenu;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.StatisticsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class OpenStatisticMenuPacket {
    private final HashMap<String, FishWrapper> fishWrapperMap;

    public OpenStatisticMenuPacket(HashMap<String, FishWrapper> fishWrapperMap) {
        this.fishWrapperMap = fishWrapperMap;
    }

    // Encode method to write HashMap<String, FishWrapper> to the buffer
    public static void encode(OpenStatisticMenuPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.fishWrapperMap.size()); // Write the size of the HashMap
        for (Map.Entry<String, FishWrapper> entry : msg.fishWrapperMap.entrySet()) {
            buf.writeUtf(entry.getKey()); // Write the key (String)
            entry.getValue().writeToBuffer(buf); // Write the FishWrapper object
        }
    }

    // Decode method to read HashMap<String, FishWrapper> from the buffer
    public static OpenStatisticMenuPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt(); // Read the size of the HashMap
        HashMap<String, FishWrapper> fishWrapperMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf(); // Read the key (String)
            FishWrapper fishWrapper = FishWrapper.readFromBuffer(buf); // Read the FishWrapper object
            fishWrapperMap.put(key, fishWrapper);
        }
        return new OpenStatisticMenuPacket(fishWrapperMap);
    }

    // Handle method to process the packet on the client side
    public static void handle(OpenStatisticMenuPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    // Pass the HashMap<String, FishWrapper> to the StatisticsMenu instead of the itemList
                    Minecraft.getInstance().setScreen(new StatisticsScreen(
                            new StatisticsMenu(ModContainerTypes.STATISTICS_CONTAINER.get(), 0, msg.fishWrapperMap),
                            player.getInventory(),
                            new TextComponent("Info Book")
                    ));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
