package io.github.mattidragon.powernetworks.config.category;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mattidragon.configloader.api.DefaultedFieldCodec;
import io.github.mattidragon.configloader.api.GenerateMutable;

import java.util.function.Function;

@GenerateMutable
public record CoilsCategory(CapacitiesGroup capacities, TransferRatesGroup transferRates) implements MutableCoilsCategory.Source {
    public static final CoilsCategory DEFAULT = new CoilsCategory(CapacitiesGroup.DEFAULT, TransferRatesGroup.DEFAULT);

    public static final Codec<CoilsCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DefaultedFieldCodec.of(CapacitiesGroup.CODEC, "capacities", DEFAULT.capacities).forGetter(CoilsCategory::capacities),
            DefaultedFieldCodec.of(TransferRatesGroup.CODEC, "transferRates", DEFAULT.transferRates).forGetter(CoilsCategory::transferRates)
    ).apply(instance, CoilsCategory::new));

    private static Codec<Long> transferRateCodec() {
        Function<Long, DataResult<Long>> check = rate -> {
            if (rate > 0)
                return DataResult.success(rate);
            return DataResult.error(() -> "Transfer rate " + rate + " must be positive", rate);
        };
        return Codec.LONG.flatXmap(check, check);
    }

    @GenerateMutable
    public record TransferRatesGroup(long basic, long improved, long advanced, long ultimate) implements MutableCoilsCategory.MutableTransferRatesGroup.Source {
        private static final TransferRatesGroup DEFAULT = new TransferRatesGroup(256, 1024, 4096, 16384);

        public static final Codec<TransferRatesGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DefaultedFieldCodec.of(transferRateCodec(), "basic", DEFAULT.basic).forGetter(TransferRatesGroup::basic),
                DefaultedFieldCodec.of(transferRateCodec(), "improved", DEFAULT.improved).forGetter(TransferRatesGroup::improved),
                DefaultedFieldCodec.of(transferRateCodec(), "advanced", DEFAULT.advanced).forGetter(TransferRatesGroup::advanced),
                DefaultedFieldCodec.of(transferRateCodec(), "ultimate", DEFAULT.ultimate).forGetter(TransferRatesGroup::ultimate)
        ).apply(instance, TransferRatesGroup::new));
    }

    @GenerateMutable
    public record CapacitiesGroup(long basic, long improved, long advanced, long ultimate) implements MutableCoilsCategory.MutableCapacitiesGroup.Source {
        private static final CapacitiesGroup DEFAULT = new CapacitiesGroup(256, 1024, 4096, 16384);

        public static final Codec<CapacitiesGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DefaultedFieldCodec.of(transferRateCodec(), "basic", DEFAULT.basic).forGetter(CapacitiesGroup::basic),
                DefaultedFieldCodec.of(transferRateCodec(), "improved", DEFAULT.improved).forGetter(CapacitiesGroup::improved),
                DefaultedFieldCodec.of(transferRateCodec(), "advanced", DEFAULT.advanced).forGetter(CapacitiesGroup::advanced),
                DefaultedFieldCodec.of(transferRateCodec(), "ultimate", DEFAULT.ultimate).forGetter(CapacitiesGroup::ultimate)
        ).apply(instance, CapacitiesGroup::new));
    }
}
