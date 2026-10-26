package knightminer.ceramics.util;

import knightminer.ceramics.items.SolidClayBucketItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class EmptySolidBucketCauldronInteraction extends EmptyClayBucketCauldronInteraction<Block> {
	public static final EmptySolidBucketCauldronInteraction INSTANCE = new EmptySolidBucketCauldronInteraction();

	private EmptySolidBucketCauldronInteraction() {}

	@Override
	protected Block getContents(ItemStack stack) {
		return SolidClayBucketItem.getBlock(stack);
	}

	// TODO: currently hardcoded to vanilla cauldrons, need a way to generalize this
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
		return contents.defaultBlockState().getSoundType().getPlaceSound();
	}
}
