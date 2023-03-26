package io.github.mattidragon.powernetworks.item;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.virtual.HeadElement;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoilBlockItem extends BlockItem implements PolymerItem {
    private final GameProfile profile;

    public CoilBlockItem(CoilBlock block, Settings settings) {
        super(block, settings);
        this.profile = HeadElement.getProfile(block.getTier());
    }

    @Override
    public Item getPolymerItem(ItemStack stack, @Nullable ServerPlayerEntity player) {
        return Items.PLAYER_HEAD;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack original, TooltipContext context, @Nullable ServerPlayerEntity player) {
        var stack = PolymerItem.super.getPolymerItemStack(original, context, player);
        stack.setSubNbt("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), profile));
        return stack;
    }
}
