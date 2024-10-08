package io.github.nattocb.treasure_seas.common.registry;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.common.item.CustomLoreItem;
import io.github.nattocb.treasure_seas.common.item.EdibleFruitItem;
import io.github.nattocb.treasure_seas.common.item.InfoBookItem;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Mod.EventBusSubscriber(modid = TreasureSeas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TreasureSeas.MOD_ID);


    public static final RegistryObject<Item> PIRATE_TREASURE = ITEMS.register("pirate_treasure", () ->
            new CustomLoreItem(new Item.Properties().stacksTo(64).tab(ModCreativeTabs.TREASURE_SEAS_TAB),
                    List.of(
                            new TranslatableComponent("item.treasure_seas.pirate_treasure.lore1"),
                            new TranslatableComponent("item.treasure_seas.pirate_treasure.lore2")
                    )));

    /**
     * Register Fruit of Power with attribute modification and lore
     */
    public static final RegistryObject<Item> POWER_FRUIT = ITEMS.register("power_fruit", () ->
            new EdibleFruitItem(
                    new Item.Properties().stacksTo(16).tab(ModCreativeTabs.TREASURE_SEAS_TAB),
                    0.5, // Amount to add to attack damage
                    "generic.attack_damage", // Attribute to modify
                    8, // Maximum uses
                    "powerFruitUses", // NBT tag for tracking uses
                    List.of(
                            new TranslatableComponent("item.treasure_seas.power_fruit.lore1"),
                            new TranslatableComponent("item.treasure_seas.power_fruit.lore2")
                    )
            ));

    /**
     * Register Fruit of Vitality with attribute modification and lore
     */
    public static final RegistryObject<Item> LIFE_FRUIT = ITEMS.register("life_fruit", () ->
            new EdibleFruitItem(
                    new Item.Properties().stacksTo(16).tab(ModCreativeTabs.TREASURE_SEAS_TAB),
                    2.0, // Amount to add to max health
                    "generic.max_health", // Attribute to modify
                    3, // Maximum uses
                    "lifeFruitUses", // NBT tag for tracking uses
                    List.of(
                            new TranslatableComponent("item.treasure_seas.life_fruit.lore1"),
                            new TranslatableComponent("item.treasure_seas.life_fruit.lore2")
                    )
            ));

    /**
     * Register Statistics Book Item
     */
    public static final RegistryObject<Item> INFO_BOOK = ITEMS.register("info_book", () ->
            new InfoBookItem(
                    new Item.Properties().stacksTo(1).tab(ModCreativeTabs.TREASURE_SEAS_TAB),
                    List.of(
                            new TranslatableComponent("item.treasure_seas.info_book.lore1")
                    )));

    public static final RegistryObject<Item> GYOTAKU = ITEMS.register("gyotaku", () ->
            new CustomLoreItem(new Item.Properties().stacksTo(64).tab(ModCreativeTabs.TREASURE_SEAS_TAB),
                    List.of(
                            new TranslatableComponent("item.treasure_seas.gyotaku.lore1"),
                            new TranslatableComponent("item.treasure_seas.gyotaku.lore2")
                    )));

}
