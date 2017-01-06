package knightminer.ceramics;

import javax.annotation.Nonnull;

import knightminer.ceramics.blocks.BlockEnumBase;
import knightminer.ceramics.blocks.BlockPorcelainClay;
import knightminer.ceramics.client.BarrelRenderer;
import knightminer.ceramics.items.ItemClayBucket.SpecialFluid;
import knightminer.ceramics.items.ItemClayUnfired.UnfiredType;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.tileentity.TileBarrel;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

	public static Minecraft minecraft = Minecraft.getMinecraft();

	@Override
	public void registerModels() {
		// color is handled by tinting
		ignoreProperty(BlockPorcelainClay.COLOR, Ceramics.porcelain, Ceramics.porcelainBarrel, Ceramics.porcelainBarrelExtension);

		// eventually loop this...
		registerItemModels(Ceramics.claySoft);
		registerItemModels(Ceramics.clayHard);

		registerItemModel(Ceramics.porcelain);
		registerItemModel(Ceramics.porcelainBarrel);
		registerItemModel(Ceramics.porcelainBarrelExtension);

		registerItemModel(Ceramics.clayBucket);
		registerItemModel(Ceramics.clayShears);

		// loop through the special bucket types
		for(SpecialFluid fluid : SpecialFluid.values()) {
			if(fluid != SpecialFluid.EMPTY) {
				registerItemModel(Ceramics.clayBucket, fluid.getMeta(), fluid.getName());
			}
		}

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

	@Override
	public void init() {
		final BlockColors blockColors = minecraft.getBlockColors();
		// porcelain colors
		if(Config.porcelainEnabled) {
			Block[] blocks;
			if(Config.barrelEnabled) {
				blocks = new Block[]{
						Ceramics.porcelain,
						Ceramics.porcelainBarrel,
						Ceramics.porcelainBarrelExtension
				};
			}
			else {
				blocks = new Block[]{
						Ceramics.porcelain
				};
			}
			blockColors.registerBlockColorHandler(
					new IBlockColor() {
						@Override
						public int colorMultiplier(@Nonnull IBlockState state, IBlockAccess access, BlockPos pos, int tintIndex) {
							EnumDyeColor type = state.getValue(BlockPorcelainClay.COLOR);
							return BlockPorcelainClay.getBlockColor(type);
						}
					},
					blocks);

			minecraft.getItemColors().registerItemColorHandler(
					new IItemColor() {
						@SuppressWarnings("deprecation")
						@Override
						public int getColorFromItemstack(@Nonnull ItemStack stack, int tintIndex) {
							IBlockState iblockstate = ((ItemBlock) stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata());
							return blockColors.colorMultiplier(iblockstate, null, null, tintIndex);
						}
					},
					blocks);

		}
	}

	private void registerItemModel(Item item) {
		registerItemModel(item, "inventory");
	}

	private void registerItemModel(Block block) {
		registerItemModel(Item.getItemFromBlock(block));
	}

	private void registerItemModel(Item item, final String variant) {
		if(item != null) {
			final ResourceLocation location = item.getRegistryName();
			ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
				@Nonnull
				@Override
				public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack) {
					return new ModelResourceLocation(location, variant);
				}
			});
			ModelLoader.registerItemVariants(item, location);
		}
	}

	private void registerItemModel(Block block, final String variant) {
		registerItemModel(Item.getItemFromBlock(block), variant);
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
	private <T extends Enum<T> & IStringSerializable & BlockEnumBase.IEnumMeta> void registerItemModels(BlockEnumBase<T> block) {
		if(block != null) {
			PropertyEnum<T> prop = block.getMappingProperty();
			for(T value : prop.getAllowedValues()) {
				registerItemModel(block, value.getMeta(), prop.getName() + "=" + value.getName());
			}
		}
	}

	private void ignoreProperty(IProperty<?> prop, Block ... blocks) {
		for(Block block : blocks) {
			if(block != null) {
				ModelLoader.setCustomStateMapper(block, (new StateMap.Builder()).ignore(prop).build());
			}
		}
	}
}
