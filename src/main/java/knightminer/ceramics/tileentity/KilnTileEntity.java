package knightminer.ceramics.tileentity;

import knightminer.ceramics.Registration;
import knightminer.ceramics.container.KilnContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class KilnTileEntity extends AbstractFurnaceTileEntity {
  public KilnTileEntity() {
    super(Registration.KILN_TILE_ENTITY.get(), Registration.KILN_RECIPE);
  }

  @Override
  protected ITextComponent getDefaultName() {
    return new TranslationTextComponent("container.ceramics.kiln");
  }

  @Override
  protected int getBurnTime(ItemStack fuel) {
    return super.getBurnTime(fuel) / 2;
  }
  
  @Override
  protected Container createMenu(int id, PlayerInventory player) {
    return new KilnContainer(id, player, this, this.furnaceData);
  }
}
