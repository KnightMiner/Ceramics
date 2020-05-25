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
    public static final Tag<Block> COLORED_TERRACOTTA = makeWrapperTag("colored_terracotta");
    public static final Tag<Block> PORCELAIN = makeWrapperTag("porcelain_block");
    public static final Tag<Block> COLORED_PORCELAIN = makeWrapperTag("colored_porcelain");
    public static final Tag<Block> RAINBOW_PORCELAIN = makeWrapperTag("rainbow_porcelain");
    public static final Tag<Block> BRICKS = makeWrapperTag("bricks");

    private static Tag<Block> makeWrapperTag(String id) {
      return new BlockTags.Wrapper(new ResourceLocation(Ceramics.MOD_ID, id));
    }
  }
  public static class Items {
    public static final Tag<Item> COLORED_TERRACOTTA = makeWrapperTag("colored_terracotta");
    public static final Tag<Item> PORCELAIN = makeWrapperTag("porcelain_block");
    public static final Tag<Item> COLORED_PORCELAIN = makeWrapperTag("colored_porcelain");
    public static final Tag<Item> RAINBOW_PORCELAIN = makeWrapperTag("rainbow_porcelain");
    public static final Tag<Item> BRICKS = makeWrapperTag("bricks");

    private static Tag<Item> makeWrapperTag(String id) {
      return new ItemTags.Wrapper(new ResourceLocation(Ceramics.MOD_ID, id));
    }
  }

}
