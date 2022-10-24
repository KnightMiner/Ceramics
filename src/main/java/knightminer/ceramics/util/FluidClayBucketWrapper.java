package knightminer.ceramics.util;

import knightminer.ceramics.items.BaseClayBucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nonnull;

/**
 * Wrapper around a clay bucket to get the fluid stored
 */
public class FluidClayBucketWrapper extends FluidBucketWrapper {
  public FluidClayBucketWrapper(ItemStack container) {
    super(container);
  }

  @Nonnull
  @Override
  public FluidStack getFluid() {
    Item item = container.getItem();
    // using base so it works with milk as well
    if (item instanceof BaseClayBucketItem fluidBucket) {
      return new FluidStack(fluidBucket.getFluid(container), FluidAttributes.BUCKET_VOLUME);
    }
    return FluidStack.EMPTY;
  }

  @Override
  protected void setFluid(FluidStack stack) {
    if(stack.isEmpty()) {
      container = container.getContainerItem();
    } else {
      Item item = container.getItem();
      if (item instanceof BaseClayBucketItem bucket) {
        container = BaseClayBucketItem.withFluid(stack.getFluid(), bucket.isCracked());
      } else {
        container = ItemStack.EMPTY;
      }
    }
  }
}
