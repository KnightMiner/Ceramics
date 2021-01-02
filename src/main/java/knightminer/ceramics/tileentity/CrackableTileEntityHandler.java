package knightminer.ceramics.tileentity;

import knightminer.ceramics.items.BaseClayBucketItem;
import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.network.CrackableCrackPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.client.model.data.SinglePropertyData;
import slimeknights.mantle.tileentity.MantleTileEntity;

/** Common logic for all crackable fluid blocks */
public class CrackableTileEntityHandler {
	/** Shared model property for the different cracked blocks */
	public static final ModelProperty<Integer> PROPERTY = new ModelProperty<>(i -> i >= 0 && i <= 5);
	/** Tag for NBT */
	private static final String TAG_CRACKS = "cracks";

	/** Parent tile entity */
	private final MantleTileEntity parent;
	/** Model data for the client */
	private final IModelData data = new SinglePropertyData<>(PROPERTY, 0);
	/** Current cracks value. Once it reaches 6 the block breaks */
	private int cracks = 0;
	/** Whether to use crackable logic, used to allow one class to handle both cases */
	private boolean active;

	public CrackableTileEntityHandler(MantleTileEntity parent, boolean active) {
		this.parent = parent;
		this.active = active;
	}

	/** Checks if this handler is active */
	public boolean isActive() {
		return active;
	}

	/**
	 * Called when a new fluid is added to set the first crack stage if hot
	 * @param fluid  Fluid added
	 */
	public void fluidAdded(FluidStack fluid) {
		if (active && cracks == 0 && BaseClayBucketItem.doesCrack(fluid.getFluid())) {
			setCracks(1);
		}
	}

	/**
	 * Increases the cracks, helper for passing a fluid stack directly
	 * @param fluid  Fluid in block
	 */
	public void updateCracks(FluidStack fluid) {
		updateCracks(fluid.getFluid(), fluid.getAmount());
	}

	/**
	 * Increases or decreases the cracks based on the fluid temperature
	 * @param fluid  Fluid in block
	 */
	public void updateCracks(Fluid fluid, int relevantAmount) {
		if (active) {
			if (BaseClayBucketItem.doesCrack(fluid)) {
				// after 5, break the block
				if (cracks >= 5) {
					World world = parent.getWorld();
					if (world != null) {
						world.destroyBlock(parent.getPos(), false);
						// if we have at least a bucket of fluid, place in world if possible
						if (relevantAmount > FluidAttributes.BUCKET_VOLUME) {
							BlockState state = fluid.getDefaultState().getBlockState();
							if (state.getBlock() != Blocks.AIR) {
								world.setBlockState(parent.getPos(), state);
							}
							// if less, try to place a flowing fluid
						} else if (fluid instanceof FlowingFluid) {
							int level = Math.max(1, relevantAmount * 8 / FluidAttributes.BUCKET_VOLUME);
							BlockState state = ((FlowingFluid) fluid).getFlowingFluidState(level, false).getBlockState();
							world.setBlockState(parent.getPos(), state);
						}
					}
				} else {
					// just increase by 1
					setCracks(cracks + 1);
				}
			} else if (cracks > 0) {
				setCracks(cracks - 1);
			}
		}
	}

	/** Updates the cracks state of this block */
	public void setCracks(int cracks) {
		if (active && this.cracks != cracks) {
			this.cracks = cracks;
			this.data.setData(PROPERTY, cracks);
			this.parent.requestModelDataUpdate();
			this.parent.markDirtyFast();

			// if client, refresh block, if server sync to client
			World world = parent.getWorld();
			if (world != null) {
				BlockPos pos = parent.getPos();
				if (world.isRemote()) {
					BlockState state = parent.getBlockState();
					world.notifyBlockUpdate(pos, state, state, 3);
				} else {
					CeramicsNetwork.getInstance().sendToClientsAround(new CrackableCrackPacket(pos, cracks), world, pos);
				}
			}
		}
	}

	/** Gets the current cracks value of this block */
	public int getCracks() {
		return cracks;
	}

	/** Gets the model data for this TE */
	public IModelData getModelData() {
		return data;
	}

	/**
	 * Reads cracks data from NBT
	 * @param nbt  NBT
	 */
	public void readNBT(BlockState state, CompoundNBT nbt) {
		Block block = state.getBlock();
		if (block instanceof ICrackableBlock && ((ICrackableBlock)block).isCrackable()) {
			this.active = true;
			this.cracks = nbt.getInt(TAG_CRACKS);
		} else {
			this.active = false;
			this.cracks = 0;
		}
		data.setData(PROPERTY, cracks);
		parent.requestModelDataUpdate();
	}

	/**
	 * Writes cracks data to NBT
	 * @param nbt  NBT
	 */
	public void writeNBT(CompoundNBT nbt) {
		if (active) {
			nbt.putInt(TAG_CRACKS, cracks);
		}
	}

	/** Interface to allow blocks to opt out of cracking behavior on some variants */
	public interface ICrackableBlock {
		/** Determines if this version of the block is crackable */
		boolean isCrackable();
	}

	/** Interface to make syncing easier */
	public interface ICrackableTileEntity {
		/** Gets the cracks handler for this TE */
		CrackableTileEntityHandler getCracksHandler();
	}
}
