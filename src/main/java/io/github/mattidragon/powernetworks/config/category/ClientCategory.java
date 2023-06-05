package io.github.mattidragon.powernetworks.config.category;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mattidragon.configloader.api.DefaultedFieldCodec;
import io.github.mattidragon.configloader.api.GenerateMutable;

import java.util.List;

@GenerateMutable
public record ClientCategory(int segmentsPerBlock, float wireWidth, float hangFactor, List<Integer> colors) implements MutableClientCategory.Source {
    public static final ClientCategory DEFAULT = new ClientCategory(8, 0.05f, 1.5f, List.of(0x884832, 0xD07D59));

    public static final Codec<ClientCategory> CODEC = RecordCodecBuilder.create(instance -> {
        Codec<List<Integer>> codec = Codec.INT.listOf();
        return instance.group(
                DefaultedFieldCodec.of(Codec.INT, "segmentsPerBlock", DEFAULT.segmentsPerBlock).forGetter(ClientCategory::segmentsPerBlock),
                DefaultedFieldCodec.of(Codec.FLOAT, "wireWidth", DEFAULT.wireWidth).forGetter(ClientCategory::wireWidth),
                DefaultedFieldCodec.of(Codec.FLOAT, "hangFactor", DEFAULT.hangFactor).forGetter(ClientCategory::hangFactor),
                DefaultedFieldCodec.of(codec, "colors", DEFAULT.colors).forGetter(ClientCategory::colors)
        ).apply(instance, ClientCategory::new);
    });
}
