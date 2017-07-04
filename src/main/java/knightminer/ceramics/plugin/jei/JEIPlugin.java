package knightminer.ceramics.plugin.jei;

import java.util.ArrayList;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.library.Util;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {

	@Override
	public void register(IModRegistry registry) {
		// descriptions

		if(Config.bucketEnabled) {
			registry.addIngredientInfo(new ItemStack(Ceramics.clayBucket), ItemStack.class, Util.prefix("jei.bucket"));
		}

		if(Config.barrelEnabled) {
			ArrayList<ItemStack> barrels = new ArrayList<>();
			ArrayList<ItemStack> extensions = new ArrayList<>();

			// basic barrels
			barrels.add(new ItemStack(Ceramics.clayBarrel, 1, 0));
			extensions.add(new ItemStack(Ceramics.clayBarrel, 1, 1));

			// stained barrels
			for(EnumDyeColor color : EnumDyeColor.values()) {
				barrels.add(new ItemStack(Ceramics.clayBarrelStained, 1, color.getMetadata()));
				extensions.add(new ItemStack(Ceramics.clayBarrelStainedExtension, 1, color.getMetadata()));
			}

			registry.addIngredientInfo(barrels, ItemStack.class, I18n.translateToLocalFormatted(Util.prefix("jei.barrel.base"), Config.barrelClayCapacity));
			registry.addIngredientInfo(extensions, ItemStack.class, I18n.translateToLocalFormatted(Util.prefix("jei.barrel.extension"), Config.barrelClayCapacity));

			// porcelain ones, have a larger capacity so a separate entry
			if(Config.porcelainEnabled) {
				ArrayList<ItemStack> porcelainBarrels = new ArrayList<>();
				ArrayList<ItemStack> porcelainExtensions = new ArrayList<>();
				for(EnumDyeColor color : EnumDyeColor.values()) {
					porcelainBarrels.add(new ItemStack(Ceramics.porcelainBarrel, 1, color.getMetadata()));
					porcelainExtensions.add(new ItemStack(Ceramics.porcelainBarrelExtension, 1, color.getMetadata()));
				}

				registry.addIngredientInfo(porcelainBarrels, ItemStack.class, I18n.translateToLocalFormatted(Util.prefix("jei.barrel.porcelain.base"), Config.barrelPorcelainCapacity));
				registry.addIngredientInfo(porcelainExtensions, ItemStack.class, I18n.translateToLocalFormatted(Util.prefix("jei.barrel.porcelain.extension"), Config.barrelPorcelainCapacity));
			}
		}
	}
}
