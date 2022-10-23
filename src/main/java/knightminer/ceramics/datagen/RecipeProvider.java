package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.RainbowPorcelain;
import knightminer.ceramics.recipe.CeramicsTags;
import knightminer.ceramics.recipe.CrackedClayRepairRecipe;
import knightminer.ceramics.recipe.NoNBTIngredient;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {

  /** Vanilla bricks as a building block object */
  private static final WallBuildingBlockObject BRICKS = new WallBuildingBlockObject(new BuildingBlockObject(Blocks.BRICKS, Blocks.BRICK_SLAB, Blocks.BRICK_STAIRS), Blocks.BRICK_WALL);

  public RecipeProvider(DataGenerator gen) {
    super(gen);
  }

  @Override
  public String getName() {
    return "Ceramics Recipes";
  }

  @Override
  protected void buildShapelessRecipes(Consumer<FinishedRecipe> consumer) {
    // recoloring terracotta
    CriterionTriggerInstance terracottaCriteria = has(CeramicsTags.Items.COLORED_TERRACOTTA);
    Registration.TERRACOTTA.forEach((color, item) ->
      ShapedRecipeBuilder.shaped(item, 8)
                         .define('B', CeramicsTags.Items.COLORED_TERRACOTTA)
                         .define('D', color.getTag())
                         .group("stained_terracotta")
                         .pattern("BBB")
                         .pattern("BDB")
                         .pattern("BBB")
                         .unlockedBy("has_terracotta", terracottaCriteria)
                         .save(consumer, location(item.getRegistryName().getPath() + "_recolor"))
    );

    // crafting porcelain
    ShapelessRecipeBuilder.shapeless(Registration.UNFIRED_PORCELAIN, 4)
                          .requires(Tags.Items.GEMS_QUARTZ)
                          .requires(Items.CLAY_BALL)
                          .requires(Items.CLAY_BALL)
                          .requires(Items.CLAY_BALL)
                          .unlockedBy("has_quartz", has(Tags.Items.GEMS_QUARTZ))
                          .save(consumer);

    // unfired porcelain
    ShapedRecipeBuilder.shaped(Registration.UNFIRED_PORCELAIN_BLOCK)
                       .define('b', Registration.UNFIRED_PORCELAIN)
                       .pattern("bb")
                       .pattern("bb")
                       .unlockedBy("has_item", has(Registration.UNFIRED_PORCELAIN))
                       .save(consumer);
    // smelting porcelain
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_PORCELAIN_BLOCK, Registration.PORCELAIN_BLOCK.get(DyeColor.WHITE), 0.3f);

    // colored porcelain
    CriterionTriggerInstance porcelainCriteria = has(Registration.PORCELAIN_BLOCK.get(DyeColor.WHITE));
    Registration.PORCELAIN_BLOCK.forEach((color, item) ->
      ShapedRecipeBuilder.shaped(item, 8)
                         .define('B', CeramicsTags.Items.PORCELAIN)
                         .define('D', color.getTag())
                         .group(locationString("dye_porcelain"))
                         .pattern("BBB")
                         .pattern("BDB")
                         .pattern("BBB")
                         .unlockedBy("has_porcelain", porcelainCriteria)
                         .save(consumer)
    );
    // rainbow porcelain
    kilnFurnaceRecipe(consumer, CeramicsTags.Items.COLORED_PORCELAIN, Registration.RAINBOW_PORCELAIN.get(RainbowPorcelain.RED), 0.1f, location("rainbow_porcelain"));

    // smelt for full rainbow
    CriterionTriggerInstance hasTheRainbow = has(CeramicsTags.Items.RAINBOW_PORCELAIN);
    Registration.RAINBOW_PORCELAIN.forEach((color, item) ->
      SingleItemRecipeBuilder.stonecutting(Ingredient.of(CeramicsTags.Items.RAINBOW_PORCELAIN), item)
                             .unlocks("has_the_rainbow", hasTheRainbow)
                             .save(consumer, item.getRegistryName())
    );

    // bricks
    // vanilla brick block shortcuts
    CriterionTriggerInstance hasClayBrick = has(Items.BRICK);
    ShapedRecipeBuilder.shaped(Items.BRICK_SLAB)
                       .define('b', Items.BRICK)
                       .pattern("bb")
                       .unlockedBy("has_bricks", hasClayBrick)
                       .group(Items.BRICK_SLAB.getRegistryName().getPath())
                       .save(consumer, location("brick_slab_from_bricks"));
    // stairs shortcut
    ShapedRecipeBuilder.shaped(Items.BRICK_STAIRS)
                       .define('b', Items.BRICK)
                       .pattern("b  ")
                       .pattern("bb ")
                       .pattern("bbb")
                       .unlockedBy("has_bricks", hasClayBrick)
                       .group(Items.BRICK_STAIRS.getRegistryName().getPath())
                       .save(consumer, location("brick_stairs_from_bricks"));
    // block from slab
    ShapedRecipeBuilder.shaped(Items.BRICKS)
                       .define('B', Items.BRICK_SLAB)
                       .pattern("B")
                       .pattern("B")
                       .unlockedBy("has_item", has(Items.BRICK_SLAB))
                       .group(Items.BRICKS.getRegistryName().getPath())
                       .save(consumer, location("bricks_from_slab"));

    // dark bricks from smelting bricks
    eachBuilding(BRICKS, Registration.DARK_BRICKS, (input, output) ->
      kilnFurnaceRecipe(consumer, input, output, 0.1f)
    );
    registerSlabStairWall(consumer, Registration.DARK_BRICKS);

    // magma bricks from lava bucket
    addBrickRecipe(consumer, BRICKS, Items.LAVA_BUCKET, Registration.LAVA_BRICKS, "lava");
    registerSlabStairWall(consumer, Registration.LAVA_BRICKS);

    // dragon bricks from dragon's breath
    addBrickRecipe(consumer, BRICKS, Items.DRAGON_BREATH, Registration.DRAGON_BRICKS, "dragon");
    registerSlabStairWall(consumer, Registration.DRAGON_BRICKS);

    // porcelain bricks
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_PORCELAIN, Registration.PORCELAIN_BRICK, 0.3f);
    // using bricks
    CriterionTriggerInstance hasBricks = has(Registration.PORCELAIN_BRICK);
    ShapedRecipeBuilder.shaped(Registration.PORCELAIN_BRICKS)
                       .define('b', Registration.PORCELAIN_BRICK)
                       .pattern("bb")
                       .pattern("bb")
                       .unlockedBy("has_bricks", hasBricks)
                       .group(Registration.PORCELAIN_BRICK.getRegistryName().toString())
                       .save(consumer);
    // slab shortcut
    ItemLike porcelainSlab = Registration.PORCELAIN_BRICKS.getSlab();
    ShapedRecipeBuilder.shaped(porcelainSlab)
                       .define('b', Registration.PORCELAIN_BRICK)
                       .pattern("bb")
                       .unlockedBy("has_bricks", hasBricks)
                       .group(porcelainSlab.asItem().getRegistryName().toString())
                       .save(consumer, suffix(porcelainSlab, "_from_bricks"));
    // stairs shortcut
    ItemLike porcelainStairs = Registration.PORCELAIN_BRICKS.getStairs();
    ShapedRecipeBuilder.shaped(porcelainStairs)
                       .define('b', Registration.PORCELAIN_BRICK)
                       .pattern("b  ")
                       .pattern("bb ")
                       .pattern("bbb")
                       .unlockedBy("has_bricks", hasBricks)
                       .group(porcelainStairs.asItem().getRegistryName().toString())
                       .save(consumer, suffix(porcelainStairs, "_from_bricks"));
    registerSlabStairWall(consumer, Registration.PORCELAIN_BRICKS);

    // golden bricks
    addBrickRecipe(consumer, Registration.PORCELAIN_BRICKS, Items.GOLD_NUGGET, Registration.GOLDEN_BRICKS, "gold");
    registerSlabStairWall(consumer, Registration.GOLDEN_BRICKS);

    // marine bricks
    addBrickRecipe(consumer, Registration.PORCELAIN_BRICKS, Items.PRISMARINE_SHARD, Registration.MARINE_BRICKS, "prismarine");
    registerSlabStairWall(consumer, Registration.MARINE_BRICKS);

    // monochrome uses ink, not black dye intentionally
    addBrickRecipe(consumer, Registration.PORCELAIN_BRICKS, Items.INK_SAC, Registration.MONOCHROME_BRICKS, "ink");
    registerSlabStairWall(consumer, Registration.MONOCHROME_BRICKS);

    // rainbow
    eachBuilding(Registration.PORCELAIN_BRICKS, Registration.RAINBOW_BRICKS, (input, output) ->
      kilnFurnaceRecipe(consumer, input, output, 0.1f)
    );
    registerSlabStairWall(consumer, Registration.RAINBOW_BRICKS);

    // buckets
    // unfired
    ShapedRecipeBuilder.shaped(Registration.UNFIRED_CLAY_BUCKET)
                       .define('c', Items.CLAY_BALL)
                       .pattern("c c")
                       .pattern(" c ")
                       .unlockedBy("has_clay", has(Items.CLAY_BALL))
                       .save(consumer);
    // fired
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_CLAY_BUCKET, Registration.CLAY_BUCKET, 0.3f);
    // cracked
    kilnFurnaceRecipe(consumer, Registration.CLAY_BUCKET, Registration.CRACKED_CLAY_BUCKET, 0.2f);

    // cistern
    ShapedRecipeBuilder.shaped(Registration.CLAY_CISTERN, 2)
                       .define('c', Items.CLAY_BALL)
                       .pattern("c c")
                       .pattern("c c")
                       .pattern("c c")
                       .unlockedBy("has_clay", has(Items.CLAY_BALL))
                       .save(consumer);
    ShapedRecipeBuilder.shaped(Registration.UNFIRED_CISTERN, 2)
                       .define('c', Registration.UNFIRED_PORCELAIN)
                       .pattern("c c")
                       .pattern("c c")
                       .pattern("c c")
                       .unlockedBy("has_clay", has(Registration.UNFIRED_PORCELAIN))
                       .save(consumer);
    // fired
    kilnFurnaceRecipe(consumer, Registration.CLAY_CISTERN, Registration.TERRACOTTA_CISTERN, 0.3f);
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_CISTERN, Registration.PORCELAIN_CISTERN.get(DyeColor.WHITE), 0.3f);
    // repair
    clayRepair(consumer, Registration.TERRACOTTA_CISTERN);
    // colored
    Registration.COLORED_CISTERN.forEach((color, block) -> {
      // craft
      ShapedRecipeBuilder.shaped(block, 4)
                         .define('c', new NoNBTIngredient(Ingredient.of(CeramicsTags.Items.TERRACOTTA_CISTERNS)))
                         .define('d', color.getTag())
                         .pattern(" c ")
                         .pattern("cdc")
                         .pattern(" c ")
                         .unlockedBy("has_cistern", has(Registration.TERRACOTTA_CISTERN))
                         .group(Ceramics.locationName("colored_cisterns"))
                         .save(consumer);
      // repair
      clayRepair(consumer, block);
    });
    Registration.PORCELAIN_CISTERN.forEach((color, block) ->
      ShapedRecipeBuilder.shaped(block, 4)
                         .define('c', CeramicsTags.Items.PORCELAIN_CISTERNS)
                         .define('d', color.getTag())
                         .pattern(" c ")
                         .pattern("cdc")
                         .pattern(" c ")
                         .unlockedBy("has_cistern", has(Registration.PORCELAIN_CISTERN.get(DyeColor.WHITE)))
                         .group(Ceramics.locationName("porcelain_cisterns"))
                         .save(consumer)
    );

    // gauge
    ShapedRecipeBuilder.shaped(Registration.TERRACOTTA_GAUGE, 4)
                       .define('b', Items.BRICK)
                       .define('p', Tags.Items.GLASS_PANES_COLORLESS)
                       .pattern(" b ")
                       .pattern("bpb")
                       .pattern(" b ")
                       .unlockedBy("has_cistern", has(Registration.TERRACOTTA_CISTERN))
                       .save(consumer);
    ShapedRecipeBuilder.shaped(Registration.PORCELAIN_GAUGE, 4)
                       .define('b', Registration.PORCELAIN_BRICK)
                       .define('p', Tags.Items.GLASS_PANES_COLORLESS)
                       .pattern(" b ")
                       .pattern("bpb")
                       .pattern(" b ")
                       .unlockedBy("has_cistern", has(Registration.PORCELAIN_CISTERN.get(DyeColor.WHITE)))
                       .save(consumer);


    // faucet
    ShapedRecipeBuilder.shaped(Registration.CLAY_FAUCET, 2)
                       .define('c', Items.CLAY_BALL)
                       .pattern("ccc")
                       .pattern(" c ")
                       .unlockedBy("has_cistern", has(Items.CLAY_BALL))
                       .save(consumer);
    ShapedRecipeBuilder.shaped(Registration.UNFIRED_FAUCET, 2)
                       .define('c', Registration.UNFIRED_PORCELAIN)
                       .pattern("ccc")
                       .pattern(" c ")
                       .unlockedBy("has_cistern", has(Registration.UNFIRED_PORCELAIN))
                       .save(consumer);
    kilnFurnaceRecipe(consumer, Registration.CLAY_FAUCET, Registration.TERRACOTTA_FAUCET, 0.3f);
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_FAUCET, Registration.PORCELAIN_FAUCET, 0.3f);
    clayRepair(consumer, Registration.TERRACOTTA_FAUCET);

    // channel
    ShapedRecipeBuilder.shaped(Registration.CLAY_CHANNEL, 3)
                       .define('p', Items.CLAY_BALL)
                       .pattern("ppp")
                       .pattern("ppp")
                       .unlockedBy("has_cistern", has(Items.CLAY_BALL))
                       .save(consumer);
    ShapedRecipeBuilder.shaped(Registration.UNFIRED_CHANNEL, 3)
                       .define('p', Registration.UNFIRED_PORCELAIN)
                       .pattern("ppp")
                       .pattern("ppp")
                       .unlockedBy("has_cistern", has(Registration.UNFIRED_PORCELAIN))
                       .save(consumer);
    kilnFurnaceRecipe(consumer, Registration.CLAY_CHANNEL, Registration.TERRACOTTA_CHANNEL, 0.3f);
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_CHANNEL, Registration.PORCELAIN_CHANNEL, 0.3f);
    clayRepair(consumer, Registration.TERRACOTTA_CHANNEL);

    // armor
    // clay plates
    ShapedRecipeBuilder.shaped(Registration.UNFIRED_CLAY_PLATE, 2)
                       .define('c', Items.CLAY_BALL)
                       .pattern("cc")
                       .unlockedBy("has_clay", has(Items.CLAY_BALL))
                       .save(consumer);
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_CLAY_PLATE, Registration.CLAY_PLATE, 0.3f);

    // helmet
    CriterionTriggerInstance hasClayPlate = has(CeramicsTags.Items.BRICK_PLATES);
    ShapedRecipeBuilder.shaped(Registration.CLAY_HELMET)
                       .define('c', CeramicsTags.Items.BRICK_PLATES)
                       .pattern("ccc")
                       .pattern("c c")
                       .unlockedBy("has_plate", hasClayPlate)
                       .save(consumer);
    // chestplate
    ShapedRecipeBuilder.shaped(Registration.CLAY_CHESTPLATE)
                       .define('c', CeramicsTags.Items.BRICK_PLATES)
                       .pattern("c c")
                       .pattern("ccc")
                       .pattern("ccc")
                       .unlockedBy("has_plate", hasClayPlate)
                       .save(consumer);
    // leggings
    ShapedRecipeBuilder.shaped(Registration.CLAY_LEGGINGS)
                       .define('c', CeramicsTags.Items.BRICK_PLATES)
                       .pattern("ccc")
                       .pattern("c c")
                       .pattern("c c")
                       .unlockedBy("has_plate", hasClayPlate)
                       .save(consumer);
    // boots
    ShapedRecipeBuilder.shaped(Registration.CLAY_BOOTS)
                       .define('c', CeramicsTags.Items.BRICK_PLATES)
                       .pattern("c c")
                       .pattern("c c")
                       .unlockedBy("has_plate", hasClayPlate)
                       .save(consumer);

    // clay uncrafting
    ShapelessRecipeBuilder.shapeless(Items.CLAY_BALL, 4)
                          .requires(Items.CLAY)
                          .unlockedBy("has_unfired", has(Items.CLAY))
                          .group(locationString("clay_uncrafting"))
                          .save(consumer, location("clay_uncrafting_4"));
    ShapelessRecipeBuilder.shapeless(Items.CLAY_BALL, 3)
                          .requires(Ingredient.of(Registration.UNFIRED_CLAY_BUCKET, Registration.CLAY_CISTERN))
                          .unlockedBy("has_unfired", has(Registration.UNFIRED_CLAY_BUCKET))
                          .group(locationString("clay_uncrafting"))
                          .save(consumer, location("clay_uncrafting_3"));
    ShapelessRecipeBuilder.shapeless(Items.CLAY_BALL, 2)
                          .requires(Ingredient.of(Registration.CLAY_FAUCET, Registration.CLAY_CHANNEL))
                          .unlockedBy("has_unfired", has(Registration.CLAY_FAUCET))
                          .group(locationString("clay_uncrafting"))
                          .save(consumer, location("clay_uncrafting_2"));
    ShapelessRecipeBuilder.shapeless(Items.CLAY_BALL, 1)
                          .requires(Ingredient.of(Registration.UNFIRED_CLAY_PLATE))
                          .unlockedBy("has_unfired", has(Registration.UNFIRED_CLAY_PLATE))
                          .group(locationString("clay_uncrafting"))
                          .save(consumer, location("clay_uncrafting"));
    // porcelain uncrafting
    ShapelessRecipeBuilder.shapeless(Registration.UNFIRED_PORCELAIN, 4)
                          .requires(Registration.UNFIRED_PORCELAIN_BLOCK)
                          .unlockedBy("has_unfired", has(Registration.UNFIRED_PORCELAIN_BLOCK))
                          .group(locationString("porcelain_uncrafting"))
                          .save(consumer, location("porcelain_uncrafting_4"));
    ShapelessRecipeBuilder.shapeless(Registration.UNFIRED_PORCELAIN, 2)
                          .requires(Ingredient.of(Registration.UNFIRED_FAUCET, Registration.UNFIRED_CHANNEL))
                          .unlockedBy("has_unfired", has(Registration.UNFIRED_FAUCET))
                          .group(locationString("porcelain_uncrafting"))
                          .save(consumer, location("porcelain_uncrafting_2"));
    ShapelessRecipeBuilder.shapeless(Registration.UNFIRED_PORCELAIN, 3)
                          .requires(Registration.UNFIRED_CISTERN)
                          .unlockedBy("has_unfired", has(Registration.UNFIRED_CISTERN))
                          .group(locationString("porcelain_uncrafting"))
                          .save(consumer, location("porcelain_uncrafting_3"));
    // compat, wish there was a better way to do this
    ShapedRecipeBuilder.shaped(Blocks.CAKE)
                       .define('M', CeramicsTags.Items.MILK_BUCKETS)
                       .define('S', Items.SUGAR)
                       .define('W', Items.WHEAT)
                       .define('E', Items.EGG)
                       .pattern("MMM")
                       .pattern("SES")
                       .pattern("WWW")
                       .unlockedBy("has_egg", has(Items.EGG))
                       .save(consumer, location("cake"));

    // kiln - crafting
    ShapedRecipeBuilder.shaped(Registration.KILN)
                       .define('B', Items.BRICK)
                       .define('F', Items.FURNACE)
                       .define('H', Items.BRICKS)
                       .pattern("BBB")
                       .pattern("BFB")
                       .pattern("HHH")
                       .unlockedBy("has_brick", has(Items.BRICK))
                       .save(consumer);

    // add vanilla furnace recipes to the kiln
    // clay
    kilnRecipe(consumer, Items.CLAY_BALL, Items.BRICK, 0.3f);
    kilnRecipe(consumer, Blocks.CLAY, Blocks.TERRACOTTA, 0.3f);
    Registration.TERRACOTTA.forEach((color, input) ->
      kilnRecipe(consumer, input, GLAZED_TERRACOTTA.get(color), 0.1f)
    );
    // sand and glass
    kilnRecipe(consumer, ItemTags.SAND, Blocks.GLASS, 0.1f);
    kilnRecipe(consumer, Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, 0.1f);
    kilnRecipe(consumer, Blocks.RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE, 0.1f);
    // rock
    kilnRecipe(consumer, Blocks.COBBLESTONE, Blocks.STONE, 0.1f);
    kilnRecipe(consumer, Blocks.STONE, Blocks.SMOOTH_STONE, 0.1f);
    kilnRecipe(consumer, Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, 0.1f);
    kilnRecipe(consumer, Blocks.NETHERRACK, Items.NETHER_BRICK, 0.1f);
    kilnRecipe(consumer, Blocks.QUARTZ_BLOCK, Blocks.SMOOTH_QUARTZ, 0.1f);

    // add recipes to smelt slabs and stairs directly for relevant blocks
    // sand
    kilnFurnaceRecipe(consumer, Blocks.SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE_SLAB, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.SANDSTONE_STAIRS, Blocks.SMOOTH_SANDSTONE_STAIRS, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE_SLAB, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.RED_SANDSTONE_STAIRS, Blocks.SMOOTH_RED_SANDSTONE_STAIRS, 0.1f);
    // rock
    kilnFurnaceRecipe(consumer, Blocks.COBBLESTONE_SLAB, Blocks.STONE_SLAB, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.COBBLESTONE_STAIRS, Blocks.STONE_STAIRS, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.STONE_SLAB, Blocks.SMOOTH_STONE_SLAB, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.QUARTZ_STAIRS, Blocks.SMOOTH_QUARTZ_STAIRS, 0.1f);
  }


  /* Location helpers */

  /**
   * Gets a resource location for Ceramics
   * @param id  Location path
   * @return  Location for Ceramics
   */
  private static ResourceLocation location(String id) {
    return new ResourceLocation(Ceramics.MOD_ID, id);
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
    return new ResourceLocation(loc.getNamespace(), loc.getPath() + suffix);
  }

  /**
   * Suffixes the item's resource location with the given value
   * @param item    Item to suffix
   * @param suffix  Suffix value
   * @return  Resource location path
   */
  private static ResourceLocation suffix(ItemLike item, String suffix) {
    return suffix(item.asItem().getRegistryName(), suffix);
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
  private void registerSlabStairWall(Consumer<FinishedRecipe> consumer, WallBuildingBlockObject building) {
    Item item = building.asItem();
    ResourceLocation location = item.getRegistryName();
    CriterionTriggerInstance hasBuilding = inventoryTrigger(ItemPredicate.Builder.item().of(item).build());
    Ingredient ingredient = Ingredient.of(item);

    // slab
    ItemLike slab = building.getSlab();
    ShapedRecipeBuilder.shaped(slab, 6)
        .define('B', item)
        .pattern("BBB")
        .unlockedBy("has_item", hasBuilding)
        .group(slab.asItem().getRegistryName().toString())
        .save(consumer, suffix(location, "_slab_crafting"));
    SingleItemRecipeBuilder.stonecutting(ingredient, slab, 2)
                           .unlocks("has_item", hasBuilding)
                           .save(consumer, suffix(location, "_slab_stonecutter"));

    // stairs
    ItemLike stairs = building.getStairs();
    ShapedRecipeBuilder.shaped(stairs, 4)
        .define('B', item)
        .pattern("B  ")
        .pattern("BB ")
        .pattern("BBB")
        .unlockedBy("has_item", hasBuilding)
        .group(stairs.asItem().getRegistryName().toString())
        .save(consumer, suffix(location, "_stairs_crafting"));
    SingleItemRecipeBuilder.stonecutting(ingredient, stairs)
                           .unlocks("has_item", hasBuilding)
                           .save(consumer, suffix(location, "_stairs_stonecutter"));

    // wall
    ItemLike wall = building.getWall();
    ShapedRecipeBuilder.shaped(wall, 6)
        .define('B', item)
        .pattern("BBB")
        .pattern("BBB")
        .unlockedBy("has_item", hasBuilding)
        .save(consumer, suffix(location, "_wall_crafting"));
    SingleItemRecipeBuilder.stonecutting(ingredient, wall)
                           .unlocks("has_item", hasBuilding)
                           .save(consumer, suffix(location, "_wall_stonecutter"));

    // block from slab, its bricks so its easy
    ShapedRecipeBuilder.shaped(item)
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
  private void addBrickRecipe(Consumer<FinishedRecipe> consumer, WallBuildingBlockObject from, Item ingredient, WallBuildingBlockObject to, String name) {
    CriterionTriggerInstance criteria = has(ingredient);
    eachBuilding(from, to, (input, output) ->
      ShapedRecipeBuilder.shaped(output, 8)
                         .define('B', input)
                         .define('i', ingredient)
                         .pattern("BBB")
                         .pattern("BiB")
                         .pattern("BBB")
                         .unlockedBy("has_" + name, criteria)
                         .group(output.asItem().getRegistryName().toString())
                         .save(consumer, suffix(output, "_" + name))
    );
  }

  /* Kiln recipes */

  /**
   * Creates a kiln recipe builder
   * @param input       Recipe input
   * @param output      Recipe output
   * @param experience  Experience earned
   * @param cookTime    Cooking time
   * @return            Builder result
   */
  private static SimpleCookingRecipeBuilder kilnRecipe(Ingredient input, ItemLike output, float experience, int cookTime) {
    return SimpleCookingRecipeBuilder.cooking(input, output, experience, cookTime, Registration.KILN_SERIALIZER.get());
  }

  /**
   * Shortcut to add a kiln recipe
   * @param consumer    Recipe consumer
   * @param input       Recipe input
   * @param criteria    Criteria to unlock the recipe
   * @param output      Recipe output
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private static void kilnRecipe(Consumer<FinishedRecipe> consumer, Ingredient input, CriterionTriggerInstance criteria, ItemLike output, float experience, ResourceLocation name) {
    kilnRecipe(input, output, experience, 100)
        .unlockedBy("has_item", criteria)
        .save(consumer, name);
  }

  /**
   * Adds a kiln recipe for vanilla item
   * @param consumer    Recipe consumer
   * @param input       Recipe input item
   * @param output      Recipe output
   * @param experience  Experience earned
   */
  private void kilnRecipe(Consumer<FinishedRecipe> consumer, ItemLike input, ItemLike output, float experience) {
    kilnRecipe(consumer, Ingredient.of(input), has(input), output, experience, location(output.asItem().getRegistryName().getPath() + "_kiln"));
  }

  /**
   * Adds a kiln recipe for vanilla item
   * @param consumer    Recipe consumer
   * @param input       Recipe input tag
   * @param output      Recipe output
   * @param experience  Experience earned
   */
  private void kilnRecipe(Consumer<FinishedRecipe> consumer, Tag<Item> input, ItemLike output, float experience) {
    kilnRecipe(consumer, Ingredient.of(input), has(input), output, experience, location(output.asItem().getRegistryName().getPath() + "_kiln"));
  }

  /**
   * Adds a new recipe to both the kiln and the furnace
   * @param consumer    Recipe consumer
   * @param input       Recipe input
   * @param criteria    Criteria to unlock the recipe
   * @param output      Recipe output
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private static void kilnFurnaceRecipe(Consumer<FinishedRecipe> consumer, Ingredient input, CriterionTriggerInstance criteria, ItemLike output, float experience, ResourceLocation name) {
    SimpleCookingRecipeBuilder.smelting(input, output, experience, 200)
                        .unlockedBy("has_item", criteria)
                        .save(consumer, suffix(name, "_smelting"));
    kilnRecipe(consumer, input, criteria, output, experience, suffix(name, "_kiln"));
  }

  /**
   * Adds a new recipe to both the kiln and the furnace using the output's registry name to name the recipe
   * @param consumer    Recipe consumer
   * @param input       Recipe item input
   * @param output      Recipe output
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private void kilnFurnaceRecipe(Consumer<FinishedRecipe> consumer, ItemLike input, ItemLike output, float experience, ResourceLocation name) {
    kilnFurnaceRecipe(consumer, Ingredient.of(input), has(input), output, experience, name);
  }

  /**
   * Adds a new recipe to both the kiln and the furnace using the output's registry name to name the recipe
   * @param consumer    Recipe consumer
   * @param input       Recipe item input
   * @param output      Recipe output
   * @param experience  Experience earned
   */
  private void kilnFurnaceRecipe(Consumer<FinishedRecipe> consumer, ItemLike input, ItemLike output, float experience) {
    kilnFurnaceRecipe(consumer, input, output, experience, location(output.asItem().getRegistryName().getPath()));
  }

  /**
   * Adds a new recipe to both the kiln and the furnace using the output's registry name to name the recipe
   * @param consumer    Recipe consumer
   * @param input       Recipe tag input
   * @param output      Recipe output
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private void kilnFurnaceRecipe(Consumer<FinishedRecipe> consumer, Tag<Item> input, ItemLike output, float experience, ResourceLocation name) {
    kilnFurnaceRecipe(consumer, Ingredient.of(input), has(input), output, experience, name);
  }

  /**
   * Adds a recipe to repair a terracotta item's cracks
   * @param consumer    Recipe consumer
   * @param repairable  Repairable item
   */
  private void clayRepair(Consumer<FinishedRecipe> consumer, ItemLike repairable) {
    consumer.accept(new CrackedClayRepairRecipe.FinishedRecipe(suffix(repairable, "_repair"), repairable, Ingredient.of(CeramicsTags.Items.TERRACOTTA_CRACK_REPAIR), has(repairable)));
  }
}
