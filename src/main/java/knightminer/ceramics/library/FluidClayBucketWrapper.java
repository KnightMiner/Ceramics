package knightminer.ceramics.library;

import javax.annotation.Nullable;

import knightminer.ceramics.Ceramics;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class FluidClayBucketWrapper extends FluidBucketWrapper {

	public FluidClayBucketWrapper(ItemStack container) {
		super(container);
	}

	@Override
	@Nullable
	public FluidStack getFluid() {
		return Ceramics.clayBucket.getFluid(container);
	}

	@Override
	protected void setFluid(FluidStack stack) {
		if(stack == null) {
			// if the current fluid breaks, return empty
			if (Ceramics.clayBucket.doesBreak(container)) {
				container = ItemStack.EMPTY;
			} else {
				container = new ItemStack(Ceramics.clayBucket);
			}
		} else {
			container = Ceramics.clayBucket.withFluid(stack.getFluid());
		}
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (container.getCount() != 1 || resource == null || resource.amount < Fluid.BUCKET_VOLUME || Ceramics.clayBucket.hasFluid(container) || !canFillFluidType(resource)) {
			return 0;
		}

		if (doFill) {
			setFluid(resource);
		}

		return Fluid.BUCKET_VOLUME;
	}

	@Override
	public boolean canFillFluidType(FluidStack fluid) {
		if(!Config.bucketHotFluids && Ceramics.clayBucket.doesBreak(fluid)) {
			return false;
		}
		return super.canFillFluidType(fluid);
	}
}
