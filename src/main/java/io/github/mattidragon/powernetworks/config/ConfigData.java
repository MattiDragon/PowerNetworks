package io.github.mattidragon.powernetworks.config;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.FieldEncoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mattidragon.powernetworks.config.category.ClientCategory;
import io.github.mattidragon.powernetworks.config.category.MiscCategory;
import io.github.mattidragon.powernetworks.config.category.TexturesCategory;
import io.github.mattidragon.powernetworks.config.category.TransferRatesCategory;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record ConfigData(TransferRatesCategory transferRates, TexturesCategory textures, MiscCategory misc, ClientCategory client) {
    public static final ConfigData DEFAULT = new ConfigData(TransferRatesCategory.DEFAULT, TexturesCategory.DEFAULT, MiscCategory.DEFAULT, ClientCategory.DEFAULT);

    public static final Codec<ConfigData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultingFieldOf(TransferRatesCategory.CODEC, "transfer_rates", DEFAULT.transferRates).forGetter(ConfigData::transferRates),
            defaultingFieldOf(TexturesCategory.CODEC, "textures", DEFAULT.textures).forGetter(ConfigData::textures),
            defaultingFieldOf(MiscCategory.CODEC, "misc", DEFAULT.misc).forGetter(ConfigData::misc),
            defaultingFieldOf(ClientCategory.CODEC, "client", DEFAULT.client).forGetter(ConfigData::client)
    ).apply(instance, ConfigData::new));

    public static <T> MapCodec<T> defaultingFieldOf(Codec<T> codec, String name, T defaultValue) {
        return MapCodec.of(
                new FieldEncoder<>(name, codec),
                new DefaultingFieldDecoder<>(name, defaultValue, codec),
                () -> "DefaultingField[" + name + ": " + codec.toString() + "]"
        );
    }

    private static final class DefaultingFieldDecoder<A> extends MapDecoder.Implementation<A> {
        private final String name;
        private final A defaultValue;
        private final Decoder<A> elementCodec;

        public DefaultingFieldDecoder(final String name, A defaultValue, final Decoder<A> elementCodec) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.elementCodec = elementCodec;
        }

        @Override
        public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
            final T value = input.get(name);
            if (value == null) {
                return DataResult.success(defaultValue);
            }
            return elementCodec.parse(ops, value);
        }

        @Override
        public <T> Stream<T> keys(final DynamicOps<T> ops) {
            return Stream.of(ops.createString(name));
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final DefaultingFieldDecoder<?> that = (DefaultingFieldDecoder<?>) o;
            return Objects.equals(name, that.name) && Objects.equals(elementCodec, that.elementCodec);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, elementCodec);
        }

        @Override
        public String toString() {
            return "DefaultingFieldDecoder[%s: %s, default: %s]".formatted(name, elementCodec, defaultValue);
        }
    }
}
