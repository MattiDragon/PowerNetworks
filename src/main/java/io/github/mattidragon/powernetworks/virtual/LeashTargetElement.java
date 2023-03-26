package io.github.mattidragon.powernetworks.virtual;

import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.function.Consumer;

public class LeashTargetElement extends TextDisplayElement {
    public LeashTargetElement() {
        setDisplayHeight(16);
        setDisplayWidth(16);
    }

    @Override
    public void startWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
        super.startWatching(player, packetConsumer);
    }

    @Override
    public void stopWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
        super.stopWatching(player, packetConsumer);
    }
}
