package io.github.nattocb.treasure_seas.registry;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.submodule.shop.gui.FishShopContainerMenu;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.StatisticsMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;


public class ModContainerTypes {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, TreasureSeas.MOD_ID);

    public static final RegistryObject<MenuType<FishShopContainerMenu>> FISH_SHOP_CONTAINER = CONTAINERS.register("fish_shop_container",
            () -> IForgeMenuType.create((windowId, inv, data) -> {
                Item shopOutputItem = data.readRegistryId();
                return new FishShopContainerMenu(windowId, inv, shopOutputItem);
            }));

    public static final RegistryObject<MenuType<StatisticsMenu>> STATISTICS_CONTAINER = CONTAINERS.register("statistics_menu",
            () -> IForgeMenuType.create((windowId, inv, data) ->
                    new StatisticsMenu(
                        ModContainerTypes.STATISTICS_CONTAINER.get(),
                        windowId,
                        new HashMap<>(),
                        new CompoundTag()
                    )
            ));

}