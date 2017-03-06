package knightminer.ceramics.blocks;

import java.util.List;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	 * Returns the slab block name with the type associated with it
	 *
	@Override
	public String getUnlocalizedName(int meta) {
		IBlockState state = getStateFromMeta(meta);
		String name = state.getValue(prop).getName();
		return super.getUnlocalizedName() + "." + name;
	}*/

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for(T type : values) {
			if(type.shouldDisplay()) {
				list.add(new ItemStack(itemIn, 1, type.getMeta()));
			}
		}
	}

	// yeah, it is what I have for the constructor
	private static class BlockMaterial extends Block {
		public BlockMaterial(Material material) {
			super(material);
		}

	}
}
