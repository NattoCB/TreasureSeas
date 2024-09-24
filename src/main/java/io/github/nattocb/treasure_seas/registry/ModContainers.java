package io.github.nattocb.treasure_seas.registry;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.submodule.shop.gui.FishShopContainerScreen;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.InfoScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TreasureSeas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModContainers {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(ModContainerTypes.FISH_SHOP_CONTAINER.get(), FishShopContainerScreen::new);
        MenuScreens.register(ModContainerTypes.STATISTICS_CONTAINER.get(), InfoScreen::new);
    }

}
