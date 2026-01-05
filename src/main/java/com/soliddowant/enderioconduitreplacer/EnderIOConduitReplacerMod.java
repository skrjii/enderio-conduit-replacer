package com.soliddowant.enderioconduitreplacer;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.soliddowant.enderioconduitreplacer.gui.ContainerConduitReplacer;
import com.soliddowant.enderioconduitreplacer.gui.GuiConduitReplacer;
import com.soliddowant.enderioconduitreplacer.init.ModItems;
import com.soliddowant.enderioconduitreplacer.item.ItemConduitReplacer;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:enderio;required-after:endercore")
public class EnderIOConduitReplacerMod {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    @Mod.Instance(Tags.MODID)
    public static EnderIOConduitReplacerMod instance;

    public static final int GUI_CONDUIT_REPLACER = 0;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        if (ModItems.CONDUIT_REPLACER == null)
            return;

        ModelLoader.setCustomModelResourceLocation(ModItems.CONDUIT_REPLACER, 0,
                new ModelResourceLocation(ModItems.CONDUIT_REPLACER.getRegistryName(), "inventory"));
    }

    /**
     * GUI Handler for opening the Conduit Replacer GUI
     */
    public static class GuiHandler implements IGuiHandler {
        private static EnumHand getHandHoldingReplacer(EntityPlayer player) {
            if (player.getHeldItemMainhand().getItem() instanceof ItemConduitReplacer) {
                return EnumHand.MAIN_HAND;
            }

            if (player.getHeldItemOffhand().getItem() instanceof ItemConduitReplacer) {
                return EnumHand.OFF_HAND;
            }

            return null;
        }

        @Nullable
        @Override
        public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            if (id != GUI_CONDUIT_REPLACER)
                return null;

            EnumHand hand = getHandHoldingReplacer(player);
            if (hand == null)
                return null;

            return new ContainerConduitReplacer(player.inventory, hand);
        }

        @Nullable
        @Override
        @SideOnly(Side.CLIENT)
        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            if (id != GUI_CONDUIT_REPLACER)
                return null;

            EnumHand hand = getHandHoldingReplacer(player);
            if (hand == null)
                return null;

            return new GuiConduitReplacer(new ContainerConduitReplacer(player.inventory, hand));
        }
    }
}
