package knightminer.ceramics.library;

import java.util.Locale;

import knightminer.ceramics.Ceramics;
import net.minecraft.util.ResourceLocation;

public class Util {
	public static String resource(String name) {
		return String.format("%s:%s", Ceramics.modID, name.toLowerCase(Locale.US));
	}
	public static String prefix(String name) {
		return String.format("%s.%s", Ceramics.modID, name.toLowerCase(Locale.US));
	}

	public static ResourceLocation getResource(String res) {
		return new ResourceLocation(Ceramics.modID, res);
	}
}
