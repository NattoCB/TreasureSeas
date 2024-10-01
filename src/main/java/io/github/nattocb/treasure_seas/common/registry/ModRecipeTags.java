package io.github.nattocb.treasure_seas.common.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModRecipeTags {

    public static final TagKey<Item> RAW_FISHES =
            ItemTags.create(new ResourceLocation("minecraft", "raw_fishes"));

}
