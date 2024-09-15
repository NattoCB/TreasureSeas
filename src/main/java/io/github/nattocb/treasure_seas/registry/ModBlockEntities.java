package io.github.nattocb.treasure_seas.registry;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.shop.block.FishShopBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, TreasureSeas.MOD_ID);

    public static final RegistryObject<BlockEntityType<FishShopBlockEntity>> FISH_BLOCK_ENTITY = BLOCK_ENTITIES.register("fish_shop_block_entity",
            () -> BlockEntityType.Builder.of(FishShopBlockEntity::new, ModBlocks.FISH_SHOP_BLOCK.get()).build(null));

}