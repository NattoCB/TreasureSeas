package io.github.nattocb.treasure_seas.common.registry;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.common.enchantment.FishFighterEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {

    public static RegistryObject<Enchantment> FISH_FIGHTER;

    public static void register() {
        DeferredRegister<Enchantment> enchantments = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, TreasureSeas.MOD_ID);
        enchantments.register(FMLJavaModLoadingContext.get().getModEventBus());
        FISH_FIGHTER = enchantments.register("fish_fighter", FishFighterEnchantment::new);
    }

}
