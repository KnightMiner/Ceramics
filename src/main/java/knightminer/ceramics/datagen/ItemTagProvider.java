package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeknights.mantle.data.ItemTagProviderExtension;

import java.util.concurrent.CompletableFuture;

public class ItemTagProvider extends ItemTagProviderExtension {
  public ItemTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper helper) {
    super(packOutput, lookupProvider, blockTags, Ceramics.MOD_ID, helper);
  }

  @Override
  public String getName() {
    return "Ceramics Item Tags";
  }

  @Override
  protected void addTags(Provider provider) {
    this.copy(CeramicsTags.Blocks.COLORED_TERRACOTTA);
    // porcelain
    this.copy(CeramicsTags.Blocks.PORCELAIN);
    this.copy(CeramicsTags.Blocks.COLORED_PORCELAIN);
    this.copy(CeramicsTags.Blocks.RAINBOW_PORCELAIN);
    // bricks
    this.copy(CeramicsTags.Blocks.BRICKS);
    // cisterns
    this.copy(CeramicsTags.Blocks.TERRACOTTA_CISTERNS);
    this.copy(CeramicsTags.Blocks.PORCELAIN_CISTERNS);

    // item unique tags
    this.tag(Tags.Items.BUCKETS_MILK).add(Items.MILK_BUCKET, Registration.MILK_CLAY_BUCKET.get(), Registration.CRACKED_MILK_CLAY_BUCKET.get());
    this.tag(CeramicsTags.Items.TERRACOTTA_CRACK_REPAIR).add(Items.CLAY_BALL);
    this.tag(CeramicsTags.Items.BRICK_PLATES).add(Registration.CLAY_PLATE.get());
    this.tag(CeramicsTags.Items.PLATES).addTag(CeramicsTags.Items.BRICK_PLATES);

    // buckets
    this.tag(CeramicsTags.Items.EMPTY_CLAY_BUCKETS).add(Registration.EMPTY_CLAY_BUCKET.asItem(), Registration.CRACKED_EMPTY_CLAY_BUCKET.asItem());
    this.tag(CeramicsTags.Items.CLAY_BUCKETS).addTag(CeramicsTags.Items.EMPTY_CLAY_BUCKETS).add(
        Registration.FLUID_CLAY_BUCKET.asItem(), Registration.CRACKED_FLUID_CLAY_BUCKET.asItem(),
        Registration.SOLID_CLAY_BUCKET.asItem(), Registration.CRACKED_SOLID_CLAY_BUCKET.asItem(),
        Registration.MILK_CLAY_BUCKET.asItem(), Registration.CRACKED_MILK_CLAY_BUCKET.asItem());
  }
}
