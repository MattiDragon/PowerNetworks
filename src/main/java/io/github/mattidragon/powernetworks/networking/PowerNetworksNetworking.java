package io.github.mattidragon.powernetworks.networking;

import eu.pb4.polymer.networking.api.PolymerServerNetworking;
import io.github.mattidragon.powernetworks.PowerNetworks;
import net.minecraft.util.Identifier;

public class PowerNetworksNetworking {
    public static final Identifier CLIENT_RENDERING = PowerNetworks.id("client_rendering");

    public static void register() {
        PolymerServerNetworking.registerSendPacket(CLIENT_RENDERING, 0);
    }
}
