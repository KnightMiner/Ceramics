package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class FluidTagProvider extends FluidTagsProvider {
	public FluidTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
		super(packOutput, lookupProvider, Ceramics.MOD_ID, existingFileHelper);
	}

	@Override
	public String getName() {
		return "Ceramics Fluid Tags";
	}

	@Override
	protected void addTags(Provider pProvider) {
		// water and lava are handled by the temperature check, so just make empty tags
		this.tag(CeramicsTags.Fluids.COOL_FLUIDS);
		this.tag(CeramicsTags.Fluids.HOT_FLUIDS);
		this.tag(CeramicsTags.Fluids.BUCKET_BLACKLIST).addOptionalTag(Tags.Fluids.POTION);
		this.tag(CeramicsTags.Fluids.HIDE_IN_BUCKET)
			.addTag(CeramicsTags.Fluids.BUCKET_BLACKLIST)
			.addOptionalTag(CeramicsTags.Fluids.POWDERED_SNOW)
			// JEI uses this tag to hide fluids, appropriate for our uses
			.addOptionalTag(Mantle.commonResource("hidden_from_recipe_viewers"));
	}
}
