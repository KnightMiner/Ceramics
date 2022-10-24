package knightminer.ceramics.client.screen;

import knightminer.ceramics.menu.KilnMenu;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.SmeltingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class KilnScreen extends AbstractFurnaceScreen<KilnMenu> {
  private static final ResourceLocation FURNACE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/furnace.png");

  public KilnScreen(KilnMenu kiln, Inventory inventory, Component name) {
    super(kiln, new SmeltingRecipeBookComponent(), inventory, name, FURNACE_GUI_TEXTURES);
  }

  @Override
  public void init() {
    super.init();
    // remove the last button (the recipe book button) as I cannot yet filter that properly
    // TODO: pretty sure Forge lets me do this now
    this.renderables.remove(this.renderables.size()-1);
//    this.children.remove(this.children.size()-1);
    if (this.recipeBookComponent.isVisible()) {
      this.recipeBookComponent.toggleVisibility();
    }
  }
}