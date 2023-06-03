package io.github.mattidragon.powernetworks.network;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import io.github.mattidragon.powernetworks.block.CoilBlock;

import java.util.List;

import static io.github.mattidragon.powernetworks.PowerNetworks.id;

public class NetworkRegistry {
    public static final GraphUniverse UNIVERSE = GraphUniverse.builder().build(id("networks"));
    public static final GraphEntityType<NetworkUpdateHandler> UPDATE_HANDLER = new GraphEntityType<>(NetworkUpdateHandler.ID, NetworkUpdateHandler::new, (tag, ctx) -> new NetworkUpdateHandler(ctx), (original, originalGraph, ctx) -> new NetworkUpdateHandler(ctx));

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
        UNIVERSE.addNodeDecoder(CoilNode.ID, tag -> CoilNode.INSTANCE);

        UNIVERSE.addLinkKeyDecoder(WireLinkKey.ID, tag -> WireLinkKey.INSTANCE);
        UNIVERSE.addLinkEntityDecoder(WireLinkKey.Entity.ID, (tag, ctx) -> new WireLinkKey.Entity(ctx));

        UNIVERSE.addGraphEntityType(UPDATE_HANDLER);
    }
}
