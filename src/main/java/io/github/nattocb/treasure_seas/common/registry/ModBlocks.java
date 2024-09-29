package io.github.nattocb.treasure_seas.common.registry;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.common.block.FishShopBlock;
import io.github.nattocb.treasure_seas.common.item.CustomBlockItem;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * 为方便查找，具有 block 的 item 也一起注册在这里
 */
@Mod.EventBusSubscriber(modid = TreasureSeas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TreasureSeas.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TreasureSeas.MOD_ID);

    public static final RegistryObject<Block> FISH_SHOP_BLOCK = BLOCKS.register("fish_shop",
            () -> new FishShopBlock(BlockBehaviour.Properties.of(Material.WOOD)
                    .strength(0.5F)
                    .noOcclusion()
                    .sound(SoundType.WOOD)
            ));

    public static final RegistryObject<Item> FISH_SHOP_ITEM = ITEMS.register("fish_shop",
            () -> new CustomBlockItem(FISH_SHOP_BLOCK.get(), new Item.Properties().tab(ModCreativeTabs.TREASURE_SEAS_TAB),
                    List.of(
                            new TranslatableComponent("item.treasure_seas.fish_shop.lore1"),
                            new TranslatableComponent("item.treasure_seas.fish_shop.lore2")
                    )
            ));

}
