package io.github.nattocb.treasure_seas.proxy;

import io.github.nattocb.treasure_seas.gui.FishFightGui;
import io.github.nattocb.treasure_seas.config.FishWrapper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.*;
import net.minecraftforge.network.*;
import net.minecraft.world.phys.*;
import net.minecraft.client.*;
import net.minecraft.network.chat.*;

/**
 * Client-side logics - extends from CommonProxy (server-side's logics)
 */
public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        MinecraftForge.EVENT_BUS.register(this);
    }

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

}
