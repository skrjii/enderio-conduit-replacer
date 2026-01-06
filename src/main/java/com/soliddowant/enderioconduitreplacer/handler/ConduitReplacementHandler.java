package com.soliddowant.enderioconduitreplacer.handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NNList;
import com.soliddowant.enderioconduitreplacer.EnderIOConduitReplacerMod;
import com.soliddowant.enderioconduitreplacer.util.ConduitSettingsUtil;

import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.conduit.IConduitItem;
import crazypants.enderio.base.conduit.IServerConduit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConduitReplacementHandler {

    /**
     * Perform conduit replacement across a network.
     *
     * @param player          The player performing the replacement
     * @param startBundle     The conduit bundle that was clicked
     * @param sourceItem      The source conduit item (what to replace)
     * @param replacementItem The replacement conduit item (what to replace with)
     * @param confirmed       Whether the player has confirmed warnings
     * @return The result of the replacement operation
     */
    @Nonnull
    public static ReplacementResult performReplacement(
            @Nonnull EntityPlayer player,
            @Nonnull IConduitBundle startBundle,
            @Nonnull ItemStack sourceItem,
            @Nonnull ItemStack replacementItem,
            boolean confirmed) {

        // Validate inputs
        if (sourceItem.isEmpty() || replacementItem.isEmpty()) {
            return ReplacementResult.invalidItems();
        }

        if (!(sourceItem.getItem() instanceof IConduitItem) || !(replacementItem.getItem() instanceof IConduitItem)) {
            return ReplacementResult.invalidItems();
        }

        // Validate same category
        if (!ConduitSettingsUtil.areSameCategory(sourceItem, replacementItem)) {
            return ReplacementResult.categoryMismatch();
        }

        // Don't replace if source and replacement are identical
        if (isSameConduitType(sourceItem, replacementItem)) {
            return ReplacementResult.noConduitsReplaced();
        }

        IConduitItem sourceConduitItem = (IConduitItem) sourceItem.getItem();
        Class<? extends IConduit> baseType = sourceConduitItem.getBaseConduitType();

        // Check if source conduit exists in clicked bundle
        IConduit startConduit = startBundle.getConduit(baseType);
        if (startConduit == null) {
            return ReplacementResult.sourceNotFound();
        }

        // Traverse network using BFS to find all conduits to replace
        List<ConduitLocation> toReplace = traverseNetwork(startBundle, baseType, sourceItem);

        if (toReplace.isEmpty()) {
            return ReplacementResult.noConduitsReplaced();
        }

        // Count required replacements and check inventory
        int required = toReplace.size();
        int available = countItemsInInventory(player, replacementItem);

        // Require full inventory - no partial replacements (skip in creative mode)
        if (!player.capabilities.isCreativeMode && available < required) {
            return ReplacementResult.insufficientAvailable(required, available);
        }

        // Check if any conduits have upgrades that might not be preservable
        boolean hasUpgradesAtRisk = checkForUpgradesAtRisk(toReplace, sourceItem, replacementItem);

        // Handle upgrade warning
        if (!confirmed && hasUpgradesAtRisk) {
            return ReplacementResult.needsConfirmationUpgrades();
        }

        // Perform the actual replacement
        int replaced = 0;

        for (ConduitLocation loc : toReplace) {
            if (replaceConduit(player, loc, replacementItem)) {
                replaced++;
            }
        }

        if (replaced == 0) {
            return ReplacementResult.noConduitsReplaced();
        }

        return ReplacementResult.success(replaced);
    }

    /**
     * Traverse the conduit network using BFS to find all conduits of the specified
     * type.
     */
    @Nonnull
    private static List<ConduitLocation> traverseNetwork(
            @Nonnull IConduitBundle startBundle,
            @Nonnull Class<? extends IConduit> baseType,
            @Nonnull ItemStack sourceItem) {

        List<ConduitLocation> result = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<IConduitBundle> queue = new LinkedList<>();

        queue.add(startBundle);

        while (!queue.isEmpty()) {
            IConduitBundle bundle = queue.poll();
            BlockPos pos = bundle.getLocation();

            if (visited.contains(pos)) {
                continue;
            }
            visited.add(pos);

            IConduit conduit = bundle.getConduit(baseType);
            if (conduit == null) {
                continue;
            }

            // Check if this conduit matches the source item type
            // (same item and damage value means same specific conduit variant)
            ItemStack conduitDrop = conduit.createItem();
            if (isSameConduitType(conduitDrop, sourceItem)) {
                result.add(new ConduitLocation(bundle, conduit, pos));
            }

            // Add connected bundles to queue
            World world = bundle.getBundleworld();
            for (EnumFacing dir : conduit.getConduitConnections()) {
                BlockPos neighborPos = pos.offset(dir);
                if (!visited.contains(neighborPos)) {
                    TileEntity te = world.getTileEntity(neighborPos);
                    if (te instanceof IConduitBundle) {
                        queue.add((IConduitBundle) te);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Check if two conduit items represent the same conduit type (item and
     * metadata).
     */
    private static boolean isSameConduitType(@Nonnull ItemStack a, @Nonnull ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        return a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata();
    }

    /**
     * Count how many of a specific item the player has in their inventory.
     */
    private static int countItemsInInventory(@Nonnull EntityPlayer player, @Nonnull ItemStack targetItem) {
        int count = 0;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == targetItem.getItem()
                    && stack.getMetadata() == targetItem.getMetadata()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Check if any conduits have upgrades that might not be preservable with the
     * new conduit type.
     */
    private static boolean checkForUpgradesAtRisk(
            @Nonnull List<ConduitLocation> conduits,
            @Nonnull ItemStack sourceItem,
            @Nonnull ItemStack replacementItem) {

        // If replacing with the exact same type, upgrades are always preserved
        if (isSameConduitType(sourceItem, replacementItem)) {
            return false;
        }

        // Check if any conduit has drops beyond just the conduit itself
        // (indicating filters, upgrades, etc.)
        for (ConduitLocation loc : conduits) {
            NNList<ItemStack> drops = loc.conduit.getDrops();
            if (drops.size() > 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Replace a single conduit.
     */
    private static boolean replaceConduit(
            @Nonnull EntityPlayer player,
            @Nonnull ConduitLocation loc,
            @Nonnull ItemStack replacementItem) {

        IConduitBundle bundle = loc.bundle;
        IConduit oldConduit = loc.conduit;

        if (!(oldConduit instanceof IServerConduit)) {
            return false;
        }

        IServerConduit oldServerConduit = (IServerConduit) oldConduit;

        try {
            // 1. Save settings from old conduit
            Map<EnumFacing, NBTTagCompound> settings = ConduitSettingsUtil.saveSettings(oldServerConduit);
            Set<EnumFacing> conduitConnections = ConduitSettingsUtil.saveConduitConnections(oldConduit);
            Set<EnumFacing> externalConnections = ConduitSettingsUtil.saveExternalConnections(oldConduit);

            // 2. Get old conduit drops (includes upgrades/filters)
            NNList<ItemStack> drops = oldConduit.getDrops();

            // 3. Remove old conduit
            bundle.removeConduit(oldConduit);

            // 4. Create and add new conduit
            IConduitItem item = (IConduitItem) replacementItem.getItem();
            IServerConduit newConduit = item.createConduit(replacementItem, player);

            if (newConduit == null) {
                // Failed to create new conduit - try to restore old one
                EnderIOConduitReplacerMod.LOGGER.warn("Failed to create new conduit at {}", loc.pos);
                return false;
            }

            boolean added = bundle.addConduit(newConduit);
            if (!added) {
                EnderIOConduitReplacerMod.LOGGER.warn("Failed to add new conduit to bundle at {}", loc.pos);
                return false;
            }

            // 5. Re-establish conduit connections
            for (EnumFacing dir : conduitConnections) {
                newConduit.conduitConnectionAdded(dir);
            }

            // 6. Re-establish external connections
            for (EnumFacing dir : externalConnections) {
                newConduit.externalConnectionAdded(dir);
            }

            // 7. Restore settings to new conduit
            ConduitSettingsUtil.restoreSettings(newConduit, settings);

            if (!player.capabilities.isCreativeMode) {
                // 8. Handle inventory: consume replacement from player (skip in creative mode)
                consumeFromInventory(player, replacementItem, 1);

                // 9. Return old conduit drops to player (skip in creative mode)
                for (ItemStack drop : drops) {
                    if (!drop.isEmpty()) {
                        // Skip the base conduit item itself - we're swapping it
                        // Only give back upgrades/filters that are extras
                        if (drops.size() > 1 && drop == drops.get(0)) {
                            // First item is typically the conduit itself, give it back
                            giveOrDropItem(player, drop);
                        } else if (drops.size() == 1) {
                            // Only one drop (just the conduit), give it back
                            giveOrDropItem(player, drop);
                        } else {
                            // Additional drops (upgrades/filters)
                            giveOrDropItem(player, drop);
                        }
                    }
                }
            }

            // 10. Mark bundle dirty to trigger re-render and network updates
            bundle.dirty();

            // 11. Notify neighboring bundles to update their visual connections
            World world = bundle.getBundleworld();
            for (EnumFacing dir : EnumFacing.VALUES) {
                BlockPos neighborPos = loc.pos.offset(dir);
                TileEntity te = world.getTileEntity(neighborPos);
                if (te instanceof IConduitBundle) {
                    IConduitBundle neighborBundle = (IConduitBundle) te;
                    neighborBundle.dirty();
                }

                // Force block update to refresh rendering
                world.notifyBlockUpdate(neighborPos, world.getBlockState(neighborPos), world.getBlockState(neighborPos),
                        3);
            }

            // Also force update on the replaced conduit's block
            world.notifyBlockUpdate(loc.pos, world.getBlockState(loc.pos), world.getBlockState(loc.pos), 3);

            return true;

        } catch (Exception e) {
            EnderIOConduitReplacerMod.LOGGER.error("Error replacing conduit at {}: {}", loc.pos, e.getMessage());
            return false;
        }
    }

    /**
     * Consume items from player inventory.
     */
    private static void consumeFromInventory(@Nonnull EntityPlayer player, @Nonnull ItemStack targetItem, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.inventory.getSizeInventory() && remaining > 0; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == targetItem.getItem()
                    && stack.getMetadata() == targetItem.getMetadata()) {
                int toTake = Math.min(remaining, stack.getCount());
                stack.shrink(toTake);
                remaining -= toTake;
                if (stack.isEmpty()) {
                    player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                }
            }
        }
    }

    /**
     * Give item to player or drop at their feet if inventory is full.
     */
    private static void giveOrDropItem(@Nonnull EntityPlayer player, @Nonnull ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack.copy())) {
            player.dropItem(stack.copy(), false);
        }
    }

    /**
     * Data class to hold conduit location information.
     */
    private static class ConduitLocation {
        final IConduitBundle bundle;
        final IConduit conduit;
        final BlockPos pos;

        ConduitLocation(IConduitBundle bundle, IConduit conduit, BlockPos pos) {
            this.bundle = bundle;
            this.conduit = conduit;
            this.pos = pos;
        }
    }
}
