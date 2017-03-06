package knightminer.ceramics.blocks;

import java.util.Locale;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.Config;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public class BlockClayWall extends BlockEnumWallBase<BlockClayWall.ClayWallType> {

	public static final PropertyEnum<ClayWallType> TYPE = PropertyEnum.<ClayWallType>create("type", ClayWallType.class);
	public BlockClayWall() {
		super(Material.ROCK, TYPE);
		this.setCreativeTab(Ceramics.tab);
		this.setHarvestLevel("pickaxe", 0);
		this.setHardness(1.25f);
		this.setResistance(30);
		this.setSoundType(SoundType.STONE);
	}

	public enum ClayWallType implements IStringSerializable, BlockEnumBase.IEnumMeta {
		BRICKS,
		PORCELAIN_BRICKS,
		DARK_BRICKS,
		MARINE_BRICKS,
		GOLDEN_BRICKS,
		DRAGON_BRICKS;

		private int meta;

		ClayWallType() {
			meta = this.ordinal();
		}

		@Override
		public int getMeta() {
			return meta;
		}

		@Override
		public boolean shouldDisplay() {
			switch(this) {
				case BRICKS:
					return Config.brickWallEnabled;
				case PORCELAIN_BRICKS:
					return Config.porcelainEnabled;
				case DARK_BRICKS:
				case MARINE_BRICKS:
				case GOLDEN_BRICKS:
				case DRAGON_BRICKS:
					return Config.fancyBricksEnabled;
			}

			return false;
		}

		public static ClayWallType fromMeta(int meta) {
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
