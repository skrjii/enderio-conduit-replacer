package com.soliddowant.enderioconduitreplacer.util;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.IConduitItem;
import crazypants.enderio.base.conduit.IServerConduit;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class ConduitSettingsUtil {

    /**
     * Save all connection settings from a conduit.
     * This includes connection modes, filters, upgrades, and other type-specific
     * settings.
     */
    @Nonnull
    public static Map<EnumFacing, NBTTagCompound> saveSettings(@Nonnull IServerConduit conduit) {
        Map<EnumFacing, NBTTagCompound> settings = new EnumMap<>(EnumFacing.class);

        for (EnumFacing dir : conduit.getExternalConnections()) {
            NBTTagCompound nbt = new NBTTagCompound();
            if (conduit.writeConnectionSettingsToNBT(dir, nbt)) {
                settings.put(dir, nbt);
            }
        }

        return settings;
    }

    /**
     * Restore connection settings to a conduit.
     */
    public static void restoreSettings(@Nonnull IServerConduit conduit,
            @Nonnull Map<EnumFacing, NBTTagCompound> settings) {
        for (Map.Entry<EnumFacing, NBTTagCompound> entry : settings.entrySet()) {
            EnumFacing dir = entry.getKey();
            if (dir != null && conduit.getExternalConnections().contains(dir)) {
                conduit.readConduitSettingsFromNBT(dir, entry.getValue());
            }
        }
    }

    /**
     * Save conduit-to-conduit connections.
     */
    @Nonnull
    public static Set<EnumFacing> saveConduitConnections(@Nonnull IConduit conduit) {
        return new HashSet<>(conduit.getConduitConnections());
    }

    /**
     * Save external (non-conduit) connections.
     */
    @Nonnull
    public static Set<EnumFacing> saveExternalConnections(@Nonnull IConduit conduit) {
        return new HashSet<>(conduit.getExternalConnections());
    }

    /**
     * Check if two conduit items are the same category (can be replaced with each
     * other).
     */
    public static boolean areSameCategory(@Nonnull ItemStack source, @Nonnull ItemStack replacement) {
        if (source.isEmpty() || replacement.isEmpty()) {
            return false;
        }

        if (!(source.getItem() instanceof IConduitItem) || !(replacement.getItem() instanceof IConduitItem)) {
            return false;
        }

        IConduitItem sourceItem = (IConduitItem) source.getItem();
        IConduitItem replacementItem = (IConduitItem) replacement.getItem();

        Class<? extends IConduit> sourceType = sourceItem.getBaseConduitType();
        Class<? extends IConduit> replacementType = replacementItem.getBaseConduitType();

        return sourceType == replacementType;
    }

    /**
     * Check if two conduit items are exactly the same type (same item and
     * damage/metadata).
     */
    public static boolean areExactlySameType(@Nonnull ItemStack source, @Nonnull ItemStack replacement) {
        if (source.isEmpty() || replacement.isEmpty()) {
            return false;
        }

        return source.getItem() == replacement.getItem() && source.getMetadata() == replacement.getMetadata();
    }

    /**
     * Get the category name for display purposes.
     */
    @Nonnull
    public static String getCategoryName(@Nonnull ItemStack conduitItem) {
        if (conduitItem.isEmpty() || !(conduitItem.getItem() instanceof IConduitItem)) {
            return "Unknown";
        }

        IConduitItem item = (IConduitItem) conduitItem.getItem();
        Class<? extends IConduit> baseType = item.getBaseConduitType();

        // Extract a readable name from the class name
        String className = baseType.getSimpleName();
        // Remove "I" prefix if it exists (e.g., IPowerConduit -> PowerConduit)
        if (className.startsWith("I") && className.length() > 1 && Character.isUpperCase(className.charAt(1))) {
            className = className.substring(1);
        }
        // Add spaces before capital letters (e.g., PowerConduit -> Power Conduit)
        StringBuilder result = new StringBuilder();
        for (char c : className.toCharArray()) {
            if (Character.isUpperCase(c) && result.length() > 0) {
                result.append(' ');
            }
            result.append(c);
        }

        return result.toString();
    }
}
