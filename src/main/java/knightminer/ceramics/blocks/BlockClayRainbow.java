package knightminer.ceramics.blocks;

import java.util.Locale;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.Config;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public class BlockClayRainbow extends BlockEnumBase<BlockClayRainbow.RainbowStart> {

	public static final PropertyEnum<RainbowStart> START = PropertyEnum.<RainbowStart>create("start", RainbowStart.class);

	public BlockClayRainbow() {
		super(Material.ROCK, START);
		this.setCreativeTab(Ceramics.tab);
		this.setHarvestLevel("pickaxe", 0);
		this.setHardness(1.25f);
		this.setResistance(30);
		this.setSoundType(SoundType.STONE);
	}

	public enum RainbowStart implements IStringSerializable, BlockEnumBase.IEnumMeta {
		RED,
		ORANGE,
		YELLOW,
		GREEN,
		CYAN,
		BLUE,
		PURPLE,
		MAGENTA;

		private int meta;

		RainbowStart() {
			meta = this.ordinal();
		}

		@Override
		public int getMeta() {
			return meta;
		}

		@Override
		public boolean shouldDisplay() {
			return Config.rainbowClayEnabled;
		}

		public static RainbowStart fromMeta(int meta) {
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
