package io.github.mattidragon.powernetworks.mixin;

import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityAttachS2CPacket.class)
public interface EntityAttachS2CPacketAccess {
    @Mutable
    @Accessor
    void setAttachedEntityId(int value);

    @Mutable
    @Accessor
    void setHoldingEntityId(int value);
}
