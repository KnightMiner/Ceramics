package knightminer.ceramics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import knightminer.ceramics.blocks.BlockBarrel;
import knightminer.ceramics.blocks.BlockBarrelPorcelain;
import knightminer.ceramics.blocks.BlockBarrelStained;
import knightminer.ceramics.blocks.BlockBarrelUnfired;
import knightminer.ceramics.blocks.BlockChannel;
import knightminer.ceramics.blocks.BlockClayBucket;
import knightminer.ceramics.blocks.BlockClayHard;
import knightminer.ceramics.blocks.BlockClayHard.ClayTypeHard;
import knightminer.ceramics.blocks.BlockClayRainbow;
import knightminer.ceramics.blocks.BlockClaySlab;
import knightminer.ceramics.blocks.BlockClaySoft;
import knightminer.ceramics.blocks.BlockClaySoft.ClayTypeSoft;
import knightminer.ceramics.blocks.BlockClayWall;
import knightminer.ceramics.blocks.BlockEnumBase;
import knightminer.ceramics.blocks.BlockFaucet;
import knightminer.ceramics.blocks.BlockStained;
import knightminer.ceramics.blocks.BlockStairsEnum;
import knightminer.ceramics.items.ItemArmorClay;
import knightminer.ceramics.items.ItemArmorClayRaw;
import knightminer.ceramics.items.ItemBlockBarrel;
import knightminer.ceramics.items.ItemBlockChannel;
import knightminer.ceramics.items.ItemBlockEnum;
import knightminer.ceramics.items.ItemBlockEnumSlab;
import knightminer.ceramics.items.ItemClayBucket;
import knightminer.ceramics.items.ItemClayShears;
import knightminer.ceramics.items.ItemClayUnfired;
import knightminer.ceramics.items.ItemClayUnfired.UnfiredType;
import knightminer.ceramics.legacy.TileEntityRenamer;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.library.CreativeTab;
import knightminer.ceramics.library.ModIDs;
import knightminer.ceramics.library.Util;
import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.plugin.bwm.BetterWithModsPlugin;
import knightminer.ceramics.plugin.tconstruct.TConstructPlugin;
import knightminer.ceramics.tileentity.TileBarrel;
import knightminer.ceramics.tileentity.TileBarrelExtension;
import knightminer.ceramics.tileentity.TileChannel;
import knightminer.ceramics.tileentity.TileFaucet;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemCloth;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

@Mod(modid = Ceramics.modID, version = Ceramics.version, name = Ceramics.name, dependencies =
		"required-after:forge@[14.21.1.2394,);after:tconstruct" )
public class Ceramics {
	public static final String name = "Ceramics";
	public static final String modID = "ceramics";
	public static final String version = "${version}";

	@SidedProxy(clientSide = "knightminer.ceramics.ClientProxy", serverSide = "knightminer.ceramics.CommonProxy")
	public static CommonProxy proxy;

	public static CreativeTab tab = new CreativeTab(modID, new ItemStack(Items.BRICK));

	public static final Logger log = LogManager.getLogger(modID);

	// functional
	public static Block clayBarrel;
	public static Block porcelainBarrel;
	public static Block porcelainBarrelExtension;
	public static Block clayBarrelStained;
	public static Block clayBarrelStainedExtension;
	public static Block faucet;
	public static Block channel;

	// materials
	public static Block clayBarrelUnfired;
	public static BlockClaySoft claySoft;
	public static Block clayBucketBlock;

	// building blocks
	public static BlockClayHard clayHard;
	public static BlockClaySlab claySlab;
	public static BlockClayWall clayWall;
	public static BlockStained porcelain;
	public static BlockClayRainbow rainbowClay;

	// stairs
	public static Block stairsPorcelainBricks;
	public static Block stairsDarkBricks;
	public static Block stairsGoldenBricks;
	public static Block stairsMarineBricks;
	public static Block stairsDragonBricks;
	public static Block stairsLavaBricks;
	public static Block stairsRainbowBricks;

	// items
	public static Item clayUnfired;
	public static ItemClayBucket clayBucket;
	public static Item clayShears;

	// armor
	public static ArmorMaterial clayArmor;
	public static Item clayHelmet;
	public static Item clayChestplate;
	public static Item clayLeggings;
	public static Item clayBoots;

	public static ArmorMaterial clayArmorRaw;
	public static Item clayHelmetRaw;
	public static Item clayChestplateRaw;
	public static Item clayLeggingsRaw;
	public static Item clayBootsRaw;

	static {
		FluidRegistry.enableUniversalBucket();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.load(event);

		// create armor materials
		clayArmor = EnumHelper.addArmorMaterial(Util.prefix("clay"), "cermamics:clay", 4,
				new int[]{1, 2, 3, 1}, 7, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0);
		clayArmor.repairMaterial = new ItemStack(Items.BRICK);
		clayArmorRaw = EnumHelper.addArmorMaterial(Util.prefix("clay_raw"), "cermamics:clay_raw", 1,
				new int[]{1, 1, 1, 1}, 0, SoundEvents.BLOCK_GRAVEL_PLACE, 0);
		clayArmor.repairMaterial = new ItemStack(Items.CLAY_BALL);

		CeramicsNetwork.registerPackets();

		proxy.preInit();

		// fix tile entities registering under the minecraft domain
		ModFixs fixer = FMLCommonHandler.instance().getDataFixer().init(modID, 1);
		fixer.registerFix(FixTypes.BLOCK_ENTITY, new TileEntityRenamer());
	}

	@Mod.EventBusSubscriber(modid=modID)
	public static class Registration {
		@SubscribeEvent
		public static void registerBlocks(RegistryEvent.Register<Block> event) {
			IForgeRegistry<Block> r = event.getRegistry();

			// core blocks
			clayHard = registerBlock(r, new BlockClayHard(), "clay_hard");
			claySoft = registerBlock(r, new BlockClaySoft(), "clay_soft");
			claySlab = registerBlock(r, new BlockClaySlab(), "clay_slab");
			clayWall = registerBlock(r, new BlockClayWall(), "clay_wall");

			// porcelain
			porcelain = registerBlock(r, new BlockStained(), "porcelain");
			stairsPorcelainBricks = registerStairsFrom(r, clayHard, ClayTypeHard.PORCELAIN_BRICKS, "porcelain_bricks_stairs");

			// rainbow clay
			rainbowClay = registerBlock(r, new BlockClayRainbow(), "rainbow_clay");
			stairsRainbowBricks = registerStairsFrom(r, clayHard, ClayTypeHard.RAINBOW_BRICKS, "rainbow_bricks_stairs");

			// fancy bricks
			stairsDarkBricks = registerStairsFrom(r, clayHard, ClayTypeHard.DARK_BRICKS, "dark_bricks_stairs");
			stairsGoldenBricks = registerStairsFrom(r, clayHard, ClayTypeHard.GOLDEN_BRICKS, "golden_bricks_stairs");
			stairsMarineBricks = registerStairsFrom(r, clayHard, ClayTypeHard.MARINE_BRICKS, "marine_bricks_stairs");
			stairsDragonBricks = registerStairsFrom(r, clayHard, ClayTypeHard.DRAGON_BRICKS, "dragon_bricks_stairs");
			stairsLavaBricks = registerStairsFrom(r, clayHard, ClayTypeHard.LAVA_BRICKS, "lava_bricks_stairs");

			// barrels
			clayBarrelUnfired = registerBlock(r, new BlockBarrelUnfired(), "clay_barrel_unfired");
			clayBarrel = registerBlock(r, new BlockBarrel(), "clay_barrel");
			clayBarrelStained = registerBlock(r, new BlockBarrelStained(false), "clay_barrel_stained");
			clayBarrelStainedExtension = registerBlock(r, new BlockBarrelStained(true), "clay_barrel_stained_extension");
			porcelainBarrel = registerBlock(r, new BlockBarrelPorcelain(false), "porcelain_barrel");
			porcelainBarrelExtension = registerBlock(r, new BlockBarrelPorcelain(true), "porcelain_barrel_extension");

			// special bucket
			clayBucketBlock = registerBlock(r, new BlockClayBucket(), "clay_bucket_block");

			registerTE(TileBarrel.class, "barrel");
			registerTE(TileBarrelExtension.class, "barrel_extension");

			// faucet
			faucet = registerBlock(r, new BlockFaucet(), "faucet");
			registerTE(TileFaucet.class, "faucet");

			// channel
			channel = registerBlock(r, new BlockChannel(), "channel");
			registerTE(TileChannel.class, "channel");
		}

		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event) {
			IForgeRegistry<Item> r = event.getRegistry();

			// generic materials
			clayUnfired = registerItem(r, new ItemClayUnfired(), "unfired_clay");

			// bucket
			clayBucket = registerItem(r, new ItemClayBucket(), "clay_bucket");
			tab.setIcon(new ItemStack(clayBucket));

			// shears
			clayShears = registerItem(r, new ItemClayShears(), "clay_shears");

			// armor
			clayHelmet = registerItem(r, new ItemArmorClay(EntityEquipmentSlot.HEAD), "clay_helmet");
			clayChestplate = registerItem(r, new ItemArmorClay(EntityEquipmentSlot.CHEST), "clay_chestplate");
			clayLeggings = registerItem(r, new ItemArmorClay(EntityEquipmentSlot.LEGS), "clay_leggings");
			clayBoots = registerItem(r, new ItemArmorClay(EntityEquipmentSlot.FEET), "clay_boots");

			clayHelmetRaw = registerItem(r, new ItemArmorClayRaw(EntityEquipmentSlot.HEAD), "clay_helmet_raw");
			clayChestplateRaw = registerItem(r, new ItemArmorClayRaw(EntityEquipmentSlot.CHEST), "clay_chestplate_raw");
			clayLeggingsRaw = registerItem(r, new ItemArmorClayRaw(EntityEquipmentSlot.LEGS), "clay_leggings_raw");
			clayBootsRaw = registerItem(r, new ItemArmorClayRaw(EntityEquipmentSlot.FEET), "clay_boots_raw");


			//// item blocks ////

			// bucket
			ItemBlock clayBucket = new ItemBlock(clayBucketBlock);
			clayBucket.setMaxStackSize(16);
			registerItemBlock(r, clayBucket);

			// base materials
			registerItemBlock(r, new ItemBlockEnum(clayHard));
			registerItemBlock(r, new ItemBlockEnum(claySoft));
			registerItemBlock(r, new ItemBlockEnum(clayWall));
			registerItemBlock(r, new ItemBlockEnumSlab<>(claySlab));

			// porcelain
			registerItemBlock(r, new ItemBlockEnum(porcelain));
			registerItemBlock(r, stairsPorcelainBricks);

			// rainbow clay
			registerItemBlock(r, new ItemBlockEnum(rainbowClay));
			registerItemBlock(r, stairsRainbowBricks);

			// fancy bricks
			registerItemBlock(r, stairsDarkBricks);
			registerItemBlock(r, stairsGoldenBricks);
			registerItemBlock(r, stairsMarineBricks);
			registerItemBlock(r, stairsDragonBricks);
			registerItemBlock(r, stairsLavaBricks);

			// barrels
			registerItemBlock(r, new ItemBlockBarrel(clayBarrelUnfired, "clay", "clay_extension", "porcelain", "porcelain_extension"));
			registerItemBlock(r, new ItemBlockBarrel(clayBarrel, "barrel", "barrel_extension"));
			registerItemBlock(r, new ItemCloth(clayBarrelStained));
			registerItemBlock(r, new ItemCloth(clayBarrelStainedExtension));
			registerItemBlock(r, new ItemCloth(porcelainBarrel));
			registerItemBlock(r, new ItemCloth(porcelainBarrelExtension));

			// faucet
			registerItemBlock(r, faucet);
			registerItemBlock(r, new ItemBlockChannel(channel));
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		oredict(clayUnfired, UnfiredType.CLAY_PLATE_RAW.getMeta(), "plateClayRaw");
		oredict(clayUnfired, UnfiredType.CLAY_PLATE.getMeta(), "plateClay", "plateBrick"); // plateBrick is because bricks are ingotBrick

		// porcelain
		if(Config.porcelainEnabled) {
			oredict(clayUnfired, UnfiredType.PORCELAIN.getMeta(), "clayPorcelain");

			// smelt raw porcelain blocks into porcelain
			GameRegistry.addSmelting(new ItemStack(claySoft, 1, ClayTypeSoft.PORCELAIN.getMeta()),
					new ItemStack(porcelain, 1, EnumDyeColor.WHITE.getMetadata()), 0.1f);

			// if enabled, register all porcelain smelting from the oredict, and add an event for later registrations
			if(Config.porcelainOredictSmelting) {
				for(ItemStack porcelain : OreDictionary.getOres("clayPorcelain")) {
					GameRegistry.addSmelting(porcelain, new ItemStack(clayUnfired, 1, UnfiredType.PORCELAIN_BRICK.getMeta()), 0.1f);
				}
				MinecraftForge.EVENT_BUS.register(FurnaceOredictRecipeHandler.class);
			} else {
				// otherwise just add a static recipe
				GameRegistry.addSmelting(new ItemStack(clayUnfired, 1, UnfiredType.PORCELAIN.getMeta()),
						new ItemStack(clayUnfired, 1, UnfiredType.PORCELAIN_BRICK.getMeta()), 0.1f);
			}
		}

		// bucket
		if(Config.bucketEnabled) {
			// fire buckets, ensure we use the right one
			ItemStack unfiredBucket = Config.placeClayBucket ? new ItemStack(clayBucketBlock) : new ItemStack(clayUnfired, 1, UnfiredType.BUCKET.getMeta());

			GameRegistry.addSmelting(unfiredBucket, new ItemStack(clayBucket), 0.5f);
		}

		// shears
		if(Config.shearsEnabled) {
			// fire shears
			GameRegistry.addSmelting(new ItemStack(clayUnfired, 1, UnfiredType.SHEARS.getMeta()), new ItemStack(clayShears), 0.5f);
		}

		// armor
		if(Config.armorEnabled) {
			// fire the plates
			GameRegistry.addSmelting(new ItemStack(clayUnfired, 1, UnfiredType.CLAY_PLATE_RAW.getMeta()),
					new ItemStack(clayUnfired, 1, UnfiredType.CLAY_PLATE.getMeta()), 0.5f);
		}

		// barrels
		if(Config.barrelEnabled) {
			// fire the barrels
			GameRegistry.addSmelting(new ItemStack(clayBarrelUnfired, 1, 0), new ItemStack(clayBarrel, 1, 0), 0.5f);
			GameRegistry.addSmelting(new ItemStack(clayBarrelUnfired, 1, 1), new ItemStack(clayBarrel, 1, 1), 0.5f);

			// fire barrels made of porcelain
			if(Config.porcelainEnabled) {
				GameRegistry.addSmelting(new ItemStack(clayBarrelUnfired, 1, 2), new ItemStack(porcelainBarrel, 1, 0), 0.5f);
				GameRegistry.addSmelting(new ItemStack(clayBarrelUnfired, 1, 3), new ItemStack(porcelainBarrelExtension, 1, 0), 0.5f);
			}
		}

		// load plugins
		if(Loader.isModLoaded(ModIDs.BWM)) {
			BetterWithModsPlugin.init();
		}

		proxy.init();
	}

	/**
	 * Event handler for porcelain oredicted after init to get furnace recipes.
	 * Used since adding recipes during postInit messes with mod compatibility and recipes cannot be gaurneteed by init
	 */
	public static class FurnaceOredictRecipeHandler {
		@SubscribeEvent
		public static void onOredictRegister(OreDictionary.OreRegisterEvent event) {
			// if enabled and someone just oredicted a porcelain, add a furnace recipe
			if("clayPorcelain".equals(event.getName())) {
				GameRegistry.addSmelting(event.getOre(), new ItemStack(clayUnfired, 1, UnfiredType.PORCELAIN_BRICK.getMeta()), 0.1f);
			}
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if(Config.bucketEnabled) {
			MinecraftForge.EVENT_BUS.register(clayBucket);
		}
		if(Config.shearsEnabled) {
			MinecraftForge.EVENT_BUS.register(clayShears);
		}

		// load plugins
		if(Loader.isModLoaded(ModIDs.TINKERS)) {
			TConstructPlugin.postInit();
		}
	}


	/* Blocks, items, and TEs */

	public static <T extends Block> T registerBlock(IForgeRegistry<Block> registry, T block, String name) {
		block.setUnlocalizedName(Util.prefix(name));
		block.setRegistryName(Util.getResource(name));

		registry.register(block);

		return block;
	}

	protected static <E extends Enum<E> & BlockEnumBase.IEnumMeta & IStringSerializable> BlockStairsEnum<E> registerStairsFrom(IForgeRegistry<Block> registry, BlockEnumBase<E> block, E value, String name) {
		return registerBlock(registry, new BlockStairsEnum<>(block, value), name);
	}

	public static ItemBlock registerItemBlock(IForgeRegistry<Item> registry, Block block) {
		return registerItemBlock(registry, new ItemBlock(block));
	}

	public static <T extends ItemBlock> T registerItemBlock(IForgeRegistry<Item> registry, T item) {
		// grab names from the existing block
		Block block = item.getBlock();
		item.setUnlocalizedName(block.getUnlocalizedName());
		item.setRegistryName(block.getRegistryName());

		registry.register(item);

		return item;
	}

	public static <T extends Item> T registerItem(IForgeRegistry<Item> registry, T block, String name) {
		block.setUnlocalizedName(Util.prefix(name));
		block.setRegistryName(Util.getResource(name));
		registry.register(block);

		return block;
	}


	protected static void registerTE(Class<? extends TileEntity> teClazz, String name) {
		GameRegistry.registerTileEntity(teClazz, Util.resource(name));
	}


	/* Oredictionary */

	public static void oredict(Item item, String... name) {
		oredict(item, OreDictionary.WILDCARD_VALUE, name);
	}

	public static void oredict(Block block, String... name) {
		oredict(block, OreDictionary.WILDCARD_VALUE, name);
	}

	public static void oredict(Item item, int meta, String... name) {
		oredict(new ItemStack(item, 1, meta), name);
	}

	public static void oredict(Block block, int meta, String... name) {
		oredict(new ItemStack(block, 1, meta), name);
	}

	public static void oredict(ItemStack stack, String... names) {
		if(stack != null && stack.getItem() != null) {
			for(String name : names) {
				OreDictionary.registerOre(name, stack);
			}
		}
	}
}
