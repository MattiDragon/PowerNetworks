package io.github.mattidragon.powernetworks.config.category;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static io.github.mattidragon.powernetworks.config.ConfigData.defaultingFieldOf;

public record TexturesCategory(String basicCoil, String improvedCoil, String advancedCoil, String ultimateCoil,
                               String inputIndicator, String outputIndicator, String wire) {
    public static final TexturesCategory DEFAULT = new TexturesCategory(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjUwNjI3NzJkMmM5MmUwMzU1YWMyZmNhOGMyYTQwY2M5NTViMDZmZTRkNWVmZWU3ODE5M2I4MTE2NjhkZmFkMCJ9fX0",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGRkODAwMDYwMjFkZGM0YjIxMDYzMDMyMzZiNTYwNTUzYzliZTdkYjMwOGRkMTEzZGI4NmI2NGE1Yjk4ZDNhNyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2FhZTNhYjQ1OTc3ODI1MWVkOGNjZjE3MzMxNjBkZDZlYzFiYTkzOGY4NzJmMjU3YmNmZThhMzIzZDhiOTEyMTkifX19",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U3YjNmOTM5OWU0Y2JhZTgwYmJmZDc5ZDFhZmQxMTMyMzdhNjM3ZDcwMjRiYmNhY2Y1ZDdkZDAyYzNlZGQ1OTEifX19",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VhMmFjMGViNWUyYTBkZjc2NzQ3NTFlNTU4ZjBiNTU4OWVlMDg5N2I3MGY1YTkzNzZmOGIzYWQ4N2E1NjI2ZWEifX19",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U3NTg1NDllMGExYjVkZDBmNGFmOWFlNTBmMzNhOGRhOGI2ODlmZWUxNWVhOGJiOTU3ZDQ1ZWQyYmNiNTUwMzIifX19",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VhYTQ5MmVmODA2NWQxMGM0M2M3ZTUzNGM0ZjcyMjk4Y2U0OTI3MzA0YmNjNmE4MDMxYTg5ZWE5OTcyNWEyNGQifX19");

    public static final Codec<TexturesCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultingFieldOf(Codec.STRING, "basicCoil", DEFAULT.basicCoil).forGetter(TexturesCategory::basicCoil),
            defaultingFieldOf(Codec.STRING, "improvedCoil", DEFAULT.improvedCoil).forGetter(TexturesCategory::improvedCoil),
            defaultingFieldOf(Codec.STRING, "advancedCoil", DEFAULT.advancedCoil).forGetter(TexturesCategory::advancedCoil),
            defaultingFieldOf(Codec.STRING, "ultimateCoil", DEFAULT.ultimateCoil).forGetter(TexturesCategory::ultimateCoil),
            defaultingFieldOf(Codec.STRING, "inputIndicator", DEFAULT.inputIndicator).forGetter(TexturesCategory::inputIndicator),
            defaultingFieldOf(Codec.STRING, "outputIndicator", DEFAULT.outputIndicator).forGetter(TexturesCategory::outputIndicator),
            defaultingFieldOf(Codec.STRING, "wire", DEFAULT.wire).forGetter(TexturesCategory::wire)
    ).apply(instance, TexturesCategory::new));

    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static final class Mutable {
        public String basicCoil;
        public String improvedCoil;
        public String advancedCoil;
        public String ultimateCoil;
        public String inputIndicator;
        public String outputIndicator;
        public String wire;

        private Mutable(TexturesCategory values) {
            this.basicCoil = values.basicCoil;
            this.improvedCoil = values.improvedCoil;
            this.advancedCoil = values.advancedCoil;
            this.ultimateCoil = values.ultimateCoil;
            this.inputIndicator = values.inputIndicator;
            this.outputIndicator = values.outputIndicator;
            this.wire = values.wire;
        }

        public TexturesCategory toImmutable() {
            return new TexturesCategory(basicCoil, improvedCoil, advancedCoil, ultimateCoil, inputIndicator, outputIndicator, wire);
        }
    }
}
