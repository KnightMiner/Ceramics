package knightminer.ceramics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import knightminer.ceramics.items.ItemArmorClay;
import knightminer.ceramics.items.ItemArmorClayRaw;
import knightminer.ceramics.items.ItemClayBucket;
import knightminer.ceramics.library.CreativeTab;
import knightminer.ceramics.library.Util;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = Ceramics.modID, version = Ceramics.version, name = Ceramics.name)
public class Ceramics {
	public static final String name = "Ceramics";
	public static final String modID = "ceramics";
	public static final String version = "${version}";

	@SidedProxy(clientSide = "knightminer.ceramics.ClientProxy", serverSide = "knightminer.ceramics.CommonProxy")
	public static CommonProxy proxy;

	public static CreativeTab tab = new CreativeTab(modID, new ItemStack(Items.BRICK));

	public static final Logger log = LogManager.getLogger(modID);

	public static ItemClayBucket clayBucket;
	public static Item clayBucketRaw;

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
		clayBucket = registerItem(new ItemClayBucket(), "clay_bucket");
		clayBucketRaw = registerItem(new Item(), "clay_bucket_raw").setCreativeTab(tab).setMaxStackSize(16);

		// armor
		clayArmor = EnumHelper.addArmorMaterial(Util.prefix("clay"), "cermamics:clay", 4, new int[]{1, 2, 3, 1}, 7,
				null, 0);
		clayArmor.customCraftingMaterial = Items.BRICK;
		clayHelmet = registerItem(new ItemArmorClay(EntityEquipmentSlot.HEAD), "clay_helmet");
		clayChestplate = registerItem(new ItemArmorClay(EntityEquipmentSlot.CHEST), "clay_chestplate");
		clayLeggings = registerItem(new ItemArmorClay(EntityEquipmentSlot.LEGS), "clay_leggings");
		clayBoots = registerItem(new ItemArmorClay(EntityEquipmentSlot.FEET), "clay_boots");

		clayArmorRaw = EnumHelper.addArmorMaterial(Util.prefix("clay_raw"), "cermamics:clay_raw", 1,
				new int[]{1, 1, 1, 1}, 1, null, 0);
		clayArmor.customCraftingMaterial = Items.CLAY_BALL;
		clayHelmetRaw = registerItem(new ItemArmorClayRaw(EntityEquipmentSlot.HEAD), "clay_helmet_raw");
		clayChestplateRaw = registerItem(new ItemArmorClayRaw(EntityEquipmentSlot.CHEST), "clay_chestplate_raw");
		clayLeggingsRaw = registerItem(new ItemArmorClayRaw(EntityEquipmentSlot.LEGS), "clay_leggings_raw");
		clayBootsRaw = registerItem(new ItemArmorClayRaw(EntityEquipmentSlot.FEET), "clay_boots_raw");

		tab.setIcon(new ItemStack(clayBucket));

		proxy.registerModels();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.addRecipe(new ItemStack(clayBucketRaw), "c c", " c ", 'c', Items.CLAY_BALL);
		GameRegistry.addSmelting(clayBucketRaw, new ItemStack(clayBucket), 0.5f);

		GameRegistry.addRecipe(new ItemStack(clayHelmetRaw), "ccc", "c c", 'c', Items.CLAY_BALL);
		GameRegistry.addRecipe(new ItemStack(clayChestplateRaw), "c c", "ccc", "ccc", 'c', Items.CLAY_BALL);
		GameRegistry.addRecipe(new ItemStack(clayLeggingsRaw), "ccc", "c c", "c c", 'c', Items.CLAY_BALL);
		GameRegistry.addRecipe(new ItemStack(clayBootsRaw), "c c", "c c", 'c', Items.CLAY_BALL);

		GameRegistry.addSmelting(clayHelmetRaw, new ItemStack(clayHelmet), 0.5f);
		GameRegistry.addSmelting(clayChestplateRaw, new ItemStack(clayChestplate), 0.5f);
		GameRegistry.addSmelting(clayLeggingsRaw, new ItemStack(clayLeggings), 0.5f);
		GameRegistry.addSmelting(clayBootsRaw, new ItemStack(clayBoots), 0.5f);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(clayBucket);
	}

	private <T extends Item> T registerItem(T item, String name) {
		item.setUnlocalizedName(Util.prefix(name));
		item.setRegistryName(Util.getResource(name));
		GameRegistry.register(item);

		return item;
	}

}
