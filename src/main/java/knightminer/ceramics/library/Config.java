package knightminer.ceramics.library;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {

	private Config() {}

	public static boolean bucketEnabled = true;
	public static boolean bucketSand = true;
	public static boolean placeClayBucket = false;
	public static boolean bucketHotFluids = true;
	public static boolean shearsEnabled = true;
	public static boolean armorEnabled = true;
	public static boolean barrelEnabled = true;
	public static boolean porcelainEnabled = true;
	public static boolean porcelainOredictSmelting = true;
	public static boolean fancyBricksEnabled = true;
	public static boolean brickWallEnabled = true;
	public static boolean faucetEnabled = true;
	public static boolean rainbowClayEnabled = true;
	public static boolean rawClayArmorEnabled = true;

	public static int barrelClayCapacity = 4;
	public static int barrelPorcelainCapacity = 6;

	static Configuration configFile;

	public static void load(FMLPreInitializationEvent event) {
		configFile = new Configuration(event.getSuggestedConfigurationFile(), "0.1", false);

		// buckets
		bucketEnabled = configFile.getBoolean("bucket", "enabled", true,
				"Enables the clay bucket, an alternative to the iron bucket that breaks from hot liquids");
		configFile.moveProperty("enabled", "bucketSand", "bucket");
		configFile.renameProperty("bucket", "bucketSand", "sand");
		bucketSand = configFile.getBoolean("sand", "bucket", true, "Allows the clay bucket to pick up sand and gravel, because why not?");
		configFile.moveProperty("enabled", "placeClayBucket", "bucket");
		placeClayBucket = configFile.getBoolean("placeClayBucket", "bucket", false,
				"Allows the unfired clay bucket to be placed in the world, for the sake of block based kilns. Normal clay buckets remain unplaceable");
		bucketHotFluids = configFile.getBoolean("hotFluids", "bucket", true,
				"If true, the clay bucket can hold hot fluids and breaks when emptying. If false they cannot be picked up at all");

		// tools
		shearsEnabled = configFile.getBoolean("shears", "enabled", true,
				"Enables the clay shears, faster than iron shears but less duribility");

		armorEnabled = configFile.getBoolean("armor", "enabled", true,
				"Enables the clay armor, an early game alternative to leather");
		rawClayArmorEnabled = configFile.getBoolean("rawClayArmorEnabled", "enabled", true,
				"Allows you to create armor out of clay directly which has one durability and terriable protection.") && armorEnabled;

		barrelEnabled = configFile.getBoolean("barrel", "enabled", true,
				"Enables the clay barrel, a liquid tank that can be expanded upwards");

		// decoration
		porcelainEnabled = configFile.getBoolean("porcelain", "enabled", true,
				"Enables porcelain, a whiter clay that produces true colors when dyed");
		porcelainOredictSmelting = configFile.getBoolean("oredictSmelting", "porcelain", porcelainOredictSmelting, "Pulls recipes from the oredict for smelting porcelain bricks. If disabled just adds one static recipe using ceramics porcelain");
		fancyBricksEnabled = configFile.getBoolean("fancyBricks", "enabled", true,
				"Enables four additional decorative bricks");
		rainbowClayEnabled = configFile.getBoolean("rainbowClay", "enabled", true,
				"Enables clay bricks and blocks with a rainbow animation. Includes brick slabs, stairs, and walls.");
		brickWallEnabled = configFile.getBoolean("brickWall", "enabled", true,
				"Enables walls made of vanilla bricks. Mainly here if another mod provides this feature (e.g. Quark)");

		// fluids
		faucetEnabled = configFile.getBoolean("porcelainFaucet", "enabled", true,
				"Enables porcelain faucets and channels for moving fluids. Requires porcelain") && porcelainEnabled;

		barrelClayCapacity = configFile.getInt("capacityClay", "barrel", 4, 1, 100,
				"Storage capacity for clay barrels in buckets. This determines the base and the amount each extension adds. Changing this will require breaking and replacing the barrel to update.");
		barrelPorcelainCapacity = configFile.getInt("capacityPorcelain", "barrel", 6, 1, 100,
				"Storage capacity for porcelain barrels in buckets. This determines the base and the amount each extension adds. Changing this will require breaking and replacing the barrel to update.");

		if(configFile.hasChanged()) {
			configFile.save();
		}
	}

	public static class ConditionConfig implements IConditionFactory {
		@Override
		public BooleanSupplier parse(JsonContext context, JsonObject json) {
			String enabled = JsonUtils.getString(json, "enabled");
			return () -> configEnabled(enabled);
		}

		// this is just so I am not doing () -> each time really, I'm lazy :)
		private static boolean configEnabled(String config) {
			switch(config) {
				case "barrel":
					return barrelEnabled;
				case "bucket":
					return bucketEnabled;
				case "bucket_block":
					return placeClayBucket;
				case "armor":
					return armorEnabled;
				case "raw_armor":
					return rawClayArmorEnabled;
				case "faucet":
					return faucetEnabled;
				case "fancy_bricks":
					return fancyBricksEnabled;
				case "brick_wall":
					return brickWallEnabled;
				case "rainbow_clay":
					return rainbowClayEnabled;
				case "porcelain":
					return porcelainEnabled;
				case "shears":
					return shearsEnabled;
			}

			throw new JsonSyntaxException("Config option '" + config + "' does not exist");
		}
	}
}
