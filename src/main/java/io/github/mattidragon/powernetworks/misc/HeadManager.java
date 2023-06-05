package io.github.mattidragon.powernetworks.misc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.config.ConfigData;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Util;

public class HeadManager {
    public static final GameProfile BASIC_COIL_PROFILE = new GameProfile(Util.NIL_UUID, "");
    public static final GameProfile IMPROVED_COIL_PROFILE = new GameProfile(Util.NIL_UUID, "");
    public static final GameProfile ADVANCED_COIL_PROFILE = new GameProfile(Util.NIL_UUID, "");
    public static final GameProfile ULTIMATE_COIL_PROFILE = new GameProfile(Util.NIL_UUID, "");

    public static final GameProfile INPUT_INDICATOR_PROFILE = new GameProfile(Util.NIL_UUID, "");
    public static final GameProfile OUTPUT_INDICATOR_PROFILE = new GameProfile(Util.NIL_UUID, "");

    public static final GameProfile WIRE_ITEM_PROFILE = new GameProfile(Util.NIL_UUID, "");

    static {
        HeadManager.setTextures(PowerNetworks.CONFIG.get());
        PowerNetworks.CONFIG.getReloadEvent().register(HeadManager::setTextures);
    }

    private HeadManager() {}

    private static void setTextures(ConfigData config) {
        var textures = config.textures();

        setTexture(BASIC_COIL_PROFILE, textures.basicCoil());
        setTexture(IMPROVED_COIL_PROFILE, textures.improvedCoil());
        setTexture(ADVANCED_COIL_PROFILE, textures.advancedCoil());
        setTexture(ULTIMATE_COIL_PROFILE, textures.ultimateCoil());

        setTexture(INPUT_INDICATOR_PROFILE, textures.inputIndicator());
        setTexture(OUTPUT_INDICATOR_PROFILE, textures.outputIndicator());

        setTexture(WIRE_ITEM_PROFILE, textures.wire());
    }

    public static void setTexture(GameProfile profile, String texture) {
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("Value", texture));
    }

    public static ItemStack createStack(GameProfile profile) {
        var stack = new ItemStack(Items.PLAYER_HEAD);
        stack.getOrCreateNbt().put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), profile));
        return stack;
    }
}
