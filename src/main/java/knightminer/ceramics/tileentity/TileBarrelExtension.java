package knightminer.ceramics.tileentity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.network.ExtensionMasterUpdatePacket;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileBarrelExtension extends TileBarrelBase {
	private BlockPos masterPos;

	public void setMaster(BlockPos pos) {
		// ensure it actually changed
		if (pos != masterPos || pos != null && !pos.equals(masterPos)) {
			masterPos = pos;

			// update clients
			if(!world.isRemote) {
				world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
				CeramicsNetwork.sendToAllAround(world, this.pos, new ExtensionMasterUpdatePacket(this.pos, pos));
			}
		}
	}

	public BlockPos getMasterPos() {
		return masterPos;
	}

	public TileBarrel getMaster() {
		if(masterPos == null) {
			return null;
		}

		TileEntity te = world.getTileEntity(masterPos);
		if(te instanceof TileBarrel) {
			return (TileBarrel) te;
		}

		return null;
	}

	@Override
	public void checkBarrelStructure() {
		TileBarrel master = getMaster();
		if(master != null) {
			master.checkBarrelStructure();
		}
	}

	/**
	 * Clears the master of this barrel and all above it
	 */
	public void clearMaster() {
		if (!world.isRemote && this.masterPos != null) {
			this.setMaster(null);
			TileEntity above = world.getTileEntity(pos.up());
			if (above instanceof TileBarrelExtension) {
				((TileBarrelExtension)above).clearMaster();
			}
		}
	}

	/* Fluid interactions */
	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			TileBarrel master = getMaster();
			if(master != null) {
				return master.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
			}
		}
		return super.hasCapability(capability, facing);
	}

	@Nonnull
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			TileBarrel master = getMaster();
			if(master != null) {
				return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
						master.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing));
			}
		}
		return super.getCapability(capability, facing);
	}

	/* NBT */
	public static final String TAG_MASTER = "masterPos";

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tags) {
		tags = super.writeToNBT(tags);

		if(masterPos != null) {
			NBTTagCompound top = new NBTTagCompound();
			top.setInteger("x", masterPos.getX());
			top.setInteger("y", masterPos.getY());
			top.setInteger("z", masterPos.getZ());

			tags.setTag(TAG_MASTER, top);
		}

		return tags;
	}

	@Override
	public void readFromNBT(NBTTagCompound tags) {
		super.readFromNBT(tags);

		NBTTagCompound top = tags.getCompoundTag(TAG_MASTER);
		if(top != null && top.hasKey("x") && top.hasKey("y") && top.hasKey("z")) {
			this.masterPos = new BlockPos(top.getInteger("x"), top.getInteger("y"), top.getInteger("z"));
		}
	}
}
