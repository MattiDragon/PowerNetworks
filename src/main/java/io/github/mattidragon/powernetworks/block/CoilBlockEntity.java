package io.github.mattidragon.powernetworks.block;

import com.kneelawk.graphlib.GraphLib;
import io.github.mattidragon.powernetworks.misc.CoilEnergyStorage;
import io.github.mattidragon.powernetworks.misc.CoilTransferMode;
import io.github.mattidragon.powernetworks.misc.CoilTier;
import io.github.mattidragon.powernetworks.network.NetworkUpdateHandler;
import io.github.mattidragon.powernetworks.virtual.CoilDisplay;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CoilBlockEntity extends BlockEntity {
    public CoilDisplay display;
    private final CoilTier tier;
    private CoilTransferMode transferMode = CoilTransferMode.DEFAULT;
    private final Set<BlockPos> connections = new HashSet<>();
    public final CoilEnergyStorage storage;

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
        storage = new CoilEnergyStorage(tier.getTransferRate(), transferMode, this::markDirty);
    }

    public static void connect(ServerWorld world, CoilBlockEntity first, CoilBlockEntity second) {
        first.connections.add(second.pos);
        second.connections.add(first.pos);
        GraphLib.getController(world).updateConnections(first.pos);
        GraphLib.getController(world).updateConnections(second.pos);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CoilBlockEntity coil) {
        if (coil.display == null)
            coil.display = new CoilDisplay(world, pos, coil.transferMode);

        coil.display.tick();
        coil.pushEnergy();
        NetworkUpdateHandler.onTick(coil);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void pushEnergy() {
        var direction = getCachedState().get(CoilBlock.FACING).getOpposite();
        var target = EnergyStorage.SIDED.find(world, pos.offset(direction), direction);
        if (target != null) {
            EnergyStorageUtil.move(storage, target, Long.MAX_VALUE, null);
        }
    }

    public void disconnectAllConnections() {
        if (!(world instanceof ServerWorld serverWorld))
            return;

        for (var connection : connections) {
            var coil2 = CoilBlock.getBlockEntity(serverWorld, connection);
            if (coil2 == null)
                continue;
            coil2.connections.remove(pos);
            GraphLib.getController(serverWorld).updateConnections(coil2.pos);
        }
        connections.clear();
        GraphLib.getController(serverWorld).updateConnections(pos);
    }

    public Set<BlockPos> getConnections() {
        return Collections.unmodifiableSet(connections);
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
        storage.setTransferMode(transferMode);
        if (display != null) // If it's null it'll be created with the new mode
            display.setTransferMode(transferMode);
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        display.clear();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        connections.clear();
        for (var element : nbt.getList("connections", NbtElement.COMPOUND_TYPE)) {
            connections.add(NbtHelper.toBlockPos((NbtCompound) element));
        }

        storage.inputBuffer.amount = nbt.getLong("inputEnergyBuffer");
        storage.outputBuffer.amount = nbt.getLong("outputEnergyBuffer");
        transferMode = CoilTransferMode.getSafe(nbt.getByte("transferMode"));
        onTransferModeChanged();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        var list = new NbtList();
        for (var connection : connections) {
            list.add(NbtHelper.fromBlockPos(connection));
        }
        nbt.put("connections", list);

        nbt.putLong("inputEnergyBuffer", storage.inputBuffer.amount);
        nbt.putLong("outputEnergyBuffer", storage.outputBuffer.amount);
        nbt.putByte("transferMode", (byte) transferMode.ordinal());
    }
}
