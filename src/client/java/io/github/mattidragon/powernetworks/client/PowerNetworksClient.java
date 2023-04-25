package io.github.mattidragon.powernetworks.client;

import io.github.mattidragon.powernetworks.block.ModBlocks;
import io.github.mattidragon.powernetworks.client.renderer.CoilBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class PowerNetworksClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererFactories.register(ModBlocks.COIL_BLOCK_ENTITY, CoilBlockEntityRenderer::new);

//		PowerNetworksConfig.ON_CHANGE.register(config -> {
//			if (config.client().forceServerRendering()) {
//				PolymerClientNetworking.registerSendPacket(PowerNetworksNetworking.CLIENT_RENDERING);
//			} else {
//				PolymerClientNetworking.registerSendPacket(PowerNetworksNetworking.CLIENT_RENDERING, 0);
//			}
//		});
	}
}