package io.github.mattidragon.powernetworks.network;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.kneelawk.graphlib.GraphLib;
import com.kneelawk.graphlib.graph.BlockGraph;
import com.kneelawk.graphlib.graph.BlockNodeHolder;
import com.kneelawk.graphlib.graph.struct.Node;
import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.block.CoilBlockEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NetworkUpdateHandler {
    private static final Multimap<RegistryKey<World>, Long> TICKED = HashMultimap.create();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> TICKED.clear());
    }

    public static void onTick(CoilBlockEntity coil) {
        if (!(coil.getWorld() instanceof ServerWorld world))
            return;

        var controller = GraphLib.getController(world);

        controller.getNodesAt(coil.getPos())
                .map(Node::data)
                .filter(holder -> holder.getNode() instanceof CoilNode)
                .mapToLong(BlockNodeHolder::getGraphId)
                .filter(id -> !TICKED.containsEntry(world.getRegistryKey(), id))
                .mapToObj(controller::getGraph)
                .filter(Objects::nonNull)
                .forEach(graph -> tickGraph(world, graph));
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void tickGraph(ServerWorld world, BlockGraph graph) {
        TICKED.put(world.getRegistryKey(), graph.getId());

        var coils = graph.getNodes()
                .filter(node -> node.data().getNode() instanceof CoilNode)
                .map(node -> CoilBlock.getBlockEntity(world, node.data().getPos()))
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
                    PowerNetworks.LOGGER.error("Energy distribution failed: no coil is accepting energy, but still have {} left. The distribution has been canceled. Graph id: {}, Involved coils: {}",
                            movingEnergy,
                            graph.getId(),
                            coils.stream().map(CoilBlockEntity::getPos).toList());
                    transaction.abort();
                    return;
                }
            }
            transaction.commit();
        }
    }
}
