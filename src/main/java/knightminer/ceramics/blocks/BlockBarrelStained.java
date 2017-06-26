package knightminer.ceramics.blocks;

import knightminer.ceramics.blocks.BlockStained.StainedColor;
import knightminer.ceramics.library.Config;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class BlockBarrelStained extends BlockBarrel {

	private boolean extension;

	public BlockBarrelStained(boolean extension) {
		super(Material.ROCK);
		this.setDefaultState(this.getBlockState().getBaseState().withProperty(BlockStained.COLOR, StainedColor.WHITE));
		this.extension = extension;
	}

	/* Extension data */
	@Override
	public boolean isExtension(IBlockState state) {
		return extension;
	}

	/* Color data */
	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		if(Config.barrelEnabled) {
			for (EnumDyeColor enumdyecolor : EnumDyeColor.values()) {
				list.add(new ItemStack(this, 1, enumdyecolor.getMetadata()));
			}
		}
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	//@Override
	//public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
	//	return state.getValue(BlockStained.COLOR).asDyeColor().getMapColor();
	//}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(BlockStained.COLOR, StainedColor.fromMeta(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockStained.COLOR).getMeta();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {BlockStained.COLOR});
	}
}
