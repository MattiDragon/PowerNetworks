package io.github.mattidragon.powernetworks.config.category;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MiscCategory(boolean useDoubleLeads) {
    public static final MiscCategory DEFAULT = new MiscCategory(false);

    public static final Codec<MiscCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("useDoubleLeads").setPartial(DEFAULT::useDoubleLeads).forGetter(MiscCategory::useDoubleLeads)
    ).apply(instance, MiscCategory::new));

    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static final class Mutable {
        public boolean useDoubleLeads;

        private Mutable(MiscCategory values) {
            this.useDoubleLeads = values.useDoubleLeads;
        }

        public MiscCategory toImmutable() {
            return new MiscCategory(useDoubleLeads);
        }
    }
}
