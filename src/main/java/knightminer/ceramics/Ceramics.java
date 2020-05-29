package knightminer.ceramics;

import knightminer.ceramics.datagen.BlockTagProvider;
import knightminer.ceramics.datagen.ItemTagProvider;
import knightminer.ceramics.datagen.LootTableProvider;
import knightminer.ceramics.datagen.RecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Ceramics.MOD_ID)
public class Ceramics {
	public static final String MOD_ID = "ceramics";
	public static final Logger LOG = LogManager.getLogger(MOD_ID);

	public Ceramics() {
	  Registration.init();
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@SubscribeEvent
	public void gatherData(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
		if (event.includeServer()) {
			gen.addProvider(new BlockTagProvider(gen));
			gen.addProvider(new ItemTagProvider(gen));
			gen.addProvider(new RecipeProvider(gen));
			gen.addProvider(new LootTableProvider(gen));
		}
	}
}
