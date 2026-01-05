package com.soliddowant.enderioconduitreplacer.gui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.soliddowant.enderioconduitreplacer.network.PacketHandler;
import com.soliddowant.enderioconduitreplacer.network.PacketSetGhostSlot;

import crazypants.enderio.base.conduit.IConduitItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class ConduitGhostSlot extends GhostSlot {

    private final int slotIndex;
    private final Supplier<ItemStack> getter;
    private final Consumer<ItemStack> setter;
    private final Supplier<EnumHand> handSupplier;

    public ConduitGhostSlot(int slotIndex, int x, int y,
            Supplier<ItemStack> getter,
            Consumer<ItemStack> setter,
            Supplier<EnumHand> handSupplier) {
        this.slotIndex = slotIndex;
        this.getter = getter;
        this.setter = setter;
        this.handSupplier = handSupplier;
        setX(x);
        setY(y);
    }

    @Override
    @Nonnull
    public ItemStack getStack() {
        ItemStack stack = getter.get();
        return stack != null ? stack : ItemStack.EMPTY;
    }

    @Override
    public void putStack(@Nonnull ItemStack stack, int realsize) {
        // Only accept conduit items
        if (!stack.isEmpty() && !(stack.getItem() instanceof IConduitItem)) {
            return;
        }

        ItemStack toSet = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        if (!toSet.isEmpty()) {
            toSet.setCount(1);
        }

        // Update local state
        setter.accept(toSet);

        // Send packet to server
        PacketHandler.INSTANCE.sendToServer(new PacketSetGhostSlot(slotIndex, toSet, handSupplier.get()));
    }

    public int getSlotIndex() {
        return slotIndex;
    }
}
