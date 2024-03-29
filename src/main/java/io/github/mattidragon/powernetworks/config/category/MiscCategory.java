package io.github.mattidragon.powernetworks.config.category;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mattidragon.configloader.api.DefaultedFieldCodec;
import io.github.mattidragon.configloader.api.GenerateMutable;

@GenerateMutable
public record MiscCategory(boolean useDoubleLeads, boolean allowAdventureModeInteractions, boolean allowRemoteEdits) implements MutableMiscCategory.Source {
    public static final MiscCategory DEFAULT = new MiscCategory(false, false, true);

    public static final Codec<MiscCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DefaultedFieldCodec.of(Codec.BOOL, "useDoubleLeads", DEFAULT.useDoubleLeads).forGetter(MiscCategory::useDoubleLeads),
            DefaultedFieldCodec.of(Codec.BOOL, "allowAdventureModeInteractions", DEFAULT.allowAdventureModeInteractions).forGetter(MiscCategory::allowAdventureModeInteractions),
            DefaultedFieldCodec.of(Codec.BOOL, "allowRemoteEdits", DEFAULT.allowRemoteEdits).forGetter(MiscCategory::allowRemoteEdits)
    ).apply(instance, MiscCategory::new));
}
