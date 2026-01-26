package com.soliddowant.enderioconduitreplacer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import crazypants.enderio.base.paint.PaintUtil;
import crazypants.enderio.util.Prep;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(PaintUtil.class)
public class PaintUtilMixin {

    /**
     * Fixes incorrect block name/icon in The One Probe when a painted block is
     * rotated.
     *
     * The original implementation used block.getMetaFromState(state) which
     * includes rotation/placement metadata (facing, top/bottom, axis). This
     * metadata is not valid as an item damage value for most blocks.
     *
     * The fix uses block.damageDropped(state) instead, which returns the item
     * damage value that represents the block variant without placement-specific
     * metadata.
     *
     * @author SolidDoWant
     * @reason Fix TOP displaying wrong block name/icon after rotation
     */
    @Overwrite(remap = false)
    public static ItemStack getPaintAsStack(IBlockState state) {
        if (state != null) {
            Block block = state.getBlock();
            Item itemFromBlock = Item.getItemFromBlock(block);
            if (itemFromBlock != Items.AIR) {
                // Use damageDropped instead of getMetaFromState to get the correct item
                // damage. damageDropped returns the variant metadata without
                // rotation/placement data.
                return new ItemStack(itemFromBlock, 1, block.damageDropped(state));
            }
        }
        return Prep.getEmpty();
    }
}
