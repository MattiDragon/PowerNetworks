package io.github.mattidragon.powernetworks.config.category;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static io.github.mattidragon.powernetworks.config.ConfigData.defaultingFieldOf;

public record MiscCategory(boolean useDoubleLeads, boolean allowRemoteEdits) {
    public static final MiscCategory DEFAULT = new MiscCategory(false, true);

    public static final Codec<MiscCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultingFieldOf(Codec.BOOL, "useDoubleLeads", DEFAULT.useDoubleLeads).forGetter(MiscCategory::useDoubleLeads),
            defaultingFieldOf(Codec.BOOL, "allowRemoteEdits", DEFAULT.allowRemoteEdits).forGetter(MiscCategory::allowRemoteEdits)
    ).apply(instance, MiscCategory::new));

    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static final class Mutable {
        public boolean useDoubleLeads;
        public boolean allowRemoteEdits;

        private Mutable(MiscCategory values) {
            this.useDoubleLeads = values.useDoubleLeads;
            this.allowRemoteEdits = values.allowRemoteEdits;
        }

        public MiscCategory toImmutable() {
            return new MiscCategory(useDoubleLeads, allowRemoteEdits);
        }
    }
}
