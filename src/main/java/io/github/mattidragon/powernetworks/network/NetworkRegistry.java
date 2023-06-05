package io.github.mattidragon.powernetworks.network;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import io.github.mattidragon.powernetworks.block.CoilBlock;

import java.util.List;

import static io.github.mattidragon.powernetworks.PowerNetworks.id;

public class NetworkRegistry {
    public static final GraphUniverse UNIVERSE = GraphUniverse.builder().build(id("networks"));
    public static final GraphEntityType<NetworkUpdateHandler> UPDATE_HANDLER = GraphEntityType.of(NetworkUpdateHandler.ID, NetworkUpdateHandler::new, (tag, ctx) -> new NetworkUpdateHandler(ctx), (original, originalGraph, ctx) -> new NetworkUpdateHandler(ctx));

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
        UNIVERSE.addGraphEntityType(UPDATE_HANDLER);
    }
}
