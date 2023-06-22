package io.github.mattidragon.powernetworks.networking;

import eu.pb4.polymer.networking.api.PolymerServerNetworking;
import io.github.mattidragon.powernetworks.PowerNetworks;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class PowerNetworksNetworking {
    public static final Identifier CLIENT_RENDERING = PowerNetworks.id("client_rendering");
    public static final Identifier CLIENT_EDITING = PowerNetworks.id("client_editing");

    public static void register() {
        PolymerServerNetworking.registerSendPacket(CLIENT_RENDERING, 0);
        PolymerServerNetworking.registerSendPacket(CLIENT_EDITING, 0);

        ServerPlayNetworking.registerGlobalReceiver(ConfigEditPackets.ApplyChangesPacket.TYPE, (packet, player, responseSender) -> {
            if (!player.hasPermissionLevel(2) || !PowerNetworks.CONFIG.get().misc().allowRemoteEdits()) {
                player.sendMessage(Text.translatable("power_networks.config_edit.denied").formatted(Formatting.RED));
                return;
            }
            PowerNetworks.CONFIG.set(packet.config());
            player.sendMessage(Text.translatable("power_networks.config_edit.applied").formatted(Formatting.GREEN));
        });
    }

    public static boolean supportsClientRendering(ServerPlayerEntity player) {
        return PolymerServerNetworking.getSupportedVersion(player.networkHandler, CLIENT_RENDERING) == 0;
    }
}
