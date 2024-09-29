package io.github.nattocb.treasure_seas.common.block;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.common.blockentity.FishShopBlockEntity;
import io.github.nattocb.treasure_seas.core.gui.menu.FishShopContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FishShopBlock extends Block implements EntityBlock {
    public FishShopBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!world.isClientSide) {
            Item shopOutputItem = TreasureSeas.getInstance().getConfigManager().getShopOutputItem();
            NetworkHooks.openGui((ServerPlayer) player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return new TranslatableComponent("fish.shop.gui");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                    return new FishShopContainerMenu(id, playerInventory, shopOutputItem);
                }
            }, buf -> {
                // 将需要的数据写入缓冲区
                buf.writeRegistryId(shopOutputItem);
            });
        }
        return InteractionResult.SUCCESS;
    }


    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FishShopBlockEntity(pos, state);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootContext.@NotNull Builder builder) {
        return Collections.singletonList(new ItemStack(this));
    }
}