package knightminer.ceramics.util;

import knightminer.ceramics.items.FluidClayBucketItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.datagen.MantleTags;

import javax.annotation.Nullable;

/**
 * Common logic to interaction with cauldrons
 */
public class EmptyFluidBucketCauldronInteraction extends EmptyClayBucketCauldronInteraction<Fluid> {
	private final FluidClayBucketItem bucket;
	public EmptyFluidBucketCauldronInteraction(FluidClayBucketItem bucket) {
		this.bucket = bucket;
	}

	@Override
	protected Fluid getContents(ItemStack stack) {
		return bucket.getFluid(stack);
	}

	/**
	 * Converts a fluid to a cauldron
	 * TODO: currently hardcoded to vanilla cauldrons, need a way to generalize this
	 * @param fluid  Fluid input
	 * @return  Cauldron, or null if none exists
	 */
	@Override
	@Nullable
	protected BlockState getFullCauldron(Fluid fluid) {
		if (fluid.is(MantleTags.Fluids.WATER)) {
			return Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
		}
		if (fluid.is(MantleTags.Fluids.LAVA)) {
			return Blocks.LAVA_CAULDRON.defaultBlockState();
		}
		return null;
	}

	@Override
	protected SoundEvent getSound(Fluid contents) {
		return contents.getAttributes().getEmptySound();
	}
}
