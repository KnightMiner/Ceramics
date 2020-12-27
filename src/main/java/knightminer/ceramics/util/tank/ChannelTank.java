package knightminer.ceramics.util.tank;

import knightminer.ceramics.tileentity.ChannelTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/** Tank for channel contents */
public class ChannelTank extends FluidTank {
	private static final String TAG_LOCKED = "locked";

	/**
	 * Amount of fluid that may not be extracted this tick
	 * Essentially, since we cannot guarantee tick order, this prevents us from having a net 0 fluid for the renderer
	 * if draining and filling at the same time
	 */
	private int locked;

	/** Tank owner */
	private final ChannelTileEntity parent;

	public ChannelTank(int capacity, ChannelTileEntity parent) {
		super(capacity);
		this.parent = parent;
	}

	/**
	 * Called on channel update to clear the lock, allowing this fluid to be drained
	 */
	public void freeFluid() {
		this.locked = 0;
	}

	/**
	 * Returns the maximum fluid that can be extracted from this tank
	 * @return  Max fluid that can be pulled
	 */
	public int getMaxUsable() {
		return Math.max(fluid.getAmount() - locked, 0);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		int amount = super.fill(resource, action);
		if(action.execute()) {
			locked += amount;
		}
		return amount;
	}

// TODO: I think the intention was to only send update when adding or removing fluids, is there an easy way to do that?
//	@Override
//	protected void sendUpdate(int amount) {
//		if(amount != 0) {
//			// if the fluid is null, we just removed fluid
//			// if the amounts matched, that means we had 0 before
//			FluidStack fluid = this.getFluid();
//			if(fluid.isEmpty() || fluid.getAmount() == amount) {
//				super.sendUpdate(amount);
//			}
//		}
//	}

	@Override
	public FluidTank readFromNBT(CompoundNBT nbt) {
		this.locked = nbt.getInt(TAG_LOCKED);
		super.readFromNBT(nbt);
		return this;
	}

	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt = super.writeToNBT(nbt);
		nbt.putInt(TAG_LOCKED, locked);
		return nbt;
	}
}