package knightminer.ceramics.recipe;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class CeramicsTags {
  public static class Blocks {

    private static Tag<Block> makeWrapperTag(String id) {
      return new BlockTags.Wrapper(new ResourceLocation(Ceramics.MOD_ID, id));
    }
  }
  public static class Items {

    private static Tag<Item> makeWrapperTag(String id) {
      return new ItemTags.Wrapper(new ResourceLocation(Ceramics.MOD_ID, id));
    }
  }

}
