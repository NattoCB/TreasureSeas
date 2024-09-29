package io.github.nattocb.treasure_seas.core.packet;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.core.FishWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record OpenInfoMenuPacket(Map<String, FishWrapper> fishWrapperMap, CompoundTag treasureSeasData) {

    // Static method to encode the packet to the buffer
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

    // Static method to decode the packet from the buffer
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
            TreasureSeas.PROXY.handleOpenInfoGui(
                    ctx.get(),
                    msg.fishWrapperMap,  // data 1
                    msg.treasureSeasData // data 2
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
