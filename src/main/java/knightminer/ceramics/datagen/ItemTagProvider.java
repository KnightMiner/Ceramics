package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.recipe.CeramicsTags;
import knightminer.ceramics.recipe.CeramicsTags.Blocks;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemTagProvider extends net.minecraft.data.ItemTagsProvider {
  public ItemTagProvider(DataGenerator gen, BlockTagsProvider blockTags, ExistingFileHelper helper) {
    super(gen, blockTags, Ceramics.MOD_ID, helper);
  }

  @Override
  public String getName() {
    return "Ceramics Item Tags";
  }

  @Override
  protected void registerTags() {
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
    this.getOrCreateBuilder(CeramicsTags.Items.MILK_BUCKETS).add(Items.MILK_BUCKET, Registration.MILK_CLAY_BUCKET.get(), Registration.CRACKED_MILK_CLAY_BUCKET.get());
  }
}
