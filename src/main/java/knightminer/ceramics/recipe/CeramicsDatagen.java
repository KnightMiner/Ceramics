package knightminer.ceramics.recipe;

import knightminer.ceramics.Registration;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** Public helpers for performing datagen */
public class CeramicsDatagen {
  private CeramicsDatagen() {}

  /** Gets the categroy for the given kiln recipe */
  private static CookingBookCategory determineKilnCategory(ItemLike result) {
    return result.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
  }

  /** Creates a kiln recipe builder */
  public static SimpleCookingRecipeBuilder kilnRecipe(Ingredient input, RecipeCategory category, ItemLike output, float experience, int cookTime) {
    return new SimpleCookingRecipeBuilder(category, determineKilnCategory(output), output, input, experience, cookTime, Registration.KILN_SERIALIZER.get());
  }
}
