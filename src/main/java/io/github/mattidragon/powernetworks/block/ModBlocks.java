package io.github.mattidragon.powernetworks.block;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.misc.CoilTier;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public class ModBlocks {
    private static final AbstractBlock.Settings COIL_SETTINGS = FabricBlockSettings.create().mapColor(MapColor.ORANGE).solid().strength(3.0F, 6.0F).sounds(BlockSoundGroup.COPPER).nonOpaque();
    public static final CoilBlock BASIC_COIL = new CoilBlock(COIL_SETTINGS, CoilTier.BASIC);
    public static final CoilBlock IMPROVED_COIL = new CoilBlock(COIL_SETTINGS, CoilTier.IMPROVED);
    public static final CoilBlock ADVANCED_COIL = new CoilBlock(COIL_SETTINGS, CoilTier.ADVANCED);
    public static final CoilBlock ULTIMATE_COIL = new CoilBlock(COIL_SETTINGS, CoilTier.ULTIMATE);

    public static final BlockEntityType<CoilBlockEntity> COIL_BLOCK_ENTITY = BlockEntityType.Builder.create(CoilBlockEntity::new, BASIC_COIL, IMPROVED_COIL, ADVANCED_COIL, ULTIMATE_COIL).build(null);

    private ModBlocks() {
    }

    public static void register() {
        Registry.register(Registries.BLOCK, PowerNetworks.id("basic_coil"), BASIC_COIL);
        Registry.register(Registries.BLOCK, PowerNetworks.id("improved_coil"), IMPROVED_COIL);
        Registry.register(Registries.BLOCK, PowerNetworks.id("advanced_coil"), ADVANCED_COIL);
        Registry.register(Registries.BLOCK, PowerNetworks.id("ultimate_coil"), ULTIMATE_COIL);

        Registry.register(Registries.BLOCK_ENTITY_TYPE, PowerNetworks.id("coil"), COIL_BLOCK_ENTITY);
        PolymerBlockUtils.registerBlockEntity(COIL_BLOCK_ENTITY);
    }
}
