package io.github.mattidragon.powernetworks.network;

import com.kneelawk.graphlib.graph.BlockNodeHolder;
import com.kneelawk.graphlib.graph.NodeView;
import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.wire.FullWireBlockNode;
import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.misc.CoilTier;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public class CoilNode implements FullWireBlockNode {
    public static final Identifier ID = PowerNetworks.id("connection_coil");
    public static final EnumMap<CoilTier, CoilNode> INSTANCES = new EnumMap<>(CoilTier.class);

    static {
        for (CoilTier tier : CoilTier.values()) {
            INSTANCES.put(tier, new CoilNode(tier));
        }
    }

    public final CoilTier tier;

    private CoilNode(CoilTier tier) {
        this.tier = tier;
    }

    @Override
    public @NotNull Identifier getTypeId() {
        return ID;
    }

    @Override
    public @NotNull Collection<Node<BlockNodeHolder>> findConnections(@NotNull ServerWorld world, @NotNull NodeView nodeView, @NotNull BlockPos pos, @NotNull Node<BlockNodeHolder> self) {
        var out = new ArrayList<Node<BlockNodeHolder>>();
        var coil = CoilBlock.getBlockEntity(world, pos);
        if (coil == null)
            return List.of();

        for (var connection : coil.getConnections()) {
            nodeView.getNodesAt(connection).filter(node1 -> canConnect(world, nodeView, pos, self, node1))
                    .forEach(out::add);
        }

        return out;
    }

    @Override
    public boolean canConnect(@NotNull ServerWorld world, @NotNull NodeView nodeView, @NotNull BlockPos pos, @NotNull Node<BlockNodeHolder> self, @NotNull Node<BlockNodeHolder> other) {
        return other.data().getNode() instanceof CoilNode && pos.getSquaredDistance(other.data().getPos()) <= 64;
    }

    @Override
    public void onConnectionsChanged(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull Node<BlockNodeHolder> self) {
    }

    @Override
    @Nullable
    public NbtElement toTag() {
        return NbtByte.of((byte) tier.ordinal());
    }

    public static CoilNode fromTag(NbtElement nbt) {
        CoilTier tier;
        if (nbt instanceof AbstractNbtNumber number) {
            var value = number.byteValue();
            if (value < 0 || value > CoilTier.values().length) {
                tier = CoilTier.BASIC;
            } else {
                tier = CoilTier.values()[number.byteValue()];
            }
        } else {
            tier = CoilTier.BASIC;
        }
        return INSTANCES.get(tier);
    }
}
