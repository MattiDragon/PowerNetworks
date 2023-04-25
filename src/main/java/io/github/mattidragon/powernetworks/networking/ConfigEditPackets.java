package io.github.mattidragon.powernetworks.networking;

import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.config.ConfigData;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;

public class ConfigEditPackets {
    private ConfigEditPackets() {}

    public record ApplyChangesPacket(ConfigData config) implements FabricPacket {
        public static final PacketType<ApplyChangesPacket> TYPE = PacketType.create(PowerNetworks.id("apply_config_changes"), ApplyChangesPacket::new);

        private ApplyChangesPacket(PacketByteBuf buf) {
            this(ConfigData.CODEC.parse(NbtOps.INSTANCE, buf.readNbt()).getOrThrow(false, error -> PowerNetworks.LOGGER.error("Failed to deserialize config payload: " + error)));
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeNbt((NbtCompound) ConfigData.CODEC.encodeStart(NbtOps.INSTANCE, config).getOrThrow(false, error -> PowerNetworks.LOGGER.error("Failed to serialize config payload: " + error)));
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }

    public record StartEditingPacket(ConfigData config) implements FabricPacket {
        public static final PacketType<StartEditingPacket> TYPE = PacketType.create(PowerNetworks.id("start_editing_config"), StartEditingPacket::new);

        private StartEditingPacket(PacketByteBuf buf) {
            this(ConfigData.CODEC.parse(NbtOps.INSTANCE, buf.readNbt()).getOrThrow(false, error -> PowerNetworks.LOGGER.error("Failed to deserialize config payload: " + error)));
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeNbt((NbtCompound) ConfigData.CODEC.encodeStart(NbtOps.INSTANCE, config).getOrThrow(false, error -> PowerNetworks.LOGGER.error("Failed to serialize config payload: " + error)));
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }
}
