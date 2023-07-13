package io.github.mattidragon.powernetworks.client.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import io.github.mattidragon.powernetworks.config.ConfigData;
import io.github.mattidragon.powernetworks.config.category.MutableClientCategory;
import io.github.mattidragon.powernetworks.config.category.MutableCoilsCategory;
import io.github.mattidragon.powernetworks.config.category.MutableMiscCategory;
import io.github.mattidragon.powernetworks.config.category.MutableTexturesCategory;
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
        var coils = config.coils().toMutable();
        var textures = config.textures().toMutable();
        var misc = config.misc().toMutable();
        var client = config.client().toMutable();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("config.power_networks"))
                .category(createTexturesCategory(textures))
                .category(createCoilsCategory(coils))
                .category(createMiscCategory(misc))
                .category(createClientCategory(client))
                .save(() -> saveConsumer.accept(new ConfigData(coils.toImmutable(), textures.toImmutable(), misc.toImmutable(), client.toImmutable())))
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory createCoilsCategory(MutableCoilsCategory coils) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.power_networks.coils"))
                .group(createTransferRatesGroup(coils.transferRates()))
                .group(createCapacitiesGroup(coils.capacities()))
                .build();
    }

    private static OptionGroup createTransferRatesGroup(MutableCoilsCategory.MutableTransferRatesGroup transferRates) {
        return OptionGroup.createBuilder()
                .name(Text.translatable("config.power_networks.transfer_rates"))
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.transfer_rates.basic"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.transfer_rates.basic.description")))
                        .binding(DEFAULT.coils().transferRates().basic(), transferRates::basic, transferRates::basic)
                        .controller(option -> LongFieldControllerBuilder.create(option).min(1L))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.transfer_rates.improved"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.transfer_rates.improved.description")))
                        .binding(DEFAULT.coils().transferRates().improved(), transferRates::improved, transferRates::improved)
                        .controller(option -> LongFieldControllerBuilder.create(option).min(1L))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.transfer_rates.advanced"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.transfer_rates.advanced.description")))
                        .binding(DEFAULT.coils().transferRates().advanced(), transferRates::advanced, transferRates::advanced)
                        .controller(option -> LongFieldControllerBuilder.create(option).min(1L))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.transfer_rates.ultimate"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.transfer_rates.ultimate.description")))
                        .binding(DEFAULT.coils().transferRates().ultimate(), transferRates::ultimate, transferRates::ultimate)
                        .controller(option -> LongFieldControllerBuilder.create(option).min(1L))
                        .build())
                .build();
    }

    private static OptionGroup createCapacitiesGroup(MutableCoilsCategory.MutableCapacitiesGroup capacities) {
        return OptionGroup.createBuilder()
                .name(Text.translatable("config.power_networks.capacities"))
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.capacities.basic"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.capacities.basic.description")))
                        .binding(DEFAULT.coils().capacities().basic(), capacities::basic, capacities::basic)
                        .controller(option -> LongFieldControllerBuilder.create(option).min(1L))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.capacities.improved"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.capacities.improved.description")))
                        .binding(DEFAULT.coils().capacities().improved(), capacities::improved, capacities::improved)
                        .controller(option -> LongFieldControllerBuilder.create(option).min(1L))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.capacities.advanced"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.capacities.advanced.description")))
                        .binding(DEFAULT.coils().capacities().advanced(), capacities::advanced, capacities::advanced)
                        .controller(option -> LongFieldControllerBuilder.create(option).min(1L))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.power_networks.capacities.ultimate"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.capacities.ultimate.description")))
                        .binding(DEFAULT.coils().capacities().ultimate(), capacities::ultimate, capacities::ultimate)
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
                        .binding(DEFAULT.misc().useDoubleLeads(), misc::useDoubleLeads, misc::useDoubleLeads)
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
                                .binding(DEFAULT.textures().basicCoil(), instance::basicCoil, instance::basicCoil)
                                .controller(StringControllerBuilder::create)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("config.power_networks.textures.improvedCoil"))
                                .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.coils.description")))
                                .binding(DEFAULT.textures().improvedCoil(), instance::improvedCoil, instance::improvedCoil)
                                .controller(StringControllerBuilder::create)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("config.power_networks.textures.advancedCoil"))
                                .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.coils.description")))
                                .binding(DEFAULT.textures().advancedCoil(), instance::advancedCoil, instance::advancedCoil)
                                .controller(StringControllerBuilder::create)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("config.power_networks.textures.ultimateCoil"))
                                .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.coils.description")))
                                .binding(DEFAULT.textures().ultimateCoil(), instance::ultimateCoil, instance::ultimateCoil)
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
                                .binding(DEFAULT.textures().inputIndicator(), instance::inputIndicator, instance::inputIndicator)
                                .controller(StringControllerBuilder::create)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("config.power_networks.textures.outputIndicator"))
                                .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.indicators.description")))
                                .binding(DEFAULT.textures().outputIndicator(), instance::outputIndicator, instance::outputIndicator)
                                .controller(StringControllerBuilder::create)
                                .flag(RequireDisconnectScreen.FLAG)
                                .build())
                        .build())
                .option(Option.<String>createBuilder()
                        .name(Text.translatable("config.power_networks.textures.wire"))
                        .description(option -> createTextureDescription(option, Text.translatable("config.power_networks.textures.wire.description")))
                        .binding(DEFAULT.textures().wire(), instance::wire, instance::wire)
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
                        .binding(DEFAULT.client().segmentsPerBlock(), instance::segmentsPerBlock, instance::segmentsPerBlock)
                        .controller(option -> IntegerFieldControllerBuilder.create(option).min(1))
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Text.translatable("config.power_networks.client.wireWidth"))
                .description(OptionDescription.of(Text.translatable("config.power_networks.client.wireWidth.description")))
                        .binding(DEFAULT.client().wireWidth(), instance::wireWidth, instance::wireWidth)
                        .controller(option -> FloatFieldControllerBuilder.create(option).range(0f, 1f).valueFormatter(FLOAT_FORMATTER))
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Text.translatable("config.power_networks.client.hangFactor"))
                .description(OptionDescription.of(Text.translatable("config.power_networks.client.hangFactor.description")))
                        .binding(DEFAULT.client().hangFactor(), instance::hangFactor, instance::hangFactor)
                        .controller(option -> FloatFieldControllerBuilder.create(option).range(0f, 1f).valueFormatter(FLOAT_FORMATTER))
                        .build())
                .group(ListOption.<Color>createBuilder()
                        .initial(Color.BLACK)
                        .name(Text.translatable("config.power_networks.client.colors"))
                        .description(OptionDescription.of(Text.translatable("config.power_networks.client.colors.description")))
                        .binding(DEFAULT.client().colors().stream().map(Color::new).toList(),
                                () -> instance.colors().stream().map(Color::new).toList(),
                                value -> instance.colors(value.stream().map(Color::getRGB).map(color -> color & 0x00ffffff).toList()))
                        .controller(ColorControllerBuilder::create)
                        .collapsed(true)
                        .build())
                .build();
    }

}
