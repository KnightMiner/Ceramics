package knightminer.ceramics.library.tank;

import knightminer.ceramics.tileentity.TileChannel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class ChannelTank extends TileTank<TileChannel> {

	private static final String TAG_LOCKED = "locked";
	/**
	 * Amount of fluid that may not be extracted this tick
	 * Essentially, since we cannot guarantee tick order, this prevents us from having a net 0 fluid for the renderer
	 * if draining and filling at the same time
	 */
	private int locked;
	public ChannelTank(int capacity, TileChannel parent) {
		super(capacity, parent);
		this.setCanDrain(false);
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
	public int maxOutput() {
		if(fluid == null) {
			return 0;
		}

		return fluid.amount - locked;
	}

	@Override
	public FluidStack drainInternal(int maxDrain, boolean doDrain) {
		int drained = maxDrain;
		int allowed = maxOutput();
		if (allowed < drained) {
			drained = allowed;
		}

		return super.drainInternal(drained, doDrain);
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		int amount = super.fill(resource, doFill);
		locked += amount;
		return amount;
	}

	@Override
	public FluidTank readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.locked = nbt.getInteger(TAG_LOCKED);

		return this;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		nbt.setInteger(TAG_LOCKED, locked);

		return nbt;
	}
}
