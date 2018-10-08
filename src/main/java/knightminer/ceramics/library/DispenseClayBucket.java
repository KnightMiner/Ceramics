package knightminer.ceramics.library;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.items.ItemClayBucket.SpecialFluid;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.DispenseFluidContainer;

public class DispenseClayBucket extends BehaviorDefaultDispenseItem {

	public static final DispenseClayBucket INSTANCE = new DispenseClayBucket();
	private final BehaviorDefaultDispenseItem dispenseBehavior = new BehaviorDefaultDispenseItem();

	private DispenseClayBucket() {}

	/**
	 * Dispense the specified stack, play the dispense sound and spawn particles.
	 */
	@Override
	public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
		// data from the dispenser and the world
		World world = source.getWorld();
		EnumFacing dispenserFacing = source.getBlockState().getValue(BlockDispenser.FACING);
		BlockPos pos = source.getBlockPos().offset(dispenserFacing);
		IBlockState state = source.getWorld().getBlockState(pos);

		// if we have a special fluid and its a block, try placing it
		SpecialFluid fluid = Ceramics.clayBucket.getSpecialFluid(stack);
		IBlockState fluidState = fluid.getState();
		if(fluidState != null) {
			// if the block in front is replaceable
			if(state.getBlock().isReplaceable(world, pos)) {
				// place the fluid there
				if(!world.isRemote) {
					world.setBlockState(pos, fluidState);
				}

				// sound
				world.playSound(null, pos, fluidState.getBlock().getSoundType(fluidState, world, pos, null).getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 0.8F);

				// and return the empty stack
				return new ItemStack(Ceramics.clayBucket);
			}

			// otherwise, drop the item, we already know it is a block type
			return this.dispenseBehavior.dispense(source, stack);
		}

		// if empty, try filling with a block
		if(!Ceramics.clayBucket.hasFluid(stack)) {
			SpecialFluid newFluid = SpecialFluid.fromState(state);
			if(newFluid != null) {
				// sound
				world.playSound(null, pos, state.getBlock().getSoundType(state, world, pos, null).getBreakSound(), SoundCategory.BLOCKS, 1.0F, 0.8F);

				// remove the sand/gravel
				if(!world.isRemote) {
					world.setBlockToAir(pos);
				}

				ItemStack filled = Ceramics.clayBucket.withSpecialFluid(newFluid);
				stack.shrink(1);

				if(stack.isEmpty()) {
					return filled;
				}

				// try adding the copy to the dispenser
				if (((TileEntityDispenser)source.getBlockTileEntity()).addItemStack(filled) < 0) {
					this.dispenseBehavior.dispense(source, filled);
				}

				return stack;
			}
		}

		// if we cannot fill with a block or have a fluid, use standard bucket code
		return DispenseFluidContainer.getInstance().dispenseStack(source, stack);
	}
}
