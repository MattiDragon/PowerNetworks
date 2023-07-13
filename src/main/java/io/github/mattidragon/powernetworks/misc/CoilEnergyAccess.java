package io.github.mattidragon.powernetworks.misc;

import com.kneelawk.graphlib.api.util.NodePos;
import io.github.mattidragon.powernetworks.block.CoilBlockEntity;
import io.github.mattidragon.powernetworks.network.CoilNode;
import io.github.mattidragon.powernetworks.network.NetworkEnergyStorage;
import io.github.mattidragon.powernetworks.network.NetworkRegistry;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

@SuppressWarnings("UnstableApiUsage")
public class CoilEnergyAccess extends SnapshotParticipant<CoilEnergyAccess.Snapshot> implements EnergyStorage {
    private final CoilBlockEntity owner;
    private long extractionLimit;
    private long insertionLimit;

    public CoilEnergyAccess(CoilBlockEntity owner) {
        this.owner = owner;
        resetLimits();
    }

    private EnergyStorage getUnderlyingStorage() {
        if (owner.getWorld() == null) return new SimpleEnergyStorage(0, 0, 0);

        var graphView = NetworkRegistry.UNIVERSE.getSidedGraphView(owner.getWorld());
        if (graphView == null) return new SimpleEnergyStorage(0, 0, 0);
        var graph = graphView.getGraphForNode(new NodePos(owner.getPos(), CoilNode.INSTANCE));
        if (graph == null) return new SimpleEnergyStorage(0, 0, 0);

        return graph.getGraphEntity(NetworkEnergyStorage.TYPE);
    }

    public void resetLimits() {
        extractionLimit = owner.getTier().getTransferRate();
        insertionLimit = owner.getTier().getTransferRate();
    }

    @Override
    public boolean supportsInsertion() {
        return owner.getTransferMode() != CoilTransferMode.OUTPUT;
    }

    @Override
    public boolean supportsExtraction() {
        return owner.getTransferMode() != CoilTransferMode.INPUT;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        if (owner.getTransferMode() == CoilTransferMode.OUTPUT) return 0;
        updateSnapshots(transaction);
        var inserted = getUnderlyingStorage().insert(Math.min(insertionLimit, maxAmount), transaction);
        insertionLimit -= inserted;
        return inserted;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        if (owner.getTransferMode() == CoilTransferMode.INPUT) return 0;
        updateSnapshots(transaction);
        var extracted = getUnderlyingStorage().extract(Math.min(extractionLimit, maxAmount), transaction);
        extractionLimit -= extracted;
        return extracted;
    }

    @Override
    public long getAmount() {
        return getUnderlyingStorage().getAmount();
    }

    @Override
    public long getCapacity() {
        return getUnderlyingStorage().getCapacity();
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(extractionLimit, insertionLimit);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        extractionLimit = snapshot.extractionLimit;
        insertionLimit = snapshot.insertionLimit;
    }

    public record Snapshot(long extractionLimit, long insertionLimit) {
    }
}
