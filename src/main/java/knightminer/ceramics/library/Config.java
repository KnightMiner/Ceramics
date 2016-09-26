package knightminer.ceramics.library;

import knightminer.ceramics.items.ItemClayUnfired.UnfiredType;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {
	private Config() {}

	public static boolean bucketEnabled = true;
	public static boolean shearsEnabled = true;
	public static boolean armorEnabled = true;
	public static boolean barrelEnabled = true;

	static Configuration configFile;

	public static void load(FMLPreInitializationEvent event) {
		configFile = new Configuration(event.getSuggestedConfigurationFile(), "0.1", false);

		bucketEnabled = configFile.getBoolean("bucket", "enabled", true,
				"Enables the clay bucket, an alternative to the iron bucket that breaks from hot liquids");
		shearsEnabled = configFile.getBoolean("bucket", "enabled", true,
				"Enables the clay shears, faster than iron shears but less duribility");
		armorEnabled = configFile.getBoolean("armor", "enabled", true,
				"Enables the clay armor, an early game alternative to leather");
		barrelEnabled = configFile.getBoolean("barrel", "enabled", true,
				"Enables the clay barrel, a liquid tank that can be expanded upwards");
	}

	public static boolean unfiredEnabled(UnfiredType type) {
		switch(type) {
			case BUCKET:
				return bucketEnabled;
			case SHEARS:
				return shearsEnabled;
			case BARREL:
			case BARREL_EXTENSION:
				return barrelEnabled;
		}
		return true;
	}
}
