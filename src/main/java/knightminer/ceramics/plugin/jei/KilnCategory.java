package knightminer.ceramics.plugin.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.recipe.KilnRecipe;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ForgeI18n;

@MethodsReturnNonnullByDefault
public class KilnCategory implements IRecipeCategory<KilnRecipe> {
  static final ResourceLocation UID = new ResourceLocation(Ceramics.MOD_ID, "kiln");
  private static final ResourceLocation RECIPE_GUI_VANILLA = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
  // slots
  static final int INPUT_SLOT = 0;
  static final int FUEL_SLOT = 1;
  private static final int OUTPUT_SLOT = 2;
  // elements
  private final IDrawable background;
  private final IDrawable icon;
  private final IDrawableAnimated animatedFlame;
  private final IDrawableAnimated arrow;
  private final String localizedName;

  KilnCategory(IGuiHelper guiHelper) {
    // elements
    this.background = guiHelper.createDrawable(RECIPE_GUI_VANILLA, 0, 114, 82, 54);
    this.icon = guiHelper.createDrawableIngredient(new ItemStack(Registration.KILN));
    this.localizedName = ForgeI18n.parseMessage("gui.jei.category.ceramics.kiln");
    // animations
    IDrawableStatic staticFlame = guiHelper.createDrawable(RECIPE_GUI_VANILLA, 82, 114, 14, 14);
    this.animatedFlame = guiHelper.createAnimatedDrawable(staticFlame, 300, StartDirection.TOP, true);
    this.arrow = guiHelper.drawableBuilder(RECIPE_GUI_VANILLA, 82, 128, 24, 17).buildAnimated(100, StartDirection.LEFT, false);
  }


  /* Properties */

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public Class<? extends KilnRecipe> getRecipeClass() {
    return KilnRecipe.class;
  }

  @Override
  public String getTitle() {
    return this.localizedName;
  }

  @Override
  public IDrawable getIcon() {
    return this.icon;
  }

  @Override
  public IDrawable getBackground() {
    return this.background;
  }


  /* Recipe */

  @Override
  public void setIngredients(KilnRecipe recipe, IIngredients ingredients) {
    ingredients.setInputIngredients(recipe.getIngredients());
    ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
  }

  @Override
  public void setRecipe(IRecipeLayout recipeLayout, KilnRecipe recipe, IIngredients ingredients) {
    IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
    guiItemStacks.init(INPUT_SLOT, true, 0, 0);
    guiItemStacks.init(OUTPUT_SLOT, false, 60, 18);
    guiItemStacks.set(ingredients);
  }

  @Override
  public void draw(KilnRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
    this.animatedFlame.draw(matrixStack, 1, 20);
    this.arrow.draw(matrixStack, 24, 18);

    float experience = recipe.getExperience();
    if (experience > 0.0F) {
      String experienceString = I18n.format("gui.jei.category.smelting.experience", experience);
      FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
      int stringWidth = fontRenderer.getStringWidth(experienceString);
      fontRenderer.drawString(matrixStack, experienceString, (float)(this.background.getWidth() - stringWidth), 0, 0xFF808080);
    }
  }
}
