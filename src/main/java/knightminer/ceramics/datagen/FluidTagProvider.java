package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.FluidTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class FluidTagProvider extends FluidTagsProvider {
	public FluidTagProvider(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper) {
		super(generatorIn, Ceramics.MOD_ID, existingFileHelper);
	}

	@Override
	public String getName() {
		return "Ceramics Fluid Tags";
	}

	@Override
	protected void registerTags() {
		// water and lava are handled by the temperature check, so just make empty tags
		this.getOrCreateBuilder(CeramicsTags.Fluids.COOL_FLUIDS);
		this.getOrCreateBuilder(CeramicsTags.Fluids.HOT_FLUIDS);
	}
}
