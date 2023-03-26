package io.github.mattidragon.powernetworks.datagen;

import io.github.mattidragon.powernetworks.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

public class BlockLootTableProvider extends FabricBlockLootTableProvider {
    BlockLootTableProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate() {
        addDrop(ModBlocks.BASIC_COIL);
        addDrop(ModBlocks.IMPROVED_COIL);
        addDrop(ModBlocks.ADVANCED_COIL);
        addDrop(ModBlocks.ULTIMATE_COIL);
    }
}
