package io.github.mattidragon.powernetworks.config.category;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MiscCategory(boolean useDoubleLeads, boolean allowRemoteEdits) {
    public static final MiscCategory DEFAULT = new MiscCategory(false, true);

    public static final Codec<MiscCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("useDoubleLeads").setPartial(DEFAULT::useDoubleLeads).forGetter(MiscCategory::useDoubleLeads),
            Codec.BOOL.fieldOf("allowRemoteEdits").setPartial(DEFAULT::allowRemoteEdits).forGetter(MiscCategory::allowRemoteEdits)
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
