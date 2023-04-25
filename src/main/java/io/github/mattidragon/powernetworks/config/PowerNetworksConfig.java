package io.github.mattidragon.powernetworks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.networking.api.PolymerServerNetworking;
import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.networking.ConfigEditPackets;
import io.github.mattidragon.powernetworks.networking.PowerNetworksNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PowerNetworksConfig {
    /**
     * Called whenever the config is changes. Used for resetting skins on player heads
     */
    public static final Event<OnChange> ON_CHANGE = EventFactory.createArrayBacked(OnChange.class,
            listeners -> config -> {
                for (var listener : listeners) {
                    listener.onChange(config);
                }
            });
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("power_networks.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static boolean prepared = false;
    private static ConfigData instance = ConfigData.DEFAULT;

    private PowerNetworksConfig() {}

    public static ConfigData get() {
        prepare();
        return instance;
    }

    public static void set(ConfigData config) {
        instance = config;
        ON_CHANGE.invoker().onChange(config);
        save();
    }

    private static void prepare() {
        if (!prepared) {
            prepared = true;
            // Use default config in datagen to avoid it getting out of date.
            if (System.getProperty("fabric-api.datagen") != null) return;

            if (Files.exists(PATH)) {
                load();
            } else {
                save();
            }
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                var root = CommandManager.literal("power_networks")
                        .requires(source -> source.hasPermissionLevel(2));

                root.then(CommandManager.literal("reload")
                        .executes(context -> {
                            try {
                                load();
                            } catch (RuntimeException e) {
                                var error = Text.translatable("command.power_networks.reload.fail")
                                        .fillStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(e.toString()))));
                                context.getSource().sendError(error);
                                PowerNetworks.LOGGER.error("Failed to reload config", e);
                                return 0;
                            }
                            context.getSource().sendFeedback(Text.translatable("command.power_networks.reload.success"), true);
                            return 1;
                        }));

                if (get().misc().allowRemoteEdits() && environment.dedicated) {
                    root.then(CommandManager.literal("edit_config")
                            .requires(source -> source.getPlayer() != null && PolymerServerNetworking.getSupportedVersion(source.getPlayer().networkHandler, PowerNetworksNetworking.CLIENT_EDITING) == 0)
                            .executes(context -> {
                                ServerPlayNetworking.send(context.getSource().getPlayerOrThrow(), new ConfigEditPackets.StartEditingPacket(PowerNetworksConfig.get()));
                                return 1;
                            }));
                }

                dispatcher.register(root);
            });
        }
    }

    private static void save() {
        var result = ConfigData.CODEC.encodeStart(JsonOps.INSTANCE, instance);
        result.resultOrPartial(PowerNetworks.LOGGER::error).ifPresent(data -> {
            try (var out = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(data, out);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to save power networks config", e);
            }
        });
    }

    private static void load() {
        boolean repair;
        try (var in = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            JsonObject json = GSON.fromJson(in, JsonObject.class);
            repair = JsonHelper.getBoolean(json, "repair", false);
            var result = ConfigData.CODEC.parse(JsonOps.INSTANCE, json);

            if (result.error().isPresent() && !repair) {
                PowerNetworks.LOGGER.error("Power networks config failed to load: {}", result.error().get());
                PowerNetworks.LOGGER.error("Add the following to attempt repair (may overwrite changes): \"repair\": true");
                throw new RuntimeException("Power networks config failed to load. Check logs.");
            }
            if (result.result().isPresent()) {
                instance = result.result().get();
                ON_CHANGE.invoker().onChange(instance);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load power networks config due to io error", e);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Power networks config has a syntax errors", e);
        }

        if (repair) {
            PowerNetworks.LOGGER.info("Repairing power networks config");
            save();
        }
    }

    @FunctionalInterface
    public interface OnChange {
        void onChange(ConfigData config);
    }
}
