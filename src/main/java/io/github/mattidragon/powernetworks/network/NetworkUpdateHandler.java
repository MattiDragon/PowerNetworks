package io.github.mattidragon.powernetworks.network;

import com.kneelawk.graphlib.api.graph.GraphEntityContext;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.block.CoilBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

import static io.github.mattidragon.powernetworks.PowerNetworks.id;

public class NetworkUpdateHandler implements GraphEntity<NetworkUpdateHandler> {
    public static final Identifier ID = id("update_handler");
    private final GraphEntityContext context;

    public NetworkUpdateHandler(GraphEntityContext context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onTick() {
        var world = context.getBlockWorld();
        var coils = context.getGraph()
                .getNodes()
                .filter(node -> node.getNode() instanceof CoilNode)
                .map(node -> CoilBlock.getBlockEntity(world, node.getPos()))
                .filter(Objects::nonNull)
                .toList();

        try (var transaction = Transaction.openOuter()) {
            // Calculate how much space the combined output buffers have, we can't move more than this
            var space = 0L;
            try (var testTransaction = transaction.openNested()) {
                for (var coil : coils) {
                    space += coil.storage.outputBuffer.insert(Long.MAX_VALUE, testTransaction);
                }
                testTransaction.abort();
            }

            // Remove energy from input buffers, we don't care about even transfer here
            var movingEnergy = 0L;
            for (var coil : coils) {
                movingEnergy += coil.storage.inputBuffer.extract(space - movingEnergy, transaction);
            }

            /*
            Add energy to output buffers. We absolutely want even transfer, but also have to handle cases of coils not accepting energy.

            The algorithm works in rounds until all energy has been inserted. Each round it loops over the coils and tries to insert
            (energy left / remaining coils in round) energy into them. Coils that don't accept any energy get removed from the cycle
            as otherwise the method could get stuck with small amounts of energy that would fit in the first coils, but won't be inserted
            due to the integer division resulting in zero.

            We also track the total energy moved each round in roundTotal as a safeguard for the algorithm getting stuck on misbehaving
            coils or concurrent changes. If this remains after a round we throw an error, causing the transaction to abort and probably
            crashing the game.
            */
            var activeCoils = new ArrayList<>(coils);
            var coilsToRemove = new ArrayList<CoilBlockEntity>();
            while (movingEnergy > 0) {
                var roundTotal = 0L;
                var coilCount = activeCoils.size();
                for (var coil : activeCoils) {
                    if (movingEnergy <= 0) break;
                    // If we don't allow at least one energy to move we can get stuck with early coils not getting any energy even when they have space.
                    var maxAmount = Math.max(movingEnergy / coilCount, 1);
                    coilCount--;

                    var inserted = coil.storage.outputBuffer.insert(maxAmount, transaction);
                    if (inserted == 0) {
                        coilsToRemove.add(coil);
                    } else {
                        movingEnergy -= inserted;
                        roundTotal += inserted;
                        coil.markDirty();
                    }
                }
                activeCoils.removeAll(coilsToRemove);
                coilsToRemove.clear();

                if (roundTotal <= 0) {
                    PowerNetworks.LOGGER.error("Energy distribution failed: no coil is accepting energy, but still have {} left. The distribution has been canceled. Graph: {}, Involved coils: {}",
                            movingEnergy,
                            context,
                            coils.stream().map(CoilBlockEntity::getPos).toList());
                    transaction.abort();
                    return;
                }
            }
            transaction.commit();
        }
    }

    @Override
    public @NotNull GraphEntityType<?> getType() {
        return NetworkRegistry.UPDATE_HANDLER;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }

    @Override
    public void merge(@NotNull NetworkUpdateHandler other) {
    }
}
