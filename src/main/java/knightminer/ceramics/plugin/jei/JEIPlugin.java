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

			// porcelain ones, we want all the colors in order
			if(Config.porcelainEnabled) {
				for(EnumDyeColor color : EnumDyeColor.values()) {
					barrels.add(new ItemStack(Ceramics.porcelainBarrel, 1, color.getMetadata()));
					extensions.add(new ItemStack(Ceramics.porcelainBarrelExtension, 1, color.getMetadata()));
				}
			}

			registry.addDescription(barrels, Util.prefix("jei.barrel.base"));
			registry.addDescription(extensions, Util.prefix("jei.barrel.extension"));
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {}

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {}

}
