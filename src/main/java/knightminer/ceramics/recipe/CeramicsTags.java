package knightminer.ceramics.recipe;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags.IOptionalNamedTag;
import net.minecraftforge.event.TagsUpdatedEvent;

import java.util.function.Consumer;

public class CeramicsTags {
  private static boolean tagsLoaded = false;

  /** Called on mod construct to set up tags */
  public static void init() {
    Blocks.init();
    Items.init();
    Fluids.init();
    Consumer<TagsUpdatedEvent.VanillaTagTypes> onTagsLoaded = (event) -> tagsLoaded = true;
    MinecraftForge.EVENT_BUS.addListener(onTagsLoaded);
  }

  /** Returns true if tags have been loaded at least once. Will be false before the world is loaded */
  public static boolean tagsLoaded() {
    return tagsLoaded;
  }

  public static class Blocks {
    private static void init() {}
    public static final IOptionalNamedTag<Block> COLORED_TERRACOTTA = ceramicsTag("colored_terracotta");
    public static final IOptionalNamedTag<Block> PORCELAIN = ceramicsTag("porcelain_block");
    public static final IOptionalNamedTag<Block> COLORED_PORCELAIN = ceramicsTag("colored_porcelain");
    public static final IOptionalNamedTag<Block> RAINBOW_PORCELAIN = ceramicsTag("rainbow_porcelain");
    public static final IOptionalNamedTag<Block> BRICKS = ceramicsTag("bricks");
    public static final IOptionalNamedTag<Block> TERRACOTTA_CISTERNS = ceramicsTag("terracotta_cisterns");
    public static final IOptionalNamedTag<Block> PORCELAIN_CISTERNS = ceramicsTag("porcelain_cisterns");
    public static final IOptionalNamedTag<Block> CISTERN_CONNECTIONS = ceramicsTag("cistern_connections");

    private static IOptionalNamedTag<Block> ceramicsTag(String id) {
      return BlockTags.createOptional(Ceramics.getResource(id));
    }
  }
  public static class Items {
    private static void init() {}
    public static final IOptionalNamedTag<Item> COLORED_TERRACOTTA = ceramicsTag("colored_terracotta");
    public static final IOptionalNamedTag<Item> PORCELAIN = ceramicsTag("porcelain_block");
    public static final IOptionalNamedTag<Item> COLORED_PORCELAIN = ceramicsTag("colored_porcelain");
    public static final IOptionalNamedTag<Item> RAINBOW_PORCELAIN = ceramicsTag("rainbow_porcelain");
    public static final IOptionalNamedTag<Item> BRICKS = ceramicsTag("bricks");
    public static final IOptionalNamedTag<Item> MILK_BUCKETS = forgeTag("buckets/milk");
    public static final IOptionalNamedTag<Item> TERRACOTTA_CISTERNS = ceramicsTag("terracotta_cisterns");
    public static final IOptionalNamedTag<Item> PORCELAIN_CISTERNS = ceramicsTag("porcelain_cisterns");
    public static final IOptionalNamedTag<Item> TERRACOTTA_CRACK_REPAIR = ceramicsTag("terracotta_crack_repair");

    public static final IOptionalNamedTag<Item> PLATES = forgeTag("plates");
    public static final IOptionalNamedTag<Item> BRICK_PLATES = forgeTag("plates/brick");

    private static IOptionalNamedTag<Item> ceramicsTag(String id) {
      return ItemTags.createOptional(Ceramics.getResource(id));
    }

    private static IOptionalNamedTag<Item> forgeTag(String id) {
      return ItemTags.createOptional(new ResourceLocation("forge", id));
    }
  }

  public static class Fluids {
    private static void init() {}
    @SuppressWarnings("WeakerAccess")
    public static final IOptionalNamedTag<Fluid> MILK = FluidTags.createOptional(new ResourceLocation("forge:milk"));

    public static final IOptionalNamedTag<Fluid> HOT_FLUIDS = makeWrapperTag("hot_fluids");
    public static final IOptionalNamedTag<Fluid> COOL_FLUIDS = makeWrapperTag("cool_fluids");

    private static IOptionalNamedTag<Fluid> makeWrapperTag(String id) {
      return FluidTags.createOptional(Ceramics.getResource(id));
    }
  }
}
