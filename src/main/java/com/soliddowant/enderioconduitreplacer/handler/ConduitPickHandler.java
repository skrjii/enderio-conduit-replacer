package com.soliddowant.enderioconduitreplacer.handler;

import com.soliddowant.enderioconduitreplacer.Tags;
import com.soliddowant.enderioconduitreplacer.item.ItemConduitReplacer;
import com.soliddowant.enderioconduitreplacer.network.PacketHandler;
import com.soliddowant.enderioconduitreplacer.network.PacketSetGhostSlot;

import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.conduit.geom.CollidableComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConduitPickHandler {

    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        // Check for middle-click (button 2)
        if (event.getButton() != 2 || !event.isButtonstate())
            return;

        // Check if player is holding the conduit replacer
        EnumHand hand = null;
        if (player.getHeldItemMainhand().getItem() instanceof ItemConduitReplacer) {
            hand = EnumHand.MAIN_HAND;
        } else if (player.getHeldItemOffhand().getItem() instanceof ItemConduitReplacer) {
            hand = EnumHand.OFF_HAND;
        } else {
            return;
        }

        // Perform ray trace to find what the player is looking at
        RayTraceResult rayTrace = mc.objectMouseOver;
        if (rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.BLOCK)
            return;

        BlockPos pos = rayTrace.getBlockPos();
        if (pos == null)
            return;

        TileEntity te = mc.world.getTileEntity(pos);

        if (!(te instanceof IConduitBundle))
            return;

        IConduitBundle bundle = (IConduitBundle) te;

        // Get the specific conduit from the raytrace hitInfo
        // EnderIO stores the CollidableComponent in hitInfo during raycasting
        IConduit conduit = null;
        if (rayTrace.hitInfo instanceof CollidableComponent) {
            CollidableComponent component = (CollidableComponent) rayTrace.hitInfo;
            if (component.conduitType != null) {
                conduit = bundle.getConduit(component.conduitType);
            }
        }

        if (conduit == null)
            return;

        ItemStack pickedStack = conduit.createItem();
        if (pickedStack.isEmpty())
            return;

        // Determine which slot to set based on shift key
        // Shift + middle-click = replacement (slot 1), normal middle-click = source
        // (slot 0)
        int slotIndex = player.isSneaking() ? 1 : 0;
        String messageKey = player.isSneaking()
                ? Tags.MODID + ".message.replacement_set"
                : Tags.MODID + ".message.source_set";

        // Send packet to server with the picked conduit
        PacketHandler.INSTANCE.sendToServer(new PacketSetGhostSlot(slotIndex, pickedStack, hand));

        // Send client-side chat message
        player.sendMessage(new TextComponentTranslation(messageKey, pickedStack.getDisplayName()));

        // Trigger item swing animation (send packet to sync with server)
        mc.getConnection().sendPacket(new CPacketAnimation(hand));

        // Cancel the event to prevent default behavior
        event.setCanceled(true);
    }
}
