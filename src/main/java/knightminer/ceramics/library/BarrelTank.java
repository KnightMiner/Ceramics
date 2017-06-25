package knightminer.ceramics.library;

import knightminer.ceramics.network.BarrelCapacityChangedPacket;
import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.network.FluidUpdatePacket;
import knightminer.ceramics.tileentity.TileBarrel;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class BarrelTank extends FluidTank {

	private TileBarrel parent;
	public int renderOffset;

	public BarrelTank(int capacity, TileBarrel parent) {
		super(capacity);
		this.parent = parent;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		int amount = super.fill(resource, doFill);
		if(amount > 0 && doFill) {
			sendUpdate(amount);
		}
		return amount;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		FluidStack fluid = super.drain(resource, doDrain);
		if(fluid != null && doDrain) {
			sendUpdate(-fluid.amount);
		}
		return fluid;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		FluidStack fluid = super.drain(maxDrain, doDrain);
		if(fluid != null && doDrain) {
			sendUpdate(-fluid.amount);
		}
		return fluid;
	}

	protected void sendUpdate(int amount) {
		if(amount != 0) {
			renderOffset += amount;
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
		renderOffset = 0; // don't render it lowering from a barrel above breaking, that looks dumb

		World world = parent.getWorld();
		if(world != null && !world.isRemote) {
			CeramicsNetwork.sendToAllAround(world, parent.getPos(), new BarrelCapacityChangedPacket(parent.getPos(), capacity));
		}
	}

	@Override
	public void onContentsChanged() {
		parent.onTankContentsChanged();
	}
}
