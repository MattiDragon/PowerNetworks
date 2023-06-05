package io.github.mattidragon.powernetworks.config.category;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mattidragon.configloader.api.DefaultedFieldCodec;
import io.github.mattidragon.configloader.api.GenerateMutable;

import java.util.function.Function;

@GenerateMutable
public record TransferRatesCategory(long basic, long improved, long advanced, long ultimate) implements MutableTransferRatesCategory.Source {
    public static final TransferRatesCategory DEFAULT = new TransferRatesCategory(256, 1024, 4096, 16384);

    public static final Codec<TransferRatesCategory> CODEC = RecordCodecBuilder.create(instance -> {
        Codec<Long> codec = transferRateCodec();
        Codec<Long> codec1 = transferRateCodec();
        Codec<Long> codec2 = transferRateCodec();
        Codec<Long> codec3 = transferRateCodec();
        return instance.group(
                DefaultedFieldCodec.of(codec3, "basic", DEFAULT.basic).forGetter(TransferRatesCategory::basic),
                DefaultedFieldCodec.of(codec2, "improved", DEFAULT.improved).forGetter(TransferRatesCategory::improved),
                DefaultedFieldCodec.of(codec1, "advanced", DEFAULT.advanced).forGetter(TransferRatesCategory::advanced),
                DefaultedFieldCodec.of(codec, "ultimate", DEFAULT.ultimate).forGetter(TransferRatesCategory::ultimate)
        ).apply(instance, TransferRatesCategory::new);
    });

    private static Codec<Long> transferRateCodec() {
        Function<Long, DataResult<Long>> check = rate -> {
            if (rate > 0)
                return DataResult.success(rate);
            return DataResult.error(() -> "Transfer rate " + rate + " must be positive", rate);
        };
        return Codec.LONG.flatXmap(check, check);
    }
}
