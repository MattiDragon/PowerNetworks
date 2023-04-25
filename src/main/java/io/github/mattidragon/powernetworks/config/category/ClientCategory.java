package io.github.mattidragon.powernetworks.config.category;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record ClientCategory(int segmentsPerBlock, float wireWidth, float hangFactor, List<Integer> colors) {
    public static final ClientCategory DEFAULT = new ClientCategory(8, 0.05f, 1.5f, List.of(0x884832, 0xD07D59));

    public static final Codec<ClientCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("segmentsPerBlock").setPartial(DEFAULT::segmentsPerBlock).forGetter(ClientCategory::segmentsPerBlock),
            Codec.FLOAT.fieldOf("wireWidth").setPartial(DEFAULT::wireWidth).forGetter(ClientCategory::wireWidth),
            Codec.FLOAT.fieldOf("hangFactor").setPartial(DEFAULT::hangFactor).forGetter(ClientCategory::hangFactor),
            Codec.INT.listOf().fieldOf("colors").setPartial(DEFAULT::colors).forGetter(ClientCategory::colors)
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
