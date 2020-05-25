package knightminer.ceramics.datagen;

import knightminer.ceramics.registration.EnumBlockObject;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;

import java.util.Arrays;

;

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
  }

  private static <T extends Enum<T>> Block[] getList(EnumBlockObject<T,?> blocks, T[] values) {
    return Arrays.stream(values)
                 .map(blocks::getBlock)
                 .toArray(Block[]::new);
  }
}
