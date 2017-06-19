package knightminer.ceramics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import knightminer.ceramics.blocks.BlockBarrel;
import knightminer.ceramics.blocks.BlockBarrelStained;
import knightminer.ceramics.blocks.BlockBarrelUnfired;
import knightminer.ceramics.blocks.BlockClayHard;
import knightminer.ceramics.blocks.BlockClayHard.ClayTypeHard;
import knightminer.ceramics.blocks.BlockClayRainbow;
import knightminer.ceramics.blocks.BlockClayRainbow.RainbowStart;
import knightminer.ceramics.blocks.BlockClaySlab;
import knightminer.ceramics.blocks.BlockClaySoft;
import knightminer.ceramics.blocks.BlockClaySoft.ClayTypeSoft;
import knightminer.ceramics.blocks.BlockClayWall;
import knightminer.ceramics.blocks.BlockClayWall.ClayWallType;
import knightminer.ceramics.blocks.BlockEnumBase;
import knightminer.ceramics.blocks.BlockStained;
import knightminer.ceramics.blocks.BlockStained.StainedColor;
import knightminer.ceramics.blocks.BlockStairsBase;
import knightminer.ceramics.blocks.IBlockEnum;
import knightminer.ceramics.items.ItemArmorClay;
import knightminer.ceramics.items.ItemArmorClayRaw;
import knightminer.ceramics.items.ItemBlockBarrel;
import knightminer.ceramics.items.ItemBlockEnum;
import knightminer.ceramics.items.ItemBlockEnumSlab;
import knightminer.ceramics.items.ItemClayBucket;
import knightminer.ceramics.items.ItemClayBucket.SpecialFluid;
import knightminer.ceramics.items.ItemClayShears;
import knightminer.ceramics.items.ItemClayUnfired;
import knightminer.ceramics.items.ItemClayUnfired.UnfiredType;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.library.CreativeTab;
import knightminer.ceramics.library.ModIDs;
import knightminer.ceramics.library.Util;
import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.plugin.bwm.BetterWithModsPlugin;
//import knightminer.ceramics.plugin.tconstruct.TConstructPlugin;
import knightminer.ceramics.tileentity.TileBarrel;
import knightminer.ceramics.tileentity.TileBarrelExtension;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.init.Blocks;
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
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

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
	public static Block porcelain;

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
	public static Block porcelainFaucet;

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
		// generic materials
		clayUnfired = registerItem(new ItemClayUnfired(), "unfired_clay");
		clayHard = registerBlock(new ItemBlockEnum(new BlockClayHard()), "clay_hard");
		claySlab = registerBlock(new ItemBlockEnumSlab(new BlockClaySlab()), "clay_slab");
		clayWall = registerBlock(new ItemBlockEnum(new BlockClayWall()), "clay_wall");

		// porcelain
		if(Config.porcelainEnabled) {
			// not technically porcelain as I may add other soft blocks later, but for now
			claySoft = registerBlock(new ItemBlockEnum(new BlockClaySoft()), "clay_soft");
			porcelain = registerBlock(new ItemBlockEnum(new BlockStained()), "porcelain");
			stairsPorcelainBricks = registerBlockStairsFrom(clayHard, ClayTypeHard.PORCELAIN_BRICKS, "porcelain_bricks_stairs");

			// for other mods adding porcelain
			oredict(clayUnfired, UnfiredType.PORCELAIN.getMeta(), "clayPorcelain");
		}

		// fancy brick stairs, slabs and main blocks done above
		if(Config.fancyBricksEnabled) {
			stairsDarkBricks = registerBlockStairsFrom(clayHard, ClayTypeHard.DARK_BRICKS, "dark_bricks_stairs");
			stairsGoldenBricks = registerBlockStairsFrom(clayHard, ClayTypeHard.GOLDEN_BRICKS, "golden_bricks_stairs");
			stairsMarineBricks = registerBlockStairsFrom(clayHard, ClayTypeHard.MARINE_BRICKS, "marine_bricks_stairs");
			stairsDragonBricks = registerBlockStairsFrom(clayHard, ClayTypeHard.DRAGON_BRICKS, "dragon_bricks_stairs");
			stairsLavaBricks = registerBlockStairsFrom(clayHard, ClayTypeHard.LAVA_BRICKS, "lava_bricks_stairs");
		}

		// animated rainbow colors
		if(Config.rainbowClayEnabled) {
			rainbowClay = registerBlock(new ItemBlockEnum(new BlockClayRainbow()), "rainbow_clay");
			stairsRainbowBricks = registerBlockStairsFrom(clayHard, ClayTypeHard.RAINBOW_BRICKS, "rainbow_bricks_stairs");
		}

		// bucket
		if(Config.bucketEnabled) {
			clayBucket = registerItem(new ItemClayBucket(), "clay_bucket");
			tab.setIcon(new ItemStack(clayBucket));
		}

		// shears
		if(Config.shearsEnabled) {
			clayShears = registerItem(new ItemClayShears(), "clay_shears");
		}

		// armor
		if(Config.armorEnabled) {
			clayArmor = EnumHelper.addArmorMaterial(Util.prefix("clay"), "cermamics:clay", 4, new int[]{1, 2, 3, 1}, 7,
					null, 0);
			clayArmor.repairMaterial = new ItemStack(Items.BRICK);
			clayHelmet = registerItem(new ItemArmorClay(EntityEquipmentSlot.HEAD), "clay_helmet");
			clayChestplate = registerItem(new ItemArmorClay(EntityEquipmentSlot.CHEST), "clay_chestplate");
			clayLeggings = registerItem(new ItemArmorClay(EntityEquipmentSlot.LEGS), "clay_leggings");
			clayBoots = registerItem(new ItemArmorClay(EntityEquipmentSlot.FEET), "clay_boots");

			clayArmorRaw = EnumHelper.addArmorMaterial(Util.prefix("clay_raw"), "cermamics:clay_raw", 1,
					new int[]{1, 1, 1, 1}, 0, null, 0);
			clayArmor.repairMaterial = new ItemStack(Items.CLAY_BALL);
			clayHelmetRaw = registerItem(new ItemArmorClayRaw(EntityEquipmentSlot.HEAD), "clay_helmet_raw");
			clayChestplateRaw = registerItem(new ItemArmorClayRaw(EntityEquipmentSlot.CHEST), "clay_chestplate_raw");
			clayLeggingsRaw = registerItem(new ItemArmorClayRaw(EntityEquipmentSlot.LEGS), "clay_leggings_raw");
			clayBootsRaw = registerItem(new ItemArmorClayRaw(EntityEquipmentSlot.FEET), "clay_boots_raw");

			oredict(clayUnfired, UnfiredType.CLAY_PLATE_RAW.getMeta(), "plateClayRaw");
			oredict(clayUnfired, UnfiredType.CLAY_PLATE.getMeta(), "plateClay", "plateBrick"); // plateBrick is because bricks are ingotBrick
		}

		// barrels
		if(Config.barrelEnabled) {
			clayBarrelUnfired = registerBlock(new ItemBlockBarrel(new BlockBarrelUnfired(), new String[] {"clay", "clay_extension", "porcelain", "porcelain_extension"}), "clay_barrel_unfired");
			clayBarrel = registerBlock(new ItemBlockBarrel(new BlockBarrel(), new String[] {"barrel", "barrel_extension"}), "clay_barrel");
			clayBarrelStained = registerBlock(new ItemCloth(new BlockBarrelStained(false)), "clay_barrel_stained");
			clayBarrelStainedExtension = registerBlock(new ItemCloth(new BlockBarrelStained(true)), "clay_barrel_stained_extension");

			if(Config.porcelainEnabled) {
				porcelainBarrel = registerBlock(new ItemCloth(new BlockBarrelStained(false)), "porcelain_barrel");
				porcelainBarrelExtension = registerBlock(new ItemCloth(new BlockBarrelStained(true)), "porcelain_barrel_extension");
			}

			registerTE(TileBarrel.class, "barrel");
			registerTE(TileBarrelExtension.class, "barrel_extension");
		}

		// load plugins
		/*
		if(Loader.isModLoaded(ModIDs.TINKERS)) {
			TConstructPlugin.preInit();
		}*/

		CeramicsNetwork.registerPackets();

		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// porcelain
		if(Config.porcelainEnabled) {
			// smelt raw porcelain blocks into porcelain
			GameRegistry.addSmelting(new ItemStack(claySoft, 1, ClayTypeSoft.PORCELAIN.getMeta()),
					new ItemStack(porcelain, 1, EnumDyeColor.WHITE.getMetadata()), 0.1f);
		}

		// bucket
		if(Config.bucketEnabled) {
			// fire buckets
			GameRegistry.addSmelting(new ItemStack(clayUnfired, 1, UnfiredType.BUCKET.getMeta()), new ItemStack(clayBucket), 0.5f);

			// register lava bucket as a fuel
			GameRegistry.registerFuelHandler(new IFuelHandler() {
				@Override
				public int getBurnTime(ItemStack fuel) {
					if(fuel != null && fuel.getItem() == clayBucket) {
						FluidStack fluid = clayBucket.getFluid(fuel);
						if(fluid != null && fluid.getFluid() == FluidRegistry.LAVA) {
							return 20000;
						}
					}
					return 0;
				}
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
		/*
		if(Loader.isModLoaded(ModIDs.TINKERS)) {
			TConstructPlugin.init();
		}*/
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

	@SuppressWarnings("unchecked")
	public static <T extends Block> T registerBlock(ItemBlock item, String name) {
		Block block = item.getBlock();

		block.setUnlocalizedName(Util.prefix(name));
		block.setRegistryName(Util.getResource(name));
		GameRegistry.register(block);

		registerItem(item, name);

		return (T) block;
	}

	protected static <E extends Enum<E> & BlockEnumBase.IEnumMeta & IStringSerializable> BlockStairsBase registerBlockStairsFrom(BlockEnumBase<E> block, E value, String name) {
		return registerBlock(new ItemBlock(new BlockStairsBase(block.getDefaultState().withProperty(block.getMappingProperty(), value))), name);
	}

	private static <T extends Item> T registerItem(T item, String name) {
		item.setUnlocalizedName(Util.prefix(name));
		item.setRegistryName(Util.getResource(name));
		GameRegistry.register(item);

		return item;
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
