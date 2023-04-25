package io.github.mattidragon.powernetworks.client;

import io.github.mattidragon.powernetworks.block.ModBlocks;
import io.github.mattidragon.powernetworks.client.config.ConfigClient;
import io.github.mattidragon.powernetworks.client.renderer.CoilBlockEntityRenderer;
import io.github.mattidragon.powernetworks.networking.ConfigEditPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class PowerNetworksClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererFactories.register(ModBlocks.COIL_BLOCK_ENTITY, CoilBlockEntityRenderer::new);

		ClientPlayNetworking.registerGlobalReceiver(ConfigEditPackets.StartEditingPacket.TYPE, (packet, player, responseSender) ->
				MinecraftClient.getInstance().setScreen(ConfigClient.createScreen(null, packet.config(), config ->
						responseSender.sendPacket(new ConfigEditPackets.ApplyChangesPacket(config)))));
	}
}