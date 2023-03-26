package io.github.mattidragon.powernetworks.misc;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

@SuppressWarnings("UnstableApiUsage")
public class CoilEnergyStorage implements EnergyStorage{
    public SimpleEnergyStorage inputBuffer;
    public SimpleEnergyStorage outputBuffer;
    private CoilTransferMode mode;
    private final Runnable markDirty;

    public CoilEnergyStorage(long capacity, CoilTransferMode mode, Runnable markDirty) {
        this.mode = mode;
        this.markDirty = markDirty;

        inputBuffer = new SimpleEnergyStorage(capacity, Long.MAX_VALUE, Long.MAX_VALUE);
        outputBuffer = new SimpleEnergyStorage(capacity, Long.MAX_VALUE, Long.MAX_VALUE);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        if (mode == CoilTransferMode.OUTPUT)
            return 0;

        transaction.addOuterCloseCallback(result -> markDirty.run());
        return inputBuffer.insert(maxAmount, transaction);
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        if (mode == CoilTransferMode.INPUT)
            return 0;

        transaction.addOuterCloseCallback(result -> markDirty.run());
        return outputBuffer.extract(maxAmount, transaction);
    }

    @Override
    public long getAmount() {
        return inputBuffer.amount + outputBuffer.amount;
    }

    @Override
    public long getCapacity() {
        return inputBuffer.capacity + outputBuffer.capacity;
    }

    public void setTransferMode(CoilTransferMode mode) {
        this.mode = mode;
    }
}
