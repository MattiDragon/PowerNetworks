package io.github.mattidragon.powernetworks.network;

import com.kneelawk.graphlib.api.graph.LinkEntityContext;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.user.*;
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
    public static final LinkKeyType TYPE = LinkKeyType.of(ID, () -> INSTANCE);

    private WireLinkKey() {
    }

    @Override
    public @NotNull LinkKeyType getType() {
        return TYPE;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }

    @Override
    public boolean shouldHaveLinkEntity(@NotNull LinkHolder<LinkKey> self) {
        return true;
    }

    @Override
    public @Nullable LinkEntity createLinkEntity(@NotNull LinkHolder<LinkKey> holder) {
        return new Entity();
    }

    @Override
    public boolean isAutomaticRemoval(@NotNull LinkHolder<LinkKey> self) {
        return false;
    }

    public static class Entity implements LinkEntity {
        public static final Identifier ID = id("wire");
        public static final LinkEntityType TYPE = LinkEntityType.of(ID, Entity::new);

        private LinkEntityContext context;

        @Override
        public void onInit(@NotNull LinkEntityContext context) {
            this.context = context;
        }

        @Override
        public @NotNull LinkEntityContext getContext() {
            return context;
        }

        @Override
        public @NotNull LinkEntityType getType() {
            return TYPE;
        }

        @Override
        public @Nullable NbtElement toTag() {
            return null;
        }

        @Override
        public void onDelete() {
            var center = context.getFirstBlockPos().add(context.getSecondBlockPos()).toCenterPos().multiply(0.5);
            ItemScatterer.spawn(context.getBlockWorld(), center.x, center.y, center.z, ModItems.WIRE.getDefaultStack());
        }
    }
}
