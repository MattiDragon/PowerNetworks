package io.github.mattidragon.powernetworks.virtual;

import com.kneelawk.graphlib.GraphLib;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.config.PowerNetworksConfig;
import io.github.mattidragon.powernetworks.item.WireItem;
import io.github.mattidragon.powernetworks.misc.CoilTransferMode;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class CoilDisplay {
    private final World world;
    private final BlockPos pos;
    private CoilTransferMode transferMode;

    private ElementHolder holder;
    private final LeashTargetElement leashTarget = new LeashTargetElement();
    private final Map<BlockPos, LeashSourceElement> connectionElements = new HashMap<>();
    private final Map<ServerPlayerEntity, LeashSourceElement> playerLeashes = new HashMap<>();

    public CoilDisplay(World world, BlockPos pos, CoilTransferMode transferMode) {
        Objects.requireNonNull(world, "world may not be null");
        this.world = world;
        this.pos = pos;
        this.transferMode = transferMode;
    }

    public void tick() {
        if (holder == null)
            setupDisplay();

        tickConnectionLeashes();
        tickPlayerLeashes();
    }

    public void clear() {
        if (holder != null) {
            holder.destroy();
            holder = null; // If the removal gets canceled we have to rebuild the display
        }
    }

    public void attachPlayerLeash(ServerPlayerEntity player) {
        if (playerLeashes.containsKey(player))
            return;

        var source = new LeashSourceElement(player.getId());
        holder.addElement(source);
        playerLeashes.put(player, source);
    }

    private void tickConnectionLeashes() {
        if (!(this.world instanceof ServerWorld serverWorld))
            return;

        var controller = GraphLib.getController(serverWorld);
        var node = controller.getNodesAt(pos).findFirst();

        if (node.isPresent()) {
            var useDoubleLeads = PowerNetworksConfig.get().misc().useDoubleLeads();
            var removedConnections = new HashSet<>(connectionElements.keySet());
            for (var link : node.get().connections()) {
                if (!useDoubleLeads) {
                    // If y positions are different we use the bottom one for target
                    var pos2 = link.other(node.get()).data().getPos();
                    if (pos.getY() < pos2.getY()) {
                        continue;
                    } else if (pos.getY() == pos2.getY() && node.get() == link.first()) {
                        continue;
                    }
                }

                var otherPos = link.other(node.get()).data().getPos();
                removedConnections.remove(otherPos);
                if (!connectionElements.containsKey(otherPos)) {
                    var coil2 = CoilBlock.getBlockEntity(serverWorld, otherPos);
                    if (coil2 == null || coil2.display == null)
                        continue;
                    var sourceElement = new LeashSourceElement(coil2.display.leashTarget.getEntityIds().getInt(0));
                    holder.addElement(sourceElement);
                    connectionElements.put(otherPos, sourceElement);
                }
            }

            for (var removed : removedConnections) {
                holder.removeElement(connectionElements.remove(removed));
            }
        }
    }

    private void tickPlayerLeashes() {
        for (var iterator = playerLeashes.entrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            var player = entry.getKey();
            var leash = entry.getValue();

            if (player.isRemoved() || !WireItem.hasAttachmentTo(player, pos)) {
                iterator.remove();
                holder.removeElement(leash);
            } else if (!holder.getElements().contains(leash)) {
                holder.addElement(leash);
            }
        }
    }

    private void setupDisplay() {
        var state = world.getBlockState(pos);

        if (holder != null) {
            holder.destroy();
            connectionElements.clear();
        }

        holder = new ElementHolder();
        // Adds attachment to chunk as side effect
        new ChunkAttachment(holder, (WorldChunk) world.getChunk(pos), Vec3d.ofCenter(pos), true);
        holder.addElement(leashTarget);
        {
            var coil = HeadElement.createCoil(((CoilBlock) state.getBlock()).getTier());
            var transform = createRotatedMatrix(state);
            transform.translate(0, 0.25f, 0);
            coil.setTransformation(transform);
            holder.addElement(coil);
        }
        if (transferMode != CoilTransferMode.DEFAULT) {
            var indicator = HeadElement.createTransferModeIndicator(transferMode);
            var transform = createRotatedMatrix(state);
            transform.rotate(RotationAxis.NEGATIVE_Z.rotationDegrees(180));
            transform.scale(0.5001f);
            transform.translate(0, 0.99f, 0);
            indicator.setTransformation(transform);
            holder.addElement(indicator);
        }
    }

    @NotNull
    private static Matrix4f createRotatedMatrix(BlockState state) {
        var transform = new Matrix4f();
        transform.rotate(switch (state.get(CoilBlock.FACING)) {
            case UP -> RotationAxis.NEGATIVE_X.rotationDegrees(0);
            case DOWN -> RotationAxis.NEGATIVE_X.rotationDegrees(180);
            case NORTH -> RotationAxis.NEGATIVE_X.rotationDegrees(90);
            case SOUTH -> RotationAxis.POSITIVE_X.rotationDegrees(90);
            case EAST -> RotationAxis.NEGATIVE_Z.rotationDegrees(90);
            case WEST -> RotationAxis.POSITIVE_Z.rotationDegrees(90);
        });
        return transform;
    }

    public void setTransferMode(CoilTransferMode transferMode) {
        this.transferMode = transferMode;
        setupDisplay();
    }
}
