package knightminer.ceramics.util;

import knightminer.ceramics.items.ClayBucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nullable;

public class FluidClayBucketWrapper extends FluidBucketWrapper {

  public FluidClayBucketWrapper(ItemStack container) {
    super(container);
  }

  @Override
  @Nullable
  public FluidStack getFluid() {
    Item item = container.getItem();
    if (item instanceof ClayBucketItem) {
      return new FluidStack(((ClayBucketItem)item).getFluid(container), FluidAttributes.BUCKET_VOLUME);
    }
    return FluidStack.EMPTY;
  }

  @Override
  protected void setFluid(FluidStack stack) {
    if(stack.isEmpty()) {
      container = container.getContainerItem();
    } else {
      Item item = container.getItem();
      if (item instanceof ClayBucketItem) {
        container = ((ClayBucketItem)item).withFluid(stack.getFluid());
      } else {
        container = ItemStack.EMPTY;
      }
    }
  }
}
