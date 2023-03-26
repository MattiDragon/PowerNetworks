package io.github.mattidragon.powernetworks.item;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.block.ModBlocks;
import io.github.mattidragon.powernetworks.misc.CoilTier;
import io.github.mattidragon.powernetworks.virtual.HeadElement;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
    public static final WireItem WIRE = new WireItem(new FabricItemSettings());
    public static final CoilBlockItem BASIC_COIL = new CoilBlockItem(ModBlocks.BASIC_COIL, new FabricItemSettings());
    public static final CoilBlockItem IMPROVED_COIL = new CoilBlockItem(ModBlocks.IMPROVED_COIL, new FabricItemSettings());
    public static final CoilBlockItem ADVANCED_COIL = new CoilBlockItem(ModBlocks.ADVANCED_COIL, new FabricItemSettings());
    public static final CoilBlockItem ULTIMATE_COIL = new CoilBlockItem(ModBlocks.ULTIMATE_COIL, new FabricItemSettings());

    private ModItems() {
    }

    public static void register() {
        Registry.register(Registries.ITEM, PowerNetworks.id("basic_coil"), BASIC_COIL);
        Registry.register(Registries.ITEM, PowerNetworks.id("improved_coil"), IMPROVED_COIL);
        Registry.register(Registries.ITEM, PowerNetworks.id("advanced_coil"), ADVANCED_COIL);
        Registry.register(Registries.ITEM, PowerNetworks.id("ultimate_coil"), ULTIMATE_COIL);

        Registry.register(Registries.ITEM, PowerNetworks.id("wire"), WIRE);
    }
}
