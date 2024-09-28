package io.github.nattocb.treasure_seas.proxy;

import io.github.nattocb.treasure_seas.gui.FishFightGui;
import io.github.nattocb.treasure_seas.config.FishWrapper;
import io.github.nattocb.treasure_seas.registry.ModContainerTypes;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.InfoMenu;
import io.github.nattocb.treasure_seas.submodule.statisticsbook.gui.InfoScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.*;
import net.minecraftforge.network.*;
import net.minecraft.world.phys.*;
import net.minecraft.client.*;
import net.minecraft.network.chat.*;

import java.util.Map;

/**
 * Client-side logics - extends from CommonProxy (server-side's logics)
 */
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOpenFightingGui(final NetworkEvent.Context ctx, final Vec3 bobberPos, final FishWrapper fishWrapper) {
        Minecraft.getInstance().setScreen(
                new FishFightGui(
                        new TextComponent("Fishing..."),
                        bobberPos,
                        fishWrapper
                )
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOpenInfoGui(NetworkEvent.Context ctx, Map<String, FishWrapper> fishWrapperMap, CompoundTag treasureSeasData) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            // Open the StatisticsScreen with the FishWrapper data
            Minecraft.getInstance().setScreen(new InfoScreen(
                    new InfoMenu(
                            ModContainerTypes.STATISTICS_CONTAINER.get(),
                            0,
                            fishWrapperMap,  // data 1
                            treasureSeasData // data 2
                    ),
                    player.getInventory(),
                    new TranslatableComponent("gui.treasure_seas.infoscreen.title")
            ));
        }
    }

}
