package knightminer.ceramics.plugin.tconstruct;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.items.ItemClayUnfired.UnfiredType;
import knightminer.ceramics.library.Config;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import slimeknights.mantle.item.ItemBlockMeta;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.BucketCastingRecipe;

public class TConstructPlugin {

	public static void preInit() {
		if(Config.porcelainFaucetEnabled) {
			Ceramics.porcelainFaucet = Ceramics.registerBlock(new ItemBlockMeta(new BlockFaucet()), "faucet");
		}
	}

	public static void init() {
		if(Config.porcelainFaucetEnabled) {
			// just the standard recipes, only using porcelain bricks
			ItemStack porcelainBrick = new ItemStack(Ceramics.clayUnfired, 1, UnfiredType.PORCELAIN_BRICK.getMeta());

			GameRegistry.addRecipe(new ItemStack(Ceramics.porcelainFaucet),
					"b b", " b ", 'b', porcelainBrick.copy()); // Faucet
		}
	}

	public static void postInit() {
		if(Config.bucketEnabled) {
			TinkerRegistry.registerTableCasting(new BucketCastingRecipe(Ceramics.clayBucket));
		}
	}
}
