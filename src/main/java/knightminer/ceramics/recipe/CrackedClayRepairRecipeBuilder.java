package knightminer.ceramics.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;

/** Builder for {@link CrackedClayRepairRecipe} */
public class CrackedClayRepairRecipeBuilder extends AbstractRecipeBuilder<CrackedClayRepairRecipeBuilder> {
  private final Item item;
  private Ingredient repairIngredient = Ingredient.of(CeramicsTags.Items.TERRACOTTA_CRACK_REPAIR);

  private CrackedClayRepairRecipeBuilder(Item item) {
    this.item = item;
  }

  public static CrackedClayRepairRecipeBuilder item(ItemLike item) {
    return new CrackedClayRepairRecipeBuilder(item.asItem());
  }

  /** Sets the repair ingredient */
  public CrackedClayRepairRecipeBuilder repair(Ingredient ingredient) {
    this.repairIngredient = ingredient;
    return this;
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, BuiltInRegistries.ITEM.getKey(item));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    output.accept(id, new CrackedClayRepairRecipe(item, repairIngredient), buildOptionalAdvancement(output, id, "clay_repair"));
  }
}
