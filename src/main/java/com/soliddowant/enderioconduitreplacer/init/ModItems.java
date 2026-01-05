package com.soliddowant.enderioconduitreplacer.init;

import com.soliddowant.enderioconduitreplacer.Tags;
import com.soliddowant.enderioconduitreplacer.item.ItemConduitReplacer;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class ModItems {

    public static ItemConduitReplacer CONDUIT_REPLACER;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        CONDUIT_REPLACER = new ItemConduitReplacer();
        registry.register(CONDUIT_REPLACER);
    }
}
