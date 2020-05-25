package knightminer.ceramics.datagen;

import knightminer.ceramics.recipe.CeramicsTags.Blocks;
import knightminer.ceramics.recipe.CeramicsTags.Items;
import net.minecraft.data.DataGenerator;

public class ItemTagProvider extends net.minecraft.data.ItemTagsProvider {
  public ItemTagProvider(DataGenerator gen) {
    super(gen);
  }

  @Override
  public String getName() {
    return "Ceramics Item Tags";
  }

  @Override
  protected void registerTags() {
    this.copy(Blocks.COLORED_TERRACOTTA, Items.COLORED_TERRACOTTA);

    // porcelain
    this.copy(Blocks.PORCELAIN, Items.PORCELAIN);
    this.copy(Blocks.COLORED_PORCELAIN, Items.COLORED_PORCELAIN);
    this.copy(Blocks.RAINBOW_PORCELAIN, Items.RAINBOW_PORCELAIN);

    // bricks
    this.copy(Blocks.BRICKS, Items.BRICKS);
  }
}
