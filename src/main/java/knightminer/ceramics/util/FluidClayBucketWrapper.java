package knightminer.ceramics.util;

import knightminer.ceramics.items.BaseClayBucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

/**
 * Wrapper around a clay bucket to get the fluid stored
 */
public class FluidClayBucketWrapper extends FluidBucketWrapper {
  public FluidClayBucketWrapper(ItemStack container) {
    super(container);
  }

  @Override
  public FluidStack getFluid() {
    Item item = container.getItem();
    if (item instanceof BaseClayBucketItem) {
      return new FluidStack(((BaseClayBucketItem)item).getFluid(container), FluidAttributes.BUCKET_VOLUME);
    }
    return FluidStack.EMPTY;
  }

  @Override
  protected void setFluid(FluidStack stack) {
    if(stack.isEmpty()) {
      container = container.getContainerItem();
    } else {
      Item item = container.getItem();
      if (item instanceof BaseClayBucketItem) {
        container = ((BaseClayBucketItem)item).withFluid(stack.getFluid());
      } else {
        container = ItemStack.EMPTY;
      }
    }
  }
}
