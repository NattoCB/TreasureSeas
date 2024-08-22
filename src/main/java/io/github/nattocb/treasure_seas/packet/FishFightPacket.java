package io.github.nattocb.treasure_seas.packet;

import io.github.nattocb.treasure_seas.config.FishWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public record FishFightPacket(Vec3 bobberPos, FishWrapper fishWrapper) {

    public FishFightPacket(Vec3 bobberPos, FishWrapper fishWrapper) {
        this.bobberPos = bobberPos;
        this.fishWrapper = fishWrapper;
    }

    public Vec3 bobberPos() {
        return this.bobberPos;
    }

    public FishWrapper fishWrapper() {
        return fishWrapper;
    }

    public static void encode(FishFightPacket msg, FriendlyByteBuf buffer) {
        buffer.writeDouble(msg.bobberPos().x());
        buffer.writeDouble(msg.bobberPos().y());
        buffer.writeDouble(msg.bobberPos().z());
        msg.fishWrapper().writeToBuffer(buffer);
    }

    public static FishFightPacket decode(FriendlyByteBuf buffer) {
        Vec3 bobberPos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        FishWrapper fishWrapper = FishWrapper.readFromBuffer(buffer);
        return new FishFightPacket(bobberPos, fishWrapper);
    }

}