package com.soliddowant.enderioconduitreplacer.integration.jei;

import javax.annotation.Nonnull;

import com.soliddowant.enderioconduitreplacer.init.ModItems;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

@JEIPlugin
public class JeiPlugin implements IModPlugin {

    @Override
    public void register(@Nonnull IModRegistry registry) {
        // Add info page for the Conduit Replacer
        addConduitReplacerInfo(registry);
    }

    private void addConduitReplacerInfo(@Nonnull IModRegistry registry) {
        ItemStack conduitReplacer = new ItemStack(ModItems.CONDUIT_REPLACER);

        String[] description = new String[] {
            TextFormatting.WHITE.toString() + TextFormatting.BOLD + "Conduit Replacer" + TextFormatting.RESET,
            "",
            "A tool for replacing all conduits of a given type.",
            "",
            "Basic Usage:",
            "1. " + TextFormatting.AQUA + "Right-click" + TextFormatting.RESET + " to open the GUI and configure, or " + TextFormatting.AQUA + "(Shift) Middle-click" + TextFormatting.RESET + " a conduit to set source/replacement",
            "2. " + TextFormatting.AQUA + "Right-click" + TextFormatting.RESET + " conduit bundles to replace matching conduits",
            "",
            "Items can be dragged from JEI into the GUI slots.",
            "",
            "Requirements:",
            "• Source and replacement must be the same category",
            "• Replacement conduits must be in your inventory",
        };

        registry.addIngredientInfo(conduitReplacer, VanillaTypes.ITEM, description);
    }
}
