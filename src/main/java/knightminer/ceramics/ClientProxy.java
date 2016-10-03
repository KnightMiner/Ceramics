package knightminer.ceramics;

import javax.annotation.Nonnull;

import knightminer.ceramics.client.BarrelRenderer;
import knightminer.ceramics.items.ItemClayUnfired.UnfiredType;
import knightminer.ceramics.tileentity.TileBarrel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerModels() {
		registerItemModel(Ceramics.clayBucket);
		registerItemModel(Ceramics.clayShears);
		registerItemModel(Ceramics.clayBucket, 1, "milk");

		registerItemModel(Ceramics.clayHelmet);
		registerItemModel(Ceramics.clayChestplate);
		registerItemModel(Ceramics.clayLeggings);
		registerItemModel(Ceramics.clayBoots);

		registerItemModel(Ceramics.clayHelmetRaw);
		registerItemModel(Ceramics.clayChestplateRaw);
		registerItemModel(Ceramics.clayLeggingsRaw);
		registerItemModel(Ceramics.clayBootsRaw);

		// unfired clay items are done using a block state
		for(UnfiredType type : UnfiredType.values()) {
			registerItemModel(Ceramics.clayUnfired, type.getMeta(), type.getName());
		}

		// barrels
		registerItemModel(Ceramics.clayBarrel, 0, "extension=false");
		registerItemModel(Ceramics.clayBarrel, 1, "extension=true");

		// barrel colors
		for(EnumDyeColor type : EnumDyeColor.values()) {
			registerItemModel(Ceramics.clayBarrelStained, type.getMetadata(), "color=" + type.getName());
			registerItemModel(Ceramics.clayBarrelStainedExtension, type.getMetadata(), "color=" + type.getName());
		}

		ClientRegistry.bindTileEntitySpecialRenderer(TileBarrel.class, new BarrelRenderer());
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
			final ResourceLocation location = item.getRegistryName();

			// tell the game which model to use for this item-meta combination
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(location, name));
		}
	}

	private void registerItemModel(Block block, int meta, String name) {
		registerItemModel(Item.getItemFromBlock(block), meta, name);
	}
}
