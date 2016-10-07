package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.BlockColored;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.EnumDyeColor;

public class BlockPorcelainClay extends BlockColored {

	public BlockPorcelainClay() {
		super(Material.ROCK);

		this.setHardness(1.25f);
		this.setResistance(30);
		setCreativeTab(Ceramics.tab);
		this.setSoundType(SoundType.STONE);
	}

	public static int getBlockColor(EnumDyeColor color) {
		switch(color) {
			case WHITE: return 0xffffff;
			case ORANGE: return 0xd87f33;
			case MAGENTA: return 0xb24cd8;
			case LIGHT_BLUE: return 0x6699d8;
			case YELLOW: return 0xeded00; //-33 +080800
			case LIME: return 0x7fcc19;
			case PINK: return 0xf27fa5;
			case GRAY: return 0x4c4c4c;
			case SILVER: return 0x999999;
			case CYAN: return 0x4c7f99;
			case PURPLE: return 0x7f3fb2;
			case BLUE: return 0x334cb2;
			case BROWN: return 0x664c33;
			case GREEN: return 0x667f33;
			case RED: return 0x993333;
			case BLACK: return 0x191919;
		}

		return 0xffffff;
	}

}
