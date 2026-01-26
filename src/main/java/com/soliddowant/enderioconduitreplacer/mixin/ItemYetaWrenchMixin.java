package com.soliddowant.enderioconduitreplacer.mixin;

import javax.annotation.Nonnull;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import crazypants.enderio.base.item.yetawrench.ItemYetaWrench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mixin(ItemYetaWrench.class)
public class ItemYetaWrenchMixin {

    /**
     * When holding ctrl, facades should NOT be hidden even if the Yeta Wrench
     * would normally hide them based on the display mode.
     * 
     * This allows for rotating conduit facades via the Yeta Wrench, which would
     * otherwise be impossible because the facades would be hidden.
     */
    @SideOnly(Side.CLIENT)
    @Inject(method = "shouldHideFacades", at = @At("HEAD"), cancellable = true, remap = false)
    private void onShouldHideFacades(@Nonnull ItemStack stack, @Nonnull EntityPlayer player,
            CallbackInfoReturnable<Boolean> cir) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            cir.setReturnValue(false);
        }
    }
}
