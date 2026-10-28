package knightminer.ceramics.recipe;

import knightminer.ceramics.Registration;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
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
    ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP, CookingResultRecipe.CATEGORY_FIELD,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", KilnRecipe::getInput),
    RESULT_FIELD, EXPERIENCE_FIELD, COOKING_TIME_FIELD,
    KilnRecipe::new);

  private final ItemOutput result;
  public KilnRecipe(ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient, ItemOutput result, float experience, int cookTime) {
    super(Registration.KILN_RECIPE.get(), id, group, category, ingredient, ItemStack.EMPTY, experience, cookTime);
    this.result = result;
  }

  /** @deprecated use {@link #KilnRecipe(ResourceLocation, String, CookingBookCategory, Ingredient, ItemOutput, float, int)} */
  @Deprecated(forRemoval = true)
  public KilnRecipe(ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookTime) {
    this(id, group, category, ingredient, ItemOutput.fromStack(result), experience, cookTime);
  }

  /** Gets the input for JEI */
  public Ingredient getInput() {
    return ingredient;
  }

  @Override
  public ItemOutput getResult() {
    return result;
  }

  @Override
  public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
    return result.get();
  }

  @Override
  public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
    return result.copy();
  }

  @Override
  public ItemStack getToastSymbol() {
    return new ItemStack(Registration.KILN);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.KILN_SERIALIZER.get();
  }
}
