package knightminer.ceramics.recipe;

import knightminer.ceramics.Ceramics;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.registration.object.BlockItemTagKey;

public class CeramicsTags {
  private static boolean tagsLoaded = false;

  /** Called on mod construct to set up tags */
  public static void init() {
    Blocks.init();
    Items.init();
    Fluids.init();
    NeoForge.EVENT_BUS.addListener(TagsUpdatedEvent.class, event -> tagsLoaded = true);
  }

  /** Returns true if tags have been loaded at least once. Will be false before the world is loaded */
  public static boolean tagsLoaded() {
    return tagsLoaded;
  }

  public static class Blocks {
    private static void init() {}
    public static final BlockItemTagKey COLORED_TERRACOTTA = ceramicsItem("colored_terracotta");
    public static final BlockItemTagKey PORCELAIN = ceramicsItem("porcelain");
    public static final BlockItemTagKey COLORED_PORCELAIN = ceramicsItem("colored_porcelain");
    public static final BlockItemTagKey RAINBOW_PORCELAIN = ceramicsItem("rainbow_porcelain");
    public static final BlockItemTagKey BRICKS = ceramicsItem("bricks");
    public static final BlockItemTagKey TERRACOTTA_CISTERNS = ceramicsItem("terracotta_cisterns");
    public static final BlockItemTagKey PORCELAIN_CISTERNS = ceramicsItem("porcelain_cisterns");
    public static final TagKey<Block> CISTERN_CONNECTIONS = ceramicsTag("cistern_connections");

    /** Blocks in this tag can be placed in a clay bucket despite not having a {@link net.minecraft.world.item.SolidBucketItem} */
    public static final TagKey<Block> BUCKET_EXTRA_BLOCKS = ceramicsTag("extra_bucket_blocks");
    /** Blocks that when placed in a clay bucket crack it */
    public static final TagKey<Block> BUCKET_CRACKING_BLOCKS = ceramicsTag("bucket_cracking_blocks");

    private static BlockItemTagKey ceramicsItem(String id) {
      return BlockItemTagKey.create(Ceramics.getResource(id));
    }
    private static TagKey<Block> ceramicsTag(String id) {
      return BlockTags.create(Ceramics.getResource(id));
    }
  }
  public static class Items {
    private static void init() {}
    public static final TagKey<Item> TERRACOTTA_CRACK_REPAIR = ceramicsTag("terracotta_crack_repair");

    public static final TagKey<Item> PLATES = commonTag("plates");
    public static final TagKey<Item> BRICK_PLATES = commonTag("plates/brick");

    public static final TagKey<Item> CLAY_BUCKETS = ceramicsTag("clay_buckets");
    public static final TagKey<Item> EMPTY_CLAY_BUCKETS = ceramicsTag("clay_buckets/empty");

    private static TagKey<Item> ceramicsTag(String id) {
      return ItemTags.create(Ceramics.getResource(id));
    }

    private static TagKey<Item> commonTag(String id) {
      return ItemTags.create(Mantle.commonResource(id));
    }
  }

  public static class Fluids {
    private static void init() {}

    /** Override to make a fluid hot when it is normally cool */
    public static final TagKey<Fluid> HOT_FLUIDS = ceramicsTag("hot_fluids");
    /** Override to make a fluid cool when it is normally hot */
    public static final TagKey<Fluid> COOL_FLUIDS = ceramicsTag("cool_fluids");
    /** Fluids in this tag cannot be used in clay buckets */
    public static final TagKey<Fluid> BUCKET_BLACKLIST = ceramicsTag("bucket_blacklist");
    /** Fluids in this tag won't show in the creative filled buckets. Unlike {@link #BUCKET_BLACKLIST}, will not prevent filling the bucket */
    public static final TagKey<Fluid> HIDE_IN_BUCKET = ceramicsTag("hide_in_bucket");
    /** Powdered snow in fluid form */
    public static final TagKey<Fluid> POWDER_SNOW = FluidTags.create(Mantle.commonResource("powder_snow"));

    private static TagKey<Fluid> ceramicsTag(String id) {
      return FluidTags.create(Ceramics.getResource(id));
    }
  }
}
