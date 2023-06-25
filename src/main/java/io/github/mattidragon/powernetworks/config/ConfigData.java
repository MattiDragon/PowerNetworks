package io.github.mattidragon.powernetworks.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mattidragon.configloader.api.DefaultedFieldCodec;
import io.github.mattidragon.powernetworks.config.category.ClientCategory;
import io.github.mattidragon.powernetworks.config.category.CoilsCategory;
import io.github.mattidragon.powernetworks.config.category.MiscCategory;
import io.github.mattidragon.powernetworks.config.category.TexturesCategory;

public record ConfigData(CoilsCategory coils, TexturesCategory textures, MiscCategory misc, ClientCategory client) {
    public static final ConfigData DEFAULT = new ConfigData(CoilsCategory.DEFAULT, TexturesCategory.DEFAULT, MiscCategory.DEFAULT, ClientCategory.DEFAULT);

    public static final Codec<ConfigData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DefaultedFieldCodec.of(CoilsCategory.CODEC, "coils", DEFAULT.coils).forGetter(ConfigData::coils),
            DefaultedFieldCodec.of(TexturesCategory.CODEC, "textures", DEFAULT.textures).forGetter(ConfigData::textures),
            DefaultedFieldCodec.of(MiscCategory.CODEC, "misc", DEFAULT.misc).forGetter(ConfigData::misc),
            DefaultedFieldCodec.of(ClientCategory.CODEC, "client", DEFAULT.client).forGetter(ConfigData::client)
    ).apply(instance, ConfigData::new));
}
