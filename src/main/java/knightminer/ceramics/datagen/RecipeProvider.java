package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.RainbowPorcelain;
import knightminer.ceramics.recipe.CeramicsTags;
import knightminer.ceramics.recipe.CrackedClayRepairRecipeBuilder;
import knightminer.ceramics.recipe.KilnRecipeBuilder;
import knightminer.ceramics.recipe.NoNBTIngredient;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@SuppressWarnings({"ConstantConditions", "SameParameterValue"})
public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {

  /** Vanilla bricks as a building block object */
  private static final WallBuildingBlockObject BRICKS = new WallBuildingBlockObject(new BuildingBlockObject(Blocks.BRICKS.builtInRegistryHolder(), Blocks.BRICK_SLAB.builtInRegistryHolder(), Blocks.BRICK_STAIRS.builtInRegistryHolder()), Blocks.BRICK_WALL.builtInRegistryHolder());

  public RecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
    super(packOutput, registries);
  }

  @Override
  protected void buildRecipes(RecipeOutput output) {
    // recoloring terracotta
    Criterion<?> terracottaCriteria = has(CeramicsTags.Items.COLORED_TERRACOTTA);
    Registration.TERRACOTTA.forEach((color, item) ->
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, item, 8)
                         .define('B', CeramicsTags.Items.COLORED_TERRACOTTA)
                         .define('D', color.getTag())
                         .group("stained_terracotta")
                         .pattern("BBB")
                         .pattern("BDB")
                         .pattern("BBB")
                         .unlockedBy("has_terracotta", terracottaCriteria)
                         .save(output, location(id(item).getPath() + "_recolor"))
    );

    // crafting porcelain
    ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Registration.UNFIRED_PORCELAIN, 4)
                          .requires(Tags.Items.GEMS_QUARTZ)
                          .requires(Items.CLAY_BALL)
                          .requires(Items.CLAY_BALL)
                          .requires(Items.CLAY_BALL)
                          .unlockedBy("has_quartz", has(Tags.Items.GEMS_QUARTZ))
                          .save(output);

    // unfired porcelain
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Registration.UNFIRED_PORCELAIN_BLOCK)
                       .define('b', Registration.UNFIRED_PORCELAIN)
                       .pattern("bb")
                       .pattern("bb")
                       .unlockedBy("has_item", has(Registration.UNFIRED_PORCELAIN))
                       .save(output);
    // smelting porcelain
    kilnFurnaceRecipe(output, Registration.UNFIRED_PORCELAIN_BLOCK, Registration.PORCELAIN_BLOCK.get(DyeColor.WHITE), 0.3f);

    // colored porcelain
    Criterion<?> porcelainCriteria = has(Registration.PORCELAIN_BLOCK.get(DyeColor.WHITE));
    Registration.PORCELAIN_BLOCK.forEach((color, item) ->
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, item, 8)
                         .define('B', CeramicsTags.Items.PORCELAIN)
                         .define('D', color.getTag())
                         .group(locationString("dye_porcelain"))
                         .pattern("BBB")
                         .pattern("BDB")
                         .pattern("BBB")
                         .unlockedBy("has_porcelain", porcelainCriteria)
                         .save(output)
    );
    // rainbow porcelain
    kilnFurnaceRecipe(output, CeramicsTags.Items.COLORED_PORCELAIN, Registration.RAINBOW_PORCELAIN.get(RainbowPorcelain.RED), 0.1f, location("rainbow_porcelain"));

    // smelt for full rainbow
    Criterion<?> hasTheRainbow = has(CeramicsTags.Items.RAINBOW_PORCELAIN);
    Registration.RAINBOW_PORCELAIN.forEach((color, item) ->
      SingleItemRecipeBuilder.stonecutting(Ingredient.of(CeramicsTags.Items.RAINBOW_PORCELAIN), RecipeCategory.BUILDING_BLOCKS, item)
                             .unlockedBy("has_the_rainbow", hasTheRainbow)
                             .save(output, id(item))
    );

    // bricks
    // vanilla brick block shortcuts
    Criterion<?> hasClayBrick = has(Items.BRICK);
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Items.BRICK_SLAB)
                       .define('b', Items.BRICK)
                       .pattern("bb")
                       .unlockedBy("has_bricks", hasClayBrick)
                       .group(path(Items.BRICK_SLAB))
                       .save(output, location("brick_slab_from_bricks"));
    // stairs shortcut
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Items.BRICK_STAIRS)
                       .define('b', Items.BRICK)
                       .pattern("b  ")
                       .pattern("bb ")
                       .pattern("bbb")
                       .unlockedBy("has_bricks", hasClayBrick)
                       .group(path(Items.BRICK_STAIRS))
                       .save(output, location("brick_stairs_from_bricks"));
    // block from slab
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Items.BRICKS)
                       .define('B', Items.BRICK_SLAB)
                       .pattern("B")
                       .pattern("B")
                       .unlockedBy("has_item", has(Items.BRICK_SLAB))
                       .group(path(Items.BRICKS))
                       .save(output, location("bricks_from_slab"));

    // dark bricks from smelting bricks
    eachBuilding(BRICKS, Registration.DARK_BRICKS, (input, result) ->
      kilnFurnaceRecipe(output, input, result, 0.1f)
    );
    registerSlabStairWall(output, Registration.DARK_BRICKS);

    // magma bricks from lava bucket
    addBrickRecipe(output, BRICKS, Items.LAVA_BUCKET, Registration.LAVA_BRICKS, "lava");
    registerSlabStairWall(output, Registration.LAVA_BRICKS);

    // dragon bricks from dragon's breath
    addBrickRecipe(output, BRICKS, Items.DRAGON_BREATH, Registration.DRAGON_BRICKS, "dragon");
    registerSlabStairWall(output, Registration.DRAGON_BRICKS);

    // porcelain bricks
    kilnFurnaceRecipe(output, Registration.UNFIRED_PORCELAIN, Registration.PORCELAIN_BRICK, 0.3f);
    // using bricks
    Criterion<?> hasBricks = has(Registration.PORCELAIN_BRICK);
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Registration.PORCELAIN_BRICKS)
                       .define('b', Registration.PORCELAIN_BRICK)
                       .pattern("bb")
                       .pattern("bb")
                       .unlockedBy("has_bricks", hasBricks)
                       .group(Registration.PORCELAIN_BRICK.getId().toString())
                       .save(output);
    // slab shortcut
    ItemLike porcelainSlab = Registration.PORCELAIN_BRICKS.getSlab();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, porcelainSlab)
                       .define('b', Registration.PORCELAIN_BRICK)
                       .pattern("bb")
                       .unlockedBy("has_bricks", hasBricks)
                       .group(id(porcelainSlab).toString())
                       .save(output, suffix(porcelainSlab, "_from_bricks"));
    // stairs shortcut
    ItemLike porcelainStairs = Registration.PORCELAIN_BRICKS.getStairs();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, porcelainStairs)
                       .define('b', Registration.PORCELAIN_BRICK)
                       .pattern("b  ")
                       .pattern("bb ")
                       .pattern("bbb")
                       .unlockedBy("has_bricks", hasBricks)
                       .group(id(porcelainStairs).toString())
                       .save(output, suffix(porcelainStairs, "_from_bricks"));
    registerSlabStairWall(output, Registration.PORCELAIN_BRICKS);

    // golden bricks
    addBrickRecipe(output, Registration.PORCELAIN_BRICKS, Items.GOLD_INGOT, Registration.GOLDEN_BRICKS, "gold");
    registerSlabStairWall(output, Registration.GOLDEN_BRICKS);

    // marine bricks
    addBrickRecipe(output, Registration.PORCELAIN_BRICKS, Items.PRISMARINE_SHARD, Registration.MARINE_BRICKS, "prismarine");
    registerSlabStairWall(output, Registration.MARINE_BRICKS);

    // monochrome uses ink, not black dye intentionally
    addBrickRecipe(output, Registration.PORCELAIN_BRICKS, Items.INK_SAC, Registration.MONOCHROME_BRICKS, "ink");
    registerSlabStairWall(output, Registration.MONOCHROME_BRICKS);

    // rainbow
    eachBuilding(Registration.PORCELAIN_BRICKS, Registration.RAINBOW_BRICKS, (input, result) ->
      kilnFurnaceRecipe(output, input, result, 0.1f)
    );
    registerSlabStairWall(output, Registration.RAINBOW_BRICKS);

    // buckets
    // unfired
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.UNFIRED_CLAY_BUCKET)
                       .define('c', Items.CLAY_BALL)
                       .pattern("c c")
                       .pattern(" c ")
                       .unlockedBy("has_clay", has(Items.CLAY_BALL))
                       .save(output);
    // fired
    kilnFurnaceRecipe(output, Registration.UNFIRED_CLAY_BUCKET, Registration.EMPTY_CLAY_BUCKET, 0.3f);
    // cracked
    kilnFurnaceRecipe(output, Registration.EMPTY_CLAY_BUCKET, Registration.CRACKED_EMPTY_CLAY_BUCKET, 0.2f);

    // cistern
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.CLAY_CISTERN, 2)
                       .define('c', Items.CLAY_BALL)
                       .pattern("c c")
                       .pattern("c c")
                       .pattern("c c")
                       .unlockedBy("has_clay", has(Items.CLAY_BALL))
                       .save(output);
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.UNFIRED_CISTERN, 2)
                       .define('c', Registration.UNFIRED_PORCELAIN)
                       .pattern("c c")
                       .pattern("c c")
                       .pattern("c c")
                       .unlockedBy("has_clay", has(Registration.UNFIRED_PORCELAIN))
                       .save(output);
    // fired
    kilnFurnaceRecipe(output, Registration.CLAY_CISTERN, Registration.TERRACOTTA_CISTERN, 0.3f);
    kilnFurnaceRecipe(output, Registration.UNFIRED_CISTERN, Registration.PORCELAIN_CISTERN.get(DyeColor.WHITE), 0.3f);
    // repair
    clayRepair(output, Registration.TERRACOTTA_CISTERN);
    // colored
    Registration.COLORED_CISTERN.forEach((color, block) -> {
      // craft
      ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block, 4)
                         .define('c', new NoNBTIngredient(Ingredient.of(CeramicsTags.Items.TERRACOTTA_CISTERNS)))
                         .define('d', color.getTag())
                         .pattern(" c ")
                         .pattern("cdc")
                         .pattern(" c ")
                         .unlockedBy("has_cistern", has(Registration.TERRACOTTA_CISTERN))
                         .group(Ceramics.locationName("colored_cisterns"))
                         .save(output);
      // repair
      clayRepair(output, block);
    });
    Registration.PORCELAIN_CISTERN.forEach((color, block) ->
      ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block, 4)
                         .define('c', CeramicsTags.Items.PORCELAIN_CISTERNS)
                         .define('d', color.getTag())
                         .pattern(" c ")
                         .pattern("cdc")
                         .pattern(" c ")
                         .unlockedBy("has_cistern", has(Registration.PORCELAIN_CISTERN.get(DyeColor.WHITE)))
                         .group(Ceramics.locationName("porcelain_cisterns"))
                         .save(output)
    );

    // gauge
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Registration.TERRACOTTA_GAUGE, 4)
                       .define('b', Items.BRICK)
                       .define('p', Tags.Items.GLASS_PANES_COLORLESS)
                       .pattern(" b ")
                       .pattern("bpb")
                       .pattern(" b ")
                       .unlockedBy("has_cistern", has(Registration.TERRACOTTA_CISTERN))
                       .save(output);
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Registration.PORCELAIN_GAUGE, 4)
                       .define('b', Registration.PORCELAIN_BRICK)
                       .define('p', Tags.Items.GLASS_PANES_COLORLESS)
                       .pattern(" b ")
                       .pattern("bpb")
                       .pattern(" b ")
                       .unlockedBy("has_cistern", has(Registration.PORCELAIN_CISTERN.get(DyeColor.WHITE)))
                       .save(output);


    // faucet
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.CLAY_FAUCET, 2)
                       .define('c', Items.CLAY_BALL)
                       .pattern("ccc")
                       .pattern(" c ")
                       .unlockedBy("has_cistern", has(Items.CLAY_BALL))
                       .save(output);
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.UNFIRED_FAUCET, 2)
                       .define('c', Registration.UNFIRED_PORCELAIN)
                       .pattern("ccc")
                       .pattern(" c ")
                       .unlockedBy("has_cistern", has(Registration.UNFIRED_PORCELAIN))
                       .save(output);
    kilnFurnaceRecipe(output, Registration.CLAY_FAUCET, Registration.TERRACOTTA_FAUCET, 0.3f);
    kilnFurnaceRecipe(output, Registration.UNFIRED_FAUCET, Registration.PORCELAIN_FAUCET, 0.3f);
    clayRepair(output, Registration.TERRACOTTA_FAUCET);

    // channel
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.CLAY_CHANNEL, 3)
                       .define('p', Items.CLAY_BALL)
                       .pattern("ppp")
                       .pattern("ppp")
                       .unlockedBy("has_cistern", has(Items.CLAY_BALL))
                       .save(output);
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.UNFIRED_CHANNEL, 3)
                       .define('p', Registration.UNFIRED_PORCELAIN)
                       .pattern("ppp")
                       .pattern("ppp")
                       .unlockedBy("has_cistern", has(Registration.UNFIRED_PORCELAIN))
                       .save(output);
    kilnFurnaceRecipe(output, Registration.CLAY_CHANNEL, Registration.TERRACOTTA_CHANNEL, 0.3f);
    kilnFurnaceRecipe(output, Registration.UNFIRED_CHANNEL, Registration.PORCELAIN_CHANNEL, 0.3f);
    clayRepair(output, Registration.TERRACOTTA_CHANNEL);

    // armor
    // clay plates
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.UNFIRED_CLAY_PLATE, 2)
                       .define('c', Items.CLAY_BALL)
                       .pattern("cc")
                       .unlockedBy("has_clay", has(Items.CLAY_BALL))
                       .save(output);
    kilnFurnaceRecipe(output, Registration.UNFIRED_CLAY_PLATE, Registration.CLAY_PLATE, 0.3f);

    // helmet
    Criterion<?> hasClayPlate = has(CeramicsTags.Items.BRICK_PLATES);
    ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Registration.CLAY_HELMET)
                       .define('c', CeramicsTags.Items.BRICK_PLATES)
                       .pattern("ccc")
                       .pattern("c c")
                       .unlockedBy("has_plate", hasClayPlate)
                       .save(output);
    // chestplate
    ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Registration.CLAY_CHESTPLATE)
                       .define('c', CeramicsTags.Items.BRICK_PLATES)
                       .pattern("c c")
                       .pattern("ccc")
                       .pattern("ccc")
                       .unlockedBy("has_plate", hasClayPlate)
                       .save(output);
    // leggings
    ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Registration.CLAY_LEGGINGS)
                       .define('c', CeramicsTags.Items.BRICK_PLATES)
                       .pattern("ccc")
                       .pattern("c c")
                       .pattern("c c")
                       .unlockedBy("has_plate", hasClayPlate)
                       .save(output);
    // boots
    ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Registration.CLAY_BOOTS)
                       .define('c', CeramicsTags.Items.BRICK_PLATES)
                       .pattern("c c")
                       .pattern("c c")
                       .unlockedBy("has_plate", hasClayPlate)
                       .save(output);

    // clay uncrafting
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.CLAY_BALL, 4)
                          .requires(Items.CLAY)
                          .unlockedBy("has_unfired", has(Items.CLAY))
                          .group(locationString("clay_uncrafting"))
                          .save(output, location("clay_uncrafting_4"));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.CLAY_BALL, 3)
                          .requires(Ingredient.of(Registration.UNFIRED_CLAY_BUCKET, Registration.CLAY_CISTERN))
                          .unlockedBy("has_unfired", has(Registration.UNFIRED_CLAY_BUCKET))
                          .group(locationString("clay_uncrafting"))
                          .save(output, location("clay_uncrafting_3"));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.CLAY_BALL, 2)
                          .requires(Ingredient.of(Registration.CLAY_FAUCET, Registration.CLAY_CHANNEL))
                          .unlockedBy("has_unfired", has(Registration.CLAY_FAUCET))
                          .group(locationString("clay_uncrafting"))
                          .save(output, location("clay_uncrafting_2"));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.CLAY_BALL, 1)
                          .requires(Ingredient.of(Registration.UNFIRED_CLAY_PLATE))
                          .unlockedBy("has_unfired", has(Registration.UNFIRED_CLAY_PLATE))
                          .group(locationString("clay_uncrafting"))
                          .save(output, location("clay_uncrafting"));
    // porcelain uncrafting
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.UNFIRED_PORCELAIN, 4)
                          .requires(Registration.UNFIRED_PORCELAIN_BLOCK)
                          .unlockedBy("has_unfired", has(Registration.UNFIRED_PORCELAIN_BLOCK))
                          .group(locationString("porcelain_uncrafting"))
                          .save(output, location("porcelain_uncrafting_4"));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.UNFIRED_PORCELAIN, 2)
                          .requires(Ingredient.of(Registration.UNFIRED_FAUCET, Registration.UNFIRED_CHANNEL))
                          .unlockedBy("has_unfired", has(Registration.UNFIRED_FAUCET))
                          .group(locationString("porcelain_uncrafting"))
                          .save(output, location("porcelain_uncrafting_2"));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.UNFIRED_PORCELAIN, 3)
                          .requires(Registration.UNFIRED_CISTERN)
                          .unlockedBy("has_unfired", has(Registration.UNFIRED_CISTERN))
                          .group(locationString("porcelain_uncrafting"))
                          .save(output, location("porcelain_uncrafting_3"));
    // compat, wish there was a better way to do this
    ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Blocks.CAKE)
                       .define('M', CeramicsTags.Items.MILK_BUCKETS)
                       .define('S', Items.SUGAR)
                       .define('W', Items.WHEAT)
                       .define('E', Items.EGG)
                       .pattern("MMM")
                       .pattern("SES")
                       .pattern("WWW")
                       .unlockedBy("has_egg", has(Items.EGG))
                       .save(output, location("cake"));

    // kiln - crafting
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.KILN)
                       .define('B', Items.BRICK)
                       .define('F', Items.FURNACE)
                       .define('H', Items.TERRACOTTA)
                       .pattern("BBB")
                       .pattern("BFB")
                       .pattern("HHH")
                       .unlockedBy("has_brick", has(Items.BRICK))
                       .save(output);

    // add vanilla furnace recipes to the kiln
    // clay
    kilnRecipe(output, Items.CLAY_BALL, Items.BRICK, 0.3f);
    kilnRecipe(output, Blocks.CLAY, Blocks.TERRACOTTA, 0.3f);
    Registration.TERRACOTTA.forEach((color, input) ->
      kilnRecipe(output, input, GLAZED_TERRACOTTA.get(color), 0.1f)
    );
    // sand and glass
    kilnRecipe(output, ItemTags.SAND, Blocks.GLASS, 0.1f);
    kilnRecipe(output, Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, 0.1f);
    kilnRecipe(output, Blocks.RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE, 0.1f);
    // rock
    kilnRecipe(output, Blocks.COBBLESTONE, Blocks.STONE, 0.1f);
    kilnRecipe(output, Blocks.STONE, Blocks.SMOOTH_STONE, 0.1f);
    kilnRecipe(output, Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, 0.1f);
    kilnRecipe(output, Blocks.NETHERRACK, Items.NETHER_BRICK, 0.1f);
    kilnRecipe(output, Blocks.QUARTZ_BLOCK, Blocks.SMOOTH_QUARTZ, 0.1f);

    // add recipes to smelt slabs and stairs directly for relevant blocks
    // sand
    kilnFurnaceRecipe(output, Blocks.SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE_SLAB, 0.1f);
    kilnFurnaceRecipe(output, Blocks.SANDSTONE_STAIRS, Blocks.SMOOTH_SANDSTONE_STAIRS, 0.1f);
    kilnFurnaceRecipe(output, Blocks.RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE_SLAB, 0.1f);
    kilnFurnaceRecipe(output, Blocks.RED_SANDSTONE_STAIRS, Blocks.SMOOTH_RED_SANDSTONE_STAIRS, 0.1f);
    // rock
    kilnFurnaceRecipe(output, Blocks.COBBLESTONE_SLAB, Blocks.STONE_SLAB, 0.1f);
    kilnFurnaceRecipe(output, Blocks.COBBLESTONE_STAIRS, Blocks.STONE_STAIRS, 0.1f);
    kilnFurnaceRecipe(output, Blocks.STONE_SLAB, Blocks.SMOOTH_STONE_SLAB, 0.1f);
    kilnFurnaceRecipe(output, Blocks.QUARTZ_STAIRS, Blocks.SMOOTH_QUARTZ_STAIRS, 0.1f);
    kilnFurnaceRecipe(output, Blocks.BASALT, Blocks.SMOOTH_BASALT, 0.1f);
  }


  /* Location helpers */

  /**
   * Gets a resource location for Ceramics
   * @param id  Location path
   * @return  Location for Ceramics
   */
  private static ResourceLocation location(String id) {
    return Ceramics.getResource(id);
  }

  /** Gets the ID of an item */
  private static ResourceLocation id(ItemLike item) {
    return Loadables.ITEM.getKey(item.asItem());
  }

  /** Gets the ID of an item as a Ceramics location */
  private static ResourceLocation location(ItemLike item) {
    return location(path(item));
  }

  /** Gets the ID of an item */
  private static String path(ItemLike item) {
    return id(item).getPath();
  }

  /**
   * Gets a resource location as a string for Ceramics
   * @param id  Location path
   * @return  String location for Ceramics
   */
  private static String locationString(String id) {
    return Ceramics.MOD_ID + ":" + id;
  }

  /**
   * Suffixes the resource location path with the given value
   * @param loc     Location to suffix
   * @param suffix  Suffix value
   * @return  Resource location path
   */
  private static ResourceLocation suffix(ResourceLocation loc, String suffix) {
    return location(loc.getPath() + suffix);
  }

  /**
   * Suffixes the item's resource location with the given value
   * @param item    Item to suffix
   * @param suffix  Suffix value
   * @return  Resource location path
   */
  private static ResourceLocation suffix(ItemLike item, String suffix) {
    return suffix(id(item.asItem()), suffix);
  }


  /* Enum blocks */
  /** Map of color to glazed terracotta types */
  private static final Map<DyeColor,Block> GLAZED_TERRACOTTA;
  static {
    EnumMap<DyeColor,Block> map = new EnumMap<>(DyeColor.class);
    map.put(DyeColor.WHITE,      Blocks.WHITE_GLAZED_TERRACOTTA);
    map.put(DyeColor.ORANGE,     Blocks.ORANGE_GLAZED_TERRACOTTA);
    map.put(DyeColor.MAGENTA,    Blocks.MAGENTA_GLAZED_TERRACOTTA);
    map.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
    map.put(DyeColor.YELLOW,     Blocks.YELLOW_GLAZED_TERRACOTTA);
    map.put(DyeColor.LIME,       Blocks.LIME_GLAZED_TERRACOTTA);
    map.put(DyeColor.PINK,       Blocks.PINK_GLAZED_TERRACOTTA);
    map.put(DyeColor.GRAY,       Blocks.GRAY_GLAZED_TERRACOTTA);
    map.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA);
    map.put(DyeColor.CYAN,       Blocks.CYAN_GLAZED_TERRACOTTA);
    map.put(DyeColor.PURPLE,     Blocks.PURPLE_GLAZED_TERRACOTTA);
    map.put(DyeColor.BLUE,       Blocks.BLUE_GLAZED_TERRACOTTA);
    map.put(DyeColor.BROWN,      Blocks.BROWN_GLAZED_TERRACOTTA);
    map.put(DyeColor.GREEN,      Blocks.GREEN_GLAZED_TERRACOTTA);
    map.put(DyeColor.RED,        Blocks.RED_GLAZED_TERRACOTTA);
    map.put(DyeColor.BLACK,      Blocks.BLACK_GLAZED_TERRACOTTA);
    GLAZED_TERRACOTTA = map;
  }


  /* Iteration helpers */

  /**
   * Runs the consumer once for each of block, slab, stairs, and wall
   * @param input      Recipe input for consumer
   * @param output     Recipe output for consumer
   * @param consumer   Consumer to create recipes
   */
  private void eachBuilding(WallBuildingBlockObject input, WallBuildingBlockObject output, BiConsumer<ItemLike,ItemLike> consumer) {
    consumer.accept(input.asItem(), output.asItem());
    consumer.accept(input.getSlab(), output.getSlab());
    consumer.accept(input.getStairs(), output.getStairs());
    consumer.accept(input.getWall(), output.getWall());
  }

  /**
   * Registers generic building block recipes
   * @param consumer  Recipe consumer
   * @param building  Building object instance
   */
  private void registerSlabStairWall(RecipeOutput consumer, WallBuildingBlockObject building) {
    Item item = building.asItem();
    ResourceLocation location = id(item);
    Criterion<?> hasBuilding = inventoryTrigger(ItemPredicate.Builder.item().of(item).build());
    Ingredient ingredient = Ingredient.of(item);

    // slab
    ItemLike slab = building.getSlab();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, slab, 6)
        .define('B', item)
        .pattern("BBB")
        .unlockedBy("has_item", hasBuilding)
        .group(id(slab.asItem()).toString())
        .save(consumer, suffix(location, "_slab_crafting"));
    SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, slab, 2)
                           .unlockedBy("has_item", hasBuilding)
                           .save(consumer, suffix(location, "_slab_stonecutter"));

    // stairs
    ItemLike stairs = building.getStairs();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, stairs, 4)
        .define('B', item)
        .pattern("B  ")
        .pattern("BB ")
        .pattern("BBB")
        .unlockedBy("has_item", hasBuilding)
        .group(id(stairs).toString())
        .save(consumer, suffix(location, "_stairs_crafting"));
    SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, stairs)
                           .unlockedBy("has_item", hasBuilding)
                           .save(consumer, suffix(location, "_stairs_stonecutter"));

    // wall
    ItemLike wall = building.getWall();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wall, 6)
        .define('B', item)
        .pattern("BBB")
        .pattern("BBB")
        .unlockedBy("has_item", hasBuilding)
        .save(consumer, suffix(location, "_wall_crafting"));
    SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, wall)
                           .unlockedBy("has_item", hasBuilding)
                           .save(consumer, suffix(location, "_wall_stonecutter"));

    // block from slab, its bricks so its easy
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, item)
                       .define('B', slab)
                       .pattern("B")
                       .pattern("B")
                       .unlockedBy("has_item", has(slab))
                       .group(location.toString())
                       .save(consumer, suffix(location, "_from_slab"));
  }

  /**
   * Add recipes for surrouding an ingredient with a brick to get another brick
   * @param consumer    Recipe consumer
   * @param from        Input bricks
   * @param ingredient  Ingredient to transform
   * @param to          Output bricks
   * @param name        Recipe name
   */
  private void addBrickRecipe(RecipeOutput consumer, WallBuildingBlockObject from, Item ingredient, WallBuildingBlockObject to, String name) {
    Criterion<?> criteria = has(ingredient);
    eachBuilding(from, to, (input, output) ->
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, output, 8)
                         .define('B', input)
                         .define('i', ingredient)
                         .pattern("BBB")
                         .pattern("BiB")
                         .pattern("BBB")
                         .unlockedBy("has_" + name, criteria)
                         .group(id(output).toString())
                         .save(consumer, suffix(output, "_" + name))
    );
  }

  /* Kiln recipes */

  /** Creates a kiln builder for the given inputs. */
  private static KilnRecipeBuilder kilnBuilder(Ingredient input, Criterion<?> criteria, ItemLike result, float experience) {
    return KilnRecipeBuilder.builder(result)
      .requires(input)
      .experience(experience)
      .unlockedBy("has_item", criteria);
  }

  /**
   * Shortcut to add a kiln recipe
   * @param output      Recipe consumer
   * @param input       Recipe input
   * @param criteria    Criteria to unlock the recipe
   * @param result      Recipe output
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private static void kilnRecipe(RecipeOutput output, Ingredient input, Criterion<?> criteria, ItemLike result, float experience, ResourceLocation name) {
    kilnBuilder(input, criteria, result, experience).save(output, name);
  }

  /**
   * Adds a kiln recipe for vanilla item
   * @param output      Recipe consumer
   * @param input       Recipe input item
   * @param result      Recipe output
   * @param experience  Experience earned
   */
  private void kilnRecipe(RecipeOutput output, ItemLike input, ItemLike result, float experience) {
    kilnRecipe(output, Ingredient.of(input), has(input), result, experience, suffix(result, "_kiln"));
  }

  /**
   * Adds a kiln recipe for vanilla item
   * @param output      Recipe consumer
   * @param input       Recipe input tag
   * @param result      Recipe output
   * @param experience  Experience earned
   */
  private void kilnRecipe(RecipeOutput output, TagKey<Item> input, ItemLike result, float experience) {
    kilnRecipe(output, Ingredient.of(input), has(input), result, experience, suffix(result, "_kiln"));
  }

  /**
   * Adds a new recipe to both the kiln and the furnace
   * @param output      Recipe consumer
   * @param input       Recipe input
   * @param criteria    Criteria to unlock the recipe
   * @param result      Recipe output
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private static void kilnFurnaceRecipe(RecipeOutput output, Ingredient input, Criterion<?> criteria, ItemLike result, float experience, ResourceLocation name) {
    kilnBuilder(input, criteria, result, experience)
      .saveSmelting(output, suffix(name, "_smelting"))
      .saveKiln(output, suffix(name, "_kiln"));
  }

  /**
   * Adds a new recipe to both the kiln and the furnace using the result's registry name to name the recipe
   * @param output      Recipe consumer
   * @param input       Recipe item input
   * @param result      Recipe result
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private void kilnFurnaceRecipe(RecipeOutput output, ItemLike input, ItemLike result, float experience, ResourceLocation name) {
    kilnFurnaceRecipe(output, Ingredient.of(input), has(input), result, experience, name);
  }

  /**
   * Adds a new recipe to both the kiln and the furnace using the result's registry name to name the recipe
   * @param output      Recipe consumer
   * @param input       Recipe item input
   * @param result      Recipe result
   * @param experience  Experience earned
   */
  private void kilnFurnaceRecipe(RecipeOutput output, ItemLike input, ItemLike result, float experience) {
    kilnFurnaceRecipe(output, input, result, experience, location(result));
  }

  /**
   * Adds a new recipe to both the kiln and the furnace using the output's registry name to name the recipe
   * @param output      Recipe consumer
   * @param input       Recipe tag input
   * @param result      Recipe output
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private void kilnFurnaceRecipe(RecipeOutput output, TagKey<Item> input, ItemLike result, float experience, ResourceLocation name) {
    kilnFurnaceRecipe(output, Ingredient.of(input), has(input), result, experience, name);
  }

  /**
   * Adds a recipe to repair a terracotta item's cracks
   * @param output    Recipe consumer
   * @param repairable  Repairable item
   */
  private void clayRepair(RecipeOutput output, ItemLike repairable) {
    CrackedClayRepairRecipeBuilder.item(repairable)
      .unlockedBy("has_item", has(repairable))
      .save(output, suffix(repairable, "_repair"));
  }
}
