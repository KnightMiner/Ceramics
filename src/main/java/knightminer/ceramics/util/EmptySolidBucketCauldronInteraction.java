package knightminer.ceramics.util;

import knightminer.ceramics.items.SolidClayBucketItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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

import javax.annotation.Nullable;

/**
 * Common logic to interaction with cauldrons
 */
public class EmptySolidBucketCauldronInteraction extends EmptyClayBucketCauldronInteraction<Block> {
	private final SolidClayBucketItem bucket;
	public EmptySolidBucketCauldronInteraction(SolidClayBucketItem bucket) {
		this.bucket = bucket;
	}

	@Override
	public InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
		// fill cauldron from bucket
		Block block = bucket.getBlock(stack);
		BlockState cauldron = getFullCauldron(block);
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
				level.playSound(null, pos, block.getSoundType(block.defaultBlockState()).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return InteractionResult.PASS;
	}

	@Override
	protected Block getContents(ItemStack stack) {
		return bucket.getBlock(stack);
	}

	/**
	 * Converts a fluid to a cauldron
	 * TODO: currently hardcoded to vanilla cauldrons, need a way to generalize this
	 * @param block  Block input
	 * @return  Cauldron, or null if none exists
	 */
	@Override
	@Nullable
	protected BlockState getFullCauldron(Block block) {
		if (block == Blocks.POWDER_SNOW) {
			return Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
		}
		return null;
	}

	@Override
	protected SoundEvent getSound(Block contents) {
		if (contents == Blocks.POWDER_SNOW) {
			return SoundEvents.BUCKET_EMPTY_POWDER_SNOW;
		}
		return contents.getSoundType(contents.defaultBlockState()).getPlaceSound();
	}
}
