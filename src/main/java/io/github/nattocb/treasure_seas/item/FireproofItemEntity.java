package io.github.nattocb.treasure_seas.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FireproofItemEntity extends ItemEntity {

    public FireproofItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack);
    }

    @Override
    public void tick() {
        super.tick();

        long currentTime = this.level.getGameTime();
        long fireImmuneUntil = this.getPersistentData().getLong("FireImmuneUntil");

        // 在防火时间内，确保物品不会着火
        if (currentTime <= fireImmuneUntil) {
            this.setSecondsOnFire(0);  // 确保物品不着火
            this.fireImmune();  // 使物品在防火时间内免疫火焰
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}