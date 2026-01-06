package com.soliddowant.enderioconduitreplacer.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.soliddowant.enderioconduitreplacer.EnderIOConduitReplacerMod;
import com.soliddowant.enderioconduitreplacer.Tags;
import com.soliddowant.enderioconduitreplacer.handler.ConduitReplacementHandler;
import com.soliddowant.enderioconduitreplacer.handler.ReplacementResult;

import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.conduit.IConduitItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemConduitReplacer extends Item {

    private static final String NBT_SOURCE = "SourceConduit";
    private static final String NBT_REPLACEMENT = "ReplacementConduit";

    // Pending confirmation tracking
    private static final Map<UUID, ConfirmationContext> pendingConfirmations = new HashMap<>();

    public ItemConduitReplacer() {
        setRegistryName(Tags.MODID, "conduit_replacer");
        setTranslationKey(Tags.MODID + ".conduit_replacer");
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
                new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player,
            @Nonnull EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);

        if (player.isSneaking()) {
            // Open GUI
            if (!world.isRemote) {
                player.openGui(EnderIOConduitReplacerMod.instance, EnderIOConduitReplacerMod.GUI_CONDUIT_REPLACER,
                        world, (int) player.posX, (int) player.posY, (int) player.posZ);
            }
            return ActionResult.newResult(EnumActionResult.SUCCESS, held);
        }

        return ActionResult.newResult(EnumActionResult.PASS, held);
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUseFirst(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos,
            @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EnumHand hand) {
        if (player.isSneaking()) {
            // Let onItemRightClick handle GUI opening
            return EnumActionResult.PASS;
        }

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IConduitBundle) {
            if (!world.isRemote) {
                performReplacement(player, (IConduitBundle) te, hand, pos);
            }
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    private void performReplacement(EntityPlayer player, IConduitBundle bundle, EnumHand hand, BlockPos pos) {
        ItemStack held = player.getHeldItem(hand);
        ItemStack source = getSourceConduit(held);
        ItemStack replacement = getReplacementConduit(held);

        // Validate that both slots are set
        if (source.isEmpty() || replacement.isEmpty()) {
            player.sendMessage(new TextComponentTranslation(Tags.MODID + ".message.set_both"));
            return;
        }

        // Validate both are conduit items
        if (!(source.getItem() instanceof IConduitItem) || !(replacement.getItem() instanceof IConduitItem)) {
            player.sendMessage(new TextComponentTranslation(Tags.MODID + ".message.invalid_items"));
            return;
        }

        UUID playerId = player.getUniqueID();
        long now = System.currentTimeMillis();

        // Check for pending confirmation
        boolean confirmed = false;
        ConfirmationContext pending = pendingConfirmations.get(playerId);
        if (pending != null && pending.isValid(now) && pending.pos.equals(pos)) {
            confirmed = true;
            pendingConfirmations.remove(playerId);
        }

        ReplacementResult result = ConduitReplacementHandler.performReplacement(
                player, bundle, source, replacement, confirmed);

        handleResult(player, result, pos, now);
    }

    private void handleResult(EntityPlayer player, ReplacementResult result, BlockPos pos, long timestamp) {
        UUID playerId = player.getUniqueID();

        switch (result.getStatus()) {
            case SUCCESS:
                player.sendMessage(new TextComponentTranslation(Tags.MODID + ".message.replaced",
                        result.getReplacedCount()));
                break;

            case INSUFFICIENT_AVAILABLE:
                player.sendMessage(new TextComponentTranslation(Tags.MODID + ".message.insufficient",
                        result.getRequiredCount(), result.getAvailableCount()));
                break;

            case NEEDS_CONFIRMATION_UPGRADES:
                pendingConfirmations.put(playerId, new ConfirmationContext(pos, timestamp));
                player.sendMessage(new TextComponentTranslation(Tags.MODID + ".message.upgrades_drop"));
                break;

            case SOURCE_NOT_FOUND:
                player.sendMessage(new TextComponentTranslation(Tags.MODID + ".message.source_not_found"));
                break;

            case CATEGORY_MISMATCH:
                player.sendMessage(new TextComponentTranslation(Tags.MODID + ".message.category_mismatch"));
                break;

            case INVALID_ITEMS:
                player.sendMessage(new TextComponentTranslation(Tags.MODID + ".message.invalid_items"));
                break;

            case NO_CONDUITS_REPLACED:
                player.sendMessage(new TextComponentTranslation(Tags.MODID + ".message.no_conduits_replaced"));
                break;
        }
    }

    // NBT access methods for ghost slot contents

    @Nonnull
    public static ItemStack getSourceConduit(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(NBT_SOURCE)) {
            return new ItemStack(tag.getCompoundTag(NBT_SOURCE));
        }
        return ItemStack.EMPTY;
    }

    public static void setSourceConduit(@Nonnull ItemStack stack, @Nonnull ItemStack source) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        if (source.isEmpty()) {
            tag.removeTag(NBT_SOURCE);
        } else {
            NBTTagCompound sourceTag = new NBTTagCompound();
            source.writeToNBT(sourceTag);
            tag.setTag(NBT_SOURCE, sourceTag);
        }
    }

    @Nonnull
    public static ItemStack getReplacementConduit(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(NBT_REPLACEMENT)) {
            return new ItemStack(tag.getCompoundTag(NBT_REPLACEMENT));
        }
        return ItemStack.EMPTY;
    }

    public static void setReplacementConduit(@Nonnull ItemStack stack, @Nonnull ItemStack replacement) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        if (replacement.isEmpty()) {
            tag.removeTag(NBT_REPLACEMENT);
        } else {
            NBTTagCompound replacementTag = new NBTTagCompound();
            replacement.writeToNBT(replacementTag);
            tag.setTag(NBT_REPLACEMENT, replacementTag);
        }
    }

    // Confirmation tracking
    private static class ConfirmationContext {
        final BlockPos pos;
        final long timestamp;

        ConfirmationContext(BlockPos pos, long timestamp) {
            this.pos = pos;
            this.timestamp = timestamp;
        }

        boolean isValid(long currentTime) {
            return currentTime - timestamp < 1000; // 1 second window
        }
    }
}
