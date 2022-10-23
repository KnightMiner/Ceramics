package knightminer.ceramics.client.gui;

import knightminer.ceramics.container.KilnContainer;
import net.minecraft.client.gui.recipebook.FurnaceRecipeGui;
import net.minecraft.client.gui.screen.inventory.AbstractFurnaceScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class KilnScreen extends AbstractFurnaceScreen<KilnContainer> {
  private static final ResourceLocation FURNACE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/furnace.png");

  public KilnScreen(KilnContainer kiln, PlayerInventory inventory, ITextComponent name) {
    super(kiln, new FurnaceRecipeGui(), inventory, name, FURNACE_GUI_TEXTURES);
  }

  @Override
  public void init() {
    super.init();
    // remove the last button (the recipe book button) as I cannot yet filter that properly
    this.buttons.remove(this.buttons.size()-1);
    this.children.remove(this.children.size()-1);
    if (this.recipeBookComponent.isVisible()) {
      this.recipeBookComponent.toggleVisibility();
    }
  }
}