package io.github.nattocb.treasure_seas.submodule.statisticsbook.gui;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class TutorialMenu extends AbstractContainerMenu {

    public TutorialMenu(int id) {
        super(null, id); // You can replace null with a proper MenuType if needed
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true; // This method defines if the menu is still valid for interaction
    }
}
