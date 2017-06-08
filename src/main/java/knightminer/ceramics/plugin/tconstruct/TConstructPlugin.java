package knightminer.ceramics.plugin.tconstruct;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.items.ItemClayUnfired.UnfiredType;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.library.ModIDs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import slimeknights.mantle.item.ItemBlockMeta;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.smeltery.BucketCastingRecipe;
import slimeknights.tconstruct.library.smeltery.CastingRecipe;
import slimeknights.tconstruct.shared.TinkerFluids;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

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
			// let our bucket be filled in a casting table
			TinkerRegistry.registerTableCasting(new BucketCastingRecipe(Ceramics.clayBucket));
		}
		if(Config.armorEnabled) {
			ItemStack castPlate = GameRegistry.makeItemStack(ModIDs.Tinkers.cast, ModIDs.Tinkers.castPlateMeta, 1, null);
			if(!castPlate.isEmpty()) {
				// cast clay plates, cause clay
				TinkerRegistry.registerTableCasting(new ItemStack(Ceramics.clayUnfired, 1, UnfiredType.CLAY_PLATE.getMeta()),
						castPlate, TinkerFluids.clay, Material.VALUE_Ingot * 2);

				for(FluidStack fluid : TinkerSmeltery.castCreationFluids) {
					TinkerRegistry.registerTableCasting(new CastingRecipe(castPlate,
							RecipeMatch.of(new ItemStack(Ceramics.clayUnfired, 1, UnfiredType.CLAY_PLATE.getMeta())), fluid, true, true));
				}
			} else {
				Ceramics.log.error("Failed to find Tinkers Construct cast item");
			}
		}
	}
}
