package io.github.mattidragon.powernetworks.network;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.SyncProfile;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.networking.PowerNetworksNetworking;

import java.util.List;

import static io.github.mattidragon.powernetworks.PowerNetworks.id;

public class NetworkRegistry {
    public static final GraphUniverse UNIVERSE = GraphUniverse.builder()
            .synchronizeToClient(SyncProfile.of(PowerNetworksNetworking::supportsClientRendering))
            .build(id("networks"));

    private NetworkRegistry() {
    }

    public static void register() {
        UNIVERSE.register();
        UNIVERSE.addDiscoverer((world, pos) -> {
            if (world.getBlockState(pos).getBlock() instanceof CoilBlock) {
                return List.of(CoilNode.INSTANCE);
            }
            return List.of();
        });
        UNIVERSE.addNodeType(CoilNode.TYPE);
        UNIVERSE.addLinkKeyType(WireLinkKey.TYPE);
        UNIVERSE.addLinkEntityType(WireLinkKey.Entity.TYPE);
        UNIVERSE.addGraphEntityType(NetworkEnergyStorage.TYPE);
    }
}
