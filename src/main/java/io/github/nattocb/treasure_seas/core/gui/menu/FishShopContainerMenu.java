package io.github.nattocb.treasure_seas.core.gui.menu;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.common.registry.ModContainerTypes;
import io.github.nattocb.treasure_seas.common.FishRarity;
import io.github.nattocb.treasure_seas.core.FishWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * todo 兼容 quark quick inv change and sort？
 */
public class FishShopContainerMenu extends AbstractContainerMenu {

    private static final Map<Item, Item> NINE_STACK_ITEMS = new HashMap<>();
    static {
        NINE_STACK_ITEMS.put(Items.IRON_INGOT, Items.IRON_BLOCK);
        NINE_STACK_ITEMS.put(Items.GOLD_INGOT, Items.GOLD_BLOCK);
        NINE_STACK_ITEMS.put(Items.EMERALD, Items.EMERALD_BLOCK);
        NINE_STACK_ITEMS.put(Items.DIAMOND, Items.DIAMOND_BLOCK);
    }

    private static final int INPUT_SLOT_ROWS = 3;
    private static final int INPUT_SLOT_COLS = 5;
    private static final int OUTPUT_SLOT_ROWS = 3;
    private static final int OUTPUT_SLOT_COLS = 3;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HOTBAR_SLOTS = 9;

    private final Container inputSlots = new SimpleContainer(INPUT_SLOT_ROWS * INPUT_SLOT_COLS);
    private final Container outputSlots = new SimpleContainer(OUTPUT_SLOT_ROWS * OUTPUT_SLOT_COLS);

    private boolean isNineStackableOutput;

    private final Item outputItem;

    private int totalValues = 0;

    private void updateTotalInputValues() {
        this.totalValues = calculateTotalInputValues();
    }

    public int getTotalValues() {
        return totalValues;
    }

    public FishShopContainerMenu(int id, Inventory playerInventory, Item shopOutputItem) {
        super(ModContainerTypes.FISH_SHOP_CONTAINER.get(), id);

        if (NINE_STACK_ITEMS.containsKey(shopOutputItem)) {
            this.isNineStackableOutput = true;
        }
        this.outputItem = shopOutputItem;

        TreasureSeas.getLogger().dev("OutputItem: " + shopOutputItem.toString());

        // Input slots
        for (int i = 0; i < INPUT_SLOT_ROWS; ++i) {
            for (int j = 0; j < INPUT_SLOT_COLS; ++j) {
                this.addSlot(new Slot(inputSlots, j + i * INPUT_SLOT_COLS, 8 + j * 18, 18 + i * 18) {
                    @Override
                    public void setChanged() {
                        super.setChanged();
                        updateOutputSlots();
                        updateTotalInputValues();
                    }
                });
            }
        }

        // Output slots positioned in a grid to the right of input slots
        int startX = 26 + 18 * INPUT_SLOT_COLS;
        int startY = 18;

        for (int i = 0; i < OUTPUT_SLOT_ROWS; ++i) {
            for (int j = 0; j < OUTPUT_SLOT_COLS; ++j) {
                int slotIndex = j + i * OUTPUT_SLOT_COLS;
                this.addSlot(new Slot(outputSlots, slotIndex, startX + j * 18, startY + i * 18) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(@NotNull Player player) {
                        // cannot click output slot with item carried on cursor
                        ItemStack carriedStack = player.containerMenu.getCarried();
                        if (!carriedStack.isEmpty()) {
                            return false;
                        }
                        return super.mayPickup(player);
                    }

                    @Override
                    public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
                        if (!player.level.isClientSide) {
                            // transfer output slot into player inv
                            handleOutputTaken(player);
                            // prevent player from taking item from output slots to cursor
                            player.containerMenu.getCarried().setCount(0);
                            // clear related input slots
                            clearSaleableItemsFromInputSlots();
                            // clear all output slots
                            for (int i = 0; i < outputSlots.getContainerSize(); ++i) {
                                outputSlots.setItem(i, ItemStack.EMPTY);
                            }
                            // sync to client
                            outputSlots.setChanged();
                        }
                        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                });
            }
        }

        // Player inventory
        int playerInvStartY = 84;
        for (int i = 0; i < PLAYER_INV_ROWS; ++i) {
            for (int j = 0; j < PLAYER_INV_COLS; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * PLAYER_INV_COLS + HOTBAR_SLOTS, 8 + j * 18, playerInvStartY + i * 18));
            }
        }

        int hotbarStartY = 142;
        for (int i = 0; i < HOTBAR_SLOTS; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, hotbarStartY));
        }

        // Initialize the output slots
        updateOutputSlots();
    }

    private int calculateTotalInputValues() {
        int totalValues = 0;
        for (int i = 0; i < inputSlots.getContainerSize(); ++i) {
            ItemStack itemStack = inputSlots.getItem(i);
            totalValues += (calculateSingleItemValue(itemStack) * itemStack.getCount());
        }
        return totalValues;
    }

    private int calculateSingleItemValue(ItemStack stack) {
        ResourceLocation registryName = stack.getItem().getRegistryName();
        if (registryName != null) {
            String namespace = registryName.getNamespace();
            String itemName = registryName.getPath();
            FishWrapper fishWrapper = TreasureSeas.getInstance().getFishConfigManager().getFishWrapperMap().get(namespace + ":" + itemName);
            if (fishWrapper != null) {
                int basePrice = fishWrapper.getBasePrice();
                CompoundTag fishTag = stack.getOrCreateTag();
                FishRarity fishRarity = FishRarity.fromName(fishTag.getString("rarity"));
                if (fishRarity != null) {
                    boolean isShiny = fishTag.getBoolean("isShiny");
                    return isShiny ?
                            (int) (basePrice * fishRarity.getPriceMultiplier() * 5) :
                            (int) (basePrice * fishRarity.getPriceMultiplier());
                } else {
                    // e.g. junk, normal treasures, ultimateTreasures, unModded fish items but id within the config
                    if (fishWrapper.isUltimateTreasure()) {
                        return basePrice;
                    } else {
                        // junk / un-modded fish, do not make it saleable for preventing auto-emerald-machines
                        return 0;
                    }
                }
            }
        }
        // If no matching FishWrapper is found, return zero value
        return 0;
    }

    private void updateOutputSlots() {
        int itemCount = calculateTotalInputValues();
        // Fill output slots with output 9-stack blocks first, then output items
        if (this.isNineStackableOutput) {
            int outputBlockCount = itemCount / 9;
            int outputCount = itemCount % 9;
            for (int i = 0; i < outputSlots.getContainerSize(); ++i) {
                if (outputBlockCount > 0) {
                    // Up to 64 blocks per slot
                    int countToPlace = Math.min(outputBlockCount, 64);
                    outputSlots.setItem(i, new ItemStack(NINE_STACK_ITEMS.get(this.outputItem), countToPlace));
                    outputBlockCount -= countToPlace;
                } else if (outputCount > 0) {
                    outputSlots.setItem(i, new ItemStack(this.outputItem, outputCount));
                    // All remaining output items placed
                    outputCount = 0;
                } else {
                    // Clear remaining slots
                    outputSlots.setItem(i, ItemStack.EMPTY);
                }
            }
        } else {
            // Fill output slots
            for (int i = 0; i < outputSlots.getContainerSize(); ++i) {
                if (itemCount > 0) {
                    // Up to 64 blocks per slot
                    int countToPlace = Math.min(itemCount, 64);
                    outputSlots.setItem(i, new ItemStack(this.outputItem, countToPlace));
                    itemCount -= countToPlace;
                } else {
                    // Clear remaining slots
                    outputSlots.setItem(i, ItemStack.EMPTY);
                }
            }
        }
        outputSlots.setChanged();
    }

    private void handleOutputTaken(Player player) {
        int totalInputValues = this.calculateTotalInputValues();
        if (this.isNineStackableOutput) {
            // nine-stackable output
            int cntNineStackedItems = totalInputValues / 9;
            for (int i = 0; i < cntNineStackedItems / 64; ++i) {
                ItemStack itemStack = new ItemStack(NINE_STACK_ITEMS.get(outputItem), 64);
                moveOrDropItem(itemStack, player);
            }
            int cntNineStackedRemainingCnt = cntNineStackedItems % 64;
            if (cntNineStackedRemainingCnt > 0) {
                ItemStack itemStack = new ItemStack(NINE_STACK_ITEMS.get(outputItem), cntNineStackedRemainingCnt);
                moveOrDropItem(itemStack, player);
            }
            int cntRemainingItems = totalInputValues - cntNineStackedItems * 9;
            if (cntRemainingItems > 0) {
                ItemStack itemStack = new ItemStack(outputItem, cntRemainingItems);
                moveOrDropItem(itemStack, player);
            }
        } else {
            // non-nine-stackable output
            int cntRemainingItems = totalInputValues % 64;
            for (int i = 0; i < totalInputValues / 64; ++i) {
                ItemStack itemStack = new ItemStack(outputItem, 64);
                moveOrDropItem(itemStack, player);
            }
            if (cntRemainingItems > 0) {
                ItemStack itemStack = new ItemStack(outputItem, cntRemainingItems);
                moveOrDropItem(itemStack, player);
            }
        }
    }

    private void clearSaleableItemsFromInputSlots() {
        for (int i = 0; i < inputSlots.getContainerSize(); ++i) {
            // 仅删除配置过的 fish，且不删除 basePrice = 0 的 fish
            ItemStack itemStack = inputSlots.getItem(i);
            ResourceLocation registryName = itemStack.getItem().getRegistryName();
            if (registryName != null) {
                String namespace = registryName.getNamespace();
                String itemName = registryName.getPath();
                FishWrapper fishWrapper = TreasureSeas.getInstance().getFishConfigManager().getFishWrapperMap().get(namespace + ":" + itemName);
                if (fishWrapper != null) {
                    int basePrice = fishWrapper.getBasePrice();
                    if (basePrice > 0) {
                        inputSlots.setItem(i, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    private int countAvailablePlayerInventorySlots(Player player) {
        int availableSlots = 0;
        for (int i = 0; i < player.getInventory().items.size(); ++i) {
            if (player.getInventory().items.get(i).isEmpty()) {
                availableSlots++;
            }
        }
        return availableSlots;
    }

    private void moveOrDropItem(ItemStack itemStack, Player player) {
        if (countAvailablePlayerInventorySlots(player) > 0) {
            player.getInventory().add(itemStack);
        } else {
            player.drop(itemStack, false);
        }
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        // drop items from input slots
        for (int i = 0; i < inputSlots.getContainerSize(); ++i) {
            ItemStack itemStack = inputSlots.getItem(i);
            if (!itemStack.isEmpty()) {
                player.drop(itemStack, false);
            }
        }
    }

    @Override
    public void slotsChanged(@NotNull Container container) {
        super.slotsChanged(container);
        updateOutputSlots();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();

            int inputSlotEnd = INPUT_SLOT_ROWS * INPUT_SLOT_COLS;
            int outputSlotStart = inputSlotEnd;
            int outputSlotEnd = outputSlotStart + OUTPUT_SLOT_ROWS * OUTPUT_SLOT_COLS;
            int playerInventoryStart = outputSlotEnd;
            int playerInventoryEnd = playerInventoryStart + PLAYER_INV_ROWS * PLAYER_INV_COLS + HOTBAR_SLOTS;

            if (index >= outputSlotStart && index < outputSlotEnd) {
                // Shift-click from output slots should act like a normal left-click
                // or else the items from output slot would be moved into the input slots
                TreasureSeas.getLogger().dev("ShopMove: oupS->pInv");
                Slot outputSlot = this.slots.get(index);
                ItemStack takenStack = outputSlot.remove(outputSlot.getMaxStackSize()); // Take the entire stack
                outputSlot.onTake(player, takenStack);  // Trigger the normal left-click behavior
                return takenStack;
            } else if (index < inputSlotEnd) {
                // inputSlot -> pInv
                TreasureSeas.getLogger().dev("ShopMove: InpS->pInv");
                if (!this.moveItemStackTo(stackInSlot, playerInventoryStart, playerInventoryEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // pInv -> inputSlot
                TreasureSeas.getLogger().dev("ShopMove: pInv->InpS");
                if (!this.moveItemStackTo(stackInSlot, 0, inputSlotEnd, false)) {
                        return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return ItemStack.EMPTY;
    }

}