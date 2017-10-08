package knightminer.ceramics.library.tank;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidStack;

public interface IFluidUpdateReciever {
	public void updateFluidTo(@Nullable FluidStack fluid);
}
