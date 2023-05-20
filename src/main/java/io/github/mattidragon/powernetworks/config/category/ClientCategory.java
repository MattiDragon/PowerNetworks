package io.github.mattidragon.powernetworks.config.category;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

import static io.github.mattidragon.powernetworks.config.ConfigData.defaultingFieldOf;

public record ClientCategory(int segmentsPerBlock, float wireWidth, float hangFactor, List<Integer> colors) {
    public static final ClientCategory DEFAULT = new ClientCategory(8, 0.05f, 1.5f, List.of(0x884832, 0xD07D59));

    public static final Codec<ClientCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultingFieldOf(Codec.INT, "segmentsPerBlock", DEFAULT.segmentsPerBlock).forGetter(ClientCategory::segmentsPerBlock),
            defaultingFieldOf(Codec.FLOAT, "wireWidth", DEFAULT.wireWidth).forGetter(ClientCategory::wireWidth),
            defaultingFieldOf(Codec.FLOAT, "hangFactor", DEFAULT.hangFactor).forGetter(ClientCategory::hangFactor),
            defaultingFieldOf(Codec.INT.listOf(), "colors", DEFAULT.colors).forGetter(ClientCategory::colors)
    ).apply(instance, ClientCategory::new));

    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static final class Mutable {
        public int segmentsPerBlock;
        public float wireWidth;
        public float hangFactor;
        public List<Integer> colors;

        private Mutable(ClientCategory values) {
            this.segmentsPerBlock = values.segmentsPerBlock;
            this.wireWidth = values.wireWidth;
            this.hangFactor = values.hangFactor;
            this.colors = List.copyOf(values.colors);
        }

        public ClientCategory toImmutable() {
            return new ClientCategory(segmentsPerBlock, wireWidth, hangFactor, colors);
        }
    }
}
