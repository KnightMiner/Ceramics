package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.datagen.MantleTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BlockEntityTagProvider extends IntrinsicHolderTagsProvider<BlockEntityType<?>> {
  @SuppressWarnings("deprecation")
  public BlockEntityTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    super(packOutput, Registries.BLOCK_ENTITY_TYPE, lookupProvider,
      // not sure why fetching the resource key from the object is such a pain
      type -> BuiltInRegistries.BLOCK_ENTITY_TYPE.getHolder(BuiltInRegistries.BLOCK_ENTITY_TYPE.getId(type)).orElseThrow().key(),
      Ceramics.MOD_ID, existingFileHelper);
  }

  @Override
  public String getName() {
    return "Ceramics block entity tag provider";
  }

  @Override
  protected void addTags(Provider pProvider) {
    tag(MantleTags.BlockEntities.HIDES_GAUGE_AMOUNT).add(Registration.FAUCET_BLOCK_ENTITY.get(), Registration.CHANNEL_BLOCK_ENTITY.get());
  }
}
