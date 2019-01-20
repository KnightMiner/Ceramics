package knightminer.ceramics;

import javax.annotation.Nonnull;

import knightminer.ceramics.blocks.BlockClayWall;
import knightminer.ceramics.blocks.BlockEnumBase;
import knightminer.ceramics.blocks.BlockEnumSlabBase;
import knightminer.ceramics.blocks.BlockStained;
import knightminer.ceramics.blocks.IBlockEnum;
import knightminer.ceramics.client.BarrelRenderer;
import knightminer.ceramics.client.ChannelRenderer;
import knightminer.ceramics.client.FaucetRenderer;
import knightminer.ceramics.items.ItemClayBucket.SpecialFluid;
import knightminer.ceramics.items.ItemClayUnfired.UnfiredType;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.library.Util;
import knightminer.ceramics.library.client.IgnoreAllStateMapper;
import knightminer.ceramics.library.client.PropertyStateMapper;
import knightminer.ceramics.library.client.RenameStateMapper;
import knightminer.ceramics.tileentity.TileBarrel;
import knightminer.ceramics.tileentity.TileChannel;
import knightminer.ceramics.tileentity.TileFaucet;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

	public static Minecraft minecraft = Minecraft.getMinecraft();
	private static final ResourceLocation FAUCET_CLAY = Util.getResource("faucet_clay");
	private static final ResourceLocation CHANNEL_CLAY = Util.getResource("channel_clay");

	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);

		if(Config.barrelEnabled) {
			ClientRegistry.bindTileEntitySpecialRenderer(TileBarrel.class, new BarrelRenderer());
		}
		if(Config.faucetEnabled) {
			ClientRegistry.bindTileEntitySpecialRenderer(TileFaucet.class, new FaucetRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileChannel.class, new ChannelRenderer());
		}
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		// color is handled by tinting
		ignoreProperty(BlockStained.COLOR, Ceramics.porcelain, Ceramics.porcelainBarrel, Ceramics.porcelainBarrelExtension);
		// separate all walls to their own file, and ignore all properties as we use multipart and it makes duplicate models otherwise
		ModelLoader.setCustomStateMapper(Ceramics.clayWall, new PropertyStateMapper("clay_wall", BlockClayWall.TYPE, true));

		// if not using porcelain, register the normal clay variants
		if(Config.porcelainEnabled) {
			registerItemModel(Ceramics.faucet);
			registerItemModel(Ceramics.channel);
			// ignore all propeties on the channel to reduce ram
			ModelLoader.setCustomStateMapper(Ceramics.channel, IgnoreAllStateMapper.INSTANCE);
		} else {
			registerItemModel(Ceramics.faucet, FAUCET_CLAY);
			registerItemModel(Ceramics.channel, CHANNEL_CLAY);
			ModelLoader.setCustomStateMapper(Ceramics.faucet, new RenameStateMapper(FAUCET_CLAY));
			ModelLoader.setCustomStateMapper(Ceramics.channel, new IgnoreAllStateMapper(CHANNEL_CLAY));
		}

		// base blocks
		registerItemModels(Ceramics.claySoft);
		registerItemModels(Ceramics.clayHard);
		registerItemModels(Ceramics.rainbowClay);
		registerItemModels(Ceramics.claySlab);
		registerItemModels(Ceramics.clayWall);

		// stairs
		registerItemModel(Ceramics.stairsPorcelainBricks);
		registerItemModel(Ceramics.stairsDarkBricks);
		registerItemModel(Ceramics.stairsGoldenBricks);
		registerItemModel(Ceramics.stairsMarineBricks);
		registerItemModel(Ceramics.stairsDragonBricks);
		registerItemModel(Ceramics.stairsLavaBricks);
		registerItemModel(Ceramics.stairsRainbowBricks);
		registerItemModel(Ceramics.stairsMonochromeBricks);

		// porcelain
		registerItemModel(Ceramics.porcelain);
		registerItemModel(Ceramics.porcelainBarrel);
		registerItemModel(Ceramics.porcelainBarrelExtension);

		// items
		registerItemModel(Ceramics.clayBucket);
		registerItemModel(Ceramics.clayBucketBlock);
		registerItemModel(Ceramics.clayShears);

		// loop through the special bucket types
		for(SpecialFluid fluid : SpecialFluid.values()) {
			if(fluid != SpecialFluid.EMPTY) {
				registerItemModel(Ceramics.clayBucket, fluid.getMeta(), fluid.getName());
			}
		}

		// armor
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
			registerItemModel(Ceramics.clayUnfired, type.getMeta(), type.getTextureName());
		}

		// barrels
		registerItemModel(Ceramics.clayBarrel, 0, "extension=false");
		registerItemModel(Ceramics.clayBarrel, 1, "extension=true");

		registerItemModel(Ceramics.clayBarrelUnfired, 0, "extension=false,type=clay");
		registerItemModel(Ceramics.clayBarrelUnfired, 1, "extension=true,type=clay");
		registerItemModel(Ceramics.clayBarrelUnfired, 2, "extension=false,type=porcelain");
		registerItemModel(Ceramics.clayBarrelUnfired, 3, "extension=true,type=porcelain");

		// barrel colors
		for(EnumDyeColor type : EnumDyeColor.values()) {
			registerItemModel(Ceramics.clayBarrelStained, type.getMetadata(), "color=" + type.getName());
			registerItemModel(Ceramics.clayBarrelStainedExtension, type.getMetadata(), "color=" + type.getName());
		}
	}

	@Override
	public void init() {
		final BlockColors blockColors = minecraft.getBlockColors();
		// porcelain colors
		Block[] blocks = {
				Ceramics.porcelain,
				Ceramics.porcelainBarrel,
				Ceramics.porcelainBarrelExtension
		};
		blockColors.registerBlockColorHandler(
				(state, access, pos, tintIndex) -> state.getValue(BlockStained.COLOR).getColor(),
				blocks);

		minecraft.getItemColors().registerItemColorHandler(
				(stack, tintIndex) -> {
					@SuppressWarnings("deprecation")
					IBlockState iblockstate = ((ItemBlock) stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata());
					return blockColors.colorMultiplier(iblockstate, null, null, tintIndex);
				},
				blocks);
	}


	/* Helper methods */

	private void registerItemModel(Item item) {
		if (item != null && item != Items.AIR) {
			registerItemModel(item, item.getRegistryName());
		}
	}

	private void registerItemModel(Block block) {
		registerItemModel(Item.getItemFromBlock(block));
	}

	private void registerItemModel(Block block, ResourceLocation location) {
		registerItemModel(Item.getItemFromBlock(block), location);
	}

	private void registerItemModel(Item item, final ResourceLocation location) {
		if(item != null && item != Items.AIR) {
			// so all meta get the item model
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
		if(item != null && item != Items.AIR) {
			// tell Minecraft which textures it has to load. This is resource-domain sensitive
			final ResourceLocation location = item.getRegistryName();

			// tell the game which model to use for this item-meta combination
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(location, name));
		}
	}

	private void registerItemModel(Block block, int meta, String name) {
		registerItemModel(Item.getItemFromBlock(block), meta, name);
	}

	private <B extends Block & IBlockEnum<T>, T extends Enum<T> & IStringSerializable & BlockEnumBase.IEnumMeta> void registerItemModels(B block) {
		registerItemModels(block, "");
	}

	private <T extends Enum<T> & IStringSerializable & BlockEnumBase.IEnumMeta> void registerItemModels(BlockEnumSlabBase<T> block) {
		registerItemModels(block, "half=bottom,");
	}

	private <B extends Block & IBlockEnum<T>, T extends Enum<T> & IStringSerializable & BlockEnumBase.IEnumMeta> void registerItemModels(B block, String prefix) {
		if(block != null && block != Blocks.AIR) {
			PropertyEnum<T> prop = block.getMappingProperty();
			for(T value : prop.getAllowedValues()) {
				registerItemModel(block, value.getMeta(), prefix + prop.getName() + "=" + value.getName());
			}
		}
	}

	private void ignoreProperty(IProperty<?> prop, Block ... blocks) {
		for(Block block : blocks) {
			if(block != null && block != Blocks.AIR) {
				ModelLoader.setCustomStateMapper(block, (new StateMap.Builder()).ignore(prop).build());
			}
		}
	}
}
