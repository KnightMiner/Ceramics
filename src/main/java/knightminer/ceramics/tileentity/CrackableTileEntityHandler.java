package knightminer.ceramics.tileentity;

import knightminer.ceramics.items.BaseClayBucketItem;
import knightminer.ceramics.items.CrackableBlockItem;
import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.network.CrackableCrackPacket;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.client.model.data.SinglePropertyData;
import slimeknights.mantle.tileentity.MantleTileEntity;
import slimeknights.mantle.util.TileEntityHelper;

/** Common logic for all crackable fluid blocks */
public class CrackableTileEntityHandler {
	/** Shared model property for the different cracked blocks */
	public static final ModelProperty<Integer> PROPERTY = new ModelProperty<>(i -> i >= 0 && i <= 5);
	/** Tag for NBT */
	public static final String TAG_CRACKS = "cracks";

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

	/* Blocks */

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
					World world = parent.getLevel();
					if (world != null) {
						world.destroyBlock(parent.getBlockPos(), false);
						// if we have at least a bucket of fluid, place in world if possible
						if (relevantAmount > FluidAttributes.BUCKET_VOLUME) {
							BlockState state = fluid.defaultFluidState().createLegacyBlock();
							if (state.getBlock() != Blocks.AIR) {
								world.setBlockAndUpdate(parent.getBlockPos(), state);
							}
							// if less, try to place a flowing fluid
						} else if (fluid instanceof FlowingFluid) {
							int level = Math.max(1, relevantAmount * 8 / FluidAttributes.BUCKET_VOLUME);
							BlockState state = ((FlowingFluid) fluid).getFlowing(level, false).createLegacyBlock();
							world.setBlockAndUpdate(parent.getBlockPos(), state);
						}
					}
				} else {
					// just increase by 1
					setCracks(cracks + 1);
				}
			}
		}
	}

	/**
	 * Internal method to safely set cracks, updating relevant properties
	 * @param cracks  New cracks value
	 * @return  True if something changed
	 */
	private boolean setCracksRaw(int cracks) {
		if (cracks < 0) cracks = 0;
		if (cracks > 5) cracks = 5;
		if (cracks != this.cracks) {
			this.cracks = cracks;
			this.data.setData(PROPERTY, cracks);
			this.parent.requestModelDataUpdate();
			return true;
		}
		return false;
	}

	/** Updates the cracks state of this block */
	public void setCracks(int cracks) {
		if (active && setCracksRaw(cracks)) {
			this.parent.markDirtyFast();

			// if client, refresh block, if server sync to client
			World world = parent.getLevel();
			if (world != null) {
				BlockPos pos = parent.getBlockPos();
				if (world.isClientSide()) {
					BlockState state = parent.getBlockState();
					world.sendBlockUpdated(pos, state, state, 3);
				} else {
					CeramicsNetwork.getInstance().sendToClientsAround(new CrackableCrackPacket(pos, this.cracks), world, pos);
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

	/* Items */

	/**
	 * Sets the cracked state of the block
	 * @param stack  Stack to crack
	 */
	public void setItemNBT(ItemStack stack) {
		if (active && cracks > 0) {
			stack.getOrCreateTag().putInt(TAG_CRACKS, cracks);
		}
	}

	/**
	 * Sets the cracks state from the item stack
	 * @param stack  Stack
	 */
	public void setCracks(ItemStack stack) {
		if (active) {
			setCracksRaw(CrackableBlockItem.getCracks(stack));
		}
	}


	/* NBT */

	/**
	 * Reads cracks data from NBT
	 * @param nbt  NBT
	 */
	public void readNBT(BlockState state, CompoundNBT nbt) {
		Block block = state.getBlock();
		if (block instanceof ICrackableBlock && ((ICrackableBlock)block).isCrackable()) {
			this.active = true;
			setCracksRaw(nbt.getInt(TAG_CRACKS));
		} else {
			this.active = false;
			this.cracks = 0;
		}
	}

	/**
	 * Writes cracks data to NBT
	 * @param nbt  NBT
	 */
	public void writeNBT(CompoundNBT nbt) {
		if (active && cracks > 0) {
			nbt.putInt(TAG_CRACKS, cracks);
		}
	}

	/** Interface to allow blocks to opt out of cracking behavior on some variants */
	public interface ICrackableBlock {
		/** Determines if this version of the block is crackable */
		boolean isCrackable();

		/** Helper to avoid having to write this line multiple times */
		static void onBlockPlacedBy(IWorld world, BlockPos pos, ItemStack stack) {
			TileEntityHelper.getTile(ICrackableTileEntity.class, world, pos).ifPresent(te -> te.getCracksHandler().setCracks(stack));
		}

		/**
		 * Attempts to repair a crackable block
		 * @param world    World instance
		 * @param pos      Position of crackable block
		 * @param player   Player interacting
		 * @param hand     Hand used
		 * @return  True if repaired, false for wrong repair item or no cracks
		 */
		static boolean tryRepair(IWorld world, BlockPos pos, PlayerEntity player, Hand hand) {
			ItemStack held = player.getItemInHand(hand);
			if (held.getItem().is(CeramicsTags.Items.TERRACOTTA_CRACK_REPAIR)) {
				return TileEntityHelper.getTile(ICrackableTileEntity.class, world, pos).filter(te -> {
					CrackableTileEntityHandler handler = te.getCracksHandler();
					int cracks = handler.getCracks();
					if (handler.isActive() && cracks > 0) {
						// play sound
						world.playSound(player, pos, SoundType.GRAVEL.getPlaceSound(), SoundCategory.BLOCKS, 1, 1);

						if (!world.isClientSide()) {
							// repair halfway
							handler.setCracks(Math.max(0, cracks - 3));
							// take clay
							if (!player.isCreative()) {
								held.shrink(1);
								player.setItemInHand(hand, held);
							}
						}

						return true;
					}
					return false;
				}).isPresent();
			}
			return false;
		}
	}

	/** Interface to make syncing easier */
	public interface ICrackableTileEntity {
		/** Gets the cracks handler for this TE */
		CrackableTileEntityHandler getCracksHandler();
	}
}
