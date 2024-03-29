package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.recipe.CeramicsTags;
import knightminer.ceramics.recipe.CeramicsTags.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemTagProvider extends ItemTagsProvider {
  public ItemTagProvider(DataGenerator gen, BlockTagsProvider blockTags, ExistingFileHelper helper) {
    super(gen, blockTags, Ceramics.MOD_ID, helper);
  }

  @Override
  public String getName() {
    return "Ceramics Item Tags";
  }

  @Override
  protected void addTags() {
    this.copy(Blocks.COLORED_TERRACOTTA, CeramicsTags.Items.COLORED_TERRACOTTA);
    // porcelain
    this.copy(Blocks.PORCELAIN, CeramicsTags.Items.PORCELAIN);
    this.copy(Blocks.COLORED_PORCELAIN, CeramicsTags.Items.COLORED_PORCELAIN);
    this.copy(Blocks.RAINBOW_PORCELAIN, CeramicsTags.Items.RAINBOW_PORCELAIN);
    // bricks
    this.copy(Blocks.BRICKS, CeramicsTags.Items.BRICKS);
    // cisterns
    this.copy(Blocks.TERRACOTTA_CISTERNS, CeramicsTags.Items.TERRACOTTA_CISTERNS);
    this.copy(Blocks.PORCELAIN_CISTERNS, CeramicsTags.Items.PORCELAIN_CISTERNS);

    // item unique tags
    this.tag(CeramicsTags.Items.MILK_BUCKETS).add(Items.MILK_BUCKET, Registration.MILK_CLAY_BUCKET.get(), Registration.CRACKED_MILK_CLAY_BUCKET.get());
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
