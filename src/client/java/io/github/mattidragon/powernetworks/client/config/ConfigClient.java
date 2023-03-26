package io.github.mattidragon.powernetworks.client.config;

import dev.isxander.yacl.api.*;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.string.StringController;
import dev.isxander.yacl.gui.controllers.string.number.LongFieldController;
import io.github.mattidragon.powernetworks.config.ConfigData;
import io.github.mattidragon.powernetworks.config.PowerNetworksConfig;
import io.github.mattidragon.powernetworks.config.category.MiscCategory;
import io.github.mattidragon.powernetworks.config.category.TexturesCategory;
import io.github.mattidragon.powernetworks.config.category.TransferRatesCategory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static io.github.mattidragon.powernetworks.config.ConfigData.DEFAULT;

public class ConfigClient {
    public static Screen createScreen(Screen parent) {
        var config = PowerNetworksConfig.get();
        var transferRates = config.transferRates().toMutable();
        var textures = config.textures().toMutable();
        var misc = config.misc().toMutable();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("config.power_networks"))
                .category(createTransferRatesCategory(transferRates))
                .category(createTexturesCategory(textures))
                .category(createMiscCategory(misc))
                .save(() -> PowerNetworksConfig.set(new ConfigData(transferRates.toImmutable(), textures.toImmutable(), misc.toImmutable())))
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory createTransferRatesCategory(TransferRatesCategory.Mutable instance) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.power_networks.transfer_rates"))
                .option(Option.createBuilder(long.class)
                        .name(Text.translatable("config.power_networks.transfer_rates.basic"))
                        .binding(DEFAULT.transferRates().basic(), () -> instance.basic, value -> instance.basic = value)
                        .controller(option -> new LongFieldController(option, 1, Long.MAX_VALUE))
                        .build())
                .option(Option.createBuilder(long.class)
                        .name(Text.translatable("config.power_networks.transfer_rates.improved"))
                        .binding(DEFAULT.transferRates().improved(), () -> instance.improved, value -> instance.improved = value)
                        .controller(option -> new LongFieldController(option, 1, Long.MAX_VALUE))
                        .build())
                .option(Option.createBuilder(long.class)
                        .name(Text.translatable("config.power_networks.transfer_rates.advanced"))
                        .binding(DEFAULT.transferRates().advanced(), () -> instance.advanced, value -> instance.advanced = value)
                        .controller(option -> new LongFieldController(option, 1, Long.MAX_VALUE))
                        .build())
                .option(Option.createBuilder(long.class)
                        .name(Text.translatable("config.power_networks.transfer_rates.ultimate"))
                        .binding(DEFAULT.transferRates().ultimate(), () -> instance.ultimate, value -> instance.ultimate = value)
                        .controller(option -> new LongFieldController(option, 1, Long.MAX_VALUE))
                        .build())
                .build();
    }

    private static ConfigCategory createTexturesCategory(TexturesCategory.Mutable instance) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.power_networks.textures"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("config.power_networks.textures.coils"))
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("config.power_networks.textures.basicCoil"))
                                .binding(DEFAULT.textures().basicCoil(), () -> instance.basicCoil, value -> instance.basicCoil = value)
                                .controller(StringController::new)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("config.power_networks.textures.improvedCoil"))
                                .binding(DEFAULT.textures().improvedCoil(), () -> instance.improvedCoil, value -> instance.improvedCoil = value)
                                .controller(StringController::new)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("config.power_networks.textures.advancedCoil"))
                                .binding(DEFAULT.textures().advancedCoil(), () -> instance.advancedCoil, value -> instance.advancedCoil = value)
                                .controller(StringController::new)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("config.power_networks.textures.ultimateCoil"))
                                .binding(DEFAULT.textures().ultimateCoil(), () -> instance.ultimateCoil, value -> instance.ultimateCoil = value)
                                .controller(StringController::new)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("config.power_networks.textures.indicators"))
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("config.power_networks.textures.inputIndicator"))
                                .binding(DEFAULT.textures().inputIndicator(), () -> instance.inputIndicator, value -> instance.inputIndicator = value)
                                .controller(StringController::new)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("config.power_networks.textures.outputIndicator"))
                                .binding(DEFAULT.textures().outputIndicator(), () -> instance.outputIndicator, value -> instance.outputIndicator = value)
                                .controller(StringController::new)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .build())
                .option(Option.createBuilder(String.class)
                        .name(Text.translatable("config.power_networks.textures.wire"))
                        .binding(DEFAULT.textures().wire(), () -> instance.wire, value -> instance.wire = value)
                        .controller(StringController::new)
                        .flag(RequireDisconnectScreen.FLAG)
                        .build())
                .build();
    }

    private static ConfigCategory createMiscCategory(MiscCategory.Mutable instance) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.power_networks.misc"))
                .option(Option.createBuilder(boolean.class)
                        .name(Text.translatable("config.power_networks.misc.useDoubleLeads"))
                        .binding(DEFAULT.misc().useDoubleLeads(), () -> instance.useDoubleLeads, value -> instance.useDoubleLeads = value)
                        .controller(TickBoxController::new)
                        .tooltip(Text.translatable("config.power_networks.misc.useDoubleLeads.tooltip1"), Text.translatable("config.power_networks.misc.useDoubleLeads.tooltip2"))
                        .build())
                .build();
    }
}
