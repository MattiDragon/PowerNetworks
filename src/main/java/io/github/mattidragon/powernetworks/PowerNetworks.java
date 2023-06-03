package io.github.mattidragon.powernetworks;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import io.github.mattidragon.powernetworks.block.ModBlocks;
import io.github.mattidragon.powernetworks.config.PowerNetworksConfig;
import io.github.mattidragon.powernetworks.item.ModItems;
import io.github.mattidragon.powernetworks.network.NetworkRegistry;
import io.github.mattidragon.powernetworks.network.NetworkUpdateHandler;
import io.github.mattidragon.powernetworks.networking.PowerNetworksNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerNetworks implements ModInitializer {
	public static final String MOD_ID = "power_networks";
	public static final Logger LOGGER = LoggerFactory.getLogger("Power Networks");

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

    @Override
	public void onInitialize() {
		// Force load config to stay more reliable
		PowerNetworksConfig.get();
		NetworkRegistry.register();
		ModBlocks.register();
		ModItems.register();
		PowerNetworksNetworking.register();

		PolymerItemGroupUtils.registerPolymerItemGroup(id("content"),
				FabricItemGroup.builder()
						.displayName(Text.translatable("itemGroup.power_networks.content"))
						.icon(ModItems.ADVANCED_COIL::getDefaultStack)
						.entries((displayContext, entries) -> {
							entries.add(ModItems.WIRE);
							entries.add(ModItems.BASIC_COIL);
							entries.add(ModItems.IMPROVED_COIL);
							entries.add(ModItems.ADVANCED_COIL);
							entries.add(ModItems.ULTIMATE_COIL);
						})
						.build());

	}
}