package knightminer.ceramics;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import knightminer.ceramics.blocks.BlockBarrel;
import knightminer.ceramics.blocks.BlockBarrelPorcelain;
import knightminer.ceramics.blocks.BlockBarrelStained;
import knightminer.ceramics.blocks.BlockBarrelUnfired;
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
import knightminer.ceramics.items.ItemBlockEnum;
import knightminer.ceramics.items.ItemBlockEnumSlab;
import knightminer.ceramics.items.ItemClayBucket;
import knightminer.ceramics.items.ItemClayShears;
import knightminer.ceramics.items.ItemClayUnfired;
import knightminer.ceramics.items.ItemClayUnfired.UnfiredType;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.library.CreativeTab;
import knightminer.ceramics.library.ModIDs;
import knightminer.ceramics.library.Util;
import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.plugin.bwm.BetterWithModsPlugin;
import knightminer.ceramics.tileentity.TileBarrel;
import knightminer.ceramics.tileentity.TileBarrelExtension;
import knightminer.ceramics.tileentity.TileFaucet;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemCloth;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
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
		"required-after:forge@[13.20.0.2289,);after:tconstruct@[1.11.2-2.7.0.27,);" )
public class Ceramics {
	public static final String name = "Ceramics";
	public static final String modID = "ceramics";
	public static final String version = "${version}";

	@SidedProxy(clientSide = "knightminer.ceramics.ClientProxy", serverSide = "knightminer.ceramics.CommonProxy")
	public static CommonProxy proxy;

	public static CreativeTab tab = new CreativeTab(modID, new ItemStack(Items.BRICK));

	public static final Logger log = LogManager.getLogger(modID);

	public static Block clayBarrel;
	public static Block clayBarrelUnfired;
	public static BlockClaySoft claySoft;
	public static BlockClayHard clayHard;
	public static BlockClaySlab claySlab;
	public static BlockClayWall clayWall;
	public static BlockClayRainbow rainbowClay;
	public static Block porcelainBarrel;
	public static Block porcelainBarrelExtension;
	public static Block clayBarrelStained;
	public static Block clayBarrelStainedExtension;
	public static BlockStained porcelain;

	public static Block stairsPorcelainBricks;
	public static Block stairsDarkBricks;
	public static Block stairsGoldenBricks;
	public static Block stairsMarineBricks;
	public static Block stairsDragonBricks;
	public static Block stairsLavaBricks;
	public static Block stairsRainbowBricks;

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

	// tic support
	public static Block faucet;

	static {
		FluidRegistry.enableUniversalBucket();
	}

	// makes oredict to int a bit easier in a couple other places
	public static final String[] dyes = {
			"White",
			"Orange",
			"Magenta",
			"LightBlue",
			"Yellow",
			"Lime",
			"Pink",
			"Gray",
			"LightGray",
			"Cyan",
			"Purple",
			"Blue",
			"Brown",
			"Green",
			"Red",
			"Black"
	};

	@SuppressWarnings({"unchecked", "rawtypes"})
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.load(event);

		// create armor materials
		clayArmor = EnumHelper.addArmorMaterial(Util.prefix("clay"), "cermamics:clay", 4,
				new int[]{1, 2, 3, 1}, 7, null, 0);
		clayArmor.repairMaterial = new ItemStack(Items.BRICK);
		clayArmorRaw = EnumHelper.addArmorMaterial(Util.prefix("clay_raw"), "cermamics:clay_raw", 1,
				new int[]{1, 1, 1, 1}, 0, null, 0);
		clayArmor.repairMaterial = new ItemStack(Items.CLAY_BALL);

		CeramicsNetwork.registerPackets();

		proxy.preInit();
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

			registerTE(TileBarrel.class, "barrel");
			registerTE(TileBarrelExtension.class, "barrel_extension");

			// faucet
			faucet = registerBlock(r, new BlockFaucet(), "faucet");
			registerTE(TileFaucet.class, "faucet");
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

			// base materials
			registerItemBlock(r, new ItemBlockEnum(clayHard));
			registerItemBlock(r, new ItemBlockEnum(claySoft));
			registerItemBlock(r, new ItemBlockEnum(clayWall));
			registerItemBlock(r, new ItemBlockEnumSlab(claySlab));

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
		}

		// bucket
		if(Config.bucketEnabled) {
			// fire buckets
			GameRegistry.addSmelting(new ItemStack(clayUnfired, 1, UnfiredType.BUCKET.getMeta()), new ItemStack(clayBucket), 0.5f);

			// register lava bucket as a fuel
			GameRegistry.registerFuelHandler((fuel) -> {
				if(fuel != null && fuel.getItem() == clayBucket) {
					FluidStack fluid = clayBucket.getFluid(fuel);
					if(fluid != null && fluid.getFluid() == FluidRegistry.LAVA) {
						return 20000;
					}
				}
				return 0;
			});
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

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if(Config.bucketEnabled) {
			MinecraftForge.EVENT_BUS.register(clayBucket);
		}
		if(Config.shearsEnabled) {
			MinecraftForge.EVENT_BUS.register(clayShears);
		}

		// add recipes using all stacks in the oredictionary for bricks
		if(Config.porcelainEnabled) {
			List<ItemStack> porcelains = OreDictionary.getOres("clayPorcelain", false);
			for(ItemStack porcelain : porcelains) {
				ItemStack brick = new ItemStack(clayUnfired, 1, UnfiredType.PORCELAIN_BRICK.getMeta());
				GameRegistry.addSmelting(porcelain, brick, 0.1f);
			}
		}
		// load plugins
		/*
		if(Loader.isModLoaded(ModIDs.TINKERS)) {
			TConstructPlugin.postInit();
		}*/
	}


	/* Blocks, items, and TEs */

	public static <T extends Block> T registerBlock(IForgeRegistry<Block> registry, T block, String name) {
		block.setUnlocalizedName(Util.prefix(name));
		block.setRegistryName(Util.getResource(name));

		registry.register(block);

		return block;
	}

	protected static <E extends Enum<E> & BlockEnumBase.IEnumMeta & IStringSerializable> BlockStairsEnum registerStairsFrom(IForgeRegistry<Block> registry, BlockEnumBase<E> block, E value, String name) {
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

	@SuppressWarnings("unchecked")
	public static <T extends Item> T registerItem(IForgeRegistry<Item> registry, T block, String name) {
		block.setUnlocalizedName(Util.prefix(name));
		block.setRegistryName(Util.getResource(name));
		registry.register(block);

		return block;
	}


	protected static void registerTE(Class<? extends TileEntity> teClazz, String name) {
		GameRegistry.registerTileEntity(teClazz, Util.prefix(name));
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
