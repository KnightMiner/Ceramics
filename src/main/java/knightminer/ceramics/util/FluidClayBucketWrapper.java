package knightminer.ceramics.util;

import knightminer.ceramics.items.BaseClayBucketItem;
import knightminer.ceramics.items.FluidClayBucketItem;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

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
    // TODO: split empty capability from full?
    if (item instanceof FluidClayBucketItem) {
      FluidStack fluid = new FluidStack(FluidClayBucketItem.getFluid(container), FluidType.BUCKET_VOLUME);
      fluid.applyComponents(container.getComponentsPatch().forget(BaseClayBucketItem.FORGET));
      return fluid;
    }
    return FluidStack.EMPTY;
  }

  @Override
  protected void setFluid(FluidStack stack) {
    if (stack.isEmpty()) {
      container = container.getCraftingRemainingItem();
    } else {
      Item item = container.getItem();
      if (item instanceof BaseClayBucketItem bucket) {
        container = BaseClayBucketItem.withFluid(stack.getFluid(), bucket.isCracked(), stack.getComponentsPatch());
      } else {
        container = ItemStack.EMPTY;
      }
    }
  }


  // blacklist

  @Override
  public boolean isFluidValid(int tank, FluidStack fluid) {
    return !fluid.getFluid().is(CeramicsTags.Fluids.BUCKET_BLACKLIST);
  }

  @Override
  public boolean canFillFluidType(FluidStack fluid) {
    return !fluid.getFluid().is(CeramicsTags.Fluids.BUCKET_BLACKLIST) && super.canFillFluidType(fluid);
  }
}
