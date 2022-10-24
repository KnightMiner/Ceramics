package knightminer.ceramics.blocks.entity;

import knightminer.ceramics.Registration;
import knightminer.ceramics.menu.KilnMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class KilnBlockEntity extends AbstractFurnaceBlockEntity {
  public KilnBlockEntity(BlockPos pos, BlockState state) {
    super(Registration.KILN_BLOCK_ENTITY.get(), pos, state, Registration.KILN_RECIPE.get());
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
    return new KilnMenu(id, player, this, this.dataAccess);
  }
}
