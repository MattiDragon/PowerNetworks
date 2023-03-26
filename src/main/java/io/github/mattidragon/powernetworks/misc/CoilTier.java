package io.github.mattidragon.powernetworks.misc;

import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.config.PowerNetworksConfig;
import net.minecraft.block.Block;

public enum CoilTier {
    BASIC,
    IMPROVED,
    ADVANCED,
    ULTIMATE;

    public long getTransferRate() {
        var rates = PowerNetworksConfig.get().transferRates();
        return switch (this) {
            case BASIC    -> rates.basic();
            case IMPROVED -> rates.improved();
            case ADVANCED -> rates.advanced();
            case ULTIMATE -> rates.ultimate();
        };
    }

    public static CoilTier ofBlock(Block block) {
        if (block instanceof CoilBlock coilBlock) {
            return coilBlock.getTier();
        }
        return CoilTier.BASIC;
    }
}
