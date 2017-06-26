package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;

public class BlockStairsEnum<T extends Enum<T> & IStringSerializable & BlockEnumBase.IEnumMeta> extends BlockStairs {
	private T value;

	public <B extends Block & IBlockEnum<T>> BlockStairsEnum(B block, T value) {
		super(block.getDefaultState().withProperty(block.getMappingProperty(), value));
		this.value = value;
		this.useNeighborBrightness = true;
		this.setCreativeTab(Ceramics.tab);
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		if(value.shouldDisplay()) {
			list.add(new ItemStack(this));
		}
	}
}
