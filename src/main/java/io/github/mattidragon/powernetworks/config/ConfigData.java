package io.github.mattidragon.powernetworks.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mattidragon.powernetworks.config.category.ClientCategory;
import io.github.mattidragon.powernetworks.config.category.MiscCategory;
import io.github.mattidragon.powernetworks.config.category.TexturesCategory;
import io.github.mattidragon.powernetworks.config.category.TransferRatesCategory;

public record ConfigData(TransferRatesCategory transferRates, TexturesCategory textures, MiscCategory misc, ClientCategory client) {
    public static final ConfigData DEFAULT = new ConfigData(TransferRatesCategory.DEFAULT, TexturesCategory.DEFAULT, MiscCategory.DEFAULT, ClientCategory.DEFAULT);

    public static final Codec<ConfigData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TransferRatesCategory.CODEC.fieldOf("transfer_rates").setPartial(DEFAULT::transferRates).forGetter(ConfigData::transferRates),
            TexturesCategory.CODEC.fieldOf("textures").setPartial(DEFAULT::textures).forGetter(ConfigData::textures),
            MiscCategory.CODEC.fieldOf("misc").setPartial(DEFAULT::misc).forGetter(ConfigData::misc),
            ClientCategory.CODEC.fieldOf("client").setPartial(DEFAULT::client).forGetter(ConfigData::client)
    ).apply(instance, ConfigData::new));
}
