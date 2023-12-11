package io.github.mattidragon.powernetworks.block;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.misc.CoilTier;
import io.github.mattidragon.powernetworks.network.NetworkRegistry;
import io.github.mattidragon.powernetworks.networking.PowerNetworksNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class CoilBlock extends RodBlock implements PolymerBlock, PolymerClientDecoded, PolymerKeepModel, BlockEntityProvider {
    private final CoilTier tier;

    public CoilBlock(Settings settings, CoilTier tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, ServerPlayerEntity player) {
        if (PowerNetworksNetworking.supportsClientRendering(player))
            return state;

        return PolymerBlock.super.getPolymerBlockState(state, player);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.LIGHTNING_ROD;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.LIGHTNING_ROD.getDefaultState().with(FACING, state.get(FACING).getOpposite());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        tooltip.add(Text.translatable("block.power_networks.coil.capacity", Text.literal(String.valueOf(tier.getCapacity())).formatted(Formatting.YELLOW)).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("block.power_networks.coil.transfer_rate", Text.literal(String.valueOf(tier.getTransferRate())).formatted(Formatting.YELLOW)).formatted(Formatting.GRAY));
    }

    public CoilTier getTier() {
        return tier;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getSide());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld && !PowerNetworks.CONFIG.get().misc().allowAdventureModeInteractions()) return ActionResult.PASS;

        var coil = getBlockEntity(world, pos);
        if (coil == null)
            return ActionResult.FAIL;

        if (player.isSneaking()) {
            coil.disconnectAllConnections();
            return ActionResult.SUCCESS;
        } else if (player.getStackInHand(hand).isEmpty()) {
            coil.cycleTransferMode();
            if (!world.isClient)
                player.playSound(SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3f, 0.6f);
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }

    @Override
    protected MapCodec<? extends RodBlock> getCodec() {
        return MapCodec.unit(this);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(super.getOutlineShape(state, world, pos, context), createCuboidShape(4, 4, 4, 12, 12, 12));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return super.getOutlineShape(state, world, pos, context);
    }

    @Override
    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
        if (world instanceof ServerWorld serverWorld) {
            NetworkRegistry.UNIVERSE.getServerGraphWorld(serverWorld).updateNodes(pos);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        var coil = getBlockEntity(world, pos);
        if (coil != null) {
            if (coil.display != null) {
                coil.display.clear();
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient)
            return null;

        return ModBlocks.COIL_BLOCK_ENTITY == type ? (BlockEntityTicker<T>) (BlockEntityTicker<CoilBlockEntity>) CoilBlockEntity::tick : null;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public static CoilBlockEntity getBlockEntity(World world, BlockPos pos) {
        if (!(world.getBlockEntity(pos) instanceof CoilBlockEntity coil))
            return null;
        return coil;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CoilBlockEntity(pos, state);
    }
}
