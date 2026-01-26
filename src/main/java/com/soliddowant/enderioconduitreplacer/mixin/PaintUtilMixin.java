package com.soliddowant.enderioconduitreplacer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import crazypants.enderio.base.paint.PaintUtil;
import crazypants.enderio.util.Prep;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
     */
    @Inject(method = "getPaintAsStack", at = @At("RETURN"), cancellable = true, remap = false)
    private static void fixPaintAsStackMetadata(IBlockState state, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack originalResult = cir.getReturnValue();

        // Only fix if we got a valid ItemStack back
        if (!Prep.isValid(originalResult) || state == null)
            return;

        Block block = state.getBlock();
        int correctDamage = block.damageDropped(state);

        // Only create a new stack if the damage value actually differs
        if (originalResult.getItemDamage() != correctDamage) {
            cir.setReturnValue(new ItemStack(originalResult.getItem(), 1, correctDamage));
        }
    }
}
