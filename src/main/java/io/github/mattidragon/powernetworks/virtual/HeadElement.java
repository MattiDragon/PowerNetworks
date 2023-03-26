package io.github.mattidragon.powernetworks.virtual;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import io.github.mattidragon.powernetworks.misc.CoilTier;
import io.github.mattidragon.powernetworks.misc.CoilTransferMode;
import io.github.mattidragon.powernetworks.misc.HeadManager;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

public class HeadElement extends ItemDisplayElement {
    private HeadElement(GameProfile profile) {
        super(HeadManager.createStack(profile));
    }

    public static HeadElement createCoil(CoilTier tier) {
        return new HeadElement(getProfile(tier));
    }

    public static HeadElement createTransferModeIndicator(CoilTransferMode mode) {
        return new HeadElement(getProfile(mode));
    }

    private static GameProfile getProfile(CoilTransferMode mode) {
        return switch (mode) {
            case DEFAULT -> new GameProfile(Util.NIL_UUID, "");
            case INPUT -> HeadManager.INPUT_INDICATOR_PROFILE;
            case OUTPUT -> HeadManager.OUTPUT_INDICATOR_PROFILE;
        };
    }

    @NotNull
    public static GameProfile getProfile(CoilTier tier) {
        return switch (tier) {
            case BASIC -> HeadManager.BASIC_COIL_PROFILE;
            case IMPROVED -> HeadManager.IMPROVED_COIL_PROFILE;
            case ADVANCED -> HeadManager.ADVANCED_COIL_PROFILE;
            case ULTIMATE -> HeadManager.ULTIMATE_COIL_PROFILE;
        };
    }
}
