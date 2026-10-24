package knightminer.ceramics.menu;

import knightminer.ceramics.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookType;

public class KilnMenu extends AbstractFurnaceMenu {
  public static final RecipeBookType KILN_RECIPE_BOOK = RecipeBookType.valueOf("CERAMICS_KILN");

  @SuppressWarnings("unused")
  public KilnMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
    super(Registration.KILN_MENU.get(), Registration.KILN_RECIPE.get(), KILN_RECIPE_BOOK, id, inventory);
  }

  public KilnMenu(int id, Inventory inventory, Container tileEntity, ContainerData furnaceData) {
    super(Registration.KILN_MENU.get(), Registration.KILN_RECIPE.get(), KILN_RECIPE_BOOK, id, inventory, tileEntity, furnaceData);
  }
}
