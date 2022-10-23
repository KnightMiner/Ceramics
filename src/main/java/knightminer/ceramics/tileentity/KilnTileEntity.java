package knightminer.ceramics.tileentity;

import knightminer.ceramics.Registration;
import knightminer.ceramics.container.KilnContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class KilnTileEntity extends AbstractFurnaceBlockEntity {
  public KilnTileEntity(BlockPos pos, BlockState state) {
    super(Registration.KILN_TILE_ENTITY.get(), pos, state, Registration.KILN_RECIPE.get());
  }

  @Override
  protected Component getDefaultName() {
    return new TranslatableComponent("container.ceramics.kiln");
  }

  @Override
  protected int getBurnDuration(ItemStack fuel) {
    return super.getBurnDuration(fuel) / 2;
  }
  
  @Override
  protected AbstractContainerMenu createMenu(int id, Inventory player) {
    return new KilnContainer(id, player, this, this.dataAccess);
  }
}
