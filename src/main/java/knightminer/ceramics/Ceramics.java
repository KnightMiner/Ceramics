package knightminer.ceramics;

import knightminer.ceramics.datagen.BlockTagProvider;
import knightminer.ceramics.datagen.ItemTagProvider;
import knightminer.ceramics.datagen.LootTableProvider;
import knightminer.ceramics.datagen.RecipeProvider;
import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("WeakerAccess")
@Mod(Ceramics.MOD_ID)
public class Ceramics {
	public static final String MOD_ID = "ceramics";
	public static final Logger LOG = LogManager.getLogger(MOD_ID);

	public Ceramics() {
	  Registration.init();
		CeramicsTags.init();
		CeramicsNetwork.init();
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::gatherData);
	}

	private void gatherData(GatherDataEvent event) {
		if (event.includeServer()) {
			DataGenerator gen = event.getGenerator();
			ExistingFileHelper helper = event.getExistingFileHelper();
			BlockTagsProvider blockTags = new BlockTagProvider(gen, helper);
			gen.addProvider(blockTags);
			gen.addProvider(new ItemTagProvider(gen, blockTags, helper));
			gen.addProvider(new RecipeProvider(gen));
			gen.addProvider(new LootTableProvider(gen));
		}
	}

	/**
	 * Gets a resource locations as a string
	 * @param name  Name for the location
	 * @return  Resource location string
	 */
	public static String locationName(String name) {
		return MOD_ID + ":" + name;
	}

	/**
	 * Gets a resource location at the Ceramics namespace
	 * @param name  Resource path
	 * @return  Resource location for Ceramics
	 */
	public static ResourceLocation getResource(String name) {
	  return new ResourceLocation(MOD_ID, name);
  }
}
