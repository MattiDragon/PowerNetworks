package io.github.mattidragon.powernetworks.misc;

import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import net.minecraft.block.Block;

public enum CoilTier {
    BASIC,
    IMPROVED,
    ADVANCED,
    ULTIMATE;

    public long getTransferRate() {
        var rates = PowerNetworks.CONFIG.get().coils().transferRates();
        return switch (this) {
            case BASIC    -> rates.basic();
            case IMPROVED -> rates.improved();
            case ADVANCED -> rates.advanced();
            case ULTIMATE -> rates.ultimate();
        };
    }

    public long getCapacity() {
        var capacities = PowerNetworks.CONFIG.get().coils().capacities();
        return switch (this) {
            case BASIC    -> capacities.basic();
            case IMPROVED -> capacities.improved();
            case ADVANCED -> capacities.advanced();
            case ULTIMATE -> capacities.ultimate();
        };
    }

    public static CoilTier ofBlock(Block block) {
        if (block instanceof CoilBlock coilBlock) {
            return coilBlock.getTier();
        }
        return CoilTier.BASIC;
    }
}
