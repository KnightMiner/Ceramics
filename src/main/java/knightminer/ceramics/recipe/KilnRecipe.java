package knightminer.ceramics.recipe;

import knightminer.ceramics.Registration;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public class KilnRecipe extends AbstractCookingRecipe {
  public KilnRecipe(ResourceLocation id, String group, Ingredient ingredient, ItemStack result, float experience, int cookTime) {
    super(Registration.KILN_RECIPE, id, group, ingredient, result, experience, cookTime);
  }

  @Override
  public ItemStack getIcon() {
    return new ItemStack(Registration.KILN);
  }

  @Override
  public IRecipeSerializer<?> getSerializer() {
    return Registration.KILN_SERIALIZER.get();
  }
}
