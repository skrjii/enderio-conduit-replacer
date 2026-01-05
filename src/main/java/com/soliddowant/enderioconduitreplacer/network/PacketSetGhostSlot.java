package com.soliddowant.enderioconduitreplacer.network;

import com.soliddowant.enderioconduitreplacer.item.ItemConduitReplacer;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetGhostSlot implements IMessage {

    private int slotIndex; // 0 = source, 1 = replacement
    private ItemStack stack;
    private int hand; // EnumHand ordinal

    public PacketSetGhostSlot() {
        this.stack = ItemStack.EMPTY;
    }

    public PacketSetGhostSlot(int slotIndex, ItemStack stack, EnumHand hand) {
        this.slotIndex = slotIndex;
        this.stack = stack != null ? stack : ItemStack.EMPTY;
        this.hand = hand.ordinal();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slotIndex = buf.readInt();
        stack = ByteBufUtils.readItemStack(buf);
        hand = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slotIndex);
        ByteBufUtils.writeItemStack(buf, stack);
        buf.writeInt(hand);
    }

    public static class Handler implements IMessageHandler<PacketSetGhostSlot, IMessage> {

        @Override
        public IMessage onMessage(PacketSetGhostSlot message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;

            player.getServerWorld().addScheduledTask(() -> {
                EnumHand enumHand = EnumHand.values()[message.hand];
                ItemStack held = player.getHeldItem(enumHand);

                if (held.getItem() instanceof ItemConduitReplacer) {
                    ItemStack toSet = message.stack.isEmpty() ? ItemStack.EMPTY : message.stack.copy();
                    if (!toSet.isEmpty()) {
                        toSet.setCount(1);
                    }

                    if (message.slotIndex == 0) {
                        ItemConduitReplacer.setSourceConduit(held, toSet);
                    } else if (message.slotIndex == 1) {
                        ItemConduitReplacer.setReplacementConduit(held, toSet);
                    }
                }
            });

            return null;
        }
    }
}
