package io.github.mattidragon.powernetworks.network;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.util.HalfLink;
import com.kneelawk.graphlib.api.wire.FullWireBlockNode;
import io.github.mattidragon.powernetworks.PowerNetworks;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class CoilNode implements FullWireBlockNode {
    public static final Identifier ID = PowerNetworks.id("connection_coil");
    public static final CoilNode INSTANCE = new CoilNode();
    public static final BlockNodeType TYPE = BlockNodeType.of(ID, tag -> INSTANCE, (buf, ctx) -> INSTANCE);

    private CoilNode() {
    }

    @Override
    public @NotNull BlockNodeType getType() {
        return TYPE;
    }

    @Override
    public @NotNull Collection<HalfLink> findConnections(@NotNull NodeHolder<BlockNode> self) {
        return List.of();
    }

    @Override
    public boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull HalfLink other) {
        return other.other().getNode() instanceof CoilNode && self.getBlockPos().getSquaredDistance(other.other().getBlockPos()) <= 64;
    }

    @Override
    public void onConnectionsChanged(@NotNull NodeHolder<BlockNode> self) {
        var world = self.getBlockWorld();
        var pos = self.getBlockPos();
        var state = self.getBlockState();
        world.updateListeners(pos, state, state, 0);
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }
}
