package knightminer.ceramics.blocks;

import java.util.Locale;
import java.util.Random;

import javax.annotation.Nullable;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.items.ItemClayUnfired;
import knightminer.ceramics.library.Config;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;

public class BlockClaySoft extends BlockEnumBase<BlockClaySoft.ClayTypeSoft> {

	public static final PropertyEnum<ClayTypeSoft> TYPE = PropertyEnum.<ClayTypeSoft>create("type", ClayTypeSoft.class);

	public BlockClaySoft() {
		super(Material.CLAY, TYPE);
		this.setCreativeTab(Ceramics.tab);
		this.setHardness(0.6f);
		this.setResistance(3);
		this.setSoundType(SoundType.GROUND);
		this.setHarvestLevel("shovel", -1);
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Ceramics.clayUnfired;
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	@Override
	public int damageDropped(IBlockState state) {
		return ItemClayUnfired.UnfiredType.PORCELAIN.getMeta();
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	@Override
	public int quantityDropped(Random random) {
		return 4;
	}

	public enum ClayTypeSoft implements IStringSerializable, BlockEnumBase.IEnumMeta {
		PORCELAIN;

		private int meta;

		ClayTypeSoft() {
			meta = this.ordinal();
		}

		@Override
		public int getMeta() {
			return meta;
		}

		public static ClayTypeSoft fromMeta(int meta) {
			if(meta < 0 || meta >= values().length) {
				meta = 0;
			}

			return values()[meta];
		}

		@Override
		public boolean shouldDisplay() {
			return Config.porcelainEnabled;
		}

		@Override
		public String getName() {
			return this.name().toLowerCase(Locale.US);
		}
	}
}
