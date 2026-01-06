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
            "A tool for quickly replacing conduits in a network.",
            "",
            TextFormatting.GOLD + "Basic Usage:" + TextFormatting.RESET,
            "1. " + TextFormatting.AQUA + "Middle-click" + TextFormatting.RESET + " a conduit to set it as the " + TextFormatting.GREEN + "source",
            "2. " + TextFormatting.AQUA + "Shift + Middle-click" + TextFormatting.RESET + " a conduit to set it as the " + TextFormatting.GREEN + "replacement",
            "3. " + TextFormatting.AQUA + "Right-click" + TextFormatting.RESET + " to open the GUI and configure",
            "4. " + TextFormatting.AQUA + "Left-click" + TextFormatting.RESET + " conduit bundles to replace matching conduits",
            "",
            TextFormatting.GOLD + "GUI:" + TextFormatting.RESET,
            "• Shows the source and replacement conduit types",
            "• Displays category compatibility (energy, item, fluid, etc.)",
            "• Items can be dragged from JEI into the ghost slots",
            "",
            TextFormatting.GOLD + "Requirements:" + TextFormatting.RESET,
            "• Source and replacement must be the same category",
            "• Replacement conduits consumed from inventory (except creative mode)",
            "• Double-click to confirm if upgrades/filters will be lost",
            "",
            TextFormatting.GRAY + "The replaced conduits will maintain their connections",
            TextFormatting.GRAY + "but may lose some upgrades or filter settings."
        };

        registry.addIngredientInfo(conduitReplacer, VanillaTypes.ITEM, description);
    }
}
