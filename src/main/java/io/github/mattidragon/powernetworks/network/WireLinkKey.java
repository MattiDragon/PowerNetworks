package io.github.mattidragon.powernetworks.network;

import com.kneelawk.graphlib.api.graph.LinkContext;
import com.kneelawk.graphlib.api.graph.LinkEntityContext;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import io.github.mattidragon.powernetworks.item.ModItems;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.github.mattidragon.powernetworks.PowerNetworks.id;

public class WireLinkKey implements LinkKey {
    public static final Identifier ID = id("wire");
    public static final WireLinkKey INSTANCE = new WireLinkKey();

    private WireLinkKey() {
    }

    @Override
    public @NotNull Identifier getTypeId() {
        return ID;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }

    @Override
    public boolean shouldHaveLinkEntity(@NotNull LinkContext ctx) {
        return true;
    }

    @Override
    public @Nullable LinkEntity createLinkEntity(@NotNull LinkEntityContext ctx) {
        return new Entity(ctx);
    }

    @Override
    public boolean isAutomaticRemoval(@NotNull LinkContext ctx) {
        return false;
    }

    public static class Entity implements LinkEntity {
        public static final Identifier ID = id("wire");

        private final LinkEntityContext context;

        public Entity(LinkEntityContext context) {
            this.context = context;
        }

        @Override
        public @NotNull Identifier getTypeId() {
            return ID;
        }

        @Override
        public @Nullable NbtElement toTag() {
            return null;
        }

        @Override
        public void onUnload() {
        }

        @Override
        public void onDelete() {
            var center = context.getFirstBlockPos().add(context.getSecondBlockPos()).toCenterPos().multiply(0.5);
            ItemScatterer.spawn(context.getBlockWorld(), center.x, center.y, center.z, ModItems.WIRE.getDefaultStack());
        }
    }
}
