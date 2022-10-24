package knightminer.ceramics.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;

public abstract class EmptyClayBucketCauldronInteraction<T> implements CauldronInteraction {

	/** Gets the contents of the given stack */
	protected abstract T getContents(ItemStack stack);

	/**
	 * Converts a fluid to a cauldron
	 * @param contents  Input
	 * @return  Cauldron, or null if none exists
	 */
	@Nullable
	protected abstract BlockState getFullCauldron(T contents);

	/** Gets the sound for the given contents */
	protected abstract SoundEvent getSound(T contents);

	@Override
	public InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
		// fill cauldron from bucket
		T contents = getContents(stack);
		BlockState cauldron = getFullCauldron(contents);
		if (cauldron != null) {
			// would use vanilla logic here, but it is dumb and insists on using a vanilla bucket
			if (!level.isClientSide) {
				// empty bucket
				player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, stack.getContainerItem()));
				// grant stats
				player.awardStat(Stats.FILL_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
				// update cauldron
				level.setBlockAndUpdate(pos, cauldron);
				// effects
				level.playSound(null, pos, getSound(contents), SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return InteractionResult.PASS;
	}
}
