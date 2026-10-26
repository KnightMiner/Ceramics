package knightminer.ceramics.util;

import knightminer.ceramics.items.FluidClayBucketItem;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Common logic to interaction with cauldrons.
 */
public class EmptyFluidBucketCauldronInteraction extends EmptyClayBucketCauldronInteraction<Fluid> {
	public static final EmptyFluidBucketCauldronInteraction INSTANCE = new EmptyFluidBucketCauldronInteraction();

	private EmptyFluidBucketCauldronInteraction() {}

	@Override
	protected Fluid getContents(ItemStack stack) {
		return FluidClayBucketItem.getFluid(stack);
	}

	@Override
	@Nullable
	protected BlockState getFullCauldron(Fluid fluid) {
		CauldronFluidContent cauldronFluid = CauldronFluidContent.getForFluid(fluid);
		if (cauldronFluid != null) {
			BlockState state = cauldronFluid.block.defaultBlockState();
			// if we have a level property, figure out how many layers 1 bucket gives
			if (cauldronFluid.levelProperty != null) {
				int layers = FluidType.BUCKET_VOLUME * cauldronFluid.maxLevel / cauldronFluid.totalAmount;
				if (layers > 0) {
					return state.setValue(cauldronFluid.levelProperty, layers);
				}

				// if no level property, we can use the plain state as long as the cauldron holds a bucket or less
			} else if (cauldronFluid.totalAmount <= FluidType.BUCKET_VOLUME) {
				return state;
			}
		}
		// its possible the powder snow cauldron is not registered to a fluid, so check it directly
		if (fluid.is(CeramicsTags.Fluids.POWDER_SNOW)) {
			return Blocks.POWDER_SNOW_CAULDRON.defaultBlockState();
		}
		return null;
	}

	@Override
	protected SoundEvent getSound(Fluid contents) {
		return Objects.requireNonNullElse(contents.getFluidType().getSound(SoundActions.BUCKET_EMPTY), SoundEvents.BUCKET_EMPTY);
	}
}
