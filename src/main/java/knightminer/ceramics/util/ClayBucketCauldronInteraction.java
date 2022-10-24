package knightminer.ceramics.util;

import knightminer.ceramics.items.BaseClayBucketItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import slimeknights.mantle.datagen.MantleTags;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Common logic to interaction with cauldrons
 */
public class ClayBucketCauldronInteraction implements CauldronInteraction {
	private static final Predicate<BlockState> ALWAYS = state -> true;

	private final BaseClayBucketItem bucket;
	public ClayBucketCauldronInteraction(BaseClayBucketItem bucket) {
		this.bucket = bucket;
	}

	@Override
	public InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
		if (bucket.hasFluid(stack)) {
			// fill cauldron from bucket
			Fluid fluid = bucket.getFluid(stack);
			BlockState cauldron = getFullCauldron(fluid);
			if (cauldron != null) {
				// would use vanilla logic here, but it is dumb and insists on using a vanilla bucket
				if (!level.isClientSide) {
					// empty bucket
					player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, stack.getContainerItem()));
					// grant stats
					player.awardStat(Stats.FILL_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(bucket));
					// update cauldron
					level.setBlockAndUpdate(pos, cauldron);
					// effects
					level.playSound(null, pos, fluid.getAttributes().getEmptySound(), SoundSource.BLOCKS, 1.0F, 1.0F);
					level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		} else {
			// fill bucket from cauldron
			Fluid fluid = getFluidFromCauldron(state);
			if (fluid != null) {
				return CauldronInteraction.fillBucket(state, level, pos, player, hand, stack, bucket.withFluid(fluid), ALWAYS, fluid.getAttributes().getFillSound());
			}
		}
		return InteractionResult.PASS;
	}

	/**
	 * Converts a fluid to a cauldron
	 * TODO: currently hardcoded to vanilla cauldrons, need a way to generalize this
	 * @param fluid  Fluid input
	 * @return  Cauldron, or null if none exists
	 */
	@Nullable
	private static BlockState getFullCauldron(Fluid fluid) {
		if (fluid.is(MantleTags.Fluids.WATER)) {
			return Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
		}
		if (fluid.is(MantleTags.Fluids.LAVA)) {
			return Blocks.LAVA_CAULDRON.defaultBlockState();
		}
		return null;
	}

	/**
	 * Converts a cauldron to a fluid
	 * TODO: currently hardcoded to vanilla cauldrons, need a way to generalize this
	 * @param cauldron  Fluid input
	 * @return  Cauldron, or null if none exists
	 */
	@Nullable
	public static Fluid getFluidFromCauldron(BlockState cauldron) {
		Block block = cauldron.getBlock();
		if (block == Blocks.LAVA_CAULDRON) {
			return Fluids.LAVA;
		}
		if (block == Blocks.WATER_CAULDRON && cauldron.getValue(LayeredCauldronBlock.LEVEL) == 3) {
			return Fluids.WATER;
		}
		return null;
	}
}
