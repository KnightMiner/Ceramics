package knightminer.ceramics.plugin.jei;

import java.util.ArrayList;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.library.Util;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {

	@Override
	public void register(IModRegistry registry) {
		// descriptions

		if(Config.bucketEnabled) {
			registry.addDescription(new ItemStack(Ceramics.clayBucket), Util.prefix("jei.bucket"));
		}

		if(Config.barrelEnabled) {
			ArrayList<ItemStack> barrels = new ArrayList<ItemStack>();
			ArrayList<ItemStack> extensions = new ArrayList<ItemStack>();

			// basic barrels
			barrels.add(new ItemStack(Ceramics.clayBarrel, 1, 0));
			extensions.add(new ItemStack(Ceramics.clayBarrel, 1, 1));

			// stained barrels
			for(EnumDyeColor color : EnumDyeColor.values()) {
				barrels.add(new ItemStack(Ceramics.clayBarrelStained, 1, color.getMetadata()));
				extensions.add(new ItemStack(Ceramics.clayBarrelStainedExtension, 1, color.getMetadata()));
			}

			registry.addDescription(barrels, Util.prefix("jei.barrel.base"));
			registry.addDescription(extensions, Util.prefix("jei.barrel.extension"));

			// porcelain ones, have a larger capacity so a separate entry
			if(Config.porcelainEnabled) {
				ArrayList<ItemStack> porcelainBarrels = new ArrayList<ItemStack>();
				ArrayList<ItemStack> porcelainExtensions = new ArrayList<ItemStack>();
				for(EnumDyeColor color : EnumDyeColor.values()) {
					porcelainBarrels.add(new ItemStack(Ceramics.porcelainBarrel, 1, color.getMetadata()));
					porcelainExtensions.add(new ItemStack(Ceramics.porcelainBarrelExtension, 1, color.getMetadata()));
				}

				registry.addDescription(porcelainBarrels, Util.prefix("jei.barrel_porcelain.base"));
				registry.addDescription(porcelainExtensions, Util.prefix("jei.barrel_porcelain.extension"));
			}
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {}

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {}

}
