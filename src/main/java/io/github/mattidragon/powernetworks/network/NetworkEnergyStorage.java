package io.github.mattidragon.powernetworks.network;

import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;
import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphEntityContext;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.*;
import com.kneelawk.graphlib.api.util.LinkPos;
import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.block.CoilBlockEntity;
import io.github.mattidragon.powernetworks.misc.CoilTier;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLong;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class NetworkEnergyStorage extends SnapshotParticipant<NetworkEnergyStorage.Snapshot> implements GraphEntity<NetworkEnergyStorage>, EnergyStorage {
    public static final Identifier ID = PowerNetworks.id("energy_storage");
    public static final GraphEntityType<NetworkEnergyStorage> TYPE = GraphEntityType.of(ID,
            () -> new NetworkEnergyStorage(0),
            tag -> new NetworkEnergyStorage(tag instanceof AbstractNbtNumber number ? number.longValue() : 0),
            (original, originalGraph, newGraph) -> {
                // Split energy evenly based on capacity of both graphs, shouldn't cause either one to overflow
                var originalCapacity = getCapacity(originalGraph);
                var newCapacity = getCapacity(newGraph);
                var totalCapacity = originalCapacity + newCapacity;
                var newWeight = (double) newCapacity / totalCapacity;
                var newEnergy = (long) (newWeight * original.energyStored);

                original.energyStored -= newEnergy;
                original.updateCapacity();
                return new NetworkEnergyStorage(newEnergy);
            },
            (buf, msgCtx) -> new NetworkEnergyStorage(buf.readVarLong()));

    private GraphEntityContext context;
    private long capacity = -1;
    private long energyStored;

    private final Deque<Profile> profiles = new ArrayDeque<>(20 * 60); // One minute of ticks stored
    private long currentTickInserted;
    private long currentTickExtracted;

    public NetworkEnergyStorage(long energyStored) {
        this.energyStored = energyStored;
    }

    @Override
    public void onInit(@NotNull GraphEntityContext context) {
        this.context = context;
    }

    @Override
    public @NotNull GraphEntityContext getContext() {
        return context;
    }

    @Override
    public @NotNull GraphEntityType<?> getType() {
        return TYPE;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return NbtLong.of(energyStored);
    }

    @Override
    public void toPacket(@NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx) {
        buf.writeVarLong(energyStored);
    }

    @Override
    public void merge(@NotNull NetworkEnergyStorage other) {
        this.energyStored += other.energyStored;
        other.energyStored = 0; // Safety
        updateCapacity();
    }

    @Override
    public void onNodeDestroyed(@NotNull NodeHolder<BlockNode> node, @Nullable NodeEntity nodeEntity, Map<LinkPos, LinkEntity> linkEntities) {
        var previousCapacity = getCapacity();
        GraphEntity.super.onNodeDestroyed(node, nodeEntity, linkEntities);

        var lostCapacity = previousCapacity - getCapacity();
        var lostEnergy = (long) (energyStored * ((double) lostCapacity / previousCapacity));
        energyStored -= lostEnergy;
        onEnergyLost(lostEnergy, node.getBlockPos());
    }

    @Override
    public void onUpdate() {
        updateCapacity();
    }

    @Override
    public void onTick() {
        var totalTransferRate = getCapacity(context.getGraph());
        try (var transaction = Transaction.openOuter()) {
            long moved;
            do {
                moved = context.getGraph().getNodes().mapToLong(holder -> {
                    if (!(holder.getBlockEntity() instanceof CoilBlockEntity coil)) return 0;
                    var transferRate = coil.getTier().getTransferRate();
                    var energyAvailable = (long) (energyStored * ((double) transferRate / totalTransferRate));

                    var direction = holder.getBlockState().get(CoilBlock.FACING).getOpposite();
                    var target = EnergyStorage.SIDED.find(holder.getBlockWorld(), holder.getBlockPos().offset(direction), direction);
                    if (target == null) return 0;

                    return EnergyStorageUtil.move(coil.storage, target, energyAvailable, transaction);
                }).sum();
            } while (moved != 0);
            transaction.commit();
        }

        profiles.addFirst(new Profile(currentTickInserted, currentTickExtracted));
        if (profiles.size() > 20 * 60) profiles.removeLast();
        currentTickInserted = 0;
        currentTickExtracted = 0;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        var inserted = Math.min(maxAmount, Math.max(getCapacity() - getAmount(), 0));
        if (inserted > 0) {
            updateSnapshots(transaction);
            currentTickInserted += inserted;
            energyStored += inserted;
        }

        return inserted;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        var extracted = Math.min(maxAmount, getAmount());
        if (extracted > 0) {
            updateSnapshots(transaction);
            currentTickExtracted += extracted;
            energyStored -= extracted;
        }

        return extracted;
    }

    @Override
    public long getAmount() {
        return energyStored;
    }

    @Override
    public long getCapacity() {
        if (capacity == -1) updateCapacity();
        return capacity;
    }

    public Deque<Profile> getProfiles() {
        return profiles;
    }

    private void onEnergyLost(long amount, BlockPos pos) {
        if (amount < 10) return;
        if (!(context.getBlockWorld() instanceof ServerWorld world)) return;

        var x = pos.getX() + 0.5;
        var y = pos.getY() + 0.5;
        var z = pos.getZ() + 0.5;
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 20, 0.1, 0.1, 0.1, 1);
        world.playSound(null, pos, SoundEvents.BLOCK_METAL_BREAK, SoundCategory.BLOCKS, 2, 3);
    }

    private void updateCapacity() {
        capacity = getCapacity(context.getGraph());
    }

    private static long getCapacity(BlockGraph graph) {
        return graph.getNodes()
                .map(NodeHolder::getBlockState)
                .map(BlockState::getBlock)
                .filter(CoilBlock.class::isInstance)
                .map(CoilBlock.class::cast)
                .map(CoilBlock::getTier)
                .mapToLong(CoilTier::getTransferRate)
                .sum();
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(energyStored, currentTickInserted, currentTickExtracted);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        energyStored = snapshot.stored;
        currentTickInserted = snapshot.currentTickInserted;
        currentTickExtracted = snapshot.currentTickExtracted;
    }

    record Snapshot(long stored, long currentTickInserted, long currentTickExtracted) {}
    public record Profile(double inserted, double extracted) {}
}
