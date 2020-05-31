package knightminer.ceramics.container;

import com.google.common.collect.Lists;
import knightminer.ceramics.Registration;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.AbstractFurnaceContainer;
import net.minecraft.util.IIntArray;

import java.util.List;

public class KilnContainer extends AbstractFurnaceContainer {
  public KilnContainer(int id, PlayerInventory inventory) {
    // TODO: recipe type
    super(Registration.KILN_CONTAINER.get(), Registration.KILN_RECIPE, id, inventory);
  }

  public KilnContainer(int id, PlayerInventory inventory, IInventory tileEntity, IIntArray furnaceData) {
    super(Registration.KILN_CONTAINER.get(), Registration.KILN_RECIPE, id, inventory, tileEntity, furnaceData);
  }

  @Override
  public List<RecipeBookCategories> getRecipeBookCategories() {
    return Lists.newArrayList(RecipeBookCategories.FURNACE_SEARCH, RecipeBookCategories.FURNACE_BLOCKS, RecipeBookCategories.FURNACE_MISC);
  }
}
