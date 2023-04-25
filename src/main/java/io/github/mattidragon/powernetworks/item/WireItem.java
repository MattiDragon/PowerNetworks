package io.github.mattidragon.powernetworks.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.block.CoilBlockEntity;
import io.github.mattidragon.powernetworks.misc.HeadManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class WireItem extends Item implements PolymerItem {
    public static final String CONNECTION_POS_KEY = "WireConnectionPos";

    public WireItem(Settings settings) {
        super(settings);
    }

    @Nullable
    public static BlockPos getPos(ItemStack stack) {
        var nbt = stack.getSubNbt(WireItem.CONNECTION_POS_KEY);
        if (nbt == null)
            return null;

        return NbtHelper.toBlockPos(nbt);
    }

    /**
     * Checks if a player is holding a wire that is targeting the specified position
     * @return {@code true} if the player is holding at least one such wire
     */
    public static boolean hasAttachmentTo(ServerPlayerEntity player, BlockPos pos) {
        return pos.equals(getPos(player.getMainHandStack())) || pos.equals(getPos(player.getOffHandStack()));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var world = context.getWorld();
        var pos = context.getBlockPos();
        var stack = context.getStack();

        var connectionNbt = stack.getSubNbt(CONNECTION_POS_KEY);

        if (world instanceof ServerWorld serverWorld) {
            var coil1 = CoilBlock.getBlockEntity(world, pos);

            if (connectionNbt != null) {
                var coil2 = CoilBlock.getBlockEntity(world, NbtHelper.toBlockPos(connectionNbt));

                if (coil1 == null || coil2 == null) {
                    stack.removeSubNbt(CONNECTION_POS_KEY);
                    return ActionResult.FAIL;
                }

                CoilBlockEntity.connect(serverWorld, coil1, coil2);
                stack.decrement(1);
                stack.removeSubNbt(CONNECTION_POS_KEY);
            } else {
                stack.setSubNbt(CONNECTION_POS_KEY, NbtHelper.fromBlockPos(pos));
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.PLAYER_HEAD;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack stack, TooltipContext context, @Nullable ServerPlayerEntity player) {
        var polymerStack = PolymerItem.super.getPolymerItemStack(stack, context, player);
        polymerStack.setSubNbt("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), HeadManager.WIRE_ITEM_PROFILE));
        return polymerStack;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
