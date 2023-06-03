package io.github.mattidragon.powernetworks.network;

import com.kneelawk.graphlib.api.graph.NodeContext;
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

    private CoilNode() {
    }

    @Override
    public @NotNull Identifier getTypeId() {
        return ID;
    }

    @Override
    public @NotNull Collection<HalfLink> findConnections(@NotNull NodeContext ctx) {
        return List.of();
    }

    @Override
    public boolean canConnect(@NotNull NodeContext ctx, @NotNull HalfLink other) {
        return other.other().getNode() instanceof CoilNode && ctx.getPos().getSquaredDistance(other.other().getPos()) <= 64;
    }

    @Override
    public void onConnectionsChanged(@NotNull NodeContext ctx) {
        var world = ctx.blockWorld();
        var pos = ctx.getPos();
        var state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, 0);
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }
}
