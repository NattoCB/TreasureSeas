package io.github.nattocb.treasure_seas.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class ModCreativeTabs {

    public static final CreativeModeTab TREASURE_SEAS_TAB = new CreativeModeTab("treasure_seas_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.PIRATE_TREASURE.get());
        }

        @Override
        public Component getDisplayName() {
            return new TranslatableComponent("itemGroup.treasure_seas_tab");
        }
    };

}
