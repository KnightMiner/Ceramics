package knightminer.ceramics.blocks;

import java.util.Locale;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public class BlockStained extends BlockEnumBase<BlockStained.StainedColor> {

	public static final PropertyEnum<StainedColor> COLOR = PropertyEnum.<StainedColor>create("color", StainedColor.class);

	public BlockStained() {
		super(Material.ROCK, COLOR);

		this.setHardness(1.25f);
		this.setResistance(30);
		setCreativeTab(Ceramics.tab);
		this.setSoundType(SoundType.STONE);
	}

	public enum StainedColor implements IStringSerializable, BlockEnumBase.IEnumMeta {
		WHITE(0xffffff),
		ORANGE(0xd87f33),
		MAGENTA(0xb24cd8),
		LIGHT_BLUE(0x6699d8),
		YELLOW(0xeded00), //-33 +080800
		LIME(0x7fcc19),
		PINK(0xf27fa5),
		GRAY(0x4c4c4c),
		SILVER(0x999999),
		CYAN(0x4c7f99),
		PURPLE(0x7f3fb2),
		BLUE(0x334cb2),
		BROWN(0x664c33),
		GREEN(0x667f33),
		RED(0x993333),
		BLACK(0x191919);

		private int meta;
		private int color;
		StainedColor(int color) {
			this.meta = ordinal();
			this.color = color;
		}

		public int getColor() {
			return color;
		}

		@Override
		public int getMeta() {
			return meta;
		}

		public static StainedColor fromMeta(int meta) {
			if(meta < 0 || meta >= values().length) {
				meta = 0;
			}

			return values()[meta];
		}

		@Override
		public String getName() {
			return this.name().toLowerCase(Locale.US);
		}

		@Override
		public boolean shouldDisplay() {
			return true;
		}
	}

}
