package com.soliddowant.enderioconduitreplacer.network;

import com.soliddowant.enderioconduitreplacer.Tags;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID);

    private static int id = 0;

    public static void init() {
        INSTANCE.registerMessage(PacketSetGhostSlot.Handler.class, PacketSetGhostSlot.class, id++, Side.SERVER);
    }
}
