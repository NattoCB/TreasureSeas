package io.github.nattocb.treasure_seas.packet;


import io.github.nattocb.treasure_seas.config.FishWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public record FishFightResultPacket(boolean success, Vec3 bobberPos, FishWrapper fishWrapper, boolean isRaining, boolean isThundering, boolean isNightTime) {
    public FishFightResultPacket(boolean success, Vec3 bobberPos, FishWrapper fishWrapper, boolean isRaining, boolean isThundering, boolean isNightTime) {
        this.success = success;
        this.bobberPos = bobberPos;
        this.fishWrapper = fishWrapper;
        this.isRaining = isRaining;
        this.isThundering = isThundering;
        this.isNightTime = isNightTime;
    }

    public boolean success() {
        return this.success;
    }

    public Vec3 bobberPos() {
        return this.bobberPos;
    }

    public FishWrapper fishWrapper() {
        return this.fishWrapper;
    }

    public boolean isRaining() {
        return this.isRaining;
    }

    public boolean isThundering() {
        return this.isThundering;
    }

    public boolean isNightTime() {
        return this.isNightTime;
    }

    public static void encode(FishFightResultPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.success());
        buffer.writeDouble(msg.bobberPos().x());
        buffer.writeDouble(msg.bobberPos().y());
        buffer.writeDouble(msg.bobberPos().z());
        msg.fishWrapper().writeToBuffer(buffer);
        buffer.writeBoolean(msg.isRaining());
        buffer.writeBoolean(msg.isThundering());
        buffer.writeBoolean(msg.isNightTime());
    }

    public static FishFightResultPacket decode(FriendlyByteBuf buffer) {
        boolean success = buffer.readBoolean();
        Vec3 bobberPos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        FishWrapper fishWrapper = FishWrapper.readFromBuffer(buffer);
        boolean isRaining = buffer.readBoolean();
        boolean isThundering = buffer.readBoolean();
        boolean isNightTime = buffer.readBoolean();
        return new FishFightResultPacket(success, bobberPos, fishWrapper, isRaining, isThundering, isNightTime);
    }

}