package knightminer.ceramics.datagen;

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
  }
}
