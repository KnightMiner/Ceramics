package knightminer.ceramics.container;

import knightminer.ceramics.Registration;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookType;

import java.util.List;

public class KilnContainer extends AbstractFurnaceMenu {
  @SuppressWarnings("unused")
  public KilnContainer(int id, Inventory inventory, FriendlyByteBuf buffer) {
    super(Registration.KILN_CONTAINER.get(), Registration.KILN_RECIPE, RecipeBookType.FURNACE, id, inventory);
  }

  public KilnContainer(int id, Inventory inventory, Container tileEntity, ContainerData furnaceData) {
    super(Registration.KILN_CONTAINER.get(), Registration.KILN_RECIPE, RecipeBookType.FURNACE, id, inventory, tileEntity, furnaceData);
  }

  @Override
  public List<RecipeBookCategories> getRecipeBookCategories() {
    return List.of(RecipeBookCategories.FURNACE_SEARCH, RecipeBookCategories.FURNACE_BLOCKS, RecipeBookCategories.FURNACE_MISC);
  }
}
