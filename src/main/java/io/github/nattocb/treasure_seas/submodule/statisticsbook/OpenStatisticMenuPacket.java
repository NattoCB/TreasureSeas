package io.github.nattocb.treasure_seas.submodule.statisticsbook;

import io.github.nattocb.treasure_seas.config.FishWrapper;
import io.github.nattocb.treasure_seas.registry.ModContainerTypes;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.StatisticsMenu;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.StatisticsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class OpenStatisticMenuPacket {

    private final Map<String, FishWrapper> fishWrapperMap;

    private final CompoundTag treasureSeasData;

    public OpenStatisticMenuPacket(Map<String, FishWrapper> fishWrapperMap, CompoundTag treasureSeasData) {
        this.fishWrapperMap = fishWrapperMap;
        this.treasureSeasData = treasureSeasData;
    }

    // Encode method to write to the buffer
    public static void encode(OpenStatisticMenuPacket msg, FriendlyByteBuf buf) {
        // param 1
        buf.writeVarInt(msg.fishWrapperMap.size()); // Write the size of the HashMap
        for (Map.Entry<String, FishWrapper> entry : msg.fishWrapperMap.entrySet()) {
            buf.writeUtf(entry.getKey()); // Write the key (String)
            entry.getValue().writeToBuffer(buf); // Write the FishWrapper object
        }
        // param 2
        buf.writeNbt(msg.treasureSeasData); // Only send treasureSeas compound
    }

    // Decode method to read from the buffer
    public static OpenStatisticMenuPacket decode(FriendlyByteBuf buf) {
        // param 1
        int size = buf.readVarInt(); // Read the size of the HashMap
        HashMap<String, FishWrapper> fishWrapperMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf(); // Read the key (String)
            FishWrapper fishWrapper = FishWrapper.readFromBuffer(buf); // Read the FishWrapper object
            fishWrapperMap.put(key, fishWrapper);
        }
        // param 2
        CompoundTag treasureSeasData = buf.readNbt(); // Read treasureSeas compound
        return new OpenStatisticMenuPacket(fishWrapperMap, treasureSeasData);
    }

    // Handle method to process the packet on the client side
    public static void handle(OpenStatisticMenuPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    // Open the StatisticsScreen with the FishWrapper data
                    Minecraft.getInstance().setScreen(new StatisticsScreen(
                            new StatisticsMenu(
                                    ModContainerTypes.STATISTICS_CONTAINER.get(),
                                    0,
                                    msg.fishWrapperMap,  // data 1
                                    msg.treasureSeasData // data 2
                            ),
                            player.getInventory(),
                            // todo i18n
                            new TextComponent("Info Book")
                    ));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
