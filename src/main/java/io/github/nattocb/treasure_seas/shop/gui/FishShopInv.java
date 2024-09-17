package io.github.nattocb.treasure_seas.shop.gui;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.registry.ModContainerTypes;
import io.github.nattocb.treasure_seas.FishRarity;
import io.github.nattocb.treasure_seas.config.FishWrapper;
import io.github.nattocb.treasure_seas.utils.PlayerMessageManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
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
public class FishShopInv extends AbstractContainerMenu {

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

    // Maximum output count
    private static int MAX_OUTPUT = 64 * OUTPUT_SLOT_ROWS * OUTPUT_SLOT_COLS;
    private boolean isNineStackItem;
    private final Item outputItem;

    public FishShopInv(int id, Inventory playerInventory) {
        super(ModContainerTypes.FISH_SHOP_CONTAINER.get(), id);

        // check config
        Item shopOutputItem = TreasureSeas.getInstance().getFishConfigManager().getShopOutputItem();
        if (NINE_STACK_ITEMS.containsKey(shopOutputItem)) {
            MAX_OUTPUT *= 9;
            this.isNineStackItem = true;
        }
        this.outputItem = shopOutputItem;
        TreasureSeas.getLogger().dev("OutputItem: " + shopOutputItem.toString());

        // Input slots
        for (int i = 0; i < INPUT_SLOT_ROWS; ++i) {
            for (int j = 0; j < INPUT_SLOT_COLS; ++j) {
                this.addSlot(new Slot(inputSlots, j + i * INPUT_SLOT_COLS, 8 + j * 18, 18 + i * 18) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        boolean canAddToInputSlots = canAddToInputSlots(stack, playerInventory.player);
                        if (!canAddToInputSlots) {
                            PlayerMessageManager.sendMessageOnce(playerInventory.player,
                                    new TranslatableComponent("message.treasure_seas.exceed_max_outputs"));
                        }
                        return canAddToInputSlots;
                    }

                    @Override
                    public void setChanged() {
                        super.setChanged();
                        updateOutputSlots();
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
                    public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
                        // 批量转移绿宝石结果
                        handleOutputTaken(player);
                        // 确保鼠标指针上的物品也执行一次背包转移判断，而不是直接被玩家拿到
                        handleCursorItem(player, stack);
                        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0F, 1.0F);
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

    private boolean canAddToInputSlots(ItemStack stack, Player player) {
        // Calculate the current total outputs value in the input slots.
        int currentOutputCount = calculateTotalOutputs();

        // Calculate how much value the new stack will add.
        int stackOutputValue = calculateOutputValue(stack);

        // Combine current value with the new stack value.
        int totalOutputCount = currentOutputCount + stackOutputValue;

        // Check the required number of slots for the total value including non-9-stackable items.
        int requiredOutputSlots = calculateRequiredSlotsForOutput(totalOutputCount, stack);

        // If required slots exceed the available output slots, return false and display a warning.
        if (requiredOutputSlots > outputSlots.getContainerSize()) {
            PlayerMessageManager.sendMessageOnce(player,
                    new TranslatableComponent("message.treasure_seas.exceed_max_outputs"));
            return false;
        }

        return true;
    }
    private int calculateRequiredSlotsForOutput(int outputCount, ItemStack stack) {
        int totalSlotsNeeded = 0;

        // Handle the case for 9-stackable items like emeralds and emerald blocks.
        int outputBlockCount = outputCount / 9;
        int remainingOutputs = outputCount % 9;

        // Each slot can hold up to 64 output blocks.
        totalSlotsNeeded += (int) Math.ceil((double) outputBlockCount / 64);

        // Handle the case for non-9-stackable items (normal 64-stack items like carrots).
        if (!this.isNineStackItem) {
            totalSlotsNeeded += (int) Math.ceil((double) stack.getCount() / 64);
        } else {
            // Handle any remaining outputs that couldn't be stacked into blocks.
            totalSlotsNeeded += (int) Math.ceil((double) remainingOutputs / 64);
        }

        return totalSlotsNeeded;
    }

    private int calculateTotalOutputs() {
        int totalOutputs = 0;
        for (int i = 0; i < inputSlots.getContainerSize(); ++i) {
            ItemStack itemStack = inputSlots.getItem(i);
            totalOutputs += calculateOutputValue(itemStack);
        }
        return totalOutputs;
    }

    private int calculateOutputValue(ItemStack stack) {
        return calculateOutputValueForSingleItem(stack) * stack.getCount();
    }

    private int calculateOutputValueForSingleItem(ItemStack stack) {
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
        int itemCount = calculateTotalOutputs();
        // Fill output slots with output 9-stack blocks first, then output items
        if (this.isNineStackItem) {
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
                    // todo test for max stackable size, shall update the max counter as well
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

    private void clearInputSlots() {
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

    private void handleOutputTaken(Player player) {
        int requiredSlots = calculateRequiredSlots();
        int availableSlots = countAvailableSlots(player);

        if (availableSlots >= requiredSlots) {
            transferItemsToPlayer(player);
        } else {
            dropItemsToWorld(player);
        }
        clearInputSlots();
        // Reset output slots after taking
        updateOutputSlots();
    }


    private void handleCursorItem(Player player, ItemStack cursorItem) {
        int availableSlots = countAvailableSlots(player);
        if (!cursorItem.isEmpty()) {
            int requiredSlots = (int) Math.ceil((double) cursorItem.getCount() / cursorItem.getMaxStackSize());
            if (availableSlots >= requiredSlots) {
                transferItemToPlayerInventory(player, cursorItem);
            } else {
                dropItemInWorld(player, cursorItem);
            }
        }
    }

    private int calculateRequiredSlots() {
        int requiredSlots = 0;
        for (int i = 0; i < outputSlots.getContainerSize(); ++i) {
            ItemStack stack = outputSlots.getItem(i);
            if (!stack.isEmpty()) {
                requiredSlots += (int) Math.ceil((double) stack.getCount() / stack.getMaxStackSize());
            }
        }
        return requiredSlots;
    }

    private int countAvailableSlots(Player player) {
        int availableSlots = 0;
        for (int i = 0; i < player.getInventory().items.size(); ++i) {
            if (player.getInventory().items.get(i).isEmpty()) {
                availableSlots++;
            }
        }
        return availableSlots;
    }

    private void transferItemsToPlayer(Player player) {
        for (int i = 0; i < outputSlots.getContainerSize(); ++i) {
            ItemStack stack = outputSlots.getItem(i);
            if (!stack.isEmpty()) {
                // Directly add to player inventory
                player.getInventory().add(stack);
                outputSlots.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private void dropItemsToWorld(Player player) {
        for (int i = 0; i < outputSlots.getContainerSize(); ++i) {
            ItemStack stack = outputSlots.getItem(i);
            if (!stack.isEmpty()) {
                // Drop item at player's location
                player.drop(stack, false);
                outputSlots.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private void transferItemToPlayerInventory(Player player, ItemStack stack) {
        while (!stack.isEmpty()) {
            player.getInventory().add(stack.split(stack.getMaxStackSize()));
        }
    }

    private void dropItemInWorld(Player player, ItemStack stack) {
        while (!stack.isEmpty()) {
            ItemStack dropStack = stack.split(stack.getMaxStackSize());
            player.drop(dropStack, false);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        dropItemsOnClose(player);
    }

    private void dropItemsOnClose(Player player) {
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
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            int inputSlotEnd = INPUT_SLOT_ROWS * INPUT_SLOT_COLS;
            int outputSlotStart = inputSlotEnd;
            int outputSlotEnd = outputSlotStart + OUTPUT_SLOT_ROWS * OUTPUT_SLOT_COLS;
            int playerInventoryStart = outputSlotEnd;
            int playerInventoryEnd = playerInventoryStart + PLAYER_INV_ROWS * PLAYER_INV_COLS + HOTBAR_SLOTS;

            if (index >= outputSlotStart && index < outputSlotEnd) {
                // Shift-click from output slots should act like a normal left-click
                // or else the items from output slot would be moved into the input slots
                Slot outputSlot = this.slots.get(index);
                ItemStack takenStack = outputSlot.remove(outputSlot.getMaxStackSize()); // Take the entire stack
                outputSlot.onTake(player, takenStack);  // Trigger the normal left-click behavior
                return takenStack;
            } else if (index < inputSlotEnd) {
                // Moving from input slots to player inventory
                if (!this.moveItemStackTo(stackInSlot, playerInventoryStart, playerInventoryEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to input slots
                // Calculate the total outputs value if this item stack is added
                int currentOutputCount = calculateTotalOutputs();
                int stackOutputValue = calculateOutputValue(stackInSlot);
                if (currentOutputCount + stackOutputValue <= MAX_OUTPUT) {
                    // Only move if it won't exceed MAX_OUTPUTS
                    if (!this.moveItemStackTo(stackInSlot, 0, inputSlotEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // If adding would exceed the max outputs, do not move
                    PlayerMessageManager.sendMessageOnce(player,
                            new TranslatableComponent("message.treasure_seas.exceed_max_outputs"));
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

}