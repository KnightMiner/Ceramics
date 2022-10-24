package knightminer.ceramics.recipe;

import knightminer.ceramics.Ceramics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;

import java.util.function.Consumer;

public class CeramicsTags {
  private static boolean tagsLoaded = false;

  /** Called on mod construct to set up tags */
  public static void init() {
    Blocks.init();
    Items.init();
    Fluids.init();
    Consumer<TagsUpdatedEvent> onTagsLoaded = (event) -> tagsLoaded = true;
    MinecraftForge.EVENT_BUS.addListener(onTagsLoaded);
  }

  /** Returns true if tags have been loaded at least once. Will be false before the world is loaded */
  public static boolean tagsLoaded() {
    return tagsLoaded;
  }

  public static class Blocks {
    private static void init() {}
    public static final TagKey<Block> COLORED_TERRACOTTA = ceramicsTag("colored_terracotta");
    public static final TagKey<Block> PORCELAIN = ceramicsTag("porcelain_block");
    public static final TagKey<Block> COLORED_PORCELAIN = ceramicsTag("colored_porcelain");
    public static final TagKey<Block> RAINBOW_PORCELAIN = ceramicsTag("rainbow_porcelain");
    public static final TagKey<Block> BRICKS = ceramicsTag("bricks");
    public static final TagKey<Block> TERRACOTTA_CISTERNS = ceramicsTag("terracotta_cisterns");
    public static final TagKey<Block> PORCELAIN_CISTERNS = ceramicsTag("porcelain_cisterns");
    public static final TagKey<Block> CISTERN_CONNECTIONS = ceramicsTag("cistern_connections");

    private static TagKey<Block> ceramicsTag(String id) {
      return BlockTags.create(Ceramics.getResource(id));
    }
  }
  public static class Items {
    private static void init() {}
    public static final TagKey<Item> COLORED_TERRACOTTA = ceramicsTag("colored_terracotta");
    public static final TagKey<Item> PORCELAIN = ceramicsTag("porcelain_block");
    public static final TagKey<Item> COLORED_PORCELAIN = ceramicsTag("colored_porcelain");
    public static final TagKey<Item> RAINBOW_PORCELAIN = ceramicsTag("rainbow_porcelain");
    public static final TagKey<Item> BRICKS = ceramicsTag("bricks");
    public static final TagKey<Item> MILK_BUCKETS = forgeTag("buckets/milk");
    public static final TagKey<Item> TERRACOTTA_CISTERNS = ceramicsTag("terracotta_cisterns");
    public static final TagKey<Item> PORCELAIN_CISTERNS = ceramicsTag("porcelain_cisterns");
    public static final TagKey<Item> TERRACOTTA_CRACK_REPAIR = ceramicsTag("terracotta_crack_repair");

    public static final TagKey<Item> PLATES = forgeTag("plates");
    public static final TagKey<Item> BRICK_PLATES = forgeTag("plates/brick");

    public static final TagKey<Item> CLAY_BUCKETS = ceramicsTag("clay_buckets");
    public static final TagKey<Item> EMPTY_CLAY_BUCKETS = ceramicsTag("clay_buckets/empty");

    private static TagKey<Item> ceramicsTag(String id) {
      return ItemTags.create(Ceramics.getResource(id));
    }

    private static TagKey<Item> forgeTag(String id) {
      return ItemTags.create(new ResourceLocation("forge", id));
    }
  }

  public static class Fluids {
    private static void init() {}

    /** Override to make a fluid hot when it is normally cool */
    public static final TagKey<Fluid> HOT_FLUIDS = ceramicsTag("hot_fluids");
    /** Override to make a fluid cool when it is normally hot */
    public static final TagKey<Fluid> COOL_FLUIDS = ceramicsTag("cool_fluids");

    private static TagKey<Fluid> ceramicsTag(String id) {
      return FluidTags.create(Ceramics.getResource(id));
    }
  }
}
