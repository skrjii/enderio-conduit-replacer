package com.soliddowant.enderioconduitreplacer.gui;

import javax.annotation.Nonnull;

import com.soliddowant.enderioconduitreplacer.item.ItemConduitReplacer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class ContainerConduitReplacer extends Container {

    private final EntityPlayer player;
    private final EnumHand hand;

    // Ghost slot contents (not actual inventory slots)
    private ItemStack sourceConduit = ItemStack.EMPTY;
    private ItemStack replacementConduit = ItemStack.EMPTY;

    public ContainerConduitReplacer(InventoryPlayer playerInventory, EnumHand hand) {
        this.player = playerInventory.player;
        this.hand = hand;

        // Load current ghost slot contents from item NBT
        ItemStack heldItem = player.getHeldItem(hand);
        if (heldItem.getItem() instanceof ItemConduitReplacer) {
            sourceConduit = ItemConduitReplacer.getSourceConduit(heldItem);
            replacementConduit = ItemConduitReplacer.getReplacementConduit(heldItem);
        }

        // Add player inventory slots (for shift-clicking, but we don't use them much)
        addPlayerInventory(playerInventory);
    }

    private void addPlayerInventory(InventoryPlayer playerInventory) {
        // Player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            int index = col;
            // Don't allow moving the held item
            if (hand == EnumHand.MAIN_HAND && col == playerInventory.currentItem) {
                addSlotToContainer(new LockedSlot(playerInventory, index, 8 + col * 18, 142));
            } else {
                addSlotToContainer(new Slot(playerInventory, index, 8 + col * 18, 142));
            }
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer player) {
        return player.getHeldItem(hand).getItem() instanceof ItemConduitReplacer;
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int index) {
        // Disable shift-clicking for simplicity - ghost slots handle this differently
        return ItemStack.EMPTY;
    }

    // Ghost slot accessors

    @Nonnull
    public ItemStack getSourceConduit() {
        return sourceConduit;
    }

    public void setSourceConduit(@Nonnull ItemStack stack) {
        this.sourceConduit = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        if (sourceConduit.isEmpty())
            return;
        sourceConduit.setCount(1);
    }

    @Nonnull
    public ItemStack getReplacementConduit() {
        return replacementConduit;
    }

    public void setReplacementConduit(@Nonnull ItemStack stack) {
        this.replacementConduit = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        if (replacementConduit.isEmpty())
            return;
        replacementConduit.setCount(1);
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public EnumHand getHand() {
        return hand;
    }

    /**
     * A slot that cannot be interacted with (for the held item slot)
     */
    private static class LockedSlot extends Slot {
        public LockedSlot(InventoryPlayer inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakeStack(@Nonnull EntityPlayer player) {
            return false;
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return false;
        }
    }
}
