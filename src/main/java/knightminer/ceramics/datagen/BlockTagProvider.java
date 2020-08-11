package knightminer.ceramics.datagen;

import knightminer.ceramics.Registration;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.TagsProvider;
import net.minecraft.item.DyeColor;
import net.minecraft.tags.BlockTags;

public class BlockTagProvider extends net.minecraft.data.BlockTagsProvider {
  public BlockTagProvider(DataGenerator gen) {
    super(gen);
  }

  @Override
  public String getName() {
    return "Ceramics Block Tags";
  }

  @Override
  protected void registerTags() {
    // vanilla colored terracotta
    TagsProvider.Builder<Block> coloredTerracotta = this.getOrCreateBuilder(CeramicsTags.Blocks.COLORED_TERRACOTTA);
    Registration.TERRACOTTA.values().forEach(coloredTerracotta::add);

    // porcelain
    this.getOrCreateBuilder(BlockTags.ENDERMAN_HOLDABLE).add(Registration.UNFIRED_PORCELAIN_BLOCK.get());
    TagsProvider.Builder<Block> coloredPorcelain = this.getOrCreateBuilder(CeramicsTags.Blocks.COLORED_PORCELAIN);
    Registration.PORCELAIN_BLOCK.forEach((color, block) -> {
      if (color != DyeColor.WHITE) {
        coloredPorcelain.add(block);
      }
    });
    this.getOrCreateBuilder(CeramicsTags.Blocks.PORCELAIN)
        .add(Registration.PORCELAIN_BLOCK.get(DyeColor.WHITE))
        .addTag(CeramicsTags.Blocks.COLORED_PORCELAIN);

    // rainbow porcelain
    TagsProvider.Builder<Block> rainbow = this.getOrCreateBuilder(CeramicsTags.Blocks.RAINBOW_PORCELAIN);
    Registration.RAINBOW_PORCELAIN.values().forEach(rainbow::add);

    // bricks
    this.getOrCreateBuilder(CeramicsTags.Blocks.BRICKS).add(
        // clay
        Blocks.BRICKS,
        Registration.DARK_BRICKS.get(),
        Registration.DRAGON_BRICKS.get(),
        Registration.LAVA_BRICKS.get(),
        // porcelain
        Registration.PORCELAIN_BRICKS.get(),
        Registration.GOLDEN_BRICKS.get(),
        Registration.MARINE_BRICKS.get(),
        Registration.MONOCHROME_BRICKS.get(),
        Registration.RAINBOW_BRICKS.get()
    );
    this.getOrCreateBuilder(BlockTags.WALLS).add(
        // clay
        Registration.DARK_BRICKS.getWall(),
        Registration.DRAGON_BRICKS.getWall(),
        Registration.LAVA_BRICKS.getWall(),
        // porcelain
        Registration.PORCELAIN_BRICKS.getWall(),
        Registration.GOLDEN_BRICKS.getWall(),
        Registration.MARINE_BRICKS.getWall(),
        Registration.MONOCHROME_BRICKS.getWall(),
        Registration.RAINBOW_BRICKS.getWall()
    );
  }
}
