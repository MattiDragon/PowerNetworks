package io.github.mattidragon.powernetworks.virtual;

import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import io.github.mattidragon.powernetworks.misc.UnsafeUtil;
import io.github.mattidragon.powernetworks.mixin.EntityAttachS2CPacketAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public class LeashSourceElement extends GenericEntityElement {
    private final int targetId;

    public LeashSourceElement(int targetId) {
        this.targetId = targetId;
        dataTracker.set(SlimeEntity.SLIME_SIZE, 0);
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.SLIME;
    }

    @Override
    public void startWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
        super.startWatching(player, packetConsumer);

        // Packet constructor requires us to provide actual entities, but we don't have them, so we use unsafe and set the fields ourselves.
        var leashPacket = UnsafeUtil.createUnsafe(EntityAttachS2CPacket.class);
        var leashPacketAccess = (EntityAttachS2CPacketAccess) leashPacket;
        leashPacketAccess.setHoldingId(targetId);
        leashPacketAccess.setAttachedId(getEntityIds().getInt(0));
        packetConsumer.accept(leashPacket);
    }

    @Override
    public void stopWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
        super.stopWatching(player, packetConsumer);
    }
}
