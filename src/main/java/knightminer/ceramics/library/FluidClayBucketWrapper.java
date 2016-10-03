package knightminer.ceramics.library;

import javax.annotation.Nullable;

import knightminer.ceramics.Ceramics;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
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
	protected void setFluid(Fluid fluid) {
		if(fluid == null) {
			Ceramics.clayBucket.drain(container, 1000, true);

		}
		else if(FluidRegistry.getBucketFluids().contains(fluid) || fluid == FluidRegistry.LAVA
				|| fluid == FluidRegistry.WATER || fluid.getName().equals("milk")) {
			Ceramics.clayBucket.fill(container, new FluidStack(fluid, Fluid.BUCKET_VOLUME), true);
		}
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (container.stackSize != 1 || resource == null || resource.amount < Fluid.BUCKET_VOLUME || Ceramics.clayBucket.hasFluid(container) || !canFillFluidType(resource)) {
			return 0;
		}

		if (doFill) {
			setFluid(resource.getFluid());
		}

		return Fluid.BUCKET_VOLUME;
	}
}
