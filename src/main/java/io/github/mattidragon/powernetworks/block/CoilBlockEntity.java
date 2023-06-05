package io.github.mattidragon.powernetworks.block;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.NodePos;
import io.github.mattidragon.powernetworks.misc.CoilEnergyStorage;
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
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CoilBlockEntity extends BlockEntity {
    public CoilDisplay display;
    private CoilTransferMode transferMode = CoilTransferMode.DEFAULT;
    public final CoilEnergyStorage storage;

    private final Set<BlockPos> clientConnectionCache = new HashSet<>();

    static {
        EnergyStorage.SIDED.registerForBlockEntity((coil, direction) -> {
            if (direction != coil.getCachedState().get(CoilBlock.FACING).getOpposite())
                return null;

            return coil.storage;
        }, ModBlocks.COIL_BLOCK_ENTITY);
    }

    public CoilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.COIL_BLOCK_ENTITY, pos, state);
        var tier = CoilTier.ofBlock(state.getBlock());
        storage = new CoilEnergyStorage(tier.getTransferRate(), transferMode, this::markDirty);
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
        coil.pushEnergy();
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

        var graphWorld = NetworkRegistry.UNIVERSE.getServerGraphWorld(serverWorld);
        var node = graphWorld.getNodeAt(new NodePos(pos, CoilNode.INSTANCE));
        if (node == null)
            return;

        for (var connection : node.getConnections()) {
            NodeHolder<BlockNode> other = connection.other(node);
            graphWorld.disconnectNodes(node.getPos(), other.getPos(), WireLinkKey.INSTANCE);
            var state = world.getBlockState(other.getBlockPos());
            world.updateListeners(other.getBlockPos(), state, state, Block.NOTIFY_ALL);
        }
        world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

    public Set<BlockPos> getClientConnectionCache() {
        return Collections.unmodifiableSet(clientConnectionCache);
    }

    public void setClientConnectionCache(Set<BlockPos> clientConnectionCache) {
        this.clientConnectionCache.clear();
        this.clientConnectionCache.addAll(clientConnectionCache);
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

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        var nbt = super.toInitialChunkDataNbt();
        if (!(world instanceof ServerWorld serverWorld)) return nbt;

        var node = NetworkRegistry.UNIVERSE.getServerGraphWorld(serverWorld).getNodeAt(new NodePos(pos, CoilNode.INSTANCE));
        if (node == null) return nbt;

        var list = new NbtList();
        for (var connection : node.getConnections()) {
            list.add(NbtHelper.fromBlockPos(connection.other(node).getBlockPos()));
        }
        nbt.put("clientConnectionCache", list);
        return nbt;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (display != null)
            display.clear();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("clientConnectionCache", NbtElement.LIST_TYPE)) {
            clientConnectionCache.clear();
            for (var element : nbt.getList("clientConnectionCache", NbtElement.COMPOUND_TYPE)) {
                clientConnectionCache.add(NbtHelper.toBlockPos((NbtCompound) element));
            }
        }

        storage.inputBuffer.amount = nbt.getLong("inputEnergyBuffer");
        storage.outputBuffer.amount = nbt.getLong("outputEnergyBuffer");
        transferMode = CoilTransferMode.getSafe(nbt.getByte("transferMode"));
        onTransferModeChanged();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putLong("inputEnergyBuffer", storage.inputBuffer.amount);
        nbt.putLong("outputEnergyBuffer", storage.outputBuffer.amount);
        nbt.putByte("transferMode", (byte) transferMode.ordinal());
    }
}
