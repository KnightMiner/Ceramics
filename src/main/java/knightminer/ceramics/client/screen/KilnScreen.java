package knightminer.ceramics.client.screen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.menu.KilnMenu;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class KilnScreen extends AbstractFurnaceScreen<KilnMenu> {
  private static final ResourceLocation LIT_PROGRESS_SPRITE = Ceramics.getResource("kiln/lit_progress");
  private static final ResourceLocation BURN_PROGRESS_SPRITE = Ceramics.getResource("kiln/burn_progress");
  private static final ResourceLocation BACKGROUND = Ceramics.getResource("textures/gui/kiln.png");

  public KilnScreen(KilnMenu kiln, Inventory inventory, Component name) {
    super(kiln, new KilnRecipeBookComponent(), inventory, name, BACKGROUND, LIT_PROGRESS_SPRITE, BURN_PROGRESS_SPRITE);
  }
}