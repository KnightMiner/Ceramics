package knightminer.ceramics.client.gui;

import knightminer.ceramics.container.KilnContainer;
import net.minecraft.client.gui.screens.recipebook.SmeltingRecipeBookComponent;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class KilnScreen extends AbstractFurnaceScreen<KilnContainer> {
  private static final ResourceLocation FURNACE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/furnace.png");

  public KilnScreen(KilnContainer kiln, Inventory inventory, Component name) {
    super(kiln, new SmeltingRecipeBookComponent(), inventory, name, FURNACE_GUI_TEXTURES);
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