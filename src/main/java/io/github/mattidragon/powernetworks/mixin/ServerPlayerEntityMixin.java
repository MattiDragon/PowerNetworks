package io.github.mattidragon.powernetworks.mixin;

import com.mojang.authlib.GameProfile;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.item.ModItems;
import io.github.mattidragon.powernetworks.item.WireItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void power_networks$handleHeldWire(CallbackInfo ci) {
        var mainStack = getMainHandStack();
        handleSingleHeldWire(mainStack);
    }

    private void handleSingleHeldWire(ItemStack stack) {
        if (!stack.isOf(ModItems.WIRE)) return;

        var posNbt = stack.getSubNbt(WireItem.CONNECTION_POS_KEY);
        if (posNbt == null) return;

        var pos = NbtHelper.toBlockPos(posNbt);
        var coil = CoilBlock.getBlockEntity(this.world, pos);
        if (coil == null) return;

        coil.display.attachPlayerLeash((ServerPlayerEntity) (Object) this);
    }
}
