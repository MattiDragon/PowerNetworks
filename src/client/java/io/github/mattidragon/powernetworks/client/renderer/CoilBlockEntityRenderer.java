package io.github.mattidragon.powernetworks.client.renderer;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.util.NodePos;
import io.github.mattidragon.powernetworks.PowerNetworks;
import io.github.mattidragon.powernetworks.block.CoilBlockEntity;
import io.github.mattidragon.powernetworks.item.WireItem;
import io.github.mattidragon.powernetworks.network.CoilNode;
import io.github.mattidragon.powernetworks.network.NetworkRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class CoilBlockEntityRenderer implements BlockEntityRenderer<CoilBlockEntity> {
    public CoilBlockEntityRenderer(BlockEntityRendererFactory.Context ignoredCtx) {
    }

    @Override
    public void render(CoilBlockEntity coil, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var world = coil.getWorld();
        if (world == null) return;
        var pos = coil.getPos();
        var node = NetworkRegistry.UNIVERSE.getGraphView(world).getNodeAt(new NodePos(pos, CoilNode.INSTANCE));
        if (node == null) return;

        var playerConnections = ((ClientWorld) world).getPlayers()
                .stream()
                .filter(player -> WireItem.hasAttachmentTo(player, pos))
                .toList();

        var blockConnections = node.getConnections()
                .stream()
                .map(link -> link.other(node))
                .map(NodeHolder::getBlockPos)
                .filter(otherPos -> otherPos.asLong() < pos.asLong())
                .toList();

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);

        for (var player : playerConnections) {
            var destinationLight = WorldRenderer.getLightmapCoordinates(world, BlockPos.ofFloored(player.getCameraPosVec(tickDelta)));
            drawConnection(Vec3d.ofCenter(pos), player.getLeashPos(tickDelta), matrices, vertexConsumers, light, destinationLight);
        }

        for (var toPos : blockConnections) {
            var destinationLight = WorldRenderer.getLightmapCoordinates(world, pos);
            drawConnection(Vec3d.ofCenter(pos), Vec3d.ofCenter(toPos), matrices, vertexConsumers, light, destinationLight);
        }

        matrices.pop();
    }

    private static void drawConnection(Vec3d fromPos, Vec3d toPos, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int fromLight, int toLight) {
        var offset = new Vector3f((float) (toPos.x - fromPos.x), (float) (toPos.y - fromPos.y), (float) (toPos.z - fromPos.z));
        var segments = (int) offset.length() * PowerNetworks.CONFIG.get().client().segmentsPerBlock();
        var segmentOffset = offset.div(segments, new Vector3f());

        var vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLeash());
        for (int segment = 0; segment <= segments; segment++) {
            var light = MathHelper.lerp((float) segment / segments, fromLight, toLight);
            var from = segmentOffset.mul(segment, new Vector3f());
            from.y = getHeightModifier((float) segment / segments, offset.y, new Vector2f(offset.x, offset.z).length());
            var to = segmentOffset.mul(segment + 1, new Vector3f());
            to.y = getHeightModifier((float) (segment + 1) / segments, offset.y, new Vector2f(offset.x, offset.z).length());
            drawSegment(from, to, matrices, vertexConsumer, light, segment, true);
        }

        vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLeash());
        for (int segment = 0; segment <= segments; segment++) {
            var light = MathHelper.lerp((float) segment / segments, fromLight, toLight);
            var from = segmentOffset.mul(segment, new Vector3f());
            from.y = getHeightModifier((float) segment / segments, offset.y, new Vector2f(offset.x, offset.z).length());
            var to = segmentOffset.mul(segment + 1, new Vector3f());
            to.y = getHeightModifier((float) (segment + 1) / segments, offset.y, new Vector2f(offset.x, offset.z).length());
            drawSegment(from, to, matrices, vertexConsumer, light, segment, false);
        }

    }

    private static void drawSegment(Vector3f from, Vector3f to, MatrixStack matrices, VertexConsumer vertexConsumer, int light, int index, boolean isFirstPass) {
        var direction = to.sub(from, new Vector3f()).normalize();
        var angle = (float) Math.acos(new Vector3f(0, 1, 0).dot(direction));

        matrices.push();
        matrices.translate(from.x, from.y, from.z);


        var cross = new Vector3f(0, 1, 0).cross(direction);
        if (cross.length() != 0)
            matrices.multiply(new Quaternionf().rotationAxis(angle, cross));

        var color = getColor(index);
        var width = PowerNetworks.CONFIG.get().client().wireWidth();
        var matrix = matrices.peek().getPositionMatrix();
        if (isFirstPass) {
            vertexConsumer.vertex(matrix, 0, 0, width / 2).color(color).light(light).next();
            vertexConsumer.vertex(matrix, 0, 0, width / -2).color(color).light(light).next();
        } else {
            vertexConsumer.vertex(matrix, width / 2, 0, 0).color(color).light(light).next();
            vertexConsumer.vertex(matrix, width / -2, 0, 0).color(color).light(light).next();
        }

        matrices.pop();
    }

    private static float getHeightModifier(float progress, float height, float length) {
        var a = height * progress * progress;
        var b = height - height * (1f - progress) * (1f - progress);
        var x = length * progress;
        // Disable hang for fully vertical connections as it causes a NaN and wouldn't make sense anyway
        float c;
        c = length == 0 ? 0 : PowerNetworks.CONFIG.get().client().hangFactor() * x * (x - length) / (length * length);

        return height > 0 ? a + c : b + c;
    }

    private static int getColor(int segmentIndex) {
        var colors = PowerNetworks.CONFIG.get().client().colors();
        if (colors.size() == 0)
            return 0xffff00ff;

        return colors.get(segmentIndex % colors.size()) | 0xff000000;
    }
}
