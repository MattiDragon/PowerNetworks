package io.github.mattidragon.powernetworks.client.config;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;

public class PlayerHeadImageRenderer implements ImageRenderer {
    private final GameProfile profile;
    private final SkullBlockEntityModel model;
    private float time = 0;

    public PlayerHeadImageRenderer(String value) {
        model = new SkullEntityModel(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_HEAD));
        profile = new GameProfile(Util.NIL_UUID, "");
        profile.getProperties().put("textures", new Property("Value", value));
    }

    @Override
    public int render(DrawContext graphics, int x, int y, int renderWidth, float tickDelta) {
        var client = MinecraftClient.getInstance();
        int size = renderWidth / 2;
        time += client.getLastFrameDuration();

        var matrices = graphics.getMatrices();
        matrices.push();
        matrices.translate(x + renderWidth / 2.0, y, 10);
        matrices.scale(size, size, -1);
        matrices.translate(-0.5, 0, 0);
        matrices.translate(0, 0.2, 0);

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
        matrices.translate(-0.5, -0.5, -0.5);

        var renderLayer = SkullBlockEntityRenderer.getRenderLayer(SkullBlock.Type.PLAYER, profile);
        DiffuseLighting.enableGuiDepthLighting();
        SkullBlockEntityRenderer.renderSkull(null, 360 * (time % 200 / 200), 0, matrices, graphics.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE, model, renderLayer);
        DiffuseLighting.disableGuiDepthLighting();
        matrices.pop();

        return size;
    }

    @Override
    public void close() {

    }
}
