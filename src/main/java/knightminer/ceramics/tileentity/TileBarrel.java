package knightminer.ceramics.tileentity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import knightminer.ceramics.blocks.BlockBarrel;
import knightminer.ceramics.library.tank.BarrelTank;
import knightminer.ceramics.library.tank.IFastMarkDirty;
import knightminer.ceramics.library.tank.IFluidUpdateReciever;
import knightminer.ceramics.network.BarrelSizeChangedPacket;
import knightminer.ceramics.network.CeramicsNetwork;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileBarrel extends TileBarrelBase implements IFluidUpdateReciever, IFastMarkDirty {

	private static final int BASE_CAPACITY = Fluid.BUCKET_VOLUME * 4;

	public int height;
	private BarrelTank tank;
	/** Capacity per block of the barrel */
	private int baseCapacity;
	/** Cached capacity for the barrel */
	private int capacity;
	private int lastStrength;

	public TileBarrel() {
		this(BASE_CAPACITY);
	}

	public TileBarrel(int baseCapacity) {
		this.tank = new BarrelTank(baseCapacity, this);
		this.baseCapacity = this.capacity = baseCapacity;
		this.lastStrength = -1;
		this.height = 0;
	}

	/* Fluid interactions */
	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Nonnull
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank);
		}
		return super.getCapability(capability, facing);
	}

	public BarrelTank getTank() {
		return tank;
	}

	/* extended barrels */
	@Override
	public void checkBarrelStructure() {
		if(world.isRemote) {
			// let the server handle all the structure checking, the client only runs it sometimes anyways
			return;
		}

		Block teBlock = this.getBlockType();
		if(!(teBlock instanceof BlockBarrel)) {
			return; // safety check, not sure if error handing is needed
		}
		BlockBarrel barrel = (BlockBarrel) teBlock;

		BlockPos topPos = this.pos.up();
		while(barrel.isValidExtension(world.getBlockState(topPos))) {
			// set the master in the TE
			TileEntity te = world.getTileEntity(topPos);
			if(te instanceof TileBarrelExtension) {
				((TileBarrelExtension)te).setMaster(this.pos);
			}

			// data for next iteration
			topPos = topPos.up();
		}

		// this position failed, so go back down to one that worked
		this.height = topPos.down().getY() - this.pos.getY();
		int newCapacity = baseCapacity * (height + 1);
		if(newCapacity != capacity) {
			this.capacity = newCapacity;
			tank.setCapacity(newCapacity);
			onTankContentsChanged();

			// send the update to the client
			CeramicsNetwork.sendToAllAround(world, pos, new BarrelSizeChangedPacket(pos, capacity, height));
		}
	}


	/**
	 * @return The current comparator strength based on the tank's capicity
	 */
	public int comparatorStrength() {
		return 15 * tank.getFluidAmount() / tank.getCapacity();
	}

	// called by the tank when contents change
	public void onTankContentsChanged() {
		int newStrength = this.comparatorStrength();

		// if we passed to another strength
		if(newStrength != lastStrength) {
			this.lastStrength = newStrength;
			// send block update so the comparators update
			this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
		}
	}

	/* Rendering */
	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		int y = pos.getY();
		y += height + 1;

		return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, y, pos.getZ() + 1);
	}

	@Override
	public void updateFluidTo(FluidStack fluid) {
		int oldAmount = tank.getFluidAmount();
		tank.setFluid(fluid);

		tank.renderOffset += tank.getFluidAmount() - oldAmount;
	}

	@SideOnly(Side.CLIENT)
	public void updateSize(int capacity, int height) {
		this.capacity = capacity;
		tank.setCapacity(capacity);
		this.height = height;
	}

	/* NBT */
	public static final String TAG_TANK = "tank";
	public static final String TAG_HEIGHT = "height";
	@Deprecated
	public static final String TAG_TOP = "topPos";
	@Deprecated
	public static final String TAG_CAPACITY = "capacity";
	public static final String TAG_BASE_CAPACITY = "baseCapacity";

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tags) {
		tags = super.writeToNBT(tags);

		tags.setTag(TAG_TANK, tank.writeToNBT(new NBTTagCompound()));

		tags.setInteger(TAG_HEIGHT, height);
		tags.setInteger(TAG_BASE_CAPACITY, baseCapacity);

		return tags;
	}

	@Override
	public void readFromNBT(NBTTagCompound tags) {
		super.readFromNBT(tags);

		NBTTagCompound tankTag = tags.getCompoundTag(TAG_TANK);
		if(tankTag != null) {
			tank.readFromNBT(tankTag);
		}

		if(tags.hasKey(TAG_HEIGHT)) {
			height = tags.getInteger(TAG_HEIGHT);
		} else {
			// transfer old tag to new one
			NBTTagCompound top = tags.getCompoundTag(TAG_TOP);
			if(top != null && top.hasKey("y")) {
				int y = top.getInteger("y");
				this.height = y - this.pos.getY();
			}
		}

		if(tags.hasKey(TAG_BASE_CAPACITY)) {
			baseCapacity = tags.getInteger(TAG_BASE_CAPACITY);
		}

		// calculate the capacity from the base and the height
		capacity = baseCapacity * (height + 1);
		tank.setCapacity(capacity);
	}
}
