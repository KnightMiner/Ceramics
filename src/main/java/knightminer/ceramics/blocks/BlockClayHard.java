package knightminer.ceramics.blocks;

import java.util.Locale;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public class BlockClayHard extends BlockEnumBase<BlockClayHard.ClayTypeHard> {

	public static final PropertyEnum<ClayTypeHard> TYPE = PropertyEnum.<ClayTypeHard>create("type", ClayTypeHard.class);

	public BlockClayHard() {
		super(Material.CLAY, TYPE);
		this.setCreativeTab(Ceramics.tab);
		this.setHarvestLevel("pickaxe", 0);
		this.setHardness(1.25f);
		this.setResistance(30);
		this.setSoundType(SoundType.STONE);
	}

	public enum ClayTypeHard implements IStringSerializable, BlockEnumBase.IBlockMeta {
		PORCELAIN_BRICKS;

		private int meta;

		ClayTypeHard() {
			meta = this.ordinal();
		}

		@Override
		public int getMeta() {
			return meta;
		}

		public static ClayTypeHard fromMeta(int meta) {
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
