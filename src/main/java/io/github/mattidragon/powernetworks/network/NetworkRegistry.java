package io.github.mattidragon.powernetworks.network;

import com.kneelawk.graphlib.GraphLib;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.misc.CoilTier;
import net.minecraft.registry.Registry;

import java.util.List;

public class NetworkRegistry {

    private NetworkRegistry() {
    }

    public static void register() {
        GraphLib.registerDiscoverer((world, pos) -> {
            if (world.getBlockState(pos).getBlock() instanceof CoilBlock block) {
                return List.of(CoilNode.INSTANCES.get(block.getTier()));
            }
            return List.of();
        });

        Registry.register(GraphLib.BLOCK_NODE_DECODER, CoilNode.ID, CoilNode::fromTag);
    }
}
