package io.github.nattocb.treasure_seas.common.blockentity;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.common.registry.ModBlockEntities;
import io.github.nattocb.treasure_seas.core.gui.menu.FishShopContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FishShopBlockEntity extends BlockEntity implements MenuProvider {

    public FishShopBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
        Item shopOutputItem = TreasureSeas.getInstance().getFishConfigManager().getShopOutputItem();
        return new FishShopContainerMenu(id, playerInventory, shopOutputItem);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TranslatableComponent("fish.shop.gui");
    }

}