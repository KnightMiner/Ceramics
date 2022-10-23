package knightminer.ceramics.recipe;

import knightminer.ceramics.Registration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;

public class KilnRecipe extends AbstractCookingRecipe {
  public KilnRecipe(ResourceLocation id, String group, Ingredient ingredient, ItemStack result, float experience, int cookTime) {
    super(Registration.KILN_RECIPE, id, group, ingredient, result, experience, cookTime);
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
