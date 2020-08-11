package knightminer.ceramics.recipe;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class CeramicsTags {
  public static class Blocks {
    public static final INamedTag<Block> COLORED_TERRACOTTA = makeWrapperTag("colored_terracotta");
    public static final INamedTag<Block> PORCELAIN = makeWrapperTag("porcelain_block");
    public static final INamedTag<Block> COLORED_PORCELAIN = makeWrapperTag("colored_porcelain");
    public static final INamedTag<Block> RAINBOW_PORCELAIN = makeWrapperTag("rainbow_porcelain");
    public static final INamedTag<Block> BRICKS = makeWrapperTag("bricks");

    private static INamedTag<Block> makeWrapperTag(String id) {
      return BlockTags.makeWrapperTag(Ceramics.locationName(id));
    }
  }
  public static class Items {
    public static final INamedTag<Item> COLORED_TERRACOTTA = makeWrapperTag("colored_terracotta");
    public static final INamedTag<Item> PORCELAIN = makeWrapperTag("porcelain_block");
    public static final INamedTag<Item> COLORED_PORCELAIN = makeWrapperTag("colored_porcelain");
    public static final INamedTag<Item> RAINBOW_PORCELAIN = makeWrapperTag("rainbow_porcelain");
    public static final INamedTag<Item> BRICKS = makeWrapperTag("bricks");
    public static final INamedTag<Item> MILK_BUCKETS = ItemTags.makeWrapperTag("forge:buckets/milk");

    private static INamedTag<Item> makeWrapperTag(String id) {
      return ItemTags.makeWrapperTag(Ceramics.locationName(id));
    }
  }
  public static class Fluids {
    @SuppressWarnings("WeakerAccess")
    public static final ResourceLocation MILK_ID = new ResourceLocation("forge:milk");

    /**
     * Gets the milk tag safely
     * @return  Milk tag, empty if no tag or the tag is empty
     */
    public static Optional<ITag<Fluid>> getMilk() {
      return Optional.ofNullable(FluidTags.getCollection().getTagMap().get(MILK_ID))
                     .filter(tag -> !tag.getAllElements().isEmpty());
    }
  }
}
