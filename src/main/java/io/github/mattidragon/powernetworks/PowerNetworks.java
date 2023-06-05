package io.github.mattidragon.powernetworks;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.networking.api.PolymerServerNetworking;
import io.github.mattidragon.configloader.api.ConfigManager;
import io.github.mattidragon.powernetworks.block.ModBlocks;
import io.github.mattidragon.powernetworks.config.ConfigData;
import io.github.mattidragon.powernetworks.item.ModItems;
import io.github.mattidragon.powernetworks.network.NetworkRegistry;
import io.github.mattidragon.powernetworks.networking.ConfigEditPackets;
import io.github.mattidragon.powernetworks.networking.PowerNetworksNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerNetworks implements ModInitializer {
	public static final String MOD_ID = "power_networks";
	public static final Logger LOGGER = LoggerFactory.getLogger("Power Networks");
	public static final ConfigManager<ConfigData> CONFIG = ConfigManager.create(ConfigData.CODEC, ConfigData.DEFAULT, "power_networks");

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		// Force load config to stay more reliable
		CONFIG.get();
		NetworkRegistry.register();
		ModBlocks.register();
		ModItems.register();
		PowerNetworksNetworking.register();
		registerCommands();

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

	public static void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			var root = CommandManager.literal("power_networks")
					.requires(source -> source.hasPermissionLevel(2));

			root.then(CommandManager.literal("reload")
					.executes(context -> {
						var error = CONFIG.reload();
						if (error.isEmpty()) {
							context.getSource().sendFeedback(() -> Text.translatable("command.power_networks.reload.success"), true);
							return 1;
						}
						var message = Text.translatable("command.power_networks.reload.fail")
								.fillStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(error.get().toString()))));
						context.getSource().sendError(message);
						LOGGER.error("Failed to reload config", error.get());
						return 0;
					}));

			if (CONFIG.get().misc().allowRemoteEdits() && environment.dedicated) {
				root.then(CommandManager.literal("edit_config")
						.requires(source -> source.getPlayer() != null && PolymerServerNetworking.getSupportedVersion(source.getPlayer().networkHandler, PowerNetworksNetworking.CLIENT_EDITING) == 0)
						.executes(context -> {
							ServerPlayNetworking.send(context.getSource().getPlayerOrThrow(), new ConfigEditPackets.StartEditingPacket(CONFIG.get()));
							return 1;
						}));
			}

			dispatcher.register(root);
		});
	}
}