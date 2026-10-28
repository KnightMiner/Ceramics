package knightminer.ceramics.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;

import java.util.function.Consumer;

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
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, Loadables.ITEM.getKey(item));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    consumer.accept(new LoadableFinishedRecipe<>(new CrackedClayRepairRecipe(id, item, repairIngredient), CrackedClayRepairRecipe.LOADABLE, buildOptionalAdvancement(id, "clay_repair")));
  }
}
