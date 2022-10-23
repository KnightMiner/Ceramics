package knightminer.ceramics;

import knightminer.ceramics.datagen.BlockTagProvider;
import knightminer.ceramics.datagen.FluidTagProvider;
import knightminer.ceramics.datagen.ItemTagProvider;
import knightminer.ceramics.datagen.LootTableProvider;
import knightminer.ceramics.datagen.RecipeProvider;
import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.event.RegistryEvent.MissingMappings;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.registration.RegistrationHelper;

import javax.annotation.Nullable;

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
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientEvents::onConstructor);
		MinecraftForge.EVENT_BUS.addGenericListener(Block.class, this::onMissingBlocks);
		MinecraftForge.EVENT_BUS.addGenericListener(Item.class, this::onMissingItems);
	}

	private void gatherData(GatherDataEvent event) {
		if (event.includeServer()) {
			DataGenerator gen = event.getGenerator();
			ExistingFileHelper helper = event.getExistingFileHelper();
			BlockTagsProvider blockTags = new BlockTagProvider(gen, helper);
			gen.addProvider(blockTags);
			gen.addProvider(new ItemTagProvider(gen, blockTags, helper));
			gen.addProvider(new FluidTagProvider(gen, helper));
			gen.addProvider(new RecipeProvider(gen));
			gen.addProvider(new LootTableProvider(gen));
		}
	}

	/** Maps a block name to a block */
	@Nullable
	private Block missingBlock(String name) {
		if ("gauge".equals(name)) {
			return Registration.TERRACOTTA_GAUGE.get();
		}
		return null;
	}

	/** Missing block event */
	private void onMissingBlocks(MissingMappings<Block> event) {
		RegistrationHelper.handleMissingMappings(event, MOD_ID, this::missingBlock);
	}

	/** Missing item event */
	private void onMissingItems(MissingMappings<Item> event) {
		RegistrationHelper.handleMissingMappings(event, MOD_ID, name -> {
			ItemLike provider = missingBlock(name);
			return provider == null ? null : provider.asItem();
		});
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

	/**
	 * Forms the mod ID into a language key
	 * @param group Language key group
	 * @param name Name within group
	 * @return Language key
	 */
	public static String lang(String group, String name) {
		return String.format("%s.%s.%s", group, MOD_ID, name);
	}
}
