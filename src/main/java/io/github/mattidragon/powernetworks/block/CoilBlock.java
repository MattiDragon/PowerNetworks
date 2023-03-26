package io.github.mattidragon.powernetworks.block;

import com.kneelawk.graphlib.GraphLib;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import io.github.mattidragon.powernetworks.misc.CoilTier;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class CoilBlock extends RodBlock implements PolymerBlock, BlockEntityProvider {
    private final CoilTier tier;

    public CoilBlock(Settings settings, CoilTier tier) {
        super(settings);
        this.tier = tier;
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
        tooltip.add(Text.translatable("block.power_networks.coil.buffer_size", tier.getTransferRate()));
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
        var coil = getBlockEntity(world, pos);
        if (coil == null)
            return ActionResult.FAIL;

        if (player.isSneaking()) {
            coil.disconnectAllConnections();
            return ActionResult.SUCCESS;
        } else if (player.getStackInHand(hand).isEmpty()) {
            coil.cycleTransferMode();
            player.playSound(SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3f, 0.6f);
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }

    @Override
    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
        if (world instanceof ServerWorld serverWorld) {
            GraphLib.getController(serverWorld).updateConnections(pos);
            GraphLib.getController(serverWorld).updateNodes(pos);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        var coil = getBlockEntity(world, pos);
        if (coil != null && coil.display != null)
            coil.display.clear();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
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
