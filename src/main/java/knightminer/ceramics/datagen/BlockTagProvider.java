package knightminer.ceramics.datagen;

import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.RainbowPorcelain;
import knightminer.ceramics.recipe.CeramicsTags;
import knightminer.ceramics.registration.EnumBlockObject;
import net.minecraft.block.Block;
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
    this.getBuilder(CeramicsTags.Blocks.PORCELAIN).add(Registration.PORCELAIN_BLOCK.getBlock(DyeColor.WHITE)).add(CeramicsTags.Blocks.COLORED_PORCELAIN);
    this.getBuilder(CeramicsTags.Blocks.RAINBOW_PORCELAIN).add(getList(Registration.RAINBOW_PORCELAIN, RainbowPorcelain.values()));
  }

  private static <T extends Enum<T>> Block[] getList(EnumBlockObject<T,?> blocks, T[] values) {
    return Arrays.stream(values)
                 .map(blocks::getBlock)
                 .toArray(Block[]::new);
  }
}
