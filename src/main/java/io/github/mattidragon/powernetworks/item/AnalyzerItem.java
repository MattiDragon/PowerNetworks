package io.github.mattidragon.powernetworks.item;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.util.NodePos;
import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.mattidragon.powernetworks.block.CoilBlock;
import io.github.mattidragon.powernetworks.misc.CoilTier;
import io.github.mattidragon.powernetworks.network.CoilNode;
import io.github.mattidragon.powernetworks.network.NetworkEnergyStorage;
import io.github.mattidragon.powernetworks.network.NetworkRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnalyzerItem extends Item implements PolymerItem {
    private static final NumberFormat NUMBER_FORMAT = Util.make(NumberFormat.getInstance(Locale.ENGLISH), format -> {
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        format.setGroupingUsed(false);
    });

    public AnalyzerItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.COMPARATOR;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() == null) return ActionResult.PASS; // Can't show message anyway
        var world = context.getWorld();
        var pos = context.getBlockPos();
        var graph = NetworkRegistry.UNIVERSE.getGraphView(world).getGraphForNode(new NodePos(pos, CoilNode.INSTANCE));
        if (graph == null) return ActionResult.PASS;
        if (world.isClient) return ActionResult.SUCCESS; // No need to do math on client as we only send message from server anyway
        var storage = graph.getGraphEntity(NetworkEnergyStorage.TYPE);
        var profiles = storage.getProfiles();

        var tickProfile = profiles.peekFirst();
        if (tickProfile == null) tickProfile = new NetworkEnergyStorage.Profile(0, 0);

        var minuteTotalProfile = profiles.stream()
                .reduce(new NetworkEnergyStorage.Profile(0, 0),
                        (first, second) -> new NetworkEnergyStorage.Profile(first.inserted() + second.inserted(), first.extracted() + second.extracted()));
        var minuteProfileCount = profiles.size(); // We could have less than a minute of data
        var minuteAverageProfile = new NetworkEnergyStorage.Profile(minuteTotalProfile.inserted() / minuteProfileCount, minuteTotalProfile.extracted() / minuteProfileCount);

        var coilCounts = graph.getNodes()
                .map(NodeHolder::getBlockState)
                .map(BlockState::getBlock)
                .filter(CoilBlock.class::isInstance)
                .map(CoilBlock.class::cast)
                .map(CoilBlock::getTier)
                .collect(Collectors.toMap(Function.identity(), tier -> 1, Integer::sum));

        var message = Text.literal("\n");
        message.append(Text.translatable("item.power_networks.analyzer.info.title").formatted(Formatting.BOLD)).append("\n");
        message.append(Text.translatable("item.power_networks.analyzer.info.fill_level",
                        Text.literal(storage.getAmount() + "/" + storage.getCapacity()).formatted(Formatting.YELLOW)))
                .append("\n");

        message.append(Text.translatable("item.power_networks.analyzer.info.flow_rate",
                        Text.translatable("item.power_networks.analyzer.info.flow_rate.tick").formatted(Formatting.YELLOW),
                        Text.translatable("item.power_networks.analyzer.info.flow_rate.minute_average").formatted(Formatting.GREEN),
                        Text.translatable("item.power_networks.analyzer.info.flow_rate.minute_total").formatted(Formatting.AQUA)))
                .append("\n");
        message.append(Text.translatable("item.power_networks.analyzer.info.flow_rate.inserted",
                        Text.literal(NUMBER_FORMAT.format(tickProfile.inserted())).formatted(Formatting.YELLOW),
                        Text.literal(NUMBER_FORMAT.format(minuteAverageProfile.inserted())).formatted(Formatting.GREEN),
                        Text.literal(NUMBER_FORMAT.format(minuteTotalProfile.inserted())).formatted(Formatting.AQUA)))
                .append("\n");
        message.append(Text.translatable("item.power_networks.analyzer.info.flow_rate.extracted",
                        Text.literal(NUMBER_FORMAT.format(tickProfile.extracted())).formatted(Formatting.YELLOW),
                        Text.literal(NUMBER_FORMAT.format(minuteAverageProfile.extracted())).formatted(Formatting.GREEN),
                        Text.literal(NUMBER_FORMAT.format(minuteTotalProfile.extracted())).formatted(Formatting.AQUA)))
                .append("\n");
        message.append(Text.translatable("item.power_networks.analyzer.info.flow_rate.total",
                        Text.literal(NUMBER_FORMAT.format(tickProfile.inserted() - tickProfile.extracted())).formatted(Formatting.YELLOW),
                        Text.literal(NUMBER_FORMAT.format(minuteAverageProfile.inserted() - minuteAverageProfile.extracted())).formatted(Formatting.GREEN),
                        Text.literal(NUMBER_FORMAT.format(minuteTotalProfile.inserted() - minuteTotalProfile.extracted())).formatted(Formatting.AQUA)))
                .append("\n");

        var coilCountTexts = new ArrayList<Text>();
        if (coilCounts.containsKey(CoilTier.BASIC)) coilCountTexts.add(Text.translatable("item.power_networks.analyzer.info.coils.basic", Text.literal(String.valueOf(coilCounts.get(CoilTier.BASIC))).formatted(Formatting.GRAY)));
        if (coilCounts.containsKey(CoilTier.IMPROVED)) coilCountTexts.add(Text.translatable("item.power_networks.analyzer.info.coils.improved", Text.literal(String.valueOf(coilCounts.get(CoilTier.IMPROVED))).formatted(Formatting.YELLOW)));
        if (coilCounts.containsKey(CoilTier.ADVANCED)) coilCountTexts.add(Text.translatable("item.power_networks.analyzer.info.coils.advanced", Text.literal(String.valueOf(coilCounts.get(CoilTier.ADVANCED))).formatted(Formatting.AQUA)));
        if (coilCounts.containsKey(CoilTier.ULTIMATE)) coilCountTexts.add(Text.translatable("item.power_networks.analyzer.info.coils.ultimate", Text.literal(String.valueOf(coilCounts.get(CoilTier.ULTIMATE))).formatted(Formatting.DARK_GRAY)));

        message.append(Text.translatable("item.power_networks.analyzer.info.coils",
                        Text.literal(String.valueOf(graph.getNodes().count())).formatted(Formatting.GREEN),
                        Texts.join(coilCountTexts, Text.literal(", "))))
                .append("\n");
        message.append(Text.translatable("item.power_networks.analyzer.info.wires",
                        Text.literal(String.valueOf(graph.getLinkEntities().count())).fillStyle(Style.EMPTY.withColor(0xd07d59))));

        context.getPlayer().sendMessage(message);

        return ActionResult.SUCCESS;
    }
}
