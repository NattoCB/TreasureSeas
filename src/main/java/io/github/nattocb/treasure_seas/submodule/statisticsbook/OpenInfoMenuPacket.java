package io.github.nattocb.treasure_seas.submodule.statisticsbook;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.config.FishWrapper;
import io.github.nattocb.treasure_seas.registry.ModContainerTypes;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.InfoMenu;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.InfoScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class OpenInfoMenuPacket {

    private final Map<String, FishWrapper> fishWrapperMap;

    private final CompoundTag treasureSeasData;

    public OpenInfoMenuPacket(Map<String, FishWrapper> fishWrapperMap, CompoundTag treasureSeasData) {
        this.fishWrapperMap = fishWrapperMap;
        this.treasureSeasData = treasureSeasData;
    }

    // Encode method to write to the buffer
    public static void encode(OpenInfoMenuPacket msg, FriendlyByteBuf buf) {
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
    public static OpenInfoMenuPacket decode(FriendlyByteBuf buf) {
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
        return new OpenInfoMenuPacket(fishWrapperMap, treasureSeasData);
    }

    // Handle method to process the packet on the client side
    public static void handle(OpenInfoMenuPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ctx.get().enqueueWork(() -> TreasureSeas.PROXY.handleOpenInfoGui(
                    ctx.get(),
                    msg.fishWrapperMap,  // data 1
                    msg.treasureSeasData // data 2
            ));
        });
        ctx.get().setPacketHandled(true);
    }

}
