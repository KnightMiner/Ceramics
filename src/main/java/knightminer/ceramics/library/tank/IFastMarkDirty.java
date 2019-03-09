package knightminer.ceramics.library.tank;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface IFastMarkDirty {
	/** Gets the current object as a TileEntity */
	default TileEntity getTE() {
		return (TileEntity)this;
	}

	/**
	 * Same as markDirty, but only marks the chunk dirty, skips metadata and comparator updates
	 */
	default void markDirtyFast() {
		TileEntity te = getTE();
		World world = te.getWorld();
		if (world != null) {
			world.markChunkDirty(te.getPos(), te);
		}
	}
}