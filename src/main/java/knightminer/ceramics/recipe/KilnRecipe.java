package knightminer.ceramics.recipe;

import knightminer.ceramics.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LegacyField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.cooking.CookingResultRecipe;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;

/** Recipe for smelting in the kiln. */
public class KilnRecipe extends AbstractCookingRecipe implements CookingResultRecipe {
  public static LoadableField<Integer, AbstractCookingRecipe> COOKING_TIME_FIELD = new LegacyField<>(IntLoadable.FROM_ONE.defaultField("cooking_time", 100, true, AbstractCookingRecipe::getCookingTime), "cookingtime");
  public static final RecordLoadable<KilnRecipe> LOADABLE = RecordLoadable.create(
    LoadableRecipeSerializer.RECIPE_GROUP, CookingResultRecipe.CATEGORY_FIELD,
    Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("ingredient", r -> r.ingredient),
    RESULT_FIELD, EXPERIENCE_FIELD, COOKING_TIME_FIELD,
    KilnRecipe::new);

  private final ItemOutput result;

  public KilnRecipe(String group, CookingBookCategory category, Ingredient ingredient, ItemOutput result, float experience, int cookTime) {
    super(Registration.KILN_RECIPE.get(), group, category, ingredient, ItemStack.EMPTY, experience, cookTime);
    this.result = result;
  }

  /** Gets the input for JEI */
  public Ingredient getInput() {
    return ingredient;
  }


  /* Kiln category */

  @Override
  public ItemStack getToastSymbol() {
    return new ItemStack(Registration.KILN);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.KILN_SERIALIZER.get();
  }


  /* Item output result */

  @Override
  public ItemOutput getResult() {
    return result;
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return result.get();
  }

  @Override
  public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
    return result.copy();
  }
}
