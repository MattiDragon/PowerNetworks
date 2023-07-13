package io.github.mattidragon.powernetworks.block;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.NodePos;
import io.github.mattidragon.powernetworks.misc.CoilEnergyAccess;
import io.github.mattidragon.powernetworks.misc.CoilTier;
import io.github.mattidragon.powernetworks.misc.CoilTransferMode;
import io.github.mattidragon.powernetworks.network.CoilNode;
import io.github.mattidragon.powernetworks.network.NetworkRegistry;
import io.github.mattidragon.powernetworks.network.WireLinkKey;
import io.github.mattidragon.powernetworks.virtual.CoilDisplay;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

public class CoilBlockEntity extends BlockEntity {
    private CoilTransferMode transferMode = CoilTransferMode.DEFAULT;
    public CoilDisplay display;
    private final CoilTier tier;
    public final CoilEnergyAccess storage;

    static {
        EnergyStorage.SIDED.registerForBlockEntity((coil, direction) -> {
            if (direction != coil.getCachedState().get(CoilBlock.FACING).getOpposite())
                return null;

            return coil.storage;
        }, ModBlocks.COIL_BLOCK_ENTITY);
    }

    public CoilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.COIL_BLOCK_ENTITY, pos, state);
        tier = CoilTier.ofBlock(state.getBlock());
        storage = new CoilEnergyAccess(this);
    }

    public static void connect(ServerWorld world, CoilBlockEntity first, CoilBlockEntity second) {
        var graphWorld = NetworkRegistry.UNIVERSE.getServerGraphWorld(world);
        graphWorld.connectNodes(new NodePos(first.pos, CoilNode.INSTANCE), new NodePos(second.pos, CoilNode.INSTANCE), WireLinkKey.INSTANCE);

        world.updateListeners(first.pos, first.getCachedState(), first.getCachedState(), 0);
        world.updateListeners(second.pos, second.getCachedState(), second.getCachedState(), 0);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CoilBlockEntity coil) {
        if (coil.display == null)
            coil.display = new CoilDisplay(world, pos, coil.transferMode);

        coil.display.tick();
        coil.storage.resetLimits();
    }

    public void disconnectAllConnections() {
        if (!(world instanceof ServerWorld serverWorld))
            return;

        var graphWorld = NetworkRegistry.UNIVERSE.getServerGraphWorld(serverWorld);
        var node = graphWorld.getNodeAt(new NodePos(pos, CoilNode.INSTANCE));
        if (node == null)
            return;

        for (var connection : List.copyOf(node.getConnections())) {
            NodeHolder<BlockNode> other = connection.other(node);
            graphWorld.disconnectNodes(node.getPos(), other.getPos(), WireLinkKey.INSTANCE);
            var state = world.getBlockState(other.getBlockPos());
            world.updateListeners(other.getBlockPos(), state, state, Block.NOTIFY_ALL);
        }
        world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

    public void cycleTransferMode() {
        transferMode = switch (transferMode) {
            case DEFAULT -> CoilTransferMode.INPUT;
            case INPUT -> CoilTransferMode.OUTPUT;
            case OUTPUT -> CoilTransferMode.DEFAULT;
        };
        onTransferModeChanged();
    }

    private void onTransferModeChanged() {
        if (display != null) // If it's null it'll be created with the new mode
            display.setTransferMode(transferMode);
    }

    public CoilTier getTier() {
        return tier;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (display != null)
            display.clear();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        transferMode = CoilTransferMode.getSafe(nbt.getByte("transferMode"));
        onTransferModeChanged();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putByte("transferMode", (byte) transferMode.ordinal());
    }

    public CoilTransferMode getTransferMode() {
        return transferMode;
    }
}
