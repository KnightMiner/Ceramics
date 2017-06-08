package knightminer.ceramics.plugin.bwm;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.blocks.BlockClayHard.ClayTypeHard;
import knightminer.ceramics.library.ModIDs;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class BetterWithModsPlugin {
	public static void init() {
		// allow our clay to be used for the kiln
		for(ClayTypeHard clay : ClayTypeHard.values()) {
			addKilnBlock(Ceramics.clayHard, clay.getMeta());
		}
	}

	private static void addKilnBlock(Block block, int meta) {
		if(block != null) {
			FMLInterModComms.sendMessage(ModIDs.BWM, "registerKilnBlock", new ItemStack(block, 1, meta));
		}
	}
}
