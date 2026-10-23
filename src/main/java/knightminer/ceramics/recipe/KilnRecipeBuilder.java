package knightminer.ceramics.recipe;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.cooking.CookingRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;

/** Recipe builder for kiln recipes. Doubles as a builder for vanilla furnace recipes. */
public class KilnRecipeBuilder extends CookingRecipeBuilder<KilnRecipeBuilder> {
  protected KilnRecipeBuilder(ItemOutput result) {
    super(result);
  }

  /** Creates a new builder instance */
  public static KilnRecipeBuilder builder(ItemOutput result) {
    return new KilnRecipeBuilder(result);
  }

  /** Creates a new builder instance */
  public static KilnRecipeBuilder builder(ItemLike output, int amount) {
    return builder(ItemOutput.fromItem(output, amount));
  }

  /** Creates a new builder instance */
  public static KilnRecipeBuilder builder(ItemLike output) {
    return builder(output, 1);
  }

  /** Creates a new builder instance for a tag with the given size */
  public static KilnRecipeBuilder builder(TagKey<Item> result, int amount) {
    return builder(ItemOutput.fromTag(result, amount));
  }

  /** Creates a new builder instance for a tag with size of 1 */
  public static KilnRecipeBuilder builder(TagKey<Item> result) {
    return builder(result, 1);
  }

  /** @deprecated default save method saves kiln recipes */
  @Deprecated(forRemoval = true)
  @Override
  public KilnRecipeBuilder type(CookingType type) {
    return super.type(type);
  }

  /** Saves a kiln recipe at the given ID */
  public KilnRecipeBuilder saveKiln(RecipeOutput output, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    output.accept(id, new KilnRecipe(group, category, ingredient, result, experience, cookingTime / 2), buildOptionalAdvancement(output, id, "cooking"));
    return this;
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    saveKiln(output, id);
  }
}
