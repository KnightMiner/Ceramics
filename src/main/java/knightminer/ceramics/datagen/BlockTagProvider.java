package knightminer.ceramics.datagen;

import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.RainbowPorcelain;
import knightminer.ceramics.recipe.CeramicsTags;
import knightminer.ceramics.registration.object.EnumObject;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.DyeColor;
import net.minecraft.tags.BlockTags;

import java.util.Arrays;
import java.util.Comparator;

;

public class BlockTagProvider extends net.minecraft.data.BlockTagsProvider {
  /** Array of all dyes except white */
  private static final DyeColor[] COLORED_DYES = Arrays.stream(DyeColor.values())
                                                       .filter((color) -> color != DyeColor.WHITE)
                                                       .sorted(Comparator.comparingInt(DyeColor::getId))
                                                       .toArray(DyeColor[]::new);

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
    this.getBuilder(CeramicsTags.Blocks.COLORED_TERRACOTTA).add(getList(Registration.TERRACOTTA, DyeColor.values()));
    // porcelain
    this.getBuilder(BlockTags.ENDERMAN_HOLDABLE).add(Registration.UNFIRED_PORCELAIN_BLOCK.get());
    this.getBuilder(CeramicsTags.Blocks.COLORED_PORCELAIN).add(getList(Registration.PORCELAIN_BLOCK, COLORED_DYES));
    this.getBuilder(CeramicsTags.Blocks.PORCELAIN).add(Registration.PORCELAIN_BLOCK.get(DyeColor.WHITE)).add(CeramicsTags.Blocks.COLORED_PORCELAIN);
    this.getBuilder(CeramicsTags.Blocks.RAINBOW_PORCELAIN).add(getList(Registration.RAINBOW_PORCELAIN, RainbowPorcelain.values()));

    // bricks
    this.getBuilder(CeramicsTags.Blocks.BRICKS).add(
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
    this.getBuilder(BlockTags.WALLS).add(
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

  private static <T extends Enum<T>> Block[] getList(EnumObject<T,? extends Block> blocks, T[] values) {
    return Arrays.stream(values)
                 .map(blocks::get)
                 .toArray(Block[]::new);
  }
}
