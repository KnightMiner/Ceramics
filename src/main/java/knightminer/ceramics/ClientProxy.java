package knightminer.ceramics;

import javax.annotation.Nonnull;

import knightminer.ceramics.items.ItemClayUnfired.UnfiredType;
import knightminer.ceramics.library.Util;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerModels() {
		for(UnfiredType type : UnfiredType.values()) {
			registerItemModel(Ceramics.clayUnfired, type.getMeta(), "unfired_" + type.getName());
		}

		registerItemModel(Ceramics.clayBucket);
		registerItemModel(Ceramics.clayShears);
		ModelLoader.setCustomModelResourceLocation(Ceramics.clayBucket, 1,
				new ModelResourceLocation(Ceramics.clayBucket.getRegistryName(), "milk"));

		registerItemModel(Ceramics.clayHelmet);
		registerItemModel(Ceramics.clayChestplate);
		registerItemModel(Ceramics.clayLeggings);
		registerItemModel(Ceramics.clayBoots);

		registerItemModel(Ceramics.clayHelmetRaw);
		registerItemModel(Ceramics.clayChestplateRaw);
		registerItemModel(Ceramics.clayLeggingsRaw);
		registerItemModel(Ceramics.clayBootsRaw);
	}

	private void registerItemModel(Item item) {
		if(item != null) {
			final ResourceLocation location = item.getRegistryName();
			ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
				@Nonnull
				@Override
				public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack) {
					return new ModelResourceLocation(location, "inventory");
				}
			});
			ModelLoader.registerItemVariants(item, location);
		}
	}

	private void registerItemModel(Item item, int meta, String name) {
		if(item != null) {
			// tell Minecraft which textures it has to load. This is resource-domain sensitive
			if(!name.contains(":")) {
				name = Util.resource(name);
			}

			ModelLoader.registerItemVariants(item, new ResourceLocation(name));
			// tell the game which model to use for this item-meta combination
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(name, "inventory"));
		}
	}
}
