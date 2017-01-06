package knightminer.ceramics.items;

import knightminer.ceramics.blocks.BlockEnumBase;
import knightminer.ceramics.blocks.BlockEnumSlabBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockEnumSlab<T extends Enum<T> & IStringSerializable & BlockEnumBase.IEnumMeta> extends ItemBlockEnum {

	private BlockEnumSlabBase<T> slab;

	public ItemBlockEnumSlab(BlockEnumSlabBase<T> block) {
		super(block);
		this.slab = block;
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		// don't place the slab if unable to edit
		if(stack.stackSize != 0 && player.canPlayerEdit(pos.offset(facing), facing, stack)) {

			// try placing the slab at the current position
			// note that this requires the slab to be extended on the side the block was clicked
			if(tryPlace(player, stack, world, pos, facing)) {
				return EnumActionResult.SUCCESS;
			}
			// otherwise. try and place it in the block in front
			else if(this.tryPlace(player, stack, world, pos.offset(facing), null)) {
				return EnumActionResult.SUCCESS;
			}

			return super.onItemUse(stack, player, world, pos, hand, facing, hitX, hitY, hitZ);
		}
		else {
			return EnumActionResult.FAIL;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
		BlockPos oldPos = pos;
		Comparable<?> type = this.slab.getTypeForItem(stack);
		IBlockState state = world.getBlockState(pos);

		// first, try placing on the same block
		if(state.getBlock() == this.slab) {
			boolean flag = state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;

			if((side == EnumFacing.UP && !flag || side == EnumFacing.DOWN && flag) && type == state.getValue(this.slab.getMappingProperty()) && this.slab.getFullBlock(state) != null) {
				return true;
			}
		}

		// if that does not work, offset by one and try same type
		pos = pos.offset(side);
		state = world.getBlockState(pos);
		return state.getBlock() == this.slab && type == state.getValue(this.slab.getMappingProperty()) || super.canPlaceBlockOnSide(world, oldPos, side, player, stack);
	}

	private boolean tryPlace(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing side) {
		IBlockState state = world.getBlockState(pos);
		Comparable<?> type = this.slab.getTypeForItem(stack);

		if(state.getBlock() == this.slab) {
			BlockSlab.EnumBlockHalf half = state.getValue(BlockSlab.HALF);

			if(type == state.getValue(this.slab.getMappingProperty())
					&& (side == null
					|| side == EnumFacing.UP && half == BlockSlab.EnumBlockHalf.BOTTOM
					|| side == EnumFacing.DOWN && half == BlockSlab.EnumBlockHalf.TOP)) {

				IBlockState fullBlock = this.slab.getFullBlock(state);
				if(fullBlock != null) {
					AxisAlignedBB axisalignedbb = fullBlock.getCollisionBoundingBox(world, pos);

					if(axisalignedbb != Block.NULL_AABB && world.checkNoEntityCollision(axisalignedbb.offset(pos)) && world.setBlockState(pos, fullBlock, 11)) {
						SoundType soundtype = fullBlock.getBlock().getSoundType(state, world, pos, player);
						world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
						--stack.stackSize;
					}

					return true;
				}
			}
		}

		return false;
	}
}
