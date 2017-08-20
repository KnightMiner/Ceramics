package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockEnumWallBase<T extends Enum<T> & IStringSerializable & BlockEnumBase.IEnumMeta> extends BlockWall implements IBlockEnum<T>  {

	private PropertyEnum<T> prop;
	private T[] values;

	public BlockEnumWallBase(Material material, PropertyEnum<T> prop) {
		super(setTemp(material, prop));
		this.prop = prop;
		this.values = prop.getValueClass().getEnumConstants();

		this.setCreativeTab(Ceramics.tab);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(UP, Boolean.valueOf(false))
				.withProperty(NORTH, Boolean.valueOf(false))
				.withProperty(EAST, Boolean.valueOf(false))
				.withProperty(SOUTH, Boolean.valueOf(false))
				.withProperty(WEST, Boolean.valueOf(false)));

	}

	// works around the property not being defined for the blockstate during construction
	private static PropertyEnum<?> temp;
	private static BlockMaterial setTemp(Material material, PropertyEnum<?> prop) {
		temp = prop;
		return new BlockMaterial(material);
	}

	@Override
	public PropertyEnum<T> getMappingProperty() {
		return prop;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		// I need variant since vanilla has just the one wall and sets for the default state
		if(prop == null) {
			return new BlockStateContainer(this, new IProperty[] {temp, UP, NORTH, EAST, WEST, SOUTH, VARIANT});
		}
		return new BlockStateContainer(this, new IProperty[] {prop, UP, NORTH, EAST, WEST, SOUTH, VARIANT});
	}

	public T fromMeta(int meta) {
		if(meta >= values.length || meta < 0) {
			meta = 0;
		}

		return values[meta];
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(prop, fromMeta(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(prop).getMeta();
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(prop).getMeta();
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for(T type : values) {
			if(type.shouldDisplay()) {
				list.add(new ItemStack(this, 1, type.getMeta()));
			}
		}
	}

	/**
	 * Determines if a torch can be placed on the top surface of this block.
	 * Useful for creating your own block that torches can be on, such as fences.
	 *
	 * @param state The current state
	 * @param world The current world
	 * @param pos Block position in world
	 * @return True to allow the torch to be placed
	 */
	@Override
	public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
		return true;
	}

	// yeah, it is what I have for the constructor
	private static class BlockMaterial extends Block {
		public BlockMaterial(Material material) {
			super(material);
		}
	}
}
