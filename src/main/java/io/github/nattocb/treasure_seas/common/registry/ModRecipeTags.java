package io.github.nattocb.treasure_seas.common.registry;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipeTags {

    public static final TagKey<Item> FISHES = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(),
            new ResourceLocation(TreasureSeas.MOD_ID, "raw_fishes"));

}
