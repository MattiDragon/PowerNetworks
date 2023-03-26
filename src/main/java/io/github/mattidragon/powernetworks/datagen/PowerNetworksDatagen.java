package io.github.mattidragon.powernetworks.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class PowerNetworksDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		var pack = generator.createPack();
		pack.addProvider(ReadmeDataProvider::new);
		pack.addProvider(BlockLootTableProvider::new);
		pack.addProvider(RecipeProvider::new);
	}
}
