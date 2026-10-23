package knightminer.ceramics.plugin.jei;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.recipe.KilnRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.recipe.NamedRecipe;

import java.util.List;
import java.util.Objects;

/** Category to show kilns in JEI. Basically a copy of the vanilla furnace category */
public class KilnCategory extends AbstractRecipeCategory<NamedRecipe<KilnRecipe>> {
  static final RecipeType<NamedRecipe<KilnRecipe>> TYPE = new RecipeType<>(Ceramics.getResource("kiln"), NamedRecipe.<KilnRecipe>recipeClass());
  /** List of fuels for the fuel slot. Will be an empty list during initial lookup. */
  static List<ItemStack> FUELS = List.of();
  // slots
  static final int INPUT_SLOT = 0;
  static final int FUEL_SLOT = 1;
  KilnCategory(IGuiHelper guiHelper) {
    super(TYPE,
      Ceramics.component("gui.jei.category", "kiln"),
      guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Registration.KILN)),
      82, 54
    );
  }


  /* Recipe */

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, NamedRecipe<KilnRecipe> holder, IFocusGroup focuses) {
    // input
    KilnRecipe recipe = holder.recipe();
    builder.addInputSlot(1, 1)
      .setStandardSlotBackground()
      .addIngredients(recipe.getInput());
    // fuel
    builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1, 37)
      .setStandardSlotBackground()
      .addItemStacks(FUELS);
    // output
    builder.addOutputSlot(61, 19)
      .setOutputSlotBackground()
      .addItemStack(recipe.getResultItem(Objects.requireNonNullElse(SafeClientAccess.getRegistryAccess(), RegistryAccess.EMPTY)));
  }

  @Override
  public void createRecipeExtras(IRecipeExtrasBuilder builder, NamedRecipe<KilnRecipe> holder, IFocusGroup focuses) {
    KilnRecipe recipe = holder.recipe();
    int cookTime = recipe.getCookingTime();
    if (cookTime <= 0) {
      cookTime = 100;
    }
    builder.addAnimatedRecipeArrowWidget(cookTime)
      .setPosition(26, 17);
    builder.addAnimatedRecipeFlameWidget(300)
      .setPosition(1, 20);

    addExperience(builder, recipe);
    addCookTime(builder, recipe);
  }

  /** Draws the recipe experience info */
  protected void addExperience(IRecipeExtrasBuilder builder, KilnRecipe recipe) {
    float experience = recipe.getExperience();
    if (experience > 0) {
      Component experienceString = Component.translatable("gui.jei.category.smelting.experience", experience);
      builder.addText(experienceString, getWidth() - 20, 10)
        .setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
        .setTextAlignment(HorizontalAlignment.RIGHT)
        .setColor(0xFF808080);
    }
  }

  /** Draws the recipe cook time info */
  protected void addCookTime(IRecipeExtrasBuilder builder, KilnRecipe recipe) {
    int cookTime = recipe.getCookingTime();
    if (cookTime > 0) {
      int cookTimeSeconds = cookTime / 20;
      Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
      builder.addText(timeString, getWidth() - 42, 10)
        .setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM)
        .setTextAlignment(HorizontalAlignment.CENTER)
        .setTextAlignment(VerticalAlignment.BOTTOM)
        .setColor(0xFF808080);
    }
  }

  @Override
  public boolean isHandled(NamedRecipe<KilnRecipe> recipe) {
    return true;
  }

  @Override
  public ResourceLocation getRegistryName(NamedRecipe<KilnRecipe> recipe) {
    return recipe.id();
  }
}
