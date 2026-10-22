package knightminer.ceramics.blocks.entity;

import knightminer.ceramics.Registration;
import knightminer.ceramics.items.BaseClayBucketItem;
import knightminer.ceramics.network.CrackableCrackPacket;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.mantle.network.PacketHelper;

/** Common logic for all crackable fluid blocks */
public class CrackableBlockEntityHandler {
	/** Shared model property for the different cracked blocks */
	public static final ModelProperty<Integer> CRACKS = new ModelProperty<>(i -> i >= 0 && i <= 5);
	/** Tag for NBT */
	public static final String TAG_CRACKS = "cracks";

	/** Parent tile entity */
	private final MantleBlockEntity parent;
	/** Current cracks value. Once it reaches 6 the block breaks */
	private int cracks = 0;
	/** Whether to use crackable logic, used to allow one class to handle both cases */
	private boolean active;

	public CrackableBlockEntityHandler(MantleBlockEntity parent, boolean active) {
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
					Level world = parent.getLevel();
					if (world != null) {
						world.destroyBlock(parent.getBlockPos(), false);
						// if we have at least a bucket of fluid, place in world if possible
						if (relevantAmount > FluidType.BUCKET_VOLUME) {
							BlockState state = fluid.defaultFluidState().createLegacyBlock();
							if (state.getBlock() != Blocks.AIR) {
								world.setBlockAndUpdate(parent.getBlockPos(), state);
							}
							// if less, try to place a flowing fluid
						} else if (fluid instanceof FlowingFluid) {
							int level = Math.max(1, relevantAmount * 8 / FluidType.BUCKET_VOLUME);
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
			this.parent.requestModelDataUpdate();
			return true;
		}
		return false;
	}

	/** Updates the cracks state of this block */
	public void setCracks(int cracks) {
		if (active && setCracksRaw(cracks)) {
			this.parent.setChangedFast();

			// if client, refresh block, if server sync to client
			Level world = parent.getLevel();
			if (world != null) {
				BlockPos pos = parent.getBlockPos();
				if (world.isClientSide()) {
					BlockState state = parent.getBlockState();
					world.sendBlockUpdated(pos, state, state, 3);
				} else {
					PacketHelper.sendToPlayersTrackingPosition(world, pos, new CrackableCrackPacket(pos, this.cracks));
				}
			}
		}
	}

	/** Gets the current cracks value of this block */
	public int getCracks() {
		return cracks;
	}

	/** Gets the model data for this TE */
	public ModelData getModelData() {
		return ModelData.builder().with(CRACKS, cracks).build();
	}


	/* Items */

	/** Runs in {@link BlockEntity#collectImplicitComponents(DataComponentMap.Builder)} to set item data. */
	public void collectImplicitComponents(DataComponentMap.Builder components) {
		if (active && cracks > 0) {
			components.set(Registration.CRACKS, (byte) Math.min(cracks, 5));
		}
	}

	/** Runs in {@link BlockEntity#applyImplicitComponents(BlockEntity.DataComponentInput)} to copy item data. */
	public void applyImplicitComponents(BlockEntity.DataComponentInput input) {
		if (active) {
			setCracksRaw(input.getOrDefault(Registration.CRACKS, (byte)0));
		}
	}

	/** Runs in {@link BlockEntity#removeComponentsFromTag(CompoundTag)} to remove the cracks data. */
	public void removeComponentsFromTag(CompoundTag tag) {
		tag.remove(TAG_CRACKS);
	}


	/* NBT */

	/**
	 * Reads cracks data from NBT
	 * @param nbt  NBT
	 */
	public void readNBT(BlockState state, CompoundTag nbt) {
		Block block = state.getBlock();
		if (block instanceof ICrackableBlock crackable && crackable.isCrackable()) {
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
	public void writeNBT(CompoundTag nbt) {
		if (active && cracks > 0) {
			nbt.putInt(TAG_CRACKS, cracks);
		}
	}

	/** Interface to allow blocks to opt out of cracking behavior on some variants */
	public interface ICrackableBlock {
		/** Determines if this version of the block is crackable */
		boolean isCrackable();

		/**
		 * Attempts to repair a crackable block
		 *
		 * @param held   Held item stack instance
		 * @param world  World instance
		 * @param pos    Position of crackable block
		 * @param player Player interacting
		 * @param hand   Hand used
		 * @return True if repaired, false for wrong repair item or no cracks
		 */
		static boolean tryRepair(ItemStack held, LevelAccessor world, BlockPos pos, Player player, InteractionHand hand) {
			if (held.is(CeramicsTags.Items.TERRACOTTA_CRACK_REPAIR) && world.getBlockEntity(pos) instanceof ICrackableBlockEntity te) {
				CrackableBlockEntityHandler handler = te.getCracksHandler();
				int cracks = handler.getCracks();
				if (handler.isActive() && cracks > 0) {
					// play sound
					world.playSound(player, pos, SoundType.GRAVEL.getPlaceSound(), SoundSource.BLOCKS, 1, 1);

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
			}
			return false;
		}
	}

	/** Interface to make syncing easier */
	public interface ICrackableBlockEntity {
		/** Gets the cracks handler for this TE */
		CrackableBlockEntityHandler getCracksHandler();
	}
}
