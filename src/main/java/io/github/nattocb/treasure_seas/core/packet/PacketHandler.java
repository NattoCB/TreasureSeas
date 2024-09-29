package io.github.nattocb.treasure_seas.core.packet;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TreasureSeas.MOD_ID, "fish"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int ID = 0;

    public static void init() {

        // Fish fight start - let client open fighting GUI
        CHANNEL.registerMessage(
                ID++,
                FishFightPacket.class,
                FishFightPacket::encode,
                FishFightPacket::decode,
                FishFightPacket::handle
        );

        // Fish fight result - client send to server
        CHANNEL.registerMessage(
                ID++,
                FishFightResultPacket.class,
                FishFightResultPacket::encode,
                FishFightResultPacket::decode,
                FishFightResultPacket::handle
        );

        // Open statistics book - server to client
        CHANNEL.registerMessage(
                ID++,
                OpenInfoMenuPacket.class,
                OpenInfoMenuPacket::encode,
                OpenInfoMenuPacket::decode,
                OpenInfoMenuPacket::handle
        );

    }

}