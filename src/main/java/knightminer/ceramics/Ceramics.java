package knightminer.ceramics;

import knightminer.ceramics.datagen.BlockEntityTagProvider;
import knightminer.ceramics.datagen.BlockTagProvider;
import knightminer.ceramics.datagen.FluidTagProvider;
import knightminer.ceramics.datagen.ItemTagProvider;
import knightminer.ceramics.datagen.LootTableProvider;
import knightminer.ceramics.datagen.RecipeProvider;
import knightminer.ceramics.datagen.client.CisternFluidProvider;
import knightminer.ceramics.datagen.client.FaucetFluidProvider;
import knightminer.ceramics.datagen.client.RenderFluidProvider;
import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.registration.RegistrationHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

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
		MinecraftForge.EVENT_BUS.addListener(this::onMissingMappings);
	}

	private void gatherData(GatherDataEvent event) {
		boolean server = event.includeServer();
		DataGenerator gen = event.getGenerator();
		PackOutput packOutput = gen.getPackOutput();
		ExistingFileHelper helper = event.getExistingFileHelper();
		CompletableFuture<Provider> lookupProvider = event.getLookupProvider();

		BlockTagProvider blockTags = new BlockTagProvider(packOutput, lookupProvider, helper);
		gen.addProvider(server, blockTags);
		gen.addProvider(server, new BlockEntityTagProvider(packOutput, lookupProvider, helper));
		gen.addProvider(server, new ItemTagProvider(packOutput, lookupProvider, blockTags.contentsGetter(), helper));
		gen.addProvider(server, new FluidTagProvider(packOutput, lookupProvider, helper));
		gen.addProvider(server, new RecipeProvider(packOutput));
		gen.addProvider(server, new LootTableProvider(packOutput));

		boolean client = event.includeClient();
		gen.addProvider(client, new RenderFluidProvider(packOutput));
		gen.addProvider(client, new FaucetFluidProvider(packOutput));
		gen.addProvider(client, new CisternFluidProvider(packOutput));
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
	private void onMissingMappings(MissingMappingsEvent event) {
		RegistrationHelper.handleMissingMappings(event, MOD_ID, Registries.BLOCK, this::missingBlock);
		RegistrationHelper.handleMissingMappings(event, MOD_ID, Registries.ITEM, name -> {
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

	/**
	 * Forms a translation component with the following details
	 * @param group Language key group
	 * @param name Name within group
	 * @return Language key
	 */
	public static MutableComponent component(String group, String name) {
		return Component.translatable(lang(group, name));
	}
}
