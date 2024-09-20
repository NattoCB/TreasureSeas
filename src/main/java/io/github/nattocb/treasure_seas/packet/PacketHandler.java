package io.github.nattocb.treasure_seas.packet;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.OpenStatisticMenuPacket;
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
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> TreasureSeas.PROXY.handleOpenFightingGui(
                            ctx.get(),
                            msg.bobberPos(),
                            msg.fishWrapper()
                    ));
                    ctx.get().setPacketHandled(true);
                });

        // Fish fight result - todo 改为单纯在服务端判断
        CHANNEL.registerMessage(
                ID++,
                FishFightResultPacket.class,
                FishFightResultPacket::encode,
                FishFightResultPacket::decode,
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> TreasureSeas.PROXY.handleFightingResult(
                            ctx.get(),
                            msg.bobberPos(),
                            msg.success(),
                            msg.fishWrapper(),
                            msg.isRaining(),
                            msg.isThundering(),
                            msg.isNightTime()
                    ));
                    ctx.get().setPacketHandled(true);
                });

        // Open statistics book
        CHANNEL.registerMessage(
                ID++,
                OpenStatisticMenuPacket.class,
                OpenStatisticMenuPacket::encode,
                OpenStatisticMenuPacket::decode,
                OpenStatisticMenuPacket::handle
        );

    }

}