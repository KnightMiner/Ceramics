package knightminer.ceramics.tileentity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.blocks.BlockBarrel;
import knightminer.ceramics.library.BarrelTank;
import net.minecraft.block.state.IBlockState;
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

public class TileBarrel extends TileBarrelBase {

	public static final int BASE_CAPACITY = Fluid.BUCKET_VOLUME * 4;

	public BlockPos topPos;
	private BarrelTank tank;
	private int capacity;
	private int lastStrength;

	public TileBarrel() {
		this.tank = new BarrelTank(BASE_CAPACITY, this);
		this.capacity = BASE_CAPACITY;
		this.lastStrength = -1;
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
		BlockPos topPos = this.pos.up();
		IBlockState state = world.getBlockState(topPos);
		while(state.getBlock() instanceof BlockBarrel && ((BlockBarrel) state.getBlock()).isExtension(state)) {

			// set the master in the TE
			TileEntity te = world.getTileEntity(topPos);
			if(te instanceof TileBarrelExtension) {
				((TileBarrelExtension)te).setMaster(this.pos);
			}

			// data for next iteration
			topPos = topPos.up();
			state = world.getBlockState(topPos);
		}

		// this position failed, so go back down to one that worked
		this.topPos = topPos.down();
		int newCapacity = BASE_CAPACITY * (this.topPos.getY() + 1 - this.pos.getY());
		if(newCapacity != capacity) {
			this.capacity = newCapacity;
			tank.setCapacity(newCapacity);
			onTankContentsChanged();
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
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		int y = pos.getY();
		if(topPos != null) {
			y = topPos.getY();
		}
		y += 1;

		return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, y, pos.getZ() + 1);
	}

	@SideOnly(Side.CLIENT)
	public void updateFluidTo(FluidStack fluid) {
		int oldAmount = tank.getFluidAmount();
		tank.setFluid(fluid);

		tank.renderOffset += tank.getFluidAmount() - oldAmount;
	}

	@SideOnly(Side.CLIENT)
	public void updateCapacityTo(int capacity2) {
		tank.setCapacity(capacity);
	}

	/* NBT */
	public static final String TAG_TANK = "tank";
	public static final String TAG_TOP = "topPos";
	public static final String TAG_CAPACITY = "capacity";

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tags) {
		tags = super.writeToNBT(tags);

		tags.setTag(TAG_TANK, tank.writeToNBT(new NBTTagCompound()));

		if(topPos != null) {
			NBTTagCompound top = new NBTTagCompound();
			top.setInteger("x", topPos.getX());
			top.setInteger("y", topPos.getY());
			top.setInteger("z", topPos.getZ());

			tags.setTag(TAG_TOP, top);
		}

		tags.setInteger(TAG_CAPACITY, capacity);

		return tags;
	}

	@Override
	public void readFromNBT(NBTTagCompound tags) {
		super.readFromNBT(tags);

		NBTTagCompound tankTag = tags.getCompoundTag(TAG_TANK);
		if(tankTag != null) {
			tank.readFromNBT(tankTag);
		}
		else {
			Ceramics.log.info("No tag");
		}

		NBTTagCompound top = tags.getCompoundTag(TAG_TOP);
		if(top != null && top.hasKey("x") && top.hasKey("y") && top.hasKey("z")) {
			this.topPos = new BlockPos(top.getInteger("x"), top.getInteger("y"), top.getInteger("z"));
		}

		if(tags.hasKey(TAG_CAPACITY)) {
			capacity = tags.getInteger(TAG_CAPACITY);
			tank.setCapacity(capacity);
		}
	}
}
