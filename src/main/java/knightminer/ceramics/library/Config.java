package knightminer.ceramics.library;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {
	private Config() {}

	public static boolean bucketEnabled = true;
	public static boolean bucketSand = true;
	public static boolean shearsEnabled = true;
	public static boolean armorEnabled = true;
	public static boolean barrelEnabled = true;
	public static boolean porcelainEnabled = true;
	public static boolean fancyBricksEnabled = true;
	public static boolean brickWallEnabled = true;

	static Configuration configFile;

	public static void load(FMLPreInitializationEvent event) {
		configFile = new Configuration(event.getSuggestedConfigurationFile(), "0.1", false);

		bucketEnabled = configFile.getBoolean("bucket", "enabled", true,
				"Enables the clay bucket, an alternative to the iron bucket that breaks from hot liquids");
		bucketSand = configFile.getBoolean("bucketSand", "enabled", true,
				"Allows the clay bucket to pick up sand and gravel, because why not?");
		shearsEnabled = configFile.getBoolean("shears", "enabled", true,
				"Enables the clay shears, faster than iron shears but less duribility");
		armorEnabled = configFile.getBoolean("armor", "enabled", true,
				"Enables the clay armor, an early game alternative to leather");
		barrelEnabled = configFile.getBoolean("barrel", "enabled", true,
				"Enables the clay barrel, a liquid tank that can be expanded upwards");
		porcelainEnabled = configFile.getBoolean("porcelain", "enabled", true,
				"Enables porcelain, a whiter clay that produces true colors when dyed");
		fancyBricksEnabled = configFile.getBoolean("fancyBricks", "enabled", true,
				"Enables four additional decorative bricks");
		brickWallEnabled = configFile.getBoolean("brickWall", "enabled", true,
				"Enables walls made of vanilla bricks. Mainly here if another mod provides this feature (e.g. Quark)");

		if(configFile.hasChanged()) {
			configFile.save();
		}
	}
}
