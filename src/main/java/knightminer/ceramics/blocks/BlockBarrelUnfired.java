package knightminer.ceramics.blocks;

import java.util.Locale;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.Config;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;

public class BlockBarrelUnfired extends BlockBarrelBase {

	public static PropertyEnum<UnfiredType> TYPE = PropertyEnum.create("type", UnfiredType.class);
	public BlockBarrelUnfired() {
		super(Material.CLAY);

		this.setHardness(0.6F);
		this.setCreativeTab(Ceramics.tab);
		this.setSoundType(SoundType.GROUND);
		this.setHarvestLevel("shovel", -1);

		this.setDefaultState(this.getBlockState().getBaseState().withProperty(EXTENSION, Boolean.FALSE));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, EXTENSION, TYPE);
	}

	@Override
	@Deprecated
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState()
				.withProperty(EXTENSION, (meta & 1) == 1)
				.withProperty(TYPE, UnfiredType.fromMeta(meta >> 1));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = 0;
		if(state.getValue(EXTENSION)) {
			meta = 1;
		}

		meta |= state.getValue(TYPE).getMeta() << 1;

		return meta;
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		if(Config.barrelEnabled) {
			list.add(new ItemStack(this, 1, 0));
			list.add(new ItemStack(this, 1, 1));
			if (Config.porcelainEnabled) {
				list.add(new ItemStack(this, 1, 2));
				list.add(new ItemStack(this, 1, 3));
			}
		}
	}

	public enum UnfiredType implements IStringSerializable {
		CLAY,
		PORCELAIN;

		private int meta;

		UnfiredType() {
			meta = this.ordinal();
		}

		public int getMeta() {
			return meta;
		}

		public static UnfiredType fromMeta(int meta) {
			if(meta < 0 || meta >= values().length) {
				meta = 0;
			}

			return values()[meta];
		}

		@Override
		public String getName() {
			return this.name().toLowerCase(Locale.US);
		}
	}
}
