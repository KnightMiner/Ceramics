package knightminer.ceramics.client.screen;

import knightminer.ceramics.menu.KilnMenu;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class KilnScreen extends AbstractFurnaceScreen<KilnMenu> {
  private static final ResourceLocation FURNACE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/furnace.png");

  public KilnScreen(KilnMenu kiln, Inventory inventory, Component name) {
    super(kiln, new KilnRecipeBookComponent(), inventory, name, FURNACE_GUI_TEXTURES);
  }
}