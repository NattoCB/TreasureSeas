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
import net.minecraft.world.inventory.ClickType;
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
public class FishShopInventory extends AbstractContainerMenu {

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
    private int MAX_OUTPUT;
    private boolean isNineStackableOutput;
    private final Item outputItem;

    public FishShopInventory(int id, Inventory playerInventory) {
        super(ModContainerTypes.FISH_SHOP_CONTAINER.get(), id);

        // check config
        Item shopOutputItem = TreasureSeas.getInstance().getFishConfigManager().getShopOutputItem();
        this.MAX_OUTPUT = 64 * OUTPUT_SLOT_ROWS * OUTPUT_SLOT_COLS;
        if (NINE_STACK_ITEMS.containsKey(shopOutputItem)) {
            this.MAX_OUTPUT *= 9;
            this.isNineStackableOutput = true;
        }
        this.outputItem = shopOutputItem;
        TreasureSeas.getLogger().dev("OutputItem: " + shopOutputItem.toString());

        // Input slots
        for (int i = 0; i < INPUT_SLOT_ROWS; ++i) {
            for (int j = 0; j < INPUT_SLOT_COLS; ++j) {
                this.addSlot(new Slot(inputSlots, j + i * INPUT_SLOT_COLS, 8 + j * 18, 18 + i * 18) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack itemStack) {
                        int totalValuesBeforeAdd = calculateTotalInputValues();
                        int totalValuesToAdd = calculateItemStackValues(itemStack);
                        boolean canAdd = totalValuesBeforeAdd + totalValuesToAdd <= MAX_OUTPUT;
                        if (!canAdd) {
                            PlayerMessageManager.sendMessageOnce(playerInventory.player, new TranslatableComponent("message.treasure_seas.exceed_max_outputs"));
                        }
                        return canAdd;
                    }
                    @Override
                    public void setChanged() {
                        if (calculateTotalInputValues() >= MAX_OUTPUT) {
                            TreasureSeas.getLogger().dev("setChanged： cur value: {}/{} setChanged interrupted", calculateTotalInputValues(), MAX_OUTPUT);
                            return;
                        }
                        TreasureSeas.getLogger().dev("setChanged: cur value: {}/{}", calculateTotalInputValues(), MAX_OUTPUT);
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
            totalValues += calculateItemStackValues(itemStack);
        }
        return totalValues;
    }

    private int calculateItemStackValues(ItemStack stack) {
        return calculateSingleItemValue(stack) * stack.getCount();
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
        int requiredSlots = countOccupiedOutputSlots();
        int availableSlots = countAvailablePlayerInventorySlots(player);
        if (availableSlots >= requiredSlots) {
            transferOutputItemsToPlayer(player);
        } else {
            dropOutputItemsToWorld(player);
        }
        clearSaleableItemsFromInputSlots();
        updateOutputSlots();
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

    private void handleCursorItem(Player player, ItemStack cursorItem) {
        int availableSlots = countAvailablePlayerInventorySlots(player);
        if (!cursorItem.isEmpty()) {
            int requiredSlots = (int) Math.ceil((double) cursorItem.getCount() / cursorItem.getMaxStackSize());
            if (availableSlots >= requiredSlots) {
                transferOutputItemToPlayer(player, cursorItem);
            } else {
                dropOutputItemToWorld(player, cursorItem);
            }
        }
    }

    private int countOccupiedOutputSlots() {
        int requiredSlots = 0;
        for (int i = 0; i < outputSlots.getContainerSize(); ++i) {
            ItemStack stack = outputSlots.getItem(i);
            if (!stack.isEmpty()) {
                requiredSlots += (int) Math.ceil((double) stack.getCount() / stack.getMaxStackSize());
            }
        }
        return requiredSlots;
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

    private void transferOutputItemsToPlayer(Player player) {
        for (int i = 0; i < outputSlots.getContainerSize(); ++i) {
            ItemStack stack = outputSlots.getItem(i);
            if (!stack.isEmpty()) {
                // Directly add to player inventory
                player.getInventory().add(stack);
                outputSlots.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private void transferOutputItemToPlayer(Player player, ItemStack stack) {
        while (!stack.isEmpty()) {
            player.getInventory().add(stack.split(stack.getMaxStackSize()));
        }
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        dropInputItemsOnClose(player);
    }

    private void dropInputItemsOnClose(Player player) {
        for (int i = 0; i < inputSlots.getContainerSize(); ++i) {
            ItemStack itemStack = inputSlots.getItem(i);
            if (!itemStack.isEmpty()) {
                player.drop(itemStack, false);
            }
        }
    }

    private void dropOutputItemsToWorld(Player player) {
        for (int i = 0; i < outputSlots.getContainerSize(); ++i) {
            ItemStack stack = outputSlots.getItem(i);
            if (!stack.isEmpty()) {
                player.drop(stack, false);
                outputSlots.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private void dropOutputItemToWorld(Player player, ItemStack stack) {
        while (!stack.isEmpty()) {
            ItemStack dropStack = stack.split(stack.getMaxStackSize());
            player.drop(dropStack, false);
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
    @Override
    public @NotNull void clicked(int slotId, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
        // 获取当前点击的 Slot
        Slot slot = slotId >= 0 && slotId < this.slots.size() ? this.slots.get(slotId) : null;

        // 输入槽位的范围
        int inputSlotStart = 0;
        int inputSlotEnd = INPUT_SLOT_ROWS * INPUT_SLOT_COLS;
        int playerInventoryStart = inputSlotEnd + OUTPUT_SLOT_ROWS * OUTPUT_SLOT_COLS;
        int hotbarSlotStart = playerInventoryStart + PLAYER_INV_ROWS * PLAYER_INV_COLS;
        int hotbarSlotEnd = hotbarSlotStart + HOTBAR_SLOTS;

        ItemStack carriedStack = this.getCarried();

        // 检查是否点击了有效的槽位
        if (slot != null && slotId >= 0) {
            // （1）鼠标左键点击 input slots
            if (clickType == ClickType.PICKUP && dragType == 0 && slotId >= inputSlotStart && slotId < inputSlotEnd) {
                TreasureSeas.getLogger().dev("Left-click on input slot: carriedStack: {}, clickedStack: {}", carriedStack, slot.getItem());
                if (carriedStack.getItem() != Items.AIR) {
                    int totalValuesBeforeAdd = calculateTotalInputValues();
                    int totalValuesToAdd = calculateItemStackValues(carriedStack);
                    TreasureSeas.getLogger().dev("Left-click on input slot: totalValuesBeforeAdd: {}, totalValuesToAdd: {}, MAX_OUTPUT: {}", totalValuesBeforeAdd, totalValuesToAdd, MAX_OUTPUT);
                    if (totalValuesBeforeAdd + totalValuesToAdd > MAX_OUTPUT) {
                        PlayerMessageManager.sendMessageOnce(player, new TranslatableComponent("message.treasure_seas.exceed_max_outputs"));
                        return;
                    }
                }
            }

            // （2）鼠标右键点击 input slots
            else if (clickType == ClickType.PICKUP && dragType == 1 && slotId >= inputSlotStart && slotId < inputSlotEnd) {
                TreasureSeas.getLogger().dev("Right-click on input slot: carriedStack: {}, clickedStack: {}", carriedStack, slot.getItem());
                if (carriedStack.getItem() != Items.AIR) {
                    int totalValuesBeforeAdd = calculateTotalInputValues();
                    int totalValuesToAdd = calculateSingleItemValue(carriedStack);
                    TreasureSeas.getLogger().dev("Right-click on input slot: totalValuesBeforeAdd: {}, totalValuesToAdd: {}, MAX_OUTPUT: {}", totalValuesBeforeAdd, totalValuesToAdd, MAX_OUTPUT);
                    if (totalValuesBeforeAdd + totalValuesToAdd > MAX_OUTPUT) {
                        PlayerMessageManager.sendMessageOnce(player, new TranslatableComponent("message.treasure_seas.exceed_max_outputs"));
                        return;
                    }
                }
            }

            // （3）鼠标 shift + 左键点击 player inv 或 hotbar slots
            else if (clickType == ClickType.QUICK_MOVE && dragType == 0) {
                if (slotId >= playerInventoryStart && slotId < hotbarSlotEnd) {
                    String slotType = (slotId >= hotbarSlotStart) ? "Hotbar" : "Player inventory";
                    TreasureSeas.getLogger().dev("Shift + left-click on " + slotType + " slot: carriedStack: {}, clickedStack: {}", carriedStack, slot.getItem());
                    int totalValuesBeforeAdd = calculateTotalInputValues();
                    ItemStack clickedItemStack = slot.getItem();
                    int totalValuesToAdd = calculateItemStackValues(clickedItemStack);
                    TreasureSeas.getLogger().dev("Shift + left-click on " + slotType + " slot: totalValuesBeforeAdd {}, totalValuesToAdd: {}, MAX_OUTPUT: {}", totalValuesBeforeAdd, totalValuesToAdd, MAX_OUTPUT);
                    if (totalValuesBeforeAdd + totalValuesToAdd > MAX_OUTPUT) {
                        int availableValueToAdd = MAX_OUTPUT - totalValuesBeforeAdd;
                        int singleItemValue = calculateSingleItemValue(clickedItemStack);
                        if (singleItemValue > 0) {
                            int availableCntToAdd = availableValueToAdd / singleItemValue;
                            TreasureSeas.getLogger().dev("Shift + left-click on " + slotType + " slot: availableValueToAdd: {}, availableCntToAdd: {}", availableValueToAdd, availableCntToAdd);
                            if (availableValueToAdd > 0) {
                                ItemStack itemStackToAdd = clickedItemStack.split(availableCntToAdd);
                                TreasureSeas.getLogger().dev("Shift + left-click on " + slotType + " slot: itemStackToAddCnt: {}, clickedItemStackCnt: {}", itemStackToAdd.getCount(), clickedItemStack.getCount());
                                if (!this.moveItemStackTo(itemStackToAdd, 0, inputSlotEnd, false)) {
                                    clickedItemStack.grow(availableCntToAdd);
                                }
                            } else {
                                PlayerMessageManager.sendMessageOnce(player, new TranslatableComponent("message.treasure_seas.exceed_max_outputs"));
                                return;
                            }
                        }
                    }
                }
            }
        }

        // 调用父类的 clicked 以保持正常功能
        super.clicked(slotId, dragType, clickType, player);
    }



}