package knightminer.ceramics;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import knightminer.ceramics.blocks.BlockBarrel;
import knightminer.ceramics.blocks.BlockBarrelStained;
import knightminer.ceramics.blocks.BlockClayHard;
import knightminer.ceramics.blocks.BlockClayHard.ClayTypeHard;
import knightminer.ceramics.blocks.BlockClaySlab;
import knightminer.ceramics.blocks.BlockClaySoft;
import knightminer.ceramics.blocks.BlockClaySoft.ClayTypeSoft;
import knightminer.ceramics.blocks.BlockClayWall;
import knightminer.ceramics.blocks.BlockClayWall.ClayWallType;
import knightminer.ceramics.blocks.BlockEnumBase;
import knightminer.ceramics.blocks.BlockStained;
import knightminer.ceramics.blocks.BlockStairsBase;
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
import knightminer.ceramics.plugin.tconstruct.TConstructPlugin;
import knightminer.ceramics.tileentity.TileBarrel;
import knightminer.ceramics.tileentity.TileBarrelExtension;
import net.minecraft.block.Block;
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
		"required-after:forge@[13.20.0.2289,);after:tconstruct@[1.11.2-2.7.0,);" )
public class Ceramics {
	public static final String name = "Ceramics";
	public static final String modID = "ceramics";
	public static final String version = "${version}";

	@SidedProxy(clientSide = "knightminer.ceramics.ClientProxy", serverSide = "knightminer.ceramics.CommonProxy")
	public static CommonProxy proxy;

	public static CreativeTab tab = new CreativeTab(modID, new ItemStack(Items.BRICK));

	public static final Logger log = LogManager.getLogger(modID);

	public static Block clayBarrel;
	public static BlockClaySoft claySoft;
	public static BlockClayHard clayHard;
	public static BlockClaySlab claySlab;
	public static BlockClayWall clayWall;
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
			clayBarrel = registerBlock(new ItemBlockBarrel(new BlockBarrel()), "clay_barrel");
			clayBarrelStained = registerBlock(new ItemCloth(new BlockBarrelStained(false)), "clay_barrel_stained");
			clayBarrelStainedExtension = registerBlock(new ItemCloth(new BlockBarrelStained(true)), "clay_barrel_stained_extension");

			if(Config.porcelainEnabled) {
				porcelainBarrel = registerBlock(new ItemCloth(new BlockBarrelStained(false)), "porcelain_barrel");
				porcelainBarrelExtension = registerBlock(new ItemCloth(new BlockBarrelStained(true)), "porcelain_barrel_extension");
			}

			registerTE(TileBarrel.class, "barrel");
			registerTE(TileBarrelExtension.class, "barrel_extension");

			// so we can use any type
			oredict(clayBarrel, 0, "barrelClay");
			oredict(clayBarrelStained, "barrelClay");
			oredict(clayBarrel, 1, "barrelExtensionClay");
			oredict(clayBarrelStainedExtension, "barrelExtensionClay");

			oredict(porcelainBarrel, "barrelPorcelain");
			oredict(porcelainBarrelExtension, "barrelExtensionPorcelain");

		}

		// load plugins
		if(Loader.isModLoaded(ModIDs.TINKERS)) {
			TConstructPlugin.preInit();
		}

		CeramicsNetwork.registerPackets();

		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// porcelain
		if(Config.porcelainEnabled) {
			ItemStack porcelainItem = new ItemStack(clayUnfired, 1, UnfiredType.PORCELAIN.getMeta());
			ItemStack brick = new ItemStack(clayUnfired, 1, UnfiredType.PORCELAIN_BRICK.getMeta());
			ItemStack boneMeal = new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage());
			ItemStack block = new ItemStack(claySoft, 1, ClayTypeSoft.PORCELAIN.getMeta());
			ItemStack blockHard = new ItemStack(porcelain, 1, EnumDyeColor.WHITE.getMetadata());
			ItemStack brickBlock = new ItemStack(clayHard, 1, ClayTypeHard.PORCELAIN_BRICKS.getMeta());

			// basic recipe: bone meal with clay
			GameRegistry.addShapelessRecipe(porcelainItem, Items.CLAY_BALL, boneMeal);
			// alt recipe: quartz
			ItemStack porcelainAlt = porcelainItem.copy();
			porcelainAlt.setCount(2);
			GameRegistry.addShapelessRecipe(porcelainAlt, Items.CLAY_BALL, Items.CLAY_BALL, Items.QUARTZ);

			// block crafting
			ItemStack porcelainAlt2 = porcelainItem.copy();
			porcelainAlt2.setCount(4);
			GameRegistry.addRecipe(new ShapedOreRecipe(block, "CC", "CC", 'C', "clayPorcelain"));
			GameRegistry.addShapelessRecipe(porcelainAlt2, block.copy());
			GameRegistry.addRecipe(brickBlock, "CC", "CC", 'C', brick);

			// bricks
			GameRegistry.addSmelting(block.copy(), blockHard, 0.1f);
			// slabs and stairs
			addSlabRecipe(claySlab, ClayTypeHard.PORCELAIN_BRICKS.getMeta(), brickBlock);
			addWallRecipe(clayWall, ClayWallType.PORCELAIN_BRICKS.getMeta(), brickBlock);
			addStairRecipe(stairsPorcelainBricks, brickBlock);

			// brick recipe
			// handled using oredict in postInit
			//GameRegistry.addSmelting(porcelainItem.copy(), brick.copy(), 0.1f);

			for(EnumDyeColor color : EnumDyeColor.values()) {
				ItemStack dyed = new ItemStack(porcelain, 8, color.getMetadata());
				ItemStack dye = new ItemStack(Items.DYE, 1, color.getDyeDamage());

				GameRegistry.addRecipe(dyed, "ccc", "cdc", "ccc", 'd', dye, 'c', new ItemStack(porcelain, 1, OreDictionary.WILDCARD_VALUE));
			}
		}

		// bucket
		if(Config.bucketEnabled) {
			ItemStack raw = new ItemStack(clayUnfired, 1, UnfiredType.BUCKET.getMeta());
			ItemStack milk = new ItemStack(clayBucket, 1, SpecialFluid.MILK.getMeta());
			GameRegistry.addRecipe(raw.copy(), "c c", " c ", 'c', Items.CLAY_BALL);
			GameRegistry.addSmelting(raw, new ItemStack(clayBucket), 0.5f);

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.CAKE), "MMM", "SES", "WWW",
					'M', milk, 'S', Items.SUGAR, 'E', "egg", 'W', "cropWheat"));

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
			ItemStack raw = new ItemStack(clayUnfired, 1, UnfiredType.SHEARS.getMeta());
			GameRegistry.addRecipe(raw.copy(), "c ", " c", 'c', Items.CLAY_BALL);
			GameRegistry.addSmelting(raw, new ItemStack(clayShears), 0.5f);
		}

		// armor
		if(Config.armorEnabled) {
			ItemStack clayPlate = new ItemStack(clayUnfired, 1, UnfiredType.CLAY_PLATE.getMeta());
			ItemStack clayPlateRaw = new ItemStack(clayUnfired, 1, UnfiredType.CLAY_PLATE_RAW.getMeta());

			// craft plates of clay
			GameRegistry.addRecipe(clayPlateRaw.copy(), "cc", 'c', Items.CLAY_BALL);
			GameRegistry.addSmelting(clayPlateRaw, clayPlate, 0.5f);

			// and forge into armor. Ceramic armor is a thing in history
			GameRegistry.addRecipe(new ItemStack(clayHelmet), "ccc", "c c", 'c', clayPlate);
			GameRegistry.addRecipe(new ItemStack(clayChestplate), "c c", "ccc", "ccc", 'c', clayPlate);
			GameRegistry.addRecipe(new ItemStack(clayLeggings), "ccc", "c c", "c c", 'c', clayPlate);
			GameRegistry.addRecipe(new ItemStack(clayBoots), "c c", "c c", 'c', clayPlate);

			// if you would rather skip a step...
			// this is kept partly because legacy, partly because two hit armor is kinda fun

			// if smelting armor, use plates for consistancy with above recipe as clay is pretty cheap
			// normally I prefer the regular clay one as it fits the easter egg better
			ItemStack rawArmorItem = Config.smeltClayArmor ? clayPlateRaw : new ItemStack(Items.CLAY_BALL);
			GameRegistry.addRecipe(new ItemStack(clayHelmetRaw), "ccc", "c c", 'c', rawArmorItem);
			GameRegistry.addRecipe(new ItemStack(clayChestplateRaw), "c c", "ccc", "ccc", 'c', rawArmorItem);
			GameRegistry.addRecipe(new ItemStack(clayLeggingsRaw), "ccc", "c c", "c c", 'c', rawArmorItem);
			GameRegistry.addRecipe(new ItemStack(clayBootsRaw), "c c", "c c", 'c', rawArmorItem);

			if(Config.smeltClayArmor) {
				GameRegistry.addSmelting(clayHelmetRaw, new ItemStack(clayHelmet), 0.5f);
				GameRegistry.addSmelting(clayChestplateRaw, new ItemStack(clayChestplate), 0.5f);
				GameRegistry.addSmelting(clayLeggingsRaw, new ItemStack(clayLeggings), 0.5f);
				GameRegistry.addSmelting(clayBootsRaw, new ItemStack(clayBoots), 0.5f);
			}
		}

		// barrels
		if(Config.barrelEnabled) {
			ItemStack raw = new ItemStack(clayUnfired, 1, UnfiredType.BARREL.getMeta());
			ItemStack rawExtension = new ItemStack(clayUnfired, 1, UnfiredType.BARREL_EXTENSION.getMeta());
			GameRegistry.addRecipe(raw.copy(), "c c", "ccc", " c ", 'c', Items.CLAY_BALL);
			GameRegistry.addRecipe(rawExtension.copy(), "c c", "c c", "c c", 'c', Items.CLAY_BALL);

			ItemStack barrel = new ItemStack(clayBarrel, 1, 0);
			ItemStack extension = new ItemStack(clayBarrel, 1, 1);

			GameRegistry.addSmelting(raw, barrel, 0.5f);
			GameRegistry.addSmelting(rawExtension, extension, 0.5f);

			// barrels made of porcelain
			if(Config.porcelainEnabled) {
				ItemStack porcelainRaw = new ItemStack(clayUnfired, 1, UnfiredType.BARREL_PORCELAIN.getMeta());
				ItemStack porcelainRawExtension = new ItemStack(clayUnfired, 1, UnfiredType.BARREL_PORCELAIN_EXTENSION.getMeta());

				GameRegistry.addRecipe(new ShapedOreRecipe(porcelainRaw.copy(), "c c", "ccc", " c ", 'c', "clayPorcelain"));
				GameRegistry.addRecipe(new ShapedOreRecipe(porcelainRawExtension.copy(), "c c", "c c", "c c", 'c', "clayPorcelain"));

				ItemStack porcelainBarrel2 = new ItemStack(porcelainBarrel, 1, 0);
				ItemStack porcelainExtension = new ItemStack(porcelainBarrelExtension, 1, 0);

				GameRegistry.addSmelting(porcelainRaw, porcelainBarrel2, 0.5f);
				GameRegistry.addSmelting(porcelainRawExtension, porcelainExtension, 0.5f);
			}

			for(EnumDyeColor color : EnumDyeColor.values()) {
				String dye = "dye" + dyes[color.getMetadata()];
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(clayBarrelStained, 8, color.getMetadata()),
						"BBB", "BdB", "BBB", 'B', "barrelClay", 'd', dye));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(clayBarrelStainedExtension, 8, color.getMetadata()),
						"BBB", "BdB", "BBB", 'B', "barrelExtensionClay", 'd', dye));

				// alt recipe for crafting just 1
				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(clayBarrelStained, 1, color.getMetadata()),
						"barrelClay", dye));
				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(clayBarrelStainedExtension, 1, color.getMetadata()),
						"barrelExtensionClay", dye));

				if(Config.porcelainEnabled) {
					GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(porcelainBarrel, 8, color.getMetadata()),
							"BBB", "BdB", "BBB", 'B', "barrelPorcelain", 'd', dye));
					GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(porcelainBarrelExtension, 8, color.getMetadata()),
							"BBB", "BdB", "BBB", 'B', "barrelExtensionPorcelain", 'd', dye));

					// alt recipe for crafting just 1
					GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(porcelainBarrel, 1, color.getMetadata()),
							"barrelPorcelain", dye));
					GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(porcelainBarrelExtension, 1, color.getMetadata()),
							"barrelExtensionPorcelain", dye));
				}
			}
		}

		// decorative bricks
		if(Config.fancyBricksEnabled) {
			String[] surround = { "bbb", "b#b", "bbb" };

			// first two: crafted with regular clay
			// dark bricks: bit of black
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(clayHard, 2, ClayTypeHard.DARK_BRICKS.getMeta()),
					surround, 'b', Items.BRICK, '#', "dyeBlack"));
			// dragon bricks: breath of the end
			GameRegistry.addRecipe(new ItemStack(clayHard, 2, ClayTypeHard.DRAGON_BRICKS.getMeta()),
					surround, 'b', Items.BRICK, '#', Items.DRAGON_BREATH);

			// if porcelain is disabled, use regular bricks for those recipes
			Object secondBrick = Items.BRICK;
			if(Config.porcelainEnabled) {
				secondBrick = new ItemStack(clayUnfired, 1, UnfiredType.PORCELAIN_BRICK.getMeta());
			}

			// marine bricks: dye of the sea
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(clayHard, 2, ClayTypeHard.MARINE_BRICKS.getMeta()),
					surround, 'b', secondBrick, '#', "dyeBlue"));
			// gold bricks: shard of gold
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(clayHard, 2, ClayTypeHard.GOLDEN_BRICKS.getMeta()),
					surround, 'b', secondBrick, '#', "nuggetGold"));


			// slabs and stairs
			ItemStack darkBricks = new ItemStack(clayHard, 1, ClayTypeHard.DARK_BRICKS.getMeta());
			addSlabRecipe(claySlab, ClayTypeHard.DARK_BRICKS.getMeta(), darkBricks);
			addWallRecipe(clayWall, ClayWallType.DARK_BRICKS.getMeta(), darkBricks);
			addStairRecipe(stairsDarkBricks, darkBricks);

			ItemStack goldenBricks = new ItemStack(clayHard, 1, ClayTypeHard.GOLDEN_BRICKS.getMeta());
			addSlabRecipe(claySlab, ClayTypeHard.GOLDEN_BRICKS.getMeta(), goldenBricks);
			addWallRecipe(clayWall, ClayWallType.GOLDEN_BRICKS.getMeta(), goldenBricks);
			addStairRecipe(stairsGoldenBricks, goldenBricks);

			ItemStack marineBricks = new ItemStack(clayHard, 1, ClayTypeHard.MARINE_BRICKS.getMeta());
			addSlabRecipe(claySlab, ClayTypeHard.MARINE_BRICKS.getMeta(), marineBricks);
			addWallRecipe(clayWall, ClayWallType.MARINE_BRICKS.getMeta(), marineBricks);
			addStairRecipe(stairsMarineBricks, marineBricks);

			ItemStack dragonBricks = new ItemStack(clayHard, 1, ClayTypeHard.DRAGON_BRICKS.getMeta());
			addSlabRecipe(claySlab, ClayTypeHard.DRAGON_BRICKS.getMeta(), dragonBricks);
			addWallRecipe(clayWall, ClayWallType.DRAGON_BRICKS.getMeta(), dragonBricks);
			addStairRecipe(stairsDragonBricks, dragonBricks);
		}

		if(Config.brickWallEnabled) {
			addWallRecipe(clayWall, ClayWallType.BRICKS.getMeta(), new ItemStack(Blocks.BRICK_BLOCK));
		}

		// load plugins
		if(Loader.isModLoaded(ModIDs.TINKERS)) {
			TConstructPlugin.init();
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
		if(Loader.isModLoaded(ModIDs.TINKERS)) {
			TConstructPlugin.postInit();
		}
	}

	// Old version compatibility
	@Mod.EventHandler
	public void onMissingMapping(FMLMissingMappingsEvent event) {
		for(FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
			// remap old name for bucket, no one should really be keeping raw buckets anyways
			if(Config.bucketEnabled && mapping.type == GameRegistry.Type.ITEM && (mapping.name.equals(Util.resource("clay_bucket_raw")))) {
				mapping.remap(clayUnfired);
			}
		}
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


	/* Recipe shortcuts */
	protected static void addSlabRecipe(Block slab, int meta, ItemStack input) {
		GameRegistry.addShapedRecipe(new ItemStack(slab, 6, meta), "BBB", 'B', input);
	}

	protected static void addStairRecipe(Block stairs, ItemStack input) {
		GameRegistry.addShapedRecipe(new ItemStack(stairs, 4), "B  ", "BB ", "BBB", 'B', input);
	}

	protected static void addWallRecipe(Block wall, int meta, ItemStack input) {
		GameRegistry.addShapedRecipe(new ItemStack(wall, 6, meta), "BBB", "BBB", 'B', input);
	}
}
