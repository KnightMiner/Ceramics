package knightminer.ceramics.util.cauldron;

import knightminer.ceramics.items.FluidClayBucketItem;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
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
	protected Fluid emptyContents() {
		return Fluids.EMPTY;
	}

	@Override
	@Nullable
	protected BlockState getFullCauldron(BlockState state, Fluid fluid) {
		CauldronFluidContent cauldronFluid = CauldronFluidContent.getForFluid(fluid);
		if (cauldronFluid != null) {
			BlockState cauldron = cauldronFluid.block.defaultBlockState();
			// if we have a level property, figure out how many layers 1 bucket gives
			if (cauldronFluid.levelProperty != null) {
				int layers = FluidType.BUCKET_VOLUME * cauldronFluid.maxLevel / cauldronFluid.totalAmount;
				if (layers > 0) {
					// if the cauldron already has the fluid, add the bucket to existing contents
					// probably not needed, but maybe someone adds a cauldron that holds more than a bucket
					if (cauldron.getBlock() == state.getBlock()) {
						layers = Math.min(layers + state.getValue(cauldronFluid.levelProperty), cauldronFluid.maxLevel);
					}
					return cauldron.setValue(cauldronFluid.levelProperty, layers);
				}
				// if no level property, we can use the plain state as long as the cauldron holds a bucket or less
			} else if (cauldronFluid.totalAmount <= FluidType.BUCKET_VOLUME) {
				return cauldron;
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
