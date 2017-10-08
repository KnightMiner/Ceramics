package knightminer.ceramics.library.tank;

import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.network.FluidUpdatePacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class TileTank<T extends TileEntity> extends FluidTank {

	protected T parent;

	public TileTank(int capacity, T parent) {
		super(capacity);
		this.parent = parent;
	}

	@Override
	public int fillInternal(FluidStack resource, boolean doFill) {
		int amount = super.fillInternal(resource, doFill);
		if(amount > 0 && doFill) {
			sendUpdate(amount);
		}
		return amount;
	}

	@Override
	public FluidStack drainInternal(int maxDrain, boolean doDrain) {
		FluidStack fluid = super.drainInternal(maxDrain, doDrain);
		if(fluid != null && doDrain) {
			sendUpdate(-fluid.amount);
		}
		return fluid;
	}

	protected void sendUpdate(int amount) {
		if(amount != 0) {
			World world = parent.getWorld();
			if(!world.isRemote) {
				CeramicsNetwork.sendToAllAround(world, parent.getPos(), new FluidUpdatePacket(parent.getPos(), this.getFluid()));
			}
		}
	}

	@Override
	public void setCapacity(int capacity) {
		this.capacity = capacity;

		// reduce the fluid size if its over the new capacity
		if(this.fluid != null && this.fluid.amount > capacity) {
			this.drain(this.fluid.amount - capacity, true);
		}
	}
}
