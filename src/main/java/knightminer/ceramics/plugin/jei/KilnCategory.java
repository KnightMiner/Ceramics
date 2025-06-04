package knightminer.ceramics.plugin.jei;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.recipe.KilnRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.client.SafeClientAccess;

import java.util.Objects;

public class KilnCategory implements IRecipeCategory<KilnRecipe> {
  static final RecipeType<KilnRecipe> TYPE = new RecipeType<>(Ceramics.getResource("kiln"), KilnRecipe.class);
  private static final ResourceLocation RECIPE_GUI_VANILLA = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
  // slots
  static final int INPUT_SLOT = 0;
  static final int FUEL_SLOT = 1;
  private static final int OUTPUT_SLOT = 2;
  // elements
  private final IDrawable background;
  private final IDrawable icon;
  private final IDrawableAnimated animatedFlame;
  private final Component name;
  private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;

  KilnCategory(IGuiHelper guiHelper) {
    // elements
    this.background = guiHelper.createDrawable(RECIPE_GUI_VANILLA, 0, 114, 82, 54);
    this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Registration.KILN));
    this.name = Ceramics.component("gui.jei.category", "kiln");
    // animations
    IDrawableStatic staticFlame = guiHelper.createDrawable(RECIPE_GUI_VANILLA, 82, 114, 14, 14);
    this.animatedFlame = guiHelper.createAnimatedDrawable(staticFlame, 300, StartDirection.TOP, true);
    this.cachedArrows = CacheBuilder.newBuilder().maximumSize(25L).build(new CacheLoader<>() {
      @Override
      public IDrawableAnimated load(Integer cookTime) {
        return guiHelper.drawableBuilder(RECIPE_GUI_VANILLA, 82, 128, 24, 17).buildAnimated(cookTime, StartDirection.LEFT, false);
      }
    });
  }

  /** Gets the arrow for the given recipe */
  protected IDrawableAnimated getArrow(KilnRecipe recipe) {
    int cookTime = recipe.getCookingTime();
    if (cookTime <= 0) {
      cookTime = 100;
    }

    return this.cachedArrows.getUnchecked(cookTime);
  }


  /* Properties */

  @Override
  public RecipeType<KilnRecipe> getRecipeType() {
    return TYPE;
  }

  @Override
  public Component getTitle() {
    return this.name;
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
  public void setRecipe(IRecipeLayoutBuilder builder, KilnRecipe recipe, IFocusGroup focuses) {
    builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.getInput());
    builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 19).addItemStack(recipe.getResultItem(Objects.requireNonNullElse(SafeClientAccess.getRegistryAccess(), RegistryAccess.EMPTY)));
  }

  @Override
  public void draw(KilnRecipe recipe, IRecipeSlotsView view, GuiGraphics graphics, double mouseX, double mouseY) {
    this.animatedFlame.draw(graphics, 1, 20);
    IDrawableAnimated arrow = this.getArrow(recipe);
    arrow.draw(graphics, 24, 18);
    this.drawExperience(recipe, graphics);
    this.drawCookTime(recipe, graphics);
  }

  /** Draws the recipe experience info */
  protected void drawExperience(KilnRecipe recipe, GuiGraphics graphics) {
    float experience = recipe.getExperience();
    if (experience > 0.0F) {
      Component experienceString = Component.translatable("gui.jei.category.smelting.experience", experience);
      Minecraft minecraft = Minecraft.getInstance();
      Font fontRenderer = minecraft.font;
      int stringWidth = fontRenderer.width(experienceString);
      graphics.drawString(fontRenderer, experienceString, (this.background.getWidth() - stringWidth), 0, 0xFF808080, false);
    }
  }

  /** Draws the recipe cook time info */
  protected void drawCookTime(KilnRecipe recipe, GuiGraphics graphics) {
    int cookTime = recipe.getCookingTime();
    if (cookTime > 0) {
      int cookTimeSeconds = cookTime / 20;
      Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
      Minecraft minecraft = Minecraft.getInstance();
      Font fontRenderer = minecraft.font;
      int stringWidth = fontRenderer.width(timeString);
      graphics.drawString(fontRenderer, timeString, (this.background.getWidth() - stringWidth), 45, 0xFF808080, false);
    }
  }
}
