package io.github.mattidragon.powernetworks.config.category;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mattidragon.powernetworks.config.ConfigData;

import java.util.function.Function;

import static io.github.mattidragon.powernetworks.config.ConfigData.defaultingFieldOf;

public record TransferRatesCategory(long basic, long improved, long advanced, long ultimate) {
    public static final TransferRatesCategory DEFAULT = new TransferRatesCategory(256, 1024, 4096, 16384);

    public static final Codec<TransferRatesCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultingFieldOf(transferRateCodec(), "basic", DEFAULT.basic).forGetter(TransferRatesCategory::basic),
            defaultingFieldOf(transferRateCodec(), "improved", DEFAULT.improved).forGetter(TransferRatesCategory::improved),
            defaultingFieldOf(transferRateCodec(), "advanced", DEFAULT.advanced).forGetter(TransferRatesCategory::advanced),
            defaultingFieldOf(transferRateCodec(), "ultimate", DEFAULT.ultimate).forGetter(TransferRatesCategory::ultimate)
    ).apply(instance, TransferRatesCategory::new));

    private static Codec<Long> transferRateCodec() {
        Function<Long, DataResult<Long>> check = rate -> {
            if (rate > 0)
                return DataResult.success(rate);
            return DataResult.error(() -> "Transfer rate " + rate + " must be positive", rate);
        };
        return Codec.LONG.flatXmap(check, check);
    }

    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static final class Mutable {
        public long basic;
        public long improved;
        public long advanced;
        public long ultimate;

        private Mutable(TransferRatesCategory values) {
            this.basic = values.basic;
            this.improved = values.improved;
            this.advanced = values.advanced;
            this.ultimate = values.ultimate;
        }

        public TransferRatesCategory toImmutable() {
            return new TransferRatesCategory(basic, improved, advanced, ultimate);
        }
    }
}
