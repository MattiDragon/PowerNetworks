package io.github.mattidragon.powernetworks.client.config;

import dev.isxander.yacl.api.*;
import dev.isxander.yacl.api.controller.*;
import io.github.mattidragon.powernetworks.config.ConfigData;
import io.github.mattidragon.powernetworks.config.category.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.mattidragon.powernetworks.config.ConfigData.DEFAULT;

public class ConfigClient {
    public static final Function<Float, Text> FLOAT_FORMATTER;

    static {
        var format = NumberFormat.getNumberInstance(Locale.ROOT);
        format.setMaximumFractionDigits(3);
        FLOAT_FORMATTER = (value) -> Text.literal(format.format(value));
    }

    public static Screen createScreen(Screen parent, ConfigData config, Consumer<ConfigData> saveConsumer) {
        var transferRates = config.transferRates().toMutable();
        var textures = config.textures().toMutable();
        var misc = config.misc().toMutable();
        var client = config.client().toMutable();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("config.power_networks"))
                .category(createTexturesCategory(textures))
                .category(createTransferRatesCategory(transferRates))
                .category(createMiscCategory(misc))
                .category(createClientCategory(client))
                .save(() -> saveConsumer.accept(new ConfigData(transferRates.toImmutable(), textures.toImmutable(), misc.toImmutable(), client.toImmutable())))
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory createTransferRatesCategory(MutableTransferRatesCategory transferRates) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.power_networks.transfer_rates"))
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.transfer_rates.basic"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.transfer_rates.basic.description")))
                        .binding(DEFAULT.transferRates().basic(), () -> transferRates.basic, value -> transferRates.basic = value)
                        .controller(option -> LongFieldControllerBuilder.create(option).min(1L))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.transfer_rates.improved"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.transfer_rates.improved.description")))
                        .binding(DEFAULT.transferRates().improved(), () -> transferRates.improved, value -> transferRates.improved = value)
                        .controller(option -> LongFieldControllerBuilder.create(option).min(1L))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.transfer_rates.advanced"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.transfer_rates.advanced.description")))
                        .binding(DEFAULT.transferRates().advanced(), () -> transferRates.advanced, value -> transferRates.advanced = value)
                        .controller(option -> LongFieldControllerBuilder.create(option).min(1L))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.transfer_rates.ultimate"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.transfer_rates.ultimate.description")))
                        .binding(DEFAULT.transferRates().ultimate(), () -> transferRates.ultimate, value -> transferRates.ultimate = value)
                        .controller(option -> LongFieldControllerBuilder.create(option).min(1L))
                        .build())
                .build();
    }

    private static ConfigCategory createMiscCategory(MutableMiscCategory misc) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.power_networks.misc"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("config.power_networks.misc.useDoubleLeads"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.misc.useDoubleLeads.description")))
                        .binding(DEFAULT.misc().useDoubleLeads(), () -> misc.useDoubleLeads, value -> misc.useDoubleLeads = value)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .build();
    }

    private static ConfigCategory createTexturesCategory(MutableTexturesCategory instance) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.power_networks.textures"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("config.power_networks.textures.coils"))
                        .description(createTextureDescription(null, Text.translatable("config.power_networks.textures.coils.description")))
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("config.power_networks.textures.basicCoil"))
                                .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.coils.description")))
                                .binding(DEFAULT.textures().basicCoil(), () -> instance.basicCoil, value -> instance.basicCoil = value)
                                .controller(StringControllerBuilder::create)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("config.power_networks.textures.improvedCoil"))
                                .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.coils.description")))
                                .binding(DEFAULT.textures().improvedCoil(), () -> instance.improvedCoil, value -> instance.improvedCoil = value)
                                .controller(StringControllerBuilder::create)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("config.power_networks.textures.advancedCoil"))
                                .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.coils.description")))
                                .binding(DEFAULT.textures().advancedCoil(), () -> instance.advancedCoil, value -> instance.advancedCoil = value)
                                .controller(StringControllerBuilder::create)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("config.power_networks.textures.ultimateCoil"))
                                .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.coils.description")))
                                .binding(DEFAULT.textures().ultimateCoil(), () -> instance.ultimateCoil, value -> instance.ultimateCoil = value)
                                .controller(StringControllerBuilder::create)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("config.power_networks.textures.indicators"))
                        .description(createTextureDescription(null, Text.translatable("config.power_networks.textures.indicators.description")))
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("config.power_networks.textures.inputIndicator"))
                                .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.indicators.description")))
                                .binding(DEFAULT.textures().inputIndicator(), () -> instance.inputIndicator, value -> instance.inputIndicator = value)
                                .controller(StringControllerBuilder::create)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("config.power_networks.textures.outputIndicator"))
                                .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.indicators.description")))
                                .binding(DEFAULT.textures().outputIndicator(), () -> instance.outputIndicator, value -> instance.outputIndicator = value)
                                .controller(StringControllerBuilder::create)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .build())
                .option(Option.<String>createBuilder()
                        .name(Text.translatable("config.power_networks.textures.wire"))
                        .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.wire.description")))
                        .binding(DEFAULT.textures().wire(), () -> instance.wire, value -> instance.wire = value)
                        .controller(StringControllerBuilder::create)
                        .flag(RequireDisconnectScreen.FLAG)
                        .build())
                .build();
    }

    private static OptionDescription createTextureDescription(@Nullable String value, Text text) {
        return OptionDescription.createBuilder()
                .customImage(CompletableFuture.completedFuture(Optional.ofNullable(value).map(PlayerHeadImageRenderer::new)))
                .text(text)
                .text(Text.translatable("config.power_networks.textures.description"))
                .build();
    }

    private static ConfigCategory createClientCategory(MutableClientCategory instance) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.power_networks.client"))
                .option(Option.<Integer>createBuilder()
                        .name(Text.translatable("config.power_networks.client.segmentsPerBlock"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.client.segmentsPerBlock.description")))
                        .binding(DEFAULT.client().segmentsPerBlock(), () -> instance.segmentsPerBlock, value -> instance.segmentsPerBlock = value)
                        .controller(option -> IntegerFieldControllerBuilder.create(option).min(1))
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Text.translatable("config.power_networks.client.wireWidth"))
                .description(OptionDescription.of(Text.translatable("config.power_networks.client.wireWidth.description")))
                        .binding(DEFAULT.client().wireWidth(), () -> instance.wireWidth, value -> instance.wireWidth = value)
                        .controller(option -> FloatFieldControllerBuilder.create(option).range(0f, 1f).valueFormatter(FLOAT_FORMATTER))
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Text.translatable("config.power_networks.client.hangFactor"))
                .description(OptionDescription.of(Text.translatable("config.power_networks.client.hangFactor.description")))
                        .binding(DEFAULT.client().hangFactor(), () -> instance.hangFactor, value -> instance.hangFactor = value)
                        .controller(option -> FloatFieldControllerBuilder.create(option).range(0f, 1f).valueFormatter(FLOAT_FORMATTER))
                        .build())
                .group(ListOption.<Color>createBuilder()
                        .initial(Color.BLACK)
                        .name(Text.translatable("config.power_networks.client.colors"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.client.colors.description")))
                        .binding(DEFAULT.client().colors().stream().map(Color::new).toList(),
                                () -> instance.colors.stream().map(Color::new).toList(),
                                value -> instance.colors = value.stream().map(Color::getRGB).map(color -> color & 0x00ffffff).toList())
                        .controller(ColorControllerBuilder::create)
                        .collapsed(true)
                        .build())
                .build();
    }

}
