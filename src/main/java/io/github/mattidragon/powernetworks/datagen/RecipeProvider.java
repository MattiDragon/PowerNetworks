package io.github.mattidragon.powernetworks.datagen;

import io.github.mattidragon.powernetworks.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;

public class RecipeProvider extends FabricRecipeProvider {
    RecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, ModItems.WIRE, 16)
                .input('I', Items.COPPER_INGOT)
                .input('N', Items.IRON_NUGGET)
                .pattern("NNN")
                .pattern("III")
                .criterion("has_copper", conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        createCoilRecipe(ModItems.BASIC_COIL, Items.IRON_INGOT, exporter);
        createCoilRecipe(ModItems.IMPROVED_COIL, Items.GOLD_INGOT, exporter);
        createCoilRecipe(ModItems.ADVANCED_COIL, Items.DIAMOND, exporter);
        createCoilRecipe(ModItems.ULTIMATE_COIL, Items.NETHERITE_INGOT, exporter);
    }

    private static void createCoilRecipe(Item coil, Item material, RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, coil, 2)
                .input('I', material)
                .input('W', ModItems.WIRE)
                .pattern(" W ")
                .pattern("WIW")
                .pattern(" W ")
                .criterion("has_wire", conditionsFromItem(ModItems.WIRE))
                .offerTo(exporter);
    }
}
