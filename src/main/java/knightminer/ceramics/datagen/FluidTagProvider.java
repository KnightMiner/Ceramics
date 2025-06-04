package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

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
	}
}
